/*
 *  Copyright (c) 2012 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.lingyang.camera.util;

//package org.webrtc.videoengine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager;

import com.lingyang.base.utils.CLog;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Exchanger;

// Wrapper for android Camera, with support for direct local preview rendering.
// Threading notes: this class is called from ViE C++ code, and from Camera &
// SurfaceHolder Java callbacks.  Since these calls happen on different threads,
// the entry points to this class are all synchronized.  This shouldn't present
// a performance bottleneck because only onPreviewFrame() is called more than
// once (and is called serially on a single thread), so the lock should be
// uncontended.  Note that each of these synchronized methods must check
// |camera| for null to account for having possibly waited for stopCapture() to
// complete.
@SuppressLint("NewApi")
public class VideoCaptureAndroid implements PreviewCallback, Callback {

    private final int id;
    private final Camera.CameraInfo mCameraInfo;
    private final long native_capturer; // |VideoCaptureAndroid*| in C++.
    // Arbitrary queue depth. Higher number means more memory allocated & held,
    // lower number means more sensitivity to processing time in the client (and
    // potentially stalling the capturer if it runs out of buffers to write to).
    private final int numCaptureBuffers = 3;
    private SurfaceHolder mSurfaceHolder;
    private Camera camera; // Only non-null while capturing.
    private CameraThread mCameraThread;
    private Handler mCameraThreadHandler;
    private Context mContext;
    private SurfaceTexture cameraSurfaceTexture;
    private int[] cameraGlTextures = null;
    private double averageDurationMs;
    private long lastCaptureTimeMs;
    private int frameCount;
    private int frameDropRatio;
    private int mDegrees;

    public VideoCaptureAndroid(int id, long native_capturer) {
        this.id = id;
        this.native_capturer = native_capturer;
        // this.context = GetContext();
        this.mCameraInfo = new Camera.CameraInfo();
        // this.info.facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        Camera.getCameraInfo(id, mCameraInfo);
    }

    // Requests future capturers to send their frames to |localPreview|
    // directly.
    public void setLocalPreview(SurfaceHolder surfaceHolder) {
        // It is a gross hack that this is a class-static. Doing it right would
        // mean plumbing this through the C++ API and using it from
        // webrtc/examples/android/media_demo's MediaEngine class.
        mSurfaceHolder = surfaceHolder;
    }

    // Return the global application context.
//    private native Context GetContext();

    // Called by native code. Returns true if capturer is started.
    //
    // Note that this actually opens the camera, and Camera callbacks run on the
    // thread that calls open(), so this is done on the CameraThread. Since ViE
    // API needs a synchronous success return value we wait for the result.
    public boolean startCapture(final int width, final int height) {
        if (mCameraThread != null || mCameraThreadHandler != null) {
            throw new RuntimeException("Camera thread already started!");
        }
        Exchanger<Handler> handlerExchanger = new Exchanger<Handler>();
        mCameraThread = new CameraThread(handlerExchanger);
        mCameraThread.start();
        mCameraThreadHandler = exchange(handlerExchanger, null);

        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mCameraThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                startCaptureOnCameraThread(width, height, result);
            }
        });
        boolean startResult = exchange(result, false); // |false| is a dummy
        // value.
        return startResult;
    }

    // Exchanges |value| with |exchanger|, converting InterruptedExceptions to
    // RuntimeExceptions (since we expect never to see these).
    private <T> T exchange(Exchanger<T> exchanger, T value) {
        try {
            return exchanger.exchange(value);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void startCaptureOnCameraThread(int width, int height, Exchanger<Boolean> result) {
        Throwable error = null;
        try {
            camera = Camera.open(id);
            if (mSurfaceHolder != null) {
                // mSurfaceHolder.addCallback(this);
                if (mSurfaceHolder.getSurface() != null && mSurfaceHolder.getSurface().isValid()) {
                    camera.setPreviewDisplay(mSurfaceHolder);
                    CLog.v("setPreviewRotation:--getDeviceOrientation" + getDeviceOrientation());
                }
            } else {
                // No local renderer (we only care about onPreviewFrame()
                // buffers, not a
                // directly-displayed UI element). Camera won't capture without
                // setPreview{Texture,Display}, so we create a SurfaceTexture
                // and hand
                // it over to Camera, but never listen for frame-ready
                // callbacks,
                // and never call updateTexImage on it.
                try {
                    cameraGlTextures = new int[1];
                    // Generate one texture pointer and bind it as an external
                    // texture.
                    GLES20.glGenTextures(1, cameraGlTextures, 0);
                    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraGlTextures[0]);
                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                    cameraSurfaceTexture = new SurfaceTexture(cameraGlTextures[0]);
                    cameraSurfaceTexture.setOnFrameAvailableListener(null);
                    camera.setPreviewTexture(cameraSurfaceTexture);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            CLog.v("Camera orientation: " + mCameraInfo.orientation + " .Device orientation: "
                    + getDeviceOrientation());
            Parameters parameters = camera.getParameters();
            CLog.v("isVideoStabilizationSupported: " + parameters.isVideoStabilizationSupported());
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(true);
            }
            parameters.setPictureSize(width, height);
            parameters.setPreviewSize(width, height);

            // Check if requested fps range is supported by camera,
            // otherwise calculate frame drop ratio.
            List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
            int[] range = supportedFpsRanges.get(0);
            frameDropRatio = 1;
            int min_mfps = range[Parameters.PREVIEW_FPS_MIN_INDEX];
            // int max_mfps = min_mfps +
            // (range[Parameters.PREVIEW_FPS_MAX_INDEX] - min_mfps) / 3;
            int max_mfps = range[Parameters.PREVIEW_FPS_MAX_INDEX];
            // for (int i = 0; i < supportedFpsRanges.size(); i++) {
            // int[] range = supportedFpsRanges.get(i);
            // if (range[Parameters.PREVIEW_FPS_MIN_INDEX] == min_mfps
            // && range[Parameters.PREVIEW_FPS_MAX_INDEX] == max_mfps) {
            // frameDropRatio = 1;
            // break;
            // }
            // if (range[Parameters.PREVIEW_FPS_MIN_INDEX] % min_mfps == 0
            // && range[Parameters.PREVIEW_FPS_MAX_INDEX] % max_mfps == 0) {
            // int dropRatio = range[Parameters.PREVIEW_FPS_MAX_INDEX] /
            // max_mfps;
            // frameDropRatio = Math.min(dropRatio, frameDropRatio);
            // }
            // }
            // if (frameDropRatio == Integer.MAX_VALUE) {
            // CLog.v("Can not find camera fps range");
            // error = new RuntimeException("Can not find camera fps range");
            // exchange(result, false);
            // return;
            // }
            // if (frameDropRatio > 1) {
            // CLog.v("Frame dropper is enabled. Ratio: " + frameDropRatio);
            // }
            //
            // min_mfps *= frameDropRatio;
            // max_mfps *= frameDropRatio;
            CLog.v("Camera preview mfps range: " + min_mfps + " - " + max_mfps);
            parameters.setPreviewFpsRange(min_mfps, max_mfps);
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            int format = ImageFormat.NV21;
            parameters.setPreviewFormat(format);
            camera.setParameters(parameters);
            // camera.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
            //
            // @Override
            // public void onAutoFocusMoving(boolean start, Camera camera) {
            //
            // }
            // })
            int bufSize = width * height * ImageFormat.getBitsPerPixel(format) / 8;
            for (int i = 0; i < numCaptureBuffers; i++) {
                camera.addCallbackBuffer(new byte[bufSize]);
            }
            camera.setPreviewCallbackWithBuffer(this);
            frameCount = 0;
            averageDurationMs = 1000000.0f / (max_mfps / frameDropRatio);

            List<Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
            for (Size size : previewSizes) {
                CLog.v("size---:" + size.width + "-" + size.height);
            }
            setCameraDisplayOrientation();
            camera.startPreview();
            exchange(result, true);
            CLog.v("setPreviewRotation");
            return;
        } catch (IOException e) {
            error = e;
        } catch (RuntimeException e) {
            error = e;
        }
        CLog.v("startCapture failed" + error);
        if (camera != null) {
            Exchanger<Boolean> resultDropper = new Exchanger<Boolean>();
            stopCaptureOnCameraThread(resultDropper);
            exchange(resultDropper, false);
        }
        exchange(result, false);
        return;
    }

    private int getDeviceOrientation() {
        int orientation = 0;
        if (mContext != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            switch (wm.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    orientation = 90;
                    break;
                case Surface.ROTATION_180:
                    orientation = 180;
                    break;
                case Surface.ROTATION_270:
                    orientation = 270;
                    break;
                case Surface.ROTATION_0:
                default:
                    orientation = 0;
                    break;
            }
        }
        return orientation;
    }

    public void setCameraDisplayOrientation() {
        mDegrees = getDeviceOrientation();
        mDegrees = (360 + mCameraInfo.orientation - mDegrees) % 360;
        camera.setDisplayOrientation(mDegrees);
    }

    private void stopCaptureOnCameraThread(Exchanger<Boolean> result) {
        if (camera == null) {
            throw new RuntimeException("Camera is already stopped!");
        }
        Throwable error = null;
        try {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            if (mSurfaceHolder != null) {
                mSurfaceHolder.removeCallback(this);
                camera.setPreviewDisplay(null);
            } else {
                camera.setPreviewTexture(null);
                cameraSurfaceTexture = null;
                if (cameraGlTextures != null) {
                    GLES20.glDeleteTextures(1, cameraGlTextures, 0);
                    cameraGlTextures = null;
                }
            }
            camera.release();
            camera = null;
            exchange(result, true);
            Looper.myLooper().quit();
            return;
        } catch (IOException e) {
            error = e;
        } catch (RuntimeException e) {
            error = e;
        }
        CLog.v("Failed to stop camera" + error);
        exchange(result, false);
        Looper.myLooper().quit();
        return;
    }

    // Called by native code. Returns true when camera is known to be stopped.
    public boolean stopCapture() {
        CLog.v("stopCapture");
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mCameraThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                stopCaptureOnCameraThread(result);
            }
        });
        boolean status = exchange(result, false); // |false| is a dummy value
        // here.
        try {
            mCameraThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mCameraThreadHandler = null;
        mCameraThread = null;
        CLog.v("stopCapture done");
        return status;
    }
//
//    private native void ProvideCameraFrame(byte[] data, int length, int rotation, long timeStamp,
//                                           long captureObject);

    // Called on cameraThread so must not "synchronized".
    @Override
    public void onPreviewFrame(byte[] data, Camera callbackCamera) {
        if (Thread.currentThread() != mCameraThread) {
            throw new RuntimeException("Camera callback not on camera thread?!?");
        }
        if (camera == null) {
            return;
        }
        if (camera != callbackCamera) {
            throw new RuntimeException("Unexpected camera in callback!");
        }
        frameCount++;
        // Check if frame needs to be dropped.
        if ((frameDropRatio > 1) && (frameCount % frameDropRatio) > 0) {
            camera.addCallbackBuffer(data);
            return;
        }
        long captureTimeMs = SystemClock.elapsedRealtime();
        if (frameCount > frameDropRatio) {
            double durationMs = captureTimeMs - lastCaptureTimeMs;
            averageDurationMs = 0.9 * averageDurationMs + 0.1 * durationMs;
            if ((frameCount % 30) == 0) {
                CLog.v("Camera TS " + captureTimeMs + ". Duration: " + (int) durationMs
                        + " ms. FPS: " + (int) (1000 / averageDurationMs + 0.5));
            }
        }
        lastCaptureTimeMs = captureTimeMs;

        // int rotation = getDeviceOrientation();
        // if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        // rotation = 360 - rotation;
        // }
        // rotation = (mCameraInfo.orientation + rotation) % 360;
        // int rotation = getDeviceOrientation();
        // if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        // rotation = (info.orientation - rotation + 360) % 360;
        // }
        // rotation = (info.orientation + rotation) % 360;
        // replace
//        MobileLivePlayer.getInstance(mContext).sendVideoDataBuffer(data, data.length, 90, System.currentTimeMillis());
        camera.addCallbackBuffer(data);
    }

    // Sets the rotation of the preview render window.
    // Does not affect the captured video image.
    // Called by native code.
    private void setPreviewRotation(final int rotation) {
        if (camera == null || mCameraThreadHandler == null) {
            return;
        }
        final Exchanger<IOException> result = new Exchanger<IOException>();
        mCameraThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                setPreviewRotationOnCameraThread(rotation, result);
            }
        });
        // Use the exchanger below to block this function until
        // setPreviewRotationOnCameraThread() completes, holding the
        // synchronized
        // lock for the duration. The exchanged value itself is ignored.
        exchange(result, null);
    }

    private void setPreviewRotationOnCameraThread(int rotation, Exchanger<IOException> result) {
        CLog.v("setPreviewRotationOnCameraThread:" + rotation);

        int resultRotation = 0;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // This is a front facing camera. SetDisplayOrientation will flip
            // the image horizontally before doing the rotation.
            // resultRotation = ( 360 - rotation ) % 360; // Compensate for the
            // mirror.
        } else {
            // Back-facing camera.
            resultRotation = rotation;
        }
        camera.setDisplayOrientation(resultRotation);
        CLog.v("setPreviewRotationOnCameraThread");
        exchange(result, null);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        CLog.v("VideoCaptureAndroid::surfaceCreated");
        if (camera == null || mCameraThreadHandler == null) {
            return;
        }
        final Exchanger<IOException> result = new Exchanger<IOException>();
        mCameraThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                setPreviewDisplayOnCameraThread(holder, result);
            }
        });
        // setPreviewRotation(90);
        IOException e = exchange(result, null); // |null| is a dummy value here.
        if (e != null) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        CLog.v("VideoCaptureAndroid::surfaceChanged ignored: " + format + ": " + width + "x"
                + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CLog.v("VideoCaptureAndroid::surfaceDestroyed");
        if (camera == null || mCameraThreadHandler == null) {
            return;
        }
        final Exchanger<IOException> result = new Exchanger<IOException>();
        mCameraThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                setPreviewDisplayOnCameraThread(null, result);
            }
        });
        IOException e = exchange(result, null); // |null| is a dummy value here.
        if (e != null) {
            throw new RuntimeException(e);
        }
    }

    private void setPreviewDisplayOnCameraThread(SurfaceHolder holder, Exchanger<IOException> result) {
        try {
            camera.setPreviewDisplay(holder);

        } catch (IOException e) {
            exchange(result, e);
            return;
        }
        exchange(result, null);
        return;
    }

    private class CameraThread extends Thread {

        private Exchanger<Handler> handlerExchanger;

        public CameraThread(Exchanger<Handler> handlerExchanger) {
            this.handlerExchanger = handlerExchanger;
        }

        @Override
        public void run() {
            Looper.prepare();
            exchange(handlerExchanger, new Handler());
            Looper.loop();
        }
    }


}

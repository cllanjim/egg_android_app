package com.lingyang.camera.util;

import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.Nullable;

import com.lingyang.base.utils.CLog;

import java.nio.ByteBuffer;

public class HardCoding {
    public static final int VIDEO_DATA_HARD = 2;
    public static final int VIDEO_DATA_SOFT = 1;
    VideoDataHandler mVideoDataHandler;
    private int videoColorFmt = 0;
    // MediaCodec
    private MediaCodec videoEncoder = null;
    private MediaCodec.BufferInfo bufferInfo = null;
    private ByteBuffer[] inputBuffers = null;
    private ByteBuffer[] outputBuffers = null;
    private ByteBuffer sps = null;
    private ByteBuffer pps = null;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private ByteBuffer yuvBuffer = null;
    private int mRotate;
    private long videoTimestamp = 0;

    public boolean isSupportHardCoding() {
        return getVideoColorFmt(getMediaCodecInfo()) != -1;
    }

    private static int getVideoColorFmt(MediaCodecInfo info) {
        MediaCodecInfo.CodecCapabilities capabilities = info.getCapabilitiesForType("video/avc");
        int k;
        for (k = 0; k < capabilities.colorFormats.length; k++) {
            if (MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar == capabilities.colorFormats[k]
                    || MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar == capabilities.colorFormats[k]) {
                return capabilities.colorFormats[k];
            }
        }
        return -1;
    }

    @Nullable
    private static MediaCodecInfo getMediaCodecInfo() {
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equals("video/avc")) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public boolean createVideoEncoder(int width, int height, int bitrate, int rotate) {
        mRotate = rotate;
        MediaCodecInfo info;
        info = getMediaCodecInfo();
        if (info == null) {
            CLog.e("Could not find a proper encoder for video/avc");
            return false;
        }

        videoColorFmt = getVideoColorFmt(info);
        if (videoColorFmt == -1) return false;
        int ew, eh;
        if (mRotate != 0 && (mRotate % 90) == 0) {
            // swap width and height
            ew = height;
            eh = width;
        } else {
            ew = width;
            eh = height;
        }

        final int fps = 15;
        MediaFormat mediaFmt = MediaFormat.createVideoFormat("video/avc", ew, eh);
        mediaFmt.setInteger(MediaFormat.KEY_BIT_RATE, bitrate * 1000);
        mediaFmt.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        mediaFmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoColorFmt);
        mediaFmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        MediaCodec mediaCodec;
        try {
            mediaCodec = MediaCodec.createByCodecName(info.getName());
            mediaCodec.configure(mediaFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        mediaCodec.start();

        int size = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
        videoWidth = width;
        videoHeight = height;
        bufferInfo = new MediaCodec.BufferInfo();
        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();
        yuvBuffer = ByteBuffer.allocateDirect(size);
        videoEncoder = mediaCodec;
        return true;
    }

    public void setVideoDataHandler(VideoDataHandler videoDataHandler) {
        mVideoDataHandler = videoDataHandler;
    }

    public void releaseVideoEncoder() {
        if (videoEncoder != null) {
            videoEncoder.stop();
            videoEncoder.release();
        }
        bufferInfo = null;
        sps = null;
        pps = null;
        yuvBuffer = null;
    }

    public void offerEncoder(byte[] input, int rotate) {
        if (videoEncoder == null) {
            return;
        }
        int inputBufferIndex = videoEncoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            long start = System.nanoTime() / 1000;

            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
//            inputBuffer.put(input);

            yuvBuffer.clear();
            if (input.length > yuvBuffer.limit()) {
                yuvBuffer.clear();
                CLog.e("BufferOverflowException: input=" + input.length + ", limit=" + yuvBuffer.limit());
            }
            yuvBuffer.put(input);
            if (!mVideoDataHandler.onProcessCameraPreview(videoWidth, videoHeight, rotate, ImageFormat.NV21, videoColorFmt, yuvBuffer, inputBuffer)) {
                inputBuffer.put(input);
            }
            long elapsed = System.nanoTime() / 1000 - start;
            CLog.v("Rotation took " + elapsed + " us");

            videoEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, start, 0);
        }

        int outputBufferIndex = 0;
        try {
            outputBufferIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, -1);
        } catch (IllegalStateException e) {
            CLog.e("dequeueOutputBuffer IllegalStateException");
        }
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            outputBuffers = videoEncoder.getOutputBuffers();
        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            CLog.v("Output format changed");
            MediaFormat format = videoEncoder.getOutputFormat();
        }

        for (; outputBufferIndex >= 0; outputBufferIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, 0)) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            outputBuffer.rewind();
            outputBuffer.limit(bufferInfo.size);
            if (sps != null && pps != null) {
                while (outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 1)
                    ;
                byte type = outputBuffer.get();
                videoTimestamp = bufferInfo.presentationTimeUs / 1000;
                if ((type & 0x1f) == 0x05) {
                    //send sps and pps with the same timestamp just before I frame
                    mVideoDataHandler.onSendVideoDataBuffer(sps, sps.remaining(), videoTimestamp);
                    mVideoDataHandler.onSendVideoDataBuffer(pps, pps.remaining(), videoTimestamp);
                }
                outputBuffer.position(outputBuffer.position() - 1);
                yuvBuffer.clear();
                yuvBuffer.put(outputBuffer);
                mVideoDataHandler.onSendVideoDataBuffer(yuvBuffer, bufferInfo.size - 4, videoTimestamp);
            } else {
                while (outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 1)
                    ;
                int indexSPS = outputBuffer.position();
                while (outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 0 || outputBuffer.get() != 1)
                    ;
                int indexPPS = outputBuffer.position();
                int lengthSPS = indexPPS - indexSPS - 4;
                int lengthPPS = bufferInfo.size - indexPPS;
                sps = ByteBuffer.allocateDirect(lengthSPS);
                pps = ByteBuffer.allocateDirect(lengthPPS);
                outputBuffer.position(indexSPS);
                outputBuffer.limit(lengthSPS + indexSPS);
                sps.put(outputBuffer);
                sps.rewind();
//                outputBuffer.limit(bufferInfo.size);
                outputBuffer.limit(lengthPPS + indexPPS);
                outputBuffer.position(indexPPS);
                pps.put(outputBuffer);
                pps.rewind();
                CLog.v("sps length=" + lengthSPS);
                CLog.v("pps length=" + lengthPPS);
            }
            videoEncoder.releaseOutputBuffer(outputBufferIndex, false);
        }
    }

    public interface VideoDataHandler {
        void onSendVideoDataBuffer(ByteBuffer data, int length, long videoTimestamp);

        boolean onProcessCameraPreview(int videoWidth, int videoHeight, int mRotate,
                                       int imageFormat, int videoColorFmt, ByteBuffer yuvBuffer,
                                       ByteBuffer outputBuffer);
    }
}

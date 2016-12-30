/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.lingyang.sdk.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;

import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.util.CLog;

public class LYGLCameraView extends GLSurfaceView {

    protected ScaleGestureDetector mScaleGestureDetector;
    private SurfaceFrameShape mShape = SurfaceFrameShape.RECTANGLE;
    private Camera mCamera;
    private int mMaxZoom;
    private Context mContext;
    private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        int mZoomWhenScaleBegan = 0;
        int mCurrentZoom = 0;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mCamera != null) {
                Camera.Parameters params = mCamera.getParameters();
                mCurrentZoom = (int) (mZoomWhenScaleBegan + (mMaxZoom * (detector.getScaleFactor() - 1)));
                mCurrentZoom = Math.min(mCurrentZoom, mMaxZoom);
                mCurrentZoom = Math.max(0, mCurrentZoom);
                params.setZoom(mCurrentZoom);
                mCamera.setParameters(params);
            }

            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mZoomWhenScaleBegan = mCamera.getParameters().getZoom();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

    public LYGLCameraView(Context context) {
        super(context);
        mContext=context;
        init();
    }

    public LYGLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
       /* TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LYGLCameraView);//获取配置属性
        boolean translucent = array.getBoolean(R.styleable.LYGLCameraView_translucent, true);
        if (translucent) {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 0, 0));
        }
        int shape = array.getInt(R.styleable.LYGLCameraView_shape, 1);
        switch (shape) {
            case 1:
                mShape = SurfaceFrameShape.RECTANGLE;
                break;
            case 2:
                mShape = SurfaceFrameShape.CIRCLE;
                break;
            case 3:
                mShape = SurfaceFrameShape.TRIANGLE;
                break;
            default:
                mShape = SurfaceFrameShape.RECTANGLE;
                break;
        }
        array.recycle();*/
        init();
    }

    private void init() {
        // Prep GLSurfaceView and attach Renderer
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 0, 0));
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
    }

    public SurfaceFrameShape getShape() {
        CLog.v("getShape:" + mShape.name());
        return mShape;
    }

    public void setShape(SurfaceFrameShape shape) {
        mShape = shape;
        CLog.v("setShape:" + mShape.name());
    }

    public void setCamera(Camera camera) {
        if(camera==null) return;
        mCamera = camera;
        Camera.Parameters camParams = mCamera.getParameters();
        if (camParams.isZoomSupported()) {
            mMaxZoom = camParams.getMaxZoom();
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        }
    }

    public void releaseCamera() {
        mCamera = null;
        mScaleGestureDetector = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScaleGestureDetector != null) {
            if (!mScaleGestureDetector.onTouchEvent(ev)) {
                // No scale gesture detected

            }
        }
        return true;
    }

//    Object obj=new Object();
//    public void updateMatrix(final int rotation, final ICameraSurfaceRenderer renderer){
//        synchronized (obj){
//            queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    renderer.updateMatrixForPreview(rotation);
//                }
//            });
//        }
//    }


private int mLastRotation;
    private int getOrientation(int rotation) {
            int degrees = 0;
            switch (rotation) {
                case android.view.Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case android.view.Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case android.view.Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case android.view.Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
        return degrees;
    }

    public void initratation(){
        final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLastRotation = windowManager.getDefaultDisplay().getRotation();
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
            int degrees = getOrientation(rotation);
            CLog.d("changed >>> " + degrees);
            mLastRotation = rotation;
            if(listener!=null){
                listener.onSuccess(degrees);
        }
    }

    public void OrientationEventListener() {

        final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLastRotation = windowManager.getDefaultDisplay().getRotation();

        OrientationEventListener orientationEventListener = new OrientationEventListener(mContext,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                CLog.d("orientation changed: " + orientation);
                Display display = windowManager.getDefaultDisplay();
                int rotation = display.getRotation();
                if (rotation != mLastRotation) {
                    int degrees = getOrientation(rotation);
                    CLog.d("changed >>> " + degrees);
                    mLastRotation = rotation;
                    if(listener!=null){
                        listener.onSuccess(degrees);
                    }
                }
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }

    }
    private CallBackListener listener;
    public void setOrientationCallback(CallBackListener listener){
        this.listener=listener;
    }
    
    @Override
    public void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    
    
    @Override
    public void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }

}

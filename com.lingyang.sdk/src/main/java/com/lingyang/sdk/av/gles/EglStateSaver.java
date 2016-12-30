package com.lingyang.sdk.av.gles;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import com.lingyang.sdk.util.CLog;


/**
 * @hide
 */
public class EglStateSaver {

    private EGLContext mSavedContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mSavedReadSurface = EGL14.EGL_NO_SURFACE;
    private EGLSurface mSavedDrawSurface = EGL14.EGL_NO_SURFACE;
    private EGLDisplay mSavedDisplay = EGL14.EGL_NO_DISPLAY;

    public void saveEGLState() {
        mSavedContext = EGL14.eglGetCurrentContext();
        if (mSavedContext.equals(EGL14.EGL_NO_CONTEXT)) CLog.e("Saved EGL_NO_CONTEXT");
        mSavedReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        if (mSavedReadSurface.equals(EGL14.EGL_NO_CONTEXT)) CLog.e("Saved EGL_NO_SURFACE");
        mSavedDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        if (mSavedDrawSurface.equals(EGL14.EGL_NO_CONTEXT)) CLog.e("Saved EGL_NO_SURFACE");
        mSavedDisplay = EGL14.eglGetCurrentDisplay();
        if (mSavedDisplay.equals(EGL14.EGL_NO_DISPLAY)) CLog.e("Saved EGL_NO_DISPLAY");

    }

    public EGLContext getSavedEGLContext() {
        return mSavedContext;
    }

    public void makeSavedStateCurrent() {
        EGL14.eglMakeCurrent(mSavedDisplay, mSavedReadSurface, mSavedDrawSurface, mSavedContext);
    }

    public void makeNothingCurrent() {
        EGL14.eglMakeCurrent(mSavedDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
    }

    public void logState() {
        if (!mSavedContext.equals(EGL14.eglGetCurrentContext()))
            CLog.i("Saved context DOES NOT equal current.");
        else
            CLog.i("Saved context DOES equal current.");

        if (!mSavedReadSurface.equals(EGL14.eglGetCurrentSurface(EGL14.EGL_READ))) {
            if (mSavedReadSurface.equals(EGL14.EGL_NO_SURFACE))
                CLog.i("Saved read surface is EGL_NO_SURFACE");
            else
                CLog.i("Saved read surface DOES NOT equal current.");
        } else
            CLog.i("Saved read surface DOES equal current.");

        if (!mSavedDrawSurface.equals(EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW))) {
            if (mSavedDrawSurface.equals(EGL14.EGL_NO_SURFACE))
                CLog.i("Saved draw surface is EGL_NO_SURFACE");
            else
                CLog.i("Saved draw surface DOES NOT equal current.");
        } else
            CLog.i("Saved draw surface DOES equal current.");

        if (!mSavedDisplay.equals(EGL14.eglGetCurrentDisplay()))
            CLog.i("Saved display DOES NOT equal current.");
        else
            CLog.i("Saved display DOES equal current.");
    }

}

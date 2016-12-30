package com.antelope.sdk.player;

import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;

import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.WorkThreadExecutor;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.SparseIntArray;

public class ACVideoRenderer  {
	// filter type
	public static final int GLFT_FilterNone = 0;
	public static final int GLFT_FilterBeauty = 1;
	public static final int GLFT_FilterVignette = 2;
	public static final int GLFL_FilterKuwahara = 3;
	public static final int GLFT_FilterGrayscale = 4;
	public static final int GLFL_FilterSepia = 5;
	public static final int GLFL_FilterPolaroid = 6;
	public static final int GLFL_FilterPi = 7;
	public static final int GLFL_FilterToneCurveVelvia = 8;
	public static final int GLFL_FilterToneCurveProvia = 9;
	public static final int GLFL_FilterToneCurvePortra = 10;
	public static final int GLFL_FilterToneCurveCrossProcess = 11;
	public static final int GLFL_FilterToneCurvePi = 12;
	public static final int GLFL_FilterGaussianBlur = 13;
	public static final int GLFL_FilterBilateral = 14;
	public static final int GLFL_FilterSigmaBlur = 15;
	
	// rotation
	public static final int GLR_ROTATION_0 = 0;
	public static final int GLR_ROTATION_90 = 90;
	public static final int GLR_ROTATION_180 = 180;
	public static final int GLR_ROTATION_270 = 270;
	
	// scale mode
	public static final int GLSM_SCALE_MODE_SCREEN = 0;
	public static final int GLSM_SCALE_MODE_IMAGE = 1;
	
	private GLSurfaceView mSurfaceView = null;
	private Renderer mRenderer = null;
	private long mPlayer = 0;
	private boolean mRequestDraw = false;
	private ACVideoFrame mFrame = null;
	private ACVideoFrame mOutFrame = null;
	private int mRotation = GLR_ROTATION_0;
	private boolean mMirror = false;
	private int mRenderImageFormat = GLPlayerAPI.GLIT_YUV_I420;
	private Exchanger<Integer> mDrawExchanger = new Exchanger<Integer>();
	private Exchanger<Integer> mPauseExchnager = new Exchanger<Integer>();
	private SparseIntArray mFilters = new SparseIntArray();
	private int mOutputImageFormat = ACImageFormat.AC_IMAGE_FMT_YUV420P;
	private int mScaleMode = GLSM_SCALE_MODE_SCREEN;
	private WorkThreadExecutor mEGLThread = null;
	
	/**
	 * 图像渲染类
	 * @param view 显示窗口
	 * @param outputImageFormat 输出图像格式 {@link ACImageFormat}
	 * @param scaleMode 图像缩放模式
	 */
	public ACVideoRenderer(GLSurfaceView view, int outputImageFormat, int scaleMode) {
		mRenderer = new VideoRenderer();
		if (view != null) {
			mSurfaceView = view;
			view.setEGLContextClientVersion(2);
			view.setRenderer(mRenderer);
			view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		} else {
			mFilters.append(GLFT_FilterGrayscale, GLFT_FilterGrayscale);
//			throw new RuntimeException("view is null");
			// off-screen
			mEGLThread = new WorkThreadExecutor("WorkThread-VideoRenderer");
			mEGLThread.start(null);
		}
		if (outputImageFormat != ACImageFormat.AC_IMAGE_FMT_YUV420P) {
			CLog.w("unsupported output image format");
		}
		mScaleMode = scaleMode;
	}
	
	public void release() {
		mSurfaceView = null;
		if (mEGLThread != null) {
			mEGLThread.executeTaskAndWaitForResult(mEGLTeardownRunnable, null);
			mEGLThread.stop();
			mEGLThread.release();
			mEGLThread = null;
		}
		mRenderer = null;
	}
	
	private class VideoRenderer implements Renderer {
		@Override
		public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
			if (mPlayer == 0) {
				mPlayer = GLPlayerAPI.LPlayerCreate(GLPlayerAPI.GLIT_YUV_I420, mScaleMode, mSurfaceView!=null);
			}
			GLPlayerAPI.LPlayerClearColor(mPlayer, 0, 0, 0, 255);
			for (int i=0; i<mFilters.size(); i++) {
				GLPlayerAPI.LPlayerAddFilter(mPlayer, mFilters.keyAt(i));
			}
		}

		@Override
		public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
			GLPlayerAPI.LPlayerSetViewport(mPlayer, 0, 0, width, height);
		}

		@Override
		public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
			if (!mRequestDraw) {
				return;
			}
			ByteBuffer outBuffer = null;
			int outOffset = 0;
			if (mOutFrame != null) {
				outBuffer = mOutFrame.buffer;
				outOffset = mOutFrame.offset;
			}
			GLPlayerAPI.LPlayerDrawFrame(mPlayer, mFrame.width, mFrame.height, mFrame.stride, mRenderImageFormat,
					mRotation, mMirror, mFrame.buffer, mFrame.offset,
					outBuffer, outOffset);
			mRequestDraw = false;
			exchange(mDrawExchanger, Integer.valueOf(1));
		}
	}
	
	private void pause() {
		GLPlayerAPI.LPlayerDestroy(mPlayer);
		mPlayer = 0;
	}
	
	private void waitDrawDone() {
		exchange(mDrawExchanger, Integer.valueOf(0));
	}
	
	public void enterBackground() {
		if (mSurfaceView == null) {
			return;
		}
		mSurfaceView.queueEvent(new Runnable() {
			@Override
			public void run() {
				ACVideoRenderer.this.pause();
				exchange(mPauseExchnager, Integer.valueOf(1));
			}
		});
		exchange(mPauseExchnager, Integer.valueOf(0));
		mSurfaceView.onPause();
	}
	
	public void enterForeground() {
		if (mSurfaceView != null) {
			mSurfaceView.onResume();
		}
	}
	
	public void drawFrame(ACVideoFrame frame, int rotation, boolean mirror, ACVideoFrame outFrame) {
		if (frame == null) {
			return;
		}
		if (rotation != GLR_ROTATION_0 &&
				rotation != GLR_ROTATION_90 &&
				rotation != GLR_ROTATION_180 &&
				rotation != GLR_ROTATION_270) {
			return;
		}
		int format = getGLFormat(frame.format);
		if (format == -1) {
			return;
		}
		mFrame = frame;
		mRotation = rotation;
		mMirror = mirror;
		mRenderImageFormat = format;
		if (outFrame != null) {
			mOutFrame = outFrame;
			if (mOutFrame.buffer == null || mOutFrame.buffer.capacity()-mOutFrame.offset < mFrame.size) {
				ByteBuffer outBuffer = ByteBuffer.allocateDirect(mFrame.size + mOutFrame.offset);
				if (mOutFrame.buffer != null && mOutFrame.offset > 0) {
					mOutFrame.buffer.limit(mOutFrame.offset);
					mOutFrame.buffer.position(0);
					outBuffer.put(mOutFrame.buffer);
				}
				mOutFrame.buffer = outBuffer;
			}
			mOutFrame.size = mFrame.size;
			//				if (rotation == GLR_ROTATION_90 || rotation == GLR_ROTATION_180) {
			//					mOutFrame.width = mFrame.height;
			//					mOutFrame.height = mFrame.width;
			//				} else {
			mOutFrame.width = mFrame.width;
			mOutFrame.height = mFrame.height;
			//				}
			mOutFrame.format = mOutputImageFormat;
			mOutFrame.stride = mOutFrame.width;
			mOutFrame.timestamp = mFrame.timestamp;
		}
		requestRender();
		waitDrawDone();
	}
	
	private boolean addFilteri(int filter) {
		if (mPlayer != 0 && GLPlayerAPI.LPlayerAddFilter(mPlayer, filter) == 0) {
			mFilters.put(filter, filter);
			return true;
		}
		return false;
	}
	
	public boolean addFilter(final int filter) {
		final Exchanger<Boolean> result = new Exchanger<Boolean>();
		if (mSurfaceView != null) {
			mSurfaceView.queueEvent(new Runnable() {
				@Override
				public void run() {
					boolean r = addFilteri(filter);
					exchange(result, r);
				}
			});
			return exchange(result, false);
		} else {
			return (Boolean) mEGLThread.executeTaskAndWaitForResult(mEGLAddFilterRunnable, (Integer)filter);
		}
	}
	
	private boolean removeFilteri(int filter) {
		if (mPlayer != 0 && GLPlayerAPI.LPlayerRemoveFilter(mPlayer, filter) == 0) {
			if (filter == 0) {
				mFilters.clear();
			} else {
				mFilters.delete(filter);
			}
			return true;
		}
		return false;
	}
	
	public boolean removeFilter(final int filter) {
		final Exchanger<Boolean> result = new Exchanger<Boolean>();
		if (mSurfaceView != null) {
			mSurfaceView.queueEvent(new Runnable() {
				@Override
				public void run() {
					boolean r = removeFilteri(filter);
					exchange(result, r);
				}
			});
			return exchange(result, false);
		} else {
			return (Boolean) mEGLThread.executeTaskAndWaitForResult(mEGLRemoveFilterRunnable, (Integer)filter);
		}
	}
	
	private void requestRender() {
		if (mSurfaceView != null) {
			mRequestDraw = true;
			mSurfaceView.requestRender();
		} else {
			mEGLThread.executeTask(mEGLRenderRunnable);
		}
	}

	private <T> T exchange(Exchanger<T> exchanger, T value) {
		try {
			return exchanger.exchange(value);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}
	
	private int getGLFormat(int format) {
		switch (format) {
		case ACImageFormat.AC_IMAGE_FMT_YUV420P:
			format = GLPlayerAPI.GLIT_YUV_I420;
			break;
		case ACImageFormat.AC_IMAGE_FMT_NV21:
			format = GLPlayerAPI.GLIT_YUV_NV21;
			break;
		case ACImageFormat.AC_IMAGE_FMT_YUV420SP:
			format = GLPlayerAPI.GLIT_YUV_NV12;
			break;
		default:
			format = -1;
			break;
		}
		return format;
	}
	
	private Runnable mEGLRenderRunnable = new Runnable() {
		@Override
		public void run() {
			if (!mEGLSetup) {
				if (setupEGL(mFrame.width, mFrame.height)) {
					mEGLSurfaceWidth = mFrame.width;
					mEGLSurfaceHeight = mFrame.height;
					mRenderer.onSurfaceCreated(null, null);
					mRenderer.onSurfaceChanged(null, mEGLSurfaceWidth, mEGLSurfaceHeight);
				} else {
					teardownEGL();
				}
				mEGLSetup = true;
			}
			if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
				return;
			}
			mRenderer.onDrawFrame(null);
			EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
		}
	};
	
	private WorkThreadExecutor.SyncRunnable mEGLTeardownRunnable = new WorkThreadExecutor.SyncRunnable() {
		@Override
		public Object run(Object obj) {
			teardownEGL();
			return null;
		}
	};
	
	private WorkThreadExecutor.SyncRunnable mEGLAddFilterRunnable = new WorkThreadExecutor.SyncRunnable() {
		@Override
		public Object run(Object obj) {
			return (Boolean) addFilteri((Integer)obj);
		}
	};
	
	private WorkThreadExecutor.SyncRunnable mEGLRemoveFilterRunnable = new WorkThreadExecutor.SyncRunnable() {
		@Override
		public Object run(Object obj) {
			return (Boolean) removeFilteri((Integer)obj);
		}
	};
	
	private boolean mEGLSetup = false;
	private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
	private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
	private EGLConfig mEGLConfig = null;
	private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
	private int mEGLSurfaceWidth = 240;
	private int mEGLSurfaceHeight = 320;
	
	private boolean setupEGL(int width, int height) {
		int[] version = new int[2];
		mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
		if (mEGLDisplay == null) {
			CLog.e("unable to get EGL14 display");
			return false;
		}
		if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
			CLog.e("unable to initialize EGL14");
			return false;
		}
		
		mEGLContext = EGL14.EGL_NO_CONTEXT;
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        mEGLConfig = chooseConfig(3);
		if (mEGLConfig != null) {
			mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);
			if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
				CLog.i("not supported GLES 3");
				mEGLContext = EGL14.EGL_NO_CONTEXT;
			}
		}
		if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
			mEGLConfig = chooseConfig(2);
			if (mEGLConfig == null) {
				return false;
			}
			attrib_list[1] = 2;
			mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);
			if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
				CLog.e("eglCreateContext failed");
				return false;
			}
		}
        return createPbufferSurface(width, height);
	}
	
	private void teardownEGL() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
            // every eglInitialize() we need an eglTerminate().
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;
	}
	
	private boolean createPbufferSurface(int width, int height) {
		int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
		mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
                surfaceAttribs, 0);
        if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
        	CLog.e("eglCreatePbufferSurface failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
        	return false;
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
           	CLog.e("eglMakeCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
        	return false;
        }
		return true;
	}
	
	private EGLConfig chooseConfig(int version) {
		int renderable_type = EGL14.EGL_OPENGL_ES2_BIT;
		if (version >= 3) {
			renderable_type |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
		}
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderable_type,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            CLog.w("unable to find RGB8888 / " + version + " EGLConfig");
            return null;
        }
        return configs[0];
	}
	
	static class GLPlayerAPI {
		static {
			System.loadLibrary("glplayer");
		}
		
		// log level
		static final int 	ALOG_LEVEL_INFO = 2;
		static final int 	ALOG_LEVEL_WARN = 3;
		static final int 	ALOG_LEVEL_ERROR = 4;
		
		// image format
		static final int GLIT_YUV_I420 = 0;
		static final int GLIT_YUV_NV21 = 1;
		static final int GLIT_YUV_NV12 = 2;
		
		static native void LPlayerSetLogLevel(int level);
		static native long LPlayerCreate(int outputImageFormat, int scaleMode, boolean preview);
		static native int LPlayerDestroy(long player);
		static native int LPlayerAddFilter(long player, int GLFilterType);
		static native int LPlayerRemoveFilter(long player, int GLFilterType);
		static native int LPlayerClearColor(long player, int r, int g, int b, int a);
		static native int LPlayerSetViewport(long player, int x, int y, int width, int height);
		static native int LPlayerDrawFrame(long player, int width, int height, int stride, int format,
				int rotation, boolean mirror,
				ByteBuffer yuv, int offset,
				ByteBuffer yuvOutput, int offsetOutput);
		
	}
	
}

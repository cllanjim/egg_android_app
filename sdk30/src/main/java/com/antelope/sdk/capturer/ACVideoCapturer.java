package com.antelope.sdk.capturer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACImageResolution.ACResolution;
import com.antelope.sdk.capturer.preview.ACPreviewRenderer;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.ACUtilAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.HandlerUtil;
import com.antelope.sdk.utils.WorkThreadExecutor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.SensorEventListener;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author liaolei
 * @version 创建时间：2016年9月8日 该类用于视频采集
 */
@SuppressWarnings("deprecation")
public class ACVideoCapturer {

	private static final int MSG_OPEN_CAMERA = 0x001;
	private static final int MSG_START_PREVIEW = 0x002;
	private static final int MSG_STOP_PREVIEW = 0x003;
	private static final int MSG_CLOSE_CAMERA = 0x004;
	private static final int MSG_SET_FRAME_RATE = 0x005;
	private static final int MSG_SET_FLASH_MODE = 0x006;
	private Context mContext;
	private boolean mAutoRotate;
	private Camera mCamera;
	private int mVideoWidth, mVideoHeight;// 分辨率
	private int mFrameRate;// 帧率
	private int mTargetCamera, mCurrentCamera;
	private GLSurfaceView mDisplayView;
	private int mShape;
	private int mFrameDropRate, mFrameCount;
	private double mAverageDurationMs;// 平均每帧间隔
	private long mNextCaptureTimeMs, mLastReadTimeMs;
	private String mCurrentFlashMode;
	private SurfaceTexture mSurfaceTexture;
	private SurfaceTexture mSurfaceTextureWithoutPreview;
	private boolean mStartPreview = false;
	private int mBufferSize;
	private byte[][] mPreviewBuffers = new byte[2][];
	private ACPreviewRenderer mRenderer;
	long currentCaptureTimeMs;
	private boolean mIsInitialized=false;
	private ACVideoFrame mVideoFrame = null;
	private ByteBuffer mFrameBuffer = null;
	private ByteBuffer mRotationBuffer = null;
	private ACFrameAvailableListener mFrameAvailableListener = null;
	private int mOutputImageFormat = ACImageFormat.AC_IMAGE_FMT_NV21;
	private int mInFormat = ACUtilAPI.IMAGE_NV21;
	private int mOutFormat = ACUtilAPI.IMAGE_NV21;
	private Exchanger<Object> mReleaseRendererExchanger = new Exchanger<Object>();
	private WorkThreadExecutor mWorkThreadCamera;
	
	public ACVideoCapturer() {
		mWorkThreadCamera = new WorkThreadExecutor("WorkThread-ACVideoCapturer");
		mWorkThreadCamera.start(mCameraMsgCallback);
	}

	/**
	 * 初始化视频采集相关
	 * @param context 应用上下文
	 * @param autoRotate 是否自动旋转自适应设备
	 * @param frameListener 视频帧数据回调
	 * @param outFormat 输出的图像格式，仅支持yuv420p, yuv420sp, nv21，传入unknown默认给出nv21，详细定义请参考{@link ACImageFormat}
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult initialize(Context context, boolean autoRotate, ACFrameAvailableListener frameListener, int outFormat) {
		if(!ACPlatformAPI.hasAuthorize())
			return ACResult.NO_AUTHORIZATION;
		if (context == null && autoRotate) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "context shoud not be null while auto rotation is on");
		}
		if (frameListener != null &&
				outFormat !=  ACImageFormat.AC_IMAGE_FMT_UNKNOWN &&
				outFormat != ACImageFormat.AC_IMAGE_FMT_YUV420P &&
				outFormat != ACImageFormat.AC_IMAGE_FMT_YUV420SP &&
				outFormat != ACImageFormat.AC_IMAGE_FMT_NV21) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "not supported image format");
		}
		mContext = context;
		mAutoRotate = autoRotate;
		mVideoHeight = 480;
		mVideoWidth = 640;
		mFrameRate = 15000;
		mCurrentFlashMode = Parameters.FLASH_MODE_OFF;
		if (frameListener != null) {
			mVideoFrame = new ACVideoFrame();
			mFrameAvailableListener = frameListener;
			if (outFormat != ACImageFormat.AC_IMAGE_FMT_UNKNOWN) {
				mOutputImageFormat = outFormat;
			}
			switch (mOutputImageFormat) {
			case ACImageFormat.AC_IMAGE_FMT_YUV420P:
				mOutFormat = ACUtilAPI.COLOR_FormatYUV420Planar;
				break;
			case ACImageFormat.AC_IMAGE_FMT_YUV420SP:
				mOutFormat = ACUtilAPI.COLOR_FormatYUV420SemiPlanar;
				break;
			}
		}
		mIsInitialized=true;
		return ACResult.SUCCESS;
	}

	/**
	 * 设置帧率
	 * @param fps 帧率
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult setVideoFrameRate(int fps) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if(fps<=0)
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid fps");
		
		return (ACResult) mWorkThreadCamera.sendMessageAndWaitForResult(MSG_SET_FRAME_RATE, fps*1000, 0);
	}

	/**
	 * 设置预览窗口及形状
	 * 
	 * @param view
	 *            预览view
	 * @param shape
	 *            形状 {@link ACShape}
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult setPreviewSurfaceView(GLSurfaceView view, int shape, Object... args) {
		if (view == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "view should not be null");
		}
//		if (!mIsInitialized) {
//			return ACResult.UNINITIALIZED;
//		}
		mDisplayView = view;
		if (mRenderer == null) {
			mRenderer = new ACPreviewRenderer(shape, mSurfaceTextureCreatedListener);
			mDisplayView.setEGLContextClientVersion(2);
			if (shape == ACShape.AC_SHAPE_CIRCLE || shape == ACShape.AC_SHAPE_TRIANGLE) {
				mDisplayView.setZOrderOnTop(true);
				mDisplayView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
				mDisplayView.getHolder().setFormat(PixelFormat.RGBA_8888);
			}
			mDisplayView.setRenderer(mRenderer);
			mDisplayView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
		mShape = shape;
		return ACResult.SUCCESS;
	}

//	/**
//	 * 获取一帧YUV数据
//	 * 
//	 * @param frame
//	 *            一帧数据，返回成功时，从frame取数据，包含一帧YUV数据以及各种相关信息 {@link ACVideoFrame}
//	 * @param timeout
//	 *            超时时间,单位毫秒；建议设置比帧率间隔时间稍长的超时时间；若传0，立即返回；
//	 * @return 成功或失败错误码 {@link ACResult}
//	 */
//	public ACResult readVideoFrame(ACVideoFrame frame, int timeout) {
//		if (!mIsInitialized) {
//			return ACResult.UNINITIALIZED;
//		}
//		ACResult aCResult;
//		try {
//			if (mSemaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
//				if (mYUVBuffer != null) {
//					if (frame.buffer == null || frame.buffer.capacity() < mYUVBuffer.capacity()) {
//						if (mFrameByteBuffer == null || mFrameByteBuffer.capacity() < mYUVBuffer.capacity()) {
//							mFrameByteBuffer = ByteBuffer.allocateDirect(mYUVBuffer.capacity());
//						}
//						frame.buffer = mFrameByteBuffer;
//					}
////					CLog.i("test yuvByteBuffer:" + mYUVBuffer);
//					int rotation = 0;
//					frame.height = mVideoHeight;
//					frame.width = mVideoWidth;
//					if (mAutoRotate) {
//						rotation = getRotation();
//					}
//					synchronized (mPutBuffer) {
//						if (rotation != 0) {
//							ACUtilAPI.ProcessCameraPreview(mVideoWidth, mVideoHeight, rotation, ACUtilAPI.IMAGE_NV21,
//									ACUtilAPI.IMAGE_NV21, mYUVBuffer, 0, frame.buffer, 0);
//							if (rotation == 90 || rotation == 270) {
//								frame.height = mVideoWidth;
//								frame.width = mVideoHeight;
//							}
//						} else {
//							frame.buffer.position(0);
//							frame.buffer.limit(mBufferSize);
//							mYUVBuffer.position(0);
//							frame.buffer.put(mYUVBuffer);
//						}
//					}
//					frame.format = ACImageFormat.AC_IMAGE_FMT_NV21;
//					frame.offset = 0;
//					frame.size = mBufferSize;
//					frame.timestamp = currentCaptureTimeMs;
//					frame.stride = frame.width;// 存储一行所用的字节
//					aCResult = new ACResult(ACResult.ACS_OK, "有可用数据");
//					return aCResult;
//				}
//			}
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//			aCResult = new ACResult(ACResult.ACS_UNKNOWN, "暂无输出数据");
//			return aCResult;
//		}
//		aCResult = new ACResult(ACResult.ACS_UNKNOWN, "暂无输出数据");
//		return aCResult;
//	}

	/**
	 * 打开摄像机
	 * 
	 * @param CameraId
	 *            摄像机id
	 * @param resolution
	 *            输出图像宽 {@link ACImageResolution}
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult openCamera(int cameraId, int resolution) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		ACResolution r = ACImageResolution.getInstance().getResolution(resolution);
		if(r == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid resolution");
		}
		mTargetCamera = cameraId;
		mVideoHeight = r.height;
		mVideoWidth = r.width;
		return (ACResult) mWorkThreadCamera.sendMessageAndWaitForResult(MSG_OPEN_CAMERA, mDisplayView);
	}

	/**
	 * 关闭摄像机
	 * 
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult closeCamera() {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		mWorkThreadCamera.sendEmptyMessageAndWaitForResult(MSG_CLOSE_CAMERA);
		return ACResult.SUCCESS;
	}

	/**
	 * 反初始化，释放camera相关资源。退出当前界面或不需要使用camera的时候必须调用此方法释放camera相关资源，
	 * 这样其他功能或其他应用才能正常使用camera；
	 * 
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult release() {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		closeCamera();
		mWorkThreadCamera.stop();
		mWorkThreadCamera.release();
		mWorkThreadCamera = null;
		mSurfaceTexture = null;
		if (mSurfaceTextureWithoutPreview != null) {
			mSurfaceTextureWithoutPreview.release();
			mSurfaceTextureWithoutPreview = null;
		}
		mIsInitialized=false;
		return ACResult.SUCCESS;
	}

	/**
	 * 闪光灯开关
	 * 
	 * @param on
	 *            是否打开闪光灯，true为打开，false为关闭；
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult turnFlashLight(boolean on) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		return (ACResult) mWorkThreadCamera.sendMessageAndWaitForResult(MSG_SET_FLASH_MODE, on?1:0, 0);
	}

	/**
	 * 应用进入后台时调用
	 */
	public void enterBackground() {
		if (mWorkThreadCamera != null) {
			mWorkThreadCamera.sendEmptyMessageAndWaitForResult(MSG_STOP_PREVIEW);
			releaseRenderer();
		}
		if (mDisplayView != null) {
			mDisplayView.onPause();
		}
	}

	/**
	 * 应用进入前台时调用
	 */
	public void enterForeground() {
		if (mDisplayView != null) {
			mDisplayView.onResume();
		}
	}
	
	private void startPreview(SurfaceTexture texture) {
		if (texture == null || mStartPreview) {
			return;
		}
		mSurfaceTexture = texture;
		if (mCamera == null) {
			return;
		}
		try {
			mCamera.setPreviewTexture(texture);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (mFrameAvailableListener != null) {
			for (byte[] b: mPreviewBuffers) {
				mCamera.addCallbackBuffer(b);
			}
			mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
		}
		mCamera.startPreview();
		mStartPreview = true;
	}
	
	private void stopPreview() {
		if (!mStartPreview || mCamera == null) {
			return;
		}
		mCamera.stopPreview();
		mCamera.setPreviewCallbackWithBuffer(null);
		try {
			mCamera.setPreviewTexture(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mStartPreview = false;
	}

	private ACResult openCameraWithPreview() {
		ACResult result = performOpenCamera();
		if (!result.isResultOK()) {
			return result;
		}
		startPreview(mSurfaceTexture);
		return result;
	}

	private ACResult openCameraWithoutPreview() {
		ACResult result = performOpenCamera();
		if (!result.isResultOK()) {
			return result;
		}
		if (mSurfaceTextureWithoutPreview == null) {
			mSurfaceTextureWithoutPreview = new SurfaceTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES);
		}
		startPreview(mSurfaceTextureWithoutPreview);
		return result;
	}

	protected ACResult performOpenCamera() {
		if (mCamera != null) {
			CLog.w("camera is already opened");
			return ACResult.SUCCESS;
		}

		Camera.CameraInfo info = new Camera.CameraInfo();
		int camNumber = Camera.getNumberOfCameras();
		for (int i = 0; i < camNumber; i++) {
			Camera.getCameraInfo(i, info);
			if (info.facing == mTargetCamera) {
				try {
					mCamera = Camera.open(i);
					mCurrentCamera = i;
					break;
				} catch (RuntimeException e) {
					e.printStackTrace();
					return new ACResult(ACResult.ACS_OPEN_CAMERA_FAILED, e.toString());
				}
			}
		}

		if (mCamera == null) {
			return new ACResult(ACResult.ACS_OPEN_CAMERA_FAILED, "no specified camera id found");
		}
		
		Camera.Parameters params = mCamera.getParameters();
		
		ACResult result = ACResult.SUCCESS;
		do {
			if (!chooseBestMatchCameraSize(params, mVideoWidth, mVideoHeight)) {
				result = new ACResult(ACResult.ACS_NOT_SUPPORTED, "not supported resolution");
				break;
			}

			if (!setFrameRate(mFrameRate, params)) {
				result = new ACResult(ACResult.ACS_EXCEED_VALUE, "frame rate exceed camera supported range");
				break;
			}
		} while (false);
		
		if (!result.isResultOK()) {
			mCamera.release();
			mCamera = null;
			return result;
		}
		
		List<String> focus = params.getSupportedFocusModes();
		if (focus.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		} else if (focus.contains(Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		} else {
			CLog.w("Camera does not support autofocus");
		}

		List<String> flash = params.getSupportedFlashModes();
		if (isValidFlashMode(flash, mCurrentFlashMode)) {
			params.setFlashMode(mCurrentFlashMode);
		}

		mCamera.setParameters(params);
		
		return result;
	}
	
	private void closeCameraInner() {
		if (mCamera != null) {
			stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	private PreviewCallback mPreviewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			currentCaptureTimeMs = SystemClock.elapsedRealtime();
			mFrameCount++;
			
			boolean dropped = false;
			do {
				if (mFrameDropRate > 1) {
					if ((mFrameCount % mFrameDropRate) != 0) {
						dropped = true;
						break;
					}
				} else if (mFrameDropRate == 0) {
					if (mNextCaptureTimeMs == 0) {
						mNextCaptureTimeMs = currentCaptureTimeMs;
					} else {
						if (currentCaptureTimeMs > mNextCaptureTimeMs+1000) {
							CLog.i("ajust next capture time: next=" + mNextCaptureTimeMs+" current="+currentCaptureTimeMs);
							mNextCaptureTimeMs = currentCaptureTimeMs;
						}
						else if (currentCaptureTimeMs < mNextCaptureTimeMs) {
							dropped = true;
							break;
						}
						mNextCaptureTimeMs += mAverageDurationMs;
					}
				}
			} while (false);
//			CLog.e("capture timestamp="+currentCaptureTimeMs + " dropped="+dropped);
			
			if (!dropped) {
				if (mFrameAvailableListener != null) {
					copyVideoFrame(data, currentCaptureTimeMs);
					mFrameAvailableListener.onFrameAvailable(mVideoFrame);
				}
			}

			camera.addCallbackBuffer(data);
		}
	};
	
	private void copyVideoFrame(byte[] data, long timestamp) {
		int rotation = 0;
		if (mAutoRotate) {
			rotation = getRotation(false);
		}
		mVideoFrame.width = mVideoWidth;
		mVideoFrame.height = mVideoHeight;
		mFrameBuffer.clear();
		mFrameBuffer.put(data);
		mVideoFrame.buffer = mFrameBuffer;
		if (rotation != 0 || mInFormat != mOutFormat) {
			ACUtilAPI.ProcessCameraPreview(mVideoWidth, mVideoHeight, rotation, mInFormat,
					mOutFormat, mFrameBuffer, 0, mRotationBuffer, 0);
			if (rotation == 90 || rotation == 270) {
				mVideoFrame.height = mVideoWidth;
				mVideoFrame.width = mVideoHeight;
			}
			mVideoFrame.buffer = mRotationBuffer;
		}
		mVideoFrame.offset = 0;
		mVideoFrame.size = data.length;
		mVideoFrame.format = mOutputImageFormat;
		mVideoFrame.stride = mVideoFrame.width;
		mVideoFrame.timestamp = timestamp;
	}
	
	private int getRotation(boolean preview) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCurrentCamera, info);// info.orientation
													// 正确显示画面应该旋转的角度，前置270 后置90
		int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();// 手机角度，值为0，1，2，3，width<height,竖屏为0，反之横屏为0
		int degree = 0;// 手机旋转的角度
		switch (rotation) {
		case Surface.ROTATION_0:// 0
			degree = 0;
			break;
		case Surface.ROTATION_90:// 1
			degree = 90;
			break;
		case Surface.ROTATION_180:// 2
			degree = 180;
			break;
		case Surface.ROTATION_270:// 3
			degree = 270;
			break;

		default:
			break;
		}

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
			if (preview) {
				rotation = (info.orientation - degree + 360) % 360;
			} else {
				rotation = (degree - info.orientation + 360) % 360;
			}
		} else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotation = 360 - (info.orientation + degree) % 360;
		}
		return rotation;
	}

	private int mLastRotation = -1;
	private int mRotation = 0;
	private OnFrameAvailableListener mPreviewFrameAvailableListener = new OnFrameAvailableListener() {
		@Override
		public void onFrameAvailable(SurfaceTexture surfaceTexture) {
			mRotation = getRotation(true);
			if (mRotation != mLastRotation) {
				mDisplayView.queueEvent(new Runnable() {
					@Override
					public void run() {
						mRenderer.setParams(mRotation, mVideoWidth, mVideoHeight);
					}
				});
				mLastRotation = mRotation;
			}
			mDisplayView.requestRender();
		}
	};
	
	private ACPreviewRenderer.OnSurfaceTextureCreatedListener mSurfaceTextureCreatedListener = new ACPreviewRenderer.OnSurfaceTextureCreatedListener() {
		@Override
		public void onSurfaceTextureCreated(SurfaceTexture texture) {
			texture.setOnFrameAvailableListener(mPreviewFrameAvailableListener);
			mWorkThreadCamera.sendMessage(MSG_START_PREVIEW, texture);
		}
	};
	
	private void releaseRenderer() {
		if (mDisplayView != null) {
			mDisplayView.queueEvent(new Runnable() {
				@Override
				public void run() {
					mRenderer.release();
					try {
						mReleaseRendererExchanger.exchange(null);
					} catch (InterruptedException e) {
					}
				}
			});
			try {
				mReleaseRendererExchanger.exchange(null);
			} catch (InterruptedException e) {
			}
		}
	}

	protected boolean chooseFPSRange(Parameters params, int fps) {
		int[] range;
		int target = -1;
		List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
		mFrameDropRate = 0;
		
		for (int i = 0; i < fpsRanges.size(); i++) {
			range = fpsRanges.get(i);
			if (range[Parameters.PREVIEW_FPS_MAX_INDEX] == fps && range[Parameters.PREVIEW_FPS_MIN_INDEX] == fps) {
				mFrameDropRate = 1; // 不丢帧
				target = i;
				break;
			}
		}
		if (target == -1) {
			for (int i = 0; i < fpsRanges.size(); i++) {
				range = fpsRanges.get(i);
				if (range[Parameters.PREVIEW_FPS_MIN_INDEX] >= fps) {
					target = i;
					break;
				}
			}
		}
		if (target == -1) {
			for (int i = 0; i < fpsRanges.size(); i++) {
				range = fpsRanges.get(i);
				if (range[Parameters.PREVIEW_FPS_MAX_INDEX] >= fps) {
					target = i;
					break;
				}
			}
		}
		
		if (target == -1) {
			return false;
		}
		
		range = fpsRanges.get(target);
		int min = range[Parameters.PREVIEW_FPS_MIN_INDEX];
		int max = range[Parameters.PREVIEW_FPS_MAX_INDEX];
		params.setPreviewFpsRange(min, max);
		
		if (mFrameDropRate != 1 && min == max && (min%fps) == 0) {
			mFrameDropRate = min/fps;
		}
			
		return true;
	}

	protected boolean chooseBestMatchCameraSize(Parameters param, int width, int height) {
		Camera.Size matchSize = null;
		List<Camera.Size> list = param.getSupportedPreviewSizes();
//		Collections.sort(list, new Comparator<Camera.Size>() {
//			@Override
//			public int compare(Camera.Size lhs, Camera.Size rhs) {
//				return lhs.width - rhs.width;
//			}
//		});
		for (Camera.Size size : list) {
			if (size.width == width && size.height == height) {
				matchSize = mCamera.new Size(width, height);
				break;
			}
		}
//		if (matchSize == null) {
//			for (Camera.Size size : list) {
//				if (size.width == width || size.height == height) {
//					matchSize = mCamera.new Size(size.width, size.height);
//					break;
//				}
//			}
//		}

//		if (matchSize == null) {
//			Iterator<Camera.Size> it = list.iterator();
//			while (it.hasNext()) {
//				Camera.Size size = it.next();
//				if (size.width > width && size.width > size.height) {
//					matchSize = mCamera.new Size(size.width, size.height);
//					break;
//				}
//			}
//		}

//		if (matchSize == null) {
//			matchSize = param.getPreferredPreviewSizeForVideo();
//		}
		if (matchSize == null) {
			return false;
		}
		
		param.setPreviewSize(matchSize.width, matchSize.height);

		mBufferSize = matchSize.width * matchSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;// 每一帧的字节长度
		if (mFrameAvailableListener != null) {
			for (int i=0; i<mPreviewBuffers.length; i++) {
				mPreviewBuffers[i] = new byte[mBufferSize];
			}
			mFrameBuffer = ByteBuffer.allocateDirect(mBufferSize);
			if (mAutoRotate || mInFormat != mOutFormat) {
				mRotationBuffer = ByteBuffer.allocateDirect(mBufferSize);
			}
		}
		
		return true;
	}

	protected ACResult setFlashMode(boolean on) {
		if(mCurrentCamera==CameraInfo.CAMERA_FACING_FRONT) {
			return new ACResult(ACResult.ACS_NOT_SUPPORTED, "front camera no flash");
		}
		
		String flashMode = on == true ? Parameters.FLASH_MODE_TORCH : Parameters.FLASH_MODE_OFF;
		if (flashMode.equals(mCurrentFlashMode)) {
			return new ACResult(ACResult.ACS_ALREADY_OPENED, "same as current flash mode");
			 
		}
		
		/*
		 * If mCamera for some reason is null now flash mode will be applied
		 * next time the camera opens through mDesiredFlash.
		 */
		if (mCamera == null) {
			return new ACResult(ACResult.ACS_NOT_OPENED, "camera is not opened");
		}
		Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		/*
		 * If the device doesn't have a camera flash or doesn't support our
		 * desired flash modes return
		 */
//		{
//			CLog.w("Trying to set flash to: " + mDesireFlashMode + " modes available: " + flashModes);
//		}

		if (isValidFlashMode(flashModes, flashMode)) {
			mCurrentFlashMode = flashMode;
			try {
				params.setFlashMode(mCurrentFlashMode);
				mCamera.setParameters(params);
//				{
//					CLog.w("Changed flash successfully!");
//				}
			} catch (RuntimeException e) {
				CLog.e("Unable to set flash", e);
				return new ACResult(ACResult.ACS_UNKNOWN, "Unable to set camera flash" + e);
			}
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * calculate frame duration
	 * @param fps frames per microsecond
	 */
	private boolean setFrameRate(int fps, Parameters params) {
		mFrameRate = fps;
		if (mCamera != null) {
			boolean set = false;
			if (params == null) {
				params = mCamera.getParameters();
				set = true;
			}
			if (!chooseFPSRange(params, fps)) {
				return false;
			}
			if (set) {
				mCamera.setParameters(params);
			}
//			int[] range = new int[2];
//			params.getPreviewFpsRange(range);
//			CLog.e("["+range[Parameters.PREVIEW_FPS_MIN_INDEX]+", "+range[Parameters.PREVIEW_FPS_MAX_INDEX]+"]");
		}
		mAverageDurationMs = 1000000.f / fps;
		mNextCaptureTimeMs = 0;
		mFrameCount = 0;
//		CLog.e("fps="+fps+", drop="+mFrameDropRate+", min="+mMinFrameRate+", max="+mMaxFrameRate);
		return true;
	}

	/**
	 * @param flashModes
	 * @param flashMode
	 * @return returns true if flashModes aren't null AND they contain the
	 *         flashMode, else returns false
	 */
	protected boolean isValidFlashMode(List<String> flashModes, String flashMode) {
		if (flashModes != null && flashModes.contains(flashMode)) {
			return true;
		}
		return false;
	}

	private Handler.Callback mCameraMsgCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_OPEN_CAMERA:
				if (msg.obj != null) {
					msg.obj = openCameraWithPreview();
				} else {
					msg.obj = openCameraWithoutPreview();
				}
				break;
			case MSG_START_PREVIEW:
				startPreview((SurfaceTexture)msg.obj);
				break;
			case MSG_STOP_PREVIEW:
				stopPreview();
				break;
			case MSG_CLOSE_CAMERA:
				closeCameraInner();
				break;
			case MSG_SET_FRAME_RATE:
				if (setFrameRate(msg.arg1, null)) {
					msg.obj = ACResult.SUCCESS;
				} else {
					msg.obj = new ACResult(ACResult.ACS_EXCEED_VALUE, "frame rate exceed camera supported range");
				}
				break;
			case MSG_SET_FLASH_MODE:
				msg.obj = setFlashMode(msg.arg1!=0?true:false);
				break;
			default:
				return false;
			}
			return true;
		}
	};

}

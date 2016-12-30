package com.lingyang.sdk.av;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioCapturer;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.capturer.ACFrame;
import com.antelope.sdk.capturer.ACFrameAvailableListener;
import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACImageResolution;
import com.antelope.sdk.capturer.ACVideoCapturer;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.codec.ACAudioEncoder;
import com.antelope.sdk.codec.ACAudioEncoderFactory;
import com.antelope.sdk.codec.ACCodecID;
import com.antelope.sdk.codec.ACEncodeMode;
import com.antelope.sdk.codec.ACPacketAvailableListener;
import com.antelope.sdk.codec.ACStreamPacket;
import com.antelope.sdk.codec.ACVideoEncoder;
import com.antelope.sdk.codec.ACVideoEncoderFactory;
import com.antelope.sdk.streamer.ACStreamer;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.view.LYGLCameraView;

import java.util.List;

import static com.lingyang.sdk.util.Preconditions.checkNotNull;

/**
 * @author liaolei
 */
public abstract class AVRecorder {

	protected SessionConfig mConfig;
	protected Context mContext;
	protected boolean mIsUseAudio, mIsUseVideo, mIsPreview;

	protected ACVideoCapturer mVideoCapture;
	protected ACAudioCapturer mAudioCapture;
	protected ACAudioEncoder mAudioEncoder;
	protected ACVideoEncoder mVideoEncoder;
	protected ACStreamer mStreamer;
	protected int mImageFormat = ACImageFormat.AC_IMAGE_FMT_YUV420P;

	protected boolean isVideoCapture, isAudioCapture, isConnect;
	protected boolean flashOn;
	protected int resolution;
	protected Handler mUIHandler;

	protected CameraPreviewCallback mCameraPreviewCallback;// 对外预览数据回调监听
	protected boolean mVideoCapturerInitialized = false;
	// 本来预览截图
	protected boolean mIsCaptureSnapchat = false;
	protected String mCaptureSnapchatPath, mCaptureSnapchatName;
	protected CallBackListener<String> mCaptureSnapchatListener;
	private int mAudioMode,mAudioCodeId;

	public AVRecorder(Context context, SessionConfig config,boolean isBroadcast) {
		mContext = context;
		mConfig = config;
		isAudioCapture = false;
		isVideoCapture = false;
		isConnect = false;
		mUIHandler = getUIHandler();
		if(isBroadcast){
			mAudioCodeId=ACCodecID.AC_CODEC_ID_AAC;
			mAudioMode=ACEncodeMode.AC_ENC_MODE_HARD;
		}else{
			mAudioCodeId=ACCodecID.AC_CODEC_ID_OPUS;
			mAudioMode=ACEncodeMode.AC_ENC_MODE_SOFT;
		}
		resolveConfig();
		init();

	}

	protected abstract Handler getUIHandler();

	private void resolveConfig() {
		if (mConfig.getVideoWidth() == 640 && mConfig.getVideoHeight() == 480) {
			resolution = ACImageResolution.R480;
		} else if (mConfig.getVideoWidth() == 1280 && mConfig.getVideoHeight() == 720) {
			resolution = ACImageResolution.R720P;
		} else if (mConfig.getVideoWidth() == 1920 && mConfig.getVideoHeight() == 1080) {
			resolution = ACImageResolution.R1080P;
		} else {
			resolution = ACImageResolution.R480;
		}
	}

	protected void init() {
		flashOn = false;
		// 视频编码器
		mVideoEncoder = ACVideoEncoderFactory.createVideoEncoder(mConfig.getVideoEncoderConfig().isHardEncode()
				? ACEncodeMode.AC_ENC_MODE_HARD : ACEncodeMode.AC_ENC_MODE_SOFT, ACCodecID.AC_CODEC_ID_H264);
		mVideoEncoder.initialize(mConfig.getVideoWidth(), mConfig.getVideoHeight(), 15, mVideoEncodeListener);
		int[] supportFormat = mVideoEncoder.getSupportedImageFormat();
		if (supportFormat != null)
			mImageFormat = supportFormat[0];
		// 视频采集
		mVideoCapture = new ACVideoCapturer();
		mVideoCapture.initialize(mContext, true, mVideoCapturerListener, mImageFormat);
		mVideoCapture.setVideoFrameRate(15);

		// 音频采集
		mAudioCapture = new ACAudioCapturer();
		mAudioCapture.initialize(mConfig.getAudioSampleRateInHz(), mConfig.getNumAudioChannels(), 16,
				mAudioCapturerListener);

//		mAudioCodeId=mConfig.getAudioEncoderConfig().getAudioEncoderType() == AudioEncoderType.AUDIO_ENCODER_TYPE_OF_AAC
//				? ACCodecID.AC_CODEC_ID_AAC : ACCodecID.AC_CODEC_ID_OPUS;
//		mAudioMode=mAudioCodeId==ACCodecID.AC_CODEC_ID_AAC ? ACEncodeMode.AC_ENC_MODE_HARD
//				: ACEncodeMode.AC_ENC_MODE_SOFT;

		// 音频编码器
		mAudioEncoder = ACAudioEncoderFactory.createAudioEncoder(mAudioMode,mAudioCodeId);
		mAudioEncoder.initialize(mConfig.getAudioSampleRateInHz(), mConfig.getNumAudioChannels(), 16,
				mConfig.getAudioBitrate(), mAudioEncodeListener);

	}

	/**
	 * 视频采集原始数据回调
	 */
	protected ACFrameAvailableListener mVideoCapturerListener = new ACFrameAvailableListener() {

		@Override
		public void onFrameAvailable(ACFrame frame) {
//			CLog.e("video frame capture" + isVideoCapture);
			if (isVideoCapture) {
//				CLog.e("video frame capture");
				mVideoEncoder.encode((ACVideoFrame) frame, false);
			}
		}
	};

	/**
	 * 音频采集原始数据回调
	 */
	protected ACFrameAvailableListener mAudioCapturerListener = new ACFrameAvailableListener() {

		@Override
		public void onFrameAvailable(ACFrame frame) {
			if (isAudioCapture) {
				 mAudioEncoder.encode((ACAudioFrame) frame);
			}
		}
	};

	/**
	 * 视频编码数据回调
	 */
	protected ACPacketAvailableListener mVideoEncodeListener = new ACPacketAvailableListener() {

		@Override
		public void onPacketAvailable(ACStreamPacket packet) {
			if (isVideoCapture) {
				mStreamer.write(packet);
			}
		}
	};

	/**
	 * 音频编码数据回调
	 */
	protected ACPacketAvailableListener mAudioEncodeListener = new ACPacketAvailableListener() {

		@Override
		public void onPacketAvailable(ACStreamPacket packet) {
			if (isAudioCapture) {
				 ACResult result=mStreamer.write(packet);
			}
		}
	};

	protected void openCamera() {
		mVideoCapture.openCamera(mConfig.getDesiredCamera(), resolution);
	}

	protected void openMic() {
		mAudioCapture.openMicrophone(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
	}

	// protected void VideoCapture() {
	// WorkThreadScanner.getInstance().executeTask(new Runnable() {
	//
	// @Override
	// public void run() {
	// ACVideoFrame videoFrame = new ACVideoFrame();
	// ACStreamPacket videoPacket = new ACStreamPacket();
	// videoPacket.buffer = ByteBuffer.allocateDirect(1024 * 1024);
	//
	// while (isVideoCapture) {
	// if (mVideoCapture.readVideoFrame(videoFrame, 3000).isResultOK()) {
	// if (mVideoEncoder.encode(videoFrame, videoPacket, false).isResultOK()) {
	// mStreamer.write(videoPacket);
	// }
	// }
	// }
	// }
	// });
	// }

	// protected void AudioCapture() {
	// WorkThreadScanner.getInstance().executeTask(new Runnable() {
	//
	// @Override
	// public void run() {
	// ACAudioFrame audioFrame = new ACAudioFrame();
	// ACStreamPacket audioPacket = new ACStreamPacket();
	// audioPacket.buffer = ByteBuffer.allocateDirect(1024 * 1024);
	//
	// while (isAudioCapture) {
	// if (mAudioCapture.readAudioFrame(audioFrame).isResultOK()) {
	// if (mAudioEncoder.encode(audioFrame, audioPacket).isResultOK()) {
	// mStreamer.write(audioPacket);
	// }
	// }
	// }
	// }
	// });
	// }

	public void startAudioRecording() {
		if (!isAudioCapture && isConnect) {
			isAudioCapture = true;
		}
	}

	public void stopAudioRecording() {
		isAudioCapture = false;
	}

	public void startVideoRecording() {
		if (!isVideoCapture && isConnect) {
			isVideoCapture = true;
		}
	}

	public void stopVideoRecording() {
		isVideoCapture = false;
	}

//	/**
//	 * 添加滤镜
//	 *
//	 * @param filter
//	 *            {@link Filters#FILTER_NONE} 无 <br/>
//	 *            {@link Filters#FILTER_BLACK_WHITE} 黑白 <br/>
//	 *            etc..
//	 */
//	public void applyFilter(int filter) {
//	}

	public void switchCamera() {
		int cameraCount = Camera.getNumberOfCameras();
		if (cameraCount <= 1)
			return;
		if (getCurrentCamera() == cameraCount - 1) {
			mVideoCapture.closeCamera();
			mVideoCapture.openCamera(0, resolution);
			mConfig.setDesiredCamera(0);
		} else if (getCurrentCamera() < cameraCount - 1) {
			mVideoCapture.closeCamera();
			mVideoCapture.openCamera(getCurrentCamera() + 1, resolution);
			mConfig.setDesiredCamera(getCurrentCamera() + 1);
		}

	}

	public void setCameraType(int camera) {
		if (isVideoCapture)
			isVideoCapture = false;
		mVideoCapture.closeCamera();
		mConfig.setDesiredCamera(camera);
		openCamera();
		// isVideoCapture = true;
	}

	public int getCurrentCamera() {
		return mConfig.getDesiredCamera();
	}

	public int getDesiredCamera() {
		return mConfig.getDesiredCamera();
	}

	/**
	 * 切换闪光类型 在 TORCH 之间 OFF切换，设置效果在预览状态下会立刻生效
	 */
	public void toggleFlashMode() {
		flashOn = !flashOn;
		mVideoCapture.turnFlashLight(flashOn);
	}

	public void toggleFlash(){
		toggleFlashMode();
	}
	/**
	 * @return 返回闪光模式
	 */
	public String getFlashMode() {
		return flashOn ? Parameters.FLASH_MODE_TORCH : Parameters.FLASH_MODE_OFF;
	}

	public List<Camera.Size> getSupportedPreviewSizes() {
		return null;
	}

	public void adjustVideoBitrate(int targetBitRate) {
	}

	/* *//**
			 * Signal that the recorder should treat incoming video frames as
			 * Vertical Video, rotating and cropping them for proper display.
			 * <p/>
			 * has been set true for the current recording session.
			 */
	/*
	 * public void signalVerticalVideo(FullFrameRect.SCREEN_ROTATION
	 * orientation) { checkNotNull(mCamEncoder);
	 * mCamEncoder.signalVerticalVideo(orientation); }
	 */

	/**
	 * 设置闪光类型
	 * 
	 * @param desiredFlash
	 *            {@link Parameters#FLASH_MODE_TORCH} or
	 *            {@link Parameters#FLASH_MODE_OFF} etc
	 */
	public void setFlashMode(String desiredFlash) {
		if (desiredFlash == Parameters.FLASH_MODE_OFF) {
			flashOn = false;
			mVideoCapture.turnFlashLight(false);
		} else {
			flashOn = true;
			mVideoCapture.turnFlashLight(true);
		}
	}

	/**
	 * 为接下来的录制做准备. 根据给定的参数重置录制相关的配置参数 必须在 {@link #stopRecording()} 之后
	 * {@link #release()}之前 调用
	 * 
	 * @param config
	 */
	public void reset(SessionConfig config) {
		checkNotNull(config);
		release();
		mConfig = config;
		init();
	}

	/**
	 * 在宿主Activity onPause()回调时使用，节省系统资源 仅语音通讯时无需调用，支持后台语音
	 * 停止采集再调用此方法，会放弃对camera的使用权，否则，不会释放camera，而且会继续采集
	 */
	public void onHostActivityPaused() {
		if(mVideoCapture!=null)
			mVideoCapture.enterBackground();
	}

	/**
	 * 在宿主Activity onResume()回调时使用，获取对camera的使用权 仅语音通讯时无需调用，支持后台语音
	 */
	public void onHostActivityResumed() {
		if(mVideoCapture!=null)
			mVideoCapture.enterForeground();
	}

	/**
	 * 设置是否使用AudioEffect进行声音效果优化，降噪，回声消除,自动增益
	 * 
	 * @param use
	 *            默认为true，只有在硬编时候才有用
	 */
	public void useAudioOptimization(boolean use) {
	}

	/**
	 * 本地预览截图
	 * 
	 * @param path
	 *            绝对路径
	 * @param name
	 *            名称
	 * @param scale
	 *            比例
	 * @param listener
	 *            结果回调 #onSuccess or #onError
	 */
	public void captureFrame(String path, String name, int scale, CallBackListener<String> listener) {
		if (TextUtils.isEmpty(path) || TextUtils.isEmpty(name)) {
			if (listener != null)
				listener.onError(new LYException(-1, "invalid value"));
			return;
		}
		mCaptureSnapchatPath = path;
		mCaptureSnapchatName = name;
		mCaptureSnapchatListener = listener;
		mIsCaptureSnapchat = true;
	}

	/**
	 * 设置此回调，可获取到预览数据进行自定义处理再发送给编码库
	 * 
	 * @param sendDataType
	 * @param cameraPreviewCallback
	 *            预览数据回调接口
	 */
	public void setCameraPreviewCallback(int sendDataType, boolean mirror,
			CameraPreviewCallback cameraPreviewCallback) {
		mImageFormat = sendDataType;
		mCameraPreviewCallback = cameraPreviewCallback;
		initVideoCapture();
	}

	private void initVideoCapture() {
		if (mVideoCapturerInitialized && mVideoCapture != null) {
			mVideoCapture.release();
		}
		mVideoCapture.initialize(mContext, true, mVideoCapturerListener, mImageFormat);
		mVideoCapture.setVideoFrameRate(15);
		mVideoCapturerInitialized = true;
	}

	/**
	 * 释放资源. Must be called after {@link #stopRecording()} After this call this
	 * instance may no longer be used.
	 */
	public void release() {
		isAudioCapture = false;
		isVideoCapture = false;
		if (mVideoCapture != null) {
			mVideoCapture.closeCamera();
			mVideoCapture.release();
		}
		if (mAudioCapture != null) {
			mAudioCapture.closeMicrophone();
			mAudioCapture.release();
		}
		if (mVideoEncoder != null) {
			mVideoEncoder.release();
		}
		if (mAudioEncoder != null) {
			mAudioEncoder.release();
		}
		if (mStreamer != null) {
			mStreamer.close();
			mStreamer.release();
		}
	}

	protected boolean isRecording() {
		return isAudioCapture && isAudioCapture;
	}

	protected void setPreviewDisplay(LYGLCameraView display) {
		checkNotNull(display);
	}

//	abstract protected void closePreview();
}

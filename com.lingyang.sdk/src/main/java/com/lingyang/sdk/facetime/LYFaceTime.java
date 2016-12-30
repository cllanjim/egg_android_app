package com.lingyang.sdk.facetime;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.capturer.ACFrame;
import com.antelope.sdk.capturer.ACShape;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.codec.ACCodecID;
import com.antelope.sdk.codec.ACStreamPacket;
import com.antelope.sdk.player.ACMediaExtra;
import com.antelope.sdk.player.ACPlayer;
import com.antelope.sdk.streamer.ACProtocolType;
import com.antelope.sdk.streamer.ACStreamerFactory;
import com.antelope.sdk.utils.HandlerUtil;
import com.antelope.sdk.utils.WorkThreadExecutor;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.av.AVRecorder;
import com.lingyang.sdk.av.ICameraOperation;
import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.cloud.IService.CloudMessage;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.cloud.LYService.onConnectedListener;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.util.CLog;
import com.lingyang.sdk.view.LYGLCameraView;
import com.lingyang.sdk.view.SurfaceFrameShape;

import java.nio.ByteBuffer;

/**
 * @author liaolei
 */
public class LYFaceTime extends AVRecorder implements IFaceTime, ICameraOperation {

	private final int FACETIME_SUCCESS = 1;
	private final int FACETIME_FAIL = 2;
	private final int FACETIME_PLAY = 3;

	private ACPlayer mACPlayer;
	private boolean isPlayRemote;

	private LYPlayer mPlayerView;
	private String mConnectMsg = "";
	private OnPlayerVideoFrameListener mPlayerVideoFrameListener;
	private WorkThreadExecutor mWorkThreadExecutor;

	public LYFaceTime(Context context, SessionConfig config) {
		super(context, config,false);
		isPlayRemote = false;
		mWorkThreadExecutor = new WorkThreadExecutor("LYFaceTime");
		mWorkThreadExecutor.start(null);
		mStreamer = ACStreamerFactory.createStreamer(ACProtocolType.AC_PROTOCOL_QSUP);
		mStreamer.initialize(new ACMessageListener() {

			@Override
			public void onMessage(int type, Object message) {

			}

		});
		setConnectListener();
	}
	
	
	@Override
	protected Handler getUIHandler() {
		return new Handler(mContext.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case FACETIME_SUCCESS:
					if (mCallbackListener != null)
						mCallbackListener.onSuccess(0);
					break;
				case FACETIME_FAIL:
					if (mCallbackListener != null) {
						ACResult status = (ACResult) msg.obj;
						mCallbackListener.onError(new LYException(status.getCode(), status.getErrMsg()));
					}
					break;

				default:
					break;
				}
			}
		};
	}

	private void initPlayer(String url) {
		if (mPlayerView == null)
			return;
		mACPlayer = mPlayerView.getPlayer();
		mACPlayer.initialize(2000, 5000, ACCodecID.AC_CODEC_ID_H264, ACCodecID.AC_CODEC_ID_OPUS, 16000, 1,
				new ACMediaExtra() {

					@Override
					public ACResult processFrame(ACFrame frame) {
						if (mPlayerVideoFrameListener != null) {
							if (frame instanceof ACVideoFrame) {
								mPlayerVideoFrameListener.onVideoFrameAvailable(frame.buffer, frame.offset, frame.size,
										((ACVideoFrame) frame).width, ((ACVideoFrame) frame).height, frame.timestamp);
							}
							return new ACResult(ACResult.ACS_OK, "");
						}
						return new ACResult(ACResult.ACS_UNIMPLEMENTED, "");
					}

					@Override
					public ACResult decodePacket(ACStreamPacket packet, ACFrame frame) {
						return new ACResult(ACResult.ACS_UNIMPLEMENTED, "");
					}
				});
	}
	
	@Override
	public void setLocalPreview(LYGLCameraView previewView) {
		int shape = ACShape.AC_SHAPE_NONE;
		if (previewView.getShape() == SurfaceFrameShape.CIRCLE)
			shape = ACShape.AC_SHAPE_CIRCLE;
		if (previewView.getShape() == SurfaceFrameShape.RECTANGLE)
			shape = ACShape.AC_SHAPE_NONE;
		if (previewView.getShape() == SurfaceFrameShape.TRIANGLE)
			shape = ACShape.AC_SHAPE_TRIANGLE;
		mVideoCapture.setPreviewSurfaceView(previewView, shape, new Object());

		openCamera();
		openMic();
	}

	@Override
	public void setRemoteView(String remoteUrl, LYPlayer view) {
		mPlayerView = view;
	}

	private void setConnectListener() {
		LYService.getInstance().setOnConnectedListener(new onConnectedListener() {

			@Override
			public void accept(CloudMessage msg) {
				CLog.i("CloudMessage:" + msg);
				// 被连接方监听到对方连接成功的消息,开始
				if (msg.Name.equals("ConnectString")) {
					openRemote(msg.Message, mCallbackListener);
				}
			}
		});
	}

	@Override
	public void openRemote(String remoteUrl, final CallBackListener<Integer> callBackListener) {
		mCallbackListener = callBackListener;
		initPlayer(remoteUrl);
		CLog.e("video frame openRemote");
		mStreamer.open(remoteUrl, 3000, mConnectMsg, new ACResultListener() {

			@Override
			public void onResult(ACResult status) {
				if (status.isResultOK()) {
					isConnect = true;
					isPlayRemote = true;
					mWorkThreadExecutor.executeTask(mPlayRunnable);
					HandlerUtil.sendMsgToHandler(mUIHandler, FACETIME_SUCCESS);
					if (mConfig.isUseAudio())
						startAudioRecording();
					if (mConfig.isUseVideo())
						startVideoRecording();
				} else {
					HandlerUtil.sendMsgToHandler(mUIHandler, FACETIME_FAIL, status);
				}
			}
		});
	}

	Runnable mPlayRunnable = new Runnable() {

		@Override
		public void run() {
			while (isPlayRemote) {
				ACStreamPacket packet = new ACStreamPacket();
				packet.buffer = ByteBuffer.allocateDirect(1024 * 1024);
				if (mStreamer.read(packet, 1000).isResultOK()) {
					mACPlayer.playFrame(packet);
				}
			}
		}
	};

	@Override
	public void closeRemote(String remoteUrl) {
		// TODO Auto-generated method stub
		stopAudioRecording();
		stopVideoRecording();
		isPlayRemote = false;
		if (mStreamer != null)
			mStreamer.close();
		isConnect = false;
	}

	@Override
	public void setFitScreen(boolean isFit) {
		if (mPlayerView != null)
			mPlayerView.setFitScreen(isFit);
	}

	@Override
	public void unmute(String remoteUrl) {
		if (mPlayerView != null)
			mPlayerView.unmute();
	}

	@Override
	public void mute(String remoteUrl) {
		if (mPlayerView != null)
			mPlayerView.mute();
	}

	@Override
	public void setVideoBitrate(int aBitrate) {
		if (mVideoEncoder != null)
			mVideoEncoder.setBitRate(aBitrate);
	}

	private CallBackListener<Integer> mCallbackListener;

	@Override
	public void setCallBackListener(CallBackListener<Integer> callbackListener) {
		mCallbackListener = callbackListener;
	}

	@Override
	public void setVideoFrameCallback(OnPlayerVideoFrameListener aFrameListener, int frameFormat) {
		mPlayerVideoFrameListener = aFrameListener;
	}

	@Override
	public void sendP2PMessage(String msg) {
		if (mStreamer != null)
			mStreamer.sendMessage(msg);
	}

	@Override
	public String getMediaParam(int aParamType) {
		if (mPlayerView != null)
			return mPlayerView.getMediaParam(aParamType);
		return "";
	}

	@Override
	public void setConnectMsg(String msg) {
		mConnectMsg = msg;
	}

	@Override
	public void release() {
		closePreview();
	}

	@Override
	public void closePreview() {
		stopVideoRecording();
		stopAudioRecording();
		if (mVideoCapture != null) {
			mVideoCapture.closeCamera();
		}
		if (mAudioCapture != null) {
			mAudioCapture.closeMicrophone();
		}
	}
}
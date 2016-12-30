package com.lingyang.sdk.broadcast;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.capturer.ACShape;
import com.antelope.sdk.streamer.ACProtocolType;
import com.antelope.sdk.streamer.ACStreamerFactory;
import com.antelope.sdk.utils.HandlerUtil;
import com.lingyang.sdk.av.AVRecorder;
import com.lingyang.sdk.av.ICameraOperation;
import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.view.LYGLCameraView;
import com.lingyang.sdk.view.SurfaceFrameShape;

/**
 * @author liaolei
 */
public class LYLiveBroadcast extends AVRecorder implements ILiveBroadcast, ICameraOperation {

	private final int BROADCAST_STATUS_START = 101;
	private final int BROADCAST_STATUS_LIVE = 102;
	private final int BROADCAST_STATUS_STOP = 103;
	private final int BROADCAST_STATUS_ERROR = 104;

	private BroadcastListener mBroadcastListener;

	public LYLiveBroadcast(Context context, SessionConfig config) {
		super(context, config,true);
		mStreamer = ACStreamerFactory.createStreamer(ACProtocolType.AC_PROTOCOL_QSTP);
		mStreamer.initialize(new ACMessageListener() {

			@Override
			public void onMessage(int type, Object message) {

			}

		});
	}

	@Override
	protected Handler getUIHandler() {
		return new Handler(mContext.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BROADCAST_STATUS_START:
					if (mBroadcastListener != null)
						mBroadcastListener.onBroadcastStart();
					break;
				case BROADCAST_STATUS_LIVE:
					if (mBroadcastListener != null)
						mBroadcastListener.onBroadcastLive();
					break;
				case BROADCAST_STATUS_STOP:
					if (mBroadcastListener != null)
						mBroadcastListener.onBroadcastStop();
					;
					break;
				case BROADCAST_STATUS_ERROR:
					ACResult result = (ACResult) msg.obj;
					if (mBroadcastListener != null)
						mBroadcastListener.onBroadcastError(new LYException(result.getCode(), result.getErrMsg()));
					;
					break;

				default:
					break;
				}
			}
		};
	}

	@Override
	public void setBroadcastListener(BroadcastListener listener) {
		mBroadcastListener = listener;
	}

	@Override
	public void setLocalPreview(LYGLCameraView glSurfaceView) {
		int shape = ACShape.AC_SHAPE_NONE;
		if (glSurfaceView.getShape() == SurfaceFrameShape.CIRCLE)
			shape = ACShape.AC_SHAPE_CIRCLE;
		if (glSurfaceView.getShape() == SurfaceFrameShape.RECTANGLE)
			shape = ACShape.AC_SHAPE_NONE;
		if (glSurfaceView.getShape() == SurfaceFrameShape.TRIANGLE)
			shape = ACShape.AC_SHAPE_TRIANGLE;
		mVideoCapture.setPreviewSurfaceView(glSurfaceView, shape, new Object());

		openCamera();
		openMic();
	}

	@Override
	public void startBroadcasting(String connecUrl) {
		mStreamer.open(connecUrl, 3000, "", new ACResultListener() {

			@Override
			public void onResult(ACResult status) {
				if (status.isResultOK()) {
					isConnect = true;
					HandlerUtil.sendMsgToHandler(mUIHandler, BROADCAST_STATUS_START);
					// TODO 有不同的消息类型，包括连接成功，连接断开，连接异常等，待处理
					if (mConfig.isUseAudio())
						startAudioRecording();
					if (mConfig.isUseVideo())
						startVideoRecording();
					HandlerUtil.sendMsgToHandler(mUIHandler, BROADCAST_STATUS_LIVE);
				} else {
					HandlerUtil.sendMsgToHandler(mUIHandler, BROADCAST_STATUS_ERROR, status);
				}
			}
		});
	}

	@Override
	public void stopBroadcasting() {
		ACResult result = mStreamer.close();
		if (result.isResultOK()) {
			isConnect = false;
			HandlerUtil.sendMsgToHandler(mUIHandler, BROADCAST_STATUS_STOP);
		} else {
			HandlerUtil.sendMsgToHandler(mUIHandler, BROADCAST_STATUS_ERROR, result);
		}
	}

	// @Override
	// public void startAudioRecording() {
	// if (!isAudioCapture) {
	// isAudioCapture = true;
	// }
	// }
	//
	// @Override
	// public void stopAudioRecording() {
	// isAudioCapture = false;
	// }
	//
	// @Override
	// public void startVideoRecording() {
	// if (!isVideoCapture) {
	// isVideoCapture = true;
	// }
	// }
	//
	// @Override
	// public void stopVideoRecording() {
	// isVideoCapture = false;
	// }

	@Override
	public boolean isBroadcasting() {
		return isRecording();
	}

	@Override
	public void release() {
		stopBroadcasting();
		super.release();
	}

}
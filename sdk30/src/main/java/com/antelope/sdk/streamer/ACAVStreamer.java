package com.antelope.sdk.streamer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.antelope.sdk.ACMediaInfo;
import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACMessageType;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.codec.ACStreamPacket;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.WorkThreadExecutor;

/**
 * @author liaolei
 * @version 创建时间：2016年10月27日 类说明
 */
  class ACAVStreamer implements ACStreamer {
	private static final ACResult RESULT_NOT_READY = new ACResult(ACResult.ACS_NOT_READY, "not open");
	private boolean mIsInitialized = false;
	private long mStreamerHandler;
	private WorkThreadExecutor mWorkExecutor = null;
	private boolean mIsOpened = false;
	private ACMessageListener mMsgListener = null;
	
	public ACAVStreamer(long streamerHandler) {
		this.mStreamerHandler=streamerHandler;
	}

	@Override
	public ACResult initialize(ACMessageListener msgListener) {
		if(!ACPlatformAPI.hasAuthorize())
			return ACResult.NO_AUTHORIZATION;
		int ressult = EDKIStreamer.initialize(mStreamerHandler);
		if (ressult == ACResult.ACS_OK) {
			mWorkExecutor = new WorkThreadExecutor("WorkThread-AVStreamer");
			mWorkExecutor.start(null);
			mMsgListener = msgListener;
			mIsInitialized = true;
		}
		return new ACResult(ressult, null);
	}

	@Override
	public ACResult release() {
		if (!mIsInitialized) {
			if (mStreamerHandler != 0) {
				EDKIStreamer.destroy(mStreamerHandler);
			}
			return ACResult.UNINITIALIZED;
		}
		int result = EDKIStreamer.release(mStreamerHandler);
		EDKIStreamer.destroy(mStreamerHandler);
		mWorkExecutor.stop();
		mWorkExecutor.release();
		mWorkExecutor = null;
		mIsInitialized = false;
		return new ACResult(result, null);
	}

	@Override
	public void open(final String url, final int timeout, final String msg, final ACResultListener resultListener) {
		if (!mIsInitialized) {
			if (resultListener != null) {
				resultListener.onResult(ACResult.UNINITIALIZED);
			}
			CLog.e("streamer was not initialized");
			return;
		}
		mWorkExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				int result = EDKIStreamer.open(mStreamerHandler, url, timeout, msg, new EDKIStreamer.Callback() {
					@Override
					public void onMessage(EDKIStreamer.StreamerMessage message) {
						if (message != null) {
							handleMessage(message);
						}
					}
				});
				mIsOpened = true;
				if (resultListener != null) {
					resultListener.onResult(new ACResult(result, null));
				}
			}
		});
	}

	@Override
	public ACResult close() {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		int result = EDKIStreamer.close(mStreamerHandler);
		mIsOpened = false;
		return new ACResult(result, null);
	}

	@Override
	public ACResult write(ACStreamPacket packet) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (!mIsOpened) {
			return RESULT_NOT_READY;
		}
		int result = EDKIStreamer.write(mStreamerHandler,packet);
		return new ACResult(result, null);
	}

	@Override
	public ACResult read(ACStreamPacket packet, int timeout) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (packet.buffer == null) {
			packet.buffer = ByteBuffer.allocateDirect(1024*1024);
		}
		int result = EDKIStreamer.read(mStreamerHandler,packet);
		return new ACResult(result, null);
	}

	@Override
	public void seek(final long timestamp, final ACResultListener resultListener) {
		if (!mIsInitialized) {
			if (resultListener != null) {
				resultListener.onResult(ACResult.UNINITIALIZED);
			}
			return;
		}
		if (!mIsOpened) {
			if (resultListener != null) {
				resultListener.onResult(RESULT_NOT_READY);
			}
			return;
		}
		mWorkExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				int result = EDKIStreamer.seek(mStreamerHandler, (int)timestamp);
				if (resultListener != null) {
					resultListener.onResult(new ACResult(result, null));
				}
			}
		});
	}

	@Override
	public ACResult sendMessage(String msg) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (!mIsOpened) {
			return RESULT_NOT_READY;
		}
		int result = EDKIStreamer.sendMessage(mStreamerHandler,msg);
		return new ACResult(result, null);
	}

	@Override
	public String getMediaInfo(int info) {
		if (!mIsInitialized || !mIsOpened) {
			return null;
		}
		String infoStr = null;
		switch (info) {
		case ACMediaInfo.AC_MEDIA_INFO_UPLOAD_VIDEO_BITRATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_UPLOAD_BITRATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_UPLOAD_AUDIO_BITRATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_UPLOAD_VIDEO_FRAMERATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_UPLOAD_FRAMERATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_UPLOAD_AUDIO_FRAMERATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_VIDEO_BITRATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_DOWNLOAD_BITRATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_AUDIO_BITRATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_VIDEO_FRAMERATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_DOWNLOAD_FRAMERATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_AUDIO_FRAMERATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_BITRATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_SOURCE_BITRATE);
			int i = -1;
			try {
				i = Integer.parseInt(infoStr);
			} catch (NumberFormatException e) {
			}
			if (i >= 0) {
				infoStr = "" + (i*8);
			}
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_AUDIO_BITRATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_FRAMERATE:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_SOURCE_FRAMERATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_AUDIO_FRAMERATE:
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_SEND_RATIO:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_SOURCE_SEND_RATE);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIEW_NUMBER:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_SOURCE_VIEW_NUM);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_DNS_PARSE_TIME:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_DNS_PARSE_TIME);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_QSTP_TCP_CONNECT_TIME:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_QSTP_TCP_CONNECT_TIME);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_QSTP_HANDSHAKE_TIME:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_QSTP_HANDSHAKE_TIME);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_QSTP_COMMAND_TIME:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_QSTP_COMMAND_TIME);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_QSTP_RECVFRAME_TIME:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_QSTP_RECVFRAME_TIME);
			break;
		case ACMediaInfo.AC_MEDIA_INFO_QSTP_CONNECT_RELAYIP:
			infoStr = EDKIStreamer.getStreamParam(mStreamerHandler, EDKIStreamer.ACC_STREAMER_QSTP_CONNECT_RELAYIP);
			break;
		default:
			break;
		}
		return infoStr;
	}

	@Override
	public ACResult sendStreamProperty(long property, ByteBuffer data) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (!mIsOpened) {
			return RESULT_NOT_READY;
		}
		int result = EDKIStreamer.sendStreamProperty(mStreamerHandler, property, data);
		return new ACResult(result, null);
	}
	
	private void handleMessage(EDKIStreamer.StreamerMessage message) {
		CLog.d("streamer message: type=" + message.type);
		Object content = null;
		int type = 0;
		boolean valid = true;
		switch (message.type) {
		case EDKIStreamer.ACC_AUDIO_FRAME:
			type = ACMessageType.AC_MESSAGE_AUDIO_FRAME;
			message.content.limit(message.size);
			message.content.position(0);
			content = message.content;
			break;
		case EDKIStreamer.ACC_VIEW_NUM:
			content = Integer.valueOf(message.size);
			valid = false;
			break;
		case EDKIStreamer.ACC_DISCONNECTION:
			type = ACMessageType.AC_MESSAGE_DISCONNECTED;
			break;
		case EDKIStreamer.ACC_CONNECTION:
			type = ACMessageType.AC_MESSSAGE_RECONNECTED;
			break;
		default:
			valid = false;
			break;
		}
		if (mMsgListener != null && valid) {
			mMsgListener.onMessage(type, content);
		}
	}

}

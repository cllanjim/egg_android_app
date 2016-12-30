package com.antelope.sdk.streamer;

import java.nio.ByteBuffer;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.codec.ACStreamPacket;

/**
 * @author zl 2016/10/26
 */
class EDKIStreamer {
	static {
		System.loadLibrary("JNIStreamer_topvdn");
	}
	
	/// streamer message types
	static final int ACC_AUDIO_FRAME = 1;
	static final int ACC_VIEW_NUM = 2;
	static final int ACC_DISCONNECTION = 3;
	static final int ACC_CONNECTION = 4;
	
	/// streamer info
	static final int ACC_UPLOAD_BITRATE = 1;
	static final int ACC_UPLOAD_FRAMERATE = 2;
	static final int ACC_DOWNLOAD_BITRATE = 3;
	static final int ACC_DOWNLOAD_FRAMERATE = 4;
	static final int ACC_STREAMER_SOURCE_BITRATE = 5;
	static final int ACC_STREAMER_SOURCE_FRAMERATE = 6;
	static final int ACC_STREAMER_SOURCE_SEND_RATE = 7;
	static final int ACC_STREAMER_SOURCE_VIEW_NUM = 8;
	static final int ACC_STREAMER_DNS_PARSE_TIME = 9;
	static final int ACC_STREAMER_QSTP_TCP_CONNECT_TIME = 10;
	static final int ACC_STREAMER_QSTP_HANDSHAKE_TIME = 11;
	static final int ACC_STREAMER_QSTP_COMMAND_TIME = 12;
	static final int ACC_STREAMER_QSTP_RECVFRAME_TIME = 13;
	static final int ACC_STREAMER_QSTP_CONNECT_RELAYIP = 14;
    
	class StreamerMessage {
		int type;
		int size;
		ByteBuffer content;
	}
	
	interface Callback {
		void onMessage(StreamerMessage message);
	}

	// ----------------返回值 0是成功 其他的失败----------------------------

	static native int create(int type);

	static native int destroy(long streamer);
	
	static native int initialize(long streamer);
	
	static native int release(long streamer);

	static native int open(long streamer, String url, int timeout, String message, Callback callback);

	static native int close(long streamer);

	static native int read(long streamer, ACStreamPacket packet);

	static native int write(long streamer, ACStreamPacket packet);

	static native int seek(long streamer, int timestamp);

	static native int sendMessage(long streamer, String message);

	static native int read(ACStreamPacket packet);
	
	static native int sendStreamProperty(long streamer, long property, ByteBuffer data);

	static native String getStreamParam(long streamer, int type);
}

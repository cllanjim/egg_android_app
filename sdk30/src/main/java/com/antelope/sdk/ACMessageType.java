package com.antelope.sdk;

public class ACMessageType {
	public static final int AC_MESSAGE_CLOUD_SERVICE = 0; /// cloud message, content type is String
	public static final int AC_MESSAGE_AUDIO_FRAME = 1; /// audio frame, content type is ByteBuffer
	public static final int AC_MESSAGE_DISCONNECTED = 3; /// QSTP was disconnected, content is null
	public static final int AC_MESSSAGE_RECONNECTED = 4; /// QSTP was reconnected successfully, content is null
}

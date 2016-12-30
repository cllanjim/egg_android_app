package com.antelope.sdk.streamer;

public class ACStreamerFactory {

	/**
	 * 创建指定协议类型的Streamer
	 * 
	 * @param protocol
	 *            协议类型 {@link ACProtocolType}
	 * @return
	 */
	public static ACStreamer createStreamer(int protocol) {
		long streamerHandler = EDKIStreamer.create(protocol);
		if (streamerHandler == 0) {
			return null;
		}
		ACStreamer acStreamer = new ACAVStreamer(streamerHandler);
		return acStreamer;
	}

}

package com.antelope.sdk.codec;

import com.antelope.sdk.capturer.ACVideoFrame;

class ACVideoSoftDecoder {
	/**
	 * Create native video soft decoder
	 * 
	 * @param codecid
	 *            See {@link ACCodecID}
	 * @return Native decoder handle
	 */
	static native long Create(int codecid);

	static native int Initialize(long decoder);

	static native int Release(long decoder);

	static native int Decode(long decoder, ACStreamPacket packet, ACVideoFrame frame);

	static native int Reset(long decoder);
}

package com.antelope.sdk.codec;

import com.antelope.sdk.capturer.ACAudioFrame;

class ACAudioSoftDecoder {
	/**
	 * Create native audio soft decoder
	 * 
	 * @param codecid
	 *            See {@link ACCodecID}
	 * @return Native decoder handle
	 */
	static native long Create(int codecid);

	static native int Initialize(long decoder, int samplerate, int channels);

	static native int Release(long decoder);

	static native int Decode(long decoder, ACStreamPacket packet, ACAudioFrame frame);

	static native int Reset(long decoder);
}

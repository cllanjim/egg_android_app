package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;

public class ACAudioSoftEncoder implements ACAudioEncoder {
	private long mEncoder;
	private ACStreamPacket mEncodedPacket = new ACStreamPacket();
	private ACPacketAvailableListener mEncodedFrameListener;
	
	ACAudioSoftEncoder(long encoder) {
		mEncoder = encoder;
	}
	
	@Override
	public ACResult initialize(int sampleRate, int channelCount, int bitSize, int bitRate, ACPacketAvailableListener packetListener) {
		int result = Initialize(mEncoder, sampleRate, channelCount, bitSize, bitRate);
		mEncodedFrameListener = packetListener;
		return new ACResult(result, null);
	}
	
	@Override
	public ACResult release() {
		int result = Release(mEncoder);
		return new ACResult(result, null);
	}
	
	@Override
	public ACResult encode(ACAudioFrame frame) {
		int result = Encode(mEncoder, frame, mEncodedPacket);
		if (result == ACResult.ACS_OK) {
			if (mEncodedFrameListener != null) {
				mEncodedFrameListener.onPacketAvailable(mEncodedPacket);
			}
		}
		return new ACResult(result, null);
	}
	
	@Override
	public ACResult reset() {
		int result = Reset(mEncoder);
		return new ACResult(result, null);
	}
	
	/**
	 * Create native audio encoder
	 * @param codecid See {@link ACCodecID}
	 * @return Native encoder handle
	 */
	static native long Create(int codecid);
	static native int Initialize(long encoder, int samplerate, int channels, int bitwidth, int bitrate);
	static native int Release(long encoder);
	static native int Encode(long encoder, ACAudioFrame frame, ACStreamPacket packet);
	static native int Reset(long encoder);
}

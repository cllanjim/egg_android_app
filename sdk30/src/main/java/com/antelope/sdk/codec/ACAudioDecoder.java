package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;

public interface ACAudioDecoder {
	ACResult initialize(int sampleRate, int channelCount);

	ACResult release();

	ACResult decode(ACStreamPacket packet, ACAudioFrame frame);

	ACResult reset();
}

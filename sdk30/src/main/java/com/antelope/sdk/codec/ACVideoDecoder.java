package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACVideoFrame;

public interface ACVideoDecoder {
	ACResult initialize();
	ACResult release();
	ACResult decode(ACStreamPacket packet, ACVideoFrame frame);
	ACResult reset();
}

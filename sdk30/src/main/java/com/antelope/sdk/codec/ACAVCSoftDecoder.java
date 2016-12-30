package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACVideoFrame;

public class ACAVCSoftDecoder implements ACVideoDecoder {
	private long mDecoder = 0;

	@Override
	public ACResult initialize() {
		if (mDecoder != 0) {
			return ACResult.SUCCESS;
		}
		long decoder = ACVideoSoftDecoder.Create(ACCodecID.AC_CODEC_ID_H264);
		if (decoder == 0) {
			return new ACResult(ACResult.ACS_UNKNOWN, "failed to create native avc decoder");
		}
		int result = ACVideoSoftDecoder.Initialize(decoder);
		if (result != ACResult.ACS_OK) {
			return new ACResult(result, "failed to initialize native avc decoder");
		}
		mDecoder = decoder;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult release() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACVideoSoftDecoder.Release(mDecoder);
		mDecoder = 0;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult decode(ACStreamPacket packet, ACVideoFrame frame) {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		int result = ACVideoSoftDecoder.Decode(mDecoder, packet, frame);
		if (result != ACResult.ACS_OK) {
			if (result == ACResult.ACC_AVC_DECODE_NOT_GOT_PIC) {
				return ACResult.IN_PROCESS;
			}
			return new ACResult(result, null);
		}
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult reset() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACVideoSoftDecoder.Reset(mDecoder);
		return ACResult.SUCCESS;
	}
	
}

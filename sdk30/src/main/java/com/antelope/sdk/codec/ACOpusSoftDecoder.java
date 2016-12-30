package com.antelope.sdk.codec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.utils.CLog;

public class ACOpusSoftDecoder implements ACAudioDecoder {
	private long mDecoder = 0;

	@Override
	public ACResult initialize(int sampleRate, int channelCount) {
		if (mDecoder != 0) {
			return ACResult.SUCCESS;
		}
		long decoder = ACAudioSoftDecoder.Create(ACCodecID.AC_CODEC_ID_OPUS);
		if (decoder == 0) {
			return new ACResult(ACResult.ACS_UNKNOWN, "failed to create native opus decoder");
		}
		int result = ACAudioSoftDecoder.Initialize(decoder, sampleRate, channelCount);
		if (result != ACResult.ACS_OK) {
			return new ACResult(result, "failed to initialize native opus decoder");
		}
		mDecoder = decoder;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult release() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACAudioSoftDecoder.Release(mDecoder);
		mDecoder = 0;
		return ACResult.SUCCESS;
	}

//	private FileOutputStream mFileOutputStream = null;
//	private void saveAudioFrame(ACAudioFrame frame) {
//		if (mFileOutputStream == null) {
//			try {
//				mFileOutputStream = new FileOutputStream(new File("/sdcard/save.pcm"));
//			} catch (FileNotFoundException e) {
//				CLog.e("open save.pcm", e);
//			}
//		}
//		if (mFileOutputStream != null) {
//			frame.buffer.limit(frame.offset+frame.size);
//			frame.buffer.position(frame.offset);
//			try {
//				mFileOutputStream.getChannel().write(frame.buffer);
//			} catch (IOException e) {
//			}
//		}
//	}
	
	@Override
	public ACResult decode(ACStreamPacket packet, ACAudioFrame frame) {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		int result = ACAudioSoftDecoder.Decode(mDecoder, packet, frame);
		if (result != ACResult.ACS_OK) {
			if (result == ACResult.ACC_AAC_DECODE_GOTFRAME_FAILED) {
				return ACResult.IN_PROCESS;
			}
			return new ACResult(result, null);
		}
//		saveAudioFrame(frame);
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult reset() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACAudioSoftDecoder.Reset(mDecoder);
		return ACResult.SUCCESS;
	}

}

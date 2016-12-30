package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;

public class ACAudioEncoderFactory {
   
	static {
		System.loadLibrary("acencoder");
	}
	
	/**
	 * 创建音频编码器对象
	 * @param mode 编码模式，软编or硬编 {@link ACEncodeMode}
	 * @param id 编码类型 OPUS or AAC {@link ACCodecID}
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public static ACAudioEncoder createAudioEncoder(int mode, int codec){
		if (mode == ACEncodeMode.AC_ENC_MODE_HARD) {
			switch (codec) {
			case ACCodecID.AC_CODEC_ID_AAC:
				return new ACAACHardEncoder();
			case ACCodecID.AC_CODEC_ID_OPUS:
				break;
			}
		} else if (mode == ACEncodeMode.AC_ENC_MODE_SOFT) {
			if (codec == ACCodecID.AC_CODEC_ID_AAC) {
				return null; // 暂不支持AAC软编
			}
			long encoder = ACAudioSoftEncoder.Create(codec);
			if (encoder != 0) {
				return new ACAudioSoftEncoder(encoder);
			}
		}
		return null;
	}
	
}

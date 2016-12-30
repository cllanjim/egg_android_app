package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;

public class ACVideoEncoderFactory {

	/**
	 * 创建视频编码器对象
	 * @param mode 编码模式，软编or硬编 {@link ACEncodeMode}
	 * @param id 编码类型 目前只支持H264编码，后续会加上H265编码类型 {@link ACCodecID}
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public static ACVideoEncoder createVideoEncoder(int mode, int id){
		if (mode == ACEncodeMode.AC_ENC_MODE_HARD) {
			switch (id) {
			case ACCodecID.AC_CODEC_ID_H264:
				return new ACAVCHardEncoder();
			}
		} else if (mode == ACEncodeMode.AC_ENC_MODE_SOFT) {
			long encoder = ACVideoSoftEncoder.Create(id);
			if (encoder != 0) {
				return new ACVideoSoftEncoder(encoder);
			}
		}
		return null;
	}
	
}

package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;

/**
* @author liaolei 
* @version 创建时间：2016年9月8日 
* 该类用于音频编码
*/
public interface ACAudioEncoder {
	
	/**
	 * 初始化音频编码器相关
	 * @param sampleRate 采样率，单位为HZ
	 * @param channelCount 声道数
	 * @param bitSize 量化位数，单位为比特位
	 * @param bitRate 码率，单位为Kbps
	 * @param packetListener 编码数据回调接口
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult initialize(int sampleRate, int channelCount, int bitSize, int bitRate, ACPacketAvailableListener packetListener);
	
	/**
	 * 反初始化，释放编码器相关资源，在退出编码器的时候必须调用；
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult release();
	
	/**
	 * 编码
	 * @param frame 待编码数据
	 * @param packet 编码完成后的数据放入该参数
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult encode(ACAudioFrame frame);

	
	/**
	 * 重置编码器
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult reset();
	
}

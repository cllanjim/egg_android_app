package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACVideoFrame;

import android.view.Surface;

/**
* @author liaolei 
* @version 创建时间：2016年9月8日 
* 视频编码接口
*/
public interface ACVideoEncoder {
	
	/**
	 * 初始化视频编码器相关
	 * @param width 视频宽
	 * @param height 视频高
	 * @param gop group of picture，I帧间隔,单位秒，最小1秒
	 * @param listener 编码数据监听
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult initialize(int width, int height, int gop, ACPacketAvailableListener listener);
	
	/**
	 * 获取输入Surface，硬编采用surface传数据效率更高
	 * @return surface
	 */
	Surface getInputSurface();
	
	/**
	 * 获取编码器支持的图像格式
	 * @return 图像格式，详细定义请参考{@link com.antelope.sdk.capturer.ACImageFormat}
	 */
	int[] getSupportedImageFormat();
	
	/**
	 * 编码
	 * @param frame 待编码数据
	 * @param forceKeyFrame 是否强制关键帧
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult encode(ACVideoFrame frame, boolean forceKeyFrame);
	
	/**
	 * 反初始化，释放编码器相关资源，在退出编码器的时候必须调用；
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult release();
	
	/**
	 * 动态设置码率
	 * @param bitRate 码率，kbps
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult setBitRate(int bitRate);
	
	/**
	 * 动态设置帧率
	 * @param frameRate 帧率
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult setFrameRate(int frameRate);
	
	/**
	 * 重置编码器
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult reset();
	
}

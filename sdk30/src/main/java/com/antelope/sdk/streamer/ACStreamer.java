package com.antelope.sdk.streamer;

import java.nio.ByteBuffer;

import com.antelope.sdk.ACMediaInfo;
import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.codec.ACStreamPacket;

/**
 * 流化器
 * 
 * @author weil
 */
public interface ACStreamer {

	/**
	 * 初始化
	 * 
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult initialize(ACMessageListener msgListener);

	/**
	 * 释放资源
	 * 
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult release();

	/**
	 * 开启Streamer
	 * 
	 * @param url
	 *            连接串地址
	 * @param timeout
	 *            连接超时，单位毫秒
	 * @param resultListener
	 *            状态监听
	 */
	void open(String url, int timeout, String msg, ACResultListener resultListener);

	/**
	 * 关闭Streamer
	 * 
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult close();

	/**
	 * 写入一帧数据
	 * 
	 * @param packet
	 *            数据包
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult write(ACStreamPacket packet);

	/**
	 * 读取一帧数据
	 * 
	 * @param packet
	 *            数据包
	 * @param timeout
	 *            等待超时，单位毫秒
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	ACResult read(ACStreamPacket packet, int timeout);

	/**
	 * 跳转到指定的时间戳（录像下载）
	 * 
	 * @param timestamp
	 *            绝对时间，秒
	 * @param resultListener
	 */
	void seek(long timestamp, ACResultListener resultListener);

	/**
	 * 发送消息
	 * 
	 * @param msg
	 *            要发关的消息内容，长度不能超过255个字节
	 * @return 错误码及错误描述
	 */
	ACResult sendMessage(String msg);
	
	/**
	 * 获取流媒体统计信息
	 * @param info 需要获取的信息 {@link ACMediaInfo}
	 * @return 流媒体信息
	 */
	String getMediaInfo(int info);
	
	/**
	 * 发送流属性消息
	 * @param property
	 * @param data
	 * @return
	 */
	ACResult sendStreamProperty(long property, ByteBuffer data);
}

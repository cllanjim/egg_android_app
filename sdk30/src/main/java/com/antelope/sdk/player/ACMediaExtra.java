package com.antelope.sdk.player;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACFrame;
import com.antelope.sdk.codec.ACStreamPacket;

/**
* @author liaolei 
* @version 创建时间：2016年9月8日 
* 类说明
*/
public interface ACMediaExtra {
	ACResult decodePacket(ACStreamPacket packet, ACFrame frame);
	ACResult processFrame(ACFrame frame);
}

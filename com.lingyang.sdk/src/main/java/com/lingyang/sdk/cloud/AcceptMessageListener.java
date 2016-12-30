package com.lingyang.sdk.cloud;

import com.lingyang.sdk.cloud.IService.CloudMessage;

/**
 * 云消息接收回调监听
 * @author liaolei
 *
 */
public interface AcceptMessageListener {

	 void accept(CloudMessage message);
}

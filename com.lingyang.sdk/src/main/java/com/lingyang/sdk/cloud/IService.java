package com.lingyang.sdk.cloud;

import com.lingyang.sdk.CallBackListener;

/**
 * 云平台接口
 */
public interface IService {

    /**
     * 获取底层SDK版本号
     *
     * @return
     */
    String getSDKVersion();

    /**
     * 是否打印日志
     * @param debuggable
     */
    void setDebuggable(boolean debuggable);

    /**
     * 是否启用底层日志打印
     *
     * @param enabled
     */
    void setNativeLoggingEnabled(boolean enabled);

    /**
     * 启动云服务。
     * 调用了此api之后,平台相关凭证及资源开始准备，并且在回调接口通知云服务是否启动成功！
     * 建议在客户端登录验证逻辑通过之后即刻调用。服务启动之后，相关的平台接口才能正常使用。
     */
    void startCloudService(String userToken, String config, CallBackListener<Long> listener);

    /**
     * 停止云服务
     * 调用了此api之后，相应的底层资源完全释放，建议在应用退出的时候调用，节省系统资源！
     */
    void stopCloudService();

    /**
     * 平台在线状态
     *
     * @return
     */
    boolean isOnline();

    /**
     * 设置云消息接收回调
     *
     * @param acceptCloudMessageListener
     */
    void setCloudMessageListener(AcceptMessageListener acceptCloudMessageListener);

    /**
     * 平台消息回调
     */
    class CloudMessage {
        public String Name;//消息类型
        public String Message;//消息
        public int SrcID;

    }

}

package com.lingyang.sdk.broadcast;

import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.view.LYGLCameraView;

/**
 * 手机直播
 *
 * @author 波
 */
public interface ILiveBroadcast {
    /**
     * QSTP广播
     */
//    int MODE_LIVE = 2;
    /**
     * 录像广播
     */
//    int MODE_LIVE_AND_RECORD = 4;

    /**
     * 设置直播状态监听
     *
     * @param listener
     */
    void setBroadcastListener(BroadcastListener listener);

    /**
     * 设置本地预览view
     *
     * @param glSurfaceView
     */
    void setLocalPreview(LYGLCameraView glSurfaceView);

    /**
     * 连接平台服务器直播
     *
     * @return
     */
    void startBroadcasting(String connecUrl);

    /**
     * 关闭直播
     */
    void stopBroadcasting();

    /**
     * 重新初始化直播相关参数. 必须在 {@link #stopBroadcasting()}之后调用
     *
     * @param config
     */
    void reset(SessionConfig config);

    /**
     * 直播过程中重新开始声音采集发送
     */
    void startAudioRecording();

    /**
     * 直播过程中停止声音采集发送
     */
    void stopAudioRecording();

    /**
     * 直播过程中重新开始视频数据发送
     */
    void startVideoRecording();

    /**
     * 直播过程中停止视频数据发送
     */
    void stopVideoRecording();
    /**
     * 是否正在直播
     */
    boolean isBroadcasting();

//    /**
//     * 添加滤镜
//     *
//     * @param filter {@link Filters#FILTER_NONE} 无 <br/>
//     *               {@link Filters#FILTER_BLACK_WHITE} 黑白 <br/>
//     *               etc..
//     */
//    void applyFilter(int filter);

    /**
     * 释放采集器，编码器资源.
     */
//    void release();

}

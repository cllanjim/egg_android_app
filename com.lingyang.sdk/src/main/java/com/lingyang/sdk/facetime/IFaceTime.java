package com.lingyang.sdk.facetime;

import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.av.CameraPreviewCallback;
import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.player.IMediaParamProtocol;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.view.LYGLCameraView;

/**
 * 音视频互联接口
 *
 */
public interface IFaceTime {
    /**
     * 打开本地预览
     *
     * @param previewView
     */
    void setLocalPreview(LYGLCameraView previewView);

    /**
     * 设置远程视频播放器view
     *
     * @param view @param view      播放展示view
     */
    void setRemoteView(String remoteUrl, LYPlayer view);

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
     * 打开跟对方的链接并推送采集数据
     *
     * @param remoteUrl 远程播放地址,被链接方设置为null
     */
    void openRemote(String remoteUrl, CallBackListener<Integer> callBackListener);


    /**
     * 断开跟对方的链接并推送采集数据
     *
     * @param remoteUrl 远程播放地址,被链接方设置为null
     */
    void closeRemote(String remoteUrl);

    /**
     * 设置video是否自适应屏幕 必须在setRemoteView之后调用
     * @param isFit true适应  false不适应
     */
    void setFitScreen(boolean isFit);

    /**
     * 重置通话相关配置，包括音视频的配置和摄像机的配置信息
     *
     * @param sessionConfig 音视频的配置和摄像机的配置信息
     */

    void reset(SessionConfig sessionConfig);

    /**
     * 打开远程语音通话,
     *
     * @param remoteUrl 语音通话地址
     */
    void unmute(String remoteUrl);

    /**
     * 关闭远程语音通话
     *
     * @param remoteUrl 远程播放地址
     */
    void mute(String remoteUrl);

    /**
     * 释放采集器，编码器资源.
     */
//    void release();
    
    /**
     * 动态设置码率
     * @param aBitrate
     */
    void setVideoBitrate(int aBitrate);
    
    /**
     * 
     */
    void setCallBackListener(CallBackListener<Integer> callbackListener);

//    /**
//     * 设置音频编码类型，软编才需调用且只有互联需调用，rtmp只能传输AAC数据，硬编是sdk上层编，固定是aac
//     * @param audioEncoderType {@see #com.lingyang.sdk.encoder.AudioEncoderType.AUDIO_ENCODER_TYPE_OF_AAC}
//     *                         {@see #com.lingyang.sdk.encoder.AudioEncoderType.AUDIO_ENCODER_TYPE_OF_OPUS}
//     */
//    void setAudioEncoderType(int audioEncoderType);


    /**
     * 设置此回调，可获取到预览数据进行自定义处理再发送给编码库
     * @param sendDataType 发送数据类型
     * @param mirror 是否镜像显示
     * @param cameraPreviewCallback 预览数据回调接口
     */
    void setCameraPreviewCallback(int sendDataType, boolean mirror, CameraPreviewCallback cameraPreviewCallback);

    /**
     * 设置播放数据回调处理
     * @param aFrameListener
     * @param frameFormat 数据类型，暂时只支持yuv420格式
     */
    void setVideoFrameCallback(OnPlayerVideoFrameListener aFrameListener, int frameFormat);

    /**
     * 发送p2p消息
     * @param msg  长度256以内的任意字符串
     */
    void sendP2PMessage(String msg);

    /**
     * 获取流媒体信息
     * @param aParamType 流媒体信息类型 {@link IMediaParamProtocol}
     * @return
     */
    String getMediaParam(int aParamType);
    
    //发送连接消息串，在连接之前设置，通过连接的时候直接传给对方，通过云消息接收
    void setConnectMsg(String msg);
    
    /**
     * 关掉预览，包括关掉释放camera，preview，surfaceTexture等
     */
    void closePreview();
    
}

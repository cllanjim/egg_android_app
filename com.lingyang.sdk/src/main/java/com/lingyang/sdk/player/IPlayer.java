package com.lingyang.sdk.player;

import android.widget.MediaController;

import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.facetime.OnPlayerVideoFrameListener;

/**
 * @author 波 播放控制接口
 */
public interface IPlayer extends IMediaParamProtocol, MediaController.MediaPlayerControl {
    int ON_ERROR = 0x0001;
    int ON_PREPARING = 0x1000;
    int ON_CONNECTING_BEGIN = 0x1001;
    int ON_CONNECTING_END = 0x1002;
    int ON_PREPARED = 0x1003;
    int ON_CLOSING = 0x1004;
    int ON_CLOSED = 0x1005;

    int ON_COMPLETION = 0x2000;
    int ON_BEGIN_BUFFERING = 0x2001;
    int ON_END_BUFFERING = 0x2002;
    int ON_BUFFERING_UPDATE = 0x2003;
    int ON_SEEK_SUCCESS = 0x2004;
    int ON_SEEK_ERROR = 0x2005;
    int ON_GET_POSTION = 0x2006;

    int ON_LOCALRECORD_START = 0x3000;
    int ON_LOCALRECORD_SIZECHANGE = 0x3001;
    int ON_LOCALRECORD_STOP = 0x3002;
    int ON_LOCALRECORD_ERROR = 0x3003;

    int E_CONNECTION = -1;

//    String SCHEME_HTTP = "http";
//    String SCHEME_QSTP = "qstp";
//    String SCHEME_RECORD = "lyrecord";
//    String SCHEME_QSUP = "qsup";
    String SCHEME_RTMP = "rtmp";
    String SCHEME_TOPVDN = "topvdn";

    int TYPE_QSUP = 1;
    int TYPE_QSTP = 2;
    int TYPE_RECORD = 3;
    String PARAM_TYPE = "protocolType";
    
    long getPlayerHandler();

    /**
     * 关闭声音
     */
    void mute();

    /**
     * 打开声音
     */
    void unmute();

    /**
     * 设置播放源
     *
     * @param url
     */
    void setDataSource(String url);
    
    //设置连接消息，必须在连接之前设置，通过连接直接发送给对方
    void setConnectMsg(String msg);

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling
     * this method, you will have to initialize it again by setting the
     * data source and calling prepare().
     */
    void reset();

    int getVideoWidth();

    int getVideoHeight();

    /**
     * 暂停
     * 直播模式调用会抛出 @throws UnsupportedOperationException
     */
    void pause() throws UnsupportedOperationException;

    /**
     * 恢复播放
     * 直播模式调用会抛出 @throws UnsupportedOperationException
     */
    void resume() throws UnsupportedOperationException;

    /**
     * 结束播放
     */
    void stop();

    void prepare();
    /**
     * 设置是否为互联中被连接方，被连接方无需调用Connect连接，直接打开播放器，然后发送数据即可
     * @param isListener 是否是互联中被连接方
     */
    void isFaceTimeListener(boolean isListener);
    /**
     * 设置播放相关配置
     *
     * @param playOptions
     */
    void setPlayOptions(PlayOptions playOptions);

    /**
     * 互联中发送p2p消息
     * @param msg 长度在256以内的任意字符串
     */
    void sendP2PMessage(String msg);

    void release();
    
    /**
	 * 开启音频采集，语音对讲
	 */
	 void startTalk();
	
	/**
	 * 关闭音频采集
	 */
	 void stopTalk();

    /**
     * 创建视频截图
     *
     * @param path     保存路径
     * @param name     截图名称，不需要填写格式后缀，保存为jpg格式
     * @param listener 接口回调
     */
    void snapshot(String path, String name, OnSnapshotListener listener);

    void useHardVideoDecode(boolean decode);
    
    /**
     * 开始录像
     */
    void startLocalRecord(String filePath);

    void setLocalRecordListener(OnLocalRecordListener onLocalRecordListener);

    /**
     * 结束录像
     */
    void stopLocalRecord();

    /**
     * 注册在播放和建立播放期间发生错误被调用的回调函数 如果回调函数没有注册，或者回调函数返回错误, 将不返回用户任何错误
     *
     * @param errorListener
     */
    void setOnErrorListener(OnErrorListener errorListener);

    /**
     * 注册播放时缓存的回调函数.
     */
    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    /**
     * 注册媒体文件装载时或即将播放时被调用的回调函数
     *
     * @param preparedListener
     */
    void setOnPreparedListener(OnPreparedListener preparedListener);

    /**
     * 直播链接建立时被调用的回调函数
     *
     * @param connectedListener
     */
    void setOnConnectedListener(OnConnectedListener connectedListener);

    /**
     * 注册媒体文件播放结束后被调用的回调函数
     *
     * @param completionListener
     */
    void setOnCompletionListener(OnCompletionListener completionListener);

    // --------------------以下为回调接口定义-----------------//
    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IPlayer p, int percent);
    }

    interface OnCompletionListener {
        void onCompletion(IPlayer p);
    }

    interface OnErrorListener {
        boolean onError(IPlayer p, LYException e);
    }

    interface OnInfoListener {
        boolean onInfo(IPlayer ip, int what, int extra);
    }

    interface OnPreparedListener {
        void onPrepared(IPlayer p, int time);
    }

    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IPlayer p, int width, int height);
    }

    interface OnSeekCompleteListener {
        void onSeekSuccess(IPlayer p, int time);
        
        void onSeekError(LYException exception);
    }

    /**
     * 播放链接建立监听
     */
    interface OnConnectedListener {
        void onConnected(IPlayer p, int time, int fd);
    }

    interface OnLocalRecordListener {
        /**
         * @param size 录制大小，单位：KB
         * @param time 录制时长，单位：S
         */
        void onRecordSizeChange(IPlayer p, int size, int time);

        void onRecordStart(IPlayer p);

        void onRecordError(IPlayer p, LYException e);

        void onRecordStop(IPlayer p);
    }

    interface OnSnapshotListener {
        /**
         * 截图返回的参数错误
         */
        int ERROR_RETURN_PARAM = -101;
        /**
         * 存储空间不足
         */
        int ERROR_NOT_ENOUGH_SPACE = -102;
        /**
         * 不能解码成jpeg
         */
        int ERROR_NOT_DECODE_TO_JPEG = -103;
        /**
         * 截图文件创建失败
         */
        int ERROR_FAIL_CREATE_FILE = -104;

        /**
         * 截图成功
         *
         * @param saveFullPath 保存的完整路径
         */
        void onSnapshotSuccess(String saveFullPath);

        /**
         * 截图失败
         */
        void onSnapshotFail(int errorCode);
    }

    /**
     * 设置播放数据回调处理,可获取到播放器收据进行自定义处理后再进行播放器播放
     * @param aFrameListener
     */
    void setVideoFrameCallback(OnPlayerVideoFrameListener aFrameListener, int frameFormat);


}

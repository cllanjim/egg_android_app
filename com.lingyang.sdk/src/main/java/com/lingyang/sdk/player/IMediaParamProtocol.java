package com.lingyang.sdk.player;

import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_DNS_PARSE_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_AUDIO_BITRATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_DOWNLOAD_VIDEO_BITRATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_PLAYER_AUDIO_FRAMERATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_PLAYER_BUFFER_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_PLAYER_DELAY_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_PLAYER_VIDEO_FRAMERATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_QSTP_COMMAND_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_QSTP_CONNECT_RELAYIP;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_QSTP_HANDSHAKE_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_QSTP_RECVFRAME_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_QSTP_TCP_CONNECT_TIME;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_SEND_RATIO;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_BITRATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_FRAMERATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_REMOTE_UPLOAD_VIEW_NUMBER;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_UPLOAD_AUDIO_BITRATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_UPLOAD_AUDIO_FRAMERATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_UPLOAD_VIDEO_BITRATE;
import static com.antelope.sdk.ACMediaInfo.AC_MEDIA_INFO_UPLOAD_VIDEO_FRAMERATE;

/**
 * 播放控制接口
 */
public interface IMediaParamProtocol {

    /**
     * 1.当前视频下载速度
     */
    int STREAM_MEDIA_PARAM_VIDEO_DOWNLOADSPEED = AC_MEDIA_INFO_DOWNLOAD_VIDEO_BITRATE;
    /**
     * 2.当前音频下载速度
     */
    int STREAM_MEDIA_PARAM_AUDIO_DOWNLOADSPEED = AC_MEDIA_INFO_DOWNLOAD_AUDIO_BITRATE;
    /**
     * 3.当前视频帧率
     */
    int STREAM_MEDIA_PARAM_VIDEO_RATE = AC_MEDIA_INFO_PLAYER_VIDEO_FRAMERATE;
    /**
     * 4.当前音频帧率
     */
    int STREAM_MEDIA_PARAM_AUDIO_RATE = AC_MEDIA_INFO_PLAYER_AUDIO_FRAMERATE;
    /**
     * 5.平均视频下载速度
     */
    int STREAM_MEDIA_PARAM_VIDEO_AVERAGEDOWNLOADSPEED = AC_MEDIA_INFO_DOWNLOAD_VIDEO_BITRATE;
    /**
     * 6.平均音频下载速度
     */
    int STREAM_MEDIA_PARAM_AUDIO_AVERAGEDOWNLOADSPEED = AC_MEDIA_INFO_DOWNLOAD_AUDIO_BITRATE;
    /**
     * 7.获取QSTP转发ip地址
     */
    int STREAM_MEDIA_PARAM_QSTP = AC_MEDIA_INFO_QSTP_CONNECT_RELAYIP;
    /**
     * 8.上行帧率
     */
    int STREAM_MEDIA_PARAM_DEVICE_UPLOADFRAME = AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_FRAMERATE;
    /**
     * 9.上行速度
     */
    int STREAM_MEDIA_PARAM_DEVICE_UPLOADSPEED = AC_MEDIA_INFO_REMOTE_UPLOAD_VIDEO_BITRATE;
    /**
     * 10. 缓冲时长（毫秒）
     */
    int STREAM_MEDIA_PARAM_BUFFER_TIME = AC_MEDIA_INFO_PLAYER_BUFFER_TIME;
    /**
     * 11.缓冲区帧数
     */
    int STREAM_MEDIA_PARAM_BUFFER_FRAME = 11;
    /**
     * 12.当前帧延时（毫秒）
     */
    int STREAM_MEDIA_PARAM_FRAME_DELAY = AC_MEDIA_INFO_PLAYER_DELAY_TIME;
    /**
     * 13. 缓冲区延时（毫秒）
     */
    int STREAM_MEDIA_PARAM_BUFFER_DELAY = AC_MEDIA_INFO_PLAYER_BUFFER_TIME;

    /**
     * 分辨率宽
     */
    int STREAM_MEDIA_PARAM_RATIO_WIDTH = 98;

    /**
     * 分辨率高
     */
    int STREAM_MEDIA_PARAM_RATIO_HEIGHT = 97;

    /**
     * 发送时间比，判断上传网络状态，返回值越小代表网络越好
     */
    int STREAM_MEDIA_QSTP_SEND_PERCENT = AC_MEDIA_INFO_REMOTE_UPLOAD_SEND_RATIO;

    /**
     * 在线人数
     */
    int STREAM_MEDIA_ONLINE_NUMBER = AC_MEDIA_INFO_REMOTE_UPLOAD_VIEW_NUMBER;

    /**
     * 上行音频帧率
     */
    int STREAM_MEDIA_PARAM_AUDIO_UPLOADFRAME = AC_MEDIA_INFO_UPLOAD_AUDIO_FRAMERATE;
    /**
     * 上行音频速度
     */
    int STREAM_MEDIA_PARAM_AUDIO_UPLOADSPEED = AC_MEDIA_INFO_UPLOAD_AUDIO_BITRATE;
    /**
     * 上行视频帧率
     */
    int STREAM_MEDIA_PARAM_VIDEO_UPLOADFRAME = AC_MEDIA_INFO_UPLOAD_VIDEO_FRAMERATE;
    /**
     * 上行视频速度
     */
    int STREAM_MEDIA_PARAM_VIDEO_UPLOADSPEED = AC_MEDIA_INFO_UPLOAD_VIDEO_BITRATE;

    /**
     * 播放器处理
     */
    int STREAM_MEDIA_PARAM_PLAYER_PROCESS=100;
    /**
     * DNS解析
     */
    int STREAM_MEDIA_PARAM_DNS_RESOLVE=AC_MEDIA_INFO_DNS_PARSE_TIME;
    /**
     * tcp握手
     */
    int STREAM_MEDIA_PARAM_TCP_HANDSHAKE=AC_MEDIA_INFO_QSTP_TCP_CONNECT_TIME;
    /**
     * rtmp握手
     */
    int STREAM_MEDIA_PARAM_RTMP_HANDSHAKE=AC_MEDIA_INFO_QSTP_HANDSHAKE_TIME;
    /**
     * rtmp播放命令
     */
    int STREAM_MEDIA_PARAM_RTMP_PLAYER_COMMAND=AC_MEDIA_INFO_QSTP_COMMAND_TIME;
    /**
     * 第一帧
     */
    int STREAM_MEDIA_PARAM_FIRST_FRAME=AC_MEDIA_INFO_QSTP_RECVFRAME_TIME;
    /**
     * IP
     */
    int STREAM_MEDIA_PARAM_IP=AC_MEDIA_INFO_QSTP_CONNECT_RELAYIP;
    /**
     * 平均解码时间
     */
    int STREAM_MEDIA_PARAM_AVERAGE_DECODE=100;


    String getMediaParam(int aParamType);



}

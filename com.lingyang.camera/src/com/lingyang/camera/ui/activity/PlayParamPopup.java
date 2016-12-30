package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.sdk.player.IMediaParamProtocol;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.widget.LYPlayer;

/**
 * 文件名: PlayParamPopup
 * 描    述:该类负责观看摄像头直播时的参数显示
 * 创建人:廖雷
 * 创建时间: 2015/11
 */
public class PlayParamPopup extends PopupWindow {

    public boolean mIsLandScape;
    LYPlayer mPlayer;
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    View v;
    private String mCameraType, mLocation, mRTMP;
    private TextView mBaseText, mCommInfoText, mMoreInfoText;
    private Context mContext;
    private boolean mIsPublic;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sb1.delete(0, sb1.length());
            sb2.delete(0, sb2.length());
            sb3.delete(0, sb3.length());
            if (mIsLandScape) {
                if (mIsPublic) {
                    initPublicPlayerParamOnLandScape();
                } else {
                    initPrivatePlayerOnLandScape();
                }
            } else {
                if (mIsPublic) {
                    initPublicPlayerParamOnPortrait();
                } else {
                    initPrivatePlayerOnPortrait();
                }
            }
            ((AppBaseActivity) mContext).mHandler.postDelayed(mRunnable, 1000);
        }
    };
    private int mOnLine, mTotalWatchNum, mStartTime;
    private FrameLayout mLineLayout;
    OnCheckedChangeListener mListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mMoreInfoText.setVisibility(View.VISIBLE);
                mLineLayout.setVisibility(View.VISIBLE);
            } else {
                mMoreInfoText.setVisibility(View.GONE);
                mLineLayout.setVisibility(View.GONE);
            }
        }
    };


    public PlayParamPopup(Context context, LYPlayer player, boolean isPublic, String cameraType, String location
            , int onLineNum, int totalWatchNum, int startTime, String RTMP) {
        mContext = context;
        mPlayer = player;
        mCameraType = cameraType;
        mLocation = location;
        mOnLine = onLineNum;
        mTotalWatchNum = totalWatchNum;
        mStartTime = startTime;
        mRTMP = RTMP;
        mIsPublic = isPublic;

        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        mIsLandScape = (((PlayerActivity) mContext).getRequestedOrientation()
                == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        // 设置弹窗的宽度和高度
        if (!mIsLandScape) {
            setWidth(context.getResources().getDisplayMetrics().widthPixels * 7 / 8);
            setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            setWidth(context.getResources().getDisplayMetrics().widthPixels * 4 / 5);
            setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        }

        //this.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.half_transparent)));
        // 设置弹窗的布局界面
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_play_param, null);
        setContentView(v);
        initView();
        ((AppBaseActivity) mContext).mHandler.post(mRunnable);

    }

    private void initView() {
        mBaseText = (TextView) v.findViewById(R.id.tv_player_param_base);
        mCommInfoText = (TextView) v.findViewById(R.id.tv_player_param_comminfo);
        mMoreInfoText = (TextView) v.findViewById(R.id.tv_player_param_moreinfo);
        ToggleButton moreInfoBtn = (ToggleButton) v.findViewById(R.id.tb_record_seldate);
        mLineLayout = (FrameLayout) v.findViewById(R.id.line);
        moreInfoBtn.setOnCheckedChangeListener(mListener);
    }

    /* *
      * 公众摄像机全屏显示时
      */
    public void initPublicPlayerParamOnLandScape() {
//        sb1.append("设备类型：");
//        sb1.append(mCameraType);
        sb1.append("设备位置：");
        sb1.append(mLocation);
        sb1.append("     \t当前在线人数：");
        sb1.append(getOnlineNumber());
        sb1.append("     \t总观看次数：");
        sb1.append(mTotalWatchNum);
        sb1.append("次");
        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
        sb2.append("    上传速率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_DEVICE_UPLOADSPEED));
        sb2.append("KB/S     上传帧率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_DEVICE_UPLOADFRAME));
        sb2.append("FPS\n启动耗时：");
        sb2.append(mStartTime);
        sb2.append("ms");
        sb2.append("     视频延时：");
        sb2.append(getDelayedTime());
        sb2.append("ms    上传网络状态：");
        sb2.append(getSendPercent());
//        sb2.append("\n播放器处理：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_PLAYER_PROCESS)).append("ms");
        sb2.append("    DNS解析：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_DNS_RESOLVE));
        sb2.append("ms     TCP握手：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_TCP_HANDSHAKE)).append("ms");
        sb2.append("\nRTMP握手：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RTMP_HANDSHAKE));
        sb2.append("ms     RTMP播放命令：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RTMP_PLAYER_COMMAND)).append("ms");
        sb2.append("    第一帧：").append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_FIRST_FRAME)).append("ms");

        sb3.append(mRTMP);
        sb3.append("\r\nip：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP));
        sb3.append("     下载速率：");
        sb3.append(getAverageDownloadSpeed());//当前下载速度
        sb3.append("KB/S     缓冲时长：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
//        sb3.append("ms\n平均解码耗时：");
//        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME));
        sb3.append("ms");
        sb3.append("    缓冲区延时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
        sb3.append("ms    下载帧率：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb3.append("FPS");
        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }

    /* *
      * 获取在线人数
      *
      * @return
      */
    private String getOnlineNumber() {
        String onlineNumberStr = mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_ONLINE_NUMBER);
        try {
            int onlineNumber = Integer.parseInt(onlineNumberStr);
            return onlineNumber != 65000 ? onlineNumber + "人" : "未知";
        } catch (Exception e) {
            CLog.e("onlineNumberStr parseInt error");
            return "未知";
        }
    }

    private String getDelayedTime() {
        String time = mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_FRAME_DELAY);
        try {
            int delayTime = Integer.parseInt(time);
            if (delayTime <= 0) {
                return "--- ";
            } else {
                return String.valueOf(delayTime);
            }
        } catch (NumberFormatException e) {
            return "--- ";
        }
    }

    /* *
      * 获取上传网络状态
      *
      * @return
      */
    private String getSendPercent() {
        try {
            int sendPercent = Integer.parseInt(
                    mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_QSTP_SEND_PERCENT));
            if (sendPercent >= 0 && sendPercent <= 10) {
//                return String.format("极好(%s)", sendPercent);
                return "极好";
            } else if (sendPercent >= 11 && sendPercent <= 40) {
                return "好";
//                return String.format("好(%s)", sendPercent);
            } else if (sendPercent >= 41 && sendPercent <= 70) {
//                return String.format("一般(%s)", sendPercent);
                return "一般";
            } else if (sendPercent >= 71 && sendPercent <= 100) {
//                return String.format("差(%s)", sendPercent);
                return "差";
            } else if (sendPercent >= 101 && sendPercent <= 254) {
//                return String.format("极差(%s)", sendPercent);
                return "极差";
            } else {
                return "未知";
//                return String.format("未知(%s)", sendPercent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e("SendPercent parseInt error");
            return "";
        }
    }

    public int getAverageDownloadSpeed() {
        return Integer.parseInt(mPlayer
                .getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_VIDEO_AVERAGEDOWNLOADSPEED))
                + Integer.parseInt(mPlayer
                .getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_AUDIO_AVERAGEDOWNLOADSPEED));
    }

    /* *
      * 公众摄像机非全屏显示时
      */
    public void initPublicPlayerParamOnPortrait() {
//        sb1.append("设备类型：");
//        sb1.append(mCameraType);
        sb1.append("设备位置：");
        sb1.append(mLocation);
        sb1.append("     \t当前在线人数：");
        sb1.append(getOnlineNumber());
        sb1.append("     \t总观看次数：");
        sb1.append(mTotalWatchNum);
        sb1.append("次");
        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
        sb2.append("     上传速率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_DEVICE_UPLOADSPEED));
        sb2.append("KB/S\r\n上传帧率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_DEVICE_UPLOADFRAME));
        sb2.append("FPS");
        sb2.append("    启动耗时：");
        sb2.append(mStartTime);
        sb2.append("ms\n视频延时：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_FRAME_DELAY));
        sb2.append("ms     上传网络状态：");
        sb2.append(getSendPercent());

        sb3.append(mRTMP);
        sb3.append("\r\nip：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP));
        sb3.append("     下载速率：");
//        sb3.append(mPlayer.getAverageDownloadSpeed());//当前下载速度
        sb3.append("KB/S");
        sb3.append("\n缓冲时长：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
        sb3.append("ms     平均解码耗时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME));
        sb3.append("ms\n缓冲区延时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
        sb3.append("ms     下载帧率：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb3.append("FPS");


        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }

    /* *
      * 私有摄像机全屏显示时
      */
    private void initPrivatePlayerOnLandScape() {
//        sb1.append("设备类型：");
//        sb1.append(mCameraType);
        sb1.append("设备位置：");
        sb1.append(mLocation);
        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
        sb2.append("    \t视频帧率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb2.append("FPS");
        sb2.append("    视频速率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_DOWNLOADSPEED));
        sb2.append("KB/S\n启动耗时：");
        sb2.append(mStartTime);
        sb2.append("ms     视频延时：");
        sb2.append(getDelayedTime());
        sb2.append("ms");
        sb2.append("    上传网络状态：");
        sb2.append(getSendPercent());
        sb2.append("\n播放器处理：").append(mPlayer.getMediaParam(18)).append("ms");
        sb2.append("    DNS解析：").append(mPlayer.getMediaParam(19));
        sb2.append("ms     TCP握手：").append(mPlayer.getMediaParam(20)).append("ms");
        sb2.append("\nRTMP握手：").append(mPlayer.getMediaParam(21));
        sb2.append("ms     RTMP播放命令：").append(mPlayer.getMediaParam(22)).append("ms");
        sb2.append("    第一帧：").append(mPlayer.getMediaParam(23)).append("ms");

        sb3.append(mRTMP);
        sb3.append("\n下载速率：");
        sb3.append(getAverageDownloadSpeed());//当前下载速度
        sb3.append("KB/S     缓冲时长： ");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
        sb3.append("ms     平均解码耗时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME));
        sb3.append("ms\n缓冲区延时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
        sb3.append("ms");
        if (mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP) != null
                && !mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP).equals("")) {
            sb3.append("     ip：");
            sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP));
        }
        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }

    /**
     * 私有摄像机非全屏显示时
     */
    private void initPrivatePlayerOnPortrait() {
        sb1.append("设备类型：");
        sb1.append(mCameraType);
        sb1.append("   设备位置：");
        sb1.append(mLocation);

        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));

        sb2.append("     视频帧率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb2.append("FPS\n视频速率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_DOWNLOADSPEED));
        sb2.append("KB/s");
        sb2.append("     启动耗时：");
        sb2.append(mStartTime);
        sb2.append("ms");
        sb2.append("\n视频延时：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_FRAME_DELAY));
        sb2.append("ms");
        sb2.append("     上传网络状态：");
        sb2.append(getSendPercent());

        sb3.append("下载速率：");
//        sb3.append(mPlayer.getAverageDownloadSpeed());//当前下载速度
        sb3.append("KB/s     缓冲时长：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
        sb3.append("ms\n平均解码耗时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME));
        sb3.append("ms     缓冲区延时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
        sb3.append("ms");
        if (mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP) != null
                && !mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP).equals("")) {
            sb3.append("\nip：");
            sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP));
        }
        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }
}
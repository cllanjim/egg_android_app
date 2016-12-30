package com.lingyang.camera.ui.activity;

import android.content.Context;
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
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.sdk.player.IMediaParamProtocol;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.widget.LYPlayer;

/**
 * 文件名: PopupResolution
 * 描    述:该类负责观看手机直播时的参数显示
 * 创建人:廖雷
 * 创建时间: 2015/11
 */
public class PopupMobilePlayParam extends PopupWindow {

    LYPlayer mPlayer;
    Camera mCamera;
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    View v;
    private Context mContext;
    private String mRtmp, mPreparedTime;
    private TextView mBaseText, mCommInfoText, mMoreInfoText;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sb1.delete(0, sb1.length());
            sb2.delete(0, sb2.length());
            sb3.delete(0, sb3.length());
            initMobilePlayerParamOnPortrait();
            ((AppBaseActivity) mContext).mHandler.postDelayed(mRunnable, 1000);
        }
    };
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
    private String mAverageDownloadSpeed;

    public PopupMobilePlayParam(Context context, LYPlayer player, Camera camera,
                                String mPreparedTime, String rtmp) {
        mContext = context;
        mPlayer = player;
        this.mCamera = camera;
        this.mPreparedTime = mPreparedTime;
        mRtmp = rtmp;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(context.getResources().getDisplayMetrics().widthPixels * 9 / 10);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 设置弹窗的布局界面
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_mobile_play_param, null);
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

    public void initMobilePlayerParamOnPortrait() {
        sb1.append("设备型号：");
        sb1.append(mCamera.camera_label);
        sb1.append("\r\n设备位置：");
        sb1.append(mCamera.address);
        sb1.append("\r\n当前在线人数：");
        sb1.append(mCamera.online_nums + 1);
        sb1.append("      总观看次数：");
        sb1.append(mCamera.total_watched_nums + 1);
        sb1.append("次");

        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
        sb2.append("     启动耗时：");
        sb2.append(mPreparedTime);
        sb2.append("ms\r\n视频延时：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_FRAME_DELAY));
        sb2.append("ms");

        sb3.append(mRtmp);
        sb3.append("\r\nip：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_QSTP));
        sb3.append("      下载速率：");
        sb3.append(getAverageDownloadSpeed());//当前下载速度
        sb3.append("KB/s\r\n下载帧率：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb3.append("FPS      音频帧率：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_AUDIO_RATE));
        sb3.append("FPS\r\n缓冲时长：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
        sb3.append("ms      平均解码耗时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME));
        sb3.append("ms\n缓冲区延时：");
        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
        sb3.append("ms");


        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }

    /**
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

    /**
     * 获取上传网络状态
     *
     * @return
     */
    private String getSendPercent() {
        try {
            int sendPercent = Integer.parseInt(
                    mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_QSTP_SEND_PERCENT));
            if (sendPercent >= 0 && sendPercent <= 10) {
                return String.format("极好(%s)", sendPercent);
            } else if (sendPercent >= 11 && sendPercent <= 40) {
                return String.format("好(%s)", sendPercent);
            } else if (sendPercent >= 41 && sendPercent <= 70) {
                return String.format("一般(%s)", sendPercent);
            } else if (sendPercent >= 71 && sendPercent <= 100) {
                return String.format("差(%s)", sendPercent);
            } else if (sendPercent >= 101 && sendPercent <= 254) {
                return String.format("极差(%s)", sendPercent);
            } else {
                return String.format("未知(%s)", sendPercent);
            }
        } catch (Exception e) {
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
}
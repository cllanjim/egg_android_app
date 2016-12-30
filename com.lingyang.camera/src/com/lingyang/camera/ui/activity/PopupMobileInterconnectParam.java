package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.net.TrafficStats;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.FileUtil;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.camera.R;
import com.lingyang.camera.util.CalculateAverageValUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.player.IMediaParamProtocol;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.widget.LYPlayer;

/**
 * 文件名: PopupMobileInterconnectParam
 * 描    述: [该类的简要描述]
 * 创建人: jiajian
 * 创建时间: 2016/3/3
 */
public class PopupMobileInterconnectParam extends PopupWindow {
    private final long mOldTotalTxBytes;
    private final long mOldTotalRxBytes;
    private final CalculateAverageValUtil mCalculateAverageValTxUtil;
    private final CalculateAverageValUtil mCalculateAverageValRxUtil;
    LYPlayer mPlayer;
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    View v;
    private Context mContext;
    private TextView  mCommInfoText, mMoreInfoText;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sb2.delete(0, sb2.length());
            sb3.delete(0, sb3.length());
            initMobilePlayerParamOnPortrait();
            ((AppBaseActivity) mContext).mHandler.postDelayed(mRunnable, 1000);
        }
    };
    private long mCurrentTxBytes;
    private long mCurrentRxBytes;

    public PopupMobileInterconnectParam(Context context, LYPlayer player,
                                long oldTotalTxBytes,long oldTotalRxBytes) {
        mContext = context;
        mPlayer = player;
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
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_mobile_interconnect_param, null);
        setContentView(v);
        mOldTotalTxBytes = oldTotalTxBytes;
        mOldTotalRxBytes =  oldTotalRxBytes;
        mCurrentTxBytes =  oldTotalTxBytes;
        mCurrentRxBytes =  oldTotalRxBytes;
        mCalculateAverageValTxUtil = new CalculateAverageValUtil(30);
        mCalculateAverageValRxUtil = new CalculateAverageValUtil(30);
        initView();
        ((AppBaseActivity) mContext).mHandler.post(mRunnable);
    }

    private void initView() {
        mCommInfoText = (TextView) v.findViewById(R.id.tv_player_param_comminfo);
        mMoreInfoText = (TextView) v.findViewById(R.id.tv_player_param_moreinfo);
    }

    public int getAverageDownloadSpeed() {
        return Integer.parseInt(mPlayer
                .getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_VIDEO_AVERAGEDOWNLOADSPEED))
                + Integer.parseInt(mPlayer
                .getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_AUDIO_AVERAGEDOWNLOADSPEED));
    }

    public void initMobilePlayerParamOnPortrait() {
        sb2.append("分辨率：");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
        sb2.append("*");
        sb2.append(mPlayer.getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
        sb2.append("    下载速率：");
        sb2.append(getAverageDownloadSpeed());//当前下载速度
        sb2.append("KB/s");
        sb2.append("\n下载帧率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_VIDEO_RATE));
        sb2.append("FPS    音频帧率：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_AUDIO_RATE));
        sb2.append("FPS\n");
        sb2.append("平均解码耗时：");
        sb2.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_FRAME)).append("ms");
        sb2.append("    网络：");
        sb2.append(NetworkStatus.getNetworkStatus(mContext));
        sb3.append("上传速率：");
        long nowUidTxBytes = TrafficStats.getUidTxBytes(Utils.getAppUid());
        long nowUidRxBytes = TrafficStats.getUidRxBytes(Utils.getAppUid());
        try {
            mCalculateAverageValTxUtil.putValue(nowUidTxBytes - mCurrentTxBytes);
            mCalculateAverageValRxUtil.putValue(nowUidRxBytes- mCurrentRxBytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sb3.append(FileUtil.parseFileSizeF(mCalculateAverageValTxUtil.getAverageVal())).append("/s");
        mCurrentTxBytes = nowUidTxBytes;
        sb3.append("    下载速率：");
        sb3.append(FileUtil.parseFileSizeF(mCalculateAverageValRxUtil.getAverageVal())).append("/s");
        mCurrentRxBytes = nowUidRxBytes;
        sb3.append("\n上传流量：");
        sb3.append(FileUtil.parseFileSizeF(nowUidTxBytes - mOldTotalTxBytes));
        sb3.append("    下载流量：");
        sb3.append(FileUtil.parseFileSizeF(nowUidRxBytes - mOldTotalRxBytes));
//        sb3.append("    缓冲时长：");
//        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_TIME));
//        sb3.append("ms\n缓冲区延时：");
//        sb3.append(mPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_BUFFER_DELAY));
//        sb3.append("ms");

        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
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

}

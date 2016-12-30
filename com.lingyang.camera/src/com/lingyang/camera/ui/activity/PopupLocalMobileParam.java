package com.lingyang.camera.ui.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lingyang.base.utils.FileUtil;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.util.CalculateAverageValUtil;
import com.lingyang.camera.util.HardCoding;
import com.lingyang.camera.util.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文件名: PopupLocalMobileParam
 * 描    述:手机直播方参数的PopupWindow
 * 创建人: 杜舒
 * 创建时间: 2015/11
 */
public class PopupLocalMobileParam extends PopupWindow {

    private final long mOldTotalTxBytes;
    private final long mOldTotalRxBytes;
    private final CalculateAverageValUtil mCalculateAverageValRxUtil;
    private long mCurrentTxBytes = 0;
    private long mCurrentRxBytes = 0;
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    View v;
    private Context mContext;
    private String mMobileType, mLocation;
    private TextView mBaseText, mCommInfoText, mMoreInfoText;
    private FrameLayout mLineLayout;
    private ActivityManager mActivityManager;
    private int mCpuRate;
    private float mRamRate;
    private int mCodingType;
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
    private final CalculateAverageValUtil mCalculateAverageValTxUtil;


    public PopupLocalMobileParam(Context context, ActivityManager am, String mobileType,
                                 String location, int codingType,long oldTotalTxBytes,long oldTotalRxBytes) {
        mContext = context;
        mActivityManager = am;
        mMobileType = mobileType;
        mLocation = location;
        mCodingType = codingType;
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
        mBaseText = (TextView) v.findViewById(R.id.tv_player_param_base);
        mCommInfoText = (TextView) v.findViewById(R.id.tv_player_param_comminfo);
        mMoreInfoText = (TextView) v.findViewById(R.id.tv_player_param_moreinfo);
        ToggleButton moreInfoBtn = (ToggleButton) v.findViewById(R.id.tb_record_seldate);
        moreInfoBtn.setVisibility(View.GONE);
    }

    public void initMobilePlayerParamOnPortrait() {
        sb1.append("设备类型：");
        sb1.append(mMobileType);
        sb1.append("    \r\n设备位置：");
        sb1.append(mLocation);
        sb2.append("CPU占用率：");
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                mCpuRate = (int) getProcessCpuRate();
            }
        });
        sb2.append(mCpuRate).append("%");
        sb2.append("    内存占用率：");
        getMemoryUsage();
        sb2.append(String.format("%.2f", mRamRate)).append("%");
        sb2.append("\n编码类型：");
        sb2.append(mCodingType == HardCoding.VIDEO_DATA_SOFT ? "软编" : "硬编");
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

        mBaseText.setText(sb1);
        mCommInfoText.setText(sb2);
        mMoreInfoText.setText(sb3);
    }

    /**
     * 获取CPU占用率
     *
     * @return
     */
    public float getProcessCpuRate() {
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try {
            Thread.sleep(360);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();
        return 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);
    }

    /**
     * 获取本应用占用内存
     */
    private void getMemoryUsage() {
        //获得系统里正在运行的所有进程
        int pid = android.os.Process.myPid();
        int[] pids = new int[]{1};
        pids[0] = pid;
        Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(pids);
        float memorySize = memoryInfo[0].dalvikPrivateDirty + 0f;
        float totalMemory = getTotalMemory();
        mRamRate = memorySize / totalMemory * 100;
    }

    /**
     * 获取系统总CPU时间
     *
     * @return
     */
    public long getTotalCpuTime() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
    }

    /**
     * 获取本应用占用的CPU时间
     *
     * @return
     */
    public long getAppCpuTime() {
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
    }

    /**
     * 获取手机总内存
     *
     * @return
     */
    private int getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        int initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            // 获得系统总内存，单位是KB
            initial_memory = Integer.parseInt(arrayOfString[1]);
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return initial_memory;
    }

    public void stopRunnable() {
        ((AppBaseActivity) mContext).mHandler.removeCallbacks(mRunnable);
    }
}
package com.lingyang.camera.ui.activity;

import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.FileUtil;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.camera.R;
import com.lingyang.camera.util.CalculateAverageValUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.IScreenRatio;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.player.widget.OnClosedListener;
import com.lingyang.sdk.player.widget.OnErrorListener;
import com.lingyang.sdk.player.widget.OnPlayingBufferCacheListener;
import com.lingyang.sdk.player.widget.OnPreparedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件名: AutoTestActivity
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/6/21
 */
public class AutoTestActivity extends AppBaseActivity{

    public final static String DATA = "data";
    private final String TAG = "AutoTestActivity";
    private final String LINE_END = "\r\n";
    StringBuilder sb = new StringBuilder();
    List<String> mTimeList = new ArrayList<String>();
    private LYPlayer mLYPlayer;
    private String path = "";
    private SurfaceView videoView;
    private Bundle mBundle;
    private long lastPosition;
    private TextView console;
    private TextView mTvLoadTime;
    private long mLoadEndTime;
    private long mLoadStartTime;
    private int mBufferCount = 0;
    private int mTestCount = 0;
    private int mConnectCount = 0;
    private TextView mTvResolution;
    private TextView mTvBufferTime;
    private long mOldTotalTxBytes;
    private long mOldTotalRxBytes;
    private long mCurrentTxBytes;
    private long mCurrentRxBytes;
    private CalculateAverageValUtil mCalculateAverageValTxUtil;
    private CalculateAverageValUtil mCalculateAverageValRxUtil;
    private View llBufferTime;
    private TextView mBtnStopAutoTest;
    private EditText mEtSetCount;
    private long mUpdateTime = 0;
    private Runnable mSpeedRunnable = new Runnable() {
        @Override
        public void run() {
            sb.delete(0, sb.length());
            sb.append("上传速率：");
            long nowUidTxBytes = TrafficStats.getUidTxBytes(Utils.getAppUid());
            long nowUidRxBytes = TrafficStats.getUidRxBytes(Utils.getAppUid());
            try {
                mCalculateAverageValTxUtil.putValue(nowUidTxBytes - mCurrentTxBytes);
                mCalculateAverageValRxUtil.putValue(nowUidRxBytes - mCurrentRxBytes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sb.append(FileUtil.parseFileSizeF(mCalculateAverageValTxUtil.getAverageVal())).append("/s");
            mCurrentTxBytes = nowUidTxBytes;
            sb.append("    下载速率：");
            sb.append(FileUtil.parseFileSizeF(mCalculateAverageValRxUtil.getAverageVal())).append("/s");
            mCurrentRxBytes = nowUidRxBytes;
            sb.append("\n上传流量：");
            sb.append(FileUtil.parseFileSizeF(nowUidTxBytes - mOldTotalTxBytes));
            sb.append("    下载流量：");
            sb.append(FileUtil.parseFileSizeF(nowUidRxBytes - mOldTotalRxBytes));

            mHandler.postDelayed(mSpeedRunnable, 1000);
        }
    };
    private boolean mIsAutoTest = false;
    private int mSetCount;
    private TextView mBtnStartAutoTest;
    private TextView mTvTestCount;
    private Runnable mTestRunnable = new Runnable() {
        @Override
        public void run() {
            stopPull();
            startPull();
            mHandler.postDelayed(mTestRunnable, 5000);
        }
    };

    private void stopPull() {
        mLYPlayer.stop();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start_auto_test:
                    mLYPlayer.start();
//                    String countStr = mEtSetCount.getText().toString();
//                    if (!TextUtils.isEmpty(countStr)) {
//                        try {
//                            mSetCount = Integer.parseInt(countStr);
//                            if (mSetCount < 1 | mSetCount > 99999) {
//                                showToast("请设置测试次数（1-99999）");
//                                return;
//                            }
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                            mSetCount = 100;
//                        }
//                    } else {
//                        showToast("请设置测试次数（1-99999）");
//                        return;
//                    }
//                    mConnectCount = 0;
//                    mTestCount = 0;
//                    mUpdateTime = 0;
//                    showToast("自动测试已开启");
//                    mHandler.post(mUpdateTimeRunnable);
//                    llBufferTime.setVisibility(View.GONE);
//                    mIsAutoTest = true;
//                    startPull();
                    break;
                case R.id.btn_stop_auto_test:
                    mHandler.removeCallbacksAndMessages(null);
                    mIsAutoTest = false;
                    mConnectCount = 0;
                    mTestCount = 0;
                    mUpdateTime = 0;
                    stopPull();
                    showToast("自动测试已停止");
                    break;
                default:
                    break;
            }
        }
    };
    private TextView mTvConnectCount;
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startPull();
        }
    };
    private TextView mTvTime;
    private Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mTvTime.setText(String.format("%s",
                    timeStampToDateByFormat(-3600 * 8 + mUpdateTime++,
                            "HH:mm:ss")));
            mHandler.postDelayed(mUpdateTimeRunnable, 1000);
        }
    };
    private TextView mTvTxtPath;

    public String timeStampToDateByFormat(Long timestamp, String formatString) {
        // SimpleDateFormat format = new
        // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        return format.format(new Date(timestamp * 1000));
    }

    private void startPull() {
        Log.d(TAG, "startPull");
        if (mIsAutoTest && mTestCount >= mSetCount) {
            Log.d(TAG, "mTestCount " + mTestCount);
            mHandler.removeCallbacksAndMessages(null);
            stopPull();
            showToast("自动测试已停止");
            return;
        }
        mTvTestCount.setText(String.format("%s", ++mTestCount));
        if (mLYPlayer != null) {
            mLoadStartTime = System.currentTimeMillis();
            mLYPlayer.start();
        }
    }



    // 处理activity生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_test);
        mCalculateAverageValTxUtil = new CalculateAverageValUtil(30);
        mCalculateAverageValRxUtil = new CalculateAverageValUtil(30);
        initView();
        setPlayerListener();
    }

    private void setPlayerListener() {
//        mLYPlayer.setDataSource("rtmp://rtmp6.public.topvdn.cn/live/1003136_3356491776_1466594882_04b7959e6290638ca0320e32996b68b1");
//        mLYPlayer.setDataSource("rtmp://rtmp8.public.topvdn.cn/live/3000000178_3355443200_1466597521_c12facf5f8f3cdca78d38c019ecd34de");
        mLYPlayer.setDataSource("rtmp://rtmp6.public.topvdn.cn/live/1003136_3356491776_1471502401_4a778a18a1c75197ef48de4424f5775c");
//        mLYPlayer.setDataSource("rtmp://rtmp8.public.topvdn.cn/live/3000000092_3355443200_1466580859_154483d4287ee62af8b73bf7eb303cf3");
        mLYPlayer.setScreenRatio(IScreenRatio.TYPE_PLAYER_RATIO_PROP_BEST);
        mLYPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(int i) {
                CLog.d("onPrepared---");
                mLoadEndTime = System.currentTimeMillis();
                long loadTime = mLoadEndTime - mLoadStartTime;
                Log.d(TAG, "首次加载耗时 " + loadTime + "ms");
                mTvLoadTime.setText(loadTime + "ms");
                mTimeList.add(loadTime + "");
                String width = mLYPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_RATIO_WIDTH);
                String height = mLYPlayer.getMediaParam(IPlayer.STREAM_MEDIA_PARAM_RATIO_HEIGHT);
                if (mIsAutoTest) {
                    saveData(loadTime + "");
                    stopPull();
                    mHandler.postDelayed(mStartRunnable, 200);
                }
                Log.d(TAG, String.format("%s%s%s", width, "*", height));
                mTvResolution.setText(String.format("%s%s%s", width, "*", height));
                mTvConnectCount.setText("" + (++mConnectCount));

            }
        });
        mLYPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(int i, String s) {
                CLog.d("onError---");
                mTvConnectCount.setText(mConnectCount + "");
                if (mIsAutoTest) {
                    stopPull();
                    if (mLYPlayer==null) {
                        mHandler.postDelayed(mStartRunnable,100);
                    }else{
                        mHandler.postDelayed(mStartRunnable, 200);
                    }
                }
                return false;
            }
        });
        mLYPlayer.setOnPlayingBufferCacheListener(new OnPlayingBufferCacheListener() {
            @Override
            public void onPlayingBufferCache(int i) {

            }

            @Override
            public void onBufferStart() {
                mTvBufferTime.setText("" + ++mBufferCount);
            }

            @Override
            public void onBufferEnd() {

            }
        });
        mLYPlayer.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                CLog.d("onClosed---");
            }
        });
    }

    private void initView() {
        mLYPlayer = (LYPlayer) findViewById(R.id.player_view);
        mTvLoadTime = (TextView) findViewById(R.id.tv_load_time);
        mTvResolution = (TextView) findViewById(R.id.tv_resolution);
        mTvBufferTime = (TextView) findViewById(R.id.tv_buffer_time);
        mTvTestCount = (TextView) findViewById(R.id.tv_test_count);
        mTvConnectCount = (TextView) findViewById(R.id.tv_connect_count);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mBtnStartAutoTest = (TextView) findViewById(R.id.btn_start_auto_test);
        mBtnStopAutoTest = (TextView) findViewById(R.id.btn_stop_auto_test);
        mTvTxtPath = (TextView) findViewById(R.id.tv_txt_path);
        mTvTxtPath.setText("启动耗时记录保存在 "+ com.lingyang.camera.util.FileUtil.getInstance().getLogFile().getPath()+"/debug.txt");
        mEtSetCount = (EditText) findViewById(R.id.et_set_count);
        mEtSetCount.setSelection(mEtSetCount.getText().toString().length());
        llBufferTime = findViewById(R.id.ll_buffer_time);

        mBtnStartAutoTest.setOnClickListener(mOnClickListener);
        mBtnStopAutoTest.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (mLYPlayer.isPlaying()) {
            mLYPlayer.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void saveData(final String data) {
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                File dir = com.lingyang.camera.util.FileUtil.getInstance().getLogFile();
                if (dir == null || !dir.exists()) {
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append(data).append(LINE_END);
                String str = sb.toString();
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.getName().contains("debug")) {
                            try {
                                if (file.length() >= 512 * 1024) {
                                    file.delete();
                                    file.createNewFile();
                                }
                                FileOutputStream fos = new FileOutputStream(file, true);
                                fos.write(str.getBytes("utf-8"));
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }

}

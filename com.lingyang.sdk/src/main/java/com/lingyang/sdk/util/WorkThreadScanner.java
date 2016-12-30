package com.lingyang.sdk.util;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * 描述：工作者线程主要负责设备状态回馈
 */
public class WorkThreadScanner {

    private static final String TAG = "WorkThreadScanner";
    private static final int MSG_RUN_SCAN = 0x0001;
    private static WorkThreadScanner sInstance;
    private final Handler mHandler;
    private HandlerThread sRunner;
    private boolean mScanning = false;

    private WorkThreadScanner() {
        sRunner = new HandlerThread("WorkThread-scanner");
        sRunner.setDaemon(true);
        sRunner.setPriority(Thread.NORM_PRIORITY);
        sRunner.start();
        CLog.v("mStart");
        mHandler = new ScanHandler();
        
//        executeTask(new Runnable() {
//			public void run() {
//				//设置核
////				int result=PlatformAPI.SetAffinityTaskMask(0, 3);
//				CLog.i("set_thread_affinity_mask_result:"+result+", tid="+Thread.currentThread().getId());
//			}
//		});
    }

    public static WorkThreadScanner getInstance() {
        if (sInstance == null) {
            synchronized (TAG) {
                if (sInstance == null) {
                    sInstance = new WorkThreadScanner();
                }
            }
        }
        return sInstance;
    }

    /**
     * 处理异步任务
     *
     * @param runnable
     */
    public void executeTask(Runnable runnable) {
    	CLog.i("workscanner_executeTask");
        mHandler.post(runnable);
    }

    /**
     * 处理异步任务
     *
     * @param runnable
     * @param delayMillis 延迟时间
     */
    public void executeTaskDelay(Runnable runnable, long delayMillis) {
        mHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * 取消异步任务
     *
     * @param runnable
     */
    public void removeTask(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    /*
     * 扫描。基于效率考虑，如果上一次扫描未结束，将不会有任何动作。
     */
    public void start() {
        if (mScanning) {
            CLog.d("prev scan is running, do nothing");
            return;
        }
        mHandler.removeMessages(MSG_RUN_SCAN);
        mHandler.sendEmptyMessage(MSG_RUN_SCAN);
        mScanning = true;
    }

    public void stop() {
        CLog.v("stop begin");
        mHandler.removeCallbacksAndMessages(null);
        sInstance = null;
        mScanning = false;
        sRunner.quit();
    }

    public Looper getWorkThreadLooper() {
        return sRunner.getLooper();
    }

    class ScanHandler extends Handler {

        public ScanHandler() {
            super(sRunner.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RUN_SCAN:
                    break;
            }
        }
    }
}

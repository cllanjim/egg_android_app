package com.lingyang.camera.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.FacadeDownloadManager;
import com.lingyang.base.utils.MD5Util;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.DownloadTaskInfo;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadState;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.DownloadStateIntent;
import com.lingyang.camera.util.ApkUtil;
import com.lingyang.camera.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * 文件名：UpgradeService
 * 描述：版本更新，核对md5，下载新版本
 * 创建人：刘波
 * 时间：2015/9
 */
public class UpgradeService extends Service {

    public static final int FINISHFLAG = 100;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    NetworkChangerReceiver mNetworkChangerReceiver;
    private String mDownloadSavePath;
    private String mDownloadSaveFileFullPath;
    private FacadeDownloadManager mFacadeDownloadManager;
    private String mMD5, saveFileName;
    private String mUrl;
    IDownloadListener mDownloadListener = new IDownloadListener() {

        @Override
        public boolean onDownloadStateChanged(DownloadState state) {
            DownloadStateIntent downloadStateIntent = new DownloadStateIntent(state.getUri(),
                    state.getDownloadLen(), state.getTotalLen(), state.getProgress(),
                    state.getState(), "");
            sendDownloadBroadcast(downloadStateIntent);
            CLog.v("state: " + state.getState() + " - progress:" + state.getProgress()
                    + " - download:" + state.getDownloadLen() + " - Total:" + state.getTotalLen()
                    + " - uri:" + state.getUri() + "-mDownloadSaveFileFullPath:"
                    + mDownloadSaveFileFullPath);
            if (state.getProgress() == 100) {
                checkMD5(mMD5);
            }
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        // 设置通知栏标题
        mBuilder.setContentTitle(getText(R.string.app_name))
                // 通知首次出现在通知栏，带上升动画效果的
                .setTicker(getText(R.string.app_name) + "开始下载。。")
                // 通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setWhen(System.currentTimeMillis())
                // 设置这个标志当用户单击面板就可以让通知将自动取消
                .setAutoCancel(true)
                // true，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,
                // 用户积极参与(如播放音乐)或以某种方式正在等待,
                // 因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setOngoing(true)
                // 向通知添加声音、闪灯和振动效果的最简单、
                // 最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                .setDefaults(Notification.DEFAULT_ALL)
                // 设置通知小ICON
                .setSmallIcon(R.drawable.ic_login)
                .setContentIntent(getDefaultIntent(Notification.FLAG_AUTO_CANCEL));

        mDownloadSavePath = FileUtil.getInstance().getmSDCacheDir().getAbsolutePath();
        CLog.v("mDownloadSavePath:" + mDownloadSavePath);
        mFacadeDownloadManager = new FacadeDownloadManager.Builder().setMaxActiveTaskCount(1)
                .setDownloadPath(mDownloadSavePath)
                .setDownloadTable(DownloadTable.DOWNLOAD_FILE_TABLE).setDownloadNeedWifi(false)
                .setTaskInstanceName("UpgradeService").build(getApplicationContext());
        mFacadeDownloadManager.addDownloadListener(mDownloadListener);
        CLog.v("mFacadeDownloadManager-hashCode" + mFacadeDownloadManager.hashCode());
        mNetworkChangerReceiver = new NetworkChangerReceiver();
    }

    public PendingIntent getDefaultIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mMD5 = intent.getStringExtra(Const.IntentKeyConst.KEY_DOWNLOADSERVICE_MD5);
            mUrl = intent.getStringExtra(Const.IntentKeyConst.KEY_DOWNLOADSERVICE_DOWNLOADURL);
            saveFileName = intent.getStringExtra(Const.IntentKeyConst.KEY_DOWNLOADSERVICE_FILENAME);
            downloadFile(mUrl, saveFileName, mMD5);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Const.Actions.ACTION_NET_CHANGE);
            registerReceiver(mNetworkChangerReceiver, filter);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        CLog.d("onDestroy() executed");
        unregisterReceiver(mNetworkChangerReceiver);
        ThreadPoolManagerQuick.execute(new Runnable() {

            public void run() {
                mFacadeDownloadManager.release();
                CLog.d("release() executed");
            }
        });
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * [执行具体的下载任务]<BR>
     *
     * @param url
     */
    private void downloadFile(final String url, final String fileName, final String md5) {
        mUrl = url;
        mMD5 = md5;
        saveFileName = fileName.replace('.', '_');
        mDownloadSaveFileFullPath = mDownloadSavePath + File.separator + saveFileName;
        CLog.v("downloadFile() -cameraUrl:" + url + " -saveFileName:" + saveFileName
                + "-----------mDownloadSaveFileFullPath----------" + mDownloadSaveFileFullPath);
        ThreadPoolManagerNormal.execute(new Runnable() {

            public void run() {
                mFacadeDownloadManager.deleteTask(mUrl, true);
                File file = new File(mDownloadSaveFileFullPath);
                if (file.exists()) {
                    CLog.d("----------下载的时候文件存在---------" + mDownloadSaveFileFullPath);
                    mFacadeDownloadManager.deleteTask(url, true);
                }
                mNotificationManager.notify(UpgradeService.this.hashCode(), mBuilder.build());
                final int state = mFacadeDownloadManager.downloadFile(url, saveFileName);
                CLog.d("downloadFile() -execute" + " state:" + state);
            }
        });

    }

    private void sendDownloadBroadcast(DownloadStateIntent downloadStateIntent) {
        Intent intent = new Intent();
        intent.putExtra(Const.IntentKeyConst.KEY_DOWNSTATE, downloadStateIntent);
        // action与接收器相同
        intent.setAction(Const.Actions.ACTION_BROADCAST_DOWNLOAD);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        if (downloadStateIntent.progress == 100) {
            mBuilder.setProgress(100, downloadStateIntent.progress, false).setContentText("下载完成！")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(getText(R.string.app_name) + "下载完成！")
                    .setWhen(System.currentTimeMillis());
            mNotificationManager.notify(this.hashCode(), mBuilder.build());
        } else {
            mBuilder.setProgress(100, downloadStateIntent.progress, false)
                    .setContentText("下载进度:" + downloadStateIntent.progress + "%")
                    .setDefaults(Notification.DEFAULT_LIGHTS);
            mNotificationManager.notify(this.hashCode(), mBuilder.build());
        }

    }

    /**
     * [校验下载的升级文件md5] <br/>
     *
     * @param md5
     */
    private void checkMD5(final String md5) {
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
                File file = new File(mDownloadSaveFileFullPath);
                try {
                    String fileMD5 = MD5Util.getFileMD5String(file).toLowerCase();
                    if (fileMD5.toLowerCase().equals(md5)) {
                        ApkUtil.installApk(getApplicationContext(), mDownloadSaveFileFullPath);
                        stopSelf();
                    } else {
                        restartDownFile(mUrl, mDownloadSaveFileFullPath);
                        CLog.v("checkMD5 error fileMD5:" + fileMD5);
                    }
                } catch (IOException e) {
                    restartDownFile(mUrl, mDownloadSaveFileFullPath);
                    CLog.v("checkMD5" + e.getMessage());
                }

            }
        });
    }

    private void restartDownFile(final String url, final String saveFileName) {
        CLog.v("restartDownFile");
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
                mFacadeDownloadManager.deleteTask(url, true);
                downloadFile(url, saveFileName, mMD5);
            }
        });

    }

    class NetworkChangerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mUrl == null)
                return;
            DownloadTaskInfo downloadTaskInfo = mFacadeDownloadManager.query(mUrl);
            if (downloadTaskInfo == null)
                return;
            CLog.v("downloadTaskInfo.staus:" + downloadTaskInfo.status);
            if (intent.getAction().equals(Const.Actions.ACTION_NET_CHANGE)
                    && !NetWorkUtils.isNetworkAvailable(context)) {
                CLog.v("NetworkChangerReceiver -onReceive disconnect");
                mFacadeDownloadManager.pauseTask(mUrl);
            } else if (intent.getAction().equals(Const.Actions.ACTION_NET_CHANGE)
                    && NetWorkUtils.isNetworkAvailable(context)) {
                CLog.v("NetworkChangerReceiver -onReceive connect");
                if (downloadTaskInfo.status == DownloadTaskInfo.DOWNLOAD_STATE_FAIL) {
                    restartDownFile(mUrl, mDownloadSaveFileFullPath);
                    CLog.v("status=DOWNLOAD_STATE_FAIL");
                } else {
                    mFacadeDownloadManager.runTask(mUrl);
                }
            }
        }
    }
}

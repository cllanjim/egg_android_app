package com.lingyang.camera.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.Thread;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.CameraState;
import com.lingyang.camera.entity.MobileInterconnectResponse.Mobile;
import com.lingyang.camera.entity.UnShareCamera;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.cloud.AcceptMessageListener;
import com.lingyang.sdk.cloud.IService;
import com.lingyang.sdk.cloud.LYService;

/**
 * 文件名：BootStartService
 * 描述：应用自启动服务，处理云平台推送的消息
 * 创建人：廖雷
 * 时间：2015/11
 */
public class BootStartService extends Service {

    String MSG_CONNECTION_ACCEPTED = "ConnectionAcceptted";
    String MSG_CONNECTION_CLOSED = "ConnectionClosed";
    String MSG_POPCONFIGRESULT = "PopConfigResult";
    String MSG_POPMSG = "PopMessage";
    String MSG_DEVICE_BIND_CONFIRM = "DeviceBindConfirm";
    private Context mContext;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.v("BootStartService_create");
        mContext = getApplicationContext();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CLog.v("onStartCommand");
        initMessageListener();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.v("onDestroy start");
        LYService.getInstance().stopCloudService();
        CLog.v("onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //  Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initMessageListener() {

        CLog.v("BootStartService_create_listener:" + LYService.getInstance().isOnline());
        if (!LYService.getInstance().isOnline()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initMessageListener();
                }
            }, 100);
        } else {
            CLog.v("BootStartService_create_listener:" + LYService.getInstance().isOnline());
            LYService.getInstance().setCloudMessageListener(new AcceptMessageListener() {
                @Override
                public void accept(IService.CloudMessage message) {
                    CLog.v("BootStartService SrcID: " + message.SrcID);
                    CLog.v("BootStartService Name: " + message.Name);
                    CLog.v("BootStartService Message: " + message.Message);
                    processMessage(message);
                }
            });
        }

    }

    private void processMessage(IService.CloudMessage cloudMessage) {
        Intent intent = new Intent();
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.REFRESH_FROM_MOBILE_INTERCONNECTION);
        if (cloudMessage == null) {
            return;
        } else if (cloudMessage.Name.equals(MSG_POPMSG)) {
            if (!TextUtils.isEmpty(cloudMessage.Message)) {
                Gson gson = new Gson();
                Mobile mobileMessage = null;
                try {
                    mobileMessage = gson.fromJson(cloudMessage.Message, Mobile.class);
                } catch (JsonSyntaxException e) {
                    CLog.e(e.getMessage());
                }
                if (mobileMessage == null||TextUtils.isEmpty(mobileMessage.message)) {
                    processCameraStateMessage(cloudMessage);
                } else {
                    if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_DELETE_CAMERA)
                            ||mobileMessage.message.equals(Const.IntentKeyConst.KEY_SHARE_CAMERA)) {
                        processUnShareCamera(cloudMessage);
                    }else {
                        processMobileMessage(mobileMessage);
                    }
                }

            }
        } else if (cloudMessage.Name.equals(MSG_CONNECTION_ACCEPTED)) {
            CLog.v("cloudMessage.Name " + Const.IntentKeyConst.KEY_CONNECTION_ACCEPTED);
            Mobile mobile = new Mobile();
            intent.setAction(Const.Actions.ACTION_MOBILE_MESSAGE);
            mobile.message = Const.IntentKeyConst.KEY_CONNECTION_ACCEPTED;
            intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE, mobile.toString());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (cloudMessage.Name.equals(MSG_CONNECTION_CLOSED)) {
            intent.setAction(Const.Actions.ACTION_MOBILE_MESSAGE);
            Mobile mobile = new Mobile();
            mobile.message = Const.IntentKeyConst.KEY_CONNECTION_CLOSED;
            intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE, mobile.toString());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        } else if (cloudMessage.Name.equals(MSG_POPCONFIGRESULT)) {

        } else if (cloudMessage.Name.equals(MSG_DEVICE_BIND_CONFIRM)) {
            intent.setAction(Const.Actions.ACTION_DEVICE_BIND_CONFIRM);
            intent.putExtra(Const.IntentKeyConst.KEY_MSG_DEVICE_BIND_CONFIRM, cloudMessage.Message);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

    }

    private void processMobileMessage(Mobile mobileMessage) {
        CLog.v("mobileMessage " + mobileMessage);
        final Intent intent = new Intent();
        intent.setAction(Const.Actions.ACTION_MOBILE_MESSAGE);
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.REFRESH_FROM_MOBILE_INTERCONNECTION);
        //处理手机互联消息
        if ((mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_HANG_UP)
                || mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_REFUSE))
                && (getRunningActivityName().equals(Const.Actions.ACTION_ACTIVITY_MOBILE_INTERCONNECT)
                || getRunningActivityName().equals(Const.Actions.ACTION_ACTIVITY_CALLED))) {
            intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE, mobileMessage.toString());
            CLog.v("BootStartService 挂断 ");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_FAIL)
                || mobileMessage.message.equals(Const.IntentKeyConst.KEY_LINE_IS_BUSY)
                || mobileMessage.message.equals(Const.IntentKeyConst.KEY_NO_ANSWER)) {
            intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE, mobileMessage.toString());
            CLog.v("BootStartService---" + mobileMessage.message);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_VIDEO_CALL)) {
            if (getRunningActivityName().equals(Const.Actions.ACTION_ACTIVITY_MOBILE_INTERCONNECT)
                    || getRunningActivityName().equals(Const.Actions.ACTION_ACTIVITY_CALLED)) {
                CLog.v(getRunningActivityName());
                final Mobile finalMobileMessage = mobileMessage;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), finalMobileMessage.nickname + " "
                                + getString(R.string.invite_video_call), Toast.LENGTH_SHORT).show();
                        finalMobileMessage.message = Const.IntentKeyConst.KEY_SEND_BUSY;
                        intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE,
                                finalMobileMessage.toString());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                });
            } else {
                CLog.v("--------------来电-------------");
                //进入来电页面
                Intent it = new Intent(Const.Actions.ACTION_ACTIVITY_CALLED);
                it.putExtra(Const.IntentKeyConst.KEY_MOBILE_MSG, mobileMessage);
                it.putExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER, mobileMessage.phoneNumber);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityUtil.startActivity(mContext, it);
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processCameraStateMessage(IService.CloudMessage cloudMessage) {
        CLog.d("processCameraStateMessage---"+cloudMessage.Message);
        CameraState cameraState = null;
        try {
            cameraState = new Gson().fromJson(cloudMessage.Message, CameraState.class);
        } catch (JsonSyntaxException e) {
            CLog.e(e.getMessage());
        }
        if (cameraState == null) {
            processUnShareCamera(cloudMessage);
            return;
        }
        CLog.d("---" + cameraState.t);
        CLog.d("---" + cameraState.p.toString());
        Intent intent = new Intent();
        intent.setAction(Const.Actions.ACTION_MSG_REFRESH_STATE);
        intent.putExtra(Const.IntentKeyConst.KEY_MSG_REFRESH_STATE, cameraState.p);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void processUnShareCamera(IService.CloudMessage cloudMessage) {
        CLog.d("processUnShareCamera--- " + cloudMessage.Message);
        UnShareCamera unShareCamera = null;
        try {
            unShareCamera = new Gson().fromJson(cloudMessage.Message, UnShareCamera.class);
        } catch (JsonSyntaxException e) {
            CLog.e(e.getMessage());
        }
        if (unShareCamera!=null) {
            CLog.d("processUnShareCamera---start");
            Intent intent = new Intent();
            intent.setAction(Const.Actions.ACTION_MOBILE_MESSAGE);
            intent.putExtra(Const.IntentKeyConst.KEY_SHARE_OR_UNSHARE_CAMERA, unShareCamera.toString());
            intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE, Const.IntentKeyConst.REFRESH_FROM_SHARE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    /**
     * 获取当前运行的activity名称
     *
     * @return
     */
    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

}

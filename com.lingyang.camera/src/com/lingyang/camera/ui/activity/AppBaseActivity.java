package com.lingyang.camera.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.MobileInterconnectResponse;
import com.lingyang.camera.entity.UnShareCamera;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.MessageSendMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.widget.LYProgressDialog;
import com.lingyang.camera.ui.widget.NoRepeatToast;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

/**
 * 文件名：AppBaseActivity
 * 描述：此类是所有Activity的基类，定义了一些通用的方法
 * 创建人：廖蕾
 * 时间：2015/9
 */
@SuppressLint("HandlerLeak")
public class AppBaseActivity extends FragmentActivity {

    public boolean mHasNetWork = false;
    public Handler mHandler;
    protected LYProgressDialog mProgressDialog;
    protected NetworkChangerReceiver mNetworkChangerReceiver;
    protected MessageSendMgmt mMessageSendMgmt;
    CameraApplication mCameraApplication;
    private MobileMsgReceiver mMobileMsgReceiver;

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NoRepeatToast.show(getApplicationContext(), msg,
                        Toast.LENGTH_SHORT);
            }
        });
    }

    public void showToastLong(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NoRepeatToast
                        .show(getApplicationContext(), msg, Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * @param isClearLoginStatus 是否清除登陆信息
     */
    public void goToLogin(boolean isClearLoginStatus) {
        if (isClearLoginStatus) {
//            MyPreference.getInstance().setValue(this, MyPreference.LOGIN_PHONE, "");
            MyPreference.getInstance().setValue(this,
                    MyPreference.LOGIN_PASSWORD, "");
            MyPreference.getInstance().setValue(this, MyPreference.LOGIN_STATE, false);
        }
        LYService.getInstance().stopCloudService();
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_LOGIN);
        intent.putExtra(Const.IntentKeyConst.KEY_ISFIRSTLOGIN, true);
        ((CameraApplication) getApplication()).AppExit();
        ActivityUtil.startActivity(this, intent);
    }

    public void goToMain() {
        ActivityUtil.startActivity(this, Const.Actions.ACTION_ACTIVITY_MAIN);
        finish();
    }

    public void onClick_Back(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        mMessageSendMgmt = (MessageSendMgmt) MgmtClassFactory.getInstance().getMgmtClass(MessageSendMgmt.class);
       /* if (BuildConfig.DEBUG) {
            CLog.v("BuildConfig.DEBUG:" + BuildConfig.DEBUG);
            HierarchyViewServer.get(this).addWindow(this);
        }*/
        mCameraApplication = (CameraApplication) getApplication();
        mCameraApplication.addActivity(this);
        mProgressDialog = new LYProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(getString(R.string.waiting));
        mNetworkChangerReceiver = new NetworkChangerReceiver();
        mMobileMsgReceiver = new MobileMsgReceiver();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.TaskState.ISRUNING:
                        if (!mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.show();
                        break;
                    case Constants.TaskState.PAUSE:
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        break;
                    case Constants.TaskState.SUCCESS:
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        break;
                    case Constants.TaskState.FAILURE:
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        break;
                    case Constants.TaskState.EXCEPITON:
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        NoRepeatToast.show(getApplicationContext(),
                                getString(R.string.req_exception),
                                Toast.LENGTH_SHORT);
                        break;
                    default:
                        break;
                }
                doMessage(msg);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        if (mMobileMsgReceiver != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMobileMsgReceiver);
            mMobileMsgReceiver = null;
        }
        mMessageSendMgmt = null;
        mHandler.removeCallbacksAndMessages(null);
        mCameraApplication.finishActivity(this);
        CLog.v("onDestroy:" + this.getClass().getName());
        mCameraApplication = null;
        /*if (BuildConfig.DEBUG) {
            HierarchyViewServer.get(this).removeWindow(this);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkChangerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
      /*  if (BuildConfig.DEBUG) {
            HierarchyViewServer.get(this).setFocusedWindow(this);
        }*/
        if (mCameraApplication.isApplicationExit()) {
            finish();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.Actions.ACTION_NET_CHANGE);
        registerReceiver(mNetworkChangerReceiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.Actions.ACTION_MOBILE_MESSAGE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMobileMsgReceiver, intentFilter);

    }

    public void loginCloud(){
        LYService.getInstance().startCloudService(
                LocalUserWrapper.getInstance().getLocalUser().getUserToken(),
                LocalUserWrapper.getInstance().getLocalUser().getInitString(),
                new CallBackListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                    }

                    @Override
                    public void onError(LYException e) {
                        mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                    }
                });
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void doMessage(Message msg) {
    }

    protected void processShareMessage(String shareMessage) {
        UnShareCamera unShareCamera = null;
        try {
            unShareCamera = new Gson().fromJson(shareMessage,
                    UnShareCamera.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (unShareCamera != null) {
            Intent it = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
            it.putExtra(Const.IntentKeyConst.KEY_NICKNAME, unShareCamera.nickname);
            it.putExtra(Const.IntentKeyConst.KEY_CNAME, unShareCamera.cname);
            if (unShareCamera.message.equals(Const.IntentKeyConst.KEY_DELETE_CAMERA)) {
                it.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                        Const.IntentKeyConst.REFRESH_FROM_UNSHARE);
                it.putExtra(Const.IntentKeyConst.KEY_CID, unShareCamera.cid);
                CLog.d("---sendBroadcast processShareMessage ---un share " + unShareCamera.cid);
                LocalBroadcastManager.getInstance(this).sendBroadcast(it);
            } else if (unShareCamera.message.equals(Const.IntentKeyConst.KEY_SHARE_CAMERA)) {
                it.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                        Const.IntentKeyConst.REFRESH_FROM_SHARE);
                LocalBroadcastManager.getInstance(this).sendBroadcast(it);
                CLog.d("---sendBroadcast processShareMessage ---  share ");
            }
            finishSharedPlayer(unShareCamera.cid);
        }
    }

    protected void finishSharedPlayer(String cid) {

    }


    /**
     * 跟踪网络变化
     *
     * @param isConnect
     */
    protected void processNetworkChange(boolean isConnect) {
        CLog.v("processNetworkChange:" + isConnect);
        mHasNetWork = isConnect;
    }

    /**
     * 处理手机消息
     *
     * @param mobileMessage
     */
    protected void processMessage(MobileInterconnectResponse.Mobile mobileMessage) {

    }

    class NetworkChangerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.Actions.ACTION_NET_CHANGE)
                    && !NetWorkUtils.isNetworkAvailable(context)) {
                processNetworkChange(false);
                showToast((String) getText(R.string.app_network_error));
            } else if (intent.getAction().equals(Const.Actions.ACTION_NET_CHANGE)
                    && NetWorkUtils.isNetworkAvailable(context)) {
                processNetworkChange(true);
            }
        }
    }

    class MobileMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int from = intent.getIntExtra(Const.IntentKeyConst.KEY_FROM_WHERE, 0);
            Gson gson = new Gson();
            switch (from) {
                case Const.IntentKeyConst.REFRESH_FROM_MOBILE_INTERCONNECTION:
                    String mobileMsg = intent.getStringExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE);
                    CLog.d("MobileMsgReceiver------mobileMsg---" + mobileMsg);
                    if (!TextUtils.isEmpty(mobileMsg)) {
                        MobileInterconnectResponse.Mobile mobileMessage = null;
                        try {
                            mobileMessage = gson.fromJson(mobileMsg,
                                    MobileInterconnectResponse.Mobile.class);
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        processMessage(mobileMessage);
                    }
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_SHARE:
                    String shareMessage = intent.getStringExtra(Const.IntentKeyConst.KEY_SHARE_OR_UNSHARE_CAMERA);
                    CLog.d("MobileMsgReceiver------unShareCid---" + shareMessage);
                    if (!TextUtils.isEmpty(shareMessage)) {
                        processShareMessage(shareMessage);
                    }
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_CALL_ACTIVITY:
                    intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE,Const.IntentKeyConst.KEY_CLOSE_PLAY_AND_LIVE);
                    String stringExtra = intent.getStringExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE);
                    if (!TextUtils.isEmpty(stringExtra)) {
                        MobileInterconnectResponse.Mobile mobile = new MobileInterconnectResponse.Mobile();
                        mobile.message = stringExtra;
                        processMessage(mobile);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

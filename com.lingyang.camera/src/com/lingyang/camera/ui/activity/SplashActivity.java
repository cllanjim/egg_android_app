package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.SdcardUtil;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.DownloadStateIntent;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.entity.UpgradeInfo.Info;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.LoginWithPhoneMgmt;
import com.lingyang.camera.mgmt.UpgradeMgmt;
import com.lingyang.camera.mgmt.UpgradeMgmt.UpgradeMgmtCallback;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.service.BootStartService;
import com.lingyang.camera.ui.widget.LYProgressDialog;
import com.lingyang.camera.util.ApkUtil;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

import java.io.File;
import java.io.IOException;

/**
 * 文件名：SplashActivity
 * 描述：
 * 此类是整个应用的入口，引导界面，主要是检查版本更新，打开更新版本服务，自动登录
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class SplashActivity extends AppBaseActivity {

    private final int TOKEN_OVERDUE = 1;
    private final int NET_CONNECT_FAIL = 2;
    private final int NET_NOT_CONNECT = 3;
    private final int PLATFORM_LOGIN_FAIL = 4;
    private final int CHECK_VERSION_NET_NOT_CONNECT = 5;
    Runnable mStartCloudServiceRunnable = new Runnable() {
        @Override
        public void run() {
            LYService.getInstance().startCloudService(
                    LocalUserWrapper.getInstance().getLocalUser().getUserToken(),
//                    "[Config]\r\nIsDebug=1\r\nLocalBasePort=8200\r\nIsCaptureDev=1\r\nIsPlayDev=1\r\nUdpSendInterval=2\r\nConnectTimeout=10000\r\nTransferTimeout=10000\r\n[Tracker]\r\nCount=3\r\nIP1=121.42.156.148\r\nPort1=80\r\nIP2=182.254.149.39\r\nPort2=80\r\nIP3=203.195.157.248\r\nPort3=80\r\n[LogServer]\r\nCount=1\r\nIP1=223.202.103.147\r\nPort1=80\r\n",
                    LocalUserWrapper.getInstance().getLocalUser().getInitString(),
                    new CallBackListener<Long>() {
                @Override
                public void onSuccess(Long aLong) {
                    mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cloudLoginSuc();
                        }
                    });
                }

                @Override
                public void onError(LYException e) {
                    mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                    loginResultDialog(PLATFORM_LOGIN_FAIL);
                }
            });
        }
    };
    private Info mUpgradeInfo;
    private LocalUser mLocalUser;
    private LYProgressDialog mProgressDialog;
    private long mStartTime;
    private UpgradeBroadcast mUpgradeBroadcast = new UpgradeBroadcast();
    private LoginWithPhoneMgmt mLoginWithEmailMgmt;
    private String mPhone, mPassword;
    /**
     * 登录回调
     */
    BaseCallBack<LoginToken.UserToken> mLoginCallBack = new BaseCallBack<LoginToken.UserToken>() {
        @Override
        public void error(ResponseError object) {
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast((String) getText(R.string.app_login_error));
            }
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            goToLogin(true);
        }

        @Override
        public void success(LoginToken.UserToken t) {
            CLog.v("success -t:" + t.access_token);
            LocalUser localUser = LocalUserWrapper.getInstance().exsitUserByPhone(mPhone, mPassword);
            if (localUser != null) {
                localUser.setAccessToken(t.access_token);
                localUser.setExpire(t.expire);
                localUser.setLastLoginTime(System.currentTimeMillis());
                localUser.setIP(NetWorkUtils.getHostIp());
                localUser.setHead(t.faceimage);
                localUser.setNickName(t.nickname);
                localUser.setUId(t.uname);
                localUser.setMobile(t.phonenumber);
                localUser.setControl(t.control);
                localUser.setInitString(t.init_string);
                localUser.setPhoneConnectAddr(t.phone_connect_addr);
                localUser.setUserToken(t.user_token);
                localUser.setUserTokenExpire(t.user_token_expire);

              /*  mControl = control;
                mInitString = init_string;
                mPhoneConnectAddr = phone_connect_addr;
                mUserToken = user_token;
                mUserTokenExpire = user_token_expire;*/

                Boolean result = LocalUserWrapper.getInstance().updateUser(localUser);
                CLog.v("mLoginCallBack updateUser result:" + result);
                LocalUserWrapper.getInstance().setLocalUser(localUser);
            } else {
                goToLogin(true);
            }
            loginCloud();
//            goToMain();
        }
    };
    UpgradeMgmtCallback mCheckUpgradeCallback = new UpgradeMgmtCallback() {
        @Override
        public void getUpgradeInfo(final Info info) {
            CLog.v("getUpgradeInfo info:" + info);
            // 测试apk
            if (info != null) {
                mUpgradeInfo = info;
                PackageInfo packageInfo = ApkUtil.getInstallAPKInfo(
                        getApplicationContext(), Const.PACKAGE_NAME);
                if (info.upgrade && packageInfo.versionCode < info.versionCode) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            if (!info.force_upgrade) {
                                confirmUpgrade();
                            } else {
                                confirmForceUpgrade();
                            }
                        }
                    });
                } else if (!info.upgrade
                        && packageInfo.versionCode < info.versionCode) {
                    //存在测试文件
                    isFileExists(info);

                } else {
                    autoLogin();
                }
            } else {
                CLog.v("getUpgradeInfo info == null  autoLogin");
                autoLogin();
            }
        }
    };
   /* ICloudOpenAPI.onlineLoginStatesChangeListener mOnLoginStatusChangeListener = new ICloudOpenAPI.onlineLoginStatesChangeListener() {

        public void onUserOnline(String... args) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    cloudLoginSuc();
                }
            });
        }

        public void onUserOffline() {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            loginResultDialog(PLATFORM_LOGIN_FAIL);
        }
    };*/

    /**
     * 判断是否存在测试文件
     */
    private void isFileExists(final Info info) {
        File file = new File(SdcardUtil.getSDPath()
                + File.separator + "lingyangtest.txt");
        if (file.exists()) {
            runOnUiThread(new Runnable() {

                public void run() {
                    if (!info.force_upgrade) {
                        confirmUpgrade();
                    } else {
                        confirmForceUpgrade();
                    }
                }
            });
        } else {
            autoLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        mStartTime = System.currentTimeMillis();
        mLoginWithEmailMgmt = (LoginWithPhoneMgmt) MgmtClassFactory.getInstance().getMgmtClass(LoginWithPhoneMgmt.class);
        checkLogFileExist();
        checkUpgrade();
//        confirmForceUpgrade();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void checkLogFileExist() {
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                File dir = FileUtil.getInstance().getLogFile();
                if (dir == null || !dir.exists()) {
                    return;
                }
                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        files[i].delete();
                    }
                }
                try {
                    String fileName = "debug.txt";
                    File httpLog = new File(dir, fileName);
                    httpLog.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开启服务
     */
    private void startMobileConnectService() {
        CLog.v("startMobileConnectService start");
        Intent it = new Intent();
        it.setClass(this, BootStartService.class);
//        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(it);
    }

    private void checkUpgrade() {
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            loginResultDialog(CHECK_VERSION_NET_NOT_CONNECT);
        } else {
            UpgradeMgmt upgradeMgmt = (UpgradeMgmt) MgmtClassFactory.getInstance()
                    .getMgmtClass(UpgradeMgmt.class);
            upgradeMgmt.checkUpgrade(getApplicationContext(), mCheckUpgradeCallback);
        }
    }

    private void confirmForceUpgrade() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mUpgradeInfo.readme == null ? "" : mUpgradeInfo.readme)
                .setPositiveButton(getString(R.string.start_upgrade), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mUpgradeBroadcast,
                                new IntentFilter(Const.Actions.ACTION_BROADCAST_DOWNLOAD));
                        startUpgradeService();
                        mProgressDialog = new LYProgressDialog(SplashActivity.this);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.setMessage(getString(R.string.is_upgrading));
//                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setColor(getResources().getColor(R.color.half_transparent_white_light));
                        mProgressDialog.show();
                        mProgressDialog.setDownloadProgress(0);
                        mProgressDialog.setText("开始下载");
                    }
                })
                .setNegativeButton(getString(R.string.exit), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        finish();
                    }
                }).setTitle(R.string.camera_confirm_forceupgrade).create()
                .show();

    }

    private void startUpgradeService() {
        Intent mUpgradeServiceIntent = new Intent(
                Const.Actions.ACTION_SERVICE_UPGRADE);
        mUpgradeServiceIntent.putExtra(
                Const.IntentKeyConst.KEY_DOWNLOADSERVICE_MD5,
                mUpgradeInfo.md5);
        mUpgradeServiceIntent
                .putExtra(Const.IntentKeyConst.KEY_DOWNLOADSERVICE_DOWNLOADURL,
                        mUpgradeInfo.url);
        mUpgradeServiceIntent
                .putExtra(Const.IntentKeyConst.KEY_DOWNLOADSERVICE_FILENAME,
                        mUpgradeInfo.name);
        mUpgradeServiceIntent.setPackage(getPackageName());//这里你需要设置你应用的包名
        startService(mUpgradeServiceIntent);
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void confirmUpgrade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mUpgradeInfo.readme)
                .setPositiveButton(getString(R.string.upgrade_in_background), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUpgradeService();
                        autoLogin();
                    }
                }).setNegativeButton(getString(R.string.next_time), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                CLog.v("confirmUpgrade autoLogin");

            }
        }).setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                autoLogin();
            }
        }).setTitle(R.string.camera_confirm_upgrade).create().show();

    }

    public void autoLogin() {
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
                // 获取登录状态和手机号
                boolean mIsLogin = MyPreference.getInstance().getBoolean(
                        SplashActivity.this, MyPreference.LOGIN_STATE);
                mPhone = MyPreference.getInstance().getString(
                        SplashActivity.this, MyPreference.LOGIN_PHONE, "");
                mPassword = MyPreference.getInstance().getString(
                        SplashActivity.this, MyPreference.LOGIN_PASSWORD, "");
                CLog.v("autologin---" + mIsLogin + mPassword + mPhone);
                if (mIsLogin && !mPhone.equals("")) {
                    // 获取用户本地信息
                    mLocalUser = LocalUserWrapper.getInstance()
                            .exsitUserByPhone(mPhone, mPassword);
                    hasLocalUser(mLocalUser);
                } else {
                    goToLogin(true);
                }
            }
        });
    }

    public void hasLocalUser(LocalUser localUser) {
        CLog.v("autologin-haslocaluser" + localUser);
        if (localUser != null) {
            mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            mLoginWithEmailMgmt.loginWithPhone(getApplicationContext(), mPhone, mPassword, mLoginCallBack);
        } else {
            goToLogin(true);
        }
    }

    public void loginCloud() {
        // 登录云平台
        CLog.v("Const.TOPVDN_CLOUD_APPID:"
                + Const.TOPVDN_CLOUD_APPID
                + "LocalUserWrapper.getInstance().getLocalUser().getUid()："
                + LocalUserWrapper.getInstance()
                .getLocalUser().getUid());
        if (!NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
            loginResultDialog(NET_NOT_CONNECT);
        } else if (!LYService.getInstance().isOnline()) {
            ThreadPoolManagerQuick.execute(mStartCloudServiceRunnable);
        } else {
            cloudLoginSuc();
        }
    }

    public void cloudLoginSuc() {
        CLog.v("cloudLoginSuc");
        startMobileConnectService();
        if ((System.currentTimeMillis() - mStartTime) > 1000) {
            goToMain();
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToMain();
                }
            }, 1000);
        }
    }

    /**
     * 自动登录返回结果处理
     *
     * @param type 登录失败原因类型
     */
    public void loginResultDialog(final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                String positiveText = getString(R.string.try_again);
                switch (type) {
                    case TOKEN_OVERDUE:
                        msg = getString(R.string.token_overdue_login_again);
                        positiveText = getString(R.string.login_again);
                        break;
                    case NET_CONNECT_FAIL:
                        msg = getString(R.string.check_net_is_connect);
                        break;
                    case CHECK_VERSION_NET_NOT_CONNECT:
                    case NET_NOT_CONNECT:
                        msg = getString(R.string.connect_to_net);
                        break;
                    case PLATFORM_LOGIN_FAIL:
                        msg = getString(R.string.platform_login_fail);
                        break;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (!isDestroyed()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                        builder.setMessage(msg)
                                .setPositiveButton(positiveText, new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        if (type == CHECK_VERSION_NET_NOT_CONNECT) {
                                            checkUpgrade();
                                        } else if (type == TOKEN_OVERDUE) {
                                            goToLogin(false);
                                        } else {
                                            autoLogin();
                                        }
                                    }
                                }).setNegativeButton(getString(R.string.exit), new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ((CameraApplication) getApplication())
                                        .AppExit();
                            }
                        }).create().show();
                    }
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public class UpgradeBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadStateIntent downloadStateIntent = (DownloadStateIntent) intent.getSerializableExtra(Const.IntentKeyConst.KEY_DOWNSTATE);
            CLog.v("onReceive: mDownloadStateIntent" + downloadStateIntent.progress);
            mProgressDialog.setText("已下载:" + downloadStateIntent.progress +"%");
            if (downloadStateIntent != null) {
                mProgressDialog.setDownloadProgress(downloadStateIntent.progress);
                if (downloadStateIntent.progress == 100) {
                    mProgressDialog.setText("下载完毕");
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.dismiss();
                    SplashActivity.this.finish();
                }
            }
        }
    }

}

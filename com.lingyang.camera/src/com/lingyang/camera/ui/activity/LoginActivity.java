package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lingyang.base.utils.MD5Util;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.LoginToken.UserToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.LoginWithPhoneMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.service.BootStartService;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.util.CLog;

/**
 * 文件名：LoginActivity
 * 描述：此类是用户登录类
 * 创建人：廖蕾
 * 时间：2015/9
 */
public class LoginActivity extends AppBaseActivity {

    Button mLoginButton, mRegistButton;
    TextView mResetPwdTextView;
    EditText mPhoneEditText, mPasswordEditText;
    LoginWithPhoneMgmt mLoginWithEmailMgmt;
    String mPhone, mPassword;
    InputMethodManager mInputMethodManager;
    /**
     * 登录云平台
     */
    Runnable mStartCloudServiceRunnable = new Runnable() {
        @Override
        public void run() {
            CLog.v("autologin-" + mPassword);
            MyPreference.getInstance().setValue(LoginActivity.this,
                    MyPreference.LOGIN_PHONE, mPhone);
            MyPreference.getInstance().setValue(LoginActivity.this,
                    MyPreference.LOGIN_PASSWORD, mPassword);
            CLog.v("autologin2-" + mPassword);
            String uname = LocalUserWrapper.getInstance().getLocalUser().getUid();
            CLog.v("uname "+uname);
            uname= uname.substring(4);
            CLog.v("uname "+uname);
            LYService.getInstance().startCloudService(
                    LocalUserWrapper.getInstance().getLocalUser().getUserToken(),
                    LocalUserWrapper.getInstance().getLocalUser().getInitString(),
                    new CallBackListener<Long>() {
                @Override
                public void onSuccess(Long aLong) {
                    mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyPreference.getInstance().setValue(LoginActivity.this,
                                    MyPreference.LOGIN_STATE, true);
                            CLog.v("MyPreference.getInstance() LOGIN_STATE" +
                                    MyPreference.getInstance().getBoolean(LoginActivity.this,
                                            MyPreference.LOGIN_STATE));
                            startMobileConnectService();
                            goToMain();
                        }
                    });
                }

                @Override
                public void onError(LYException e) {
                    mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                    showToast((String) getText(R.string.cloud_login_fail));
                }
            });
        }
    };

    /**
     * 开启服务
     */
    private void startMobileConnectService() {
        Intent it = new Intent();
        it.setClass(this, BootStartService.class);
        startService(it);
    }

    /**
     * 登录
     */
    BaseCallBack<UserToken> mLoginCallBack = new BaseCallBack<UserToken>() {
        @Override
        public void error(ResponseError object) {
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast((String) getText(R.string.app_login_error));
            }
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(UserToken t) {
            CLog.v("autologin--" + mPassword);
            LocalUser localUser = LocalUserWrapper.getInstance().exsitUserByPhone(mPhone, mPassword);
            if (localUser != null) {
                localUser.setAccessToken(t.access_token);
                localUser.setExpire(t.expire);
                localUser.setLastLoginTime(System.currentTimeMillis());
                localUser.setIP(NetWorkUtils.getHostIp());
                localUser.setHead(t.faceimage);
                localUser.setNickName(t.nickname);
                localUser.setPassword(mPassword);
                localUser.setUId(t.uname);
                localUser.setMobile(t.phonenumber);

                localUser.setControl(t.control);
                localUser.setInitString(t.init_string);
                localUser.setPhoneConnectAddr(t.phone_connect_addr);
                localUser.setUserToken(t.user_token);
                localUser.setUserTokenExpire(t.user_token_expire);
                Boolean result = LocalUserWrapper.getInstance().updateUser(localUser);
                CLog.v("mLoginCallBack updateUser result:" + result);
            } else {
                localUser = new LocalUser(t.nickname, t.faceimage,
                        mPassword, NetWorkUtils.getHostIp(), t.phonenumber, t.expire,
                        t.uname, t.access_token,t.control,t.init_string,t.phone_connect_addr,t.user_token,t.user_token_expire);
                Boolean result = LocalUserWrapper.getInstance().addUser(
                        localUser);
                CLog.v("mLoginCallBack addUser result:" + result);
            }

            LocalUserWrapper.getInstance().setLocalUser(localUser);
            ThreadPoolManagerQuick.execute(mStartCloudServiceRunnable);
//            goToMain();
        }
    };


    /**
     * 登录
     */
    OnClickListener mLoginOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPassword = mPasswordEditText.getText().toString().trim();
            mPhone = mPhoneEditText.getText().toString().trim();
            if (TextUtils.isEmpty(mPhone)) {
                showToast((String) getText(R.string.app_login_input_phone));
                return;
            }
            if (TextUtils.isEmpty(mPassword)) {
                showToast((String) getText(R.string.app_login_input_pwd));
                return;
            }
            if (!Utils.isMobileNO(mPhone)) {
                showToast((String) getText(R.string.login_error_phone));
                return;
            }
            if (!Utils.isPassword(mPassword)) {
                showToast((String) getText(R.string.app_login_pwd_notify));
                return;
            }
            mPassword = MD5Util.getMD5String(mPassword);
            mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            mLoginWithEmailMgmt.loginWithPhone(LoginActivity.this, mPhone, mPassword, mLoginCallBack);
        }
    };

    /**
     * 注册
     */
    OnClickListener mRegistListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_REGIST_AND_RESET_PWD);
            intent.putExtra(Const.IntentKeyConst.KEY_COME_FEOM_WHERE, RegistAndResetPasswordActivity.COME_FROM_REGIST);
            ActivityUtil.startActivity(LoginActivity.this, intent);
        }
    };

    /**
     * 找回密码
     */
    OnClickListener mResetPwdListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_REGIST_AND_RESET_PWD);
            intent.putExtra(Const.IntentKeyConst.KEY_COME_FEOM_WHERE, RegistAndResetPasswordActivity.COME_FROM_RESET);
            ActivityUtil.startActivity(LoginActivity.this, intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        mLoginWithEmailMgmt = (LoginWithPhoneMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(LoginWithPhoneMgmt.class);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initView() {
        mResetPwdTextView = (TextView) findViewById(R.id.tv_reset_pwd);
        mPhoneEditText = (EditText) findViewById(R.id.et_phone);
        mPhone = MyPreference.getInstance().getString(
                LoginActivity.this, MyPreference.LOGIN_PHONE, "");
        if (mPhone!=null&&!mPhone.equals("")) {
            mPhoneEditText.setText(mPhone);
        }
        mPasswordEditText = (EditText) findViewById(R.id.et_pwd);
        mLoginButton = (Button) findViewById(R.id.btn_login);
        mRegistButton = (Button) findViewById(R.id.btn_regist);
        mLoginButton.setOnClickListener(mLoginOnClickListener);
        mRegistButton.setOnClickListener(mRegistListener);
        mResetPwdTextView.setOnClickListener(mResetPwdListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            ((CameraApplication) getApplication()).AppExit();
        }
        return super.onKeyDown(keyCode, event);
    }
}

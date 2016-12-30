package com.lingyang.camera.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.MD5Util;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.LoginGetIdentifyMgmt;
import com.lingyang.camera.mgmt.RegisterMgmt;
import com.lingyang.camera.mgmt.ResetPwdMgmt;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.util.CLog;

public class RegistAndResetPasswordActivity extends AppBaseActivity {
    public static final int COME_FROM_REGIST = 1;
    public static final int COME_FROM_RESET = 2;
    /**
     * 重置密码接口回调
     */
    BaseCallBack<BaseResponse> mResetCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void error(ResponseError object) {
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast((String) getText(R.string.app_reset_error));
            }
        }

        @Override
        public void success(BaseResponse baseResponse) {
            showToast((String) getText(R.string.app_reset_suc));
            ActivityUtil.startActivity(RegistAndResetPasswordActivity.this, Const.Actions.ACTION_ACTIVITY_LOGIN);
            finish();
        }
    };
    /**
     * 注册回调
     */
    BaseCallBack<LoginToken.UserToken> mRegistCallBack = new BaseCallBack<LoginToken.UserToken>() {
        @Override
        public void error(ResponseError object) {
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast((String) getText(R.string.app_reg_error));
            }
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(LoginToken.UserToken t) {
            showToast((String) getText(R.string.app_reg_suc));
            LocalUser localUser = new LocalUser("", "",
                    mPassword, NetWorkUtils.getHostIp(), t.phonenumber, t.expire,
                    t.uname, t.access_token,t.control,t.init_string,t.phone_connect_addr,t.user_token,t.user_token_expire);
            Boolean result = LocalUserWrapper.getInstance().addUser(
                    localUser);
            CLog.v("mLoginCallBack addUser result:" + result);
            LocalUserWrapper.getInstance().setLocalUser(localUser);
            ActivityUtil.startActivity(RegistAndResetPasswordActivity.this, Const.Actions.ACTION_ACTIVITY_REGISTER);
            finish();
        }
    };
    View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private LoginGetIdentifyMgmt mLoginGetIdentifyMgmt;
    private ResetPwdMgmt mResetPwdMgmt;
    private RegisterMgmt mRegisterMgmt;
    private EditText mPhoneEditText, mIdentityEditText, mPwdEditText;
    private Button mGetCodeBtn;
    /**
     * 获取验证码回调
     */
    BaseCallBack<BaseResponse> mIdentifyCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void error(ResponseError object) {
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast((String) getText(R.string.login_identity_send_fail));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGetCodeBtn.setText(getResources().getString(
                            R.string.app_login_get_identify));
                    mGetCodeBtn.setEnabled(true);
                }
            });
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(BaseResponse t) {
            showToast((String) getText(R.string.login_identity_was_send));
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
        }
    };
    /**
     * 发送短信倒计时
     */
    CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            mGetCodeBtn.setText(Utils.makeTextWithTag(millisUntilFinished
                            / 1000 + "s", Color.RED,
                    getResources().getString(R.string.app_login_Reacquire)));
        }

        @Override
        public void onFinish() {
            mGetCodeBtn.setText(getResources().getString(
                    R.string.app_login_get_identify));
            mGetCodeBtn.setEnabled(true);
            mGetCodeBtn.setClickable(true);
        }
    };
    private int where;
    private String mPhone, mPassword, mIdentify;
    /**
     * 注册/重置密码确认
     */
    View.OnClickListener mSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPassword = mPwdEditText.getText().toString().trim();
            mPhone = mPhoneEditText.getText().toString().trim();
            mIdentify = mIdentityEditText.getText().toString().trim();
            if (TextUtils.isEmpty(mPhone)) {
                showToast((String) getText(R.string.app_login_input_phone));
                return;
            }
            if (TextUtils.isEmpty(mPassword)) {
                showToast((String) getText(R.string.app_login_input_pwd));
                return;
            }
            if (TextUtils.isEmpty(mIdentify)) {
                showToast((String) getText(R.string.app_login_input_identify));
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
            if (where == COME_FROM_REGIST) {
                mRegisterMgmt.registe(RegistAndResetPasswordActivity.this, mPhone, mPassword,
                        mIdentify, mRegistCallBack);
            } else if (where == COME_FROM_RESET) {
                mResetPwdMgmt.ResetPwd(RegistAndResetPasswordActivity.this, mPhone, mPassword,
                        mIdentify, mResetCallBack);
            }
        }
    };
    /**
     * 获取验证码
     */
    View.OnClickListener mIdentifyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPhone = mPhoneEditText.getText().toString().trim();
            if (TextUtils.isEmpty(mPhone)) {
                showToast((String) getText(R.string.app_login_input_phone));
                return;
            }
            if (!Utils.isMobileNO(mPhone)) {
                showToast((String) getText(R.string.login_error_phone));
                return;
            }
            mCountDownTimer.start();
            mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            mLoginGetIdentifyMgmt.LoginGetIdentify(getApplicationContext(), mPhone,
                    mIdentifyCallBack);
            mGetCodeBtn.setEnabled(false);
            mGetCodeBtn.setClickable(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_and_reset_password);
        mLoginGetIdentifyMgmt = (LoginGetIdentifyMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(LoginGetIdentifyMgmt.class);
        mResetPwdMgmt = (ResetPwdMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(ResetPwdMgmt.class);
        mRegisterMgmt = (RegisterMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(RegisterMgmt.class);

        initView();
    }

    private void initView() {
        where = getIntent().getIntExtra(Const.IntentKeyConst.KEY_COME_FEOM_WHERE, 0);
        mPhoneEditText = (EditText) findViewById(R.id.et_phone);
        mIdentityEditText = (EditText) findViewById(R.id.et_identify);
        mPwdEditText = (EditText) findViewById(R.id.et_pwd);
        mGetCodeBtn = (Button) findViewById(R.id.btn_get_identity);
        Button saveBtn = (Button) findViewById(R.id.btn_save);
        ImageView backImg = (ImageView) findViewById(R.id.iv_heder_back);
        TextView titleTextView = (TextView) findViewById(R.id.tv_header_title);
        backImg.setVisibility(View.VISIBLE);

        if (where == COME_FROM_REGIST) {
            titleTextView.setText(getString(R.string.register_title));
            saveBtn.setText(getString(R.string.register_title));
            mPwdEditText.setHint(getString(R.string.app_regist_pwd));
        } else if (where == COME_FROM_RESET) {
            titleTextView.setText(getString(R.string.app_reset_regist_pwd));
            saveBtn.setText(getString(R.string.reset_confirm));
            mPwdEditText.setHint(getString(R.string.app_new_pwd));
        }

        mGetCodeBtn.setOnClickListener(mIdentifyListener);
        saveBtn.setOnClickListener(mSaveListener);
        backImg.setOnClickListener(mBackListener);

    }


}

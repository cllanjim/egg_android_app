package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.LoginToken.UserToken;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件名: LoginWithPhoneMgmt
 * 描    述: 手机号登录
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class LoginWithPhoneMgmt extends BaseMgmt {

    public BaseCallBack<UserToken> mAppInfoMgmtCallback;
    private String mPhone,mPwd;

    public void loginWithPhone(Context context, String phone,String pwd,
                               BaseCallBack<UserToken> callback) {
        mContext = context;
        mPhone = phone;
        this.mPwd = pwd;
        mAppInfoMgmtCallback = callback;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            LoginToken info = (LoginToken) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mAppInfoMgmtCallback != null) {
                    mAppInfoMgmtCallback.success(info.getData());
                }
            } else {
                if (mAppInfoMgmtCallback != null) {
                    mAppInfoMgmtCallback.error(info.error);
                }
            }
        } else {
            if (mAppInfoMgmtCallback != null) {
                mAppInfoMgmtCallback.error(null);
            }
        }
    }

    protected String getRequestUrl() {
        return String.format("%s/users/phone_login", APP_REQUEST_URL);
    }

    protected void request() {
        mPostParams = new HashMap<String, String>();
        mPostParams.put("phonenumber", mPhone);
        mPostParams.put("phone_pwd", mPwd);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(LoginToken.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
        CLog.v("loginWithPhone request");
    }

}

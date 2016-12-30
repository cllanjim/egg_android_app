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
 * 文件名: RegisterMgmt
 * 描    述: 用户注册
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class RegisterMgmt extends BaseMgmt {
    private String mPhone, mPwd, mIdentify;
    private BaseCallBack<UserToken> callBack;
    private Runnable mRequestRunnable = new Runnable() {

        @Override
        public void run() {
            CLog.v("regist request0");
            request();
        }
    };

    public void registe(Context context, String phone, String pwd, String identify, BaseCallBack<UserToken> callBack) {
        mContext = context;
        mPhone = phone;
        mPwd = pwd;
        mIdentify = identify;
        this.callBack = callBack;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        CLog.v("register result---" + result);
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            LoginToken info = (LoginToken) result;
            if (info != null
                    && info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (callBack != null) {
                    callBack.success(info.getData());
                }
            } else {
                if (callBack != null) {
                    callBack.error(info.getError());
                }
            }
        } else {
            if (callBack != null) {
                callBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/users/phone/register", APP_REQUEST_URL);
    }


    @Override
    protected void request() {
        Map<String, String> postParam = new HashMap<String, String>();
        postParam.put("phone_pwd", mPwd);
        postParam.put("phonenumber", mPhone);
        postParam.put("message", mIdentify);
        Request request = new Request(getRequestUrl());
        request.setHttpHead(mHeadParams);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(LoginToken.class));
        HttpConnectManager.getInstance(mContext).doPost(request, postParam);
    }


}

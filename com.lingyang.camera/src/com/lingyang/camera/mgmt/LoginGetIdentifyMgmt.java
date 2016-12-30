package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件名: LoginGetIdentifyMgmt
 * 描    述: 登录认证
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class LoginGetIdentifyMgmt extends BaseMgmt {

    public BaseCallBack<BaseResponse> mAppInfoMgmtCallback;
    private String mPhone;
    private Runnable mRequestRunnable = new Runnable() {

        @Override
        public void run() {
            CLog.v("loginGetIdentify request0");
            request();
        }
    };

    public void LoginGetIdentify(Context context, String phone,
                                 BaseCallBack<BaseResponse> callback) {
        mContext = context;
        mPhone = phone;
        mAppInfoMgmtCallback = callback;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BaseResponse info = (BaseResponse) result;
            if (info != null && info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mAppInfoMgmtCallback != null) {
                    mAppInfoMgmtCallback.success(null);
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
            return;
        }
    }

    protected String getRequestUrl() {
        return String.format("%s/users/send_message", APP_REQUEST_URL);
    }

    protected void request() {
        mPostParams = new HashMap<String, String>();
        mPostParams.put("phonenumber", mPhone);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
        CLog.v("loginGetIdentify request");
    }

}

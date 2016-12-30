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
 * 文件名: ResetPwdMgmt
 * 描    述: 重置密码
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class ResetPwdMgmt extends BaseMgmt {

    public BaseCallBack<BaseResponse> mBaseCallBack;
    private String mPhone,mPwd,mIdentify;

    public void ResetPwd(Context context, String phone,String pwd,String identify, BaseCallBack<BaseResponse> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mPhone=phone;
        mPwd=pwd;
        mIdentify=identify;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BaseResponse info = (BaseResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (mBaseCallBack != null) {
                    mBaseCallBack.success(null);
                }
            } else {
                mBaseCallBack.error(info.getError());
            }
        } else {
            if (mBaseCallBack != null) {
                mBaseCallBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/users/set_passwd", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        mPostParams = new HashMap<String, String>();
        mPostParams.put("phonenumber", mPhone);
        mPostParams.put("message", mIdentify);
        mPostParams.put("phone_pwd", mPwd);
        request.setUriParam(mUrlParams);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request,mPostParams);
    }

}

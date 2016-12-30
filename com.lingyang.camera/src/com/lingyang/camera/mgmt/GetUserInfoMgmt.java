package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.UserInfo;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: GetUserInfoMgmt
 * 描    述: 获取用户信息
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class GetUserInfoMgmt extends BaseMgmt {

    public BaseCallBack<UserInfo> mUserInfoMgmtCallback;

    public void updateUserInfo(Context context, BaseCallBack<UserInfo> callback) {
        mContext = context;
        mUserInfoMgmtCallback = callback;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            UserInfo info = (UserInfo) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mUserInfoMgmtCallback != null) {
                    mUserInfoMgmtCallback.success(info);
                }
            } else {
                if (mUserInfoMgmtCallback != null) {
                    mUserInfoMgmtCallback.error(info.error);
                }
            }
        } else {
            if (mUserInfoMgmtCallback != null) {
                mUserInfoMgmtCallback.error(null);
            }
        }
    }

    protected String getRequestUrl() {
        //%s/users/get_user_infos?uid=%s&ip=%s&expire=%s&access_token=%s
        return String.format("%s/users/get_user_infos", APP_REQUEST_URL);
    }

    protected void request() {
        addUserToken();
        addValidUrlParams();
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(UserInfo.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}

package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CallContactResponse;
import com.lingyang.camera.entity.OnlineUsersResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.List;
import java.util.Map;

/**
 * 文件名: GetUserInfoMgmt
 * 描    述: 获取用户信息
 * 创建人:杜舒
 * 创建时间: 2016/3
 */
public class GetOnlineUsersMgmt extends BaseMgmt {

    public BaseCallBack<List<CallContactResponse.CallContact>> mGetOnlineUsersMgmtCallback;

    public void getOnlineUsers(Context context, BaseCallBack<List<CallContactResponse.CallContact>> callback) {
        mContext = context;
        mGetOnlineUsersMgmtCallback = callback;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            OnlineUsersResponse info = (OnlineUsersResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mGetOnlineUsersMgmtCallback != null) {
                    List<CallContactResponse.CallContact> data = info.getOnline_users();
                    mGetOnlineUsersMgmtCallback.success(data);
                }
            } else {
                if (mGetOnlineUsersMgmtCallback != null) {
                    mGetOnlineUsersMgmtCallback.error(info.error);
                }
            }
        } else {
            if (mGetOnlineUsersMgmtCallback != null) {
                mGetOnlineUsersMgmtCallback.error(null);
            }
        }
    }

    protected String getRequestUrl() {
        //http://223.202.103.135/app/API/users/online/users?page=1
        return String.format("%s/users/online/users", APP_REQUEST_URL);
    }

    protected void request() {
        addUserToken();
        addValidUrlParams();
        mUrlParams.put("page", "1");
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(OnlineUsersResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}

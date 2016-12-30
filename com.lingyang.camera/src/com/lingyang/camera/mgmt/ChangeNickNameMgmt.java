package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.ChangeNickNameEntity;
import com.lingyang.camera.entity.ChangeNickNameEntity.NickNameCls;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: ChangeNickNameMgmt
 * 描    述: 更改昵称
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class ChangeNickNameMgmt extends BaseMgmt {

    private String mNickName;
    private BaseCallBack<NickNameCls> mCallBack;

    public void changeName(Context context, String name, BaseCallBack<NickNameCls> callBack) {
        mContext = context;
        this.mNickName = name;
        this.mCallBack = callBack;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            ChangeNickNameEntity info = (ChangeNickNameEntity) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mCallBack != null) {
                    mCallBack.success(info.getData());
                }
            } else {
                mCallBack.error(info.getError());
            }
        } else {
            if (mCallBack != null) {
                mCallBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/users/update_nickname", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put("nickname", mNickName);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(ChangeNickNameEntity.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }
}

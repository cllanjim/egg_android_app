package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BindCameraEntity;
import com.lingyang.camera.entity.BindCameraEntity.BindEntity;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: BindCameraMgmt
 * 描    述: 绑定摄像头
 * 创建人: 刘波
 * 创建时间: 2015/9
 */
public class BindCameraMgmt extends BaseMgmt {

    public BaseCallBack<BindEntity> mBaseCallBack;
    private String mCName;
    private String mSN;

    public void bindCamera(Context context, final String sn, String cname, BaseCallBack<BindEntity> callback) {
        mContext = context;
        mCName = cname;
        mSN = sn;
        mBaseCallBack = callback;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BindCameraEntity info = (BindCameraEntity) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (mBaseCallBack != null) {
                    mBaseCallBack.success(info.getData());
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
        return String.format("%s/camera/bind", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CSN, mSN);
        mPostParams.put(KEY_CNAME, mCName);
        addUserToken();
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BindCameraEntity.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);

    }

}

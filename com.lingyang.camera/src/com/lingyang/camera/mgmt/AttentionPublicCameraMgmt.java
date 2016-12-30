package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: AttentionCameraMgmt
 * 描    述: 获取自己的摄像头列表
 * 创建人: 刘波
 * 创建时间: 2015/9
 */
public class AttentionPublicCameraMgmt extends BaseMgmt {

    public BaseCallBack mBaseCallBack;
    public String mCID;

    public void AttentionPublicCamera(Context context, String cid, BaseCallBack callback) {
        mContext = context;
        mCID = cid;
        mBaseCallBack = callback;
        new Handler().post(mRequestRunnable);
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
        return String.format("%s/public/follow", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, mCID);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(CameraResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}

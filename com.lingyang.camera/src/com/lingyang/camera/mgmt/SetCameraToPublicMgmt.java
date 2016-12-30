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

import java.util.Map;

/**
 * 文件名: SetCameraToPublicMgmt
 * 描    述: 设为公众
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class SetCameraToPublicMgmt extends BaseMgmt {

    public BaseCallBack<BaseResponse> mBaseCallBack;
    private String mCID;


    public void setPublic(Context context, final String cId, BaseCallBack<BaseResponse> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mCID = cId;
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
        return String.format("%s/camera/set_public", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, mCID);
        addUserTokenAndDeviceToken(mCID);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }


}

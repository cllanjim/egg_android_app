package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: CancelMyAttentionMgmt
 * 描    述: 取消关注
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class CancelMyAttentionMgmt extends BaseMgmt {

    public BaseCallBack<ResponseError> mBaseCallBack;
    private Handler mHandler;
    private Camera mAttention;

    public void cancelMyAttention(Context context, Camera mAttention, BaseCallBack<ResponseError> callback) {
        mContext = context;
        mBaseCallBack = callback;
        this.mAttention = mAttention;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final BaseResponse info = (BaseResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mBaseCallBack.success(null);
                    }
                });
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (mBaseCallBack != null) {
                            mBaseCallBack.error(info.getError());
                        }
                    }
                });

            }
        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (mBaseCallBack != null) {
                        mBaseCallBack.error(null);
                    }
                }
            });
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/public/unfollow", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, mAttention.cid);
        addUserTokenAndDeviceToken(mAttention.cid);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}

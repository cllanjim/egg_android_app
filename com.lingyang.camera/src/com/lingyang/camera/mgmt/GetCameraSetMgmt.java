package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.GetCameraSetResponse;
import com.lingyang.camera.entity.GetCameraSetResponse.CameraSet;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: GetCameraSetMgmt
 * 描    述: 获取摄像头配置信息
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class GetCameraSetMgmt extends BaseMgmt {

    public BaseCallBack<CameraSet> mBaseCallBack;
    private Handler mHandler;
    private String mCid;

    public void getCameraSet(Context context, String cid, BaseCallBack<CameraSet> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mCid = cid;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final GetCameraSetResponse info = (GetCameraSetResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (info.getData() != null && mBaseCallBack != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mBaseCallBack.success(info.getData());
                        }
                    });
                }
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
        //%s/cameras/config?cid=%s&uid=%s&ip=%s&expire=%s&access_token=%s
        return String.format("%s/camera/get_config", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addUserTokenAndDeviceToken(mCid);
        addValidUrlParams();
        mUrlParams.put("cid", mCid);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(GetCameraSetResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}

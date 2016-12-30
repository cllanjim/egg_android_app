package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.MyCamerasResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.List;
import java.util.Map;

/**
 * 文件名: GetMyCameraListMgmt
 * 描    述: 获取我的设备列表
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class GetMyCameraListMgmt extends BaseMgmt {

    public BaseCallBack<List<Camera>> mBaseCallBack;
    private Handler mHandler;

    public void getCameraList(Context context, BaseCallBack<List<Camera>> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final MyCamerasResponse info = (MyCamerasResponse) result;
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
        //%s/users/cameras/own/list?uid=%s&ip=%s&expire=%s&access_token=%s
        return String.format("%s/users/mine", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addUserToken();
        addValidUrlParams();
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(MyCamerasResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}

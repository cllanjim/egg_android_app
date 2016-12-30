package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CameraResponse.MyCameras;
import com.lingyang.camera.entity.PublicCameraResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.List;
import java.util.Map;

/**
 * 文件名: GetPublicCameraMgmt
 * 描    述: 获取公众摄像头
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class GetPublicCameraMgmt extends BaseMgmt {

    public BaseCallBack<List<MyCameras.Camera>> mBaseCallBack;
    private int mPage, mPageSize, mType;
    Handler mHandler;

    public void getPublicCamera(Context context, int page, int pageSize, int camera_type,
                                BaseCallBack<List<MyCameras.Camera>> callback) {
        mContext = context;
        mPageSize = pageSize;
        mBaseCallBack = callback;
        mType = camera_type;
        mPage = page;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final PublicCameraResponse info = (PublicCameraResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (info.getCameras() != null && mBaseCallBack != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mBaseCallBack.success(info.getCameras());
                        }
                    });

                }
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mBaseCallBack.error(info.getError());
                    }
                });

            }
        } else {
            if (mBaseCallBack != null) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mBaseCallBack.error(null);
                    }
                });
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        //%s/cameras/public/explore?uid=%s&ip=%s&expire=%s&access_token=%s&page=%s&page_size=%s&camera_type=%s
        return String.format("%s/public/square", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidUrlParams();
        mUrlParams.put("page", mPage + "");
        mUrlParams.put("size", mPageSize + "");
        mUrlParams.put("camera_type", mType + "");
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(PublicCameraResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }


}

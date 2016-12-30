package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CameraResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera.CameraOwner;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文件名: AttentionCameraMgmt
 * 描    述: 获取自己的摄像头列表
 * 创建人: 刘波
 * 创建时间: 2015/9
 */
public class AttentionCameraMgmt extends BaseMgmt {

    public BaseCallBack<List<Camera>> mBaseCallBack;
    private List<Camera> cameraList;
    private Handler mHandler;

    public void getMyCamera(Context context, BaseCallBack<List<Camera>> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final CameraResponse info = (CameraResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (info.getData() != null && mBaseCallBack != null) {
                    cameraList = new ArrayList<Camera>();
                    for (Camera item : info.getData().mine) {
                        item.setCameraOwner(CameraOwner.CAMERA_MINE);
                    }
                    for (Camera item : info.getData().share_to_me) {
                        item.setCameraOwner(CameraOwner.CAMERA_SHARA_TO_ME);
                    }
                    for (Camera item : info.getData().public_cameras) {
                        item.setCameraOwner(CameraOwner.CAMERA_PUBLIC);
                    }
                    cameraList.addAll(Arrays.asList(info.getData().mine));
                    cameraList.addAll(Arrays.asList(info.getData().share_to_me));
                    cameraList.addAll(Arrays.asList(info.getData().public_cameras));
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mBaseCallBack.success(cameraList);
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
        // /app/API/users/cameras?uid=5507f2bf96d336792a09c009&ip=23.12.34.12&expire=1429846954&access_token=bbd37875430acba31ef3c4b08420dd80
        return String.format("%s/users/cameras", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        String url = getRequestUrl();
        if (url == null)
            return;
        addUserToken();
        addValidUrlParams();
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(CameraResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }
}

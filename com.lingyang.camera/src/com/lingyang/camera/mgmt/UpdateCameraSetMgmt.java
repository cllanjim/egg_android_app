package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.GetCameraSetResponse.CameraSet;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: UpdateCameraSetMgmt
 * 描    述: 更改摄像头配置
 * 创建人:廖蕾
 * 创建时间: 2015/9
 */
public class UpdateCameraSetMgmt extends BaseMgmt {

    public BaseCallBack<BaseResponse> mBaseCallBack;
    private Handler mHandler;
    private String cid;
    private CameraSet mCameraSet;

    /**
     * @param context
     * @param cid
     * @param callback
     */
    public void UpdateCameraSet(Context context, String cid, CameraSet cs, BaseCallBack<BaseResponse> callback) {
        mContext = context;
        mBaseCallBack = callback;
        this.cid = cid;
        this.mCameraSet = cs;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final BaseResponse info = (BaseResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (mBaseCallBack != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mBaseCallBack.success(null);
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
        return String.format("%s/camera/update_config", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, cid);
        mPostParams.put("rate", mCameraSet.rate + "");
        mPostParams.put("silence", mCameraSet.silence + "");
        mPostParams.put("camera_type", mCameraSet.camera_type + "");
        addUserTokenAndDeviceToken(cid);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}

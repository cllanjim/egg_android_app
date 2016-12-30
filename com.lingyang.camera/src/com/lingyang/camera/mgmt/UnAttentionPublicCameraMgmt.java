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
 * 文件名: UnAttentionPublicCameraMgmt
 * 描    述: 取消关注公众摄像头
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class UnAttentionPublicCameraMgmt extends BaseMgmt {

    public BaseCallBack mBaseCallBack;
    public String mCID;
    public String mActionType;//1,取消关注  0，取消分享
    public static final String MGMT_UNATTENTION="1";
    public static final String MGMT_UNSHARED="0";


    public void UnAttentionPublicCamara(Context context, String cid,String actionType, BaseCallBack callback) {
        mContext = context;
        mCID = cid;
        mActionType=actionType;
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
        return String.format("%s/public/unfollow", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, mCID);
        mPostParams.put("is_ifollowed",mActionType);
        addUserTokenAndDeviceToken(mCID);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(CameraResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}

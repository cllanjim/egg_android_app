package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: RelieveBindMgmt
 * 描    述: 解除绑定摄像头
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class RelieveBindMgmt extends BaseMgmt {

    private BaseCallBack<BaseResponse> mCallBack;
    private String cid;

    public void relieveBind(Context context, String cid, BaseCallBack<BaseResponse> callBack) {
        mContext = context;
        this.mCallBack = callBack;
        this.cid = cid;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BaseResponse info = (BaseResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (mCallBack != null) {
                    mCallBack.success(null);
                }
            } else {
                mCallBack.error(info.getError());
            }
        } else {
            if (mCallBack != null) {
                mCallBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/camera/unbind", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, cid);
        addUserTokenAndDeviceToken(cid);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BaseResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }
}

package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.ShareResponse;
import com.lingyang.camera.entity.ShareUserResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: ShareCameraToOthersMgmt
 * 描    述: 分享摄像头
 * 创建人:刘波
 * 创建时间: 2015/9
 */
public class ShareCameraToOthersMgmt extends BaseMgmt {

    public BaseCallBack<ShareUserResponse.ShareUser> mBaseCallBack;
    private String mCid, mShareNickName;
    Handler handler = new Handler();

    public void shareCamaraToOther(Context context, final String cid, final String shareNickName,
                                   BaseCallBack<ShareUserResponse.ShareUser> callback) {
        mContext = context;
        mCid = cid;
        mShareNickName = shareNickName;
        mBaseCallBack = callback;
        handler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            ShareResponse info = (ShareResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (mBaseCallBack != null) {
                    mBaseCallBack.success(info.data);
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
        return String.format("%s/share/add", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addUserTokenAndDeviceToken(mCid);
        addValidPostParams();
        mPostParams.put(KEY_CID, mCid);
        mPostParams.put("share_nickname", mShareNickName);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(ShareResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }


}

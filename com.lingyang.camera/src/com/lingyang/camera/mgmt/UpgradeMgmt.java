package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.entity.UpgradeInfo;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

public class UpgradeMgmt extends BaseMgmt {

    private Context mContext;
    public UpgradeMgmtCallback mUpgradeMgmtCallback;

    /**
     * [获取版本信息]<BR>
     *
     * @param context
     * @param callback
     */
    public void checkUpgrade(Context context, UpgradeMgmtCallback callback) {
        mContext = context;
        mUpgradeMgmtCallback = callback;
        new Handler().post(mRequestRunnable);
    }

    public interface UpgradeMgmtCallback {
        void getUpgradeInfo(UpgradeInfo.Info otaUpgradeInfo);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            UpgradeInfo info = (UpgradeInfo) result;
            if (info != null) {
                if (mUpgradeMgmtCallback != null) {
                    mUpgradeMgmtCallback.getUpgradeInfo(info.getData());
                }
            }
        } else {
            if (mUpgradeMgmtCallback != null) {
                mUpgradeMgmtCallback.getUpgradeInfo(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return APP_REQUEST_URL + "/versions/version/latest";
    }

    @Override
    protected void request() {
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(UpgradeInfo.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}

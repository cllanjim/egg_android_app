package com.lingyang.sdk.cloud;

import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.extra.ACCDeviceBind;
import com.antelope.sdk.service.ACService;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.util.CLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 云平台接口
 */
public class LYService implements IService {

	private static ACService acService;
	private static LYService lyService;

	public static LYService getInstance() {

		if (lyService == null) {
			lyService = new LYService();
		}
		if (acService == null) {
			acService = ACService.getInstance();
		}
		return lyService;
	}

	@Override
	public void setDebuggable(boolean debuggable){
		CLog.setDebuggable(debuggable);
	}

	@Override
	public String getSDKVersion() {
		return acService.getVersion();
	}

	@Override
	public void setNativeLoggingEnabled(boolean enabled) {
		CLog.setDebuggable(enabled);
	}

	@Override
	public void startCloudService(String userToken, String config, final CallBackListener<Long> listener) {
		final long startTime = System.currentTimeMillis();
		ACResult result = acService.startCloudService(userToken, config, 5000, new ACResultListener() {

			@Override
			public void onResult(ACResult status) {
				long endTime = System.currentTimeMillis();
				if (status.isResultOK())
					listener.onSuccess(endTime - startTime);
				else
					listener.onError(new LYException(status.getCode(), "failed to start cloud service"));
				CLog.i("startcloudservice result " + status.getCode());
			}
		}, new ACMessageListener() {

			@Override
			public void onMessage(int type, Object message) {
				if (message != null) {
					CloudMessage msg=exeMsg((String)message);
					mAcceptMessageListenr.accept(msg);
					if(connectedListener!=null)
						connectedListener.accept(msg);
				}
			}

		});
		if (!result.isResultOK()) {
			listener.onError(new LYException(result.getCode(), result.getErrMsg()));
		}
	}

	private CloudMessage exeMsg(String msg) {
		try {
			JSONObject jsobj = new JSONObject(msg);
			CloudMessage Cmsg = new CloudMessage();
			Cmsg.Name = jsobj.optString("Name");
			Cmsg.Message=jsobj.optString("Message");
			Cmsg.SrcID=jsobj.optInt("SrcId");
			return Cmsg;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void stopCloudService() {
		if (isOnline())
			acService.stopCloudService();
	}

	@Override
	public boolean isOnline() {
		// TODO Auto-generated method stub
		return acService.isOnline();
	}

	private AcceptMessageListener mAcceptMessageListenr;

	@Override
	public void setCloudMessageListener(AcceptMessageListener acceptCloudMessageListener) {
		mAcceptMessageListenr = acceptCloudMessageListener;
	}

	public boolean connectAP(String aUUID,String aPassword,int aWifiType,String aHashID){
		return ACCDeviceBind.getInstance().connectDeviceAp(aUUID,aPassword,aWifiType,aHashID)==0;
	}
	
	  /**
     * 对方连接成功监听接口，内部调用
     * @author LiaoLei
     *
     */
    public interface onConnectedListener{
    	void accept(CloudMessage msg);
    }
    private onConnectedListener connectedListener;
    public void setOnConnectedListener(onConnectedListener listener){
    	connectedListener=listener;
    }

}

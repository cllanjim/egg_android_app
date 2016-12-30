package com.antelope.sdk.service;

import android.text.TextUtils;

import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;

public class ACService {
	
	private static class SingletonInstanceHolder {
		private static final ACService instance = new ACService();
	}
	
	private ACService() {
		
	}
	
	
	/**
	 * 获取云服务实例
	 * @return 云服务实例
	 */
	public static ACService getInstance() {
		return SingletonInstanceHolder.instance;
	}
	
	/**
	 * 启动云服务
	 * @param token 校验token
	 * @param config 配置串
	 * @param timeout 超时，单位毫秒，-1表示一直等待，0表示立即返回，大于0表示操作完成前要等待的毫秒数
	 * @param resultListener 状态值监听
	 * @param messageListener 消息监听
	 */
	public ACResult startCloudService(final String token,final String config, final int timeout, final ACResultListener resultListener, final ACMessageListener messageListener) {
		
			//确保所有参数合理合法
			if(TextUtils.isEmpty(token)) //token 为null或者是空串
				return new ACResult(ACResult.ACS_INVALID_ARG,"token is empty"); 
		    if(TextUtils.isEmpty(config))
		    	return new ACResult(ACResult.ACS_INVALID_ARG,"config is empty"); 
		    
		    if(resultListener==null)
		    	return  new ACResult(ACResult.ACS_INVALID_ARG,"resultListener is none");
		    
		    if(messageListener==null){
		    	return  new ACResult(ACResult.ACS_INVALID_ARG,"messageListener is none");
		    }
		    
		    ACWorkThread.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					ACPlatformAPI.getInstance().startCloudService(token, config, timeout, resultListener, messageListener);
				}
			});
		    
		    return ACResult.SUCCESS;
	}
	
	/**
	 * 停止云服务
	 */
	public void stopCloudService() {
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ;
		}
		
		if(!isOnline()){  //不在线就
			return ;
		}
		ACWorkThread.getInstance().stop();//停止工作线程
		ACPlatformAPI.StopCloudService(); //停止云服务
		
	}
	
	/**
	 * 获取SDK版本号
	 * @return sdk版本号
	 */
	public String getVersion() {
		if(!ACPlatformAPI.hasAuthorize()){
			return "please open cloud service";
		}
		    return "3.0.10";
	}
	
	/**
	 * 判断用户当前是否在线
	 * @return 在线状态
	 * result 1表示在线  0表示离线  -1表示jni层调用c++中的方法失败
	 */
	public boolean isOnline() {
		
		int result=ACPlatformAPI.IsOnline();
		if(result==1){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 更新token
	 * @param type token类型，详细定义请参考 {@link ACTokenType}
	 * @param token token串
	 * @return 错误码及错误描述
	 */
	public ACResult updateToken(int type, String token) {
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
		if(TextUtils.isEmpty(token)){
			return new ACResult(ACResult.ACS_INVALID_ARG,"token can not be null");
		}
		
		if(!isOnline()){ //
			return new ACResult(ACResult.ACS_NOT_ONLINE,"not online,please open cloud service");
		}
		int result =ACPlatformAPI.UpdateToken(type,token);
		if(result==0){
			return ACResult.SUCCESS;
		}else{
			return new ACResult(result,"fail update userToken");
		}
	}
	
	public ACResult setCloudPlatformMessageListener(ACMessageListener messageListener){
		if(messageListener==null)
			 return new ACResult(ACResult.ACS_INVALID_ARG,"messageListener is none");
		ACPlatformAPI.getInstance().setCloudPlatformMessageListener(messageListener);
		     return ACResult.SUCCESS;
	}
	
}

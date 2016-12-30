package com.antelope.sdk.service;

import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACMessageType;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;

public class ACPlatformAPI {
	private static ACPlatformAPI instance=null;
    private ACPlatformAPI(){}
    static{
   	 System.loadLibrary("EDKService_topvdn");
    }
    
    public static ACPlatformAPI getInstance(){
   	 if(instance==null)
   		instance=new ACPlatformAPI();
   	   return instance;
    }
    
    private ACMessageListener mACMessageListener=null; //平台及P2P消息监听器
    
    

	//底层回调上层的方法
    public void JMessageNativeCallback(String message){
   	   if(mACMessageListener!=null){
   	     mACMessageListener.onMessage(ACMessageType.AC_MESSAGE_CLOUD_SERVICE, message);
   	   }
    }
    
    
    
    //处理下 ACMessageListener
    private void processACMessageListener(ACMessageListener messageListener)
    {
   	 if(messageListener!=null){
   		 MessageNativeCallback(); 
   	 }
   	 mACMessageListener= messageListener;
    }
   
   /**
    * 开启云服务
    * @param token           用户token
    * @param config          配置串
    * @param timeout         等待多久之后登录云服务
    * @param resultListener  登录成功与否的结果监听
    * @param messageListener 平台消息的监听
    */
   public void startCloudService(String token, String config, int timeout, ACResultListener resultListener, ACMessageListener messageListener)
   {  
   	 int result=StartCloudService(token,config,timeout);
   	 
   	 if(result==0){
   		 resultListener.onResult(ACResult.SUCCESS);
   	 }else{
   		 ACResult status =new ACResult(result,"fail startCloudService");
   		 resultListener.onResult(status);
   	 }
   	 
   	 processACMessageListener(messageListener);
   }
    
    
   public static boolean hasAuthorize(){
   	if(HasAuthorize()==1){
   		return true;
   	}else{
   		return false;
   	}
   }
   
   //设置云平台消息监听
   public void setCloudPlatformMessageListener(ACMessageListener messageListener){
	   processACMessageListener(messageListener);
   }
   
    //是否在线    1是在线    0是离线
    public static native int IsOnline();
    
    //停止云服务
    public static native void StopCloudService();
    
    //更新userToken 0是成功,其他值为失败
    public static native int UpdateToken(int tokenType,String userToken);
    
    //开始云服务 	0 开启云服务成功,其他值为失败		
    private static native int StartCloudService(String userToken, String configString,int timeOut);
    
    //是否已经授权    1表示 授权           0表示 没有授权   
    private static native int HasAuthorize();
    
    //用于底层，获取javaVM指针以及一个全局的jobject
    private native void MessageNativeCallback();
    
}

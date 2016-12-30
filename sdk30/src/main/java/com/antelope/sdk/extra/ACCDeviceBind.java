package com.antelope.sdk.extra;

/**
 * Created by liaolei on 2016/12/15.
 */

public class ACCDeviceBind {

    static{
        System.loadLibrary("EDKExtra_topvdn");
    }

    private static ACCDeviceBind mInstance;

    public static ACCDeviceBind getInstance(){
        if(mInstance==null){
            mInstance=new ACCDeviceBind();
        }
        return mInstance;
    }
    //绑定设备
    native int ConnectDeviceAP(String aUUID,String aPassword,int aWifiType,String aHashID);

    public int connectDeviceAp(String aUUID,String aPassword,int aWifiType,String aHashID){
        return ConnectDeviceAP(aUUID,aPassword,aWifiType,aHashID);
    }

}

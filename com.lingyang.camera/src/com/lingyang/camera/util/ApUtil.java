package com.lingyang.camera.util;

import com.lingyang.base.utils.StringUtils;
import com.lingyang.sdk.util.CLog;

/**
 * 获取热点sn
 *
 * @author 波
 */
public class ApUtil {

    /**
     * 根据mac地址获取设备natid
     * 通过wifi扫描得到设备SSID、BSSID,BSSID=mac地址，用这个mac地址可以算出设备ID，
     *
     * @param strMac
     * @return
     */
    public static String getDevIDByMac(String strMac) {
        String ret = null;
        String[] strMacB = strMac.split(":");
        String strTemp = "";
        int iLen = strMacB.length;
        for (int i = 0; i < iLen; i++) {
            strTemp += strMacB[i];
        }
        int[] bnatid = new int[6];
        byte[] b = StringUtils.toByte(strTemp);
        for (int i = 0; i < 6; i++) {
            bnatid[i] = b[i];
            bnatid[i] &= 0xff;
        }
        long tInt1 = (bnatid[0] << 8) | bnatid[1];
        long tInt2 = (bnatid[2] << 24) | (bnatid[3] << 16) | (bnatid[4] << 8)
                | (bnatid[5]);

        long natid = (tInt1 << 32) | (tInt2 & 0xffffffffl);
        ret = Long.toString(natid);
        CLog.v("mac is " + strMac + ", natid=" + ret);
        return ret;
    }


}

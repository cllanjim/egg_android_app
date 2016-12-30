package com.lingyang.camera.entity;

import android.net.wifi.WifiManager;

import com.lingyang.camera.config.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Wifi implements CameraType, Serializable, Comparable<Wifi> {

    /**
     *
     */
    private static final long serialVersionUID = 3445443367215054189L;

    private String SSID;
    private String BSSID;
    private String MODE;
    private String FREQ;
    private String RATE;
    private String SIGNAL;
    private String AUTH;
    private String ACTIVE;
    private String PASSWORD = "";
    private int LEVEL;
    private long TIMESTAMP;
    private int AUTHTYPE;
    private String QID;
    private String DST;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String sSID) {
        SSID = sSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String bSSID) {
        BSSID = bSSID;
    }

    public String getMODE() {
        return MODE;
    }

    public void setMODE(String mODE) {
        MODE = mODE;
    }

    public String getFREQ() {
        return FREQ;
    }

    public void setFREQ(String fREQ) {
        FREQ = fREQ;
    }

    public String getRATE() {
        return RATE;
    }

    public void setRATE(String rATE) {
        RATE = rATE;
    }

    public String getSIGNAL() {
        return SIGNAL;
    }

    public void setSIGNAL(String sIGNAL) {
        SIGNAL = sIGNAL;
    }

    public String getAUTH() {
        return AUTH;
    }

    public void setAUTH(String aUTH) {
        AUTH = aUTH;
    }

    public String getACTIVE() {
        return ACTIVE;
    }

    public void setACTIVE(String aCTIVE) {
        ACTIVE = aCTIVE;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String pASSWORD) {
        PASSWORD = pASSWORD;
    }

    public int getLevel() {
        return LEVEL;
    }

    public void setLevel(int level) {
        this.LEVEL = level;
    }

    public long getTIMESTAMP() {
        return TIMESTAMP;
    }

    public void setTIMESTAMP(long tIMESTAMP) {
        TIMESTAMP = tIMESTAMP;
    }

    public int getAUTHTYPE() {
        return AUTHTYPE;
    }

    public void setAUTHTYPE(int aUTHTYPE) {
        AUTHTYPE = aUTHTYPE;
        if (AUTHTYPE == Constants.WifiType.WIFI_TYPE_UNSUPPORTED) {
            SSID = SSID + "(不支持使用)";
        }
    }

    public String getQID() {
        return QID;
    }

    public void setQID(String qID) {
        QID = qID;
    }

    public String getDST() {
        return DST;
    }

    public void setDST(String DST) {
        this.DST = DST;
    }

    public String getQRcode() {

        String qr = "";
        /*
         * try { qr = "WIFI:S:" + getSSID() + ";P:" + getPASSWORD() + ";TS:" +
		 * getTIMESTAMP() + ";Q:" + QID + ";;"; qr =
		 * AESUtil.encryptBase64(Const.AEC_KEY, qr); } catch (CameraAesException
		 * e) { e.printStackTrace(); }
		 */
        return qr;
    }

    public JSONObject getSmartConfig() throws JSONException {
        JSONObject smartConfig = new JSONObject();
        smartConfig.put("ssid", SSID);
        smartConfig.put("password", PASSWORD);
        smartConfig.put("broadlinkv2", 1);
        smartConfig.put("dst", DST);
        return smartConfig;
    }

    @Override
    public int compareTo(Wifi another) {
        if (getAUTHTYPE() != 4 && another.getAUTHTYPE() == 4) {
            return -1;
        }
        if (getAUTHTYPE() == 4 && another.getAUTHTYPE() != 4) {
            return 1;
        }
        if (getLevel() != Integer.MAX_VALUE && another.getLevel() == Integer.MAX_VALUE)
            return -1;
        if (getLevel() == Integer.MAX_VALUE && another.getLevel() != Integer.MAX_VALUE)
            return 1;
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(another.getLevel(), getLevel());
        if (difference != 0) {
            return difference;
        }
        return 0;
    }
}

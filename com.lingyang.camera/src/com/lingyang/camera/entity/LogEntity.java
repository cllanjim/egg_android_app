package com.lingyang.camera.entity;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

/**
 * 
 * @author Administrator
 * 
 */
public class LogEntity {

    private String taskid;
    private int type;
    private long t1;
    private long t2;
    private long t3;
    private long t4;
    private long t5;

    private int value;//
    /*
     * net 网络类型， 说明: 0=wifi 1=GPRS 2=EDGE 3=UMTS 4=CDMA: Either IS95A or IS95B 5=EVDO revision 0
     * 6=EVDO revision A 7=1xRTT 8=HSDPA 9=HSUPA 10=HSPA 11=iDen 12=EVDO revision B 13=LTE 14=eHRPD
     * 15= HSPA+ 16=其他位置手机网络
     */
    private int net = -1;
    private String data;// wifi或者移动网络ip 没有填null | type = 6时  为断开连接的异常信息


    public boolean isComplete() {
        if (t1 == 0) {
            return false;
        }
        if (t2 == 0) {
            return false;
        }
        if (type == 1) {// 同步请求
            return true;
        }
        if (type == 2) {// 异步请求
            if (t3 == 0) {
                return false;
            }
            return true;
        }
        if (type == 3) {// 播放请求
            if (t4 == 0 && t5 == 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(System.currentTimeMillis());
        return timestamp + "\ttype=" + type + "\ttaskid=" + taskid + "\tt1=" + t1 + "\tt2=" + t2
                + "\tt3=" + t3 + "\tt4=" + t4 + "\tt5=" + t5 + "\r\n";
    }

    @SuppressLint("SimpleDateFormat")
    public String toStringLog() {
        String valueKey;
        String taskidItem = "";
        String netItem = "";
        String ipItem = "";
        switch (type) {
            case 4:
                valueKey = "errorCode";
                break;
            case 5:
            case 6:
                valueKey = "status";
                break;
            default:
                valueKey = "unknow";
                break;
        }
        if (taskid != null) {
            taskidItem = "\ttaskid=" + taskid;
        }
        if (getNet() != -1) {
            netItem = "\tnet=" + getNet();
        }

        if (data != null) {
            ipItem = "\tdata=" + data;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        sb.append(timestamp);
        sb.append("\ttype=");
        sb.append(type);
        sb.append(taskidItem);
        sb.append("\t");
        sb.append(valueKey);
        sb.append("=");
        sb.append(value);
        sb.append(netItem);
        sb.append(ipItem);
        sb.append("\r\n");
        
        return sb.toString();
    }

    public boolean setTimestamp(int tName, Long timestamp) {
        switch (tName) {
            case 1:
                t1 = timestamp;
                break;
            case 2:
                t2 = timestamp;
                break;
            case 3:
                t3 = timestamp;
                break;
            case 4:
                t4 = timestamp;
                break;
            case 5:
                t5 = timestamp;
                break;
            default:
                break;
        }
        return isComplete();
    }

    /**
     * 
     * @param value http请求的错误码 | 长连1=连接，2=断开
     */
    public void setValue(int value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public long getT1() {
        return t1;
    }

    public void setT1(long t1) {
        this.t1 = t1;
    }

    public long getT2() {
        return t2;
    }

    public void setT2(long t2) {
        this.t2 = t2;
    }

    public long getT3() {
        return t3;
    }

    public void setT3(long t3) {
        this.t3 = t3;
    }

    public int getValue() {
        return value;
    }

    public int getNet() {
        return net;
    }

    public void setNet(int net) {
        this.net = net;
    }

    public String getIp() {
        return data;
    }

    public void setData(String ip) {
        this.data = ip;
    }
}

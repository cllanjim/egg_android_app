package com.lingyang.camera.exception;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.base.utils.http.Request;
import com.lingyang.base.utils.http.WebUtils;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.LogEntity;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.camera.util.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 日志工具类 日志记录入本地文件 上传服务器
 */
public class LogUtil {

    public static int TYPE1 = 1;// 同步请求时间（获取列表）
    public static int TYPE2 = 2;// 异步请求时间（信令相关）
    public static int TYPE3 = 3;// 播放时间
    public static int TYPE4 = 4;// 错误
    public static int TYPE5 = 5;// 网络连接
    public static int TYPE6 = 6;// 长连状�?? 1连接�? 2未连接上
    public static int T1 = 1;// 请求�?�?
    public static int T2 = 2;// https请求完成
    public static int T3 = 3;// (异步�?)收到push | 超时 | 连上relay
    public static int T4 = 4;// 收到�?
    public static int T5 = 5;// 播放放弃时间
    private static LogUtil instance = new LogUtil();
    private final String LINE_END = "\r\n";
    Context mContext;
    String lineSeparator = "----------";
    private Map<String, LogEntity> logPool = new HashMap<String, LogEntity>();

    private LogUtil() {
        super();
    }

    public static LogUtil getInstance() {
        return instance;
    }

    /**
     * �?要分几个时间点搜集数�?,齐了再写入日志的用此方法
     *
     * @param taskid    任务id
     * @param type      任务类型,参见本类常量 TYPE
     * @param tName     时间戳名,参见本类常量T
     * @param timestamp 时间�?
     */
    public void setTimestamp(String taskid, int type, int tName, Long timestamp) {
        final LogEntity logEntity;
        if (logPool.containsKey(taskid + type)) {// 用taskid+type 作为键�??,确保唯一�?
            logEntity = logPool.get(taskid + type);
        } else {
            if (tName != T1) {// 要新建一个LogEntity 必须从T1�?�?
                return;
            }
            logEntity = new LogEntity();
            logEntity.setTaskid(taskid);
            logEntity.setType(type);
            logPool.put(taskid + type, logEntity);

        }
        if (logEntity.setTimestamp(tName, timestamp)) {
            ThreadPoolManagerNormal.execute(new Runnable() {

                @Override
                public void run() {
                    saveLog2File(logEntity.toString());
                }
            });
            logPool.remove(taskid + type);
        }
    }

    @SuppressLint("SdCardPath")
    private void saveLog2File(String content) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(FileUtil.getInstance().getLogFile(), "log.txt");
                if (!file.exists()) {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(collectDeviceInfo(Utils.getContext()).toString().getBytes("utf-8"));
                    fos.close();
                }
                CLog.d("记录:" + file.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(content.getBytes("utf-8"));
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuffer collectDeviceInfo(Context ctx) {
        // 用来存储设备信息和异常信�?
        Map<String, String> infos = new HashMap<String, String>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            CLog.e("an error occured when collect package info:" + e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                CLog.d(field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                CLog.e("an error occured when collect crash info:" + e);
            }
        }

        StringBuffer sb = new StringBuffer();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(System.currentTimeMillis());
        sb.append(timestamp + "\ttype=0\t");
        TelephonyManager tm = (TelephonyManager) Utils.getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        sb.append("imei=" + deviceId + "\t");
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\t");
        }
        sb.append("\r\n");
        return sb;
    }

    /**
     * type�? 4 5 6 �?(�?次�?�记入的log) 用此方法
     *
     * @param taskid 没有填null
     * @param type   必须
     * @param value  记入的�?? : errorCode | 长连状�?? | 网络连接状�?? 1=连接�?2=断开
     * @param net    网络类型 没有�? 0
     * @param data   wifi或�?�移动网络ip 没有填null | type = 6�? 为断�?连接的异常信�?
     */
    public void saveLogValue(String taskid, int type, int value, int net, String data) {
        final LogEntity logEntity = new LogEntity();
        logEntity.setTaskid(taskid);
        logEntity.setType(type);
        logEntity.setValue(value);
        logEntity.setNet(net);
        logEntity.setData(data);
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                saveLog2File(logEntity.toStringLog());
            }
        });
    }

    public void saveHttpLog2File(final long requestStartTime, final long responseTime,
                                 final Request request, final int state,
                                 final long requestTime, final Map<String, String> headMap,
                                 final Map<String, String> headParams,
                                 final Map<String, String> postParams,
                                 final Map<String, String> urlParams) {
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                File dir = FileUtil.getInstance().getLogFile();
                if (dir == null || !dir.exists()) {
                    return;
                }
                StringBuilder httpLog = new StringBuilder();

                httpLog.append(lineSeparator).append("http log start ");
                httpLog.append(DateTimeUtil.formatTime(requestStartTime));
                httpLog.append(lineSeparator).append(LINE_END);

                httpLog.append("url:").append(request.getUrl()).append(LINE_END);

                httpLog.append("params:");
                try {
                    String params = "";
                    if (postParams != null) {
                        params = WebUtils.buildQuery(postParams, WebUtils.DEFAULT_CHARSET, false);
                    } else if (urlParams != null) {
                        params = WebUtils.buildQuery(urlParams, WebUtils.DEFAULT_CHARSET, false);
                    }
                    httpLog.append(params);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpLog.append(LINE_END);

                if (headParams != null) {
                    for (String key : headParams.keySet()) {
                        String value = headParams.get(key);
                        httpLog.append("Head:" + key + " = " + value).append(LINE_END);
                    }
                }
                httpLog.append("response:");
                httpLog.append(headMap == null ? state + "" : headMap.get("X-Android-Response-Source"));
                httpLog.append(LINE_END);
                httpLog.append("requestTime:").append(requestTime).append("ms").append(LINE_END);

                httpLog.append(lineSeparator).append("http log end  ");
                httpLog.append(DateTimeUtil.formatTime(responseTime));
                httpLog.append(lineSeparator).append(LINE_END).append(LINE_END);

                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    createLogFileAndAddContent(dir, httpLog.toString());
                } else {
                    File file = getExistLog();
                    if (file != null) {
                        addContent2File(file, httpLog.toString());
                    } else {
                        createLogFileAndAddContent(dir, httpLog.toString());
                    }
                }
            }
        });
    }

    private void createLogFileAndAddContent(File dir, String httpLog) {
        try {
            String fileName = "debug.txt";
            File logFile = new File(dir, fileName);
            logFile.createNewFile();
            addContent2File(logFile, httpLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getExistLog() {
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().contains("debug")) {
                    return file;
                }
            }
        }
        return null;
    }


    private void addContent2File(File logFile, String content) {
        try {
            CLog.v("file length " + logFile.length());
            if (logFile.length() >= 512 * 1024) {
                logFile.delete();
                logFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(content.getBytes("utf-8"));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSystemLog2File() {
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return;
        }
        StringBuilder systemLog = new StringBuilder();
        systemLog.append(lineSeparator).append("system log start").append(lineSeparator).append(LINE_END);
        systemLog.append(getSystemLog()).append(LINE_END);
        systemLog.append(lineSeparator).append("system log end").append(lineSeparator)
                .append(LINE_END).append(LINE_END);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                if (files[i].getName().contains("debug")) {
                    addContent2File(files[i], systemLog.toString());
                    break;
                } else {
                    try {
                        String fileName = "debug.txt";
                        File logFile = new File(dir, fileName);
                        logFile.createNewFile();
                        addContent2File(logFile, systemLog.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getSystemLog() {
        StringBuilder log = new StringBuilder();
        try {
            Process process = null;
            process = Runtime.getRuntime().exec("logcat -d -v time |grep \"(" +
                    Utils.getAppPid() + ")\"");//+ "|grep -v \"Surface::\" |grep -v \"System.out\""
            CLog.v("getAppPid " + Utils.getAppPid());
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (i <= 500) {
                    log.append(line).append(LINE_END);
                    i++;
                } else {
                    break;
                }
            }
            bufferedReader.close();
            return log.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将日志文件上传到服务�?
     */
    public void uploadLog2Server() {
        ThreadPoolManagerNormal.execute(new Runnable() {

            @Override
            public void run() {
                uploadLog();
            }
        });
    }
    public boolean uploadDebugLog(){
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return false;
        }
        for (File logFile : dir.listFiles()) {
            if (logFile.isFile()) {
                return uploadFile(logFile, Const.APP_SERVER_UPLOAD_LOG,true);
            }
        }
        return false;
    }

    public boolean uploadLog() {
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return false;
        }

        File crashDir = new File(dir, "crash");
        if (crashDir.exists()) {
            for (File crashFile : crashDir.listFiles()) {
                if (crashFile.isFile()) {
                    return uploadFile(crashFile, Const.APP_SERVER_UPLOAD_LOG,false);
                }
            }
        }
        return false;
    }

    private boolean uploadFile(File logFile, String server,boolean isDebugLog) {
        if (LocalUserWrapper.getInstance().getLocalUser() == null)
            return false;
        try {
            if (logFile.getName().equals("log.txt")) {
                logFile.renameTo(new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android.")));
                logFile = new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android."));
            } else if (logFile.getName().contains("error")) {// crash日志
                boolean renameTo = logFile.renameTo(new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android_crash.")));
                CLog.v(renameTo + " " + logFile.getName());
                logFile = new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android_crash."));
                CLog.v(logFile.getName());
            } else if (logFile.getName().contains("debug")&&isDebugLog) {//debug日志
                boolean renameTo = logFile.renameTo(new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android_debug.")));
                CLog.v(renameTo + " " + logFile.getName());
                logFile = new File(FileUtil.getInstance().getLogFile(),
                        getFilename("camera_android_debug."));
                CLog.v(logFile.getName());
            } else {
                return true;
            }

            String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
            String PREFIX = "--";

            URL url = new URL(server);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoInput(true); // 允许输入�?
            conn.setDoOutput(true); // 允许输出�?
            conn.setUseCaches(false); // 不允许使用缓�?
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Host", Const.APP_SERVER_HOST_IP);
            conn.setRequestProperty("Charset", "utf-8");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            // 表单内容
            StringBuffer sb = new StringBuffer();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            sb.append("Content-Disposition: form-data; name=\"uname\"");
            sb.append(LINE_END);
            sb.append(LINE_END);
            sb.append(LocalUserWrapper.getInstance().getLocalUser().getUid());
            sb.append(LINE_END);

            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            sb.append("Content-Disposition: form-data; name=\"expire\"");
            sb.append(LINE_END);
            sb.append(LINE_END);
            sb.append(LocalUserWrapper.getInstance().getLocalUser().getExpire());
            sb.append(LINE_END);

            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            sb.append("Content-Disposition: form-data; name=\"access_token\"");
            sb.append(LINE_END);
            sb.append(LINE_END);
            sb.append(LocalUserWrapper.getInstance().getLocalUser().getAccessToken());
            sb.append(LINE_END);

            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            sb.append("Content-Disposition: form-data; name=\"log\"; filename=\""
                    + logFile.getName() + "\"" + LINE_END);
            sb.append("Content-Type: application/octet-stream; charset=" + "utf-8" + LINE_END);
            sb.append(LINE_END);
            dos.write(sb.toString().getBytes());

            InputStream is = new FileInputStream(logFile);
            byte[] bytes = new byte[1024 * 1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                dos.write(bytes, 0, len);
            }
            is.close();
            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();

            int res = conn.getResponseCode();
            CLog.d("ResponseCode:" + res);
            if (res == 200) {
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                String result = sb1.toString();
                result = new String(result.getBytes("iso8859-1"), "utf-8");
                CLog.d("result : " + result + " : over");// result不是合法的json格式
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("status_code")
                        && jsonObject.getInt("status_code") == Constants.Http.STATUS_CODE_SUCESS) {
//                    if (!Const.DEBUG) {
                    logFile.delete();
//                    }
                    CLog.d("成功: " + server + "   " + logFile.getName() + " deleted ");
                    return true;
                }
            }
            CLog.d("失败: " + server + "   " + logFile.getName());
            return false;
        } catch (Exception e) {
            CLog.e(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getFilename(String pre) {

        TelephonyManager tm = (TelephonyManager) Utils.getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(System.currentTimeMillis());
        String uid = LocalUserWrapper.getInstance().getLocalUser() == null ? "" : LocalUserWrapper
                .getInstance().getLocalUser().getUid();
        return pre + "uid[" + uid + "]." + deviceId + "." + getCurrentTime() + ".txt";
    }

    public static String getCurrentTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String time = sdf.format(date);
        return time;
    }

    public void savePlayerLog2File(String format) {
        CLog.d(format);
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return;
        }
        StringBuilder playerLog = new StringBuilder();
        playerLog.append(format);

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            createLogFileAndAddContent(dir, playerLog.toString());
        } else {
            File file = getExistLog();
            if (file != null) {
                addContent2File(file, playerLog.toString());
            } else {
                createLogFileAndAddContent(dir, playerLog.toString());
            }
        }
    }
}

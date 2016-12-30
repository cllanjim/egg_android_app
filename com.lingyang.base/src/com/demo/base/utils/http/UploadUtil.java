package com.lingyang.base.utils.http;

import android.content.Context;

import com.lingyang.base.utils.FileUtil;
import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.http.UploadRequest.UploadFileBean;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UploadUtil {

    private static final String TAG = "UploadUtil";

    private static final int CONNECTION_TIMEOUT = 40 * 1000;

    /**
     * 调用错误
     */
    public static final int STATE_CALL_ERROR = -1;

    /**
     * 崩溃异常
     */
    public static final int STATE_EXCEPTION = 0;

    /**
     * 请求成功
     */
    public static final int STATE_SUC = 1;

    /**
     * 请求超时
     */
    public static final int STATE_TIME_OUT = 2;

    /**
     * 网络不可用
     */
    public static final int STATE_NETWORD_UNSEARCHABLE = 3;

    public static final String BOUNDARY = "***************7da2137580612";
    static final String END = "\r\n";
    static final String TWOHYPHENS = "--";

    public static void startUploadFile(Context context, UploadRequest request) {
        if (context == null || request == null) {
            return;
        }
        Thread thread = new UploadThread(context, request);
        thread.start();
    }

    static class UploadThread extends Thread {

        Context context;
        UploadRequest request;

        UploadThread(Context context, UploadRequest request) {
            this.request = request;
            this.context = context;
        }

        public void run() {
            OnRequestListener moreListener = request.getOnRequestListener();
            String url = request.getUrl();
            List<UploadFileBean> uploadList = request.getUploadFileList();
            Map<String, String> getParams = request.getUriParam();
            long timeOut = request.getTimeout();
            Map<String, String> httpHead = request.getHttpHead();
            if (httpHead == null) {
                httpHead = new HashMap<String, String>();
            }
            httpHead.put("Charset", "UTF-8");
            httpHead.put("connection", "keep-alive");
            httpHead.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            if (timeOut == -1) {
                timeOut = CONNECTION_TIMEOUT;
            }

            int state = STATE_SUC;
            Object result = null;
            if (!NetWorkUtils.isNetworkAvailable(context)) {
                Log.e(TAG, "connection - 网络不可用，抛弃请求");
                state = STATE_NETWORD_UNSEARCHABLE;
                if (moreListener != null) {
                    moreListener.onResponse(url, state, result, 0, request, null);
                }
                return;
            }

            long t1 = System.currentTimeMillis();

            Map<String, String> resultMap = null;
            try {
                Object postParam = request.getPostData();

                if (postParam != null && postParam instanceof HashMap) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) postParam;
                    resultMap = uploadFile(url, hashMap, uploadList, httpHead, getParams, (int) timeOut);
                } else {
                    resultMap = uploadFile(url, null, uploadList, httpHead, getParams, (int) timeOut);
                }
                result = resultMap.get(WebUtils.MAP_KEY_RESULT);

            } catch (HttpConnectionResultException e) {
                // Http Result不为200
                result = e.getMessage();
                state = Integer.parseInt(result.toString());

            } catch (IOException e) {
                result = "connection error";
                state = STATE_TIME_OUT;
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                result = e.getMessage() + "";
                state = STATE_EXCEPTION;
            }

            long t2 = System.currentTimeMillis();
            long time = t2 - t1;
            request.setRequestTime(time);
            Log.i(TAG, "【End】time:" + ((time) / 1000.0f) + " - state:" + state + " - " + url);

            if (result == null) {
                state = STATE_EXCEPTION;
            }

            Log.i(TAG, "连接结束：" + url + " - state:" + state);
            Log.i(TAG, "result: " + result);
            if (state == STATE_SUC) {
                IDataParser parser = request.getParser();
                if (parser != null) {
                    result = parser.parseData(result.toString());
                }
            }

            if (moreListener != null) {
                moreListener.onResponse(url, state, result, request.getRequestType(), request, resultMap);
            }
        }
    }


    public static Map<String, String> uploadFile(String url, Map<String, String> postParam, List<UploadFileBean> fileList, Map<String, String> httpHead,
                                                 Map<String, String> getParamMap, int timeOut) throws IOException {

        HttpURLConnection conn = null;
        OutputStream out = null;
        BufferedInputStream fileInputStream = null;
        String rsp = null;

        url = WebUtils.buildRequestUrl(url, getParamMap, true);

        Log.i(TAG, "uploadFile: " + url);
        try {
            conn = WebUtils.getConnection(new URL(url), WebUtils.METHOD_POST, httpHead, timeOut);
            conn.setDoOutput(true);
            out = conn.getOutputStream();
//			out.write(post.getBytes());
            //                writeFileOutputStream(out, fileList, true);
            if (out != null)
                writeFileOutputStream(out, postParam, fileList);

            int responseCode = conn.getResponseCode();

            Log.i(TAG, "connection - responseCode: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                String charset = WebUtils.getResponseCharset(conn.getContentType());
                rsp = WebUtils.getStreamAsString(inputStream, charset);

                Map<String, String> resultMap = new HashMap<String, String>();
                resultMap.put(WebUtils.MAP_KEY_RESULT, rsp);
                WebUtils.getHttpHeadMap(conn, resultMap);

                Log.v(TAG, "结果(" + charset + ")：" + rsp);
                return resultMap;
            } else {
                throw new HttpConnectionResultException(responseCode);
            }

        } catch (IOException e) {
            Log.e(TAG, "connection - IOException:" + e.getLocalizedMessage());
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }


    /**
     * @param outputStream
     * @param postParm
     * @param fileList
     * @throws IOException
     */
    private static void writeFileOutputStream(OutputStream outputStream, Map<String, String> postParm, List<UploadFileBean> fileList)
            throws IOException {

        if (fileList == null) {
            return;
        }

        DataOutputStream out = new DataOutputStream(outputStream);
        out.writeBytes(END);
        if (postParm != null) {
            for (Map.Entry<String, String> entry : postParm.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (name == null || name.length() == 0 || value == null || value.length() == 0) {
                    continue;
                }
                out.writeBytes(TWOHYPHENS + BOUNDARY + END);
                out.writeBytes("Content-Disposition: form-data; " + "name=\"" + name + "\"" + END + END);
                out.writeBytes(value);
                out.writeBytes(END);
            }
        }
        if (fileList != null) {
            for (UploadFileBean bean : fileList) {
                String filePath = bean.getFilePath();
                Map<String, String> headMap = bean.getHeadMap();
                String name = bean.getName();
                String fileName = FileUtil.getFileName(filePath);
                if (name == null) {
                    name = fileName;
                }
                out.writeBytes(TWOHYPHENS + BOUNDARY + END);
                out.writeBytes("Content-Disposition: form-data; " + "name=\"" + name + "\"; "
                        + "filename=\"" + fileName + "\"" + END);

                if (headMap != null) {
                    Iterator<String> it = headMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        String value = headMap.get(key);

                        out.writeBytes(key);
                        out.writeBytes(":");
                        out.writeBytes(value);
                        out.writeBytes(END);
                    }
                }
                out.writeBytes(END);

                BufferedInputStream fileInputStream = null;
                File file = new File(filePath);
                try {
                    InputStream inputStream = new FileInputStream(file);
                    fileInputStream = new BufferedInputStream(inputStream);
                    byte[] buffer = new byte[1024 * 1024];
                    int readLen = 0;
                    // ----------------
                    while ((readLen = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, readLen);
                    }
                    out.writeBytes(END);

                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }

            }
        }


        out.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + END);
        out.flush();

    }

}

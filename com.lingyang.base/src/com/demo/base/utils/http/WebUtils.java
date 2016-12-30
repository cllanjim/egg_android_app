package com.lingyang.base.utils.http;

import android.util.Base64;

import com.lingyang.base.utils.Base64Utils;
import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.MultiMemberGZIPInputStream;
import com.lingyang.base.utils.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class WebUtils {

    private static final String TAG = "WebUtils";

    public static final String DEFAULT_CHARSET = "utf-8";

    public static final String METHOD_POST = "POST";

    public static final String METHOD_GET = "GET";

    public static final int CONNECTION_TIMEOUT = 40 * 1000;

    public static final String MAP_KEY_RESULT = "result";

    public static final String GZIP_ENCODING = "gzip";// gzip的encode名称

    private static final boolean isPrintHeadInfo = false;

    public static String doPost(String url, Map<String, String> postParams,
                                Map<String, String> getParams, Map<String, String> httpHead) throws IOException {
        return doPost(url, postParams, getParams, httpHead, CONNECTION_TIMEOUT);
    }

    public static String doPost(String url, Map<String, String> postParams,
                                Map<String, String> getParams, Map<String, String> httpHead, int timeOut)
            throws IOException {

        String param = buildQuery(postParams, DEFAULT_CHARSET, false);
        return doPost(url, param, getParams, httpHead, timeOut);
    }

    public static String doPost(String url, Map<String, String> httpHead) throws IOException {

        return doPost(url, "", null, httpHead);
    }

    public static String doPost(String url, String postParams) throws IOException {

        return doPost(url, postParams, null, null);
    }

    public static String doPost(String url, String postParams, Map<String, String> getParams,
                                Map<String, String> httpHead) throws IOException {
        return doPost(url, postParams, getParams, httpHead, CONNECTION_TIMEOUT);
    }

    public static String doPost(String url, String postParams, Map<String, String> getParams,
                                Map<String, String> httpHead, int timeOut) throws IOException {

        Map<String, String> map = doPostConnect(url, postParams, getParams, httpHead, timeOut);
        if (map != null) {
            String result = map.get(MAP_KEY_RESULT);
            return result;
        }
        return null;
    }

    public static Map<String, String> doPostConnect(String url, String postParams,
                                                    Map<String, String> getParams, Map<String, String> httpHead, int timeOut)
            throws IOException {
        byte[] datas = null;
        if (postParams != null && !"".equals(postParams)) {
            datas = postParams.getBytes(DEFAULT_CHARSET);
        }
        return doPostConnect(url, datas, getParams, httpHead, timeOut, false);
    }

    public static Map<String, String> doPostConnect(String url, byte[] postParams,
                                                    Map<String, String> getParams, Map<String, String> httpHead, int timeOut,
                                                    boolean isParseResultBase64) throws IOException {

        if (url == null) {
            return null;
        }

        if (getParams != null && !"".equals(getParams)) {
            url = buildRequestUrl(url, getParams, true);
        }

        Log.i(TAG, "doPost:" + url);
        Log.i(TAG, "doPost - postParams:" + postParams);

        return connection(url, METHOD_POST, postParams, httpHead, timeOut, isParseResultBase64);
    }

    /**
     * doGet请求方法
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String doGet(String url) throws IOException {
        return doGet(url, null);
    }

    public static String doGet(String url, Map<String, String> params) throws IOException {
        return doGet(url, params, null);
    }

    public static String doGet(String url, Map<String, String> params, Map<String, String> httpHead)
            throws IOException {
        return doGet(url, params, httpHead, CONNECTION_TIMEOUT);
    }

    public static String doGet(String url, Map<String, String> params,
                               Map<String, String> httpHead, int timeout) throws IOException {
        Map<String, String> map = doGetConnect(url, params, httpHead, timeout, false);
        if (map != null) {
            String result = map.get(MAP_KEY_RESULT);
            return result;
        }
        return null;
    }

    /**
     * doGet请求方法
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static Map<String, String> doGetConnect(String url, Map<String, String> params,
                                                   Map<String, String> httpHead, int timeOut, boolean isParseResultBase64)
            throws IOException {

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        if (params != null) {
            url = buildRequestUrl(url, params, true);
        }

        Log.i(TAG, "doGet:" + url);

        return connection(url, METHOD_GET, null, httpHead, timeOut, isParseResultBase64);
    }

    /**
     * doPost请求方法
     *
     * @param url
     * @param ctype
     * @param content
     * @return
     * @throws IOException
     */
    private static Map<String, String> connection(String url, String method, byte[] postContent,
                                                  Map<String, String> httpHead, int timeOut, boolean isParseResultBase64)
            throws IOException {

        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream inputStream = null;
        try {
            conn = getConnection(new URL(url), method, httpHead, timeOut);

            if (method.equals(METHOD_POST) && postContent != null) {
                conn.setDoOutput(true);
                out = conn.getOutputStream();
                if (out != null) {
                    out.write(postContent);
                }
            }

            Map<String, String> resultMap = new HashMap<String, String>();

            int responseCode = conn.getResponseCode();
            if (!isErrorRequest(responseCode)) {
                String encoding = conn.getContentEncoding();
                Log.i(TAG, "connection - responseCode: " + responseCode);

                if (encoding != null && encoding.contains(GZIP_ENCODING)) {
                    inputStream = new MultiMemberGZIPInputStream(conn.getInputStream());
                    Log.v(TAG, "MultiMemberGZIPInputStream");
                } else {
                    inputStream = conn.getInputStream();
                }
            } else {
                inputStream = conn.getErrorStream();
            }

            if (!isParseResultBase64) {
                String charset = getResponseCharset(conn.getContentType());
                String rsp = getStreamAsString(inputStream, charset);
                resultMap.put(MAP_KEY_RESULT, rsp);

                Log.v(TAG, "结果(" + charset + ")：" + rsp);
            } else {
                byte[] rspbytes = getStreamAsBytes(inputStream);
                String base64 = Base64Utils.encode(rspbytes, Base64.NO_WRAP);
                resultMap.put(MAP_KEY_RESULT, base64);
            }
            getHttpHeadMap(conn, resultMap);
            //根据http响应码，正确请求返回resultMap，错误请求（>=400）抛出异常
            /*if (!isErrorRequest(responseCode)) {
                return resultMap;
            } else {
                HttpConnectionResultException cException = new HttpConnectionResultException(
                        responseCode);
                cException.resultMap = resultMap;
                throw cException;
            }*/

            //不根据http错误码来判断请求是否正确
            //根据返回的resultMap中的status_code判断请求是否正确 正确结果在data中 错误信息在error中
            return resultMap;
        } catch (IOException e) {
            Log.e(TAG, "connection - IOException:" + e.getLocalizedMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static HttpURLConnection getConnection(URL url, String method,
                                                  Map<String, String> httpHead) throws IOException {
        return getConnection(url, method, httpHead, CONNECTION_TIMEOUT);
    }

    /**
     * 得到Http连接
     *
     * @param url
     * @param method
     * @param ctype
     * @return
     * @throws IOException
     */
    public static HttpURLConnection getConnection(URL url, String method,
                                                  Map<String, String> httpHead, int timeout) throws IOException {
        if (timeout == -1) {
            timeout = CONNECTION_TIMEOUT;
        }
        HttpURLConnection conn = null;

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="
                + DEFAULT_CHARSET);
        conn.setRequestProperty("connection", "Keep-Alive");

        if (httpHead != null) {
            Iterator<String> it = httpHead.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = httpHead.get(key);

                if (key != null && value != null) {
                    conn.setRequestProperty(key, value);
                }

                Log.v(TAG, "Head:" + key + " - " + value);
            }
        }

        return conn;
    }

    public static String buildRequestUrl(String url, Map<String, String> params,
                                         boolean hasSeparator) {
        try {
            String query = buildQuery(params, DEFAULT_CHARSET, false);
            return url = buildURl(url, query, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [URL参数拼接]<br/>
     *
     * @param strUrl
     * @param query
     * @return
     * @throws MalformedURLException
     */
    public static String buildURl(String strUrl, String query, boolean hasSeparator)
            throws MalformedURLException {
        URL url = new URL(strUrl);
        if (StringUtils.isEmpty(query)) {
            return strUrl;
        }

        if (StringUtils.isEmpty(url.getQuery())) {
            if (strUrl.endsWith("?")) {
                strUrl = strUrl + query;
            } else {
                if (hasSeparator) {
                    if (strUrl.endsWith("/")) {
                        strUrl = strUrl + "?" + query;
                    } else {
                        strUrl = strUrl + "/?" + query;
                    }
                } else {
                    strUrl = strUrl + "?" + query;
                }
            }
        } else {
            if (strUrl.endsWith("&")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "&" + query;
            }
        }
        Log.v(TAG, "  ------------------> request url : " + strUrl);
        return strUrl;
    }

    /**
     * 根据参数Map来动态构造请求参数
     *
     * @param params
     * @param charset
     * @return
     * @throws IOException
     */
    public static String buildQuery(Map<String, String> params, String charset, boolean isEncoder)
            throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name == null || name.length() == 0 || value == null || value.length() == 0) {
                continue;
            }
            if (hasParam) {
                query.append("&");
            } else {
                hasParam = true;
            }

            query.append(name).append("=");
            if (isEncoder) {
                query.append(URLEncoder.encode(value, charset));
            } else {
                query.append(value);
            }
        }

        Log.v(TAG, "  ------------------> params list : " + query.toString());

        return query.toString();
    }

    /**
     * 将流转换成字符
     *
     * @param stream
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getStreamAsString(InputStream stream, String charset) throws IOException {

        String resultString = "";

        if (stream == null || charset == null) {
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();
            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            resultString = writer.toString();
            // Log.v(TAG, "返回的结果: "+ resultString);
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (stream != null) {

                stream.close();
            }
        }
    }

    public static byte[] getStreamAsBytes(InputStream stream) throws IOException {

        if (stream == null) {
            return null;
        }

        int num = -1;
        byte[] buf = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((num = stream.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, num);
        }
        byte[] b = baos.toByteArray();
        baos.flush();
        baos.close();
        return b;
    }

    /**
     * 得到返回信息的编码
     *
     * @param ctype
     * @return
     */
    public static String getResponseCharset(String ctype) {
        String charset = DEFAULT_CHARSET;

        if (!StringUtils.isEmpty(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!StringUtils.isEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    public static Map<String, String> getParamMap(String url) {

        Map<String, String> map = new HashMap<String, String>();

        int beginIndex = url.lastIndexOf("?") + 1;
        url = url.substring(beginIndex, url.length());

        String[] params = url.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] param = params[i].split("=");
            String key = param[0];
            String value = param[1];

            map.put(key, value);
        }

        return map;
    }

    public static void getHttpHeadMap(HttpURLConnection conn, Map<String, String> map) {
        Map<String, List<String>> headmap = conn.getHeaderFields();
        if (headmap == null) {
            return;
        }
        Iterator<Entry<String, List<String>>> it = headmap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, List<String>> entry = it.next();
            String headKey = entry.getKey();
            List<String> headValueList = entry.getValue();
            String headValue = null;
            if (headValueList != null && headValueList.size() > 0) {
                headValue = headValueList.get(0);
                map.put(headKey, headValue);
                if (isPrintHeadInfo) {
                    Log.v(TAG, "[HEAD]Key:" + headKey + " - Value:" + headValue);
                }
            }
        }
    }

    private static boolean isErrorRequest(int responseCode) {
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            return true;
        }
        return false;
    }
}

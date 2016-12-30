package com.lingyang.base.utils.http;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;

import com.lingyang.base.utils.ExceptionUtil;
import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.executor.TaskRunnable;
import com.lingyang.base.utils.executor.ThreadPoolManager;

/**
 * [Http网络请求管理器]
 * 网络以队列进行请求
 */
public class HttpConnectManager {

    public static final String TAG = "HttpConnectManager";

    /**
     * 网络请求连接池等待10秒
     */
    private final long CONNECTER_KEEP_ALIVE_TIME = 10 * 1000;

    /**
     * State包括以下错误码，也包括HTTP的返回错误码
     */

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

    private static HttpConnectManager instance;
    private static HttpConnectManager backgroundInstance;

    private LinkedList<Request> requestList;

    private Thread httpThread;

    private Context mContext;

    public final static int CONNECT_TYPE_SINGLE = 0;
    public final static int CONNECT_TYPE_MULTI = 1;
    private int mConnectType;

    private Integer mCurrentConnectCount = new Integer(0);

    private HttpConnectManager(Context context, int connectType) {
        if (context == null) {
            throw new RuntimeException("context不能为空");
        }

        mConnectType = connectType;
        requestList = new LinkedList<Request>();
        mContext = context;
        ThreadPoolManager.initThreadPoolManager(context);
    }

    /**
     * [后台数据请求队列管理器]<BR>
     * 单线程排队执行，在遇到后台非前台业务数据请求的时候可以使用该单线程进行排队，避免非即时性请求妨碍即时性请求队列的执行
     * 该单线程请求支持队列优先级处理，新的请求排队在队列最前面
     * 即时性请求请使用多线程处理请求：getInstance
     *
     * @param context
     * @return
     */
    public static HttpConnectManager getBackgroundInstance(Context context) {
        if (backgroundInstance == null) {
            backgroundInstance = new HttpConnectManager(context, CONNECT_TYPE_SINGLE);
        }
        return backgroundInstance;
    }

    /**
     * [常规数据请求队列管理器]<BR>
     * 使用线程池进行请求 ThreadPoolManager.TYPE_NORMAL
     * 即时性请求请使用此实例请求，非即时性请求使用 getBackgroundInstance() 的实例
     *
     * @param context
     * @return
     */
    public static HttpConnectManager getInstance(Context context) {
        if (instance == null) {
            instance = new HttpConnectManager(context, CONNECT_TYPE_MULTI);
        }
        return instance;
    }

    public static void release() {
        instance = null;
    }

    /**
     * [Get请求]<br/>
     *
     * @param request 请求参数
     */
    public boolean doGet(Request request) {
        if (request == null) {
            return false;
        }
        request.setHttpType(Request.HTTP_TYPE_GET);
        return connection(request);
    }

    /**
     * [Post请求]<br/>
     *
     * @param request 请求参数
     */
    public boolean doPost(Request request) {

        if (request == null) {
            return false;
        }
        return connection(request);
    }

    /**
     * [Post请求]<br/>
     *
     * @param request   请求参数
     * @param postParam Post参数
     */
    public boolean doPost(Request request, Map<String, String> postParam) {

        if (request == null) {
            return false;
        }
        request.setHttpType(Request.HTTP_TYPE_POST);
        request.setPostDataType(Request.DATA_MAP);
        request.setPostData(postParam);

        return connection(request);
    }

    /**
     * [Post请求]<br/>
     *
     * @param request   请求参数
     * @param postParam Post参数
     */
    public boolean doPost(Request request, String postParam) {

        if (request == null) {
            return false;
        }
        request.setHttpType(Request.HTTP_TYPE_POST);
        request.setPostDataType(Request.DATE_STRING);
        request.setPostData(postParam);

        return connection(request);
    }

    public boolean doPost(Request request, byte[] postParam) {

        if (request == null) {
            return false;
        }
        request.setHttpType(Request.HTTP_TYPE_POST);
        request.setPostDataType(Request.DATA_BYTES);
        request.setPostData(postParam);

        return connection(request);
    }

    /**
     * [添加网络请求到请求队列]<br/>
     *
     * @param request
     */
    private boolean connection(Request request) {

        if (request == null) {
            return false;
        }
        Context context = mContext;
        if (context == null) {
            Log.e(TAG, "connection - Context 为空");
            return false;
        }
        if (!NetWorkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "connection - 网络不可用");
            int state = STATE_NETWORD_UNSEARCHABLE;
            OnRequestListener listener = request.getOnRequestListener();
            if (listener != null) {
                listener.onResponse(request.getUrl(), state, null, request.getRequestType(), request, null);
            }
            return false;
        }

        if (mConnectType == CONNECT_TYPE_SINGLE) {
            synchronized (requestList) {
                requestList.addLast(request);
            }

            if (httpThread == null) {
                //启动连接管理器
                httpThread = new ConnectroSingleThread();
                httpThread.start();
            } else {
                awakeDownload();
            }
        } else if (mConnectType == CONNECT_TYPE_MULTI) {
            addThreadPollHttpCount();
            int threadType = request.getThreadType();
            ThreadPoolManager.getInstance(threadType).execute(new ConnectroRunnable(request));
        }
        return true;
    }

    /**
     * [唤醒下载线程]<br/>
     */
    public void awakeDownload() {
        //唤醒连接管理器
        if (httpThread == null) {
            return;
        }
        State state = null;
        try {
            state = httpThread.getState();
        } catch (Exception e) {
            state = state.BLOCKED;
            e.printStackTrace();
        }
        if (state == State.TIMED_WAITING || state == State.BLOCKED) {
            try {
                httpThread.interrupt();
            } catch (Exception e) {
            }
        }
    }

    public void clearRequest() {
        if (mConnectType == CONNECT_TYPE_SINGLE) {
            synchronized (requestList) {
                requestList.clear();
            }
        } else if (mConnectType == CONNECT_TYPE_MULTI) {
            ThreadPoolManager.getInstance(ThreadPoolManager.TYPE_NORMAL).clearTask();
        }
    }

    class ConnectroSingleThread extends Thread {

        public void run() {
            while (true) {
                if (requestList.isEmpty()) {
                    //线程等待处理
                    try {
                        Log.e(TAG, "数据连接池等待中.....");
                        Thread.sleep(CONNECTER_KEEP_ALIVE_TIME);
                    } catch (Exception e) {
                    }
                }

                Request request = null;
                synchronized (requestList) {
                    if (requestList.isEmpty()) {
                        Log.i(TAG, "关闭数据连接池");
                        httpThread = null;
                        break;
                    } else {
                        Log.i(TAG, "连接池Size：" + requestList.size());
                        request = requestList.removeFirst();
//						request = getRequest();
                    }
                }
                if (request == null) {
                    continue;
                }
                requestConnection(false, request);
            }
        }

        private Request getRequest() {
            Request request = null;
            Iterator<Request> it = requestList.iterator();
            while (it.hasNext()) {
                Request r = it.next();
                if (request == null) {
                    request = r;
                    continue;
                }
                if (r.getPriority() > request.getPriority()) {
                    request = r;
                }
            }
            requestList.remove(request);
            return request;
        }
    }

    /**
     * [网络请求线程]<br/>
     *
     * @author 李理
     * @date 2012-7-18
     */
    class ConnectroRunnable extends TaskRunnable {

        Request request;

        ConnectroRunnable(Request request) {
            this.request = request;
        }

        public void run() {
            requestConnection(true, request);
            reduceThreadPollHttpCount();
        }
    }

    private void requestConnection(boolean isFormThreadPool, Request request) {
        Object result = null;
        int state = STATE_SUC;
        String url = request.getUrl();
        Map<String, String> params = request.getUriParam();

        String requestUrl = url;
        long t1 = System.currentTimeMillis();
        try {
            String query = WebUtils.buildQuery(params, WebUtils.DEFAULT_CHARSET, true);
            requestUrl = WebUtils.buildURl(url, query, false);
            Log.v(TAG, "【Start】" + requestUrl);
        } catch (Exception e) {
        }

        String taskSize = null;
        if (isFormThreadPool) {
            int threadType = request.getThreadType();
            int activeCount = ThreadPoolManager.getInstance(threadType).getActiveCount();
            int queueSize = ThreadPoolManager.getInstance(threadType).getQueueSize();
            taskSize = "TaskSize:" + queueSize + " - ActiveCount:" + activeCount + " - threadType:" + threadType;
        } else {
            taskSize = "TaskSize:" + requestList.size() + " - ActiveCount:1";
        }
        Log.v(TAG, "任务队列 " + taskSize);

        Context context = mContext;
        if (context == null) {
            return;
        }
        if (!NetWorkUtils.isNetworkAvailable(context)) {
            Log.v(TAG, "网络不可用，抛弃请求");

            state = STATE_NETWORD_UNSEARCHABLE;
            OnRequestListener listener = request.getOnRequestListener();
            if (listener != null) {
                listener.onResponse(url, state, null, request.getRequestType(), request, null);
            }
            return;
        }

        try {
            int timeOut = request.getTimeout();
            Map<String, String> uriParam = request.getUriParam();
            Map<String, String> httpHead = request.getHttpHead();

            if (request.getHttpType() == Request.HTTP_TYPE_POST) {
                //进行Post请求
                Object postParam = request.getPostData();
                if (postParam != null) {
                    int dataType = request.getPostDataType();
                    switch (dataType) {
                        case Request.DATE_STRING:
                            result = WebUtils.doPostConnect(url, ((String) postParam).getBytes(), uriParam,
                                    httpHead, timeOut, request.isNeedResultBase64());
                            break;
                        case Request.DATA_MAP:
                            String param = WebUtils.buildQuery((Map<String, String>) postParam,
                                    WebUtils.DEFAULT_CHARSET, false);
                            result = WebUtils.doPostConnect(url, param.getBytes(), uriParam, httpHead,
                                    timeOut, request.isNeedResultBase64());
                            break;
                        case Request.DATA_BYTES:
                            result = WebUtils.doPostConnect(url, (byte[]) postParam, uriParam, httpHead,
                                    timeOut, request.isNeedResultBase64());
                            break;
                        default:
                            result = "error";
                            state = STATE_CALL_ERROR;
                            break;
                    }
                } else {
                    result = WebUtils.doPostConnect(url, "", uriParam, httpHead, timeOut);
                }
            } else {
                //进行Get请求
                result = WebUtils.doGetConnect(url, params, httpHead, timeOut, request.isNeedResultBase64());
            }
        } catch (HttpConnectionResultException e) {
            //Http Result不为200
            result = e.getMessage();
            state = Integer.parseInt(result.toString());

        } catch (IOException e) {
            result = "connection error";
            state = STATE_TIME_OUT;
            String eString = ExceptionUtil.getExceptionMsg(e);
            Log.e(TAG, eString);
        } catch (Exception e) {
            String eString = ExceptionUtil.getExceptionMsg(e);
            Log.e(TAG, eString);

            result = e.getMessage() + "";
            state = STATE_EXCEPTION;
        }

        if (result == null) {
            state = STATE_EXCEPTION;
        }
        Map<String, String> resultMap = null;
        if (state == STATE_SUC && result != null) {
            resultMap = (Map<String, String>) result;
            result = resultMap.get(WebUtils.MAP_KEY_RESULT);
        }

        long t2 = System.currentTimeMillis();
        long time = t2 - t1;
        request.setRequestTime(time);
        Log.i(TAG, "【End】time:" + ((time) / 1000.0f) + " - state:" + state + " - " + requestUrl);

        if (state == STATE_SUC && result != null) {
            IDataParser parser = request.getParser();
            if (parser != null) {
                result = parser.parseData(result.toString());
            }
        }

        OnRequestListener listener = request.getOnRequestListener();
        if (listener != null) {
            listener.onResponse(url, state, result, request.getRequestType(), request, resultMap);
        }
    }

    public void onDestroy() {

        if (requestList != null) {
            synchronized (requestList) {
                requestList.clear();
            }
        }
        awakeDownload();
        instance = null;
    }

    private void addThreadPollHttpCount() {
        synchronized (mCurrentConnectCount) {
            mCurrentConnectCount++;
        }
    }

    private void reduceThreadPollHttpCount() {
        synchronized (mCurrentConnectCount) {
            mCurrentConnectCount--;
        }
    }

    public int getConnectionCount() {
        int singleSize = requestList.size();
        int connectCount = mCurrentConnectCount + singleSize;
        return connectCount;
    }

}


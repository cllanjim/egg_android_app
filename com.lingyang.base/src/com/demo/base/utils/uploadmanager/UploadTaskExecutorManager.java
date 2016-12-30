package com.lingyang.base.utils.uploadmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutorTarget;
import com.lingyang.base.utils.downloadmanager.excutor.TaskQueue;
import com.lingyang.base.utils.executor.ThreadPoolManager;
import com.lingyang.base.utils.http.UploadRequest;
import com.lingyang.base.utils.http.UploadUtil;
import com.lingyang.base.utils.http.UploadRequest.UploadFileBean;
import com.lingyang.base.utils.uploadmanager.storage.UploadBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * TODO 上传任务管理 执行
 * 
 */
public class UploadTaskExecutorManager extends Thread {
    private static final String TAG = UploadTaskExecutorManager.class.getSimpleName();
    public static final int THREAD_POOL_TYPE = -999;
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_POOL_SIZE = 0;
    public static final int KEEP_ALIVE = 60 * 1000;
    public static final String THREAD_NAME = "Uploader";
    private Context context;
    private volatile TaskQueue<UploadExecutorTarget> queue;
    private boolean isRun;
    private ThreadPoolManager threadPoolManager;
    private Map<String, UploadExcutor> excutorMap = new HashMap<String, UploadExcutor>();
    private Map<String, UploadExecutorTarget> excutorTargetMap = new HashMap<String, UploadExecutorTarget>();

    public UploadTaskExecutorManager(Context context) {
        this.context = context;
        isRun = false;
        queue = new TaskQueue<UploadExecutorTarget>();
        threadPoolManager = ThreadPoolManager.getInstance(THREAD_POOL_TYPE);
        threadPoolManager.init(context);
        threadPoolManager.setMaxPoolSize(MAX_POOL_SIZE);
        threadPoolManager.setMinPoolSize(MIN_POOL_SIZE);
        threadPoolManager.setKeepAlive(KEEP_ALIVE);
        threadPoolManager.setThreadName(THREAD_NAME);
        threadPoolManager.initThreadPoll(THREAD_POOL_TYPE);
    }

    @Override
    public void start() {
        if (isRun) {
            return;
        }
        this.isRun = true;
        super.start();
    }

    @Override
    public void run() {
        UploadExecutorTarget target;
        while (isRun) {
            target = getTask();

            if (target == null) {
                continue;
            }
            upload(target);
        }
    }

    public UploadExecutorTarget getTask() {
        UploadExecutorTarget task = null;
        try {
            task = queue.take();
            if (task != null) {
                synchronized (task) {
                    if (task.isRun) {
                        task.state = UploadState.STATE_INTO_UPLOADING_QUEUE;
                        Log.i(TAG, "getTask:" + task.filePath + "task.state:" + task.state);
                        changeTargetStatus(task.uploadId, task.state);
                    }
                }
            }

        } catch (InterruptedException e) {
            Log.d(TAG, "getTask InterruptedException");
        }

        return task;
    }

    public void runTask(UploadExecutorTarget target) {
        Log.i(TAG, "runtask:"+target.uploadLen);
        synchronized (target) {
            UploadExcutor executor = excutorMap.get(target.uploadId);
            if (executor != null && target.isRun) {
                // 如果该任务正在运行
                return;
            }

            target.isRun = true;
            if (!queue.contains(target)) {
                // 回调 状态 STATE_PENDING
                queue.offer(target);
                target.state = UploadState.STATE_PENDING;
                Log.i(TAG, "runTask:" + target.filePath + "task.state:" + target.state);
                excutorTargetMap.put(target.uploadId, target);
                changeTargetStatus(target.uploadId, target.state);
            }
        }
        start();
    }

    class UploadResponseListener implements OnUploadRequestListener {

        @Override
        public void onUploadResponse(UploadResponse uploadResponse) {
        	Log.i(TAG, uploadResponse.state +"|" +uploadResponse.fileTotalSize + "|"+uploadResponse.fileUploadSize );
            String id = uploadResponse.fileList.get(0).getId();
            changeTargetStatus(id, uploadResponse.state, uploadResponse.fileTotalSize,
                    uploadResponse.fileUploadSize,uploadResponse.uploadRate);
        }
    }

    private void changeTargetStatus(String id, int state) {
        UploadExecutorTarget target = excutorTargetMap.get(id);
        if (target != null) {
            target.state = state;
            if (onUploadTaskLisetner != null) {
                onUploadTaskLisetner.onUploadTask(target);
            }
        } else {
            Log.i(TAG, "id:" + id + " task not exist");
        }
    }

    // 改变状态
    private void changeTargetStatus(String id, int state, long totalSize, long curSize,int uploadRate) {
        UploadExecutorTarget target = excutorTargetMap.get(id);
        if (target != null) {
            target.state = state;
            target.uploadLen = curSize;
            Log.i(TAG, "target.uploadLen:"+curSize);
            target.filelen = totalSize;
            target.uploadRate = uploadRate;

            if (state == UploadState.STATE_FAIL || state == UploadState.STATE_PAUSE
                    || state == UploadState.STATE_SUC) {
                target.isRun = false;
                excutorMap.remove(id);
                finishDownloadTask(target);
            }

            if (onUploadTaskLisetner != null) {
                onUploadTaskLisetner.onUploadTask(target);
            }
        }

    }

    public void upload(final UploadExecutorTarget target) {

        threadPoolManager.execute(new Runnable() {

            @Override
            public void run() {
                String seprator = "---------------------------";
                UploadRequest uploadRequest = new UploadRequest();
                List<UploadFileBean> list = new ArrayList<UploadFileBean>();
                UploadFileBean bean = new UploadFileBean();
                bean.setFilePath(target.filePath);
                bean.setId(target.uploadId);
                bean.setName(target.fileName);
                list.add(bean);
                uploadRequest.setUploadFileList(list);
                Map<String, String> httpHead = new HashMap<String, String>();
                httpHead.put("Content-Type", "multipart/form-data; boundary=" + seprator
                        + UploadUtil.BOUNDARY);
                httpHead.put(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36");
                httpHead.put("Accept-Language", "zh-CN,zh;q=0.8");
                uploadRequest.setHttpHead(httpHead);
                uploadRequest.setUrl("http://172.16.2.101:8090/uploadfile");
                uploadRequest.setMulti(true);
                uploadRequest.setOnUploadRequestListener(new UploadResponseListener());
                UploadExcutor excutor = new UploadExcutor(context, uploadRequest);
                excutor.setTwoHyphens(seprator + "--");
                excutorMap.put(target.uploadId, excutor);
                excutor.startUploadFile();
            }
        });

    }

    // 通过设置queue中的maxRunningSize 来控制同时有几个任务可以并行
    // 默认5
    public void setMaxRunningSize(int maxRunningSize) {
        queue.setMaxRunningSize(maxRunningSize);
    }

    public List<UploadExecutorTarget> removeAllTask() {
        List<UploadExecutorTarget> collection = queue.clearTask();
        return collection;
    }

    public boolean pauseTask(UploadExecutorTarget info) {
        return pauseTask(info, true);
    }

    public boolean pauseTask(UploadExecutorTarget info, boolean isNotify) {
        Log.i(TAG, "pause Task");
        if (info == null) {
            return false;
        }
        boolean isSuc = false;
        synchronized (info) {
            isSuc = queue.removeQueue(info);
            info.isRun = false;
            UploadExcutor excutor = excutorMap.get(info.uploadId);
            if (excutor != null) {
                // info.executor.setDownloadNotify(isNotify);
                excutor.pauseUpload();
                excutorMap.remove(info.uploadId);
            }
            Log.d(TAG, "pauseTask " + info.fileName + " - isSuc:" + isSuc + " - executor:"
                    + excutor);
            excutor = null;
        }
        // signalNetwork();
        return isSuc;
    }

    public boolean pauseAllTask() {
        List<UploadExecutorTarget> collection = queue.clearTask();
        if (collection == null) {
            return false;
        }

        Iterator<UploadExecutorTarget> it = collection.iterator();
        while (it.hasNext()) {
            UploadExecutorTarget target = it.next();
            synchronized (target) {
                target.isRun = false;
                UploadExcutor excutor = excutorMap.get(target.fileName);
                if (excutor != null) {
                    excutor.pauseUpload();
                    Log.v(TAG, "pauseAllTask url:" + target.fileName);
                    excutorMap.remove(target.fileName);
                }
                excutor = null;
            }
        }
        Log.d(TAG,
                "pauseAllTask queueHashCode:" + queue.hashCode() + " - queueSize:" + queue.size());
        return true;
    }

    /**
     * [暂停所有正在下载的任务但保持当前的队列状态]
     */
    private void pauseAllDownloadingHoldQueue() {
        Log.v(TAG, "pauseAllDownloadingHoldQueue");
        List<UploadExecutorTarget> list = queue.getRunningQueue();
        if (list == null || list.size() == 0) {
            return;
        }
        for (UploadExecutorTarget target : list) {
            synchronized (target) {
                UploadExcutor excutor = excutorMap.get(target.fileName);
                if (excutor != null) {
                    // target.executor.setDownloadNotify(false);
                    excutor.pauseUpload();
                    // Log.v(TAG, "pauseAllDownloadingHoldQueue url:" +
                    // target.url);
                }
                excutor = null;
            }
        }
    }

    public void deleteTaskById(String uploadId) {
        UploadExecutorTarget target = excutorTargetMap.get(uploadId);
        if (target != null) {
            deleteTask(target);
        }
    }

    public void deleteTask(UploadExecutorTarget target) {
        pauseTask(target);
        excutorTargetMap.remove(target.uploadId);
    }

    public int runAllTask() {
        Log.d(TAG, "runAllTask");
        boolean hasRunTask = false;
        Iterator<Entry<String, UploadExecutorTarget>> it = excutorTargetMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, UploadExecutorTarget> entry = it.next();
            UploadExecutorTarget info = entry.getValue();
            if (isTargetRun(info)) {
                continue;
            }
            runTask(info);
            hasRunTask = true;
        }
        return 0;
    }
    
    private boolean isTargetRun(UploadExecutorTarget info) {
        UploadExcutor executor = excutorMap.get(info.uploadId);
        if (executor != null && info.isRun && executor.isTaskRun()) {
            return true;
        }
        return false;   
    }

    /*
     * public void refreshDownloadTarget(UploadExecutorTarget target) { if
     * (target == null) { return; } DownloadExecutor executor = target.executor;
     * if (executor == null) { return; } target.fileLength =
     * executor.getTotalDownloadSize(); long currentDownloadSize =
     * executor.getCurrentDownloadSize(); if (currentDownloadSize != -1) {
     * target.downloadLength = currentDownloadSize; } }
     */
    public List<UploadExecutorTarget> queryAllTask() {
        List<UploadExecutorTarget> list = new ArrayList<UploadExecutorTarget>();
        Set<Entry<String, UploadExecutorTarget>> set = excutorTargetMap.entrySet();
        for (Entry<String, UploadExecutorTarget> entry : set) {
            UploadExecutorTarget target = entry.getValue();
            Log.i(TAG, "traget.uploadPath:"+target.uploadLen);
            list.add(target);
        }

        return list;
    }

    private void finishDownloadTask(UploadExecutorTarget params) {
        queue.removeRunningTask(params);
    }

    public boolean constainsPendingTask(UploadExecutorTarget info) {
        if (info == null) {
            return false;
        }
        UploadExecutorTarget executorInfo = queue.getPendingTask(info);
        if (executorInfo != null) {
            return true;
        }
        return false;
    }

    public List<UploadExecutorTarget> getPending() {
        return queue.getPendingQueue();
    }

    public List<UploadExecutorTarget> getDownloading() {
        return queue.getRunningQueue();
    }

    public int getPendingTaskCount() {
        return queue.getPendingSize();
    }

    public int getRunningTaskCount() {
        return queue.getRunningSize();
    }

    public int getMaxRunningSize() {
        return queue.getMaxRunningSize();
    }

    public boolean hashDownloadTask() {
        return !queue.isEmpty();
    }

    public int getAllTaskCount() {
        return excutorTargetMap.size();
    }

    public void release() {
        // 结束线程loop任务
        isRun = false;
        excutorTargetMap.clear();
    }

    class NetworkChangerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ThreadPoolManagerQuick.execute(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

    public interface OnUploadTaskLisetner {
        void onUploadTask(UploadExecutorTarget target);
    }

    private OnUploadTaskLisetner onUploadTaskLisetner;

    public void setOnUploadTaskLisetner(OnUploadTaskLisetner onUploadTaskLisetner) {
        this.onUploadTaskLisetner = onUploadTaskLisetner;
    }

    public OnUploadTaskLisetner getOnUploadTaskLisetner() {
        return onUploadTaskLisetner;
    }

}

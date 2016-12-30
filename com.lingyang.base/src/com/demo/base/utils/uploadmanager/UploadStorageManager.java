package com.lingyang.base.utils.uploadmanager;

import android.content.Context;
import android.text.TextUtils;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.downloadmanager.DownloadUtil;
import com.lingyang.base.utils.uploadmanager.UploadConfiguration.UploadTable;
import com.lingyang.base.utils.uploadmanager.UploadTaskExecutorManager.OnUploadTaskLisetner;
import com.lingyang.base.utils.uploadmanager.storage.IUploadTaskStorage;
import com.lingyang.base.utils.uploadmanager.storage.UploadBean;
import com.lingyang.base.utils.uploadmanager.storage.UploadTaskStorageFractory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO 封装db操作相关的类
 */
public class UploadStorageManager {
    private static final String TAG = UploadStorageManager.class.getSimpleName();
    private static Map<String, UploadStorageManager> mInstances = new HashMap<String, UploadStorageManager>();
    private Context context;
    private String instanceId;
    private IUploadTaskStorage uploadStorage;
    private UploadTable uploadTable = UploadConfiguration.UploadTable.UPLOAD_FILE_TABLE;
    private String storageName;
    private boolean isInit = false;
    private UploadTaskExecutorManager uploadTaskExecutorManager;
    private Map<String, UploadBean> uploadBeanMap = new HashMap<String, UploadBean>();
    private List<OnUploadTaskParamListener> taskParamsLisetners = new ArrayList<OnUploadTaskParamListener>();
    private ReentrantLock uploadLock = new ReentrantLock();
    private ReentrantLock mListenerLock = new ReentrantLock();
    private boolean isUseStorage = true;
    private int maxActivieTaskCount;

    private OnUploadTaskLisetner onUploadTaskLisetner = new OnUploadTaskLisetner() {
        @Override
        public void onUploadTask(UploadExecutorTarget target) {
            if (isUseStorage) {
                // 改变对应id的uploadBean的状态
                String uploadId = target.uploadId;
                UploadBean uploadBean = uploadBeanMap.get(uploadId);
                uploadBean.uploadState = target.state;
                uploadBean.uploadLen = target.uploadLen;
                
                // 数据库保存
                uploadStorage.updateTask(uploadBean);
            }
            // 回调外抛
            UploadTaskInfo taskInfo = parseUploadTaskInfo(target);
            for (OnUploadTaskParamListener onUploadTaskParamListener : taskParamsLisetners) {
                onUploadTaskParamListener.onUploadTaskParam(taskInfo);
            }
        }
    };

    private UploadStorageManager(Context context, String instanceId) {
        this.context = context;
        this.instanceId = instanceId;
        uploadTaskExecutorManager = new UploadTaskExecutorManager(context);
        uploadTaskExecutorManager.setOnUploadTaskLisetner(onUploadTaskLisetner);
    }

    public static UploadStorageManager getInstance(Context context, String instanceId) {
        synchronized (mInstances) {
            UploadStorageManager uploadStorageManager = mInstances.get(instanceId);
            if (uploadStorageManager == null) {
                uploadStorageManager = new UploadStorageManager(context, instanceId);
                mInstances.put(instanceId, uploadStorageManager);
            }
            return uploadStorageManager;
        }
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public boolean isUseStorage() {
        return isUseStorage;
    }

    public void setUseStorage(boolean isUseStorage) {
        this.isUseStorage = isUseStorage;
    }

    public int getMaxActivieTaskCount() {
        return maxActivieTaskCount;
    }

    public void setMaxActivieTaskCount(int maxActivieTaskCount) {
        this.maxActivieTaskCount = maxActivieTaskCount;
        if (uploadTaskExecutorManager != null) {
            uploadTaskExecutorManager.setMaxRunningSize(maxActivieTaskCount);
        }
    }

    public synchronized void init() {
        if (isInit) {
            return;
        }
        isInit = true;
        if (isUseStorage) {
            if (uploadStorage == null) {
                uploadStorage = UploadTaskStorageFractory.createUploadStorage(context,
                        UploadTaskStorageFractory.STORAGE_TYPE_DB_FILE_MANAGER);
            }

            try {
                if (TextUtils.isEmpty(storageName)) {
                    storageName = uploadTable.name();
                }
                uploadStorage.init(context, storageName);
                if (uploadStorage != null) {
                    List<UploadBean> uploadBeans = uploadStorage
                            .queryExcludeStateAllTask(UploadState.STATE_SUC);
                    if (uploadBeans != null && uploadBeans.size() > 0) {
                        for (UploadBean uploadBean : uploadBeans) {
                            // DownloadExecutorTarget target =
                            // parseDownloadExecutorTarget(uploadBean);
                            // mDownloadFiles.put(target.downloadId, target);

                            uploadBeanMap.put(uploadBean.uploadId, uploadBean);
                            // uploadBeanList.add(uploadBean);
                            Log.v(TAG, "init " + uploadBean.uploadState + " - name:"
                                    + uploadBean.fileName + "- path:" + uploadBean.filePath);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                isInit = false;
                Log.e(TAG, "init 初始化出错");
            }
        }

    }

    public int createTask(String uploadId, UploadTaskParam param, boolean autoRun) {
        // 查询内存中的任务，是否已存在相同任务
        long createTime = new Date().getTime();
        UploadBean uploadBean = parseUploadBean(param, createTime, uploadId);
        int createTaskResult = KeyConstants.CREATE_TASK_SUC;
        try {
            uploadLock.lock();
            if (isUseStorage) {
                if (!uploadBeanMap.containsKey(uploadId)) {
                    uploadBeanMap.put(uploadId, uploadBean);
                    if (uploadStorage != null) {
                        boolean isSuc = uploadStorage.addTask(uploadBean);
                        if (!isSuc) {
                            Log.e(TAG, "createTask 失败：存储任务到本地");
                        }
                    }
                } else {
                    createTaskResult = KeyConstants.CREATE_TASK_ERROR_EXIST;
                }
            }
        } finally {
            uploadLock.unlock();
        }

        if (autoRun) {
            // 开始进入下载队列准备下载
            // uploadTaskExecutorManager.runTask(parseUploadExecutorTarget(uploadBean));
            // notifyDownloadState(target, target.state.downloadState);
            // checkAutoUpdateDownloading();
            runTask(uploadId);
        }
        // requestNotifyDownlaodListState();
        return createTaskResult;
    }

    public void runTask(String uploadId) {
        uploadLock.lock();
        try {
            UploadBean uploadBean = uploadBeanMap.get(uploadId);
            if (uploadBean == null) {
                Log.i(TAG, uploadId + " task not create");
                return;
            }
            if (!isUploadFileExists(uploadBean)) {
                Log.i(TAG, uploadBean.filePath + "path not exists");
                return;
            }
            if (isUploadComplete(uploadBean)) {
                Log.i(TAG, uploadId + " upload complete!!!");
                return;
            }
            Log.i(TAG, "run task" + uploadId + "upladlength:"+uploadBean.uploadLen);
           
            UploadExecutorTarget target = parseUploadExecutorTarget(uploadBean);
            uploadTaskExecutorManager.runTask(target);

        } finally {
            uploadLock.unlock();
        }
    }

    public boolean pauseTask(String uploadId) {
        uploadLock.lock();
        try {
            UploadBean uploadBean = uploadBeanMap.get(uploadId);
            if (uploadBean == null) {
                Log.i(TAG, uploadId + " task not create");
                return false;
            }
            if (!isUploadFileExists(uploadBean)) {
                Log.i(TAG, uploadBean.filePath + "path not exists");
                return false;
            }
            if (isUploadComplete(uploadBean)) {
                Log.i(TAG, uploadId + " upload complete!!!");
                return false;
            }

            Log.i(TAG, "pause upload id:" + uploadId);
            UploadExecutorTarget target = parseUploadExecutorTarget(uploadBean);
            uploadTaskExecutorManager.pauseTask(target);

        } finally {
            uploadLock.unlock();
        }
        
        return true;
    }

    private boolean isUploadFileExists(UploadBean bean) {
        return new File(bean.filePath).exists();
    }

    private boolean isUploadComplete(UploadBean bean) {
        if (bean.filelen > 0 && bean.filelen == bean.uploadLen) {
            return true;
        }
        return false;
    }

    /**
     * TODO 查询内存中的UploadBean
     * 
     * @throw
     * @return List<UploadTaskInfo>
     */
    public List<UploadTaskInfo> queryAllStorageTask() {
        uploadLock.lock();
        List<UploadTaskInfo> list = new ArrayList<UploadTaskInfo>();
        try {
            Set<Entry<String, UploadBean>> set = uploadBeanMap.entrySet();
            for (Entry<String, UploadBean> entry : set) {
                UploadBean uploadBean = entry.getValue();
                list.add(parseUploadTaskInfo(uploadBean));
            }
        } finally {
            uploadLock.unlock();
        }
        return list;

    }

    public List<UploadTaskInfo> queryAllTask() {
        List<UploadExecutorTarget> targetList = uploadTaskExecutorManager.queryAllTask();
        List<UploadTaskInfo> list = new ArrayList<UploadTaskInfo>();
        for (UploadExecutorTarget target : targetList) {
            Log.i(TAG, "traget.uploadPath:"+target.uploadLen);
            UploadTaskInfo uploadTaskInfo =
                    parseUploadTaskInfo(target);
            list.add(uploadTaskInfo);
        }
        return list;
    }
    
    
    public void runAllTask() {
        uploadTaskExecutorManager.runAllTask();
    }
    
    public UploadTaskInfo queryTask(String uploadId) {
        UploadBean uploadBean = uploadBeanMap.get(uploadId);
        return parseUploadTaskInfo(uploadBean);
    }

    private UploadBean parseUploadBean(UploadTaskParam param,
            long createTime, String uploadId) {
        UploadBean uploadBean = new UploadBean();
        uploadBean.url = param.uri;
        uploadBean.postParam = param.postParam;
        uploadBean.filePath = param.filePath;
        uploadBean.fileName = param.fileName;
        uploadBean.uploadId = uploadId;
        uploadBean.createTime = createTime;
        uploadBean.owner = param.onwer;
        uploadBean.isPriv = param.isPrivate ? 1 : 0;
        // uploadBean.readTimeout = param.readTimeout != -1 ? param.readTimeout
        // : mReadTimeout;
        // uploadBean.connectionTimeout = param.connectionTimeout != -1 ?
        // param.connectionTimeout : mConnectionTimeout;
        // uploadBean.retryCount = param.retryCount != -1 ? param.retryCount :
        // mRetryCount;
        uploadBean.httpHead = DownloadUtil.parseMapToString(param.httpHead);
        uploadBean.fileType = param.fileType;
        uploadBean.fileVersion = param.fileVersion;
        uploadBean.uploadId = uploadId;
        File file = new File(param.filePath);
        if (file.exists()) {
            uploadBean.filelen = file.length();
        }
        return uploadBean;
    }

    private UploadExecutorTarget parseUploadExecutorTarget(UploadBean uploadBean) {
        UploadExecutorTarget target = new UploadExecutorTarget();
        target.url = uploadBean.url;
        target.httpMethod = uploadBean.httpMethod;
        target.postParam = uploadBean.postParam;
        target.filePath = uploadBean.filePath;
        target.filelen = uploadBean.filelen;
        target.uploadLen = uploadBean.uploadLen;
        // target.notAcceptTypes = mNotAcceptTypeList;
        target.uploadId = uploadBean.uploadId;
        target.createTime = uploadBean.createTime;
        target.owner = uploadBean.owner;
        target.isPriv = uploadBean.isPriv;
        // target.readTimeout = uploadBean.readTimeout != -1 ?
        // uploadBean.readTimeout : mReadTimeout;
        // target.connectTimeout = uploadBean.connectionTimeout != -1 ?
        // uploadBean.connectionTimeout : mConnectionTimeout;
        // target.retryCount = uploadBean.retryCount != -1 ?
        // uploadBean.retryCount : mRetryCount;
        // target.httpHead = DownloadUtil.parseStringToMap(uploadBean.httpHead);
        target.fileType = uploadBean.fileType;
        target.fileVersion = uploadBean.fileVersion;
        target.fileName = uploadBean.fileName;
        return target;
    }

    private UploadTaskInfo parseUploadTaskInfo(UploadExecutorTarget target) {
        UploadTaskInfo taskInfo = new UploadTaskInfo();
        taskInfo.uri = target.url;
        taskInfo.postParam = target.postParam;
        taskInfo.filelen = target.filelen;
        taskInfo.fileName = target.fileName;
        taskInfo.createTime = target.createTime;
        taskInfo.uploadId = target.uploadId;
        taskInfo.uploadLen = target.uploadLen;
        taskInfo.fileVersion = target.fileVersion;
        taskInfo.fileType = target.fileType;
        taskInfo.state = target.state;
        taskInfo.filePath = target.filePath;
        taskInfo.uploadRate = target.uploadRate;
        return taskInfo;
    }

    private UploadTaskInfo parseUploadTaskInfo(UploadBean target) {
        UploadTaskInfo taskInfo = new UploadTaskInfo();
        taskInfo.uri = target.url;
        taskInfo.postParam = target.postParam;
        taskInfo.filelen = target.filelen;
        taskInfo.fileName = target.fileName;
        taskInfo.createTime = target.createTime;
        taskInfo.uploadId = target.uploadId;
        taskInfo.uploadLen = target.uploadLen;
        taskInfo.fileVersion = target.fileVersion;
        taskInfo.fileType = target.fileType;
        taskInfo.state = target.uploadState;
        taskInfo.filePath = target.filePath;
        return taskInfo;
    }

    public void release() {
        isInit = false;
        uploadTaskExecutorManager.release();
        uploadBeanMap.clear();
        mListenerLock.lock();
        try {
            if (taskParamsLisetners != null) {
                taskParamsLisetners.clear();
            }
        } finally {
            mListenerLock.unlock();
        }

        pauseAll();

        synchronized (mInstances) {
            mInstances.remove(instanceId);
        }

        Log.v(TAG, "release");
    }

    public void pauseAll() {
        uploadTaskExecutorManager.pauseAllTask();
    }

    public boolean deleteTaskById(String uploadId) {
        if (uploadId == null) {
            return false;
        }
        uploadLock.lock();
        try {
            UploadBean uploadBean = uploadBeanMap.get(uploadId);
            if (uploadBean == null) {
                Log.i(TAG, uploadId + " task not create");
                return false;
            }
            UploadExecutorTarget target = parseUploadExecutorTarget(uploadBean);
            uploadTaskExecutorManager.deleteTask(target);
            if (uploadStorage != null) {
                // delete 数据存储
                uploadStorage.deleteTask(uploadId);
            }
            uploadBeanMap.remove(uploadId);
        } finally {
            uploadLock.unlock();
        }

        return true;
    }

    /*
     * @Override public boolean deleteTask(String url, boolean deleteFile) {
     * CompareCondition compareCondition = new CompareCondition<String>() {
     * @Override public boolean isSameCondition(String url,
     * DownloadExecutorTarget target) { if (url.equals(target.url)) { return
     * true; } return false; } }; return deleteTask(url, deleteFile,
     * compareCondition); }
     * @Override public boolean deleteTaskByType(int type, boolean deleteFile) {
     * CompareCondition compareCondition = new CompareCondition<Integer>() {
     * @Override public boolean isSameCondition(Integer type,
     * DownloadExecutorTarget target) { if (target.fileType == type) { return
     * true; } return false; } }; return deleteTask(type, deleteFile,
     * compareCondition); } public boolean deleteTask(Object value, boolean
     * deleteFile, CompareCondition compareCondition) {
     * List<DownloadExecutorTarget> deleteTargets = null; mDownloadLock.lock();
     * try { Iterator<Entry<String, DownloadExecutorTarget>> it =
     * mDownloadFiles.entrySet().iterator(); while(it.hasNext()) { Entry<String,
     * DownloadExecutorTarget> entry = it.next(); DownloadExecutorTarget info =
     * entry.getValue(); if (! compareCondition.isSameCondition(value, info)) {
     * continue; } it.remove(); if (deleteTargets == null) { deleteTargets = new
     * ArrayList<DownloadExecutorTarget>(); } deleteTargets.add(info); } if
     * (deleteTargets == null) { return false; } for(DownloadExecutorTarget
     * target : deleteTargets) { //移除下载队列 mDownloader.pauseTask(target); if
     * (mIsUseStorage && mDownloadStorage != null) {
     * mDownloadStorage.deleteTask(target.downloadId); } //flag
     * 可能造成正在写入文件的时候删除文件情况，是否存在问题 deleteTaskFile(target, deleteFile); } }finally
     * { mDownloadLock.unlock(); } requestNotifyDownlaodListState(); return
     * true; }
     * @Override public boolean deleteAllTask(boolean deleteFile) {
     * List<DownloadExecutorTarget> deleteTargets = new
     * ArrayList<DownloadExecutorTarget>(); mDownloadLock.lock(); try {
     * Iterator<Entry<String, DownloadExecutorTarget>> it =
     * mDownloadFiles.entrySet().iterator(); while(it.hasNext()) { Entry<String,
     * DownloadExecutorTarget> entry = it.next(); DownloadExecutorTarget info =
     * entry.getValue(); deleteTargets.add(info); } mDownloadFiles.clear(); if
     * (deleteTargets.size() == 0) { return false; } for(DownloadExecutorTarget
     * target : deleteTargets) { mDownloader.pauseTask(target); if
     * (mIsUseStorage && mDownloadStorage != null) { //delete 数据库
     * mDownloadStorage.deleteTask(target.downloadId); } //flag
     * 可能造成正在写入文件的时候删除文件情况，是否存在问题 deleteTaskFile(target, deleteFile); } }
     * finally { mDownloadLock.unlock(); } requestNotifyDownlaodListState();
     * return true; }
     */
    public int getAllStorageTaskCount() {
        return uploadBeanMap.size();
    }

    public int getAllTaskCount() {
        return uploadTaskExecutorManager.getAllTaskCount();
    }

    public int getRunningTaskCount() {
        return uploadTaskExecutorManager.getRunningTaskCount();
    }

    public int getPendingTaskCount() {
        return uploadTaskExecutorManager.getPendingTaskCount();
    }

    public boolean hasTasks() {
        return uploadTaskExecutorManager.hashDownloadTask();
    }

    public interface OnUploadTaskParamListener {
        void onUploadTaskParam(UploadTaskInfo param);
    }

    private OnUploadTaskParamListener onUploadTaskParamListener;

    public void addOnUploadTaskParamListener(OnUploadTaskParamListener onUploadTaskParamListener) {
        taskParamsLisetners.add(onUploadTaskParamListener);
    }

    public void removeOnUploadTaskParamListener(OnUploadTaskParamListener onUploadTaskParamListener) {
        taskParamsLisetners.remove(onUploadTaskParamListener);
    }

    public OnUploadTaskParamListener getOnUploadTaskParamListener() {
        return onUploadTaskParamListener;
    }
}

package com.lingyang.base.utils.downloadmanager;

import java.util.List;

import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;

/**
 * 基础的下载服务接口
 * 该下载服务建议不要为Android的Service，只作为服务工具
 */
public interface IBaseDownloadService {
	
	void init();
	
	/**
	 * 
	 * 创建网络任务
	 * 
	 * @param downloadId
	 * @param param
	 * @return
	 * 		0 成功
	 * 		-1 操作失败，未知错误
	 * 		-2 任务已存在
	 * 		-3 磁盘空间不足
	 */
	int createTask(String downloadId,int flag, DownloadTaskParam param, boolean autoRun);
	
	/**
	 * 查询下载指定的任务数据
	 * 
	 * @return
	 */
	DownloadTaskInfo queryTask(String downloadId);
	
	/**
	 * 查询所有的下载任务数据
	 * 
	 * @param owner
	 * @return
	 */
	List<DownloadTaskInfo> queryAllTask(String owner);
	
	/**
	 * 获取等待下载的任务
	 * 
	 * @return
	 */
	List<DownloadTaskInfo> getPending();
	
	/**
	 * 获取正在下载的任务
	 * 
	 * @return
	 */
	List<DownloadTaskInfo> getDownloading();
	
	/**
	 * 获取等待的任务总数
	 * 
	 * @return
	 */
	int getPendingTaskCount();
	
	/**
	 * 获取正在下载的任务的总数
	 *
	 * @return
	 */
	int getRunningTaskCount();
	
	/**
	 * 激活并开始下载所有指定url的网络任务
	 * 
	 * @param url
	 * @return
	 * 		0 操作成功
	 * 		-1 操作失败，未知错误
	 * 		-2 任务不存在
	 * 		-3 磁盘空间不足
	 * 		其他自定义错误
	 */
	int runTask(String url, boolean needJumpQueue);
	
	/**
	 * 开启所有的下载任务
	 * 
	 * @return
	 */
	int runAllTask();
	
	/**
	 * 暂停所有指定url的网络任务下载
	 * 
	 * @param url
	 * @return
	 * 		0 操作成功
	 * 		-1 操作失败，未知错误
	 * 		-2 任务不存在
	 * 		其他自定义错误
	 */
	int pauseTask(String url);
	
	/**
	 * 暂停所有的网络任务的下载
	 */
	void pauseAll();
	
	/**
	 * 暂停指定的网络任务下载
	 * 
	 * @param downloadId
	 * @return
	 */
	int pauseTaskById(String downloadId);
	
	/**
	 * 激活并开始下载指定的网络任务
	 * 
	 * @param downloadId
	 * @return
	 */
	int runTaskById(String downloadId, boolean needJumpQueue);
	
	/**
	 * 删除指定的网络任务
	 * 
	 * @param downloadId
	 * @param deleteFile 是否删除已下载完的文件
	 * @return
	 */
	boolean deleteTaskById(String downloadId, boolean deleteFile);
	
	/**
	 * 删除所有指定url的网络任务
	 * 
	 * @param url
	 * @param deleteFile 是否删除已下载完的文件
	 * @return
	 */
	boolean deleteTask(String url, boolean deleteFile);
	
	/**
	 * 删除所有的网络任务
	 * 
	 * @param deleteFile 是否删除已下载完的文件
	 * @return
	 */
	boolean deleteAllTask(boolean deleteFile);

	/**
	 * 设置最大同时可下载数
	 * 
	 * @param count 最大可下载数
	 */
	boolean setMaxActiveTaskCount(int count);
	
	int getMaxActiveTaskCount();
	
	/**
	 * 设置单个任务的下载线程数
	 * 
	 * @return
	 */
	boolean setDownloadThreadCount(int count);
	
	/**
	 * 设置最大下载速度
	 *
	 * @param speed 下载速度，单位KB/s
	 */
	boolean setRateLimit(int speed);
	
	/**
	 * 是否有网络任务存在
	 * 
	 * @return
	 */
	boolean hasTasks();
	
	/**
	 * 停止下载并释放内存、CPU资源
	 * 该方法不会记录保存当前下载状态，如果需要保存状态需要调用 
	 * {@link #pauseAll()} 或 {@link #recordAllTaskState()}}
	 * @param stopTask 是否停止下载
	 */
	void release(boolean stopTask);
	
	/**
	 * [记录当前任务的状态]<BR>
	 */
	void recordAllTaskState();
	
	/**
	 * [恢复记录的下载状态]<BR>
	 * 如果正在下载，则恢复下载，否则忽略
	 */
	void resumeAllTaskState();
	
	/**
	 * 
	 * [对所有指定状态的任务开启下载]<BR>
	 * 
	 * @param state 
	 * {@link com.lingyang.base.utils.downloadmanager.excutor.DownloadState#STATE_FAIL}
	 * {@link com.qvod.player.utils.downloadmanager.excutor.DownloadState#STATE_*}
	 */
	void runAllStateTask(int state);
	
	boolean addDownloadListListener(IDownloadListListener listener);
	
	boolean removeDownloadListListener(IDownloadListListener listener);
	
	boolean addDownloadListener(IDownloadListener listener);
	
	boolean removeDownloadListener(IDownloadListener listener);
	
	void setNotAcceptTypeList(List<String> list);
	
	/**
	 * [根据文件类型查询任务]<BR>
	 * @param type
	 * @return
	 */
	List<DownloadTaskInfo> queryTaskByType(int type);
	
	/**
	 * 查询所有已完成任务
	 * @return
	 */
	List<DownloadTaskInfo> queryAllComplete();
	
	/**
	 * [根据文件类型下载任务]<BR>
	 * @param type
	 * @return
	 */
	int runTaskByType(int type, boolean needJumpQueue);
	
	/**
	 * [根据文件类型暂停任务]<BR>
	 * @param type
	 * @return
	 */
	int pauseTaskByType(int type);
	
	/**
	 * [根据文件类型删除任务]<BR>
	 * 
	 * @param type
	 * @param deleteFile
	 * @return
	 */
	boolean deleteTaskByType(int type, boolean deleteFile);
	
	/**
	 * [强行插入一个任务到下载中的任务]<BR>
	 * 如果下载中的队列已满，会将其中一个下载中的任务暂停并且排到队列末尾，该任务则进入下载队列进行下载
	 * 
	 * @param downloadId
	 * @return
	 */
	int insertRunTaskById(String downloadId);

	int getAllTaskCount();
	
	void pauseAllAndRecordTask();
}




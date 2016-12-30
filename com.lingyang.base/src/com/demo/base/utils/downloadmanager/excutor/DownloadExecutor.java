package com.lingyang.base.utils.downloadmanager.excutor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.StringUtils;
import com.lingyang.base.utils.downloadmanager.DownloadUtil;
import com.lingyang.base.utils.downloadmanager.KeyConstants;

/**
 * [单文件下载任务]<br/>
 */
public class DownloadExecutor {
	
	private final static String TAG = "DownloadExecutor";
	
	public final static int RESULT_SUC = 0;
	public final static int RESULT_NETWORK_ERROR = KeyConstants.ERROR_REASON_NETWORK_ERROR;
	public final static int RESULT_NETWORK_TIME_OUT_ERROR = KeyConstants.ERROR_REASON_NETWORK_TIME_OUT_ERROR;
	public final static int RESULT_SDCARD_NOT_MOUNTED_ERROR = KeyConstants.ERROR_REASON_SDCARD_NOT_MOUNTED_ERROR;
	public final static int RESULT_NOT_ENOUGH_SPACE_ERROR = KeyConstants.ERROR_REASON_NOT_ENOUGH_SPACE_ERROR;
	public final static int RESULT_NOT_SUPPORT_CONTENT_TYPE_ERROR = KeyConstants.ERROR_REASON_NOT_SUPPORT_CONTENT_TYPE_ERROR;
	public final static int RESULT_HTTP_DOWNLOAD_LENGTH_ERROR = KeyConstants.ERROR_REASON_HTTP_DOWNLOAD_LENGTH_ERROR;
	public final static int RESULT_OTHER_ERROR = KeyConstants.ERROR_REASON_OTHER_ERROR;
	public final static int RESULT_PAUSE = KeyConstants.ERROR_REASON_PAUSE;
	public final static int RESULT_DOWNLOADING = KeyConstants.ERROR_REASON_DOWNLOADING;
	
	private static int DOWNLOAD_BUFFER_SIZE = 1024;
	
	private static long NOTIFY_PROGRESS_INTERVAL_TIME = 1 * 1000;
	
	/**
	 * 是否答应头信息
	 */
	private static boolean IS_PRINT_HEAD_INFO = false;
	
	private DownloadExecutorTarget downloadOptions;
	
	private boolean isRun = true;
	private boolean isNotify = true;
	private long currentDownloadSize = -1;
	private long totalDownloadSize = 0;
	private int currentDownloadState = DownloadState.STATE_NONE;
	private int currentDownloadRate;
	private boolean isDownloading = false;
	/**
	 * 下载失败时的已重试过的次数
	 */
	private int currentRetryCount = 0;
	
	/**
	 * 超时重试下载时候的重试间隔时间
	 */
	private long retryIntervals[];
	
	private IDownloadListener listener;
	
	private RandomAccessFile writeAccessFile;
	
	/**
	 * 是否下发下载进度的通知
	 * 针对于listener的处理
	 */
	private boolean isNotifyDownloadingListener = true;
	
	private Thread mCurrentThread;
	
	public DownloadExecutor(DownloadExecutorTarget downloadOptions) {
		this.downloadOptions = downloadOptions;
	}
	
	public int startDownload() {
		return startDownload(null);
	}
	
	public int startDownload(Map<String, String> resultMap) {
		try {
			if (isDownloading) {
				Log.w(TAG, "startDownload 正在下载中");
				return RESULT_DOWNLOADING;
			}
			
			isDownloading = true;
			if (downloadOptions == null) {
	//			throws new RuntimeException("");
				Log.e(TAG, "需要设置下载参数");
				return RESULT_OTHER_ERROR;
			}
			Log.w(TAG, hashCode() + "->> startDownload 开始下载");
			int retryCount = downloadOptions.retryCount;
			int ret = 0;
			if (isTaskRun()) {
				do {
					Log.v(TAG, "开始下载任务： " + downloadOptions.url);
					ret = download(resultMap);
					if (! isTaskRun()) {
						break;
					}
					//超时重试
					if (ret == RESULT_NETWORK_TIME_OUT_ERROR
							|| ret == RESULT_NETWORK_ERROR) {
						if (retryCount != DownloadExecutorTarget.RETRY_REPEAT && 
								currentRetryCount >= retryCount) {
							//超出重试次数
							break;
						}
						if (isDownloadFinish()) {
							break;
						}
						int retryIndex = currentRetryCount;
						currentRetryCount++;
						Log.v(TAG, "下载网络异常， 下载重试，retryIndex: " + retryIndex);
						if (retryIntervals != null && retryIntervals.length > retryIndex) {
							long intervalTime = retryIntervals[retryIndex];
							if (isTaskRun()) {
								try {
									Log.v(TAG, "下载网络异常， 下载重试，睡眠: " + intervalTime);
									synchronized (retryIntervals) {
										retryIntervals.wait(intervalTime);
									}
//									Thread.sleep(intervalTime);
								} catch(Exception e) {
									e.printStackTrace();
								}
								Log.v(TAG, "下载重试，睡眠结束：" + intervalTime);
							}
						}
					} else {
						break;
					}
					
				}while(isRun);
			}
			
			Log.v(TAG, "结束下载任务： " + downloadOptions.url + " - isRun:" + isRun + " - ret:" + ret);
			
			if (isDownloadFinish()) {
				notifyListener(DownloadState.STATE_SUC, 0);
			} else {
				if (! isTaskRun()) {
					notifyListener(DownloadState.STATE_PAUSE, 0);
				} else {
					notifyListener(DownloadState.STATE_FAIL, ret);
				}
			}
			Log.v(TAG, this.downloadOptions.url + "->> startDownload 下载结束 isRun:" + isRun);
			isDownloading = false; 
			return ret;
		}catch(Exception e) {
			Log.v(TAG, "startDownload 下载异常");
			e.printStackTrace();
			
			notifyListener(DownloadState.STATE_FAIL, RESULT_NETWORK_ERROR);
			return RESULT_NETWORK_ERROR;
		}
	}
	
	private int download(Map<String, String> resultMap) {
		HttpURLConnection conn = null;
		OutputStream out = null;
		
		String url = downloadOptions.url;
		String method = downloadOptions.method;
		Map<String, String> httpHead = downloadOptions.httpHead;
		int connectTimeout = downloadOptions.connectTimeout;
		int readTimeout = downloadOptions.readTimeout;
		this.currentDownloadSize  = downloadOptions.downloadLength;
		this.totalDownloadSize = downloadOptions.fileLength;
		byte[] postContent = downloadOptions.postContent;
		String savePath = downloadOptions.savePath;
		List<String> notAcceptTypes = downloadOptions.notAcceptTypes;
		if (postContent == null && downloadOptions.postParam != null) {
			postContent = downloadOptions.postParam.getBytes();
		}
		
		String tempFilePath = savePath+SUFFIX_TEMP;
		File file = new File(tempFilePath);
		if (!file.getParentFile().exists()) {
			boolean suc = file.getParentFile().mkdirs();
			if (! suc) {
				//sdcard未挂载
				Log.w(TAG, "download sdcard未挂载");
				return RESULT_SDCARD_NOT_MOUNTED_ERROR;
			}
		}
		
		Log.v(TAG, "download 准备下载 - currentDownloadSize:" + currentDownloadSize  + " - url:" + url);
		notifyListener(DownloadState.STATE_PREPARE, 0);
		
		try 
		{
			if (! isTaskRun()) {
				Log.v(TAG, "download 准备下载 - 任务被暂停 currentDownloadSize:" + currentDownloadSize);
				return RESULT_PAUSE;
			}
			
			if (file.exists()) {
				if (currentDownloadSize == 0 || file.length() > totalDownloadSize
						|| (file.length() == 0 && currentDownloadSize > 0)) {
//					Log.v(TAG, "文件长度异常， 重新下载文件 currentDownloadSize:" + currentDownloadSize
//							+ " - totalDownloadSize:" + totalDownloadSize + " - fileLen:" + file.length());
					currentDownloadSize = 0;
					file.delete();
					file.createNewFile();
				}
			} else {
				currentDownloadSize = 0;
				file.createNewFile();
				Log.v(TAG, "download 创建新文件");
			}
			
			Log.w(TAG, "download 连接中... currentDownloadSize:" + currentDownloadSize);
			conn = getConnection(new URL(url), method, httpHead, connectTimeout, 
					readTimeout, currentDownloadSize, totalDownloadSize);	
			Log.v(TAG, "download 连接上 - currentDownloadSize:" + currentDownloadSize  + " - url:" + url);

			if (method.equals(METHOD_POST) && postContent != null) {
				//Post参数
				conn.setDoOutput(true);
				out = conn.getOutputStream();
				if (out != null) {
					out.write(postContent);
				}
			}
			
			int responseCode = conn.getResponseCode();
			if (! isTaskRun()) {
				Log.i(TAG, "download 任务被暂停 - currentDownloadSize:" + currentDownloadSize + " - url:" + url);
				return RESULT_PAUSE;
			}
			
//			Log.w(TAG, "download 连接成功 currentDownloadSize:" + currentDownloadSize);
			Log.i(TAG, "connection - responseCode: " + responseCode + " - url:" + url);
			if (responseCode != HttpURLConnection.HTTP_OK && responseCode != 206) {
				//网络连接失败
				Log.i(TAG, "download 连接异常 - currentDownloadSize:" + currentDownloadSize);
				if (responseCode == 404) {
					if (file.exists()) {
						file.delete();
					}
				}
				return responseCode;
			}
			
			if (IS_PRINT_HEAD_INFO) {
				Map<String, List<String>> map = conn.getHeaderFields();
				if (map != null) {
					Iterator<Entry<String, List<String>>> it = map.entrySet().iterator();
					while(it.hasNext()) {
						Entry<String, List<String>> entry = it.next();
						String key = entry.getKey();
						List<String> list = entry.getValue();
						StringBuilder builder = new StringBuilder();
						for(int i = 0;i < list.size();i++) {
							if (i != 0) {
								builder.append(",");
							}
							String value = list.get(i);
							builder.append(value);
						}
						Log.v(TAG, "KEY:" + key + " - VALUE:" + builder.toString());
					}
				}
			}
			
			String contentType = conn.getHeaderField("Content-Type");
//			Log.v(TAG, "contentType:" + contentType);
			if (contentType != null && notAcceptTypes != null) {
				/**
				 * 连接CMCC或肯德基等限制网络时，返回的可能是text/html的网页，
				 * 注意如果要下载的不是网页，则加入此限制参数
				 */
				for(String type : notAcceptTypes) {
					if (contentType.contains(type)) {
						Log.i(TAG, "download 不支持的连接类型 - currentDownloadSize:" + currentDownloadSize + " - type:" + type);
						return RESULT_NOT_SUPPORT_CONTENT_TYPE_ERROR;
					}
				}
			}
			
			long contentLength = -1;
			String len = conn.getHeaderField("Content-Length");
			if (StringUtils.isNumber(len)) {
				contentLength = Long.parseLong(len.toString());
			}
			if (contentLength <= 0) {
				Log.i(TAG, "download 远程文件长度异常 - currentDownloadSize:" + currentDownloadSize + " - contentLength:" + contentLength);
				return RESULT_HTTP_DOWNLOAD_LENGTH_ERROR;
			}
			//对参数的长度矫正
//			Log.v(TAG, "start downloading currentDownloadSize: " + currentDownloadSize + " - contentLength:" + contentLength);
			this.downloadOptions.fileLength = currentDownloadSize + contentLength;
			this.totalDownloadSize = currentDownloadSize + contentLength;
			Log.v(TAG, "download - 开始下载 - totalDownloadSize:" + this.totalDownloadSize  + " - url:" + url);
			long availableSpace = DownloadUtil.getAvailaleSize(file.getParentFile().getPath());
			if(contentLength >= availableSpace){
				//磁盘空间不足
				Log.e(TAG, "download 磁盘空间不足!!! - currentDownloadSize:" + currentDownloadSize 
						+ " - availableSpace:" + availableSpace + " - contentLength:" + contentLength);
				return RESULT_NOT_ENOUGH_SPACE_ERROR;
			}
			
			String encoding = conn.getContentEncoding();
			
			InputStream inputStream;
			if(encoding != null && encoding.contains(GZIP_ENCODING)){
				inputStream= new GZIPInputStream(conn.getInputStream());
			}else{
				inputStream = conn.getInputStream();
			}
			
			if (! isTaskRun()) {
				Log.i(TAG, "download 任务被暂停2 - currentDownloadSize:" + currentDownloadSize);
				return RESULT_PAUSE;
			}
			
			downloadStream(savePath, currentDownloadSize, totalDownloadSize, inputStream);
			
//				resultMap.put(MAP_KEY_RESULT, rsp);
			getHttpHeadMap(conn, resultMap);				
			
			if (isTaskRun()) {
				return RESULT_SUC;
			} else {
				return RESULT_PAUSE;
			}
			
		} catch (InterruptedIOException e) {
			e.printStackTrace();
			Log.v(TAG, "InterruptedIOException");
			return RESULT_NETWORK_TIME_OUT_ERROR;
		} catch(IOException e){
			Log.e(TAG, "connection - IOException");
//			throw e;
			e.printStackTrace();
			return RESULT_NETWORK_ERROR;
		} catch (Exception e) {
			Log.e(TAG, "connection - Exception");

			e.printStackTrace();
			return RESULT_OTHER_ERROR;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	private void downloadStream(String downloadPath, long startPos, 
			long fileSize, InputStream inputStream) throws IOException {
		Log.w(TAG, "downloadStream 下载数据流 downloadPath:" + downloadPath 
				+ " - startPos:" + startPos + " - fileSize:" + fileSize  + " - url:" + downloadOptions.url);
		String tempFilePath = downloadPath+SUFFIX_TEMP;
		File file = new File(tempFilePath);
		if (! file.exists()) {
			throw new RuntimeException("下载文件中途被删除");
		}

		notifyListener(DownloadState.STATE_START, 0);

		int offset = 0;
		long lastNotifyTime = System.currentTimeMillis();
		long lastDownloadSize = currentDownloadSize;
		try {
			writeAccessFile = new RandomAccessFile(file, "rw");
			writeAccessFile.seek(startPos);
			Log.v(TAG, "downloadStream seek:" + startPos + " - currentDownloadSize:" + currentDownloadSize);
			
			BufferedInputStream bis = new BufferedInputStream(inputStream, DOWNLOAD_BUFFER_SIZE);
			byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
			
			offset = bis.read(buffer, 0, DOWNLOAD_BUFFER_SIZE);
			if (offset != -1) {
				/**
				 * 成功下载到数据后将重试次数置为0
				 */
				currentRetryCount = 0;
				
				do {
					if (! isTaskRun()) {
						Log.i(TAG, "downloadStream 任务被暂停 1 - currentDownloadSize:" + currentDownloadSize);
						break;
					}
	//				Log.v(TAG, "downloadStream1 currentDownloadSize:" + currentDownloadSize);
	
					writeAccessFile.write(buffer, 0, offset);
					currentDownloadSize += offset;
					
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastNotifyTime < NOTIFY_PROGRESS_INTERVAL_TIME) {
						continue;
					}
					
					if (! isTaskRun()) {
						Log.i(TAG, "downloadStream 任务被暂停 2 - currentDownloadSize:" + currentDownloadSize);
						break;
					}
					
					currentDownloadRate = (int)(currentDownloadSize - lastDownloadSize);
					
					lastNotifyTime = currentTime;
					lastDownloadSize = currentDownloadSize;
					if (isNotifyDownloadingListener || currentDownloadState != DownloadState.STATE_DOWNLOADING) {
						notifyListener(DownloadState.STATE_DOWNLOADING, 0);
					} else {
						refreshDownloadTarget();
					}
					
	//				Log.v(TAG, "downloadStream currentDownloadSize:" + currentDownloadSize  + " - url:" + downloadOptions.url);
				} while((offset = bis.read(buffer, 0, DOWNLOAD_BUFFER_SIZE)) != -1);
			}
			currentDownloadRate = 0;
			
			Log.v(TAG, "downloadStream 下载结束，当前下载大小：" + currentDownloadSize);
			
			writeAccessFile.close();
			
			if (offset == -1) {
				//下载完成
				File tempFile = new File(tempFilePath);
				File endFile = new File(downloadPath);
				tempFile.renameTo(endFile);
			}
		} finally {
			currentDownloadRate = 0;
			writeAccessFile.close();
			inputStream.close();
		}
	}
	
	public boolean isTaskRun() {
		if (isRun && downloadOptions.state.isRun) {
			return true;
		}
//		Log.w(TAG, hashCode() + "->> isTaskRun 未运行 isRun:" + isRun + " - targetIsRun:" + downloadOptions.state.isRun);
		isRun = false;
//		DownloadUtil.printStack(TAG);
		return false;
	}
	
	private boolean isDownloadFinish() {
		if (totalDownloadSize != 0
				&& currentDownloadSize >= totalDownloadSize) {
			return true;
		}
		return false;
	}
	
	public int getDownloadRate() {
		return this.currentDownloadRate;
	}
	
	public void setCurrentThread(Thread thread) {
		this.mCurrentThread = thread;
	}
	
	public void setRetryIntervals(long[] intervals) {
		this.retryIntervals = intervals;
	}
	
	/**
	 * [设置是否下发状态通知]
	 * @param isNotify
	 */
	public void setDownloadNotify(boolean isNotify) {
		this.isNotify = isNotify;
	}
	
	public void pauseDownload() {
		isRun = false;
		if (mCurrentThread != null) {
			mCurrentThread.interrupt();
		}
		if (writeAccessFile != null) {
			try {
				writeAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.v(TAG, this.downloadOptions.url + "->> pauseDownload");
	}
	
	public boolean isDownloadRunning() {
		return this.isRun;
	}

	private HttpURLConnection getConnection(URL url, String method, 
			Map<String, String> httpHead, int connectTimeOut, int readTimeout, 
			long startPos, long endPos) throws IOException {
		
		HttpURLConnection conn = null;
		
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setConnectTimeout(connectTimeOut);
		conn.setReadTimeout(readTimeout);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + DEFAULT_CHARSET);
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("connection", "Keep-Alive");
 		if (startPos >= 0 && endPos > 0) {
			conn.setRequestProperty("Range", "bytes=" + startPos + "-");// 设置获取实体数据的范围
			//不能设置end范围，部分服务器不支持end参数会导致请求错误
//			conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// 设置获取实体数据的范围
		}
		
		if (httpHead != null) {
			Iterator<String> it = httpHead.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				String value = httpHead.get(key);
				
				if (key != null && value != null) {
					conn.setRequestProperty(key, value);
					
					Log.v(TAG, "头信息 " + key +"=" + value);
				}
			}
		}
		
		return conn;
	}
	
	private void getHttpHeadMap(HttpURLConnection conn, Map<String, String> map) {
		if (map == null) {
			return;
		}
		Map<String, List<String>> headmap = conn.getHeaderFields();
		if (headmap == null) {
			return;
		}
		Iterator<Entry<String, List<String>>> it = headmap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			String headKey = entry.getKey();
			List<String> headValueList = entry.getValue();
			String headValue = null;
			if (headValueList != null && headValueList.size() > 0) {
				headValue = headValueList.get(0);
				map.put(headKey, headValue);
			}
		}
	}
	
	public boolean isDownloading() {
		return this.isDownloading;
	}
	
	public DownloadExecutorTarget getDownloadExecutorTarget() {
		return downloadOptions;
	}
	
	public int getDownloadProgress() {
		if (totalDownloadSize <= 0) {
			return 0;
		}
		int progress = (int)((currentDownloadSize*100)/totalDownloadSize);  
		return progress;
	}
	
	public int getDownloadState() {
		return this.currentDownloadState;
	}
	
	public int getCurrentRetryCount() {
		return this.currentRetryCount;
	}
	
	public long getCurrentDownloadSize() {
		return this.currentDownloadSize;
	}
	
	public long getTotalDownloadSize() {
		return this.totalDownloadSize;
	}
	
	private void refreshDownloadTarget() {
		downloadOptions.downloadLength = currentDownloadSize;
		downloadOptions.fileLength = totalDownloadSize;
		downloadOptions.state.downloadState = currentDownloadState;
		
		if (currentDownloadState == DownloadState.STATE_FAIL 
				|| currentDownloadState  == DownloadState.STATE_SUC) {
			downloadOptions.executor = null;
		}
	}
	
	private void notifyListener(int state, int errorReason) {
		if (listener == null) {
			return;
		}
		synchronized (downloadOptions) {
			if (! isNotify) {
				return;
			}
			
			if (mExecutorTargetNotify != null) {
				boolean isCanNotify = mExecutorTargetNotify.isCanNotifyState(state, downloadOptions);
				if (! isCanNotify) {
					return;
				}
			}
			currentDownloadState = state;
			refreshDownloadTarget();

			int progress = DownloadUtil.getDownloadProgress(totalDownloadSize, currentDownloadSize);
			DownloadState downloadState = new DownloadState();
			downloadState.setDownloadId(downloadOptions.downloadId);
			downloadState.setDownloadLen(currentDownloadSize);
			downloadState.setErrorReason(errorReason);
			downloadState.setProgress(progress);
			downloadState.setRetryCount(currentRetryCount);
			downloadState.setState(state);
			downloadState.setTotalLen(totalDownloadSize);
			downloadState.setUri(downloadOptions.url);
			downloadState.setDownloadExecutorTarget(downloadOptions);
		
			if (state == DownloadState.STATE_PAUSE) {
				listener.onDownloadStateChanged(downloadState);
			} else
			if (isRun) {
				Log.v(TAG, "notifyListener isRun:" + isRun + " - state:" + state 
						+ " - url:" + downloadOptions.url
						+ " - ThreadID:" + Thread.currentThread().getId());
				if (state == DownloadState.STATE_FAIL || state == DownloadState.STATE_SUC) {
					downloadOptions.state.isRun = false;
					Log.v(TAG, hashCode() + "->> notifyListener download end");
				}
				listener.onDownloadStateChanged(downloadState);
			} 
		}
	}
	
	public void setIsNotifyDownloadingListener(boolean b) {
		this.isNotifyDownloadingListener = b;
	}
	
	public boolean isNotifyDownloadingListener() {
		return this.isNotifyDownloadingListener;
	}
	
	public void setDownloadListener(IDownloadListener listener) {
		this.listener = listener;
	}
	
	public IDownloadListener getDownloadListener() {
		return this.listener;
	}
	
	private IExecutorTargetNotify mExecutorTargetNotify;
	
	public void setExecutorTargetNotify(IExecutorTargetNotify notify) {
		this.mExecutorTargetNotify = notify;
	}
	public interface IExecutorTargetNotify {
		boolean isCanNotifyState(int state, DownloadExecutorTarget target);
	}
	
	public static final String DEFAULT_CHARSET = "utf-8";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final String MAP_KEY_RESULT = "result";
	public static final String GZIP_ENCODING = "gzip";//gzip的encode名称
	
	public static final String SUFFIX_TEMP = ".temp";
	
	public static final String CONTENT_TYPE_HTML = "text/html";
}


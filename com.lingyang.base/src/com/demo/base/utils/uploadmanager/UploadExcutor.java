package com.lingyang.base.utils.uploadmanager;

import android.content.Context;

import com.lingyang.base.utils.FileUtil;
import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.http.HttpConnectionResultException;
import com.lingyang.base.utils.http.IDataParser;
import com.lingyang.base.utils.http.OnRequestListener;
import com.lingyang.base.utils.http.UploadRequest;
import com.lingyang.base.utils.http.WebUtils;
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

public class UploadExcutor {

	private static final String TAG = UploadExcutor.class.getSimpleName();

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

	public static final String BOUNDARY = "******";

	private String twoHyphens = "--";
	private long fileTotalSize;
	private long fileCurSize;
	private UploadRequest request;
	private UploadResponse response;
	private Context context;
	private OnUploadRequestListener onUploadRequestListener;
	private List<UploadFileBean> uploadList;
	private static final float FACTOR = 0.00f;
	private boolean isRun = true;
	private long notify_progress_interval_time;
	private static final int SIZE_UNIT = 1000 * 1024;
	private Thread mCurrentThread;
	private int currentUploadRate;

	public UploadExcutor(Context context, UploadRequest request) {
		this.request = request;
		this.context = context;
		instanceResponse();
	}

	private void instanceResponse() {
		response = new UploadResponse();
		response.fileList = request.getUploadFileList();
		onUploadRequestListener = request.getOnUploadRequestListener();
		uploadList = request.getUploadFileList();
		fileTotalSize = calculateFileSize(uploadList);
		response.fileTotalSize = fileTotalSize;
	}

	private void initIntervalTime(long size) {
		/*
		 * if (size < 10 * SIZE_UNIT) { notify_progress_interval_time = 20; }
		 * else if (size <50 * SIZE_UNIT) { notify_progress_interval_time = 100;
		 * } else { notify_progress_interval_time = 1000; }
		 */
		notify_progress_interval_time = 100;
	}

	public void setTwoHyphens(String twoHyphens) {
		this.twoHyphens = twoHyphens;
	}

	private int calculateFileSize(List<UploadFileBean> uploadList) {
		int fileSize = 0;
		for (UploadFileBean bean : uploadList) {
			fileSize += new File(bean.getFilePath()).length();
		}

		return fileSize + (int) (fileSize * FACTOR);
	}

	public void startUploadFile() {
		// 根据文件大小初始化进度回调间隔
		initIntervalTime(fileTotalSize);
		OnRequestListener moreListener = request.getOnRequestListener();
		String url = request.getUrl();
		Map<String, String> getParams = request.getUriParam();
		long timeOut = request.getTimeout();
		boolean isMulti = request.isMulti();
		Map<String, String> httpHead = request.getHttpHead();
		if (httpHead == null) {
			httpHead = new HashMap<String, String>();
			httpHead.put("Content-Type", "application/octet-stream");
		}
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
			if (onUploadRequestListener != null) {
				response.state = UploadState.STATE_FAIL;
				onUploadRequestListener.onUploadResponse(response);
			}
			return;
		}
		long t1 = System.currentTimeMillis();
		Map<String, String> resultMap = null;
		try {
			Object obj = request.getPostData();
			if (obj != null && obj instanceof String) {
				resultMap = uploadFile(url, (String) obj, uploadList, httpHead,
						getParams, isMulti, (int) timeOut);

			} else {
				resultMap = uploadFile(url, uploadList, httpHead, getParams,
						isMulti, (int) timeOut);

			}

			result = resultMap.get(WebUtils.MAP_KEY_RESULT);

		} catch (HttpConnectionResultException e) {
			// Http Result不为200
			e.printStackTrace();
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
		Log.i(TAG, "【End】time:" + ((time) / 1000.0f) + " - state:" + state
				+ " - " + url);

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
			moreListener.onResponse(url, state, result,
					request.getRequestType(), request, resultMap);
		}

		if (onUploadRequestListener != null) {
			if (!isRun) {
				response.state = UploadState.STATE_PAUSE;
				response.fileUploadSize = fileCurSize;
			} else if (state == STATE_SUC) {
				response.state = UploadState.STATE_SUC;
				response.fileUploadSize = fileTotalSize;
			} else {
				response.state = UploadState.STATE_FAIL;
				response.fileUploadSize = fileCurSize;
			}

			onUploadRequestListener.onUploadResponse(response);
		}
	}

	public Map<String, String> uploadFile(String url,
			List<UploadFileBean> fileList, Map<String, String> httpHead,
			Map<String, String> getParamMap, boolean isMulti, int timeOut)
			throws IOException {

		HttpURLConnection conn = null;
		OutputStream out = null;
		BufferedInputStream fileInputStream = null;
		String rsp = null;

		url = WebUtils.buildRequestUrl(url, getParamMap, true);

		Log.i(TAG, "uploadFile: " + url);
		try {
			conn = WebUtils.getConnection(new URL(url), WebUtils.METHOD_POST,
					httpHead, timeOut);
			conn.setChunkedStreamingMode(2048);
			conn.setDoOutput(true);
			// conn.setUseCaches(false);
			out = conn.getOutputStream();
			if (out != null) {
				writeFileOutputStream(out, fileList, isMulti);
			}

			int responseCode = conn.getResponseCode();

			Log.i(TAG, "connection - responseCode: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream inputStream = conn.getInputStream();
				String charset = WebUtils.getResponseCharset(conn
						.getContentType());
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

	private void writeFileOutputStream(OutputStream outputStream,
			List<UploadFileBean> fileList, boolean isMulti) throws IOException {

		if (fileList == null) {
			return;
		}

		String end = "\r\n";
		DataOutputStream out = new DataOutputStream(outputStream);
		for (UploadFileBean bean : fileList) {
			String filePath = bean.getFilePath();
			Map<String, String> headMap = bean.getHeadMap();
			if (isMulti) {
				String name = bean.getName();
				String fileName = FileUtil.getFileName(filePath);
				if (name == null) {
					name = fileName;
				}

				out.writeBytes(twoHyphens + BOUNDARY + end);
				out.writeBytes("Content-Disposition: form-data; " + "name=\""
						+ name + "\"; " + "filename=\"" + fileName + "\"" + end);
				if (headMap != null) {
					Iterator<String> it = headMap.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = headMap.get(key);

						out.writeBytes(key);
						out.writeBytes(":");
						out.writeBytes(value);
						out.writeBytes(end);
					}
				}
				out.writeBytes(end);
			}

			BufferedInputStream fileInputStream = null;
			File file = new File(filePath);
			try {
				FileInputStream inputStream = new FileInputStream(file);
				fileInputStream = new BufferedInputStream(inputStream);
				byte[] buffer = new byte[1024];
				int readLen = 0;
				long startTime = System.currentTimeMillis();
				long lastNotifyTime = System.currentTimeMillis();
				long lastUploadSize = fileCurSize;
				// long lastNotifyTime = System.nanoTime();
				while ((readLen = fileInputStream
						.read(buffer, 0, buffer.length)) != -1) {
					if (!isRun) {
						break;
					}
					out.write(buffer);
					fileCurSize += readLen;
					long currentTime = System.currentTimeMillis();
					// long currentTime = System.nanoTime();
					// Log.i(TAG, "before" + notify_progress_interval_time + "|"
					// +currentTime);
					if (currentTime - lastNotifyTime < notify_progress_interval_time) {
						// Log.i(TAG, "continue" + (currentTime -
						// lastNotifyTime));
						continue;
					}
					// Log.i(TAG, "after");
					currentUploadRate = (int) (fileCurSize - lastUploadSize);

					lastNotifyTime = currentTime;
					lastUploadSize = fileCurSize;
					if (onUploadRequestListener != null) {
						Log.i(TAG, "while fileCurSize:" + fileCurSize);
						response.fileUploadSize = fileCurSize;
						response.state = UploadState.STATE_UPLOADING;
						response.uploadRate = currentUploadRate;
						onUploadRequestListener.onUploadResponse(response);
					}
					out.flush();
				}

				if (isMulti && isRun) {
					out.writeBytes(end);
					out.writeBytes(twoHyphens + BOUNDARY + twoHyphens + end);
					response.state = UploadState.STATE_UPLOADING;
				}
				if (!isRun) {
					response.state = UploadState.STATE_PAUSE;
				}

				if (onUploadRequestListener != null) {
					Log.i(TAG, "fileCurSize:" + fileCurSize);
					response.fileUploadSize = fileCurSize;
					onUploadRequestListener.onUploadResponse(response);
				}
				long endtime = System.currentTimeMillis();
				Log.i(TAG, "time:" + (endtime - startTime));

			} finally {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			}

			fileInputStream = null;
		}
	}

	public Map<String, String> uploadFile(String url, String postParam,
			List<UploadFileBean> fileList, Map<String, String> httpHead,
			Map<String, String> getParamMap, boolean isMulti, int timeOut)
			throws IOException {

		HttpURLConnection conn = null;
		OutputStream out = null;
		BufferedInputStream fileInputStream = null;
		String rsp = null;

		url = WebUtils.buildRequestUrl(url, getParamMap, true);

		Log.i(TAG, "uploadFile: " + url);
		try {
			conn = WebUtils.getConnection(new URL(url), WebUtils.METHOD_POST,
					httpHead, timeOut);
			conn.setDoOutput(true);
			out = conn.getOutputStream();
			String post = postParam;
			// out.write(post.getBytes());
			if (out != null) {
				// writeFileOutputStream(out, fileList, true);
				writeFileOutputStream(out, post, fileList);
			}

			int responseCode = conn.getResponseCode();

			Log.i(TAG, "connection - responseCode: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream inputStream = conn.getInputStream();
				String charset = WebUtils.getResponseCharset(conn
						.getContentType());
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
	 * @param skipBytes
	 *            跳过的字节数
	 */
	private void writeFileOutputStream(OutputStream outputStream,
			String postParm, List<UploadFileBean> fileList) throws IOException {

		if (fileList == null) {
			return;
		}

		String end = "\r\n";
		String twoHyphens = "--";
		StringBuffer sb = new StringBuffer();
		DataOutputStream out = new DataOutputStream(outputStream);

		out.writeBytes(twoHyphens + BOUNDARY + end);
		out.writeBytes("Content-Disposition: form-data; " + "name=\"" + "data"
				+ "\"" + end + end);
		out.writeBytes(postParm);
		for (UploadFileBean bean : fileList) {
			String filePath = bean.getFilePath();
			Map<String, String> headMap = bean.getHeadMap();
			// if (isMulti) {
			String name = bean.getName();
			String fileName = FileUtil.getFileName(filePath);
			if (name == null) {
				name = fileName;
			}
			out.writeBytes(twoHyphens + BOUNDARY + end);
			out.writeBytes("Content-Disposition: form-data; " + "name=\""
					+ "file" + "\"; " + "filename=\"" + fileName + "\"" + end);

			sb.append(twoHyphens + BOUNDARY + end);
			sb.append("Content-Disposition: form-data; " + "name=\"" + "file"
					+ "\"; " + "filename=\"" + fileName + "\"" + end);

			if (headMap != null) {
				Iterator<String> it = headMap.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					String value = headMap.get(key);

					out.writeBytes(key);
					out.writeBytes(":");
					out.writeBytes(value);
					out.writeBytes(end);

					sb.append(key);
					sb.append(":");
					sb.append(value);
					sb.append(end);
				}
			}
			out.writeBytes(end);
			sb.append(end);
			// }

			BufferedInputStream fileInputStream = null;
			File file = new File(filePath);
			try {
				FileInputStream inputStream = new FileInputStream(file);
				fileInputStream = new BufferedInputStream(inputStream);
				byte[] buffer = new byte[1024];
				int readLen = 0;
				long lastNotifyTime = System.currentTimeMillis();
				// ----------------
				while ((readLen = fileInputStream
						.read(buffer, 0, buffer.length)) != -1) {
					if (!isRun) {
						break;
					}
					out.write(buffer, 0, readLen);
					fileCurSize += readLen;
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastNotifyTime < notify_progress_interval_time) {
						continue;
					}
					lastNotifyTime = currentTime;
					if (onUploadRequestListener != null) {
						response.fileUploadSize = fileCurSize;
						response.state = UploadState.STATE_UPLOADING;
						onUploadRequestListener.onUploadResponse(response);
					}
				}
				// String data = "data";
				// out.write(data.getBytes());
				if (isRun) {
					out.writeBytes(end);
					out.writeBytes(twoHyphens + BOUNDARY + twoHyphens + end);

					if (onUploadRequestListener != null) {
						response.fileUploadSize = fileCurSize;
						response.state = UploadState.STATE_UPLOADING;
						onUploadRequestListener.onUploadResponse(response);
					}
				}

			} finally {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			}

			fileInputStream = null;
		}

	}

	public boolean isTaskRun() {
		return isRun;
	}

	public void pauseUpload() {
		isRun = false;
		if (mCurrentThread != null) {
			mCurrentThread.interrupt();
		}
		Log.v(TAG, this.request.getUploadFileList().get(0).getFilePath()
				+ "->> pauseDownload");
	}

	public void setCurrentThread(Thread thread) {
		this.mCurrentThread = thread;
	}
}

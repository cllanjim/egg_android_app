package com.lingyang.base.utils.downloadmanager;

public class KeyConstants {

	public static final int CREATE_TASK_SUC = 0;
	public static final int CREATE_TASK_ERROR_EXIST = -2;
	
	public static final int OPERATE_SUC = 0;
	public static final int OPERATE_FAIL = -1;
	public static final int OPERATE_NO_TASK = -2;
	public static final int OPERATE_NO_SPACE = -3;
	public static final int OPERATE_RUNNING = -4;

	public final static int UNKOWN_KEY = -999;
	
	/**
	 * 未知状态
	 */
	public final static int DOWNLOAD_STATE_UNKOWN = UNKOWN_KEY;
	/**
	 * 无状态
	 */
	public final static int DOWNLOAD_STATE_NONE = -2;
	/**
	 * 下载失败
	 */
	public final static int DOWNLOAD_STATE_FAIL = -1;
	/**
	 * 下载成功
	 */
	public final static int DOWNLOAD_STATE_SUC = 0;
	/**
	 * 下载排队中
	 */
	public final static int DOWNLOAD_STATE_PENDING = 1;
	/**
	 * 下载准备中
	 * 进入下载中的队列
	 */
	public final static int DOWNLOAD_STATE_INTO_DOWNLOADING_QUEUE = 2;
	/**
	 * 下载准备中
	 * 连接服务器等操作
	 */
	public final static int DOWNLOAD_STATE_PREPARE = 3;
	/**
	 * 开始下载
	 * 连接上服务器，开始接受文件流
	 */
	public final static int DOWNLOAD_STATE_START = 4;
	/**
	 * 下载中
	 */
	public final static int DOWNLOAD_STATE_DOWNLOADING = 5;
	/**
	 * 下载停止
	 */
	public final static int DOWNLOAD_STATE_PAUSE = 6;
	
	public final static String DOWNLOADER_INSTANCE_ID = "download_service";
	
	public final static String GAME_DOWNLOADER_INSTANCE_ID = "game_download_service";
	
	public final static String SCAN_DOWNLOADER_INSTANCE_ID = "scan_download_service";
	
	/**
	 * 下载出错，网络问题导致的下载错误
	 */
	public final static int ERROR_REASON_NETWORK_ERROR = -1;
	/**
	 * 网络超时
	 */
	public final static int ERROR_REASON_NETWORK_TIME_OUT_ERROR = -2;
	/**
	 * sdcard未挂载
	 */
	public final static int ERROR_REASON_SDCARD_NOT_MOUNTED_ERROR = -3;
	/**
	 * 磁盘空间不足
	 */
	public final static int ERROR_REASON_NOT_ENOUGH_SPACE_ERROR = -4;
	/**
	 * 不支持的下载数据类型
	 * 可以设置不支持的下载类型，默认为 html，保证下载文件时候连接到受限网络时候返回网页的情况
	 */
	public final static int ERROR_REASON_NOT_SUPPORT_CONTENT_TYPE_ERROR = -5;
	/**
	 * 目标文件的下载长度为0
	 */
	public final static int ERROR_REASON_HTTP_DOWNLOAD_LENGTH_ERROR = -6;
	/**
	 * 其他错误
	 * 比如Exception、下载参数没有设置
	 */
	public final static int ERROR_REASON_OTHER_ERROR = -7;
	/**
	 * 由于暂停导致的
	 */
	public final static int ERROR_REASON_PAUSE = -8;
	/**
	 * 正在下载中，又被调用下载则会返回下载中
	 */
	public final static int ERROR_REASON_DOWNLOADING = -9;
	
	public final static int DEFAULT_CONNECT_TIME_OUT = 20 * 1000;
	public final static int DEFAULT_READ_TIME_OUT = 35 * 1000;
}



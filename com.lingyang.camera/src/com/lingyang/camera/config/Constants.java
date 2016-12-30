package com.lingyang.camera.config;

/**
 * 系统常量
 */
public final class Constants {

	/*
	 * 固件常量
	 */
	public static final class FirmWare {

		public static final int STATE_NONE_UPDATE = 0;// 没有更新
		public static final int STATE_HAVE_UPDATE = 1;// 有更新
		public static final int STATE_UPDATEING = 2;// 正在升级
		public static final int STATE_UPDATE_FILED = 3;// 升级失败
		public static final int STATE_FORCE_UPDATE = 4;// 强制升级
	}

	/*
	 * 固件升级常量
	 */
	public static final class upgrade {

		public static final int FIRMWARE_UPGRADE_SUCCEED = 0; // 固件升级成功
		public static final int FIRMWARE_MD5CHECK_FAILURE = -40001; // 下载固件校验失败
		public static final int FIRMWARE_DOWNLOAD_FAILURE = -40002;// 网络异常,下载失败
		public static final int FIRMWARE_INSTALL_FAILURE = -40003;// 固件安装过程失败
		public static final int FIRMWARE_UNKNOWN_ERROR = -1;// 未知错误
	}

	/*
	 * 推送消息常量
	 */
	public static final class Push {

		public static final int TYPE_ONLINE = 1;// 上线报警
		public static final int TYPE_OFFLINE = 2;// 离线警报
		public static final int TYPE_MOTION = 3;// 动作警报
		public static final int TYPE_CMDRST = 4;// 异步响应
		// public static final int TYPE_WEB_LOGIN = 6; // web页面登录了
		public static final int TYPE_FORCE_OFFLINE = 7; // 账号互斥，强制下线
		public static final int TYPE_SESSION_OVERDUE = 8; // session 过期
		// public static final int TYPR_CLOUD_SAVE_PAST_DUE = 9; // 云存储服务到期通知
		// public static final int TYPE_FIRMWARE = 10;// 固件升级
		// public static final int EVENT_AUTHSUCCESS = 11; //表示授权成功
		// public static final int EVENT_AUTHOUTOFDATE = 12; //表示授权已过期
		// public static final int EVENT_DELAUTHED = 13;
		// //删除被授权摄像头(此消息为被授权方发起，授权方接收)
		// public static final int EVENT_DELAUTH = 14;
		// public static final int EVENT_RESOLUTION = 20;//播放模式切换
	}

	/*
	 * HTTP常量
	 */
	public static final class Http {
		/**
		 * 访问正确
		 */
		public static final int STATUS_CODE_SUCESS = 200;
		/**
		 * 访问不存在的页面
		 */
		public static final int STATUS_CODE_URI_NOT_EXSITS = 404;

		/**
		 * 系统错误
		 */
		public static final int ERROR_CODE_SYSTEM_ERROR = 10001;

		/**
		 * 参数错误
		 */
		public static final int ERROR_CODE_NOT_PARAM_ERROR = 20001;

		/**
		 * no permission access_token错误
		 */
		public static final int ERROR_CODE_NO_PERMISSION = 20010;

		/**
		 * 密码错误
		 */
		public static final int ERROR_CODE_LOGIN_PWD_ERROR = 20011;

		/**
		 * 用户名不存在
		 */
		public static final int ERROR_CODE_USER_NOT_EXSIT = 20012;

		/**
		 * 用户名已存在
		 */
		public static final int ERROR_CODE_USER_EXSIT = 20013;

		/**
		 * 密码格式错误
		 */
		public static final int ERROR_CODE_PWD_FORMAT_ERROR = 20014;
		/**
		 * 账户已经激活
		 */
		public static final int ERROR_CODE_ACTIVATE_ACCOUNT = 20015;

		/**
		 * 账户激活已过期
		 */
		public static final int ERROR_CODE_ACTIVATE_EXPIRED = 20016;

		/**
		 * activate_token错误
		 */
		public static final int ERROR_CODE_TOKEN_ERROR = 20017;
		/**
		 * 账户未激活
		 */
		public static final int ERROR_CODE_LOGIN_ACCOUNT_NOT_ACTIVATED = 20018;
		/**
		 * 账户未激活
		 */
		public static final int ERROR_CODE_DEVICE_ALREADY_ADDED = 20021;

		/**
		 * URI 错误
		 */
		public static final int ERROR_CODE_NOT_EXSITS_URI = 30001;

		// public static final int ERROR_CODE_NEWINSTANCE_ERROR = -13;//
		// bean实例化异常
		// public static final int ERROR_CODE_NET_UNAVAILABLE = -12;//
		// 网络不可用,手机网没开
		// public static final int ERROR_CODE_CONNECTION = -11; // 网络异常
		// public static final int ERROR_CODE_ILLEGAL = -10;// HTTP错误参数
		// public static final int ERROR_CODE_NET_WAP = -9;// WAP网络
		// public static final int ERROR_CODE_SSL_HANDS = -8;// SSL握手异常
		// public static final int ERROR_CODE_SSL = -7;// SSL通道建立失败
		// public static final int ERROR_UNKNOWN = -6;// 未定义
		// public static final int ERROR_CODE_DES = -5;// 加解密失败
		// public static final int ERROR_CODE_IO = -4;// IO异常
		// public static final int ERROR_CODE_ENCODING = -3;// URL参数编码失败
		// public static final int ERROR_CODE_JSON = -2;// Json解析异常
		// public static final int ERROR_CODE_SUCCEED = 0;// 成功
		// public static final int ERROR_DEV_IS_BIND = 409;// 摄像头已经被其他人绑定
		// public static final int ERROR_DEV_ADD = 412;// 添加摄像头失败
		// public static final int ERROR_DEV_ADD_SET = 413;// 添加摄像头基本属性失败
		// public static final int ERROR_DEV_ADD_FW = 414;// 添加固件版本失败
		// public static final int ERROR_DEV_INIT = 415; // 初始化设置信息失败
		// public static final int ERROR_DEV_BIND = 416;// 建立绑定关系失败
		// public static final int ERROR_REQUEST = 417; // 错误请求
		// public static final int ERROR_DEV_GET_SET = 418;// 获取摄像头信息失败
		// public static final int ERROR_LOGIN = 419;// 登录失败
		// public static final int ERROR_REMOVE_BIND = 420;// 解除绑定失败
		// public static final int ERROR_UNPERMISSION = 421;// 无权操作
		// public static final int ERROR_GET_EVENTS = 422;// 获取事件列表失败
		// public static final int ERROR_ADD_EVENT = 423;// 添加事件出错
		// public static final int ERROR_GET_EVENT_DATA = 424; // 获取事件数据出错
		// public static final int ERROR_WRITE_FEEDBACK = 425;// 用户反馈写入失败
		// public static final int ERROR_DEV_UPDATE = 426;// 设备信息更新失败
		// public static final int ERROR_FIRMWARE_UPDATE = 427;// 修改升级状态失败
		// public static final int ERROR_GET_RELAY = 428;// 获取Relay失败
		// public static final int ERROR_GET_FIRMWARE_UPGRADE = 429;//
		// 获取固件升级信息失败
		// public static final int ERROR_GET_DEV = 430;// 获取设备信息失败
		// public static final int ERROR_GET_DEV_SET = 431;// 获取设备设置信息失败
		// public static final int ERROR_UPDATE_SET = 432;// 更新设备设置信息失败
		// public static final int ERROR_SET_DEV = 433;// 设置设备失败
		// public static final int ERROR_GET_FIRMWARE = 434;// 获取固件信息失败
		// public static final int ERROR_CAM_NOT_EXIST = 435;// 摄像头不存在
		// public static final int ERROR_USER_NOT_EXIST = 436; // 用户不存在
		// public static final int ERROR_CONNECT_SERVER = 437;// 连接服务器失败
		// public static final int ERROR_SEND_COMMAND = 438; // 发送命令失败
		// public static final int ERROR_GET_MASTER = 439; // 获取master信息失败
		// public static final int ERROR_VALIDATION_SIGNATURE = 440; //
		// 验证signature失败
		// public static final int ERROR_CAM_OFFLINE = 441; // 摄像头离线了
		// public static final int ERROR_CODE_SIDOVERDUE = 442; // sid过期
		// public static final int ERROR_GET_THUMBNAIL = 443;// 获取缩略图失败
		// public static final int ERROR_PUBCAM_CANCEL = 444;// 取消公共摄像头失败
		// public static final int ERROR_CAM_SHARE = 445;// 共享摄像头失败
		// public static final int ERROR_GET_PUBCAM = 446;// 获取公共摄像头列表失败
		// public static final int ERROR_PUBCAM_ADD_TYPE = 447;// 添加公共摄像头分类失败
		// public static final int ERROR_GET_PUBCAM_TYPE = 448;// 获取公共摄像头分类失败
		// public static final int ERROR_CAM_REMOVE = 449;// 该摄像头被移除
		// public static final int ERROR_GET_CAM = 450;// 获取我的公共摄像头列表失败
		// public static final int ERROR_CAM_MODIFY_STATE = 451;// 修改摄像头状态失败
		// public static final int ERROR_CODE_UNAUTHORIZED = 452; // 验证用户信息失败
		// public static final int ERROR_CODE_DEL_SHARE_CODE_ERROR = 518;

		/*
		 * 一下为master服务器新增错误码,以供查阅
		 * 
		 * public static final int GET_PLAY_KEY_ERROR = 500;//获取视频流加密解密key失败
		 * public static final int GET_IPC_SID_ERROR = 501;//获取摄像机的session id失败
		 * public static final int GET_RELAY_SID_ERROR = 502;//获取signature失败
		 * public static final int GET_IPC_LIST_ERROR = 503;//获取摄像机列表失败 public
		 * static final int GET_MAX_FIRMWARE_ERROR= 504;//获取最新版固件信息失败 public
		 * static final int PUSH_TO_IPC_ERROR = 505;//Master2IPC推送失败 public
		 * static final int PUSH_TO_APP_ERROR = 506;//Master2APP推送失败 public
		 * static final int SET_IPC_STATUS_ERROR = 507;//设置IPC状态失败 public static
		 * final int SAVE_IMAGE_ERROR = 508;//保存图片失败 public static final int
		 * GET_USER_USID_ERROR = 509;//获取用户usid失败 public static final int
		 * DECODE_IMAGEDATA_ERROR= 510;//DECODE_IMAGEDATA_ERROR public static
		 * final int GET_NEW_SESSION_ERROR = 511;//获取新session失败 public static
		 * final int ADD_NEW_SESSION_ERROR = 512;//添加新session失败 public static
		 * final int DEL_SESSION_ERROR = 513;
		 */

	}

	/*
	 * 任务运行状态
	 */
	public static final class TaskState {

		public static final int SUCCESS = 0x1111;// 任务成功
		public static final int FAILURE = 0x1112;// 任务失败
		public static final int ISRUNING = 0x1113;// 任务正在运行
		public static final int PAUSE = 0x1114;// 任务暂停
		public static final int EXCEPITON = 0x1115;// 任务异常
	}

	public static final class SSL {

		public static final String CLIENT_AGREEMENT = "TLS"; // 使用协议
		public static final String CLIENT_KEY_MANAGER = "X509"; // 密钥管理器
		public static final String CLIENT_TRUST_MANAGER = "X509"; // 信任证书管理器
		public static final String CLIENT_KEY_KEYSTORE = "BKS"; // "JKS";//密库，这里用的是BouncyCastle密库
		public static final String CLIENT_TRUST_KEYSTORE = "BKS"; // "JKS";//
	}

	/*
	 * 通知栏ID
	 */
	public static final class Notify {

		public static final int NORMAL = 0x2222;
		public static final int APP_UPDATE = 0x2223;
		public static final int FIRMWARE_UPDATE = 0x2224;
	}

	public static final class CameraType {

		public static final int OWN = 0x3331;// 自己的摄像头
		public static final int SHATE = 0x3332;// 分享摄像头
		public static final int PUBLIC = 0x3333;// 公共摄像头

	}

	public static final class WifiType{
		//wifi参数类型，ap模式配置摄像机时需根据不同摄像头
		public static final int WIFI_TYPE_UNSUPPORTED = -1;
		public static final int WIFI_TYPE_WPA = 0;
		public static final int WIFI_TYPE_WPA2 = 0;
		public static final int WIFI_TYPE_WEP = 1;
		public static final int WIFI_TYPE_802_1X = 2;
		public static final int WIFI_TYPE_NOPASSWORD = 3;
	}

}

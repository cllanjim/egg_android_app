package com.antelope.sdk;

public class ACResult {
	private int code;
	private String msg;
	
	public ACResult(int code,String msg){
		this.code=code;
		this.msg=msg;
	}
	
	public int getCode(){
		return code;
	}
	
	public String getCodeDesc() {
		switch (code) {
		case ACS_OK:
			return "No error";
		case ACS_UNKNOWN:
			return "Unknown error";
		case ACS_INVALID_ARG:
			return "Invalid argument";
		case ACS_UNIMPLEMENTED:
			return "Method is unimplemented";
		case ACS_UNINITIALIZED:
			return "Uninitialized or had been released";
		case ACS_NOT_COMPLETED:
			return "Operation not completed yet";
		case ACS_IN_PROCESS:
			return "Operation in processing";
		case ACS_END_OF_STREAM:
			return "There is no more data";
		case ACS_INVALID_DATA:
			return "Invalid data format";
		case ACS_NOT_READY:
			return "Operation not ready for executing";
		case ACS_NOT_SUPPORTED:
			return "Operation not supported";
		case ACS_NOT_CONFIGURED:
			return "Codec specific data not configured yet";
		case ACS_INSUFFICIENT_BUFFER:
			return "Insufficient buffer";
		case ACS_RESUQEST_TIMEDOUT:
			return "Operation running out of time";
		case ACS_FILE_NOT_FOUND:
			return "Specified file cannot be found";
		case ACS_NO_AUTHORIZATION:
			return "no authorization";
		case ACS_NOT_ONLINE:
			return "not online";
		}
		return "Unknown";
	}
	
	public String getErrMsg(){
		return msg;
	}
	
	public boolean isResultOK() {
		return code == ACS_OK;
	}
	
	public static final int ACS_OK = 0;
	public static final int ACS_UNKNOWN = -10001;
	public static final int ACS_INVALID_ARG = -10002;
	public static final int ACS_UNIMPLEMENTED = -10003;
	public static final int ACS_UNINITIALIZED = -10004;
	public static final int ACS_NOT_COMPLETED = -10005;
	public static final int ACS_IN_PROCESS = -10006;
	public static final int ACS_END_OF_STREAM = -10007;
	public static final int ACS_INVALID_DATA = -10008;
	public static final int ACS_NOT_READY = -10009;
	public static final int ACS_NOT_SUPPORTED = -10010;
	public static final int ACS_NOT_CONFIGURED = -10011;
	public static final int ACS_INSUFFICIENT_BUFFER = -10012;
	public static final int ACS_RESUQEST_TIMEDOUT = -10013;
	public static final int ACS_FILE_NOT_FOUND = -10014;
	public static final int ACS_NO_PERMISSION = -10015;
	public static final int ACS_ILLEGAL_STATE = -10016;
	public static final int ACS_EXCEED_VALUE = -10017;
	public static final int ACS_NOT_LOADED = -10018;
	public static final int ACS_NO_DATA = -10019;
	public static final int ACS_NOT_OPENED = -10020;
	public static final int ACS_ALREADY_OPENED = -10021;
	public static final int ACS_OPEN_CAMERA_FAILED=-10101;
	public static final int ACS_NO_AUTHORIZATION=-15000;  //没有授权
	public static final int ACS_NOT_ONLINE=-15001; //不在线
	/// native error code
	public static final int ACC_MALLOC_ERROR                        = -1;
	public static final int ACC_THREAD_CREATE_FAILED                = -2;
	public static final int ACC_PARAM_ERROR                         = -3;
	public static final int ACC_PARAM_NOT_SUPPROT                   = -4;

	public static final int ACC_PROTOCOL_ERROR                      = -101;
	public static final int ACC_UNSUPPORT_PROTOCOL                  = -102;
	public static final int ACC_URL_LENGTH_TOO_LARGE                = -103;
	public static final int ACC_URL_FORMAT_ERROR                    = -104;
	public static final int ACC_URL_IP_ERROR                        = -105;
	public static final int ACC_URL_PORT_ERROR                      = -106;
	public static final int ACC_METHOD_NOT_IMPLEMENT                = -107;
	public static final int ACC_QSUP_NOT_CLOSE                      = -1007;
	public static final int ACC_QSUP_IS_FREE                        = -1008;

	public static final int ACC_SERVICE_INIT_FAILED                 = -901;
	public static final int ACC_SERVICE_IS_OPEN                     = -902;
	public static final int ACC_SERVICE_GETRELAYSERVER_FAILED       = -903;
	public static final int ACC_SERVICE_NOT_PRIVILEGE               = -904;
	public static final int ACC_SERVICE_STATES_ERROR                = -905;
	public static final int ACC_SERVICE_TOKEN_ERROR                 = -906;
	public static final int ACC_SERVICE_INVALID_AREAID              = -907;

	public static final int ACC_QSUP_CONNECT_FAILED                 = -1001;
	public static final int ACC_QSUP_DISCONNECT_FAILED              = -1002;
	public static final int ACC_QSUP_WRITE_FAILED                   = -1003;
	public static final int ACC_QSUP_READ_FAILED                    = -1004;
	public static final int ACC_QSUP_CREATE_FAILED                  = -1005;
	public static final int ACC_QSUP_NOT_OPEN                       = -1006;

	public static final int ACC_QSTP_INIT_FAILED                    = -1101;
	public static final int ACC_QSTP_CREATE_FAILED                  = -1102;
	public static final int ACC_QSTP_CONNECT_FAILED                 = -1103;
	public static final int ACC_QSTP_CHANNEL_CLOSE                  = -1104;
	public static final int ACC_QSTP_PROTOCOL_ERROR                 = -1105;
	public static final int ACC_QSTP_GET_RELAY_FAILED               = -1106;
	public static final int ACC_QSTP_SET_METADATA_ERROR             = -1107;
	public static final int ACC_QSTP_WRITE_FAILED                   = -1108;
	public static final int ACC_QSTP_READ_FAILED                    = -1109;
	public static final int ACC_QSTP_WRITE_PROPERTY_FAILED          = -1110;
	public static final int ACC_QSTP_WRITE_PACKET_ERROR             = -1111;
	public static final int ACC_QSTP_CONTEXT_NOT_CLOSE              = -1112;
	public static final int ACC_QSTP_CONTEXT_IS_CLOSE               = -1113;
	public static final int ACC_QSTP_CONTEXT_IS_FREE                = -1114;
	public static final int ACC_QSTP_GET_PARAM_ERROR                = -1115;

	public static final int ACC_RECORD_INIT_FAILED                  = -1201;
	public static final int ACC_RECORD_QUERY_FAILED                 = -1202;
	public static final int ACC_RECORD_START_DOWNLOAD_FAILED        = -1203;
	public static final int ACC_RECORD_SEEK_FAILED                  = -1204;
	public static final int ACC_RECORD_READ_FAILED                  = -1205;
	public static final int ACC_RECORD_READ_NO_DATA					= -1206;
	public static final int ACC_RECORD_CREATE_FAILED                = -1207;
	public static final int ACC_RECORD_NOT_CLOSE                    = -1208;
	public static final int ACC_RECORD_IS_FREE                      = -1209;
	public static final int ACC_RECORD_IS_CLOSE                     = -1210;

	public static final int ACC_AAC_ENCODE_IS_OPENED                = -1301;
	public static final int ACC_AAC_ENCODE_OPEN_FAILED              = -1302;
	public static final int ACC_AAC_ENCODE_NOT_OPEN                 = -1303;
	public static final int ACC_AAC_ENCODE_FAILED                   = -1304;

	public static final int ACC_OPUS_ENCODE_IS_OPENED               = -1401;
	public static final int ACC_OPUS_ENCODE_OPEN_FAILED             = -1402;
	public static final int ACC_OPUS_ENCODE_NOT_OPEN                = -1403;
	public static final int ACC_OPUS_ENCODE_SAMPLES_ERROR           = -1404;
	public static final int ACC_OPUS_ENCODE_DATA_TOO_SHORT          = -1405;
	public static final int ACC_OPUS_ENCODE_FAILED                  = -1406;
	public static final int ACC_OPUS_ENCODE_SAMPLEPERFRAME_FAILED   = -1407;

	public static final int ACC_AAC_DECODE_INIT_FAILED              = -1501;
	public static final int ACC_AAC_DECODE_CONTEXT_ERROR            = -1502;
	public static final int ACC_AAC_DECODE_OPEN_FAILED              = -1503;
	public static final int ACC_AAC_DECODE_FRAME_ERROR              = -1504;
	public static final int ACC_AAC_DECODE_NOT_OPEN                 = -1505;
	public static final int ACC_AAC_DECODE_FAILED                   = -1506;
	public static final int ACC_AAC_DECODE_GOTFRAME_FAILED          = -1507;
	public static final int ACC_AAC_DECODE_ADTS_ERROR               = -1508;
	public static final int ACC_AAC_DECODE_UNSUPPORTTED_CHANNEL     = -1509;

	public static final int ACC_OPUS_DECODE_IS_OPENED               = -1601;
	public static final int ACC_OPUS_DECODE_OPEN_FAILED             = -1602;
	public static final int ACC_OPUS_DECODE_NOT_OPEN                = -1603;
	public static final int ACC_OPUS_DECODE_DATA_ERROR              = -1604;
	public static final int ACC_OPUS_DECODE_FAILED                  = -1605;

	public static final int ACC_AVC_ENCODER_PRESET_FAILED           = -1701;
	public static final int ACC_AVC_ENCODER_APPLY_PROFILE_FAILED    = -1702;
	public static final int ACC_AVC_ENCODER_OPEN_FAILED             = -1703;
	public static final int ACC_AVC_ENCODER_NOT_OPEN                = -1704;
	public static final int ACC_AVC_ENCODER_SET_SAME_PARAM          = -1705;
	public static final int ACC_AVC_ENCODER_FAILED                  = -1706;
	public static final int ACC_AVC_ENCODER_PARAM_ERROR             = -1707;

	public static final int ACC_AVC_DECODE_INIT_FAILED              = -1801;
	public static final int ACC_AVC_DECODE_CONTEXT_ERROR            = -1802;
	public static final int ACC_AVC_DECODE_OPEN_FAILED              = -1803;
	public static final int ACC_AVC_DECODE_FRAME_ERROR              = -1804;
	public static final int ACC_AVC_DECODE_NOT_OPEN                 = -1805;
	public static final int ACC_AVC_DECODE_FAILED                   = -1806;
	public static final int ACC_AVC_DECODE_NOT_GOT_PIC              = -1807;
	public static final int ACC_AVC_DECODE_IMAGE_ALLOC_FAILED       = -1808;
	public static final int ACC_AVC_DECODE_CREATE_BITMAP_FAILED     = -1809;
	
	public static final int ACC_MP4_FORMAT_CREATE_FAILED            = -1901;
	public static final int ACC_MP4_FORMAT_TIMESCALE_FAILED         = -1902;
	public static final int ACC_MP4_FORMAT_VIDEOBUFFER_NEW_ERROR    = -1903;
	public static final int ACC_MP4_FORMAT_TYPE_ERROR               = -1904;
	public static final int ACC_MP4_FORMAT_WRITE_VIDEO_FAILED       = -1905;
	public static final int ACC_MP4_FORMAT_WRITE_AUDIO_FAILED       = -1906;

	public static final int ACC_EVENT_TOO_LARGE                     = -2001;
	public static final int ACC_EVENT_SEND_FAILED                   = -2002;
	
	public static final int ACC_JNI_PARAM_ERROR                     = -3001;
	public static final int ACC_JNI_PARSE_PARAM_ERROR               = -3002;
	public static final int ACC_JNI_STREAMER_CALLBACK_ERROR         = -3003;
	public static final int ACC_JNI_STREAMER_MESSAGE_ERROR          = -3004;
	
	/// frequent used results
	public static final ACResult SUCCESS = new ACResult(ACResult.ACS_OK, "success");
	public static final ACResult UNINITIALIZED = new ACResult(ACResult.ACS_UNINITIALIZED, "uninitialized");
	public static final ACResult IN_PROCESS = new ACResult(ACResult.ACS_IN_PROCESS, "processing");
	public static final ACResult NO_AUTHORIZATION = new ACResult(ACResult.ACS_NO_AUTHORIZATION, "no authorization");
}

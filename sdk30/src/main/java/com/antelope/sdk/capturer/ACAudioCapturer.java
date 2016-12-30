package com.antelope.sdk.capturer;

import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.WorkThreadExecutor;

/**
* @author zl
* @version 创建时间：2016年9月8日 
* 该类用于音频采集
*/
public class ACAudioCapturer {
	
	private AudioRecord mAudioRecord=null;
	private int mBufferSizeInBytes= 0; //音频最小缓冲区大小
	private int mFrameSize=640;//音频一次读取的字节数
	private int mAudioSource=AudioSource.MIC; //音频源 
	private int mSampleRateInHz=8000; //采样率
	private int mChannelConfig=AudioFormat.CHANNEL_IN_MONO;  //通道数
	private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;    //量化位数
	private int mBitSize = 16;
	private int mChannelCount = 1;
	private int mSampleFormat = ACSampleFormat.AC_SAMPLE_FMT_S16;
	private boolean mIsInitialize=false;//是否初始化了，默认没有初始化
	private boolean mIsOpenMicrophone=false;//麦克风是否打开，默认关闭
	private int mSessionId=-1;
	private byte[] mReadBuffer;
	private ACAudioFrame mAudioFrame;
	private ACFrameAvailableListener mFrameAvailableListener;
	private WorkThreadExecutor mWorkThreadAudioRecord;
	
	
	/**
	 * 初始化音频捕获类
	 * @param sampleRate 音频采样率（必须为8000或者16000）
	 * @param channelCount 音频通道数（必须为1或2）
	 * @param bitSize 量化位数 （必须为16）
	 * @param frameListener 音频数据回调接口
	 * @return ACResult状态码 ,成功或失败错误码 {@link ACResult}
	 */
	public ACResult initialize(int sampleRate, int channelCount, int bitSize, ACFrameAvailableListener frameListener){
		// check channelCount bitSize
		if(sampleRate==8000||sampleRate==16000||sampleRate==11025){
			mSampleRateInHz=sampleRate;
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG, "sample rate should be one of {8000, 16000, 11025}");
		}
		
		if(channelCount==1||channelCount==2){
			mChannelConfig=ConvertChannelConfig(channelCount);
			mChannelCount = channelCount;
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG,"channelCount must be 1 or 2");
		}
		
		if(bitSize==16){
			mAudioFormat = ConvertBitSize(bitSize);
			mBitSize = bitSize;
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG,"bitSize must be 16");
		}
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
	    mBufferSizeInBytes=AudioRecord.getMinBufferSize(mSampleRateInHz,mChannelConfig,mAudioFormat);
	    
	    
	    if(mBufferSizeInBytes==AudioRecord.ERROR_BAD_VALUE){   
	    	return new ACResult(ACResult.ACS_NOT_SUPPORTED," hardware does not support these parameters");
	    }
	    
	    mAudioFrame = new ACAudioFrame();
	    mFrameAvailableListener = frameListener;
	    mWorkThreadAudioRecord = new WorkThreadExecutor("WorkThread-AudioCapturer");
	    
	    mIsInitialize=true;
	    return ACResult.SUCCESS;
	}
	
	
	public ACResult setFrameSize(int frameSize) {
		if (frameSize <= 0) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid frame size");
		}
		if (!mIsInitialize) {
			return ACResult.UNINITIALIZED;
		}
		mWorkThreadAudioRecord.sendMessage(MSG_SET_FRAME_SIZE, frameSize, 0);
		return ACResult.SUCCESS;
	}
	
	
	/**
	 * 打开麦克风
	 * @param source 使用的mic资源；
	 * 如果需要且可用回声消除或自动增益控制，建议传入{@link android.media.MediaRecorder.AudioSource#VOICE_COMMUNICATION}
	 * @return ACResult状态码 ,成功或失败错误码 {@link ACResult}
	 */
	public ACResult openMicrophone(int source){   
		
		// check initialized
		if(!mIsInitialize) { 
			return new  ACResult(ACResult.ACS_UNINITIALIZED,"not initialize");
		}
		//chheck open
		if(!mIsOpenMicrophone) {

			mAudioSource=CheckAudioSource(source); //check audio source

			if(mAudioSource==-1993){
				return new ACResult(ACResult.ACS_INVALID_ARG,"AudioSource is not valid");
			}

			mAudioRecord=new AudioRecord(mAudioSource,mSampleRateInHz,mChannelConfig,mAudioFormat,mBufferSizeInBytes);
			mSessionId=mAudioRecord.getAudioSessionId();
			try {
				mAudioRecord.startRecording();//开始录音
			} catch (IllegalStateException e) {
				CLog.e("start recording failed", e);
				mAudioRecord = null;
				return new ACResult(ACResult.ACS_NO_PERMISSION, "start recording failed, please check permission");
			}
			mIsOpenMicrophone=true;
			
			if (mWorkThreadAudioRecord != null) {
				mWorkThreadAudioRecord.start(mReadFrameMsgCallback);
				mWorkThreadAudioRecord.executeTask(mRreadFrameRunnable);
			}
		}
		return ACResult.SUCCESS;
	}
	
	
	/**
	 * 关闭麦克风
	 * @return ACResult状态码 ,成功或失败错误码 {@link ACResult}
	 */
	public ACResult closeMicrophone(){  
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
		if(!mIsOpenMicrophone){
			return new ACResult(ACResult.ACS_NOT_OPENED,"not open microphone");
		}
		
		mWorkThreadAudioRecord.stop();
		mAudioRecord.stop();
		mAudioRecord.release();
		mAudioRecord=null;
		mIsOpenMicrophone=false;
		mSessionId=-1;
		mReadBuffer = null;
		return ACResult.SUCCESS;
	}
	
	
	
	/**
	 * 音频捕获类反初始化，释放麦克风相关资源。退出当前界面或不需要使用麦克风的时候必须调用此方法释放麦克风相关资源，
	 * 这样其他功能或其他应用才能正常使用麦克风;
	 * @return ACResult状态码 ,成功或失败错误码 {@link ACResult}
	 */
	public ACResult release(){  
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		if (!mIsInitialize) {
			return ACResult.UNINITIALIZED;
		}
		mWorkThreadAudioRecord.stop();
		mWorkThreadAudioRecord.release();
		mWorkThreadAudioRecord = null;
		mIsInitialize=false;
		return ACResult.SUCCESS;
	}
	
	
	
	
	
	/**
	 * 获取麦克风的开闭状态
	 * @return 麦克风是否已经打开
	 */
	public boolean isOpenMicrophone() {
		
		return mIsOpenMicrophone;
	}
	
	
	private Runnable mRreadFrameRunnable = new Runnable() {
		@Override
		public void run() {
			boolean ok = false;
			if (mAudioFrame.buffer == null || mAudioFrame.buffer.capacity() < mFrameSize) {
				mAudioFrame.buffer = ByteBuffer.allocateDirect(mFrameSize);
			} else {
				mAudioFrame.buffer.clear();
			}
			if (Build.VERSION.SDK_INT < 23) {
				if (mReadBuffer == null || mReadBuffer.length < mFrameSize) {
					mReadBuffer = new byte[mFrameSize];
				}
				if (mAudioRecord.read(mReadBuffer, 0, mFrameSize) == mFrameSize) {
					mAudioFrame.buffer.put(mReadBuffer);
					ok = true;
				}
			} else {
				if (mAudioRecord.read(mAudioFrame.buffer, mFrameSize) == mFrameSize) {
					ok = true;
				}
			}
			if (ok) {
				mAudioFrame.offset = 0;
				mAudioFrame.size = mFrameSize;
				mAudioFrame.timestamp = SystemClock.elapsedRealtime();
				mAudioFrame.format = ACSampleFormat.AC_SAMPLE_FMT_S16;
				mAudioFrame.bitSize = mBitSize;
				mAudioFrame.channelCount = mChannelCount;
				mAudioFrame.sampleRate = mSampleRateInHz;
				if (mFrameAvailableListener != null) {
					mFrameAvailableListener.onFrameAvailable(mAudioFrame);
				}
			}
			mWorkThreadAudioRecord.executeTask(mRreadFrameRunnable);
		}
	};
	
	private static final int MSG_OPEN_MIC = 1;
	private static final int MSG_CLOSE_MIC = 2;
	private static final int MSG_SET_FRAME_SIZE = 3;
	
	private Handler.Callback mReadFrameMsgCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_OPEN_MIC:
				break;
			case MSG_CLOSE_MIC:
				break;
			case MSG_SET_FRAME_SIZE:
				mFrameSize = msg.arg1;
				break;
			default:
				return false;
			}
			return true;
		}
	};


	/**
	 * 转换通道数
	 * @param channelCount 通道数
	 * @return 转化结果
	 */
	private int ConvertChannelConfig(int channelCount){  
		
		switch (channelCount) 
		{
	            case 1:
	            	return  AudioFormat.CHANNEL_IN_MONO;
		        case 2:
	            	return  AudioFormat.CHANNEL_IN_STEREO;
		        default:
		        	return  AudioFormat.CHANNEL_IN_MONO;
	                
	     }
	}
	
	
	/**
	 * 转换为通道参数
	 * @param audioFormatChannel 转换后的通道数参数
	 * @return 通道数
	 */
	private int ReverseChannelConfig(int audioFormatChannel){
		
		switch(audioFormatChannel)
		{
		      case AudioFormat.CHANNEL_IN_MONO:
        	    return  1;
              case AudioFormat.CHANNEL_IN_STEREO:
        	    return  2;
              default:
        	     return 1;
		}
	}
	
	
	/**
	 * 转化为量化位数
	 * @param bitSize  量化位数
	 * @return 转换结果
	 */
	private int ConvertBitSize(int bitSize){
		
		switch (bitSize) 
		{
		    case 16:
		    	mSampleFormat = ACSampleFormat.AC_SAMPLE_FMT_S16;
		       return AudioFormat.ENCODING_PCM_16BIT;
		     default:
		    	 mSampleFormat = ACSampleFormat.AC_SAMPLE_FMT_S16;
		       return AudioFormat.ENCODING_PCM_16BIT;
		}
	}
	
	
	/**
	 * 转化为量化位数
	 * @param audioFormat  转换后的量化位数参数
	 * @return 量化位数
	 */
	private int ReverseBitSize(int audioFormat){
		
		switch(audioFormat)
		{
		      case AudioFormat.ENCODING_PCM_16BIT:
		    	  return 16;
		      default: 
		          return 16;
		}
	}
	
	/**
	 * 检验输入得音频源是否合法
	 * @param source 待检验的音频源
	 * @return 音频源
	 */
	private int CheckAudioSource(int source){  
		
		switch(source)
	    {
		      case 0:
		           return AudioSource.DEFAULT; /** Default audio source **/
		      case 1:
			       return AudioSource.MIC;  /** Microphone audio source */
		      case 2:
				  return AudioSource.VOICE_UPLINK; /** Voice call uplink (Tx) audio source */
		      case 3:
				  return AudioSource.VOICE_DOWNLINK; /** Voice call downlink (Rx) audio source */
		      case 4:
				  return AudioSource.VOICE_CALL; /** Voice call uplink + downlink audio source */
		      case 5:
				  return AudioSource.CAMCORDER; /** Microphone audio source with same orientation as camera if available, the main  device microphone otherwise */
		      case 6:
				  return AudioSource.VOICE_RECOGNITION; /** Microphone audio source tuned for voice recognition if available, behaves like*{@link #DEFAULT} otherwise. */
		      case 7:
				  return AudioSource.VOICE_COMMUNICATION; /** Microphone audio source tuned for voice communications such as VoIP.*/
		      case 8:
		    	  if(android.os.Build.VERSION.SDK_INT>=19)
		    	  {
				    return AudioSource.REMOTE_SUBMIX; /**Audio source for a submix of audio streams to be presented remotely.*/
		    	  }
		      default:
		    	  return  -1993; 
	    }
	}
	
	
	
	public int getBufferSizeInBytes() {
		
		return mBufferSizeInBytes;
	}
	
	
	public int getSessionId() {
		return mSessionId;
	}
	

}

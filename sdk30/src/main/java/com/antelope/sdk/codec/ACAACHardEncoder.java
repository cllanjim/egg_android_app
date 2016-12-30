package com.antelope.sdk.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.WorkThreadExecutor;

/**
 * @author zl
 * AAC 音频编码 (硬编)
 */
public class ACAACHardEncoder implements ACAudioEncoder{
  
	private MediaCodec mMediaCodec=null; //编码器
	private int mSampleRate=0;//采样率
	private int mChannelCount=1;//通道数
	private int mBitSize=16;//量化位数
	private int mBitRate=16;//码率
	
	private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private MediaCodec.BufferInfo mBufferInfo = null;
    private ACStreamPacket mEncodedPacket;
    private ACPacketAvailableListener mEncodedPacketListener;
    private WorkThreadExecutor mWorkThreadGetEncodedFrame;
	
	/* 
	 * 初始化编码器
	 * (non-Javadoc)
	 * @see com.antelope.sdk.codec.ACAudioEncoder#initialize(int, int, int, int)
	 */
	@Override
	public ACResult initialize(int sampleRate, int channelCount,int bitSize,int bitRate, ACPacketAvailableListener packetListener) {
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
		if(mMediaCodec!=null){ //check initialized
			return ACResult.SUCCESS;
		}
		
		if(sampleRate==8000||sampleRate==16000||sampleRate==11025){
			this.mSampleRate=sampleRate;
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG, "sample rate should be one of {8000, 16000, 11025}");
		}
		
		if(channelCount==1||channelCount==2){
			this.mChannelCount=channelCount;
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG,"channelCount must be 1 or 2");
		}
		
		if (bitSize != 16) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid bit size, should be 16");
		}
		
		int maxBitRate = (sampleRate*bitSize)/1000;
		if (bitRate > 0 && bitRate < maxBitRate){
			this.mBitRate=bitRate;	
		}else{
			return new ACResult(ACResult.ACS_INVALID_ARG,"bitRate must be > 0 and < "+maxBitRate);
		}
		
		try {
			 mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
		} catch (IOException e) {
			CLog.e("Initialize aac encoder", e);
			return new ACResult(ACResult.ACS_NOT_SUPPORTED, "aac encoder can't be created");
		}
		
		if (!EncoderConfig()) {
			return new ACResult(ACResult.ACS_NOT_CONFIGURED, "aac encoder configure failed");
		}
		
		mEncodedPacket = new ACStreamPacket();
		mWorkThreadGetEncodedFrame = new WorkThreadExecutor("WorkThread-AACEncoder");
		mWorkThreadGetEncodedFrame.start(null);
		
		this.mBitSize=bitSize;
		mEncodedPacketListener = packetListener;
		mBufferInfo = new MediaCodec.BufferInfo();
		
		return ACResult.SUCCESS;
	}
    
	 //配置编码器
     private boolean EncoderConfig(){  
    	// aac encoder
    	MediaFormat format = new MediaFormat();
 		format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
 		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT,mChannelCount);
 		format.setInteger(MediaFormat.KEY_SAMPLE_RATE,mSampleRate);
 		format.setInteger(MediaFormat.KEY_BIT_RATE,mBitRate*1000); //9600
 		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
 		
 		try {
 			mMediaCodec.configure(format, null, null,MediaCodec.CONFIGURE_FLAG_ENCODE);
 			mMediaCodec.start();
 		} catch (IllegalStateException e) {
 			return false;
 		}
 		
 		mInputBuffers = mMediaCodec.getInputBuffers();
 		mOutputBuffers = mMediaCodec.getOutputBuffers();
 		return true;
     }

	/* 
	 * 编码
	 * (non-Javadoc)
	 * @see com.antelope.sdk.codec.ACAudioEncoder#encode(com.antelope.sdk.capturer.ACAudioFrame, com.antelope.sdk.codec.ACStreamPacket, java.lang.Boolean)
	 */
	@Override
	public ACResult encode(ACAudioFrame frame) {
		if (mMediaCodec == null) {
			return ACResult.UNINITIALIZED;
		}
		
		if (frame != null && frame.buffer != null && frame.size > 0) {
			frame.buffer.limit(frame.offset + frame.size);
			frame.buffer.position(frame.offset);

			int inputBufferIndex = -1;
			try {
				inputBufferIndex = mMediaCodec.dequeueInputBuffer(1000000);
			} catch (IllegalStateException e) {
				return new ACResult(ACResult.ACS_ILLEGAL_STATE, "aac encoder is not in executing state");
			}
			if (inputBufferIndex >= 0){
				ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(frame.buffer);
				try {
					mMediaCodec.queueInputBuffer(inputBufferIndex,0,frame.size,frame.timestamp*1000,0);
				} catch (IllegalStateException e) {
					return new ACResult(ACResult.ACS_ILLEGAL_STATE, "aac encoder is not in executing state");
				}
			}
		}
		
		mWorkThreadGetEncodedFrame.executeTask(mDrainFrameRunnable);
		return ACResult.SUCCESS;
	}
   
	
	
	/* 
	 * 重置编码器
	 * (non-Javadoc)
	 * @see com.antelope.sdk.codec.ACAudioEncoder#reset()
	 */
	@Override
	public ACResult reset() {
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
		if(mMediaCodec==null){
			return ACResult.UNINITIALIZED;
		}
		
		mWorkThreadGetEncodedFrame.removeTasksAndMessages(null);
		
		try {
			mMediaCodec.stop();
		} catch (IllegalStateException e) {
		}
		
		mInputBuffers = null;
		mOutputBuffers = null;
		
		if (!EncoderConfig()) {
			return new ACResult(ACResult.ACS_NOT_CONFIGURED, "aac encoder configure failed");
		}
		return ACResult.SUCCESS;
	}
	
	/* 
	 * 释放
	 * (non-Javadoc)
	 * @see com.antelope.sdk.codec.ACAudioEncoder#release()
	 */
	@Override
	public ACResult release() {
		
		if(!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		
		if(mMediaCodec==null){
			return ACResult.UNINITIALIZED;
		}
		
		mWorkThreadGetEncodedFrame.stop();
		mWorkThreadGetEncodedFrame.release();
		mWorkThreadGetEncodedFrame = null;
		
		try {
			mMediaCodec.stop();
		} catch (IllegalStateException e) {
		}
		try {
			mMediaCodec.release();
		} catch (IllegalStateException e) {
		}
		
		mMediaCodec = null;
		mBufferInfo = null;
		mInputBuffers = null;
		mOutputBuffers = null;
		return ACResult.SUCCESS;
	}
	
	private Runnable mDrainFrameRunnable = new Runnable() {
		@Override
		public void run() {
	        int outputBufferIndex;
	        ByteBuffer outputBuffer;
	        while (true) {
	        	try {
	        		outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 100000);
	        	} catch (IllegalStateException e) {
	        		break;
	        	}
	        	if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
	        		// not available right now, try again later
	        		break;
	        	} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
	        		mOutputBuffers = mMediaCodec.getOutputBuffers();
	        		continue;
	        	} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat format = mMediaCodec.getOutputFormat();
					CLog.i("aac encoder output format changed:" + format);
	        		continue;
	        	} else if (outputBufferIndex < 0) {
	        		// unexpected result
	        		break;
	        	}
	        	
	        	outputBuffer = mOutputBuffers[outputBufferIndex];
	        	outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
	        	outputBuffer.position(mBufferInfo.offset);
	        	
	        	if (mEncodedPacket.buffer == null || mEncodedPacket.buffer.capacity() < mBufferInfo.size) {
	        		mEncodedPacket.buffer = ByteBuffer.allocateDirect(mBufferInfo.size);
	        	} else {
	        		mEncodedPacket.buffer.clear();
	        	}
	        	
	        	mEncodedPacket.buffer.put(outputBuffer);
	        	mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);

	        	mEncodedPacket.offset = 0;
	        	mEncodedPacket.size = mBufferInfo.size;
	        	mEncodedPacket.timestamp = mBufferInfo.presentationTimeUs/1000;
	        	if ((mBufferInfo.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
	        		mEncodedPacket.type = ACFrameType.AC_AAC_TYPE_INFO;
	        	} else {
	        		mEncodedPacket.type = ACFrameType.AC_AAC_TYPE_SAMPLE;
	        	}
	        	
	        	if (mEncodedPacketListener != null) {
	        		mEncodedPacketListener.onPacketAvailable(mEncodedPacket);
	        	}
	        }
		}
	};
	
	/**
	  * 添加头部信息
	  * Add ADTS header at the beginning of each and every AAC packet. This is
	  * needed as MediaCodec encoder generates a packet of raw AAC data.
	  * Note the packetLen must count in the ADTS header itself.
	  * packet 数据
	  * packetLen 数据长度
	  * sampleInHz 采样率
	  * chanCfgCounts 通道数
	  **/
	  private  void addADTStoPacket(byte[] packet, int packetLen,int sampleInHz,int chanCfgCounts) {
	  
      int profile = 2; // AAC LC
	  int freqIdx = 8; // 16KHz    39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
	  
	  switch (sampleInHz){
	      case 8000:{
	    	  freqIdx = 11; 
	    	  break;  
	      }
	      case 16000:{
	    	  freqIdx = 8; 
	    	  break;
	      }
	       default:
		     break;
	   }
	  int chanCfg = chanCfgCounts; // CPE
	  // fill in ADTS data
	  packet[0] = (byte) 0xFF;
	  packet[1] = (byte) 0xF9;
	  packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
	  packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
	  packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
	  packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
	  packet[6] = (byte) 0xFC;
	  
	  }

  }

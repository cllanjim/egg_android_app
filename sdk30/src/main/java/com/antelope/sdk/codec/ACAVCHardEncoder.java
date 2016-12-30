package com.antelope.sdk.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.spec.EllipticCurve;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.WorkThreadExecutor;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.Surface;

/**
 * 视频H264/AVC硬编码类
 * 
 * @author weil
 */
public class ACAVCHardEncoder implements ACVideoEncoder {

	private static final String MINE_TYPE = "video/avc";

	private MediaCodec mEncoder;
	private MediaCodec.BufferInfo mBufferInfo;
	private MediaFormat mFormat;
	private int[] mColorFormat = new int[1];
	private int[] mSupportedFormat = new int[1];
	private ByteBuffer[] mInputBuffers;
	private ByteBuffer[] mOutputBuffers;
	private int mWidth, mHeight;
	private int mBitRate = 500;
	private int mFrameRate = 15;
	private int mGop = 5;// 默认5秒一个关键帧
	private boolean isConfigure;
	private ByteBuffer mConfigBuffer,mOutputBuffer;
	private int mConfigSize;
	private boolean mIsInitialized=false;
	private ACStreamPacket mEncodedPacket;
	private ACPacketAvailableListener mEncodedFrameListener;
	private WorkThreadExecutor mGetEncodedDataThread;
	
	@Override
	public ACResult initialize(int width, int height, int gop, ACPacketAvailableListener listener) {
		if (gop <= 0) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid gop");
		}
		if (mIsInitialized) {
			return ACResult.SUCCESS;
		}
		if (!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		mWidth = width;
		mHeight = height;
		mGop = gop;
		mBufferInfo = new MediaCodec.BufferInfo();
		try {
			mEncoder = MediaCodec.createEncoderByType(MINE_TYPE);
		} catch (IOException e) {
			CLog.e("create avc encoder", e);
			return new ACResult(ACResult.ACS_NOT_SUPPORTED, "not supported codec type:" + MINE_TYPE);
		}

		boolean found = false;
		MediaCodecInfo.CodecCapabilities capabilities = mEncoder.getCodecInfo().getCapabilitiesForType(MINE_TYPE);
		for (int i = 0; i < capabilities.colorFormats.length; i++) {
			int format = capabilities.colorFormats[i];
			if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
					|| format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
				mColorFormat[0] = format;
				found = true;
				break;
			}
		}
		if (!found) {
			return new ACResult(ACResult.ACS_NOT_SUPPORTED, "no proper color format found");
		}
		
		if (mColorFormat[0] == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
			mSupportedFormat[0] = ACImageFormat.AC_IMAGE_FMT_YUV420P;
		} else if (mColorFormat[0] == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
			mSupportedFormat[0] = ACImageFormat.AC_IMAGE_FMT_YUV420SP;
		}
		
		mEncodedPacket = new ACStreamPacket();
		mGetEncodedDataThread = new WorkThreadExecutor("WorkThread-AVCEncoder");
		mGetEncodedDataThread.start(null);

		mEncodedFrameListener = listener;
		isConfigure=false;
		mIsInitialized=true;
		return ACResult.SUCCESS;

	}

	@Override
	public Surface getInputSurface() {
		return null;
	}
	
	@Override
	public int[] getSupportedImageFormat() {
		if (!mIsInitialized) {
			return null;
		}
		return mSupportedFormat;
	}

	@SuppressLint("NewApi")
	@Override
	public ACResult encode(ACVideoFrame frame, boolean forceKeyFrame) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (frame.format != mSupportedFormat[0]) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "not supported image format");
		}
		if(!(frame.width==mWidth&&frame.height==mHeight)){
			mWidth = frame.width;
			mHeight = frame.height;
			if(isConfigure){
				try {
					mEncoder.stop();
				} catch (IllegalStateException e) {
				}
				isConfigure=false;
			}
		}
		if(!isConfigure){
			setFormat();
			// Create a MediaCodec encoder, and configure it with our format. Get a
			// Surface
			// we can use for input and wrap it with a class that handles the EGL
			// work.
			try {
				mEncoder.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				mEncoder.start();
			} catch (IllegalStateException e) {
				return new ACResult(ACResult.ACS_NOT_CONFIGURED, "avc encoder configure failed");
			}
			mInputBuffers = mEncoder.getInputBuffers();
			mOutputBuffers = mEncoder.getOutputBuffers();
			isConfigure=true;
		}

		if (frame != null && frame.buffer != null) {
			int index = -1;
			try {
				index = mEncoder.dequeueInputBuffer(100000);// 有数据返回输入缓冲区的索引，无数据返回-1，10000等待时间
			} catch (IllegalStateException e) {
				return new ACResult(ACResult.ACS_ILLEGAL_STATE, "avc encoder is not in executing state");
			}
			if (index >= 0) {
				frame.buffer.limit(frame.size);
				frame.buffer.position(frame.offset);
				mInputBuffers[index].clear();
				mInputBuffers[index].put(frame.buffer);
				try {
					mEncoder.queueInputBuffer(index, 0, frame.size, frame.timestamp * 1000, 0);
				} catch (IllegalStateException e) {
					return new ACResult(ACResult.ACS_ILLEGAL_STATE, "avc encoder is not in executing state");
				}
				if (forceKeyFrame) { // 强制关键帧
					Bundle params = new Bundle();
					params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
					mEncoder.setParameters(params);
				}
			}
		}
		
		mGetEncodedDataThread.executeTask(mDrainFrameRunnable);
		return ACResult.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	private Runnable mDrainFrameRunnable = new Runnable() {
		@Override
		public void run() {
			int status;
			ByteBuffer buffer;
			while (true) {
				try {
					status = mEncoder.dequeueOutputBuffer(mBufferInfo, 100000);
				} catch (IllegalStateException e) {
					break;
				}
				if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					mOutputBuffers = mEncoder.getOutputBuffers();
					continue;
				} else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat format = mEncoder.getOutputFormat();
					CLog.i("avc encoder output format changed:" + format);
					continue;
				} else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
					// no output
					break;
				} else if (status < 0) {
					CLog.d("unexpect result from Encoder.dequeueOutputBuffer :" + status);
					break;
				}
				
				buffer = mOutputBuffers[status]; 
				buffer.limit(mBufferInfo.offset+mBufferInfo.size);
				buffer.position(mBufferInfo.offset);
				
				//按位与& 操作，以二进制形式操作，两个都为1才为1，有一个为0就是0；
				if((mBufferInfo.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){
					//sps,pps,编码后的第一帧数据为sps,pps,放在每个关键帧前面
					mConfigBuffer=ByteBuffer.allocateDirect(mBufferInfo.size);
					mConfigBuffer.put(buffer);
					mConfigSize=mBufferInfo.size;
					mEncoder.releaseOutputBuffer(status, false);
					continue;
				}
				else if((mBufferInfo.flags&MediaCodec.BUFFER_FLAG_KEY_FRAME)!=0){
					//关键帧
					mConfigBuffer.clear();
					if(mOutputBuffer==null||mOutputBuffer.capacity()<(mBufferInfo.size+mConfigBuffer.remaining()))
						mOutputBuffer=ByteBuffer.allocateDirect(mConfigBuffer.remaining()+mBufferInfo.size);
					else mOutputBuffer.clear();
					mOutputBuffer.put(mConfigBuffer);
					mOutputBuffer.put(buffer);
					mEncodedPacket.type = ACFrameType.AC_NALU_TYPE_SPS;
					mEncodedPacket.size=mBufferInfo.size+mConfigSize;
				}else{
					//非关键帧 p帧或B帧
					if(mOutputBuffer==null||mOutputBuffer.capacity()<mBufferInfo.size)
						mOutputBuffer=ByteBuffer.allocateDirect(mBufferInfo.size);
					else mOutputBuffer.clear();
					mOutputBuffer.put(buffer);
					mEncodedPacket.type = ACFrameType.AC_NALU_TYPE_SLICE;
					mEncodedPacket.size = mBufferInfo.size;
				}
				mEncodedPacket.buffer = mOutputBuffer;
				mEncodedPacket.offset = 0;
				mEncodedPacket.timestamp = mBufferInfo.presentationTimeUs/1000;
				mEncoder.releaseOutputBuffer(status, false);
				if (mEncodedFrameListener != null) {
					mEncodedFrameListener.onPacketAvailable(mEncodedPacket);
				}
			}
		}
	};

	private void setFormat() {
		// Set some properties. Failing to specify some of these can cause the
		// MediaCodec
		// configure() call to throw an unhelpful exception.
		mFormat = MediaFormat.createVideoFormat(MINE_TYPE, mWidth, mHeight);
		mFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate*1000);
		mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat[0]);
		mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
		mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mGop);
	}

	@Override
	public ACResult release() {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		mGetEncodedDataThread.stop();
		mGetEncodedDataThread.release();
		mGetEncodedDataThread = null;
		try {
			mEncoder.stop();
		} catch (IllegalStateException e) {
		}
		try {
			mEncoder.release();
		} catch (IllegalStateException e) {
		}
		mEncoder = null;
		mOutputBuffers = null;
		mInputBuffers = null;
		mFormat = null;
		mBufferInfo = null;
		mEncoder=null;
		isConfigure=false;
		mIsInitialized=false;
		return ACResult.SUCCESS;
	}

	@SuppressLint("NewApi")
	@Override
	public ACResult setBitRate(int bitRate) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if(bitRate<=0)
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid value");
		if (mBitRate != bitRate) {
			mBitRate = bitRate;
			Bundle params = new Bundle();
			params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, mBitRate);
			mEncoder.setParameters(params);
		}
		return ACResult.SUCCESS;
	}

	@SuppressLint("NewApi")
	@Override
	public ACResult setFrameRate(int frameRate) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if(mFrameRate<=0)
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid value");
		if (mFrameRate != frameRate) {
			mFrameRate = frameRate;
			Bundle params = new Bundle();
			params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, mFrameRate);
			mEncoder.setParameters(params);
		}
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult reset() {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		mGetEncodedDataThread.removeTasksAndMessages(null);
		try {
			mEncoder.stop();
		} catch (IllegalStateException e) {
		}
		mOutputBuffers = null;
		mInputBuffers = null;
		mFormat = null;
		isConfigure=false;
		return ACResult.SUCCESS;
	}

}

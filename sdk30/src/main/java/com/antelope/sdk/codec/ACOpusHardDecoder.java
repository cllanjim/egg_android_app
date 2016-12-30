package com.antelope.sdk.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.capturer.ACSampleFormat;
import com.antelope.sdk.utils.CLog;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;

public class ACOpusHardDecoder implements ACAudioDecoder {
	 private static final String MIME_TYPE = "audio/opus"; 
	 private static final int TIMEOUT_USEC = 20000;
	 
	 private MediaCodec mDecoder = null;
	 private ByteBuffer[] mDecoderInputBuffers = null;
	 private ByteBuffer[] mDecoderOutputBuffers = null;
	 private BufferInfo mBufferInfo = new BufferInfo();
	 private boolean mConfigured = false;
	 private ByteBuffer mOutputBuffer = null;
	 private ByteBuffer mConfigBuffer = null;
	 private ByteBuffer mPreSkipBuffer = null;
	 private ByteBuffer mPreRollBuffer = null;
	 
	 private int mSampleRate = 48000;
	 private int mChannelCount = 2;
	 private int mBitSize = 16;
	 
	@Override
	public ACResult initialize(int sampleRate, int channelCount) {
		try {
			mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
		} catch (IOException e) {
			CLog.e("Initialize opus decoder", e);
			return new ACResult(ACResult.ACS_UNKNOWN, "opus decoder can't be created");
		}
//		mSampleRate = sampleRate;
		mChannelCount = channelCount;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult release() {
		if (mDecoder == null) {
			return ACResult.UNINITIALIZED;
		}
		if (mDecoder != null) {
			try {
				mDecoder.stop();
			} catch (IllegalStateException e) {
			}
			try {
				mDecoder.release();
			} catch (IllegalStateException e) {
			}
			mDecoder = null;
		}
		mOutputBuffer = null;
		mConfigBuffer = null;
		mPreSkipBuffer = null;
		mPreRollBuffer = null;
		mConfigured = false;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult decode(ACStreamPacket packet, ACAudioFrame frame) {
		if (frame == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, null);
		}
		
		if (packet != null && packet.buffer != null) {
			packet.buffer.limit(packet.offset + packet.size);
			packet.buffer.position(packet.offset);
			
			if (!mConfigured) {
				if (!configureDecoder()) {
					return new ACResult(ACResult.ACS_INVALID_DATA, "no codec specific data found");
				}
				mConfigured = true;
			}
			
			 int inputBufIndex = -1;
			 try {
				 inputBufIndex = mDecoder.dequeueInputBuffer(100000);
			 } catch (IllegalStateException e) {
				 CLog.e("opus decoder dequeueInputBuffer", e);
			 }
			 if (inputBufIndex >= 0) {
				 ByteBuffer inputBuf = mDecoderInputBuffers[inputBufIndex];
				 inputBuf.clear();
				 inputBuf.put(packet.buffer);
				 try {
					 mDecoder.queueInputBuffer(inputBufIndex, 0, packet.size, packet.timestamp*1000, 0);
				 } catch (IllegalStateException e) {
					 CLog.e("opus decoder queueInputBuffer", e);
				 }
			 }
		}
		
		int gotFrame = getDecodedFrame(frame);
		if (gotFrame == 1) {
			return ACResult.SUCCESS;
		} else if (gotFrame == -1) {
			return new ACResult(ACResult.ACS_END_OF_STREAM, null);
		}
		return ACResult.IN_PROCESS;
	}

	@Override
	public ACResult reset() {
		if (mDecoder == null) {
			return ACResult.UNINITIALIZED;
		}
		 if (mDecoder != null) {
			 try {
				 mDecoder.stop();
			 } catch (IllegalStateException e) {
			 }
		 }
		 mOutputBuffer = null;
		 mConfigBuffer = null;
		 mPreSkipBuffer = null;
		 mPreRollBuffer = null;
		 mConfigured = false;
		return ACResult.SUCCESS;
	}

	private boolean configureDecoder() {
		if (!generateConfigBuffer()) {
			return false;
		}
		MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, mSampleRate, mChannelCount);
		if (mConfigBuffer != null) {
			format.setByteBuffer("csd-0", mConfigBuffer);
			format.setByteBuffer("csd-1", mPreSkipBuffer);
			format.setByteBuffer("csd-2", mPreRollBuffer);
		}
		try {
			mDecoder.stop();
		} catch (IllegalStateException e) {
		}
       mDecoder.configure(format, null, null, 0);
       mDecoder.start();
       mDecoderInputBuffers = mDecoder.getInputBuffers();
       mDecoderOutputBuffers = mDecoder.getOutputBuffers();
       return true;
	}
	
	 private boolean generateConfigBuffer() {
		 mConfigBuffer = ByteBuffer.allocate(19);
		 mConfigBuffer.order(ByteOrder.nativeOrder());
		 mConfigBuffer.put((byte) 'O');
		 mConfigBuffer.put((byte) 'p');
		 mConfigBuffer.put((byte) 'u');
		 mConfigBuffer.put((byte) 's');
		 mConfigBuffer.put((byte) 'H');
		 mConfigBuffer.put((byte) 'e');
		 mConfigBuffer.put((byte) 'a');
		 mConfigBuffer.put((byte) 'd');
		 mConfigBuffer.put((byte) 1); // version
		 mConfigBuffer.put((byte) mChannelCount); // channel count
		 mConfigBuffer.putShort((short) 0); // Pre-skip
		 mConfigBuffer.putInt(mSampleRate); // original input sample rate in Hz
		 mConfigBuffer.putShort((short) 0); // output gain Q7.8 in dB
		 mConfigBuffer.put((byte) 0); // channel map
		 mConfigBuffer.clear();
		 mPreSkipBuffer = ByteBuffer.allocate(8);
		 mPreSkipBuffer.order(ByteOrder.nativeOrder());
		 mPreSkipBuffer.putLong(80l);
		 mPreSkipBuffer.clear();
		 mPreRollBuffer = ByteBuffer.allocate(8);
		 mPreRollBuffer.order(ByteOrder.nativeOrder());
		 mPreRollBuffer.putLong(0l);
		 mPreRollBuffer.clear();
		 return true;
	 }
	 
	 private int getDecodedFrame(ACAudioFrame frame) {
		 int gotFrame = 0;
		 int decoderStatus;
		 while (true) {
			 try {
				 decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
			 } catch (IllegalStateException e) {
				 CLog.e("opus decoder dequeueOutputBuffer", e);
				 return -1;
			 }
			 if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				 // no output available yet
//				 CLog.d("no output from opus decoder available");
				 break;
			 } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				 // The storage associated with the direct ByteBuffer may already be unmapped,
				 // so attempting to access data through the old output buffer array could
				 // lead to a native crash.
//				 CLog.d("opus decoder output buffers changed");
				 mDecoderOutputBuffers = mDecoder.getOutputBuffers();
			 } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				 // this happens before the first frame is returned
				 MediaFormat decoderOutputFormat = mDecoder.getOutputFormat();
				 CLog.d("opus decoder output format changed: " + decoderOutputFormat);
				 // remain init param
//				 mSampleRate = decoderOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//				 mChannelCount = decoderOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
				 if (decoderOutputFormat.containsKey("bit-width")) {
					 mBitSize = decoderOutputFormat.getInteger("bit-width");
				 }
			 } else if (decoderStatus < 0) {
				 CLog.e("unexpected result from opus deocder.dequeueOutputBuffer: " + decoderStatus);
			 } else {  // decoderStatus >= 0
//				 CLog.d("opus decoded buffer " + decoderStatus +
//						 " (size=" + mBufferInfo.size + ", timestamp=" + mBufferInfo.presentationTimeUs/1000 + ")");
				 if (mBufferInfo.size > 0) {
					 gotFrame = 1;
					 copy2Frame(frame, decoderStatus);
				 }
				 mDecoder.releaseOutputBuffer(decoderStatus, false);
				 break;
			 }

			 if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				 CLog.d("opus decoder end of stream reached");
				 gotFrame = -1;
				 break;
			 }
		 }
		 return gotFrame;
	 }
	 
	 private void copy2Frame(ACAudioFrame frame, int outIdx) {
			ByteBuffer outputBuffer = mDecoderOutputBuffers[outIdx];
			
			if (frame.buffer == null || frame.buffer.capacity() < mBufferInfo.size) {
				if (mOutputBuffer == null || mOutputBuffer.capacity() < mBufferInfo.size) {
					mOutputBuffer = ByteBuffer.allocate(mBufferInfo.size);
				}
				frame.buffer = mOutputBuffer;
			}
			
			outputBuffer.position(mBufferInfo.offset);
			outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
			
			frame.buffer.position(0);
			frame.buffer.put(outputBuffer);
			frame.size = mBufferInfo.size;
			frame.offset = 0;
			frame.sampleRate = mSampleRate;
			frame.channelCount = mChannelCount;
			switch (mBitSize) {
			case 8:
				frame.bitSize = 8;
				frame.format = ACSampleFormat.AC_SAMPLE_FMT_U8;
				break;
			case 16:
			default:
				frame.bitSize = 16;
				frame.format = ACSampleFormat.AC_SAMPLE_FMT_S16;
				break;
			}
			frame.timestamp = mBufferInfo.presentationTimeUs / 1000;
	 }

}

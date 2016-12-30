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

public class ACAACHardDecoder implements ACAudioDecoder {
	 private static final String MIME_TYPE = "audio/mp4a-latm"; 
	 private static final int TIMEOUT_USEC = 32000;
	 
	 private MediaCodec mDecoder = null;
	 private ByteBuffer[] mDecoderInputBuffers = null;
	 private ByteBuffer[] mDecoderOutputBuffers = null;
	 private BufferInfo mBufferInfo = new BufferInfo();
	 private ByteBuffer mOutputBuffer = null;
	 private ByteBuffer mConfigBuffer = null;
	 private boolean mConfigured = false;
	 private int mHeadSize = 0;
	 private int mSampleRate = 16000;
	 private int mChannelCount = 1;
	 private int mBitSize = 16;
	 
	@Override
	public ACResult initialize(int sampleRate, int channelCount) {
		try {
			mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
		} catch (IOException e) {
			CLog.e("Initialize aac decoder", e);
			return new ACResult(ACResult.ACS_UNKNOWN, "aac decoder can't be created");
		}
		mSampleRate = sampleRate;
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
		mConfigured = false;
		mHeadSize = 0;
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
			
			int flags = 0;
			int size = packet.size;
			if (!mConfigured) {
				if (size < 2 ||
						(packet.type == ACFrameType.AC_AAC_TYPE_INFO && size != 2 && size != 5) ||
						!configureDecoder(packet.buffer)) {
					return new ACResult(ACResult.ACS_INVALID_DATA, "no codec specific data found");
				}
				mConfigured = true;
				packet.buffer.position(packet.offset + mConfigBuffer.capacity());
				size = packet.buffer.remaining();
				if (size == 0) {
					return ACResult.IN_PROCESS;
				}
			} else if (!mConfigured || packet.type == ACFrameType.AC_AAC_TYPE_INFO) {
				return ACResult.IN_PROCESS;
			}
			
			 if (mHeadSize > 0) {
				 if (mHeadSize >= size) {
					 return new ACResult(ACResult.ACS_INVALID_DATA, "invalid data size");
				 }
				 packet.buffer.position(packet.buffer.position()+mHeadSize);
				 size -= mHeadSize;
			 }
			
			 int inputBufIndex = -1;
			 try {
				 inputBufIndex = mDecoder.dequeueInputBuffer(100000);
			 } catch (IllegalStateException e) {
				 CLog.e("aac decoder dequeueInputBuffer", e);
			 }
			 if (inputBufIndex >= 0) {
				 ByteBuffer inputBuf = mDecoderInputBuffers[inputBufIndex];
				 inputBuf.clear();
				 inputBuf.put(packet.buffer);
				 try {
					 mDecoder.queueInputBuffer(inputBufIndex, 0, size, packet.timestamp*1000, flags);
				 } catch (IllegalStateException e) {
					 CLog.e("aac decoder queueInputBuffer", e);
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
				 CLog.e("Stop aac decoder", e);
			 }
		 }
		 mOutputBuffer = null;
		 mConfigBuffer = null;
		 mConfigured = false;
		 mHeadSize = 0;
		return ACResult.SUCCESS;
	}

	private boolean configureDecoder(ByteBuffer buffer) {
		if (buffer != null && !getConfigBuffer(buffer)) {
			return false;
		}
		MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, mSampleRate, mChannelCount);
		if (mConfigBuffer != null) {
			format.setByteBuffer("csd-0", mConfigBuffer);
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
	
	 private boolean getConfigBuffer(ByteBuffer data) {
		 int pos = data.position();
		 int size = data.remaining();
		 data.order(ByteOrder.BIG_ENDIAN);
		 short syncword = data.getShort(pos);
		 if ( size >= 7 && (syncword & 0xfff0) == 0xfff0) { // ADTS
			 byte[] adts = new byte[7];
			 data.get(adts);
			 data.position(pos);
			 
			 int profile = ((adts[2]>>6) & 0x03) + 1;
			 int frequency_index = (adts[2] >> 2) & 0x0f;
			 int channel_config = ((adts[2] & 0x01) << 2) | ((adts[3] >> 6 ) & 0x03);
			 int protection_absent = adts[1] & 0x01;
			 
			 byte[] config = new byte[2];
			 config[0] = (byte)(((profile << 3) & 0xf8) | ((frequency_index >> 1) & 0x07));
			 config[1] = (byte)(((frequency_index << 7) & 0x80) | ((channel_config << 3) & 0x78));
//			 byte profile = (config[0]>>3)&0x1f;
//			 byte frequency_index = (config[0]&0x7) <<1 | (config[1]>>7) &0x1;
//			 byte channel_config = (config[1]>>3) &0xf;
			 
			 mConfigBuffer = ByteBuffer.allocateDirect(2);
			 mConfigBuffer.put(0, config[0]);
			 mConfigBuffer.put(1, config[1]);
			 mHeadSize = protection_absent==1?7:9;
		 } else if (size == 2 || size == 5) { // assume ES
			 mConfigBuffer = ByteBuffer.allocateDirect(2);
			 for (int i=0; i<2; i++) {
				 mConfigBuffer.put(i, data.get(pos + i));
			 }
		 } else {
			 return false;
		 }
		 return true;
	 }
	 
	 private int getDecodedFrame(ACAudioFrame frame) {
		 int gotFrame = 0;
		 int decoderStatus;
		 while (true) {
			 try {
				 decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
			 } catch (IllegalStateException e) {
				 CLog.e("aac decoder dequeueOutputBuffer", e);
				 return -1;
			 }
			 if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				 // no output available yet
//				 CLog.d("no output from aac decoder available");
				 break;
			 } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				 // The storage associated with the direct ByteBuffer may already be unmapped,
				 // so attempting to access data through the old output buffer array could
				 // lead to a native crash.
//				 CLog.d("aac decoder output buffers changed");
				 mDecoderOutputBuffers = mDecoder.getOutputBuffers();
			 } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				 // this happens before the first frame is returned
				 MediaFormat decoderOutputFormat = mDecoder.getOutputFormat();
				 CLog.d("aac decoder output format changed: " + decoderOutputFormat);
				 mSampleRate = decoderOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
				 mChannelCount = decoderOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
				 if (decoderOutputFormat.containsKey("bit-width")) {
					 mBitSize = decoderOutputFormat.getInteger("bit-width");
				 }
			 } else if (decoderStatus < 0) {
				 CLog.e("unexpected result from aac deocder.dequeueOutputBuffer: " + decoderStatus);
			 } else {  // decoderStatus >= 0
//				 CLog.d("aac decoded buffer " + decoderStatus +
//						 " (size=" + mBufferInfo.size + ", timestamp=" + mBufferInfo.presentationTimeUs/1000 + ")");
				 if (mBufferInfo.size > 0) {
					 gotFrame = 1;
					 copy2Frame(frame, decoderStatus);
				 }
				 mDecoder.releaseOutputBuffer(decoderStatus, false);
				 break;
			 }

			 if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				 CLog.d("aac decoder end of stream reached");
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

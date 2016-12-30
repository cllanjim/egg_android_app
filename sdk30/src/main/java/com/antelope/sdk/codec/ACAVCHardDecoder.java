package com.antelope.sdk.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.utils.CLog;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public class ACAVCHardDecoder implements ACVideoDecoder {

	private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
	private static final int TIMEOUT_USEC = 10000;
	private MediaCodec mDecoder = null;
	private ByteBuffer[] mDecoderInputBuffers = null;
	private ByteBuffer[] mDecoderOutputBuffers = null;
	private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
	private int mWidth = 320;
	private int mHeight = 240;
	private int mStride = 320;
	private int mSliceHeight = 240;
	private int mColorFormat = 0;
	private boolean mConfigured = false;
	private ByteBuffer mOutputBuffer = null;
	private ByteBuffer[] mSPSBuffer = new ByteBuffer[2];
	private ByteBuffer[] mPPSBuffer = new ByteBuffer[2];
	private int mGotConfigData = 0;

	@Override
	public ACResult initialize() {
		try {
			mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
		} catch (IOException e) {
			CLog.e("Initialize avc decoder", e);
			return new ACResult(ACResult.ACS_UNKNOWN, "avc decoder can't be created");
		}
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
		mSPSBuffer = null;
		mPPSBuffer = null;
		mConfigured = false;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult decode(ACStreamPacket packet, ACVideoFrame frame) {
		if (frame == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, null);
		}

		if (packet != null && packet.buffer != null) {
			packet.buffer.limit(packet.offset + packet.size);
			packet.buffer.position(packet.offset);

			int flags = 0;
			if (packet.type == ACFrameType.AC_NALU_TYPE_SPS || packet.type == ACFrameType.AC_NALU_TYPE_PPS) {
				// check if codec specific data changed
				ByteBuffer sps = extractSpecificDataBufferFrom(packet.buffer, (byte) 0x07, mSPSBuffer[1]);
				ByteBuffer pps = extractSpecificDataBufferFrom(packet.buffer, (byte) 0x08, mPPSBuffer[1]);
				if (sps != null) {
					if (mSPSBuffer[1] == null) {
						mSPSBuffer[1] = sps;
					}
					mGotConfigData++;
				}
				if (pps != null) {
					if (mPPSBuffer[1] == null) {
						mPPSBuffer[1] = pps;
					}
					mGotConfigData++;
				}
				if (mGotConfigData == 2) {
					mGotConfigData = 0;
					if ((mSPSBuffer[0] == null || mPPSBuffer[0] == null) ||
							(mSPSBuffer[1].compareTo(mSPSBuffer[0]) != 0 || mPPSBuffer[1].compareTo(mPPSBuffer[0]) != 0)) {
						mSPSBuffer[0] = mSPSBuffer[1];
						mPPSBuffer[0] = mPPSBuffer[1];
						mSPSBuffer[1] = null;
						mPPSBuffer[1] = null;
						if (mConfigured) {
							CLog.i("avc codec specific data changed");
							mConfigured = false; // reconfigure the decoder
						}
					}
				}
				if (!mConfigured) {
					if (mSPSBuffer[0] == null || mPPSBuffer[0] == null) {
						return ACResult.IN_PROCESS;
					}
					if (!configureDecoder(mSPSBuffer[0], mPPSBuffer[0])) {
						return new ACResult(ACResult.ACS_UNKNOWN, "avc decoder configure failed");
					}
					mConfigured = true;
				}
				if (!extractKeyFrame(packet.buffer)) {
					return new ACResult(ACResult.ACS_INVALID_DATA, "no key frame found");
				}
				flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
			} else if (!mConfigured) {
				return new ACResult(ACResult.ACS_NOT_CONFIGURED, null);
			} else if (packet.type == ACFrameType.AC_NALU_TYPE_IDR) {
				flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
			}

			queueInputBuffer(packet.buffer, packet.timestamp, flags);
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
				CLog.e("Stop avc decoder", e);
			}
		}
		mOutputBuffer = null;
		mSPSBuffer[0] = mSPSBuffer[1] = null;
		mPPSBuffer[0] = mPPSBuffer[1] = null;
		mConfigured = false;
		return ACResult.SUCCESS;
	}

	private boolean configureDecoder(ByteBuffer sps, ByteBuffer pps) {
		MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
		format.setByteBuffer("csd-0", sps);
		format.setByteBuffer("csd-1", pps);
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

	private ByteBuffer extractSpecificDataBufferFrom(ByteBuffer data, byte type, ByteBuffer buffer) {
		int start = -1;
		int last = -1;
		int i, v, k = 0;
		byte b;
		int pos = data.position();
		int limit = data.limit();
		data.order(ByteOrder.BIG_ENDIAN);
		for (i=pos; i<limit-4; i++) {
			v = data.getInt(i);
			if (v == 0x00000001) {
				b = (byte) (data.get(i+4) & 0x1f);
				k = 4;
			} else if ((v&0xffffff00) == 0x00000100) {
				b = (byte) (data.get(i+3) & 0x1f);
				k = 3;
			} else {
				continue;
			}
			if (start != -1) {
				last = i;
				break;
			} else if (b == type) {
				start = i;
			}
			i += k;
		}
		if (start == -1) {
			return null;
		}
		if (last == -1) {
			last = limit;
		}
		int size = last - start;
		if (k == 3) {
			size++;
		}
		ByteBuffer config = (buffer==null||buffer.capacity()<size)?null:buffer;
		if (config == null) {
			config = ByteBuffer.allocate(size);
		} else {
			config.clear();
		}
		if (k == 3) {
			config.put((byte) 0);
		}
		data.limit(last);
		data.position(start);
		config.put(data);
		config.clear();
		data.limit(limit);
		data.position(last);
		return config;
	}

	private boolean extractKeyFrame(ByteBuffer buffer) {
		boolean fok = false;
		byte type;
		int i, v, k;
		buffer.order(ByteOrder.BIG_ENDIAN);
		for (i=buffer.position(); i<buffer.limit()-4;) {
			v = buffer.getInt(i);
			if (v == 0x00000001) {
				type = (byte) (buffer.get(i+4) & 0x1f);
				k = 0;
			} else if ((v&0xffffff00) == 0x00000100) {
				type = (byte) (v&0x0000001f);
				k = -1;
			} else {
				i++;
				continue;
			}
			if (type == 5) {
				if (i+k < 0) {
					break;
				}
				buffer.position(i+k);
				if (k == -1) {
					buffer.put(buffer.position(), (byte)0);
				}
				fok = true;
				break;
			}
			i += 5 + k;
		}
		return fok;
	}
	
	private void queueInputBuffer(ByteBuffer data, long timestamp, int flags) {
		int size = data.remaining();
		if (size == 0) {
			return;
		}
		int inputBufIndex = -1;
		try {
			inputBufIndex = mDecoder.dequeueInputBuffer(100000);
		} catch (IllegalStateException e) {
			CLog.e("avc decoder dequeueInputBuffer", e);
		}
		if (inputBufIndex < 0) {
			return;
		}
		ByteBuffer inputBuf = mDecoderInputBuffers[inputBufIndex];
		inputBuf.clear();
		inputBuf.put(data);
		try {
			mDecoder.queueInputBuffer(inputBufIndex, 0, size, timestamp*1000, flags);
		} catch (IllegalStateException e) {
			CLog.e("avc decoder queueInputBuffer", e);
		}
	}

	private int getDecodedFrame(ACVideoFrame frame) {
		int gotFrame = 0;
		int decoderStatus;
		while (true) {
			try {
				decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
			} catch (IllegalStateException e) {
//				CLog.e("avc decoder dequeueOutputBuffer", e);
				return -1;
			}
			if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				// no output available yet
//				CLog.d("no output from avc decoder available");
				break;
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				// The storage associated with the direct ByteBuffer may already be unmapped,
				// so attempting to access data through the old output buffer array could
				// lead to a native crash.
//				CLog.d("avc decoder output buffers changed");
				mDecoderOutputBuffers = mDecoder.getOutputBuffers();
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				// this happens before the first frame is returned
				MediaFormat decoderOutputFormat = mDecoder.getOutputFormat();
				CLog.d("avc decoder output format changed: " + decoderOutputFormat);
				mWidth = decoderOutputFormat.getInteger(MediaFormat.KEY_WIDTH);
				mHeight = decoderOutputFormat.getInteger(MediaFormat.KEY_HEIGHT);
				mColorFormat = decoderOutputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
				mStride = decoderOutputFormat.getInteger("stride");
				mSliceHeight = decoderOutputFormat.getInteger("slice-height");
			} else if (decoderStatus < 0) {
				CLog.e("unexpected result from avc deocder.dequeueOutputBuffer: " + decoderStatus);
			} else {  // decoderStatus >= 0
//				CLog.d("avc decoded buffer " + decoderStatus +
//						" (size=" + mBufferInfo.size + ", timestamp=" + mBufferInfo.presentationTimeUs/1000 + ")");
				if (mBufferInfo.size > 0) {
					gotFrame = 1;
					copy2Frame(frame, decoderStatus);
				}
				mDecoder.releaseOutputBuffer(decoderStatus, false);
				break;
			}

			if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				CLog.d("avc decoder end of stream reached");
				gotFrame = -1;
				break;
			}
		}
		return gotFrame;
	}

	private void copy2Frame(ACVideoFrame frame, int outIdx) {
		ByteBuffer outputBuffer = mDecoderOutputBuffers[outIdx];

		if (frame.buffer == null || frame.buffer.capacity() < mBufferInfo.size) {
			if (mOutputBuffer == null || mOutputBuffer.capacity() < mBufferInfo.size) {
				mOutputBuffer = ByteBuffer.allocateDirect(mBufferInfo.size);
			}
			frame.buffer = mOutputBuffer;
		}

		int pos;
		frame.buffer.clear();
		// remain the strides
		if (mSliceHeight == mHeight) {
			outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
			outputBuffer.position(mBufferInfo.offset);
			frame.buffer.put(outputBuffer);
		} else {
			outputBuffer.limit(mBufferInfo.offset + mStride*mHeight);
			outputBuffer.position(mBufferInfo.offset);
			frame.buffer.put(outputBuffer);
			pos = mBufferInfo.offset + mSliceHeight*mStride;
			outputBuffer.limit(pos + mStride*mHeight/2);
			outputBuffer.position(pos);
			frame.buffer.put(outputBuffer);
		}
		//		if (mStride == mWidth) {
		//			outputBuffer.limit(mBufferInfo.offset + mStride*mHeight);
		//			outputBuffer.position(mBufferInfo.offset);
		//			frame.buffer.put(outputBuffer);
		//		} else {
		//			for (int i=0; i<mHeight; i++) {
		//				pos = mBufferInfo.offset + i*mStride;
		//				outputBuffer.limit(pos + mWidth);
		//				outputBuffer.position(pos);
		//				frame.buffer.put(outputBuffer);
		//			}
		//		}
		switch (mColorFormat) {
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			//			if (mStride == mWidth) {
			//				pos = mBufferInfo.offset + mSliceHeight*mStride;
			//				outputBuffer.limit(pos + mStride*mHeight/2);
			//				outputBuffer.position(pos);
			//				frame.buffer.put(outputBuffer);
			//			} else {
			//				for (int i=0; i<mHeight/2; i++) {
			//					pos = mBufferInfo.offset + (mSliceHeight+i)*mStride/2;
			//					outputBuffer.limit(pos + mWidth/2);
			//					outputBuffer.position(pos);
			//					frame.buffer.put(outputBuffer);
			//				}
			//				for (int i=0; i<mHeight/2; i++) {
			//					pos = mBufferInfo.offset + (mSliceHeight+mHeight/2+i)*mStride/2;
			//					outputBuffer.limit(pos + mWidth/2);
			//					outputBuffer.position(pos);
			//					frame.buffer.put(outputBuffer);
			//				}
			//			}
			frame.format = ACImageFormat.AC_IMAGE_FMT_YUV420P;
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
		default:
			//			if (mStride == mWidth) {
			//				pos = mBufferInfo.offset + mSliceHeight*mStride;
			//				outputBuffer.limit(pos + mStride*mHeight/2);
			//				outputBuffer.position(pos);
			//				frame.buffer.put(outputBuffer);
			//			} else {
			//				for (int i=0; i<mHeight/2; i++) {
			//					pos = mBufferInfo.offset+(mSliceHeight+i)*mStride;
			//					outputBuffer.limit(pos + mWidth);
			//					outputBuffer.position(pos);
			//					frame.buffer.put(outputBuffer);
			//				}
			//			}
			frame.format = ACImageFormat.AC_IMAGE_FMT_YUV420SP;
			break;
		}

		frame.size = mStride*mHeight*3/2;
		frame.buffer.limit(frame.size);
		frame.buffer.position(0);
		frame.offset = 0;
		frame.width = mWidth;
		frame.height = mHeight;
		frame.stride = mStride;
		frame.timestamp = mBufferInfo.presentationTimeUs / 1000;
	}

}

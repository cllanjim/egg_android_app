package com.antelope.sdk.codec;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.service.ACPlatformAPI;

import android.view.Surface;

class ACVideoSoftEncoder implements ACVideoEncoder {
	private long mEncoder = 0;
	private boolean mIsInitialized=false;
	private int mFrameRate = 25;
	private int[] mImageFormat = new int[] {
		ACImageFormat.AC_IMAGE_FMT_NV21,
		ACImageFormat.AC_IMAGE_FMT_YUV420P,
		ACImageFormat.AC_IMAGE_FMT_YUV420SP
	};
	private ACPacketAvailableListener mEncodedFrameListener;
	private ACStreamPacket mEncodedPacket = new ACStreamPacket();
	
	static{
		System.loadLibrary("acencoder");
	}
	
	ACVideoSoftEncoder(long encoder) {
		mEncoder = encoder;
	}
	
	@Override
	public ACResult initialize(int width, int height, int gop, ACPacketAvailableListener listener) {
		if (mIsInitialized) {
			return ACResult.SUCCESS;
		}
		gop *= mFrameRate;
		int result = Initialize(mEncoder, width, height, gop);
		if (result == ACResult.ACS_OK) {
			mEncodedFrameListener = listener;
			mIsInitialized = true;
		}
		return new ACResult(result, null);
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
		return mImageFormat;
	}

	@Override
	public ACResult encode(ACVideoFrame frame, boolean forceKeyFrame) {
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		int result = Encode(mEncoder, frame, mEncodedPacket, forceKeyFrame);
		if (result == ACResult.ACS_OK) {
			if (mEncodedPacket.type == ACFrameType.AC_NALU_TYPE_IDR && (mEncodedPacket.buffer.get(mEncodedPacket.offset+4)&0x1f) == 0x07) {
				mEncodedPacket.type = ACFrameType.AC_NALU_TYPE_SPS;
			}
			if (mEncodedFrameListener != null) {
				mEncodedFrameListener.onPacketAvailable(mEncodedPacket);
			}
		}
		return new ACResult(result, null);
	}

	@Override
	public ACResult release() {
		int result = Release(mEncoder);
		if (!mIsInitialized) {
			return ACResult.UNINITIALIZED;
		}
		mEncodedFrameListener = null;
		mIsInitialized=false;
		return new ACResult(result, null);
	}

	@Override
	public ACResult setBitRate(int bitRate) {
		int result = SetBitRate(mEncoder, bitRate);
		return new ACResult(result, null);
	}

	@Override
	public ACResult setFrameRate(int frameRate) {
		mFrameRate = frameRate;
		int result = SetFrameRate(mEncoder, frameRate);
		return new ACResult(result, null);
	}

	@Override
	public ACResult reset() {
		int result = Reset(mEncoder);
		return new ACResult(result, null);
	}
	
	/**
	 * Create native video encoder
	 * @param codecid See {@link ACCodecID}
	 * @return Native encoder handle
	 */
	static native long Create(int codecid);
	static native int Initialize(long encoder, int width, int height, int gop);
	static native int Release(long encoder);
	static native int SetBitRate(long encoder, int bitrate);
	static native int SetFrameRate(long encoder, int framerate);
	static native int Encode(long encoder, ACVideoFrame frame, ACStreamPacket packet, boolean forceKeyFrame);
	static native int Reset(long encoder);
}

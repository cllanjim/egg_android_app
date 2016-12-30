package com.lingyang.sdk.av;

/**
 * @hide
 */
public class VideoEncoderConfig {
    protected final int mWidth;
    protected final int mHeight;
    protected final int mBitRate;
    protected final boolean mUseHardCode;
    protected final boolean mUseHardDecode;

    public VideoEncoderConfig(int width, int height, int bitRate, boolean useHardEnCode, boolean useHardDecode) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
        mUseHardCode = useHardEnCode;
        mUseHardDecode=useHardDecode;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public boolean isHardEncode() {
        return mUseHardCode;
    }
    
    public boolean isHardDecode(){
    	return mUseHardDecode;
    }

    @Override
    public String toString() {
        return "VideoEncoderConfig: " + mWidth + "x" + mHeight + " @"
                + mBitRate + " bps" + " useHardEnCode-" + mUseHardCode
                + " useHardDecode" + mUseHardDecode;
    }
}
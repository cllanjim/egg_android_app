package com.lingyang.sdk.av;

/**
 * @hide
 */
public class AudioEncoderConfig {
    protected final int mNumChannels;
    protected final int mSampleRate;
    protected final int mBitrate;
    protected final boolean mIsEncode;
    protected final int mAudioEncoderType;


    public AudioEncoderConfig(int channels, int sampleRate, int bitRate, boolean isEncode, int audioEncoderType) {
        mNumChannels = channels;
        mBitrate = bitRate;
        mSampleRate = sampleRate;
        mIsEncode = isEncode;
        mAudioEncoderType = audioEncoderType;
    }

    public int getNumChannels() {
        return mNumChannels;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getBitrate() {
        return mBitrate;
    }

    public boolean isEncode() {
        return mIsEncode;
    }

    public int getAudioEncoderType() {
        return mAudioEncoderType;
    }

    @Override
    public String toString() {
        return "AudioEncoderConfig: " + mNumChannels + " channels totaling " + mBitrate + " bps @" +
                mSampleRate + " Hz" + " audioEncoderType " + mAudioEncoderType;
    }
}
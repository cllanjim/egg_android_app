package com.lingyang.sdk.av;

import android.hardware.Camera;

import com.lingyang.sdk.encoder.AudioEncoderType;
import com.lingyang.sdk.util.Utils;

import static com.lingyang.sdk.util.Preconditions.checkArgument;
import static com.lingyang.sdk.util.Preconditions.checkNotNull;


/**
 * 配置直播流和采集相关属性，包括音视频编码等相关信息
 */
public class SessionConfig {

    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    private final VideoEncoderConfig mVideoConfig;
    private final AudioEncoderConfig mAudioConfig;
    private boolean mConvertVerticalVideo, mIsAdaptiveBitrate;
    private boolean mIsUseAudio, mIsUseVideo;
    private int mDesiredCamera;
    private int mDisplayOrientation;
    private boolean mIsUseAudioEffect = false;

    public SessionConfig() {
        mVideoConfig = new VideoEncoderConfig(640, 480, 500 , false,false);
        mAudioConfig = new AudioEncoderConfig(1, 16000, 64 , false, AudioEncoderType.AUDIO_ENCODER_TYPE_OF_AAC);
    }

    public SessionConfig(VideoEncoderConfig videoConfig, AudioEncoderConfig audioConfig) {
        mVideoConfig = checkNotNull(videoConfig);
        mAudioConfig = checkNotNull(audioConfig);
    }

    public int getTotalBitrate() {
        return mVideoConfig.getBitRate() + mAudioConfig.getBitrate();
    }

    public int getVideoWidth() {
        return mVideoConfig.getWidth();
    }

    public VideoEncoderConfig getVideoEncoderConfig() {
        return mVideoConfig;
    }

    public AudioEncoderConfig getAudioEncoderConfig() {
        return mAudioConfig;
    }
    
    public boolean isUseHardVideoDecode(){
    	return mVideoConfig.isHardDecode();
    }

    public int getVideoHeight() {
        return mVideoConfig.getHeight();
    }

    public int getVideoBitrate() {
        return mVideoConfig.getBitRate();
    }

    public int getNumAudioChannels() {
        return mAudioConfig.getNumChannels();
    }

    public int getAudioBitrate() {
        return mAudioConfig.getBitrate();
    }

    public int getAudioSampleRateInHz() {
        return mAudioConfig.getSampleRate();
    }

    public int getDesiredCamera() {
        return mDesiredCamera;
    }

    public void setDesiredCamera(int camera) {
        mDesiredCamera = camera;
    }

    public boolean isUseAudioEffect() {
        return mIsUseAudioEffect;
    }

    public void setUseAudioEffect(boolean use) {
        mIsUseAudioEffect = use;
    }

    public int getCameraDisplayOrientation() {
        return mDisplayOrientation;
    }

    public void setCameraDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
    }

    public boolean isUseAudio() {
        return mIsUseAudio;
    }

    public void setUseAudio(boolean use) {
        mIsUseAudio = use;
    }

    public boolean isUseVideo() {
        return mIsUseVideo;
    }

    public void setUseVideo(boolean use) {
        mIsUseVideo = use;
    }

    public boolean isAdaptiveBitrate() {
        return mIsAdaptiveBitrate;
    }

  /*  public boolean isConvertingVerticalVideo() {
        return mConvertVerticalVideo;
    }*/

    public void setUseAdaptiveBitrate(boolean useAdaptiveBit) {
        mIsAdaptiveBitrate = useAdaptiveBit;
    }

    public void setConvertVerticalVideo(boolean convertVerticalVideo) {
        mConvertVerticalVideo = convertVerticalVideo;
    }

    public static class Builder {
        SessionConfig session;
        private int mWidth;
        private int mHeight;
        private int mVideoBitrate;
        private boolean mUseAudio, mUseVideo;
        private int mAudioSamplerate;
        private int mAudioBitrate;
        private int mNumAudioChannels;
        private int mAudioEncoderType;
        private int mDesiredCamera;
        private int mDisplayOrientation;
        private boolean mConvertVerticalVideo, mAdaptiveStreaming,
                mIsUseHardAudioEncode, mIsUseHardVideoEncode, mUseAudioEffect,
                mIsUseHardVideoDecode;

        public Builder() {
            setAVDefaults();
            setMetaDefaults();
        }

        private void setAVDefaults() {
            mWidth = 640;
            mHeight = 480;
            mVideoBitrate = 500 ;

            mUseAudio = true;
            mUseVideo = true;
            mAudioSamplerate = 16000;
            mAudioBitrate = 60 ;
            mNumAudioChannels = 1;
            mAudioEncoderType = AudioEncoderType.AUDIO_ENCODER_TYPE_OF_AAC;

            this.mIsUseHardAudioEncode = false;
            this.mIsUseHardVideoEncode = false;
            this.mUseAudioEffect = false;
            this.mIsUseHardVideoDecode=false;
            this.mDesiredCamera = CAMERA_FACING_BACK;
            mDisplayOrientation = 90;
        }

        private void setMetaDefaults() {
            mAdaptiveStreaming = Utils.isKitKat();
            mConvertVerticalVideo = false;
        }

        public Builder withAdaptiveBitrate(boolean adaptiveStreaming) {
            mAdaptiveStreaming = adaptiveStreaming;
            return this;
        }

        /**
         * 设置图像的旋转角度
         *
         * @param displayOrientation 只能是 0,90,180,270
         * @return
         */
        public Builder withCameraDisplayOrientation(int displayOrientation) {
            checkArgument(displayOrientation == 0 || displayOrientation == 90
                    || displayOrientation == 180 || displayOrientation == 270);
            mDisplayOrientation = displayOrientation;
            return this;
        }

       /* public Builder withVerticalVideoCorrection(boolean convertVerticalVideo) {
            mConvertVerticalVideo = convertVerticalVideo;
            return this;
        }*/

        /**
         * 是否需要使用音频
         *
         * @param use
         * @return
         */
        public Builder useAudio(boolean use) {
            this.mUseAudio = use;
            return this;
        }

        /**
         * 是否需要使用视频
         *
         * @param use
         * @return
         */
        public Builder useVideo(boolean use) {
            this.mUseVideo = use;
            return this;
        }

        /**
         * 期望摄像机
         *
         * @param camera {@link #CAMERA_FACING_FRONT} or {@link #CAMERA_FACING_BACK}
         * @return
         */
        public Builder withDesireadCamera(int camera) {
            this.mDesiredCamera = camera;
            return this;
        }

        /**
         * 设置视频宽高
         *
         * @param width  宽
         * @param height 高
         * @return
         */
        public Builder withVideoResolution(int width, int height) {
            mWidth = width;
            mHeight = height;
            return this;
        }

        /**
         * 使用视频硬编码
         *
         * @param encode
         * @return
         */
        public Builder useHardVideoEncode(boolean encode) {
            this.mIsUseHardVideoEncode = encode;
            return this;
        }

        /**
         * 使用音频硬编码
         *
         * @param encode
         * @return
         */
        public Builder useHardAudioEncode(boolean encode) {
            this.mIsUseHardAudioEncode = encode;
            return this;
        }
        
        public Builder useHardVideoDecode(boolean decode){
        	this.mIsUseHardVideoDecode=decode;
        	return this;
        }

        /**
         * 设置音频编码类型
         *
         * @param audioEncoderType {@see #com.lingyang.sdk.encoder.AudioEncoderType.AUDIO_ENCODER_TYPE_OF_AAC}
         *                         {@see #com.lingyang.sdk.encoder.AudioEncoderType.AUDIO_ENCODER_TYPE_OF_OPUS}
         */
        public Builder setmAudioEncoderType(int audioEncoderType) {
            this.mAudioEncoderType = audioEncoderType;
            return this;
        }

        /**
         * 使用视频硬编码
         *
         * @param use
         * @return
         */
        public Builder useAudioEffect(boolean use) {
            this.mUseAudioEffect = use;
            return this;
        }

        /**
         * 设置视频码率
         *
         * @param bitrate
         * @return
         */
        public Builder withVideoBitrate(int bitrate) {
            mVideoBitrate = bitrate;
            return this;
        }

        /**
         * 设置音频采样频率
         *
         * @param samplerate 采样频率
         * @return
         */
        public Builder withAudioSampleRateInHz(int samplerate) {
            mAudioSamplerate = samplerate;
            return this;
        }

        /**
         * 设置音频采样码率
         *
         * @param bitrate 码率 单位bpf
         * @return
         */
        public Builder withAudioBitrate(int bitrate) {
            mAudioBitrate = bitrate;
            return this;
        }

        /**
         * 设置省道数
         *
         * @param numChannels 1代表单声道，2代表立体声
         * @return
         */
        public Builder withAudioChannels(int numChannels) {
            checkArgument(numChannels == 1 || numChannels == 2);
            mNumAudioChannels = numChannels;
            return this;
        }

        public SessionConfig build() {
            session = new SessionConfig(
                    new VideoEncoderConfig(mWidth, mHeight, mVideoBitrate, mIsUseHardVideoEncode,mIsUseHardVideoDecode),
                    new AudioEncoderConfig(mNumAudioChannels, mAudioSamplerate, mAudioBitrate,
                            mIsUseHardAudioEncode, mAudioEncoderType));
            session.setUseAdaptiveBitrate(this.mAdaptiveStreaming);
            session.setConvertVerticalVideo(this.mConvertVerticalVideo);
            session.setUseAudio(this.mUseAudio);
            session.setUseVideo(this.mUseVideo);
            session.setUseAudioEffect(mUseAudioEffect);
            session.setDesiredCamera(this.mDesiredCamera);
            session.setCameraDisplayOrientation(mDisplayOrientation);
            return session;
        }


    }
}

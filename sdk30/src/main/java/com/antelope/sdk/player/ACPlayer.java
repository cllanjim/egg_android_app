package com.antelope.sdk.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.antelope.sdk.ACMediaInfo;
import com.antelope.sdk.ACMediaType;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.capturer.ACImageFormat;
import com.antelope.sdk.capturer.ACSampleFormat;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.codec.ACAACHardDecoder;
import com.antelope.sdk.codec.ACAACSoftDecoder;
import com.antelope.sdk.codec.ACAVCHardDecoder;
import com.antelope.sdk.codec.ACAVCSoftDecoder;
import com.antelope.sdk.codec.ACAudioDecoder;
import com.antelope.sdk.codec.ACCodecID;
import com.antelope.sdk.codec.ACFrameType;
import com.antelope.sdk.codec.ACOpusHardDecoder;
import com.antelope.sdk.codec.ACOpusSoftDecoder;
import com.antelope.sdk.codec.ACStreamPacket;
import com.antelope.sdk.codec.ACVideoDecoder;
import com.antelope.sdk.service.ACPlatformAPI;
import com.antelope.sdk.utils.CLog;
import com.antelope.sdk.utils.ImageConvertor;
import com.antelope.sdk.utils.RawFramePipe;
import com.antelope.sdk.utils.StreamPacketPipe;
import com.antelope.sdk.utils.WorkThreadExecutor;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaCodec.BufferInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 播放器
 * @author weil
 */
public class ACPlayer {
	/**
	 * 初始化标志
	 */
	private boolean mInitialized = false;
	/**
	 * 视频解码器
	 */
	private ACVideoDecoder mVideoDecoder = null;
	/**
	 * 音频解码器
	 */
	private ACAudioDecoder mAudioDecoder = null;
	/**
	 * 开始播放缓冲时长，单位毫秒
	 */
	private int mStartPlayBufferSize = 1000;
	/**
	 * 开始丢帧缓冲时长，单位毫秒
	 */
	private int mStartDropBufferSize = 5000;
	/**
	 * 是否显示视频
	 */
	private boolean mIsShowVideo = true;
	/**
	 * 是否静音
	 */
	private boolean mIsMute = false;
	/**
	 * 是否工作
	 */
	private boolean mIsWorking = false;
	/**
	 * 播放状态
	 */
	private boolean mIsPlaying = true;
	/**
	 * 视频数据包管道
	 */
	private StreamPacketPipe mVideoPacketPipe = null;
	/**
	 * 音频数据包管道
	 */
	private StreamPacketPipe mAudioPacketPipe = null;
	/**
	 * 一帧视频数据和信息缓存
	 */
	private ByteBuffer mVideoFrameBuffer = null;
	/**
	 * 一帧音频数据和信息缓存
	 */
	private ByteBuffer mAudioFrameBuffer = null;
	/**
	 * 媒体数据处理扩展接口
	 */
	private ACMediaExtra mMediaExtra = null;
	/**
	 * 渲染类
	 */
	private ACVideoRenderer mVideoRenderer = null;
	/**
	 * 音频播放器
	 */
	private AudioTrack mAudioTrack = null;
	/**
	 * 音频播放帧大小
	 */
	private int mAudioPlayTrunkSize = 0;
	/**
	 * 本地录像文件
	 */
	private MediaMuxer mMuxerRecord = null;
	private Object mSyncObjRecord = new Object();
	private boolean mRecording = false;
	private boolean mRecordStarting = false;
	private int mVideoTrackIndex = -1;
	private int mAudioTrackIndex = -1;
	private MediaFormat mVideoFormat = null;
	private MediaFormat mAudioFormat = null;
	private BufferInfo mBufferInfo = new BufferInfo();
	private boolean mSaveVideo = false;
	private boolean mSaveAudio = false;
	/**
	 * 截图
	 */
	private Object mSyncObjSnapshot = new Object();
	private ACVideoFrame mVideoFrame4Snapshot = new ACVideoFrame();
	private boolean mRequestingSnapshot = false;
	private boolean mCopyVideoFrameSuccess = false;
	private OutputStream mSnapshotOutputStream = null;
	/**
	 * 清空缓存
	 */
	private Object mSyncObjClearVideoBuffer = new Object();
	private Object mSyncObjClearAudioBuffer = new Object();
	private boolean mIsClearVideoBuffer = false;
	private boolean mIsClearAudioBuffer = false;
	/**
	 * 暂停/恢复播放
	 */
	private Object mSyncObjPauseResume = new Object();
	private int mResetVideoTimestamp = 0;
	private boolean mResetAudioTimestmap = true;
	private boolean mIsClearBuffer = false;
	/**
	 * 缓存时长
	 */
	private long mFirstFrameTimestamp = -1;
	private long mLastestFrameTimestamp = -1;
	private boolean mResetFrameTimestamp = true;
	private boolean mResetDelayTime = false;
	/**
	 * 回滚
	 */
	private long mLastVideoTimestamp = -1;
	private long mLastAudioTimestamp = -1;
	private long mLatestAudioTimestamp = -1;
	/**
	 * 播放延时
	 */
	private int mBufferDelaySize = 1000;
	private int mAudioDelayTime = 0;
	/**
	 * 丢帧
	 */
//	private boolean mStartDroppingFrame = false;
	/**
	 * 音视频同步
	 */
//	private SynchronizeVideoWithAudio mSynchronizeVideoWithAudio = new SynchronizeVideoWithAudio();
	/**
	 * 播放缓存控制
	 */
//	private ConditionVariable mConditionVariable = new ConditionVariable(true);
	/**
	 * 应用进后台
	 */
	private boolean mEnterBackground = false;
	/**
	 * 视频播放线程
	 */
	private WorkThreadExecutor mVideoWorkThread = null;
	/**
	 * 音频播放线程
	 */
	private WorkThreadExecutor mAudioWorkThread = null;
	/**
	 * 统计信息
	 */
	private MediaInfo mMediaInfo = new MediaInfo();

	
	public ACPlayer() {
		
	}
	
	private ACResult initVideoDecoder(int codecId) {
		ACVideoDecoder decoder = null;
		if (codecId == ACCodecID.AC_CODEC_ID_H264) {
			decoder = new ACAVCHardDecoder();
			if (!decoder.initialize().isResultOK()) {
				CLog.i("failed to initialize video/avc decoder, try native decoder");
				ACNativeLibraryLoader.TRIGGER = 1;
				if (!ACNativeLibraryLoader.ACDECODER_LOADED) {
					return new ACResult(ACResult.ACS_NOT_LOADED, "the native decode library not loaded");
				}
				decoder = new ACAVCSoftDecoder();
				if (!decoder.initialize().isResultOK()) {
					return new ACResult(ACResult.ACS_UNKNOWN, "failed to initialize native avc decoder");
				}
			}
		} else {
			return new ACResult(ACResult.ACS_UNIMPLEMENTED, "unimplemented video codec");
		}
		mVideoDecoder = decoder;
		return ACResult.SUCCESS;
	}
	
	private ACResult initAudioDecoder(int codecId, int sampleRate, int channelCount) {
		ACAudioDecoder decoder = null;
		if (codecId == ACCodecID.AC_CODEC_ID_AAC) {
			decoder = new ACAACHardDecoder();
			if (!decoder.initialize(sampleRate, channelCount).isResultOK()) {
				CLog.i("failed to initialize audio/mp4a-latm decoder, try native decoder");
				ACNativeLibraryLoader.TRIGGER = 1;
				if (!ACNativeLibraryLoader.ACDECODER_LOADED) {
					return new ACResult(ACResult.ACS_NOT_LOADED, "the native decode library not loaded");
				}
				decoder = new ACAACSoftDecoder();
				if (!decoder.initialize(sampleRate, channelCount).isResultOK()) {
					return new ACResult(ACResult.ACS_UNKNOWN, "failed to initialize native aac decoder");
				}
			}
		} else if (codecId == ACCodecID.AC_CODEC_ID_OPUS) {
			decoder = new ACOpusHardDecoder();
			if (!decoder.initialize(sampleRate, channelCount).isResultOK()) {
				CLog.i("failed to initialize audio/opus decoder, try native decoder");
				ACNativeLibraryLoader.TRIGGER = 1;
				if (!ACNativeLibraryLoader.ACDECODER_LOADED) {
					return new ACResult(ACResult.ACS_NOT_LOADED, "the native decode library not loaded");
				}
				decoder = new ACOpusSoftDecoder();
				if (!decoder.initialize(sampleRate, channelCount).isResultOK()) {
					return new ACResult(ACResult.ACS_UNKNOWN, "failed to initialize native opus decoder");
				}
			}
		} else {
			return new ACResult(ACResult.ACS_UNIMPLEMENTED, "unimplemented audio codec");
		}
		mAudioDecoder = decoder;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 初始化
	 * @param startPlayBufferSize 开始播放缓冲时长，单位毫秒
	 * @param startDropBufferSize 开始丢帧缓冲时长，即播放器最大的缓冲时长，单位为毫秒
	 * @param videoCodecId 视频解码类型，详细定义请参考{@link com.antelope.sdk.codec.ACCodecID}
	 * @param audioCodecId 音频解码类型，详细定义请参考{@link com.antelope.sdk.codec.ACCodecID}
	 * @param sampleRate 音频播放采集率，单位HZ
	 * @param channelCount 音频声道数
	 * @param mediaExtra 高级数据处理扩展接口
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult initialize(int startPlayBufferSize, int startDropBufferSize, int videoCodecId, int audioCodecId, int sampleRate, int channelCount, ACMediaExtra mediaExtra) {
		if (startPlayBufferSize < 0 || startDropBufferSize < 0 || (startDropBufferSize > 0 && startPlayBufferSize >= startDropBufferSize)) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid buffer size");
		}
		if (!ACPlatformAPI.hasAuthorize()){
			return ACResult.NO_AUTHORIZATION; 
		}
		ACResult result = initVideoDecoder(videoCodecId);
		boolean videoDecoderOk = false;
		if (result.isResultOK()) {
			videoDecoderOk = true;
		}
		boolean audioDecoderOk = false;
		result = initAudioDecoder(audioCodecId, sampleRate, channelCount);
		if (result.isResultOK()) {
			audioDecoderOk = true;
		}
		if (!videoDecoderOk && !audioDecoderOk) {
			return new ACResult(ACResult.ACS_UNKNOWN, "neither video nor audio decoder was prepared");
		}
		
		mStartPlayBufferSize = startPlayBufferSize;
		mBufferDelaySize = startPlayBufferSize;
		mStartDropBufferSize = startDropBufferSize;
		if (videoDecoderOk) {
			mVideoPacketPipe = new StreamPacketPipe(32*1024*1024);
			mVideoPacketPipe.allocate(2*1024*1024);
			mVideoWorkThread = new WorkThreadExecutor("WorkThread-ACPlayer-Video");
		}
		if (audioDecoderOk) {
			mAudioPacketPipe = new StreamPacketPipe(4*1024*1024);
			mAudioPacketPipe.allocate(256*1024);
			mAudioWorkThread = new WorkThreadExecutor("WorkThread-ACPlayer-Audio");
		}
		mMediaExtra = mediaExtra;
		mIsWorking = true;
		if (mVideoWorkThread != null) {
			mVideoWorkThread.start(mVideoMessageCallback);
			mVideoWorkThread.executeTask(mVideoPlayRunnable);
		}
		if (mAudioWorkThread != null) {
			mAudioWorkThread.start(mAudioMessageCallback);
			mAudioWorkThread.executeTask(mAudioPlayRunnable);
		}
		mMediaInfo.start();
		mMediaInfo.setDelayTime(mBufferDelaySize);
		mInitialized = true;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 释放资源
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult release() {
		if (!mInitialized) {
			return ACResult.UNINITIALIZED;
		}
		mIsWorking = false;
//		mConditionVariable.open();
		if (mVideoPacketPipe != null) {
			mVideoPacketPipe.cancel();
		}
		if (mAudioPacketPipe != null) {
			mAudioPacketPipe.cancel();
		}
		if (mVideoWorkThread != null) {
			mVideoWorkThread.stop();
			mVideoWorkThread.release();
			mVideoWorkThread = null;
		}
		mVideoPacketBuffer = null;
		mVideoPacket = null;
		mVideoFrame = null;
		if (mAudioWorkThread != null) {
			mAudioWorkThread.stop();
			mAudioWorkThread.release();
			mAudioWorkThread = null;
		}
		stopAudioTrack();
		mAudioPacketBuffer = null;
		mAudioPacket = null;
		mAudioFrame = null;
		if (mVideoDecoder != null) {
			mVideoDecoder.release();
			mVideoDecoder = null;
		}
		if (mAudioDecoder != null) {
			mAudioDecoder.release();
			mAudioDecoder = null;
		}
		synchronized (mSyncObjRecord) {
			if (mMuxerRecord != null) {
				try {
					mMuxerRecord.stop();
					mMuxerRecord.release();
				} catch (IllegalStateException e) {
					// TODO: handle exception
				}
				mMuxerRecord = null;
			}
			mAudioTrackIndex = -1;
			mAudioFormat = null;
			mRecording = false;
		}
		if (mVideoPacketPipe != null) {
			mVideoPacketPipe.release();
			mVideoPacketPipe = null;
		}
		if (mAudioPacketPipe != null) {
			mAudioPacketPipe.release();
			mAudioPacketPipe = null;
		}
		if (mMediaInfo != null) {
			mMediaInfo.stop();
		}
		mVideoFrameBuffer = null;
		mAudioFrameBuffer = null;
		mVideoRenderer = null;
		mMediaExtra = null;
		mInitialized = false;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 设置播放窗口
	 * @param view 播放窗口类
	 * @param shape 播放器形状，{@link com.antelope.sdk.capturer.ACShape}
	 * @param args 播放器形状参数，视具体的形状而定
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult setPlaySurfaceView(GLSurfaceView view, int shape, Object... args) {
		if (view == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "view should not be null");
		} else {
			if (mVideoRenderer == null) {
				mVideoRenderer = new ACVideoRenderer(view, ACImageFormat.AC_IMAGE_FMT_YUV420P, ACVideoRenderer.GLSM_SCALE_MODE_SCREEN);
			}
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 应用进后台后调用
	 */
	public void enterBackground() {
		mEnterBackground = true;
		if (mVideoRenderer != null) {
			mVideoRenderer.enterBackground();
		}
	}
	
	/**
	 * 应用启动或从后台恢复后调用
	 */
	public void enterForeground() {
		if (mVideoRenderer != null) {
			mVideoRenderer.enterForeground();
		}
		mEnterBackground = false;
	}
	
	/**
	 * 播放一帧数据
	 * @param packet 一帧数据
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult playFrame(ACStreamPacket packet) {
		if (!mInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (packet.type == ACFrameType.AC_NALU_TYPE_IDR || packet.type == ACFrameType.AC_NALU_TYPE_SLICE || packet.type == ACFrameType.AC_NALU_TYPE_SPS || packet.type == ACFrameType.AC_NALU_TYPE_PPS) {
			if (mVideoDecoder == null) {
				return ACResult.UNINITIALIZED;
			}
			if (!writeVideoStreamPacketToPipe(packet)) {
				return new ACResult(ACResult.ACS_INSUFFICIENT_BUFFER, "buffer is full");
			}
		} else if (packet.type == ACFrameType.AC_AAC_TYPE_INFO || packet.type == ACFrameType.AC_AAC_TYPE_SAMPLE || packet.type == ACFrameType.AC_OPUS_TYPE_SAMPLE) {
			if (mAudioDecoder == null) {
				return ACResult.UNINITIALIZED;
			}
			if (!writeAudioStreamPacketToPipe(packet)) {
				return new ACResult(ACResult.ACS_INSUFFICIENT_BUFFER, "buffer is full");
			}
		} else {
			return new ACResult(ACResult.ACS_UNKNOWN, "Unknown frame type");
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 静音
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult mute() {
		mIsMute = true;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 播放声音
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult unmute() {
		mIsMute = false;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 显示视频
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult showVideo() {
		mIsShowVideo = true;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 关闭视频
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult hideVideo() {
		mIsShowVideo = false;
		return ACResult.SUCCESS;
	}
	
	/**
	 * 恢复播放
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult resume() {
		if (!mInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (mIsWorking && !mIsPlaying) {
			mIsPlaying = true;
			mResetVideoTimestamp = 1;
			mResetAudioTimestmap = true;
			mVideoWorkThread.executeTask(mVideoPlayRunnable);
			mAudioWorkThread.executeTask(mAudioPlayRunnable);
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 暂停播放
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult pause() {
		if (!mInitialized) {
			return ACResult.UNINITIALIZED;
		}
		if (mIsPlaying) {
			mIsPlaying = false;
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 获取媒体播放信息
	 * @param info 需要获取的信息 {@link ACMediaInfo}
	 * @return 媒体信息
	 */
	public String getMediaInfo(int info) {
		if (!mInitialized) {
			return null;
		}
		String infoStr = null;
		switch (info) {
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_VIDEO_BITRATE:
			infoStr = Integer.toString(mMediaInfo.getVideoBitRate());
			break;
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_AUDIO_BITRATE:
			infoStr = Integer.toString(mMediaInfo.getAudioBitRate());
			break;
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_VIDEO_FRAMERATE:
			infoStr = Integer.toString(mMediaInfo.getVideoFrameRate());
			break;
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_AUDIO_FRAMERATE:
			infoStr = Integer.toString(mMediaInfo.getAudioFrameRate());
			break;
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_BUFFER_TIME:
			infoStr = Integer.toString(mMediaInfo.getBufferLength());
			break;
		case ACMediaInfo.AC_MEDIA_INFO_PLAYER_DELAY_TIME:
			infoStr = Integer.toString(mMediaInfo.getDelayTime());
			break;
		default:
			break;
		}
		return infoStr;
	}
	
	/**
	 * 获取当前播放位置（录像回放）
	 * @return 播放位置
	 */
	public long getPosition() {
		return mFirstFrameTimestamp;
	}
	
	/**
	 * 截图
	 * @param path 截图保存路径
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult snapshot(String path) {
		if (path == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, null);
		}
		if (path.charAt(0) != '/') {
			return new ACResult(ACResult.ACS_INVALID_ARG, "path should be absolute directory");
		}
		int lastSlashIndex = path.lastIndexOf('/');
		String dir = path.substring(0, lastSlashIndex);
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				return new ACResult(ACResult.ACS_FILE_NOT_FOUND, "unable to create directory for snap file");
			}
		}
		synchronized (mSyncObjSnapshot) {
			//TODO: take a better snapshot
			// take the latest frame instead
			// that's not a good idea
			// but we have no choice
//			mRequestingSnapshot = true;
//			mCopyVideoFrameSuccess = false;
//			try {
//				mSyncObjSnapshot.wait(5000);
//			} catch (InterruptedException e) {
//				mRequestingSnapshot = false;
//			}
			if (!mCopyVideoFrameSuccess) {
//				mRequestingSnapshot = false;
//				return new ACResult(ACResult.ACS_RESUQEST_TIMEDOUT, null);
				return new ACResult(ACResult.ACS_NOT_READY, "video frame not ready for now, please try again later");
			}
			try {
				mSnapshotOutputStream = new FileOutputStream(path);
			} catch (FileNotFoundException e1) {
				return new ACResult(ACResult.ACS_FILE_NOT_FOUND, "file cannot be opened for writing");
			}
			byte[] data = new byte[mVideoFrame4Snapshot.size];
			int[] strides = new int[4];
			mVideoFrame4Snapshot.buffer.limit(mVideoFrame4Snapshot.offset + mVideoFrame4Snapshot.size);
			mVideoFrame4Snapshot.buffer.position(mVideoFrame4Snapshot.offset);
			switch (mVideoFrame4Snapshot.format) {
			case ACImageFormat.AC_IMAGE_FMT_YUV420P:
				strides[0] = mVideoFrame4Snapshot.stride;
				strides[1] = strides[0]/2;
				strides[2] = strides[0]/2;
				ImageConvertor.convertYUV420PToNV21(mVideoFrame4Snapshot.buffer.array(), mVideoFrame4Snapshot.width, mVideoFrame4Snapshot.height, strides, data);
				break;
			case ACImageFormat.AC_IMAGE_FMT_YUV420SP:
				strides[0] = mVideoFrame4Snapshot.stride;
				strides[1] = strides[0];
				ImageConvertor.convertYUV420SPToNV21(mVideoFrame4Snapshot.buffer.array(), mVideoFrame4Snapshot.width, mVideoFrame4Snapshot.height, strides, data);
				break;
			case ACImageFormat.AC_IMAGE_FMT_NV21:
			default:
				mVideoFrame4Snapshot.buffer.get(data);
				break;
			}
			// Currently only ImageFormat.NV21 and ImageFormat.YUY2 are supported
			YuvImage image = new YuvImage(data, ImageFormat.NV21, mVideoFrame4Snapshot.width, mVideoFrame4Snapshot.height, null);
			Rect rect = new Rect(0, 0, mVideoFrame4Snapshot.width, mVideoFrame4Snapshot.height);
			boolean compress = image.compressToJpeg(rect, 75, mSnapshotOutputStream);
			try {
				mSnapshotOutputStream.close();
			} catch (IOException e1) {
			}
//			mRequestingSnapshot = false;
			if (!compress) {
				return new ACResult(ACResult.ACS_UNKNOWN, "compress to jpeg failed");	
			}
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 开始本地录像
	 * @param path 录像保存路径
	 * @param flags 音视频类型标志，详细定义请参考 {@link ACMediaType}，如果音视频同时录，传入AC_MEDIA_TYPE_VIDEO | AC_MEDIA_TYPE_AUDIO
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult startRecord(String path, int flags) {
		if (path == null) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "path is null");
		}
		
		boolean saveVideo = false;
		boolean saveAudio = false;
		if ((flags&ACMediaType.AC_MEDIA_TYPE_VIDEO) != 0) {
			saveVideo = true;
		}
		if ((flags&ACMediaType.AC_MEDIA_TYPE_AUDIO) != 0) {
			saveAudio = true;
		}
		if (!saveVideo && !saveAudio) {
			return new ACResult(ACResult.ACS_INVALID_ARG, "invalid flags");
		}
		
		if (path.charAt(0) != '/') {
			return new ACResult(ACResult.ACS_INVALID_ARG, "path should be absolute directory");
		}
		int lastSlashIndex = path.lastIndexOf('/');
		String dir = path.substring(0, lastSlashIndex);
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				return new ACResult(ACResult.ACS_FILE_NOT_FOUND, "unable to create directory for record file");
			}
		}
		synchronized (mSyncObjRecord) {
			if (mRecording) {
				return ACResult.IN_PROCESS;
			}
			try {
				mMuxerRecord = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			} catch (IOException e) {
				CLog.e("failed to open record file", e);
				return new ACResult(ACResult.ACS_FILE_NOT_FOUND, "failed to open record file");
			}
			mSaveVideo = saveVideo;
			mSaveAudio = saveAudio;
			mRecording = true;
		}
		return ACResult.SUCCESS;
	}
	
	/**
	 * 停止本地录像
	 * @return 成功或失败错误码 {@link ACResult}
	 */
	public ACResult stopRecord() {
		ACResult result = ACResult.SUCCESS;
		synchronized (mSyncObjRecord) {
			if (!mRecording) {
				return new ACResult(ACResult.ACS_UNINITIALIZED, "record wasn't starting");
			}
			if (mRecordStarting) {
				try {
					mMuxerRecord.stop();
				} catch (IllegalStateException e) {
					CLog.e("failed to stop recording", e);
					result = new ACResult(ACResult.ACS_ILLEGAL_STATE, "failed to stop recording");
				}
			} else {
				result = new ACResult(ACResult.ACS_NO_DATA, "no data had been wrote to the record file");
			}
			mMuxerRecord.release();
			mRecording = false;
			mRecordStarting = false;
			mMuxerRecord = null;
			mVideoFormat = null;
			mVideoTrackIndex = -1;
			mAudioFormat = null;
			mAudioTrackIndex = -1;
		}
		return result;
	}
	
	/**
	 * 清理播放器缓存
	 */
	public void clearQueueBuffer() {
		if (!mInitialized) {
			return;
		}
		if (mVideoWorkThread != null) {
			mIsClearVideoBuffer = true;
			mVideoPacketPipe.cancel();
			mVideoWorkThread.sendEmptyMessage(MESSAGE_CLEAR_CACHE);
		}
		if (mAudioWorkThread != null) {
			mIsClearAudioBuffer = true;
			mAudioPacketPipe.cancel();
			mAudioWorkThread.sendEmptyMessage(MESSAGE_CLEAR_CACHE);
		}
//		mConditionVariable.open();
//		synchronized (mSyncObjPauseResume) {
//			mIsClearBuffer = true;
//			if (!mIsPlaying) {
//				mSyncObjPauseResume.notifyAll();
//			}
//		}
//		if (mVideoPacketPipe != null) {
//			mVideoPacketPipe.clear();
//			synchronized (mSyncObjClearVideoBuffer) {
//				mIsClearVideoBuffer = true;
//				mVideoPacketPipe.cancel();
//				try {
//					mSyncObjClearVideoBuffer.wait();
//				} catch (InterruptedException e) {
//				}
//				mIsClearVideoBuffer = false;
//			}
//		}
//		if (mAudioPacketPipe != null) {
//			mAudioPacketPipe.clear();
//			synchronized (mSyncObjClearAudioBuffer) {
//				mIsClearAudioBuffer = true;
//				mAudioPacketPipe.cancel();
//				try {
//					mSyncObjClearAudioBuffer.wait();
//				} catch (InterruptedException e) {
//				}
//				mIsClearAudioBuffer = false;
//			}
//		}
//		synchronized (mSyncObjPauseResume) {
//			mIsClearBuffer = false;
//		}
		mResetVideoTimestamp = 0;
		mResetAudioTimestmap = true;
		mResetFrameTimestamp = true;
		mMediaInfo.reset();
	}
	
	private synchronized boolean writeVideoStreamPacketToPipe(ACStreamPacket packet) {
		if (mVideoFrameBuffer == null || mVideoFrameBuffer.capacity() < packet.getPacketSize()) {
			mVideoFrameBuffer = ByteBuffer.allocateDirect(packet.getPacketSize());
			mVideoFrameBuffer.order(ByteOrder.BIG_ENDIAN);
		}
//		CLog.d("write video stream packet: size=" + packet.getPacketSize() + ", nalu=" + Integer.toHexString((packet.buffer.get(packet.offset+4)&0xff)));
		if (mResetFrameTimestamp) {
			mFirstFrameTimestamp = packet.timestamp;
			mLastestFrameTimestamp = packet.timestamp;
			mResetFrameTimestamp = false;
		}
		long timestamp = packet.timestamp;
		// 如果当前帧的时间戳和前一帧时间戳差值小于1分钟就认为是重复的数据，应该丢弃（录像）
		// 否则认为是时间戳回滚（RTMP 3字节回滚)
		if (timestamp < mLastestFrameTimestamp) {
			if (mLastestFrameTimestamp - timestamp < 60000) {
				return true;
			}
			if (mLastVideoTimestamp == -1) {
				timestamp = mLastestFrameTimestamp + 10;
			} else {
				if (packet.timestamp <= mLastVideoTimestamp) {
					mLastVideoTimestamp = packet.timestamp + 10;
				}
				timestamp = (packet.timestamp - mLastVideoTimestamp) + mLastestFrameTimestamp;
			}
//			CLog.i("set packet timestmap=" + timestamp + ", old=" + packet.timestamp + ", last=" + mLastVideoTimestamp);
			mLastVideoTimestamp = packet.timestamp;
			packet.timestamp = timestamp;
		} else {
			mLastVideoTimestamp = -1;
		}
		
		if (mVideoPacketPipe.cache() == 0 && mStartPlayBufferSize > 0) {
//			CLog.i("video cache is empty");
			mResetDelayTime = true;
		}
		
		if (mStartDropBufferSize > 0 && timestamp - mFirstFrameTimestamp > mStartDropBufferSize) {
			int drop = (int) (timestamp - mFirstFrameTimestamp) - mStartDropBufferSize;
			if (drop > (mLastestFrameTimestamp-mFirstFrameTimestamp)) {
				CLog.w("invalid timestamp: " + mFirstFrameTimestamp + " - " + timestamp);
				drop = 0;
			}
			if (!mResetDelayTime) {
				mVideoWorkThread.sendMessageAndWaitForResult(MESSAGE_START_DROPPING, drop, 0);
			}
		} else if (mVideoPacketPipe.remaining() < packet.getPacketSize()) {
			int capacity = packet.getPacketSize() - mVideoPacketPipe.remaining();
			if (capacity < 256*1024) {
				capacity = 256*1024;
			}
			capacity += mVideoPacketPipe.capacity();
			if (!mVideoPacketPipe.allocate(capacity)) {
//				CLog.e("allocate memory for video packet failed, we may be run out of memory or exceed max size");
				return false;
			}
		}
		
		packet.writeTo(mVideoFrameBuffer, 0);
		mVideoFrameBuffer.limit(packet.getPacketSize());
		mVideoFrameBuffer.position(0);
		if (!mVideoPacketPipe.write(mVideoFrameBuffer, 2000)) {
			CLog.e("write a video stream packet to packet pipe failed");
			return false;
		}
		mLastestFrameTimestamp = timestamp;
		mMediaInfo.setLastestTimestamp(timestamp);
//		CLog.d("current video cache length: " + (timestamp - mFirstFrameTimestamp));
//		CLog.d("write video stream packet: size=" + packet.size + ", type=" + packet.type + ", timestamp=" + packet.timestamp);
		return true;
	}
	
	private synchronized boolean writeAudioStreamPacketToPipe(ACStreamPacket packet) {
		if (mAudioFrameBuffer == null || mAudioFrameBuffer.capacity() < packet.getPacketSize()) {
			mAudioFrameBuffer = ByteBuffer.allocateDirect(packet.getPacketSize());
			mAudioFrameBuffer.order(ByteOrder.BIG_ENDIAN);
		}
//		CLog.d("write audio stream packet: size=" + packet.size + ", timestamp=" + packet.timestamp);
		long timestamp = packet.timestamp;
		if (mLatestAudioTimestamp == -1) {
			mLatestAudioTimestamp = timestamp;
		}
		// 如果当前帧的时间戳和前一帧时间戳差值小于1分钟就认为是重复的数据，应该丢弃（录像）
		// 否则认为是时间戳回滚（RTMP 3字节回滚)
		if (timestamp < mLatestAudioTimestamp) {
			if (mLatestAudioTimestamp - timestamp < 60000) {
				CLog.w("invalid audio frame: timestamp=" + timestamp + ", " + mLatestAudioTimestamp);
				return true;
			}
			if (mLastAudioTimestamp == -1) {
				timestamp = mLatestAudioTimestamp;
			} else {
				if (packet.timestamp <= mLastAudioTimestamp) {
					mLastAudioTimestamp = packet.timestamp;
				}
				timestamp = (packet.timestamp - mLastAudioTimestamp) + mLatestAudioTimestamp;
			}
			mLastAudioTimestamp = packet.timestamp;
			packet.timestamp = timestamp;
		} else {
			mLastAudioTimestamp = -1;
		}
		
		packet.writeTo(mAudioFrameBuffer, 0);
		mAudioFrameBuffer.limit(packet.getPacketSize());
		mAudioFrameBuffer.position(0);
		if (mAudioPacketPipe.remaining() < packet.getPacketSize()) {
			int capacity = packet.getPacketSize() - mAudioPacketPipe.remaining();
			if (capacity < 64*1024) {
				capacity = 64*1024;
			}
			capacity += mAudioPacketPipe.capacity();
			if (!mAudioPacketPipe.allocate(capacity)) {
//				CLog.e("allocate memory for audio packet failed, we may be run out of memory or exceed max size");
				return false;
			}
		}
		if (!mAudioPacketPipe.write(mAudioFrameBuffer, 2000)) {
			CLog.e("write an audio stream packet to packet pipe failed");
			return false;
		}
		
		mLatestAudioTimestamp = timestamp;
		return true;
	}
	
	 private ByteBuffer extractSpecificDataBufferFrom(ByteBuffer data, int offset, int size, byte type) {
		 int start = -1;
		 int last = -1;
		 int i, v, k = 0;
		 byte b;
		 data.order(ByteOrder.BIG_ENDIAN);
		 data.limit(offset + size);
		 int limit = data.limit() - 4;
		 for (i=offset; i<limit; i++) {
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
			 last = data.limit();
		 }
		 int length = last - start;
		 if (k == 3) {
			 length++;
		 }
		 ByteBuffer config = ByteBuffer.allocate(length);
		 if (k == 3) {
			 config.put((byte) 0);
		 }
		 data.limit(last);
		 data.position(start);
		 config.put(data);
		 config.clear();
		 return config;
	 }
	 
	 private int extractKeyFrame(ByteBuffer buffer, int offset, int size) {
		 boolean fok = false;
		 byte type;
		 int i, v, k;
		 buffer.order(ByteOrder.BIG_ENDIAN);
		 buffer.limit(offset + size);
		 buffer.position(offset);
		 for (i=offset; i<offset+size-4;) {
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
		 return fok ? buffer.position() : -1;
	 }
	 
	 private ByteBuffer getAudioSpecificDataBufferFrom(ByteBuffer data, int offset, int size) {
		 data.limit(offset + size);
		 data.position(offset);
		 data.order(ByteOrder.BIG_ENDIAN);
		 short syncword = data.getShort(offset);
		 ByteBuffer config = ByteBuffer.allocate(2);
		 if (size >= 7 && (syncword & 0xfff0) == 0xfff0) { // ADTS
			 byte[] adts = new byte[7];
			 data.get(adts);
			 int profile = ((adts[2]>>6) & 0x03) + 1;
			 int frequency_index = (adts[2] >> 2) & 0x0f;
			 int channel_config = ((adts[2] & 0x01) << 2) | ((adts[3] >> 6 ) & 0x03);
			 byte config1 = (byte)(((profile << 3) & 0xf8) | ((frequency_index >> 1) & 0x07));
			 byte config2 = (byte)(((frequency_index << 7) & 0x80) | ((channel_config << 3) & 0x78));
			 config.put(0, config1);
			 config.put(1, config2); 
		 } else if (size == 2 || size == 5) { // ESDS
			 config.put(0, data.get(offset));
			 config.put(1, data.get(offset + 1)); 
		 } else {
			 return null;
		 }
		 return config;
	 }
	 
	private static final int MESSAGE_CLEAR_CACHE = 1;
	private static final int MESSAGE_START_DROPPING = 2;
	private static final int MESSAGE_MUTE = 3;
	
	private Handler.Callback mVideoMessageCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CLEAR_CACHE:
				clearVideoCache();
				break;
			case MESSAGE_START_DROPPING:
				startDroppingVideoFrames(msg.arg1);
				break;
			default:
				return false;
			}
			return true;
		}
	};
	
	private void clearVideoCache() {
		mIsClearVideoBuffer = false;
		mVideoPacketPipe.clear();
		mVideoDecoder.reset();
		mGotVideoFrame = false;
		mBufferDelaySize = mStartPlayBufferSize;
		mLastVideoTimestamp = -1;
		if (mIsWorking && mIsPlaying) {
			mVideoWorkThread.executeTask(mVideoPlayRunnable);
		}
		mMediaInfo.setDelayTime(mBufferDelaySize);
	}
	
	private ByteBuffer mVideoPacketBuffer = ByteBuffer.allocateDirect(512*1024);
	private ACStreamPacket mVideoPacket = new ACStreamPacket();
	private ACVideoFrame mVideoFrame = new ACVideoFrame();
	private long mVideoStartSysTime = 0;
	private long mVideoStartTimestamp = 0;
	private boolean mGotVideoFrame = false;
	private ByteBuffer mSPSBuffer = null;
	private ByteBuffer mPPSBuffer = null;
	
	private Runnable mVideoPlayRunnable = new Runnable() {
		@Override
		public void run() {
			if (readVideoPacket(mVideoPacket, -1)) {
				mMediaInfo.addVideoBytes(mVideoPacket.size);
				playVideoFrame(mVideoPacket);
				extractVideoCodecSpecificData(mVideoPacket);
				if (mGotVideoFrame) {
					saveVideoToFile(mVideoPacket);
				}
			}
			if (mIsWorking && mIsPlaying && !mIsClearVideoBuffer) {
				mVideoWorkThread.executeTask(mVideoPlayRunnable);
			}
		}
	};
	
	private void playVideoFrame(ACStreamPacket packet) {
		long currTimestamp = packet.timestamp;
		long currSysTime = SystemClock.elapsedRealtime();
		// 以第2个帧作为基准时间，因为rtmp第1帧是上一gop组的关键帧，而第2帧是当前gop组的关键帧，中间是没有数据的，
		// 两帧之间的时间差为一gop长度，这么做是为了尽快打开视频，目前暂时这么写
		if (mResetVideoTimestamp < 2) {
			mVideoStartSysTime = currSysTime;
			mVideoStartTimestamp = currTimestamp;
			mResetVideoTimestamp++;
		} else {
			if (mStartDropBufferSize > 0) { // 录像
				long cached = mLastestFrameTimestamp - mFirstFrameTimestamp;
				if (cached > mBufferDelaySize) {
					mVideoStartSysTime -= cached - mBufferDelaySize;
					CLog.i("recalc start systime: cached=" + cached + ", delay=" + mBufferDelaySize + ", systime=" + mVideoStartSysTime);
				}
			}
			if (mResetDelayTime) {
				long yield = (currSysTime - mVideoStartSysTime) - (currTimestamp - mVideoStartTimestamp);
				if (yield > mBufferDelaySize) {
//					if (yield > mStartDropBufferSize) {
//						CLog.i("delay too large, reset start playing buffer size");
//						mBufferDelaySize = mStartPlayBufferSize;
//						mResetVideoTimestamp = 0;
//						mResetAudioTimestmap = true;
//						mResetFrameTimestamp = true;
//					} else {
						CLog.i("adjust start playing buffer size: " + yield);
						mBufferDelaySize = (int)yield;
//					}
					mMediaInfo.setDelayTime(mBufferDelaySize);
				}
				mResetDelayTime = false;
			}
		}
		
		boolean decoded = false;
		ACResult result = null;
		if (mMediaExtra != null) {
			result = mMediaExtra.decodePacket(packet, mVideoFrame);
			decoded = (result.getCode() == ACResult.ACS_OK || result.getCode() == ACResult.ACS_IN_PROCESS);
		}
		if (!decoded) {
			result = mVideoDecoder.decode(packet, mVideoFrame);
		}
		if (result.isResultOK()) {
			mGotVideoFrame = true;
			if (mMediaExtra != null) {
				mMediaExtra.processFrame(mVideoFrame);
			}
			renderVideoFrame(mVideoFrame);
			saveVideoFrame(mVideoFrame);
		}
	}
	
	private boolean readVideoPacket(ACStreamPacket packet, int timedout) {
		mVideoPacketBuffer.clear();
		ByteBuffer packetBuffer = mVideoPacketPipe.read(mVideoPacketBuffer, timedout);
		if (packetBuffer != null) {
			mVideoPacketBuffer = packetBuffer;
			int packetSize = packetBuffer.remaining();
			packet.readFrom(packetBuffer, 0, false);
			if (packet.getPacketSize() != packetSize) {
				CLog.e("broken video stream packet: " + packet.getPacketSize() + "/" + packetSize);
				return false;
			}
//			CLog.d("read video stream packet: type=" + packet.type + ", size=" + packetSize);
			mFirstFrameTimestamp = packet.timestamp;
			mMediaInfo.setFirstTimestamp(packet.timestamp);
			return true;
		}
		return false;
	}
	
	private void renderVideoFrame(ACVideoFrame frame) {
		if (mResetVideoTimestamp > 0) {
			long currTimestamp = frame.timestamp;
			long currSysTime = SystemClock.elapsedRealtime();
			long yield;

			yield = (currSysTime - mVideoStartSysTime) - (currTimestamp - mVideoStartTimestamp) - mAudioDelayTime;
//			CLog.d("current video frame delay: " + yield + ", playing buffer size: " + mBufferDelaySize);
			if (yield < mBufferDelaySize) {
				if (yield < -10000 + mAudioDelayTime) {
					CLog.i("video duration(" + (-yield) + "ms) too large, reset base time, current frame timestamp=" + currTimestamp);
					mVideoStartSysTime = currSysTime;
					mVideoStartTimestamp = currTimestamp;
					mResetFrameTimestamp = true;
					int d = mMediaInfo.getVideoFrameRate();
					if (d == 0) {
						d = 30;
					} else {
						d = 1000/d;
						if (d > 10) {
							d -= 10;
						}
					}
					yield = d - mAudioDelayTime;
				} else {
					yield = mBufferDelaySize - yield;
				}
			} else {
				yield = 0;
			}
			if (yield > 0) {
				try {
					Thread.sleep(yield);
				} catch (InterruptedException e) {
				}
			}
		}
		
		if (mVideoRenderer != null && mIsShowVideo && !mEnterBackground) {
			mVideoRenderer.drawFrame(frame, ACVideoRenderer.GLR_ROTATION_0, false, null);
		}
		
		mMediaInfo.incVideoFrames();
	}
	
	private void startDroppingVideoFrames(int drop) {
		int droppedVideoFrames = 0;
		int dropped = 0;
		long startDroppingVideoTimestamp = 0;
		boolean done = false;
		CLog.d("start dropping video frames: length=" + drop);
		while (readVideoPacket(mVideoPacket, 0)) {
			if (mGotVideoFrame) {
				saveVideoToFile(mVideoPacket);
			}
			if (droppedVideoFrames > 0) {
				dropped = (int) (mVideoPacket.timestamp - startDroppingVideoTimestamp);
				if (dropped >= drop &&
						(mVideoPacket.type == ACFrameType.AC_NALU_TYPE_SPS || mVideoPacket.type == ACFrameType.AC_NALU_TYPE_IDR)) {
					CLog.d("dropped video frames: count=" + droppedVideoFrames + ", length=" + dropped);
					mBufferDelaySize -= dropped;
					mMediaInfo.setDelayTime(mBufferDelaySize);
					playVideoFrame(mVideoPacket);
					if (mAudioPacketPipe.cache() > 0) {
						mAudioWorkThread.sendMessage(MESSAGE_START_DROPPING, dropped, 0);
					}
					done = true;
					break;
				} 
			} else {
				startDroppingVideoTimestamp = mVideoPacket.timestamp;
			}
			droppedVideoFrames++;
		}
		if (!done) {
			CLog.w("left dropping video frames undone");
			mVideoWorkThread.sendMessage(MESSAGE_START_DROPPING, drop-dropped, 0);
		}
	}
	
	private void extractVideoCodecSpecificData(ACStreamPacket packet) {
		if ((mSPSBuffer == null || mPPSBuffer == null) &&
				(packet.type == ACFrameType.AC_NALU_TYPE_SPS || packet.type == ACFrameType.AC_NALU_TYPE_PPS)) {
			if (mSPSBuffer == null) {
				mSPSBuffer = extractSpecificDataBufferFrom(packet.buffer, packet.offset, packet.size, (byte) 0x07);
			}
			if (mPPSBuffer == null) {
				mPPSBuffer = extractSpecificDataBufferFrom(packet.buffer, packet.offset, packet.size, (byte) 0x08);
			}
		}
	}
	
	private void saveVideoToFile(ACStreamPacket packet) {
		synchronized (mSyncObjRecord) {
			if (mVideoFormat == null && mSPSBuffer != null && mPPSBuffer != null) {
				mVideoFormat = MediaFormat.createVideoFormat("video/avc", mVideoFrame.width, mVideoFrame.height);
				mVideoFormat.setByteBuffer("csd-0", mSPSBuffer);
				mVideoFormat.setByteBuffer("csd-1", mPPSBuffer);
			}
			if (mRecording && mSaveVideo) {
				if (!mRecordStarting &&
						(packet.type == ACFrameType.AC_NALU_TYPE_IDR || packet.type == ACFrameType.AC_NALU_TYPE_SPS)) {
					mRecordStarting = startRecording();
				}
				if (mRecordStarting) {
					mBufferInfo.offset = packet.offset;
					mBufferInfo.presentationTimeUs = packet.timestamp * 1000;
					mBufferInfo.size = packet.size;
					mBufferInfo.flags = 0;
					if (packet.type == ACFrameType.AC_NALU_TYPE_SPS) {
						int pos = extractKeyFrame(packet.buffer, packet.offset, packet.size);
						if (pos == -1) {
							mBufferInfo.size = 0;
						} else {
							mBufferInfo.offset = pos;
							mBufferInfo.size -= pos;
							mBufferInfo.flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
						}
					} else if (packet.type == ACFrameType.AC_NALU_TYPE_IDR) {
						mBufferInfo.flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
					}
					if (mBufferInfo.size > 0) {
//						CLog.d("mp4 write video sample data: size=" + mBufferInfo.size + ", timestamp=" + packet.timestamp);
						mMuxerRecord.writeSampleData(mVideoTrackIndex, packet.buffer, mBufferInfo);
					}
				}
			}
		}
	}
	
	private void saveVideoFrame(ACVideoFrame frame) {
		synchronized (mSyncObjSnapshot) {
			// always save the last frame
				if (mVideoFrame4Snapshot.buffer == null || mVideoFrame4Snapshot.buffer.capacity() < frame.size) {
					mVideoFrame4Snapshot.buffer = ByteBuffer.allocate(frame.size);
				}
				frame.buffer.limit(frame.offset + frame.size);
				frame.buffer.position(frame.offset);
				mVideoFrame4Snapshot.buffer.clear();
				mVideoFrame4Snapshot.buffer.put(frame.buffer);
				mVideoFrame4Snapshot.width = frame.width;
				mVideoFrame4Snapshot.height = frame.height;
				mVideoFrame4Snapshot.format = frame.format;
				mVideoFrame4Snapshot.stride = frame.stride;
				mVideoFrame4Snapshot.offset = 0;
				mVideoFrame4Snapshot.size = frame.size;
				mVideoFrame4Snapshot.timestamp = frame.timestamp;
				mCopyVideoFrameSuccess = true;
//				mSyncObjSnapshot.notify();
//			}
		}
	}
	
	private Handler.Callback mAudioMessageCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CLEAR_CACHE:
				clearAudioCache();
				break;
			case MESSAGE_START_DROPPING:
				startDroppingAudioFrames(msg.arg1);
				break;
			case MESSAGE_MUTE:
				break;
			default:
				return false;
			}
			return true;
		}
	};
	
	private void clearAudioCache() {
		mIsClearAudioBuffer = false;
		mAudioPacketPipe.clear();
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				mAudioTrack.pause();
			}
			mAudioTrack.flush();
		}
		mAudioDecoder.reset();
		mGotAudioSample = false;
		mAudioDelayTime = 0;
		mLastAudioTimestamp = -1;
		mLatestAudioTimestamp = -1;
		if (mIsWorking && mIsPlaying) {
			mAudioWorkThread.executeTask(mAudioPlayRunnable);
		}
	}
	
	private ByteBuffer mAudioPacketBuffer = ByteBuffer.allocateDirect(4*1024);
	private ACStreamPacket mAudioPacket = new ACStreamPacket();
	private ACAudioFrame mAudioFrame = new ACAudioFrame();
	private long mAudioStartSysTime = 0;
	private long mAudioStartTimestamp = 0;
	private boolean mMuteState = mIsMute;
	private byte[] mAudioSamples = null;
	private ByteBuffer mESBuffer = null;
	private int mAudioConfigSize = 0;
	private boolean mGotAudioSample = false;
	
	private Runnable mAudioPlayRunnable = new Runnable() {
		@Override
		public void run() {
			if (readAudioPacket(mAudioPacket, -1)) {
				mMediaInfo.addAudioBytes(mAudioPacket.size);
				playAudioFrame(mAudioPacket);
				if (mESBuffer == null &&
						((mAudioPacket.type == ACFrameType.AC_AAC_TYPE_INFO && (mAudioPacket.size == 2 || mAudioPacket.size == 5) ||
						(mAudioPacket.type == ACFrameType.AC_AAC_TYPE_SAMPLE && mAudioPacket.size > 7)))) {
					mESBuffer = getAudioSpecificDataBufferFrom(mAudioPacket.buffer, mAudioPacket.offset, mAudioPacket.size);
					mAudioConfigSize = mESBuffer.remaining();
				}
				if (mGotAudioSample) {
					saveAudioToFile(mAudioPacket);
				}
			}
			
			if (!mIsPlaying) {
				if (mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
					mAudioTrack.pause();
				}
			} else if (mIsWorking && !mIsClearAudioBuffer) {
				mAudioWorkThread.executeTask(mAudioPlayRunnable);
			}
		}
	};
	
	private void playAudioFrame(ACStreamPacket packet) {
		long currTimestamp = packet.timestamp;
		long currSysTime = SystemClock.elapsedRealtime();
		// we don't play first audio sample
		boolean playAudioSample = true;
		if (mResetAudioTimestmap && (packet.type != ACFrameType.AC_AAC_TYPE_INFO || packet.size > 2)) {
			mAudioStartSysTime = currSysTime;
			mAudioStartTimestamp = currTimestamp;
			mResetAudioTimestmap = false;
			playAudioSample = false;
		}
		boolean decoded = false;
		ACResult result = null;
		if (mMediaExtra != null) {
			result = mMediaExtra.decodePacket(packet, mAudioFrame);
			decoded = (result.getCode() == ACResult.ACS_OK || result.getCode() == ACResult.ACS_IN_PROCESS);
		}
		if (!decoded) {
			result = mAudioDecoder.decode(packet, mAudioFrame);
		}
		if (result.isResultOK()) {
			mGotAudioSample = true;
			if (mMediaExtra != null) {
				mMediaExtra.processFrame(mAudioFrame);
			}
			if (mAudioTrack == null) {
				startAudioTrack(mAudioFrame);
			}
			if (mAudioTrack != null && playAudioSample) {
				writeAudioTrack(mAudioFrame);
			}
		}
	}
	
	private boolean readAudioPacket(ACStreamPacket packet, int timedout) {
		mAudioPacketBuffer.clear();
		ByteBuffer packetBuffer = mAudioPacketPipe.read(mAudioPacketBuffer, timedout);
		if (packetBuffer != null) {
			mAudioPacketBuffer = packetBuffer;
			int packetSize = packetBuffer.remaining();
			packet.readFrom(packetBuffer, 0, false);
			if (packet.getPacketSize() != packetSize) {
				CLog.e("broken audio stream packet: " + packet.getPacketSize() + "/" + packetSize);
				return false;
			}
//			CLog.d("read audio stream packet: type=" + mAudioPacket.type + ", size=" + mAudioPacket.size + ", timestamp=" + mAudioPacket.timestamp);
			mLatestAudioTimestamp = packet.timestamp;
			return true;
		}
		return false;
	}
	
	private void startDroppingAudioFrames(int drop) {
		int droppedAudioFrames = 0;
		int dropped = 0;
		long startDroppingAudioTimestamp = 0;
		boolean done = false;
		CLog.d("start dropping audio frames: length=" + drop);
		while (readAudioPacket(mAudioPacket, 0)) {
			if (mAudioPacket.type == ACFrameType.AC_AAC_TYPE_INFO && mAudioPacket.size == 2) {
				continue;
			}
			if (mGotAudioSample) {
				saveAudioToFile(mAudioPacket);
			}
			if (droppedAudioFrames > 0) {
				dropped = (int) (mAudioPacket.timestamp - startDroppingAudioTimestamp);
				if (dropped >= drop) {
					CLog.d("dropped audio frames: count=" + droppedAudioFrames + ", length=" + dropped);
					playAudioFrame(mAudioPacket);
					done = true;
					break;
				} 
			} else {
				startDroppingAudioTimestamp = mAudioPacket.timestamp;
			}
			droppedAudioFrames++;
		}
		if (!done) {
			CLog.w("left dropping audio frames undone");
			mAudioWorkThread.sendMessage(MESSAGE_START_DROPPING, drop-dropped, 0);
		}
	}
	
	private void startAudioTrack(ACAudioFrame frame) {
		int channelConfig = AudioFormat.CHANNEL_INVALID;
		switch (frame.channelCount) {
		case 1:
			channelConfig = AudioFormat.CHANNEL_OUT_MONO;
			break;
		case 2:
			channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
			break;
		case 3:
			channelConfig = AudioFormat.CHANNEL_OUT_STEREO | AudioFormat.CHANNEL_OUT_FRONT_CENTER;
			break;
		case 4:
			channelConfig = AudioFormat.CHANNEL_OUT_QUAD;
			break;
		case 5:
			channelConfig = AudioFormat.CHANNEL_OUT_QUAD | AudioFormat.CHANNEL_OUT_FRONT_CENTER;
			break;
		case 6:
			channelConfig = AudioFormat.CHANNEL_OUT_5POINT1;
			break;
		case 7:
			channelConfig = AudioFormat.CHANNEL_OUT_5POINT1 | AudioFormat.CHANNEL_OUT_BACK_CENTER;
			break;
		case 8:
			if (Build.VERSION.SDK_INT >= 23) {
				channelConfig = AudioFormat.CHANNEL_OUT_7POINT1_SURROUND;
				break;
			}
		default:
			CLog.e("error channel count: " + frame.channelCount);
			break;
		}
		int audioFormat = AudioFormat.ENCODING_INVALID;
		switch (frame.format) {
		case ACSampleFormat.AC_SAMPLE_FMT_U8:
			audioFormat = AudioFormat.ENCODING_PCM_8BIT;
			break;
		case ACSampleFormat.AC_SAMPLE_FMT_S16:
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;
			break;
		default:
			CLog.e("error audio format: " + frame.format);
			break;
		}
		if (channelConfig != AudioFormat.CHANNEL_INVALID  && audioFormat != AudioFormat.ENCODING_INVALID) {
//			mAudioPlayTrunkSize = frame.sampleRate * frame.bitSize * frame.channelCount / 8;
//			frameSize = (long) 1000 * frame.size / mAudioPlayTrunkSize;
//			mAudioPlayTrunkSize = 20 * mAudioPlayTrunkSize / 1000;
			mAudioPlayTrunkSize = frame.size * 2;
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frame.sampleRate, channelConfig, audioFormat,
					mAudioPlayTrunkSize, AudioTrack.MODE_STREAM);
			mAudioTrack.play();
		}
	}
	
	private void stopAudioTrack() {
		if (mAudioTrack != null) {
			try {
				mAudioTrack.stop();
			} catch (IllegalStateException e) {
				CLog.e("stop audio track", e);
			}
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}
	
	@SuppressLint("NewApi")
	private void writeAudioTrack(ACAudioFrame frame) {
		if (mIsMute) {
			if (!mMuteState) {
				if (Build.VERSION.SDK_INT < 21) {
					mAudioTrack.setStereoVolume(.0f, .0f);
				} else {
					mAudioTrack.setVolume(.0f);
				}
				mMuteState = true;
			}
		} else {
			if (mMuteState) {
				if (Build.VERSION.SDK_INT < 21) {
					mAudioTrack.setStereoVolume(1.f, 1.f);
				} else {
					mAudioTrack.setVolume(1.f);
				}
				mMuteState = false;
			}
		}
		if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			mAudioTrack.play();
		}
		
		long currTimestamp = frame.timestamp;
		long currSysTime = SystemClock.elapsedRealtime();
		long yield = (currSysTime - mAudioStartSysTime) - (currTimestamp - mAudioStartTimestamp);
//		CLog.d("current audio frame delay: " + yield);
		mAudioDelayTime = (int) (mBufferDelaySize - yield);
//		CLog.d("audio delay: " + mAudioDelayTime);
		boolean dropFrame = false;
		if (yield < mBufferDelaySize) {
			yield = mBufferDelaySize - yield;
			if (yield < -10000) {
				CLog.i("audio duration(" + (-yield) + "ms) too large, reset base time, current frame timestamp=" + currTimestamp);
				mAudioStartSysTime = currSysTime;
				mAudioStartTimestamp = currTimestamp;
				mAudioDelayTime = 0;
			}
		} else {
			if (mStartDropBufferSize > 0 && yield > mStartDropBufferSize) {
				dropFrame = true;
			}
			yield = 0;
		}
		if (yield > 0) {
			try {
				Thread.sleep(yield);
			} catch (InterruptedException e) {
			}
		}
		
		if (!dropFrame) {
			if (frame.buffer.isDirect()) {
				if (mAudioSamples == null || mAudioSamples.length < frame.size) {
					mAudioSamples = new byte[frame.size];
				}
				frame.buffer.limit(frame.offset + frame.size);
				frame.buffer.position(frame.offset);
				frame.buffer.get(mAudioSamples, 0, frame.size);
				mAudioTrack.write(mAudioSamples, 0, frame.size);
			} else {
				mAudioTrack.write(frame.buffer.array(), frame.offset, frame.size);
			}
			// TODO: how to get the precise play time
			// actually current system time isn't the exact playing time
//			currSysTime = SystemClock.elapsedRealtime();
			// CLog.d("write audio track: timestamp=" + frame.timestamp + ", time=" + currSysTime + ", length=" + frameSize + ", size=" + frame.size);
			// mSynchronizeVideoWithAudio.updateAudioTimestamp(frame.timestamp, currSysTime);
		}
		
		mMediaInfo.incAudioFrames();
	}
	
	private void saveAudioToFile(ACStreamPacket packet) {
		synchronized (mSyncObjRecord) {
			// only aac supported
			if (mAudioFormat == null && mESBuffer != null) {
				mAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", mAudioFrame.sampleRate, mAudioFrame.channelCount);
				mAudioFormat.setByteBuffer("csd-0", mESBuffer);
			}
			if (mRecording && mSaveAudio) {
				if (!mRecordStarting && !mSaveVideo) { // 保存视频时由视频开始录像
					mRecordStarting = startRecording();
				}
				if (mRecordStarting) {
					mBufferInfo.offset = packet.offset;
					mBufferInfo.presentationTimeUs = packet.timestamp * 1000;
					mBufferInfo.size = packet.size;
					if (packet.type == ACFrameType.AC_AAC_TYPE_INFO) {
						mBufferInfo.size -= mAudioConfigSize;
					}
					mBufferInfo.flags = 0;
					if (mBufferInfo.size > 0) {
//						CLog.d("mp4 write audio sample data: size=" + mBufferInfo.size + ", timestamp=" + packet.timestamp);
						mMuxerRecord.writeSampleData(mAudioTrackIndex, packet.buffer, mBufferInfo);
					}
				}
			}
		}
	}
	
	private boolean startRecording() {
		if (mVideoFormat == null && mAudioFormat == null) {
			return false;
		}
		if (mSaveVideo && mVideoFormat != null && mVideoTrackIndex == -1) {
			mVideoTrackIndex = mMuxerRecord.addTrack(mVideoFormat);
		}
		if (mSaveAudio && mAudioFormat != null && mAudioTrackIndex == -1) {
			mAudioTrackIndex = mMuxerRecord.addTrack(mAudioFormat);
		}
		if (mSaveVideo && mVideoTrackIndex == -1) {
			return false;
		}
		if (mSaveAudio && mAudioTrackIndex == -1) {
			return false;
		}
		mMuxerRecord.start();
		return true;
	}
	
	
	private class MediaInfo {
		public static final int BITRATE_DURATION = 10;
		public static final int FRAMERATE_DURATION = 5;
		
		private static final int MSG_RESET = 0;
		private static final int MSG_ADD_VIDEO_BYTE = 1;
		private static final int MSG_ADD_AUDIO_BYTE = 2;
		private static final int MSG_INC_VIDEO_FRAME = 3;
		private static final int MSG_INC_AUDIO_FRAME = 4;
		private static final int MSG_SET_FIRST_TIME = 5;
		private static final int MSG_SET_LATEST_TIME = 6;
		private static final int MSG_SET_DELAY_TIME = 7;
		
		private WorkThreadExecutor mMediaInfoWorkThread = null;
		private boolean mStarted = false;
		private boolean mStartedBitRate = false;
		private boolean mStartedFrameRate = false;
		private int mCurrentBytesIndex = 0;
		private boolean mBytesFull = false;
		private int mCurrentFramesIndex = 0;
		private boolean mFramesFull = false;
		
		private int mTotalVideoBytes = 0;
		private int mTotalAudioBytes = 0;
		private int mTotalVideoFrames = 0;
		private int mTotalAudioFrames = 0;
		private int[] mVideoBytes = new int[BITRATE_DURATION];
		private int[] mAudioBytes = new int[BITRATE_DURATION];
		private int[] mVideoFrames = new int[FRAMERATE_DURATION];
		private int[] mAudioFrames = new int[FRAMERATE_DURATION];
		
		private int mVideoBitRate = 0;
		private int mAudioBitRate = 0;
		private int mVideoFrameRate = 0;
		private int mAudioFrameRate = 0;
		private long mFirstTime = 0;
		private long mLatestTime = 0;
		private int mDelayTime = 0;
		
		public void start() {
			mStarted = true;
			resetInner();
			mMediaInfoWorkThread = new WorkThreadExecutor("WorkThread-ACPlayer-MediaInfo");
			mMediaInfoWorkThread.start(mMsgCallback);
		}
		
		public void stop() {
			mStarted = false;
			mStartedBitRate = false;
			mStartedFrameRate = false;
			if (mMediaInfoWorkThread != null) {
				mMediaInfoWorkThread.stop();
				mMediaInfoWorkThread.release();
				mMediaInfoWorkThread = null;
			}
		}
		
		private void resetInner() {
			if (mMediaInfoWorkThread != null) {
				mMediaInfoWorkThread.removeTasksAndMessages(null);
			}
			mStartedBitRate = false;
			mStartedFrameRate = false;
			
			mTotalVideoBytes = 0;
			mTotalAudioBytes = 0;
			mTotalVideoFrames = 0;
			mTotalAudioFrames = 0;
			
			mCurrentBytesIndex = 0;
			mBytesFull = false;
			mCurrentFramesIndex = 0;
			mFramesFull = false;
			
			mVideoBitRate = 0;
			mAudioBitRate = 0;
			mVideoFrameRate = 0;
			mAudioFrameRate = 0;
			mFirstTime = 0;
			mLatestTime = 0;
			mDelayTime = 0;
		}
		
		private void updateBitRate() {
			mVideoBytes[mCurrentBytesIndex] = mTotalVideoBytes;
			mAudioBytes[mCurrentBytesIndex] = mTotalAudioBytes;
			mTotalVideoBytes = 0;
			mTotalAudioBytes = 0;
			mCurrentBytesIndex++;
			if (mCurrentBytesIndex == BITRATE_DURATION) {
				mCurrentBytesIndex = 0;
				mBytesFull = true;
			}
			mVideoBitRate = 0;
			mAudioBitRate = 0;
			int t = mBytesFull ? BITRATE_DURATION : mCurrentBytesIndex;
			for (int i=0; i<t; i++) {
				mVideoBitRate += mVideoBytes[i];
				mAudioBitRate += mAudioBytes[i];
			}
			mVideoBitRate = Math.round((float) mVideoBitRate*8/t/1000);
			mAudioBitRate = Math.round((float) mAudioBitRate*8/t/1000);
		}
		
		private void updateFrameRate() {
			mVideoFrames[mCurrentFramesIndex] = mTotalVideoFrames;
			mAudioFrames[mCurrentFramesIndex] = mTotalAudioFrames;
			mTotalVideoFrames = 0;
			mTotalAudioFrames = 0;
			mCurrentFramesIndex++;
			if (mCurrentFramesIndex == FRAMERATE_DURATION) {
				mCurrentFramesIndex = 0;
				mFramesFull = true;
			}
			mVideoFrameRate = 0;
			mAudioFrameRate = 0;
			int t = mFramesFull ? FRAMERATE_DURATION : mCurrentFramesIndex;
			for (int i=0; i<t; i++) {
				mVideoFrameRate += mVideoFrames[i];
				mAudioFrameRate += mAudioFrames[i];
			}
			mVideoFrameRate = Math.round((float) mVideoFrameRate/t);
			mAudioFrameRate = Math.round((float) mAudioFrameRate/t);
		}
		
		private Handler.Callback mMsgCallback = new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_RESET:
					resetInner();
					break;
				case MSG_ADD_VIDEO_BYTE:
					mTotalVideoBytes += msg.arg1;
					if (!mStartedBitRate) {
						mStartedBitRate = true;
						mMediaInfoWorkThread.executeTaskDelay(mRunnableUpdateBitRate, 1000);
					}
					break;
				case MSG_ADD_AUDIO_BYTE:
					mTotalAudioBytes += msg.arg1;
					if (!mStartedBitRate) {
						mStartedBitRate = true;
						mMediaInfoWorkThread.executeTaskDelay(mRunnableUpdateBitRate, 1000);
					}
					break;
				case MSG_INC_VIDEO_FRAME:
					mTotalVideoFrames++;
					if (!mStartedFrameRate) {
						mStartedFrameRate = true;
						mMediaInfoWorkThread.executeTaskDelay(mRunnableUpdateFrameRate, 1000);
					}
					break;
				case MSG_INC_AUDIO_FRAME:
					mTotalAudioFrames++;
					if (!mStartedFrameRate) {
						mStartedFrameRate = true;
						mMediaInfoWorkThread.executeTaskDelay(mRunnableUpdateFrameRate, 1000);
					}
					break;
				case MSG_SET_FIRST_TIME:
					mFirstTime = (((long)(msg.arg1))<<32)|(((long)msg.arg2)&0xffffffffL);
					break;
				case MSG_SET_LATEST_TIME:
					mLatestTime = (((long)(msg.arg1))<<32)|(((long)msg.arg2)&0xffffffffL);
					break;
				case MSG_SET_DELAY_TIME:
					mDelayTime = msg.arg1;
					break;
				default:
					return false;
				}
				return true;
			}
		};
		
		private Runnable mRunnableUpdateBitRate = new Runnable() {
			@Override
			public void run() {
				updateBitRate();
				if (mStartedBitRate) {
					mMediaInfoWorkThread.executeTaskDelay(this, 1000);
				}
			}
		};
		
		private Runnable mRunnableUpdateFrameRate = new Runnable() {
			@Override
			public void run() {
				updateFrameRate();
				if (mStartedFrameRate) {
					mMediaInfoWorkThread.executeTaskDelay(this, 1000);
				}
			}
		};
		
		public void reset() {
			if (mStarted) {
				mMediaInfoWorkThread.sendEmptyMessage(MSG_RESET);
			}
		}
		
		public void addVideoBytes(int size) {
			if (mStarted) {
				mMediaInfoWorkThread.sendMessage(MSG_ADD_VIDEO_BYTE, size, 0);
			}
		}
		
		public void addAudioBytes(int size) {
			if (mStarted) {
				mMediaInfoWorkThread.sendMessage(MSG_ADD_AUDIO_BYTE, size, 0);
			}
		}
		
		public void incVideoFrames() {
			if (mStarted) {
				mMediaInfoWorkThread.sendEmptyMessage(MSG_INC_VIDEO_FRAME);
			}
		}
		
		public void incAudioFrames() {
			if (mStarted) {
				mMediaInfoWorkThread.sendEmptyMessage(MSG_INC_AUDIO_FRAME);
			}
		}
		
		public void setFirstTimestamp(long timestamp) {
			if (mStarted) {
				mMediaInfoWorkThread.sendMessage(MSG_SET_FIRST_TIME, (int)(timestamp>>32), (int)timestamp);
			}
		}
		
		public void setLastestTimestamp(long timestamp) {
			if (mStarted) {
				mMediaInfoWorkThread.sendMessage(MSG_SET_LATEST_TIME, (int)(timestamp>>32), (int)timestamp);
			}
		}
		
		public void setDelayTime(int time) {
			if (mStarted) {
				mMediaInfoWorkThread.sendMessage(MSG_SET_DELAY_TIME, time, 0);
			}
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetVideoBitRate = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf(mVideoBitRate);
			}
		};
		
		public int getVideoBitRate() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetVideoBitRate, null);
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetAudioBitRate = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf(mAudioBitRate);
			}
		};
		
		public int getAudioBitRate() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetAudioBitRate, null);
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetVideoFrameRate = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf(mVideoFrameRate);
			}
		};
		
		public int getVideoFrameRate() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetVideoFrameRate, null);
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetAudioFrameRate = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf(mAudioFrameRate);
			}
		};
		
		public int getAudioFrameRate() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetAudioFrameRate, null);
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetBufferLength = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf((int)(mLatestTime-mFirstTime));
			}
		};
		
		public int getBufferLength() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetBufferLength, null);
		}
		
		private WorkThreadExecutor.SyncRunnable mRunnableGetDelayTime = new WorkThreadExecutor.SyncRunnable() {
			@Override
			public Object run(Object obj) {
				return Integer.valueOf(mDelayTime);
			}
		};
		
		public int getDelayTime() {
			if (!mStarted) {
				return 0;
			}
			return (Integer) mMediaInfoWorkThread.executeTaskAndWaitForResult(mRunnableGetDelayTime, null);
		}
		
	}
	
	private static class ACNativeLibraryLoader {
		public static int TRIGGER = 0;
		public static final boolean ACDECODER_LOADED;

		static {
			boolean loaded = false;
			try {
				System.loadLibrary("acdecoder");
				loaded = true;
			} catch (UnsatisfiedLinkError e) {
				CLog.e("failed to load library acdecoder", e);
			}
			ACDECODER_LOADED = loaded;
		}
		
	}
	
	private static class SynchronizeVideoWithAudio {
		private Object mFence = new Object();
		private long mAudioBaseTimestamp = -1;
		private long mAudioBaseSysTime = -1;
		private long mAudioLastTimestamp = -1;
		private long mAudioLastSysTime = -1;
		private long mVideoBaseTimestamp = -1;
		private long mVideoBaseSysTime = -1;
		private long mVideoLastTimestamp = -1;
		private long mVideoLastSysTime = -1;
		
		public void resetBaseTimestamp() {
			synchronized (mFence) {
				mAudioBaseTimestamp = -1;
				mAudioBaseSysTime = -1;
				mAudioLastTimestamp = -1;
				mAudioLastSysTime = -1;
				mVideoBaseTimestamp = -1;
				mVideoBaseSysTime = -1;
				mVideoLastTimestamp = -1;
				mVideoLastSysTime = -1;
			}
		}
		
		public void updateAudioTimestamp(long timestamp, long sysTime) {
			synchronized (mFence) {
				if (mAudioBaseTimestamp < 0) {
					mAudioBaseTimestamp = timestamp;
					mAudioBaseSysTime = sysTime;
				}
				mAudioLastTimestamp = timestamp;
				mAudioLastSysTime = sysTime;
			}
		}
		
		public long synchronizeVideo(long timestamp, long sysTime) {
			synchronized (mFence) {
				if (mVideoBaseTimestamp < 0) {
					mVideoBaseTimestamp = timestamp;
					mVideoBaseSysTime = sysTime;
				}
				mVideoLastTimestamp = timestamp;
				mVideoLastSysTime = sysTime;
				long audioDelay = (mAudioLastSysTime-mAudioBaseSysTime) - (mAudioLastTimestamp-mAudioBaseTimestamp);
				long videoYield = (mVideoLastTimestamp-mVideoBaseTimestamp) -  (mVideoLastSysTime-mVideoBaseSysTime);
				return audioDelay+videoYield;
			}
		}
	}
	
}

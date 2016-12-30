package com.lingyang.sdk.player.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.MediaController;

import com.antelope.sdk.ACMediaInfo;
import com.antelope.sdk.ACMediaType;
import com.antelope.sdk.ACMessageListener;
import com.antelope.sdk.ACResult;
import com.antelope.sdk.ACResultListener;
import com.antelope.sdk.capturer.ACAudioCapturer;
import com.antelope.sdk.capturer.ACAudioFrame;
import com.antelope.sdk.capturer.ACFrame;
import com.antelope.sdk.capturer.ACFrameAvailableListener;
import com.antelope.sdk.capturer.ACShape;
import com.antelope.sdk.capturer.ACVideoFrame;
import com.antelope.sdk.codec.ACAudioDecoder;
import com.antelope.sdk.codec.ACAudioEncoder;
import com.antelope.sdk.codec.ACAudioEncoderFactory;
import com.antelope.sdk.codec.ACCodecID;
import com.antelope.sdk.codec.ACEncodeMode;
import com.antelope.sdk.codec.ACPacketAvailableListener;
import com.antelope.sdk.codec.ACStreamPacket;
import com.antelope.sdk.codec.ACVideoDecoder;
import com.antelope.sdk.player.ACMediaExtra;
import com.antelope.sdk.player.ACPlayer;
import com.antelope.sdk.streamer.ACProtocolType;
import com.antelope.sdk.streamer.ACStreamer;
import com.antelope.sdk.streamer.ACStreamerFactory;
import com.antelope.sdk.utils.HandlerUtil;
import com.antelope.sdk.utils.WorkThreadExecutor;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.facetime.OnPlayerVideoFrameListener;
import com.lingyang.sdk.player.IMediaParamProtocol;
import com.lingyang.sdk.player.PlayOptions;
import com.lingyang.sdk.player.PlayType;
import com.lingyang.sdk.util.CLog;
import com.lingyang.sdk.util.HttpUtil;
import com.lingyang.sdk.util.SchemeUtil;

import java.nio.ByteBuffer;

public class LYPlayer extends PlayerContainerView implements MediaController.MediaPlayerControl {

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final int PLAYER_PREPARED = 6;
    private static final int PLAYER_ERROR = -11;
    private static final int PLAYER_SEEK_COMPLETE = 7;
    private static final int PLAYER_RECORD_PROGRESS = 8;
    private static final int PLAYER_CONNECTED = 9;

    PlayOptions mPlayOptions;
    private LYGLPlayerView mLYGLPlayerView;
    // private GLSurfaceView mPlayerView;
    private int mPlayType;
    // private IPlayer mPlayer;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    /**
     * 视频宽高
     */
    private int mVideoWidth, mVideoHeight;
    /**
     * 非全屏时View的大小
     */
    private int mMinWith = 0, mMinHeight = 0;
    private int mCurrentScreenType = TYPE_PLAYER_RATIO_PROP_FULL;
    private OnPlayProgressListener mPlayProgressListener;
    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnPlayingBufferCacheListener mOnBufferListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnLocalRecordListener mLocalRecordListener;
    private OnSnapshotListener mOnSnapshotListener;
    private OnConnectedListener mOnConnectedListener;
    private OnClosedListener mOnclosedListener;

    private boolean mIsFitScreen = false;

    // 3.0
    private ACAudioDecoder mAudioDecoder;
    private ACVideoDecoder mVideoDecoder;
    private ACStreamer mStreamer;
    private ACPlayer mPlayer;
    private ACAudioCapturer mAudioCapture;
    private ACAudioEncoder mAudioEncoder;

    private int mStart, mEnd, mPlay;
    private String mSegment;
    private boolean isStartPlay;

    private WorkThreadExecutor mWorkThreadExecutor;
    private Handler mUIHandler;
    private Context mContext;

    public LYPlayer(Context context) {
        this(context, null);
    }

    public LYPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LYPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initData();
        // initView();
    }

    private void initData() {
        // EventBus.getDefault().register(this);
        mWorkThreadExecutor = new WorkThreadExecutor("WorkThreadExecutor_LYPlayer");
        mWorkThreadExecutor.start(null);
        mUIHandler = getUIHandler();
        setBackgroundColor(Color.BLACK);
        initSurface();
    }

    private void initSurface() {
        mLYGLPlayerView = new LYGLPlayerView(getContext());
        addSurfaceView(mLYGLPlayerView, mCurrentScreenType);
        mPlayer = new ACPlayer();
        mPlayer.setPlaySurfaceView(mLYGLPlayerView, ACShape.AC_SHAPE_NONE, new Object());
        CLog.v("initSurface hashCode- " + mLYGLPlayerView.hashCode());
    }

    private Handler getUIHandler() {
        return new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PLAYER_PREPARED:
                        if (mOnPreparedListener != null)
                            mOnPreparedListener.onPrepared(3000);
                        break;
                    case PLAYER_ERROR:
                        ACResult status = (ACResult) msg.obj;
                        if (mOnErrorListener != null) {
                            mOnErrorListener.onError(status.getCode(), status.getErrMsg());
                        }
                        break;
                    case PLAYER_SEEK_COMPLETE:
                        ACResult result = (ACResult) msg.obj;
                        if (mOnSeekCompleteListener != null) {
                            if (result.isResultOK())
                                mOnSeekCompleteListener.onSeekSuccess(1);
                            else
                                mOnSeekCompleteListener.onSeekError(new LYException(result.getCode(), result.getErrMsg()));
                        }
                        break;
                    case PLAYER_RECORD_PROGRESS:
                        performProgressListener();
                        break;
                    case PLAYER_CONNECTED:
                        if (mOnConnectedListener != null) {
                            mOnConnectedListener.onConnected(3000);
                        }
                        break;

                    default:
                        break;
                }
            }
        };
    }

    private void performProgressListener() {
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD && isStartPlay && mPlayProgressListener != null) {
            mPlayProgressListener.onPlayProgress(getCurrentPosition());
            HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_RECORD_PROGRESS, 1000);
        }

    }

    public ACPlayer getPlayer() {
        return mPlayer;
    }

    ACMediaExtra mMediaExtra = new ACMediaExtra() {

        /**
         * 视频渲染之前和音频播放之前都会调用 （数据已解码） 自行判断区分音视频
         */
        @Override
        public ACResult processFrame(ACFrame frame) {
            mFrameCount++;
            if (mFrameCount == 3) {
                HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_PREPARED);
            }
            if (mPlayVideoFrameListener != null) {
                if (frame instanceof ACVideoFrame) {
                    mPlayVideoFrameListener.onVideoFrameAvailable(frame.buffer, frame.offset, frame.size,
                            ((ACVideoFrame) frame).width, ((ACVideoFrame) frame).height, frame.timestamp);
                }
                return new ACResult(ACResult.ACS_OK, "");
            }
            return new ACResult(ACResult.ACS_UNIMPLEMENTED, "");
        }

        /**
         * 音视频解码前调用 packet 未解码的数据 frame 放解码后的数据
         */
        @Override
        public ACResult decodePacket(ACStreamPacket packet, ACFrame frame) {
            return new ACResult(ACResult.ACS_UNIMPLEMENTED, "");
        }
    };

    // private void initView() {
    // mPlayerView=new GLSurfaceView(getContext());
    // LayoutParams params=new LayoutParams(getWidth(), getHeight());
    // params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    // addView(mPlayerView, params);
    // }

    // private void processRecordEvent(Message msg) {
    // switch (msg.what) {
    // case IPlayer.ON_BUFFERING_UPDATE:
    // if (mOnBufferListener != null) {
    // mOnBufferListener.onPlayingBufferCache(msg.arg1);
    // }
    // break;
    // case IPlayer.ON_COMPLETION:
    // if (mOnCompletionListener != null) {
    // mOnCompletionListener.onCompletion();
    // }
    // mCurrentState = STATE_IDLE;
    // break;
    // case IPlayer.ON_ERROR:
    // if (mOnErrorListener != null) {
    // CLog.i("ON_ERROR @@@@@@@@@@@@@@@ ON_ERROR = "
    // + IPlayer.ON_ERROR + " arg1 = " + msg.arg1);
    // LYException exception = (LYException) msg.obj;
    // mOnErrorListener.onError(exception.getCode(),
    // exception.getMessage());
    // // close();
    // }
    // mCurrentState = STATE_ERROR;
    // break;
    // case IPlayer.ON_BEGIN_BUFFERING:
    // if (mOnBufferListener != null) {
    // CLog.i("Buffering infor debug MEDIA_INFO_BUFFERING_START "
    // + "NativeVideoPlayer ****************** ON_BEGIN_BUFFERING = "
    // + msg.what + " arg1 = " + msg.arg1);
    // mOnBufferListener.onBufferStart();
    // }
    // break;
    // case IPlayer.ON_END_BUFFERING:
    // if (mOnBufferListener != null) {
    // CLog.i("Buffering infor debug MEDIA_INFO_BUFFERING_START "
    // + "NativeVideoPlayer ****************** ON_BEGIN_BUFFERING = "
    // + msg.what + " arg1 = " + msg.arg1);
    // mOnBufferListener.onBufferEnd();
    // }
    // break;
    //
    // case IPlayer.ON_CLOSED:
    // if(mOnclosedListener!=null){
    // mOnclosedListener.onClosed();
    // CLog.i("ON_CLOSED !!!!!!!!!!!!!!!!!!!!");
    // }
    // mCurrentState=STATE_IDLE;
    // break;
    //
    // case IPlayer.ON_PREPARED:
    // if (mOnPreparedListener != null) {
    //// mLYGLPlayerView.getRenderer().setHandler(
    //// mPlayer.getPlayerHandler());
    // mOnPreparedListener.onPrepared(msg.arg1);
    // }
    // getVideoWidthAndHeight();
    // if(mIsFitScreen) resetRatio();
    // mCurrentState = STATE_PREPARED;
    // break;
    // case IPlayer.ON_GET_POSTION:
    // if (mPlayProgressListener != null) {
    // mPlayProgressListener.onPlayProgress(msg.arg1);
    // }
    // break;
    // case IPlayer.ON_SEEK_SUCCESS:
    // if (mOnSeekCompleteListener != null) {
    // mOnSeekCompleteListener.onSeekSuccess(msg.arg1);
    // }
    // break;
    // case IPlayer.ON_SEEK_ERROR:
    // if (mOnSeekCompleteListener != null) {
    // mOnSeekCompleteListener.onSeekError((LYException) msg.obj);
    // }
    // break;
    // default:
    // break;
    //
    // }
    // }
    //
    // private void processLiveEvent(Message msg) {
    // switch (msg.what) {
    // case IPlayer.ON_ERROR:
    // if (mOnErrorListener != null) {
    // CLog.i("ON_ERROR @@@@@@@@@@@@@@@ ON_ERROR = "
    // + IPlayer.ON_ERROR + " arg1 = " + msg.arg1);
    // LYException exception = (LYException) msg.obj;
    // mOnErrorListener.onError(exception.getCode(),
    // exception.getMessage());
    // // close();
    // }
    // mCurrentState = STATE_ERROR;
    // break;
    // case IPlayer.ON_CONNECTING_END:
    // if (mOnConnectedListener != null) {
    // mOnConnectedListener.onConnected(msg.arg1);
    // }
    // break;
    // case IPlayer.ON_CLOSED:
    // if(mOnclosedListener!=null){
    // mOnclosedListener.onClosed();
    // CLog.i("ON_CLOSED !!!!!!!!!!!!!!!!!!!!");
    // }
    // mCurrentState=STATE_IDLE;
    // break;
    // case IPlayer.ON_PREPARED:
    // if (mOnPreparedListener != null) {
    // mOnPreparedListener.onPrepared(msg.arg1);
    // }
    // getVideoWidthAndHeight();
    // if(mIsFitScreen) resetRatio();
    // mCurrentState = STATE_PREPARED;
    // break;
    // default:
    // break;
    //
    // }
    // }
    //
//	 private void getVideoWidthAndHeight(){
//	 try {
//	 CLog.i("mVideoWidth:" +
//	 getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH)
//	 + "mVideoHeight:" +
//	 getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
//	 mVideoWidth=Integer.parseInt(getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH));
//	 mVideoHeight=Integer.parseInt(getMediaParam(IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT));
//	 }catch (RuntimeException e){
//	 e.printStackTrace();
//	// throw e;
//	 }
    // }
    private void reInitSurface() {
        if (getChildCount() > 0 && getChildAt(0) instanceof SurfaceView) {
            CLog.v("not initSurface hashCode- " + mLYGLPlayerView.hashCode());
            resetRatio();
        } else {
            mLYGLPlayerView = new LYGLPlayerView(getContext());
            addSurfaceView(mLYGLPlayerView, mCurrentScreenType);
            CLog.v("initSurface hashCode- " + mLYGLPlayerView.hashCode());
        }
    }
    //
    // /**
    // * 是否是4.3及以上的系统
    // *
    // * @return
    // */
    // private boolean isJellyBeanMr2() {
    // return android.os.Build.VERSION.SDK_INT >= 18;
    // }
    //
    // private boolean isInPlaybackState() {
    // return (mPlayer != null && mCurrentState != STATE_ERROR
    // && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    // }
    //
    // private void createPlayer(PlayType playerType) {
    // PlayerFactory playerFactory = new PlayerFactory(getContext());
    // mPlayer = playerFactory.getPlayer(playerType);
    // }
    //
    // public IPlayer getPlayer() {
    // return mPlayer;
    // }
    //

    /**
     * 设置video是否自适应屏幕
     *
     * @param isFit true适应 false不适应
     */
    public void setFitScreen(boolean isFit) {
        mIsFitScreen = isFit;
        resetRatio();
    }

    // /**
    // * 设置自定义播放控制器，{@link #setDataSource(String)}之前调用
    // *
    // * @param player
    // */
    // public void setPlayer(IPlayer player) {
    // Preconditions.checkNotNull(player);
    // mPlayer = player;
    // }

    private boolean useHardVideoDecode;

    public void useHardVideoDecode(boolean decode) {
        useHardVideoDecode = decode;
        // if(mPlayer!=null)
        // mPlayer.useHardVideoDecode(decode);
    }

    /**
     * 设置播放源
     *
     * @param url
     */
    public void setDataSource(String url) {
        if (url == null)
            return;
        mPlayType = PlayType.getPlayType(url);
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD)
            resolveUrl(url);
        // if (mPlayer == null) {
        // createPlayer(mPlayType);
        // }
        // if (mPlayOptions != null) {
        // mPlayer.setPlayOptions(mPlayOptions);
        // }
        // mPlayer.setDataSource(url);
        mCurrentState = STATE_PREPARING;
        initPlayer();
        initStreamer(url);

    }

    private void initPlayer() {

        mPlayer.initialize(mPlayType == ACProtocolType.AC_PROTOCOL_RECORD ? 0 : 2000, mPlayType == ACProtocolType.AC_PROTOCOL_RECORD ? 0 : 5000, ACCodecID.AC_CODEC_ID_H264, ACCodecID.AC_CODEC_ID_AAC, 16000, 1, mMediaExtra);
    }

    private void resolveUrl(String url) {

        String start = SchemeUtil.getParamValue(url, "begin");
        String end = SchemeUtil.getParamValue(url, "end");
        String play = SchemeUtil.getParamValue(url, "play");
        String token = SchemeUtil.getParamValue(url, "token");

        start = start == null ? "0" : start;
        end = end == null ? "0" : end;
        play = play == null ? "0" : play;

        mStart = Integer.parseInt(start);
        mEnd = Integer.parseInt(end);
        mPlay = Integer.parseInt(play);
        String cId = token == null ? "" : token.split("_")[0];
        String path;
        if (mStart == 0 || mEnd == 0) {
            path = String.format("http://api.topvdn.com/v2/record/%s/timeline?client_token=%s", cId, token);
        } else {
            path = String.format("http://api.topvdn.com/v2/record/%s/timeline?client_token=%s&start=%s&end=%s", cId, token, mStart, mEnd);
        }
        com.antelope.sdk.utils.CLog.i("test Record doGet before path=" + path);

        mSegment = HttpUtil.doGet(path);
    }

    private void initStreamer(String url) {
        // 流化器
        mStreamer = ACStreamerFactory.createStreamer(mPlayType);
        mStreamer.initialize(new ACMessageListener() {

            @Override
            public void onMessage(int type, Object message) {

            }

        });
        com.antelope.sdk.utils.CLog.i("test Record streamerOpen before mSegment=" + mSegment);
        mStreamer.open(url, 3000, mSegment, new ACResultListener() {

            @Override
            public void onResult(ACResult status) {
                com.antelope.sdk.utils.CLog.i("test Record streamerOpen result result=" + status.getCode());
                // TODO 有不同的消息类型，包括连接成功，连接断开，连接异常等，待处理
                if (status.isResultOK()) {
                    HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_CONNECTED);
                    mCurrentState = STATE_PREPARED;
                    if (mFlagStart && !isStartPlay) {
                        isStartPlay = true;
                        play();
                        HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_RECORD_PROGRESS, 1000);
                    }
                } else {
                    mCurrentState = STATE_ERROR;
                    HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_ERROR, status);

                }
                CLog.i("failed to open streamer in LYPlayer" + status.getCode());
            }
        });

    }

    // /**
    // * 配置播放缓冲时长，可选择性调用 ，必须在{@link #start()} 之前调用
    // *
    // * @param playOptions
    // */
    // public void setPlayOptions(PlayOptions playOptions) {
    // mPlayOptions = playOptions;
    //// if (mPlayer != null) {
    //// mPlayer.setPlayOptions(mPlayOptions);
    //// }
    // }
    //
    // @Subscribe
    // public void onEvent(PlayEvent event) {
    // CLog.i("PlayType=" + event.getPlayType() + " msg="
    // + event.getMsg().toString());
    // switch (event.getPlayType()) {
    // case Record:
    // processRecordEvent(event.getMsg());
    // break;
    // case AULP:
    // case ATLP:
    // case RTMP:
    // processLiveEvent(event.getMsg());
    // break;
    // default:
    // break;
    //
    // }
    // }
    //
    // @Subscribe
    // public void onEvent(SnapshotEvent event) {
    // if (mOnSnapshotListener == null)
    // return;
    // if (event.isSuccess()) {
    // mOnSnapshotListener.onSnapshotSuccess(event.getFullSavePath());
    // } else {
    // mOnSnapshotListener.onSnapshotFail(event.getLingYangException());
    // }
    // }
    //
    // @Subscribe
    // public void onEvent(LocalRecordEvent event) {
    // if (mLocalRecordListener == null)
    // return;
    // if (event instanceof LocalRecordErrorEvent) {
    // mLocalRecordListener.onRecordError(((LocalRecordErrorEvent) event)
    // .getLingYangException());
    // } else if (event instanceof LocalRecordSizeChangeEvent) {
    // mLocalRecordListener.onRecordSizeChange(
    // ((LocalRecordSizeChangeEvent) event).getRecordSize(),
    // ((LocalRecordSizeChangeEvent) event).getRecordTime());
    // } else if (event instanceof LocalRecordStateChangeEvent) {
    // LocalRecordStateChangeEvent localRecordStateChangeEvent =
    // (LocalRecordStateChangeEvent) event;
    // switch (localRecordStateChangeEvent.getState()) {
    // case LocalRecordStateChangeEvent.STATE_START:
    // mLocalRecordListener.onRecordStart();
    // break;
    // case LocalRecordStateChangeEvent.STATE_STOP:
    // mLocalRecordListener.onRecordStop();
    // break;
    // default:
    // break;
    // }
    // }
    // }
    //
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CLog.d("onConfigurationChanged:" + System.currentTimeMillis() / 1000);
        resetRatio();
    }

    /**
     * 设置画面显示比例
     *
     * @param screenType {@link #TYPE_PLAYER_RATIO_PROP_BEST}
     *                   {@link #TYPE_PLAYER_RATIO_PROP_FULL}
     *                   {@link #TYPE_PLAYER_RATIO_PROP_16X9} or
     *                   {@link #TYPE_PLAYER_RATIO_PROP_4X3} or
     *                   {@link #TYPE_PLAYER_RATIO_PROP_FULL} or
     *                   {@link #TYPE_PLAYER_RATIO_PROP_9X16}
     */
    public void setScreenRatio(int screenType) {
        mCurrentScreenType = screenType;
        resetRatio();
    }

    /**
     * 设置全屏播放 指将画面以全屏渲染，但是只能显示surface大小的画面
     *
     * @param isFullScreen
     */
    public void setFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            setContainerViewSize(0, 0);
        } else {
            setContainerViewSize(mMinWith, mMinHeight);
        }
        resetRatio();
    }

    /**
     * 获取非全屏的宽
     * <p/>
     * {@hide}
     *
     * @return
     */
    public int getMinWidth() {
        return mMinWith;
    }

    /**
     * 获取非全屏的高
     * <p/>
     * {@hide}
     *
     * @return
     */
    public int getMinHeight() {
        return mMinHeight;
    }

    /**
     * 进入后台时调用此方法暂停播放
     */
    public void pauseToBackground() {
        /*
         * Log.d(TAG, "pauseToBackground isStartPlay: " +
		 * mPlayer.isStartPlay()); if (mPlayer.isStartPlay()) { mPlayer.pause();
		 * mPlayer.removeSurfaceHolder(); if (isJellyBeanMr2()) { //
		 * 4.3及以上的系统需要将surfaceview移除 removeSurface(); } } else {
		 * mPlayer.setCanStartPlay(false); }
		 */
        // if (isJellyBeanMr2()) {
        // CLog.v("pauseToBackground");
        // // 4.3及以上的系统需要将surfaceview移除
        // }
        // removeSurfaceView();
        // mLYGLPlayerView.onPause();
        mPlayer.enterBackground();
        mute();
    }

    /**
     * 从后台进入前台时
     */
    public void resumeFromBackground() {
        /*
		 * if (mPlayer.isStartPlay()) { if (isJellyBeanMr2()) { //
		 * 4.3及以上的系统需要重新创建surfaceview reInitSurface(); }
		 * mPlayer.setSurfaceHolder(mSurfaceHolder); if (!mIsPause) {
		 * mPlayer.resumePlay(); } } else { mPlayer.setCanStartPlay(true); }
		 */
        // if (isJellyBeanMr2()) {
        // // 4.3及以上的系统需要重新创建surfaceview
        // CLog.v("resumeFromBackground");
        // }
        // initSurface();
        // mPlayerFactory = new PlayerFactory(getContext());
        // mLYGLPlayerView.onResume();
        if (mPlayer != null)
            mPlayer.enterForeground();
        unmute();
    }

    private boolean mFlagStart = false;

    /**
     * 开始播放
     */
    @Override
    public void start() {
        // if (!isInPlaybackState()) {
        // mPlayer.start();
        // mCurrentState = STATE_PLAYING;
        // }
        // mTargetState = STATE_PLAYING;
        // mPlayer.start();
        mFlagStart = true;
        if (mCurrentState == STATE_PREPARING) {
            return;
        } else if (mCurrentState != STATE_PREPARED)
            return;
        if (isStartPlay)
            return;
        else if (mCurrentState == STATE_PREPARED) {
            isStartPlay = true;
            play();
            HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_RECORD_PROGRESS, 1000);
        }
    }

    private int mFrameCount;

    private void play() {
        mWorkThreadExecutor.executeTask(new Runnable() {
            public void run() {
                int i = 0;
                while (isStartPlay) {
                    mCurrentState = STATE_PLAYING;
                    ACStreamPacket packet = new ACStreamPacket();
                    packet.buffer = ByteBuffer.allocateDirect(1024 * 1024);
                    if (mStreamer.read(packet, 1000).isResultOK()) {
                        mPlayer.playFrame(packet);
                    }

                }
                mFrameCount = 0;
                mCurrentState = STATE_PREPARED;
            }
        });
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        // if (isInPlaybackState()) {
        // if (mPlayer.isPlaying()) {
        // mPlayer.pause();
        // mCurrentState = STATE_PAUSED;
        // }
        // }
        // mTargetState = STATE_PAUSED;
        mPlayer.pause();
        mCurrentState = STATE_PAUSED;
    }

    private boolean isTalk = false;

    /**
     * 开启音频采集，语音对讲
     */
    public void startTalk() {
//        if (mCurrentState != STATE_PREPARING)
//            return;
        if (mAudioEncoder == null)
            initAudio();
        isTalk = true;
    }

    ;

    private void initAudio() {
        // 音频采集
        if (mAudioCapture == null) {
            mAudioCapture = new ACAudioCapturer();
            mAudioCapture.initialize(16000, 1, 16, mAudioCaptureListener);
        }
        if (!mAudioCapture.isOpenMicrophone())
            mAudioCapture.openMicrophone(MediaRecorder.AudioSource.VOICE_COMMUNICATION);

        // 音频硬编
        mAudioEncoder = ACAudioEncoderFactory.createAudioEncoder(ACEncodeMode.AC_ENC_MODE_HARD,
                ACCodecID.AC_CODEC_ID_AAC);
//        if (!mAudioEncoder.initialize(16000, 1, 16, 500, mAudioEncodeListrener).isResultOK()) {
//            mAudioEncoder = ACAudioEncoderFactory.createAudioEncoder(ACEncodeMode.AC_ENC_MODE_SOFT,
//                    ACCodecID.AC_CODEC_ID_AAC);
        mAudioEncoder.initialize(16000, 1, 16, 255, mAudioEncodeListrener);
//        }
    }

    /**
     * 音频采集原始数据回调
     */
    private ACFrameAvailableListener mAudioCaptureListener = new ACFrameAvailableListener() {

        @Override
        public void onFrameAvailable(ACFrame frame) {
            if (isTalk)
                mAudioEncoder.encode((ACAudioFrame) frame);
        }
    };

    /**
     * 音频编码数据回调
     */
    private ACPacketAvailableListener mAudioEncodeListrener = new ACPacketAvailableListener() {

        @Override
        public void onPacketAvailable(ACStreamPacket packet) {
            if (isTalk)
                mStreamer.write(packet);
        }
    };

    // Runnable talkRunnable = new Runnable() {
    //
    // @Override
    // public void run() {
    // ACAudioFrame frame = new ACAudioFrame();
    // ACStreamPacket packet = new ACStreamPacket();
    // packet.buffer = ByteBuffer.allocateDirect(1024 * 1024);
    // while (isTalk) {
    // if (mAudioCapture.readAudioFrame(frame).isResultOK()) {
    // mAudioEncoder.encode(frame, packet);
    // mStreamer.write(packet);
    // }
    // }
    // }
    // };

    /**
     * 关闭音频采集
     */
    public void stopTalk() {
        isTalk = false;
    }

    /**
     * 获取时长，仅在录像播放状态下有效
     *
     * @return
     */
    // @Override
    // public int getDuration() {
    //// return mPlayer.getDuration();
    //// mPlayer.
    // }
    @Override
    public int getCurrentPosition() {
        if (mPlayer != null)
            return (int) mPlayer.getPosition() - mStart;
        return -1;
    }

    @Override
    public void seekTo(int pos) {
        if (pos < 0 || mPlayType != ACProtocolType.AC_PROTOCOL_RECORD || (pos + mStart) > mEnd)
            return;
        mStreamer.seek(getCurrentPosition() + pos, new ACResultListener() {

            @Override
            public void onResult(ACResult status) {
                HandlerUtil.sendMsgToHandler(mUIHandler, PLAYER_SEEK_COMPLETE, status);
            }
        });
        ;
    }

    @Override
    public boolean isPlaying() {
        return mPlayer != null && mCurrentState == STATE_PLAYING;
    }

    // @Override
    // public int getBufferPercentage() {
    // return mPlayer.getBufferPercentage();
    // }
    //
    @Override
    public boolean canPause() {
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD)
            return true;
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD && mPlayer.getPosition() > mStart)
            return true;
        return false;
    }

    @Override
    public boolean canSeekForward() {
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD && mPlayer.getPosition() < mEnd)
            return true;
        return false;
    }
    //
    // /**
    // * 未实现
    // *
    // * @return
    // * @hide
    // */
    // @Override
    // public int getAudioSessionId() {
    // return 0;
    // }

    /**
     * 恢复播放
     */
    public void resume() {
        // if (isInPlaybackState()) {
        // if (mPlayer.isPlaying()) {
        // mPlayer.pause();
        // mCurrentState = STATE_PAUSED;
        // }
        // }
        // mTargetState = STATE_PAUSED;
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }

    /**
     * 本地截图
     *
     * @param snapPath 保存路径
     * @param name     截图名称
     * @param listener 消息回调
     */
    public void snapshot(final String snapPath, final String name, OnSnapshotListener listener) {
        mOnSnapshotListener = listener;
        String path = snapPath + name + ".jpg";
        ACResult result = mPlayer.snapshot(path);
        if (result.isResultOK()) {
            if (mOnSnapshotListener != null) {
                mOnSnapshotListener.onSnapshotSuccess(path);
            }
        } else if (mOnSnapshotListener != null) {
            mOnSnapshotListener.onSnapshotFail(new LYException(result.getCode(), result.getErrMsg()));
        }
    }

    /**
     * 开始本地录制
     *
     * @param filePath 录制文件存放目录
     */
    public void startLocalRecord(final String filePath) {
        if (isPlaying()) {
            ACResult result = mPlayer.startRecord(filePath, ACMediaType.AC_MEDIA_TYPE_AUDIO | ACMediaType.AC_MEDIA_TYPE_VIDEO);
            if (result.isResultOK()) {
                if (mLocalRecordListener != null) {
                    mLocalRecordListener.onRecordStart();
                }
            } else {
                if (mLocalRecordListener != null) {
                    mLocalRecordListener.onRecordError(new LYException(result.getCode(), result.getErrMsg()));
                }
            }
        }
    }

    /**
     * 设置本地录制状态回调
     *
     * @param onLocalRecordListener
     */
    public void setLocalRecordListener(OnLocalRecordListener onLocalRecordListener) {
        mLocalRecordListener = onLocalRecordListener;
    }

    /**
     * 停止本地录制
     */
    public void stopLocalRecord() {
        ACResult result = mPlayer.stopRecord();
        if (result.isResultOK()) {
            if (mLocalRecordListener != null) {
                mLocalRecordListener.onRecordStop();
            }
        } else if (mLocalRecordListener != null) {
            mLocalRecordListener.onRecordError(new LYException(result.getCode(), result.getErrMsg()));
        }
    }

    /**
     * 重置播放器
     */
    public void reset() {
        CLog.d("reset");
        if (mPlayer != null) {
            mPlayer.clearQueueBuffer();
            stop();
            mPlayer = null;
            mPlayOptions = null;
        }
        if (mStreamer != null) {
            mStreamer.release();
            mStreamer = null;
        }
        onDetachedFromWindow();
//        removeSurfaceView();

        initData();
    }

    /**
     * 静音
     */
    public void mute() {
        if (mPlayer != null) {
            mPlayer.mute();
        }
    }

    /**
     * 开启声音
     */
    public void unmute() {
        if (mPlayer != null) {
            mPlayer.unmute();
        }
    }

    /**
     * 获取流媒体参数
     *
     * @param paramType {@link IMediaParamProtocol}
     * @return
     */
    public String getMediaParam(int paramType) {
        String info = "0";
        if (mPlayer != null) {
            if (paramType == IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_WIDTH) {
                info = getWidth() + "";
            } else if (paramType == IMediaParamProtocol.STREAM_MEDIA_PARAM_RATIO_HEIGHT) {
                info = getHeight() + "";
            } else {
                switch (paramType){
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_VIDEO_BITRATE:
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_AUDIO_BITRATE:
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_VIDEO_FRAMERATE:
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_AUDIO_FRAMERATE:
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_BUFFER_TIME:
                    case ACMediaInfo.AC_MEDIA_INFO_PLAYER_DELAY_TIME:
                        info = mPlayer.getMediaInfo(paramType);
                        break;
                    default:
                        info = mStreamer.getMediaInfo(paramType);
                }
            }
        }

        return (info == null || info.equals("")) ? "0" : info;
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mPlayer != null && isStartPlay) {
            isStartPlay = false;
            mFlagStart = false;
        }
        if (mStreamer != null) {
            mStreamer.close();
        }
        if (mOnclosedListener != null) {
            mOnclosedListener.onClosed();
        }
    }

    /**
     * 注册播放进度回调监听
     *
     * @param playProgressListener
     */
    public void setOnPlayProgressListener(OnPlayProgressListener playProgressListener) {
        if (mPlayType == ACProtocolType.AC_PROTOCOL_RECORD) {
            mPlayProgressListener = playProgressListener;
        }
    }

    /**
     * 注册媒体文件装载时或即将播放时被调用的回调函数
     *
     * @param preparedListener
     */
    public void setOnPreparedListener(OnPreparedListener preparedListener) {
        // mPlayer.setOnPreparedListener(preparedListener);
        mOnPreparedListener = preparedListener;
    }

    /**
     * 注册媒体播放链路建立成功时被调用的回调函数
     *
     * @param connectedListener
     * @hide
     */
    private void setOnConnectedListener(OnConnectedListener connectedListener) {
        mOnConnectedListener = connectedListener;
    }

    /**
     * 注册媒体文件播放结束后被调用的回调函数
     *
     * @param completionListener
     */
    public void setOnCompletionListener(OnCompletionListener completionListener) {
        mOnCompletionListener = completionListener;
    }

    /**
     * 注册在播放和建立播放期间发生错误被调用的回调函数 如果回调函数没有注册，或者回调函数返回错误, 将不返回用户任何错误
     *
     * @param errorListener
     */
    public void setOnErrorListener(OnErrorListener errorListener) {
        mOnErrorListener = errorListener;
    }

    /**
     * 注册播放时缓存的回调函数.
     *
     * @param bufferListener
     */
    public void setOnPlayingBufferCacheListener(OnPlayingBufferCacheListener bufferListener) {
        mOnBufferListener = bufferListener;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener seekCompleteListener) {
        mOnSeekCompleteListener = seekCompleteListener;
    }

    public void setOnClosedListener(OnClosedListener closedListener) {
        mOnclosedListener = closedListener;
    }

    @Override
    protected void onAttachedToWindow() {
        CLog.v("onAttachedToWindow");
        super.onAttachedToWindow();
    }

    // activity销毁时自动调用该方法
    @Override
    protected void onDetachedFromWindow() {
        if (mPlayer != null) {
            stop();
            mPlayer.clearQueueBuffer();
            mPlayer.release();
            mPlayer = null;
        }
        if (mStreamer != null) {
            mStreamer.close();
            mStreamer.release();
            mStreamer = null;
        }
        if (mWorkThreadExecutor != null) {
            mWorkThreadExecutor.stop();
            mWorkThreadExecutor.release();
            mWorkThreadExecutor = null;
        }
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
            mUIHandler = null;
        }
        removeSurfaceView();
        CLog.v("onDetachedFromWindow");
        // EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        CLog.d("onLayout changed:" + changed + ",getWidth:" + getWidth()
                + ",getHeight:" + getHeight() + ",mMinWith:" + mMinWith);
        if (changed) {
            mMinWith = getWidth();
            mMinHeight = getHeight();
            // if (mMinWith == mMinHeight)
            // return;
            setContainerViewSize(getWidth(), getHeight());
            reInitSurface();
        }
        super.onLayout(changed, l, t, r, b);

    }

    //
    // protected void removeSurfaceView() {
    // if (getChildCount() > 0 && getChildAt(0).equals(mLYGLPlayerView)) {
    // removeView(mLYGLPlayerView);
    // mLYGLPlayerView = null;
    // CLog.v("removeSurfaceView");
    // }
    //
    // }
    //
    protected void resetRatio() {
        if (mIsFitScreen)
            setRatioModel(mCurrentScreenType, mVideoWidth, mVideoHeight);
        else
            setRatioModel(mCurrentScreenType, getWidth(), getHeight());
    }

    /**
     * 设置播放数据回调处理
     *
     * @param aFrameListener
     */
    public void setVideoFrameCallback(OnPlayerVideoFrameListener aFrameListener, int frameFormat) {
        // mPlayer.setVideoFrameCallback(aFrameListener, frameFormat);
        mPlayVideoFrameListener = aFrameListener;
    }

    ;

    private OnPlayerVideoFrameListener mPlayVideoFrameListener;

    /**
     * 设置顶层布局
     *
     * @param isTop
     */
    public void setZOrderOnTop(boolean isTop) {
        if (mLYGLPlayerView != null) {
            mLYGLPlayerView.setZOrderOnTop(isTop);
        }
    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        // TODO Auto-generated method stub
        return getCurrentPosition() / (mEnd - mStart);
    }

    @Override
    public int getDuration() {
        // TODO Auto-generated method stub
        return mEnd - mStart;
    }

}

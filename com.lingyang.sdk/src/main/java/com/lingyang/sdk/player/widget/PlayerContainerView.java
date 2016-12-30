package com.lingyang.sdk.player.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.lingyang.sdk.player.IScreenRatio;
import com.lingyang.sdk.util.CLog;

abstract class PlayerContainerView extends RelativeLayout implements IScreenRatio {

    protected long mMediaHandler;
    private LYGLPlayerView mGLSurfaceView;
    // 播放视图的矩形区域顶点
    private int mSurfaceviewLeft = 0;
    private int mSurfaceviewTop = 0;
    private int mSurfaceviewRight = 0;
    private int mSurfaceviewBottom = 0;
    private int mContainerWidth, mContainerHeight;
    private boolean isCreated=false;

    protected PlayerContainerView(Context context) {
        this(context, null);
    }

    protected PlayerContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected PlayerContainerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 获取播放视窗的显示属性
     *
     * @param videoWidth  数据宽高
     * @param videoHeight
     */
    private LayoutParams getSurfaceviewLayoutParams(int screenMode, int videoWidth,
                                                    int videoHeight) {
        // Log.i("player surfaceview", "setScreenMode:" + screenMode);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float scale = getResources().getDisplayMetrics().density;
        // int viewWidth = getWidth();
        // int viewHeight = getHeight();
        int viewWidth = mContainerWidth; //视图宽高
        int viewHeight = mContainerHeight;
        CLog.v("screenWidth:" + screenWidth + " viewWidth: " + viewWidth + " screenHeight: "
                + screenHeight + " viewHeight: " + viewHeight + " scale:" + scale + "videoWidth:"
        + videoWidth + "videoHeight:" + videoHeight);
        //
        if (viewWidth == 0 || viewHeight == 0) {
            // 如果视图宽高为0，当前还未初始化，直接使用屏幕宽高
            viewWidth = screenWidth;
            viewHeight = screenHeight;
        }

        int width = 0;
        int height = 0;
        CLog.v("screenMode:" + screenMode + " screenWidth:" + screenWidth + " viewWidth: " + viewWidth + " screenHeight: "
                + screenHeight + " viewHeight: " + viewHeight + " scale:" + scale);
        switch (screenMode) {
            case TYPE_PLAYER_RATIO_PROP_BEST:
                // 等比最佳效果
                if (videoHeight > 0 && videoWidth > 0) {
                    width = Math.min(videoWidth * viewHeight / videoHeight, viewWidth);
                    height = Math.min(videoHeight * viewWidth / videoWidth, viewHeight);
                } else {
                    width = Math.min(4 * viewHeight / 3, viewWidth);
                    height = Math.min(3 * viewWidth / 4, viewHeight);
                }
                break;
            case TYPE_PLAYER_RATIO_PROP_FULL:
                // 等比全屏
                if (videoHeight > 0 && videoWidth > 0) {
                    width = Math.max(videoWidth * viewHeight / videoHeight, viewWidth);
                    height = Math.max(videoHeight * viewWidth / videoWidth, viewHeight);
                } else {
                    width = Math.min(4 * viewHeight / 3, viewWidth);
                    height = Math.min(3 * viewWidth / 4, viewHeight);
                }
                break;
            case TYPE_PLAYER_RATIO_PROP_16X9:
                width = Math.min(16 * viewHeight / 9, viewWidth);
                height = Math.min(9 * viewWidth / 16, viewHeight);
                break;
            case TYPE_PLAYER_RATIO_PROP_9X16:
                width = Math.min(9 * viewHeight / 16, viewWidth);
                height = Math.min(16 * viewWidth / 9, viewHeight);
                break;

            case TYPE_PLAYER_RATIO_PROP_5X4:
                width = Math.min(5 * viewHeight / 4, viewWidth);
                height = Math.min(4 * viewWidth / 5, viewHeight);
                break;
            case TYPE_PLAYER_RATIO_PROP_4X3:
                width = Math.min(4 * viewHeight / 3, viewWidth);
                height = Math.min(3 * viewWidth / 4, viewHeight);
                break;
            case TYPE_PLAYER_RATIO_PROP_3X4:
                width = Math.min(3 * viewHeight / 4, viewWidth);
                height = Math.min(4 * viewWidth / 3, viewHeight);
                break;
            default:
                width = Math.min(4 * viewHeight / 3, viewWidth);
                height = Math.min(3 * viewWidth / 4, viewHeight);
        }

        setSurfaceviewPosition(viewWidth, viewHeight, width, height);

        CLog.v("viewWidth:" + viewWidth + " viewHeight:" + viewHeight + " draw width:" + width
                + " draw height:" + height + " videoWidth:" + videoWidth + " videoHeight:"
                + videoHeight);

        LayoutParams layoutParams = new LayoutParams(width, height);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        CLog.v("getSurfaceviewLayoutParams()");
        return layoutParams;
    }

    /**
     * 设置播放视窗的位置
     *
     * @param videoWidth
     * @param videoHeight
     */
    private void setSurfaceviewPosition(int viewWidth, int viewHeight, int videoWidth,
                                        int videoHeight) {
        mSurfaceviewLeft = (viewWidth - videoWidth) / 2;
        mSurfaceviewTop = (viewHeight - videoHeight) / 2;
        mSurfaceviewRight = mSurfaceviewLeft + videoWidth;
        mSurfaceviewBottom = mSurfaceviewTop + videoHeight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//    	if(isCreated&&!changed){
//    		return;
//    	}
    	CLog.d("onLayout l:" + l + ",t:" + t
    			+ ",r:" + r + ",b:" + b);
        super.onLayout(changed, l, t, r, b);
        if (mGLSurfaceView != null) {
            mGLSurfaceView.layout(mSurfaceviewLeft, mSurfaceviewTop,
                    mSurfaceviewRight, mSurfaceviewBottom);
            
            isCreated=true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // performSetRatioModel(mRatio, mSurfaceWidth, mSurfaceHeight);
    }

    protected void addSurfaceView(LYGLPlayerView surfaceView, int ratioModel) {
        if (surfaceView != null) {
            mGLSurfaceView = surfaceView;
            LayoutParams params = getSurfaceviewLayoutParams(ratioModel, 0, 0);
            addView(mGLSurfaceView, params);
        }
    }

    protected void removeSurfaceView() {
        if (mGLSurfaceView != null) {
            removeView(mGLSurfaceView);
        }
    }

    protected void performSetRatioModel(int ratioModel, int videoWidth, int videoHeight) {
        if (mGLSurfaceView != null) {
            LayoutParams params = getSurfaceviewLayoutParams(ratioModel, videoWidth,
                    videoHeight);
            try {
                mGLSurfaceView.setLayoutParams(params);
            } catch (Exception e) {
            }

        }
    }

    protected void setRatioModel(int ratioModel, int videoWidth, int videoHeight) {
        performSetRatioModel(ratioModel, videoWidth, videoHeight);
    }

    /**
     * 设置包裹surfaceview的容器视图的宽高， 默认为当前屏幕的宽高
     *
     * @param width
     * @param height
     */
    protected void setContainerViewSize(int width, int height) {
        mContainerWidth = width;
        mContainerHeight = height;
        CLog.v("setContainerViewSize mContainerWidth-" + mContainerWidth + "mContainerHeight-"
                + mContainerHeight);
    }
}

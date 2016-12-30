package com.lingyang.base.widget;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.lingyang.base.utils.Log;

/**
  * @ClassName: GestureDetectorExpand
  * @Description: 手势扩展，增加水平滚动，左右竖向滚动手势
  */
public class GestureDetectorExpand implements OnGestureListener, OnDoubleTapListener {
	private static final String TAG = "GestureDetectorExpand";
	private static final int SCROLL_NULL = 0;
	private static final int SCROLL_HORIZONTAL = 1;
	private static final int SCROLL_VERTICAL_LEFT = 2;
	private static final int SCROLL_VERTICAL_RIGHT = 3;

	private OnGestureExpandListener mListener;
	private GestureDetector mGestureDetector;
	private int mWidth, mHeight;
	private float mContentCenterX = 0;
	private int mScrollType;

	public GestureDetectorExpand(Context context, OnGestureExpandListener listener) {
		initListener(listener);
		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setOnDoubleTapListener(this);
	}

	public GestureDetectorExpand(Context context, OnGestureExpandListener listener, Handler handler) {
		initListener(listener);
		mGestureDetector = new GestureDetector(context, this, handler);
		mGestureDetector.setOnDoubleTapListener(this);
	}

	public GestureDetectorExpand(Context context, OnGestureExpandListener listener, Handler handler,
			boolean unused) {
		initListener(listener);
		mGestureDetector = new GestureDetector(context, this, handler, unused);
		mGestureDetector.setOnDoubleTapListener(this);
	}

	public void setTouchDist(int width, int height) {
		mWidth = width;
		mHeight = height;
		mContentCenterX = width / 2;
	}

	private void initListener(OnGestureExpandListener listener) {
		mListener = listener;
		if (mListener == null) {
			throw new NullPointerException();
		}
	}

	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();

		// 弹起 重置手势的参数
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			if (mScrollType == SCROLL_HORIZONTAL) {
				mListener.onHorizontalScroll(0, 0, OnGestureExpandListener.STATE_END);
			} else if (mScrollType == SCROLL_VERTICAL_LEFT) {
				mListener.onVerticalLeftScroll(0, 0, OnGestureExpandListener.STATE_END);
			} else if (mScrollType == SCROLL_VERTICAL_RIGHT) {
				mListener.onVerticalRightScroll(0, 0, OnGestureExpandListener.STATE_END);
			}
			mScrollType = SCROLL_NULL;
		}
		return mGestureDetector.onTouchEvent(ev);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return mListener.onDown(e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return mListener.onFling(e1, e2, velocityX, velocityY);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		mListener.onLongPress(e);
	}

	private void computeHorizontalScroll(float distance) {
		final float width = mWidth;
		float increase = distance / width;
		mListener.onHorizontalScroll(distance, increase, OnGestureExpandListener.STATE_PROCESSING);
	}

	private void computeVerticalScroll(float distance, int scrollType) {
		final float d = distance;
		final float height = mHeight;
		float increase = d / height;
		if (scrollType == SCROLL_VERTICAL_LEFT) {
			mListener.onVerticalLeftScroll(distance, increase, OnGestureExpandListener.STATE_PROCESSING);
		} else {
			mListener.onVerticalRightScroll(distance, increase, OnGestureExpandListener.STATE_PROCESSING);
		}
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (e1 == null || e2 == null || mListener!=null && mListener.onScroll(e1, e2, distanceX, distanceY)==false) {
			Log.d(TAG, "null or isPoint");
			return false;
		}
		// Log.i(TAG, "onScroll e1 X: " + e1.getX() + " e1 Y: " + e1.getY() +
		// " e2 X: " + e2.getX() + " e2 Y: " + e2.getY() + " distanceX: "
		// + distanceX + " distanceY: " + distanceY);
		final float dx = distanceX;
		final float dy = distanceY;

		if (mScrollType == SCROLL_NULL) {
			final float x = e1.getX();
			final float absX = Math.abs(dx);
			final float absY = Math.abs(dy);

			if (absX > absY) {
				// 横向滚动 调节播放进度
				mScrollType = SCROLL_HORIZONTAL;
				mListener.onHorizontalScroll(dx, 0, OnGestureExpandListener.STATE_BEGAIN);
			} else {
				if (x < mContentCenterX) {
					mScrollType = SCROLL_VERTICAL_LEFT;
					mListener.onVerticalLeftScroll(dx, 0, OnGestureExpandListener.STATE_BEGAIN);
				} else {
					mScrollType = SCROLL_VERTICAL_RIGHT;
					mListener.onVerticalRightScroll(dx, 0, OnGestureExpandListener.STATE_BEGAIN);
				}
			}
		} else if (mScrollType == SCROLL_HORIZONTAL) {
			computeHorizontalScroll(dx);
		} else {
			computeVerticalScroll(dy, mScrollType);
		}

		return mListener.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public void onShowPress(MotionEvent e) {
		mListener.onShowPress(e);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return mListener.onSingleTapUp(e);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		return mListener.onDoubleTap(e);
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return mListener.onDoubleTapEvent(e);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return mListener.onSingleTapConfirmed(e);
	}

}

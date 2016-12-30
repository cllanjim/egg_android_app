package com.lingyang.base.widget;

import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;


/**
  * @ClassName: OnGestureExpandListener
  * @Description: 手势扩展回调，增加水平滚动，左右竖向滚动回调
  */
public interface OnGestureExpandListener extends OnGestureListener, OnDoubleTapListener {
	public static final int STATE_BEGAIN = 0;
	public static final int STATE_PROCESSING = 1;
	public static final int STATE_END = 2;

	/**
	 * 左边区域竖向滚动
	 * 
	 * @param distanceY
	 *            竖向滚动的距离
	 * @param rate
	 *           滑动的距离 /滑动的高度 
	 * @param state
	 *            状态；0：开始；1：滚动中；2：结束；
	 */
	void onVerticalLeftScroll(float distanceY, float rate, int state);

	/**
	 * 右边区域竖向滚动
	 * 
	 * @param distanceY
	 *            竖向滚动的距离
	 * @param rate
	 *           滑动的距离 /滑动的高度 
	 * @param state
	 *            状态；0：开始；1：滚动中；2：结束；
	 */
	void onVerticalRightScroll(float distanceY, float rate, int state);

	/**
	 * 横向滚动
	 * 
	 * @param distanceX
	 *            横向滚动的距离
	 * @param rate
	 *           滑动的距离 /滑动的高度 
	 * @param state
	 *            状态；0：开始；1：滚动中；2：结束；
	 */
	void onHorizontalScroll(float distanceX, float rate, int state);

}

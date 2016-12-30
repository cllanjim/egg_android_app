package com.lingyang.base.widget;

import android.view.MotionEvent;

public class SimpleGestureExpandListener implements OnGestureExpandListener{

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public void onVerticalLeftScroll(float distanceY, float rate, int state) {
	}

	@Override
	public void onVerticalRightScroll(float distanceY, float rate, int state) {
	}

	@Override
	public void onHorizontalScroll(float distanceX, float rate, int state) {
	}
}

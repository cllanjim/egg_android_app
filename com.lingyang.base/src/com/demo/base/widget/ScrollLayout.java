package com.lingyang.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.lingyang.base.utils.Log;

public class ScrollLayout extends ViewGroup {

	private static final String TAG = "SlideViewScrollLayout";

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mCurrentScreen;
	private int mDefaultScreen = 0;
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private static final int SNAP_VELOCITY = 600;
	private int mTouchState = TOUCH_STATE_REST;
	private float mTouchSlop;
	private float mLastMotionX;
	private boolean mLoopable = true;
	private boolean mIsScroolBegin = false;
	
	public ScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		mCurrentScreen = mDefaultScreen;
		float density = getResources().getDisplayMetrics().density;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop() + 30 * density ;
		Log.d(TAG,"TouchSlop:" + mTouchSlop);
		//mTouchSlop = 35; // 大于SlideView的值
	}
	
	/**
	 * 设置X位移多少才截获事件
	 * @param touchSlop
	 */
	public void setTouchSlop(float touchSlop){
		mTouchSlop = touchSlop;
	}
	
	/**
	 * 设置是否可以循环滚动
	 * @param loopable
	 */
	public void setLoopable(boolean loopable){
		mLoopable = loopable;
	}
	
	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
	
		if(getChildCount() > 1){
			int scrollX = getScrollX();
			int scrollXForCurrentScreen = mCurrentScreen * getWidth();
			View nextOrPrevView = null;
			if(scrollX > scrollXForCurrentScreen){
				// 此时应该绘制下一个
				nextOrPrevView = getChildAt(getNextScreen());
			}else if(scrollX < scrollXForCurrentScreen){
				// 绘制上一个
				nextOrPrevView = getChildAt(getPrevScreen());
			}
			
			if(mIsLoopScrolling){
				super.drawChild(canvas, child, drawingTime);
			}else if(child == nextOrPrevView){
//				Log.d("douzi","drawNextOrprev:" + child);
				return super.drawChild(canvas, child, drawingTime);
			}
		}
		
		if(child == getChildAt(mCurrentScreen)){
//			Log.d("douzi","drawChild:" + child);
			return super.drawChild(canvas, child, drawingTime);
		}
		
		// don't draw if not current
		return true;
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		Log.e(TAG,"ScrollLayout onLayout");
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}
	
	public void reset(){
		mCurrentScreen = 0;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		Log.e(TAG,"ScrollLayout onMeasure");
//		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	public void setToScreen(int whichScreen) {
       // whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurrentScreen = whichScreen;
        scrollTo(whichScreen * getWidth(), 0);
    }

    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    public boolean isScrolling(){
    	if(mScroller == null){
    		return false;
    	}
    	return !mScroller.isFinished();
    }
    
    private boolean mIsLoopScrolling = false; // 是否正在进行循环滚动，比如从0到最后一个，或者从1到0
	public void snapToScreen(int newScreen, boolean  isEdgeLoop, boolean fireBeginMove) {
		Log.d(TAG," snapToScreen : " +newScreen + " count:" +getChildCount() + " oldScreen : " + mCurrentScreen);
		
		int targetScrollX = newScreen * getWidth();
		if (getScrollX() != targetScrollX * getWidth()) {         
		    int delta = targetScrollX - getScrollX();
		    
		    Log.e(TAG, "snapToScreen delta:" + delta);
		    if(!isEdgeLoop){
		        // 小于一个屏幕，认为不是从0到最后一个或者从最后一个到0
		        mIsScroolBegin = true;
		        if(fireBeginMove){
			    	mOnMoveListener.onScrollLayoutBeginMove(newScreen);
			    }
		        int duration = 250 + (int)(((float)Math.abs(delta) / getWidth()) * 250);
//		        Log.d("duration","duration:" + duration + " delta: " + delta);
		        if(duration > 500){
		        	duration = 500;
		        }
		    	mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		        invalidate();
		    }else{
		        mIsLoopScrolling = true;
		        if(mCurrentScreen > newScreen){
		            // 从 4 —> 0 Scroller 继续往右走一页
		             delta = getChildCount() * getWidth() - getScrollX();
		        }else if(mCurrentScreen < newScreen){
		            // 从 0 -> 4 Scrooller 继续往左走一页
		            delta = -1 * getWidth() - getScrollX();
		        }
		        mIsScroolBegin = true;
		        if(fireBeginMove){
			    	mOnMoveListener.onScrollLayoutBeginMove(newScreen);
			    }
		        mScroller.startScroll(getScrollX(), 0, delta, 0, 500);
		        invalidate();
		    }
		  
		    mCurrentScreen = newScreen;
        }
	}
	
	@Override
	public void computeScroll() {
	    if(mIsLoopScrolling){
	        if(mScroller.computeScrollOffset()){
	            // 这个时候正在跨边界运动
	            //mLoopingScrollX = mScroller.getCurrX();
	            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
	            postInvalidate();
	        }else{
	            setToScreen(mCurrentScreen);
	            mIsLoopScrolling = false;
	        }
	    }else if (mScroller.computeScrollOffset()) {
		//	Log.d(TAG,"computeScroll scroller computing");
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	    
	    if(mScroller.isFinished() && mIsScroolBegin){
	    	// 此时动画结束，回调 onMoveto
	    	 if (mOnMoveListener != null) {
		         mOnMoveListener.onScrollLayoutMoveTo(mCurrentScreen);
		     }
	    	 mIsScroolBegin = false;
	    }
	}
	
	boolean mAllowDispatchTouchEvent = true;
	
	public void setAllowDispatchTouchEvent(boolean allow){
	    mAllowDispatchTouchEvent = allow;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//	    Log.e(TAG,"dispatchTouchEvent " + UIUtils.getActionString(ev) + " x:" + mLastMotionX);
	    if(!mAllowDispatchTouchEvent){
	        return false;
	    }
	    boolean ret = super.dispatchTouchEvent(ev);
	    if(mTouchState == TOUCH_STATE_SCROLLING){
	        mLastMotionX = ev.getX();
	    }
	    return ret;
	}
	
	@Override
    protected void dispatchDraw(Canvas canvas) {
		
		if(!mLoopable){
			super.dispatchDraw(canvas);
			return;
		}
        
        int scrollX = getScrollX() ; 
        final int N = getChildCount();
        final int width = getWidth();
        if (scrollX < 0 && !mDoNotDrawExtraLeft) {
            View lastChild = getChildAt(N - 1);
            if(lastChild == null){
                return;
            }
            canvas.save();
            canvas.translate(-width, 0);
            canvas.clipRect(0, 0, width, getBottom());
            lastChild.draw(canvas);
            canvas.restore();
        } else if (scrollX > (N - 1) * width && !mDoNotDrawExtraRight) {
            View firstChild = getChildAt(0);
            if(firstChild == null){
                return;
            }
            canvas.save();
            canvas.translate(N * width, 0);
            canvas.clipRect(0, 0, width, getBottom());
            firstChild.draw(canvas);
            canvas.restore();
        }

        super.dispatchDraw(canvas);
    }
	
	private boolean mIsScrollable = true;
	public void setScrollable(boolean scrollable){
        mIsScrollable = scrollable;
    }
	
	private boolean mIsFirstCallBeginMove = true;
	private float mDownX;
	
	private int getNextScreen(){
	    int next = mCurrentScreen + 1;
	    if(next >= getChildCount()){
	    	if(mLoopable){
	    		next = 0;
	    	}else{
	    		next = mCurrentScreen;
	    	}
	    }
	    return next;
	}
	
	private int getPrevScreen(){
	    int prev = mCurrentScreen - 1;
	    if(prev < 0){
	    	if(mLoopable){
	    		prev = getChildCount() - 1;
	    	}else{
	    		prev = mCurrentScreen;
	    	}
	    }
	    return prev;
	}
	
	// 是否可以快速滑倒左边 ,手向右
	private boolean mCanFastScrollToLeft = false;
	public void setCanFastScroolToLeft(boolean enable){
		mCanFastScrollToLeft = enable;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.d(TAG,"onTouchEvent " + UIUtils.getActionString(event));
		if (!mIsScrollable) {
			return false;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				Log.d(TAG ," scroller not finished yet , abort animation");
				//mScroller.abortAnimation();
				return false;
			}
			mLastMotionX = x;
			mDownX = x;
			break;

		case MotionEvent.ACTION_MOVE:
			if(x - mDownX > 0 && !mCanSlideToRight){
//				Log.d(TAG, " scroll to right , ignore ");
				snapToScreen(mCurrentScreen, false, false);
				return true;
			}
			
			int deltaX = (int) (mLastMotionX - x);
			if (mIsFirstCallBeginMove) {
                if (mOnMoveListener != null) {
                    int nextScreen = 0;
                    if(deltaX > 0){
                        nextScreen = getNextScreen();
                    }else{
                        nextScreen = getPrevScreen();
                    }
                    
                    mOnMoveListener.onScrollLayoutBeginMove(nextScreen);
                }
                mIsFirstCallBeginMove = false;
            }
			mLastMotionX = x;
			if(mLoopable){
				scrollBy(deltaX, 0);
			}else{
				int scrollX = getScrollX();
				int totalWidth = (getChildCount() - 1) * getWidth();
				if(scrollX + deltaX <= 0){
					scrollTo(0, 0);
				}else if(scrollX + deltaX >= totalWidth){
					scrollTo(totalWidth, 0);
				}else{
					scrollBy(deltaX, 0);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(x - mDownX > 0 && !mCanSlideToRight){
				Log.d(TAG, " scroll to right , ignore ");
				return true;
			}
			// Log.e(TAG, "event : up");
			// if (mTouchState == TOUCH_STATE_SCROLLING) {
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity();
			// Log.e(TAG, "velocityX:"+velocityX);
			float offsetX = x -mDownX;
			
			if (velocityX > SNAP_VELOCITY) {
				// Fling enough to move left
				// Log.e(TAG, "snap left");
				//snapToScreen(getPrevScreen(), mCurrentScreen == 0);
				if(mCanFastScrollToLeft){
					snapToScreen(getPrevScreen(), mCurrentScreen == 0, false);
				}else{
					snapToScreen(mCurrentScreen, false, false);
				}
			} else if (velocityX < -SNAP_VELOCITY) {
				// Fling enough to move right
				snapToScreen(getNextScreen(), mCurrentScreen == getChildCount() - 1, false);
			} else if (offsetX > getWidth() / 2) {
				snapToScreen(getPrevScreen(), mCurrentScreen == 0, false);
			} else if (offsetX < -getWidth() / 2) {
				snapToScreen(getNextScreen(), mCurrentScreen == getChildCount() - 1, false);
			} else {
				snapToScreen(mCurrentScreen, false, false);
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			mIsFirstCallBeginMove = true;
			return false;
//		case MotionEvent.ACTION_CANCEL:
//		    mTouchState = TOUCH_STATE_REST;
//            mIsFirstCallBeginMove = true;
//            return false;
		}

		return true;
	}
	
	private boolean mCanSlideToRight = true; // 是否可以向右滑动 ，SlideView时不能向右滑动
	public void setCanSlideToRight(boolean canSlideToRight){
		mCanSlideToRight = canSlideToRight;
	}
	
	private float mDownY = 0;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		Log.d(TAG,"onInterceptTouchEvent " + UIUtils.getActionString(ev));
		if (!mIsScrollable){
//			Log.d(TAG, " can not scroll mode  return false ");
			return false;
		}
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) && (mTouchState != TOUCH_STATE_REST)) {
//			Log.d(TAG, " moving return true ");
			return true;
		}

		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if(x - mDownX > 0 && !mCanSlideToRight){
//				Log.d(TAG, " scroll to right , ignore ");
				return false;
			}
			final float xDiff = mDownX - x;
			final float yDiff = mDownY - ev.getY();
//			Log.d(TAG,"xDiff:" + xDiff + " touchSlop:" + mTouchSlop);
			if (Math.abs(xDiff) > mTouchSlop && Math.abs(yDiff) < mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mDownX = x;
			mDownY = ev.getY();
			//mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
//			break;
			boolean ret = super.onInterceptTouchEvent(ev);
			//Log.d(TAG, "intercepted ? " + ret);
			return ret;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			mIsFirstCallBeginMove = true;
			return false;
		}
		//Log.d(TAG, "intercepted ? " +  (mTouchState != TOUCH_STATE_REST));
		return mTouchState != TOUCH_STATE_REST;
	}

	private OnScrollLayoutMoveListener mOnMoveListener;

	public static interface OnScrollLayoutMoveListener {
		public void onScrollLayoutMoveTo(int index);

		/**
		 * 将要移动到哪个视图
		 */
		public void onScrollLayoutBeginMove(int index);
	}

	public void setOnMoveListener(OnScrollLayoutMoveListener l) {
		mOnMoveListener = l;
	}

	private boolean mDoNotDrawExtraLeft = false;
	
	public void setDoNotDrawExtraLeft(boolean doNotDraw){
		mDoNotDrawExtraLeft = doNotDraw;
	}
	
	private boolean mDoNotDrawExtraRight = false;
	
	public void setDoNotDrawExtraRight(boolean doNotDraw){
		mDoNotDrawExtraRight = doNotDraw;
	}
}

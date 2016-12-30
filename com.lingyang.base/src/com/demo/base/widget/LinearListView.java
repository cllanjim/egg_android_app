package com.lingyang.base.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.lingyang.base.utils.Log;

public class LinearListView extends LinearLayout implements OnClickListener{
	
	private final String TAG = "LinearListView";
	private BaseAdapter mAdapter;
	
	private View mFooterView;

	private OnItemClickListener mItemClickListener;

	public LinearListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && getChildCount() == 0) {
			notifyDatasetChanged();
		}
	}
	
	public void notifyDatasetChanged() {
		if(mAdapter == null || mAdapter.getCount() == 0) {
			Log.i(TAG, "notifyDatasetChanged - null");
			return;
		}
		Log.i(TAG, "notifyDatasetChanged - " + mAdapter.getCount());
		this.removeAllViews();
		int count = mAdapter.getCount();
		for(int i = 0;i < count;i++) {
			View itemView = mAdapter.getView(i, null, this);
			if (itemView == null) {
				continue;
			}
			itemView.setTag(i);
			itemView.setOnClickListener(this);
			this.addView(itemView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		}
		if (mFooterView != null) {
			this.addView(mFooterView);
			mFooterView.setTag(count);
			mFooterView.setOnClickListener(this);
		}
		requestLayout();
		Log.i(TAG, "notifyDatasetChanged - requestLayout");
	}

	public void notifyDataSetInvalidated() {
		this.removeAllViews();
	}
	
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(BaseAdapter adapter) {
		if (adapter == null) {
			Log.i(TAG, "setAdapter - null");
			return;
		}
		if (adapter == mAdapter) {
			Log.i(TAG, "setAdapter - same");
			mAdapter.notifyDataSetChanged();
			return;
		}
		this.mAdapter = adapter;
		this.mAdapter.registerDataSetObserver(new AdapterDataSetObserver());
		this.mAdapter.notifyDataSetChanged();
	}
	
	public void addFooterView(View v) {
		mFooterView = v;
	}
	
	public void removeFooterView(View v) {
		if (mFooterView != v) {
			return;
		}
		this.removeView(v);
		mFooterView = null;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		this.mItemClickListener = l;
	}

	@Override
	public void onClick(View v) {
		int position = this.indexOfChild(v);
		if (mItemClickListener != null) {
			mItemClickListener.onItemClick(this, v, position, Integer.parseInt(v.getTag().toString()));
		}
	}
	
	public interface OnItemClickListener {
        void onItemClick(ViewGroup parent, View view, int position, long id);
    }
	 
	class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
        	notifyDatasetChanged();
        }

        @Override
        public void onInvalidated() {
        	notifyDataSetInvalidated();
        }
    }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}
}


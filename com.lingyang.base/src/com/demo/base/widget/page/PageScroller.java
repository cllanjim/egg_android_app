package com.lingyang.base.widget.page;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lingyang.base.ResIdDynamicGenerator;
import com.lingyang.base.utils.Log;
import com.lingyang.base.widget.ScrollLayout;

public class PageScroller {
	
	private Context mContext;
	
	private ScrollLayout mScrollLayout;
	
	private BasePageAdapter mAdapter;
	
	public PageScroller(Context context, ScrollLayout scrollLayout) {
		this.mContext = context;
		this.mScrollLayout = scrollLayout;
	}

	public void setPageAdapter(BasePageAdapter adapter) {
		if (mAdapter == adapter) {
			return;
		}
		this.mAdapter = adapter;
		init();
	}
	
	private void init() {
		FrameLayout frameLayout = new FrameLayout(mContext);
		frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT));
		int id = ResIdDynamicGenerator.genarateResId();
		frameLayout.setId(id);
	}

}

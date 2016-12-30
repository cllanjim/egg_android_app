package com.lingyang.camera.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lingyang.camera.R;

public class FooterView extends LinearLayout {

	private Context mContext;

	public static final int HIDE = 0;
	public static final int MORE = 1;
	public static final int LOADING = 2;
	public static final int BADNETWORK = 3;

	private ProgressBar mProgressBar;
	private TextView mTextView;
	private Button mButton;

	private int mCurStatus;

	private OnClickListener mOnClickListener;

	public FooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public FooterView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		LayoutInflater.from(mContext).inflate(R.layout.view_list_no_connect, this, true);
		mProgressBar = (ProgressBar) findViewById(R.id.footer_loading);
		mTextView = (TextView) findViewById(R.id.footview_text);
		mButton = (Button) findViewById(R.id.footview_button);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
			}
		});

		setStatus(MORE);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
		super.setOnClickListener(mOnClickListener);
	}

	public void setStatus(int status) {
		mCurStatus = status;
		switch (status) {
		case HIDE:
			setVisibility(View.GONE);
			break;
		case MORE:
			mProgressBar.setVisibility(View.GONE);
			mButton.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText("点击加载更多");
			this.setVisibility(View.VISIBLE);
			break;
		case LOADING:
			mProgressBar.setVisibility(View.VISIBLE);
			mButton.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText("正在加载...");
			this.setVisibility(View.VISIBLE);
			break;
		case BADNETWORK:
			mProgressBar.setVisibility(View.GONE);
			mButton.setVisibility(View.VISIBLE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText("网络连接有问题");
			this.setVisibility(View.VISIBLE);
			break;
		}
	}

	public int getStatus() {
		return mCurStatus;
	}

}

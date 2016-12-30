/*
 */

package com.lingyang.camera.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lingyang.camera.R;

/**
 * 描述：Toast封装
 * 
 */
public class ToastUtil {

	private final Toast mToast;
	private final TextView mText;

	public ToastUtil(Context ctx) {
		mToast = new Toast(ctx);
		mText = (TextView) LayoutInflater.from(ctx).inflate(R.layout.layout_toast, null);

		mToast.setView(mText);
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
	}

	public ToastUtil duration(boolean l) {
		mToast.setDuration(l ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
		return this;
	}

	public ToastUtil gravity(int gravity, int xOffset, int yOffset) {
		mToast.setGravity(gravity, xOffset, yOffset);
		return this;
	}

	public ToastUtil show(int textId) {
		return show(0, textId);
	}

	public ToastUtil show(CharSequence text) {
		return show(0, text);
	}

	public ToastUtil show(int iconId, int textId) {
		final TextView t = mText;
		t.setText(textId);
		t.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
		t.setVisibility(View.VISIBLE);

		show_();
		return this;
	}

	public ToastUtil show(int iconId, CharSequence text) {
		final TextView t = mText;
		t.setText(text);
		t.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
		t.setVisibility(View.VISIBLE);

		show_();
		return this;
	}

	public ToastUtil hide() {
		mText.setVisibility(View.GONE);
		mToast.cancel();
		return this;
	}

	private void show_() {
		mToast.show();
	}

}

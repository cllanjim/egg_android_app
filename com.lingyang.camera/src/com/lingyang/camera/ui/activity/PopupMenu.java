package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.util.Utils;

/**
 * 文件名: PopupMenu
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/4/21
 */
public class PopupMenu extends PopupWindow {
    public static final int MENU_ADD_CAMERA = 0;
    public static final int MENU_CAMERA_LIVE = 1;
    public static final int MENU_MOBILE_LIVE = 2;
    private Context mContext;
    private View v;
    private OnMenuClickListener mMenuClickListener;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_add_camera:
                    if (mMenuClickListener != null) {
                        mMenuClickListener.onMenuClick(MENU_ADD_CAMERA);
                    }
                    dismiss();
                    break;
                case R.id.tv_go_to_public:
                    mMenuClickListener.onMenuClick(MENU_CAMERA_LIVE);
                    dismiss();
                    break;
                case R.id.tv_mobile_live:
                    mMenuClickListener.onMenuClick(MENU_MOBILE_LIVE);
                    dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    public PopupMenu(Context context) {
        mContext = context;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(Utils.dip2px(mContext, 120));
        setHeight(Utils.dip2px(mContext, 100));
        this.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.transparent)));
        // 设置弹窗的布局界面
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_menu, null);
        setContentView(v);
        initView();
    }

    private void initView() {
        TextView tvAddCamera = (TextView) v.findViewById(R.id.tv_add_camera);
        TextView tvGoToPublic = (TextView) v.findViewById(R.id.tv_go_to_public);
        TextView tvMobileLive = (TextView) v.findViewById(R.id.tv_mobile_live);
        tvAddCamera.setOnClickListener(mOnClickListener);
        tvGoToPublic.setOnClickListener(mOnClickListener);
        tvMobileLive.setOnClickListener(mOnClickListener);
    }


    public void setOnMenuClickListener(OnMenuClickListener l) {
        mMenuClickListener = l;
    }

    public interface OnMenuClickListener {
        void onMenuClick(int type);
    }
}

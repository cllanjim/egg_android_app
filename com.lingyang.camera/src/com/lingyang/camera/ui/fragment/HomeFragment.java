package com.lingyang.camera.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.EdgeEffectCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.ui.activity.PopupHomeGroup;
import com.lingyang.camera.ui.activity.PopupMenu;
import com.lingyang.camera.ui.adapter.HomeAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：AttentionFragment
 * 描述：
 * 此Fragment是看视频模块，包含我的和广场两个fragment
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class HomeFragment extends Fragment {
    private ViewPager mPager;
    private int mCurrentIndex = 0;
    private ImageView mIndicatorIv, mAddTv;
    private CheckBox mGroupBox;
    PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mGroupBox.setChecked(false);
        }
    };
    private float mIndicatorWidth;
    private float mOffset;
    private List<TextView> mTextList = new ArrayList<TextView>();
    private HomeAdapter mHomeAdapter;
    /**
     * 分组查询点击监听
     */
    PopupHomeGroup.OnGroupListener mGroupListener = new PopupHomeGroup.OnGroupListener() {
        @Override
        public void callBack(int type) {
            mHomeAdapter.refreshPublicFragment(1, false, null, true, type);
        }
    };
    private RelativeLayout mHomeHeader;
    private View view;
    private PopupHomeGroup mPopupGroup;
    private PopupMenu mPopupMenu;
    private PopupMenu.OnMenuClickListener mMenuClickListener = new PopupMenu.OnMenuClickListener() {
        @Override
        public void onMenuClick(int type) {
            Intent intent;
            switch (type) {
                case PopupMenu.MENU_ADD_CAMERA:
                    ActivityUtil.startActivity(getActivity(), new Intent(Const.Actions.ACTION_ACTIVITY_FIRST_OF_ADD_DEVICE));
                    break;
                case PopupMenu.MENU_CAMERA_LIVE:
                    ActivityUtil.startActivity(getActivity(), new Intent(Const.Actions.ACTION_ACTIVITY_PUBLIC_CAMERA));
//                    intent= new Intent();
//                    intent.setAction(Const.Actions.ACTION_ACTIVITY_PUBLIC_CAMERA);
//                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_TYPE,PopupMenu.MENU_CAMERA_LIVE);
//                    ActivityUtil.startActivity(getActivity(), intent);
                    break;
                case PopupMenu.MENU_MOBILE_LIVE:
//                    intent = new Intent();
//                    intent.setAction(Const.Actions.ACTION_ACTIVITY_PUBLIC_CAMERA);
//                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_TYPE,PopupMenu.MENU_MOBILE_LIVE);
//                    ActivityUtil.startActivity(getActivity(), intent);
                    break;
                default:
                    break;
            }

        }
    };
    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_main_title_mine:
                    if (mCurrentIndex != 0) {
                        mPager.setCurrentItem(0);
                    } else {
                        backToTop(mCurrentIndex);
                    }
                    break;
                case R.id.tv_main_title_public:
                    if (mCurrentIndex != 1) {
                        mPager.setCurrentItem(1);
                    } else {
                        backToTop(mCurrentIndex);
                    }
                    break;
                case R.id.rl_home_header:
                    backToTop(mCurrentIndex);
                    break;
                case R.id.iv_main_title_add:
                    //  点击添加摄像机
                    showMenuPopup();
                    break;
                case R.id.btn_main_title_group:
                    // 点击分组查询
                    if (mGroupBox.isChecked()) {
                        mPopupGroup.showAsDropDown(mHomeHeader);
                    } else {
                        mPopupGroup.dismiss();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void showMenuPopup() {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getActivity());
            mPopupMenu.setOnMenuClickListener(mMenuClickListener);
        }
        mPopupMenu.showAsDropDown(mAddTv);
    }

    public void gotoPublic() {
//        mPager.setCurrentItem(1);
        ActivityUtil.startActivity(getActivity(),Const.Actions.ACTION_ACTIVITY_PUBLIC_CAMERA);
    }

    /**
     * 顶部indcator滑动动画
     */
    private void startIndicatorAnimation(int position) {
        float curPos;
        float nextPos;
        if (position == 0) {
            curPos = mOffset + mIndicatorWidth;
            nextPos = mOffset;
        } else {
            curPos = mOffset;
            nextPos = mOffset + mIndicatorWidth;
        }
        CLog.v("curPos => " + curPos + ",nextPos =>" + nextPos);
        TranslateAnimation anim = new TranslateAnimation(Animation.ABSOLUTE,
                curPos, Animation.ABSOLUTE, nextPos,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        mCurrentIndex = position;
        anim.setDuration(300);
        anim.setFillAfter(true);
        mIndicatorIv.startAnimation(anim);
    }

    /**
     * 点击回到顶部
     *
     * @param currentPosition
     */
    private void backToTop(int currentPosition) {
        switch (currentPosition) {
            case 0:
                AttentionFragment attentionFragment = (AttentionFragment) mHomeAdapter.getItem(0);
                PullToRefreshListView attentionCameraLv = attentionFragment.mCameraListView;
                if (attentionCameraLv != null) {
                    ListView attentionListView = attentionCameraLv.getRefreshableView();
                    if (attentionListView != null) {
                        if (!(attentionListView).isStackFromBottom()) {
                            attentionListView.smoothScrollToPosition(0);
                        }
                        attentionListView.setStackFromBottom(false);
                    }
                }
                break;
            case 1:
//                PublicCameraFragment publicFragment = (PublicCameraFragment) mHomeAdapter.getItem(1);
//                PullToRefreshListView publicCameraLv = publicFragment.mCameraListView;
//                if (publicCameraLv != null) {
//                    ListView publicListView = publicCameraLv.getRefreshableView();
//                    if (publicListView != null) {
//                        if (!(publicListView).isStackFromBottom()) {
//                            publicListView.smoothScrollToPosition(1);
//                        }
//                        publicListView.setStackFromBottom(false);
//                    }
//                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CLog.v("onActivityResult ");
        if (resultCode == AttentionFragment.RESULT_CODE_SETTING) {
            ((AttentionFragment) getAdapter().getItem(0)).
                    refreshConfig();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_home, null);
        initView();
        mPopupGroup = new PopupHomeGroup(getActivity());
        mPopupGroup.setOnGroupListener(mGroupListener);
        mPopupGroup.setOnDismissListener(mOnDismissListener);
        // 用于动画
        mIndicatorWidth = getActivity().getResources().getDimension(R.dimen.tab_indicator_width);
        int TAB_COUNT = 2;
        mOffset = (Utils.getScreenWidth(getActivity()) / (TAB_COUNT) - mIndicatorWidth);
        initIndicatorPosition();
        return view;
    }

    public void initView() {
        mHomeHeader = (RelativeLayout) view.findViewById(R.id.rl_home_header);
        mPager = (ViewPager) view.findViewById(R.id.vp_home_list);
        mIndicatorIv = (ImageView) view.findViewById(R.id.iv_indicator);
        mAddTv = (ImageView) view.findViewById(R.id.iv_main_title_add);
        mGroupBox = (CheckBox) view.findViewById(R.id.btn_main_title_group);
        TextView mainTitleMineTv = (TextView) view.findViewById(R.id.tv_main_title_mine);
        TextView mainTitlePublicTv = (TextView) view.findViewById(R.id.tv_main_title_public);

        mainTitleMineTv.setOnClickListener(mOnClickListener);
        mainTitlePublicTv.setOnClickListener(mOnClickListener);
        mHomeHeader.setOnClickListener(mOnClickListener);
        mAddTv.setOnClickListener(mOnClickListener);
        mGroupBox.setOnClickListener(mOnClickListener);

        mHomeAdapter = new HomeAdapter(getChildFragmentManager(), getActivity());
        mPager.setAdapter(mHomeAdapter);
        OnPageListener onPagerListener = new OnPageListener();
        mPager.setOnPageChangeListener(onPagerListener);
        mTextList.clear();
        mTextList.add(mainTitleMineTv);
//        mTextList.add(mainTitlePublicTv);
    }

    private void initIndicatorPosition() {
        float curPos = mOffset;
        float nextPos = mOffset;
        TranslateAnimation anim = new TranslateAnimation(Animation.ABSOLUTE,
                curPos, Animation.ABSOLUTE, nextPos,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        anim.setFillAfter(true);
        mIndicatorIv.startAnimation(anim);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public HomeAdapter getAdapter() {
        if (mHomeAdapter == null) {
            mHomeAdapter = new HomeAdapter(getChildFragmentManager(), getActivity());
        }
        return mHomeAdapter;
    }

    /**
     * 去掉边界阴影效果
     */
    class OnPageListener implements OnPageChangeListener {
        private EdgeEffectCompat leftEdge;
        private EdgeEffectCompat rightEdge;

        public OnPageListener() {
            try {
                Field leftEdgeField = mPager.getClass().getDeclaredField(
                        "mLeftEdge");
                Field rightEdgeField = mPager.getClass().getDeclaredField(
                        "mRightEdge");
                CLog.v("=======leftEdgeField:" + leftEdgeField
                        + ",rightEdgeField:" + rightEdgeField);

                if (leftEdgeField != null && rightEdgeField != null) {
                    leftEdgeField.setAccessible(true);
                    rightEdgeField.setAccessible(true);
                    leftEdge = (EdgeEffectCompat) leftEdgeField.get(mPager);
                    rightEdge = (EdgeEffectCompat) rightEdgeField.get(mPager);
                    CLog.v("=======OK啦，leftEdge:" + leftEdge + ",rightEdge:"
                            + rightEdge);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (leftEdge != null && rightEdge != null) {
                leftEdge.finish();
                rightEdge.finish();
                leftEdge.setSize(0, 0);
                rightEdge.setSize(0, 0);
            }
        }

        @Override
        public void onPageSelected(int position) {
            startIndicatorAnimation(position);
//            mTextList.get(position).setTextColor(
//                    getResources().getColor(R.color.orange));
//            mTextList.get(position == 0 ? 1 : 0).setTextColor(
//                    getResources().getColor(R.color.text_dark));
            mAddTv.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            mGroupBox.setVisibility(position == 1 ? View.VISIBLE : View.GONE);

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }
}

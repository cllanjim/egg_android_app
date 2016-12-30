package com.lingyang.camera.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.entity.UserInfo;
import com.lingyang.camera.entity.UserInfo.userInfo;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.GetUserInfoMgmt;
import com.lingyang.camera.ui.activity.MainActivity;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;

public class MineFragment extends Fragment implements OnClickListener {
    private View v;
    private RoundImageView mHeadCircleImageView;
    private TextView mNameText, mPhoneText;
    BaseCallBack<UserInfo> mCallback = new BaseCallBack<UserInfo>() {

        @Override
        public void error(ResponseError object) {
        }

        @Override
        public void success(UserInfo t) {
            final userInfo data = t.getData();
            Activity activity = getActivity();
            if (activity!=null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LocalUserWrapper.getInstance().getLocalUser()
                                .getNickName().equals(data.nickname)
                                && LocalUserWrapper.getInstance().getLocalUser()
                                .getHead().equals(data.faceimage)) {
                            updateUi();
                        } else {
                            mNameText.setText(data.nickname);
                            Utils.displayUserIconImageView(mHeadCircleImageView,
                                    data.faceimage);
                            mPhoneText.setText(String.format("%s%s", "手机号码：",
                                    LocalUserWrapper.getInstance().getLocalUser()
                                            .getMobile()));
                        }
                        LocalUserWrapper.getInstance().getLocalUser()
                                .setNickName(data.nickname);
                        LocalUserWrapper.getInstance().getLocalUser()
                                .setHead(data.faceimage);
                    }
                });
            }

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_info:
                Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_USER_INFO);
                startActivityForResult(intent, Const.SET_RESULT_REQUEST_CODE);
                break;
            case R.id.layout_attention:
                ActivityUtil.startActivity(getActivity(),
                        Const.Actions.ACTION_ACTIVITY_MY_ATTENTION_LIST);
                break;
            case R.id.layout_file:
                ActivityUtil.startActivity(getActivity(),
                        Const.Actions.ACTION_ACTIVITY_MY_FILE_LIST);
                break;
            case R.id.layout_about:
                ActivityUtil.startActivity(getActivity(),
                        Const.Actions.ACTION_ACTIVITY_ABOUT);
                break;
            case R.id.layout_visual_telephone:
                ActivityUtil.startActivity(getActivity(),
                        Const.Actions.ACTION_ACTIVITY_CONTACTS);
                break;
            case R.id.btn_exit:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.system_prompt)
                        .setMessage(R.string.confirm_logout)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        exit();
                    }
                }).create().show();
                break;
            case R.id.head:
//                startActivity(new Intent(Const.Actions.ACTION_ACTIVITY_MYTEST));
                break;

            default:
                break;
        }
    }

    private void exit() {
        ((MainActivity) getActivity()).goToLogin(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.SET_RESULT_REQUEST_CODE) {
            updateUi();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = inflater.inflate(R.layout.fragment_mine, null);
        initView();
        updateUserInfo();
        return v;
    }

    private void initView() {
        TextView titleText = (TextView) v.findViewById(R.id.tv_header_title);
        mNameText = (TextView) v.findViewById(R.id.tv_name);
        mPhoneText = (TextView) v.findViewById(R.id.tv_phone);
        RelativeLayout infoLayout = (RelativeLayout) v.findViewById(R.id.layout_info);

        RelativeLayout attentionLayout = (RelativeLayout) v
                .findViewById(R.id.layout_attention);
        RelativeLayout fileLayout = (RelativeLayout) v.findViewById(R.id.layout_file);
        RelativeLayout about = (RelativeLayout) v.findViewById(R.id.layout_about);
        RelativeLayout visualTelephoneLayout = (RelativeLayout) v
                .findViewById(R.id.layout_visual_telephone);
        mHeadCircleImageView = (RoundImageView) v.findViewById(R.id.head);
        Button exitBtn = (Button) v.findViewById(R.id.btn_exit);

        titleText.setText(getText(R.string.mine));

        infoLayout.setOnClickListener(this);
        attentionLayout.setOnClickListener(this);
        fileLayout.setOnClickListener(this);
        about.setOnClickListener(this);
        visualTelephoneLayout.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        mHeadCircleImageView.setOnClickListener(this);
    }

    private void updateUserInfo() {
        GetUserInfoMgmt updateUserInfoMgmt = (GetUserInfoMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(GetUserInfoMgmt.class);
        updateUserInfoMgmt.updateUserInfo(getActivity().getApplicationContext(), mCallback);
    }

    private void updateUi() {
        mNameText.setText(LocalUserWrapper.getInstance().getLocalUser()
                .getNickName());
        mPhoneText.setText(String.format("%s%s", "手机号码：",
                LocalUserWrapper.getInstance().getLocalUser().getMobile()));
        Utils.displayUserIconImageView(mHeadCircleImageView, LocalUserWrapper
                .getInstance().getLocalUser().getHead());
    }

}
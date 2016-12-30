package com.lingyang.camera.entity;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.R;
import com.lingyang.camera.ui.activity.AppBaseActivity;
import com.lingyang.camera.util.Utils;

import java.io.Serializable;

public class BaseResponse implements Serializable {

    private static final long serialVersionUID = -4006081040958323864L;
    public int status_code;
    public ResponseError error;
    final int ERROR_CODE_TOKEN_INVALID = 20010;

    public int getStatusCode() {
        return status_code;
    }

    public ResponseError getError() {
        if (error != null) {
            CLog.v(this.getClass().getName() + "-error-error_code?" + error.error_code
                    + "-error_msg" + error.error_msg);
            if (error.error_code.equals(ERROR_CODE_TOKEN_INVALID + "")) {
                new AlertDialog.Builder(((CameraApplication) Utils.getContext()).currentActivity())
                        .setTitle(R.string.system_prompt)
                        .setMessage(R.string.login_expire_login_again)
                        .setNegativeButton(R.string.login_again,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        ((AppBaseActivity) ((CameraApplication)
                                                Utils.getContext()).currentActivity()).goToLogin(true);
                                    }
                                })
                        .setPositiveButton(R.string.app_exit,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        ((CameraApplication) Utils.getContext()).AppExit();
                                    }
                                }).create().show();
                return null;
            }

        }
        return error;
    }

}

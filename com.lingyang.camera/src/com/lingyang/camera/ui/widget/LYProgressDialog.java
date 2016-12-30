package com.lingyang.camera.ui.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

/**
 * 文件名: LYProgressDialog
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/6/14
 */
public class LYProgressDialog extends ProgressDialog {

    private CircularFillableLoaders mLyProgressBar;
    private TextView mTvText;

    public LYProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_download_progressdialog);
//        if (mLayoutInflater != null) {
//            View view = mLayoutInflater.inflate(R.layout.ly_progressdialog, null);
//            setContentView(view);
//        }
        mLyProgressBar = (CircularFillableLoaders) findViewById(R.id.ly_pb_bar);
        mTvText = (TextView) findViewById(R.id.tv_text);
        CLog.d("lyProgressBar--- " + mLyProgressBar);
        CLog.d("tv_text--- " + mTvText);
    }
    public void setDownloadProgress(int progress){
        if (mLyProgressBar!=null) {
            CLog.d("progress---" + progress);
            mLyProgressBar.setProgress(progress);
        }
    }


    public void setText(String s) {
        if (mTvText!=null) {
            CLog.d("setText---" + s);
            mTvText.setText(s);
        }
    }

    public void setColor(int color) {
        if (mLyProgressBar!=null) {
            CLog.d("setColor---" + color);
            mLyProgressBar.setColor(color);
        }
    }
}

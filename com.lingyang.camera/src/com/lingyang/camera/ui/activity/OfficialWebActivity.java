package com.lingyang.camera.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;

/**
 * 文件名：OfficialWebActivity
 * 描述：官网
 * 创建人：杜舒
 * 时间：2016/3
 */
public class OfficialWebActivity extends AppBaseActivity {

    TextView mTitleTv;
    ImageView mBackIv;
    WebView mWebView;
    OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_web);
        initView();
    }

    private void initView() {
        mTitleTv = (TextView) findViewById(R.id.tv_header_title);
        mBackIv = (ImageView) findViewById(R.id.iv_heder_back);
        PullToRefreshWebView webViewOfficialWeb = (PullToRefreshWebView) findViewById(R.id.wv_official_web);
        mWebView = webViewOfficialWeb.getRefreshableView();
        mTitleTv.setText(getText(R.string.official_website));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new SampleWebViewClient());
        mWebView.loadUrl(Const.TOPVDN_OFFICIAL_WEB);
        mWebView.setDownloadListener(mDownloadListener);
        mBackIv.setOnClickListener(mListener);
        mBackIv.setVisibility(View.VISIBLE);
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//            mWebView.goBack();
//        }
//        return true;
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class SampleWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}

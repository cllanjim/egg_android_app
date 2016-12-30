package com.lingyang.camera.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.camera.R;
import com.lingyang.camera.ui.adapter.UpdateHistoryAdapter;

import java.util.ArrayList;
import java.util.Collections;


public class UpdateHistoryActivity extends AppBaseActivity {

    private PullToRefreshListView mLvUpdateHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_history);
        initView();
        setData();
    }
    private void setData() {
        ArrayList<String> list = new ArrayList<>();
        String[] stringArray = getResources().getStringArray(R.array.UpdateHistory);
        Collections.addAll(list, stringArray);
        UpdateHistoryAdapter updateHistoryAdapter = new UpdateHistoryAdapter(this);
        updateHistoryAdapter.setData(list);
        mLvUpdateHistory.setAdapter(updateHistoryAdapter);
    }
    private void initView() {
        TextView titleTv = (TextView) findViewById(R.id.tv_header_title);
        ImageView backIv = (ImageView) findViewById(R.id.iv_heder_back);
        mLvUpdateHistory = (PullToRefreshListView) findViewById(R.id.lv_update_history);
        titleTv.setText(getText(R.string.update_history));
        backIv.setVisibility(View.VISIBLE);
    }


}

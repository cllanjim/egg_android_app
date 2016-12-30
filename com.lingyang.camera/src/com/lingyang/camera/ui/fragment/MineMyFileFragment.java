package com.lingyang.camera.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalRecordWrapper;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.ui.activity.MyFileListActivity;
import com.lingyang.camera.ui.adapter.MineMyFileAdapter;
import com.lingyang.camera.ui.adapter.MineMyFileAdapter.DeleteListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MineMyFileFragment extends Fragment {

    private Context mContext;
    private PullToRefreshListView mRefreshLv;
    private MineMyFileAdapter mMineMyFileAdapter;
    private int mFileType;
    DeleteListener mDeleteListener = new DeleteListener() {
        @Override
        public void delete(LocalRecord localRecord, List<LocalRecord> list) {
            //删除本地数据库文件
            boolean result = LocalRecordWrapper.getInstance().delMediaFile(
                    localRecord);
            if (result) {
                list.remove(localRecord);
                mMineMyFileAdapter.notifyDataSetChanged();
                ((MyFileListActivity) getActivity()).updateUi();
                ((MyFileListActivity) getActivity()).getAdapter().refresh(mFileType);
            }
            //删除手机内存文件
            File file=new File(localRecord.getFilePath());
            if(file.exists()){
                file.delete();
            }
            if(localRecord.getType()==LocalRecord.TYPE_MEDIA_VIDEO){
                File f=new File(localRecord.getImagePath());
                if(f.exists()){
                    f.delete();
                }
            }
        }
    };
    private List<LocalRecord> recordList = new ArrayList<LocalRecord>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mFileType = getArguments().getInt(Const.IntentKeyConst.KEY_FILE_TYPE, 0);
        CLog.e("MyFile---" + mFileType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.mine_myfile_common_layout, container,
                false);
        mRefreshLv = (PullToRefreshListView) v.findViewById(R.id.refreshlv);
        mRefreshLv.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mMineMyFileAdapter.isLoadMore()) {
                    getData(mMineMyFileAdapter.getSize() - 1);
                }
            }
        });

        mRefreshLv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                getData(0);
            }
        });
        mMineMyFileAdapter = new MineMyFileAdapter(mContext, recordList);
        mMineMyFileAdapter.setDeleteListener(mDeleteListener);
        mRefreshLv.setAdapter(mMineMyFileAdapter);
        getData(0);
        return v;
    }

    public void getData(final int size) {
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                final List<LocalRecord> list = LocalRecordWrapper.getInstance()
                        .getLocalMediaList(mFileType, MineMyFileAdapter.PAGE_SIZE, size);
                CLog.v("mFileType-" + mFileType + " list.size" + list.size());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLv.onRefreshComplete();
                        if (size == 0) {
                            mMineMyFileAdapter.refresh(list);
                        } else {
                            mMineMyFileAdapter.AddList(list);
                        }
                    }
                });

            }
        });
    }

    /**
     * 文件刷新
     */
    public void deleteUpdate() {
        getData(0);
    }

}

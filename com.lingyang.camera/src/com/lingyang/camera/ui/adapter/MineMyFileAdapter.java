package com.lingyang.camera.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.ui.activity.MyFileListActivity;
import com.lingyang.camera.ui.widget.FooterView;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.camera.util.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MineMyFileAdapter extends BaseAdapter {

    public static final int MAXSIZE = 200;
    public static final int PAGE_SIZE = 10;
    public static FooterView mFooterView;
    final List<String> mDisplayedImages = Collections
            .synchronizedList(new LinkedList<String>());
    public DeleteListener mDeleteListener;
    ImageLoadingListener mOnLoadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view,
                                    FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !mDisplayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    mDisplayedImages.add(imageUri);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };
    private Context mContext;
    private List<LocalRecord> mLocalRecordList;
    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final LocalRecord localRecord = (LocalRecord) v.getTag();
            Intent intent;
            switch (v.getId()) {
                case R.id.ib_start:
                    intent = FileUtil.getVideoFileIntent(localRecord.getFilePath());
                    if (intent == null) {
                        ((MyFileListActivity) mContext).showToast((String) mContext.getText(R.string.mine_file_not_exist));
                        break;
                    }
                    mContext.startActivity(intent);
                    break;
                case R.id.iv_capture:
                    CLog.v("localRecord.getFilePath():" + localRecord.getFilePath());
                    if (localRecord.getType() == LocalRecord.TYPE_MEDIA_PHOTO) {
                        intent = FileUtil.getImageFileIntent(localRecord
                                .getFilePath());
                    } else {
                        intent = FileUtil.getVideoFileIntent(localRecord
                                .getFilePath());
                    }
                    if (intent == null) {
                        ((MyFileListActivity) mContext).showToast((String) mContext.getText(R.string.mine_file_not_exist));
                        break;
                    }
                    mContext.startActivity(intent);
                    break;
                case R.id.ib_delete:
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.system_prompt)
                            .setMessage("确定删除?")
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDeleteListener.delete(localRecord, mLocalRecordList);
                            dialog.dismiss();
                        }
                    }).create().show();

                    break;

                default:
                    break;
            }

        }
    };
    private boolean mIsLoadMore = false;

    public MineMyFileAdapter(Context context, List<LocalRecord> recordList) {
        super();
        mContext = context;
        mLocalRecordList = recordList;
    }

    @Override
    public int getCount() {
        return mLocalRecordList == null ? 0 : mLocalRecordList.size();
    }

    @Override
    public LocalRecord getItem(int i) {
        if (i < mLocalRecordList.size() && mLocalRecordList.size() > 0) {
            return mLocalRecordList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        CLog.v("getView -position:" + position);
        LocalRecord localRecord = getItem(position);
        boolean flg = isFirstByDate(localRecord.getTimeStamp());

        ViewHolder holder;
        if (convertView == null || (convertView == mFooterView)) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.localrecord_list_item, null);
            holder = new ViewHolder();
            setViewHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (localRecord.getType() == LocalRecord.TYPE_MEDIA_VIDEO) {
            holder.start.setVisibility(View.VISIBLE);
        } else {
            CLog.v("position:" + position + " holder" + holder
                    + " holder.start" + holder.start);
            holder.start.setVisibility(View.GONE);
        }
        if (flg) {
            holder.header.setVisibility(View.VISIBLE);
            holder.date.setText(DateTimeUtil.timeStampToDateByFormat(
                    localRecord.getTimeStamp() / 1000, "yyyy/MM/dd"));
        } else {
            holder.header.setVisibility(View.GONE);
        }
        holder.capture.setTag(localRecord);
        holder.capture.setOnClickListener(mOnClickListener);
        holder.delete.setTag(localRecord);
        holder.delete.setOnClickListener(mOnClickListener);
        holder.start.setTag(localRecord);
        holder.start.setOnClickListener(mOnClickListener);
        holder.duration.setText(DateTimeUtil.timeStampToDateByFormat(
                localRecord.getTimeStamp() / 1000, "HH:mm:ss"));
        CLog.v("localRecord.getImagePath()" + localRecord.getImagePath());
        Utils.displayLocalCaptureView(holder.capture,
                ImageDownloader.Scheme.FILE.wrap(localRecord.getImagePath()));
        CLog.v(convertView.toString() + " -position:" + position);
        return convertView;
    }

    private boolean isFirstByDate(Long timeStamp) {
        for (LocalRecord localRecord : mLocalRecordList) {
            if (localRecord == null) {
                continue;
            }
            String result0 = DateTimeUtil.timeStampToDateByFormat(
                    localRecord.getTimeStamp() / 1000, "MM-dd");
            String result1 = DateTimeUtil.timeStampToDateByFormat(
                    timeStamp / 1000, "MM-dd");
            CLog.v("result0:" + result0 + " -result1:" + result1
                    + " -timeStamp:" + timeStamp + " -getTimeStamp: "
                    + localRecord.getTimeStamp());
            if (result0.equals(result1)
                    && timeStamp != localRecord.getTimeStamp()) {
                return false;
            } else if (result0.equals(result1)
                    && timeStamp == localRecord.getTimeStamp()) {
                return true;
            }
        }
        return false;
    }

    private void setViewHolder(ViewHolder holder, View convertView) {
        holder.capture = (ImageView) convertView.findViewById(R.id.iv_capture);
        holder.date = (TextView) convertView.findViewById(R.id.tv_date);
        holder.delete = (ImageButton) convertView.findViewById(R.id.ib_delete);
        holder.duration = (TextView) convertView.findViewById(R.id.tv_duration);
        holder.start = (ImageButton) convertView.findViewById(R.id.ib_start);
        holder.header = (RelativeLayout) convertView
                .findViewById(R.id.layout_header);
    }

    public void refresh(List<LocalRecord> list) {
        if (mLocalRecordList != null) {
            mLocalRecordList.clear();
        }
        mIsLoadMore = false;
        AddList(list);
        CLog.v("refresh ----" + mLocalRecordList.size());
    }

    public void AddList(List<LocalRecord> l) {
        CLog.v("AddList ----" + l.size());
        if (mLocalRecordList.size() > 0) {
            mLocalRecordList.remove(mLocalRecordList.size() - 1);
        }
        // 分页加载
        if (l.size() < PAGE_SIZE
                || mLocalRecordList.size() + l.size() == MAXSIZE) {
            // 如果加载出来的数目小于指定条数，可视为已全部加载完成
            mIsLoadMore = false;
            mLocalRecordList.addAll(l);
            notifyDataSetChanged();
        } else {
            // 还有数据可加载。
            mIsLoadMore = true;
            mLocalRecordList.addAll(l);
            notifyDataSetChanged();
        }
    }

    public boolean isLoadMore() {
        return mIsLoadMore;
    }

    public int getSize() {
        return mLocalRecordList.size();
    }

    public void setDeleteListener(DeleteListener listener) {
        this.mDeleteListener = listener;
    }

    public interface DeleteListener {
        void delete(LocalRecord localRecord, List<LocalRecord> list);
    }

    static class ViewHolder {
        ImageView capture;
        ImageButton start, delete;
        RelativeLayout header;
        TextView date, duration;

    }
}

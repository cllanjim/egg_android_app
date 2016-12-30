package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.CoverUrlRecordWrapper;
import com.lingyang.camera.db.bean.CoverUrlRecord;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.ui.adapter.AttentionAdapter.ViewHolder;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PublicCameraAdapter extends BaseAdapter {
    public static final int MAXSIZE = 300;
    List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
    ArrayMap<String, String> mCoverHashMap = new ArrayMap<String, String>();
    boolean mIsLoadFinished = false;
    //图片第一次加载的监听器
    private ImageLoadingListener mAnimateFirstListener = new AnimateFirstDisplayListener();
    private DisplayImageOptions mLocalCoverOptions = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.camera)
            .cacheInMemory(true)
            .showImageOnLoading(R.drawable.camera)
            .showImageOnFail(R.drawable.camera)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .displayer(new RoundedBitmapDisplayer(20,
                    RoundedBitmapDisplayer.CORNER_TOP_LEFT | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
            .bitmapConfig(Bitmap.Config.RGB_565).build();

    private DisplayImageOptions mLoadRemoteCoverOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(true)
            .considerExifParams(true)
            .showImageForEmptyUri(R.drawable.camera)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .displayer(new RoundedBitmapDisplayer(20,
                    RoundedBitmapDisplayer.CORNER_TOP_LEFT | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
            .bitmapConfig(Bitmap.Config.RGB_565).build();

    private DisplayImageOptions mLoadRemoteCoverWithDefaultOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(true)
            .showImageOnLoading(R.drawable.camera)
            .showImageOnFail(R.drawable.camera)
            .considerExifParams(true)
            .showImageForEmptyUri(R.drawable.camera)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .displayer(new RoundedBitmapDisplayer(20,
                    RoundedBitmapDisplayer.CORNER_TOP_LEFT | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
            .bitmapConfig(Bitmap.Config.RGB_565).build();

    private ImageLoadingListener mImageLoadingFromCacheListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            CLog.v("onLoadingStarted: " + imageUri + " time:" + SystemClock.elapsedRealtime());
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Camera camera = (Camera) view.getTag();
            CLog.v("onLoadingFailed: " + imageUri + " cid:" + camera.cid);
            ImageLoader.getInstance().displayImage(camera.cover_url,
                    (ImageView) view, mLoadRemoteCoverOptions, mAnimateFirstListener);
        }

        @Override
        public void onLoadingComplete(String imageUri, final View view, final Bitmap loadedImage) {
            Camera camera = (Camera) view.getTag();
            CLog.v("onLoadingComplete: " + imageUri + " cid:" + camera.cid
                    + " camera.cover_url:" + camera.cover_url);
            ImageLoader.getInstance().displayImage(camera.cover_url,
                    (ImageView) view, mLoadRemoteCoverOptions, mAnimateFirstListener);

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            CLog.v("onLoadingCancelled: " + imageUri);
        }
    };
    private List<Camera> mList = new ArrayList<Camera>();
    private Context mContext;
    private CameraClickCallBackListener mCameraOnClickCallBackListener;
    private OnClickListener mOnItemClickListener = new OnClickListener() {


        @Override
        public void onClick(View v) {
            Camera camera = (Camera) v.getTag();
            switch (v.getId()) {
                case R.id.iv_camera_capture:
                    if (mCameraOnClickCallBackListener != null) {
                        mCameraOnClickCallBackListener.viewLive(camera, camera.cover_url);
                    }

                    break;
                case R.id.iv_camera_attention:
                    if (mCameraOnClickCallBackListener != null) {
                        mCameraOnClickCallBackListener.attention(camera);
                    }
                    break;

                default:
                    break;
            }

        }
    };
    private ArrayMap<String, Camera> mMap = new ArrayMap<String, Camera>();

    public PublicCameraAdapter(Context context, List<Camera> list) {
        if (list != null) {
            this.mList = list;
        }
        this.mContext = context;
    }

    private void setViewHolder(ViewHolder holder, View convertView) {
        holder.cameraCaptureImageView = (ImageView) convertView
                .findViewById(R.id.iv_camera_capture);
        holder.settingImageView = (TextView) convertView
                .findViewById(R.id.iv_camera_setting);
        holder.camera_place = (TextView) convertView
                .findViewById(R.id.tv_camera_place);
        holder.cameraNameTextView = (TextView) convertView
                .findViewById(R.id.tv_camera_nickname);
        holder.userNameTextView = (TextView) convertView
                .findViewById(R.id.tv_camera_username);
        holder.isOnlineTextView = (TextView) convertView
                .findViewById(R.id.tv_camera_isonline);
        holder.cameraNameTextView = (TextView) convertView
                .findViewById(R.id.tv_camera_nickname);
        holder.shareToView = (TextView) convertView
                .findViewById(R.id.iv_camera_to_share);
        holder.userIconImageView = (RoundImageView) convertView
                .findViewById(R.id.iv_camera_user_icon);
        holder.cameraHistoryImageView = (TextView) convertView
                .findViewById(R.id.iv_camera_history);
        holder.attentionToImageView = (TextView) convertView
                .findViewById(R.id.iv_camera_to_attention);

        holder.mLineIv1 = (ImageView) convertView.findViewById(R.id.iv_line1);
        holder.mLineIv2 = (ImageView) convertView.findViewById(R.id.iv_line2);
    }

    public void localUpdate(Camera c) {
        if (c.type == Camera.TYPE_PUBLIC) {
            if (mMap.get(c.cid) != null) {
                mMap.get(c.cid).cname = c.cname;
                notifyDataSetChanged();
            } else {
                localAdd(c);
            }
        } else {
            localRemove(c.cid);
        }
    }

    public void localAdd(Camera c) {
        if (mMap.get(c.cid) == null) {
            c.type = Camera.TYPE_PUBLIC;
            mList.add(0, c);
            mMap.put(c.cid, c);
            notifyDataSetChanged();
        }
    }

    public void localRemove(String cid) {
        Camera c = mMap.get(cid);
        if (c != null) {
            mList.remove(c);
            mMap.remove(cid);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void localCancelAttention(String cid, boolean isAtt) {
        Camera c = mMap.get(cid);
        if (c != null) {
            if (isAtt) {
                c.followed++;
            } else {
                c.followed--;
            }
            c.is_followed = isAtt;
            notifyDataSetChanged();
        }
    }

    public boolean isLoadFinished() {
        return mIsLoadFinished;
    }

    public void Refresh(List<Camera> list) {
        if (list == null)
            return;
        mList.clear();
        mIsLoadFinished = false;
        AddList(list);
        mMap.clear();
        for (int i = 0; i < mList.size(); i++) {
            mMap.put(mList.get(i).cid, mList.get(i));
        }
    }

    public void AddList(final List<Camera> list) {
        if ((list != null && list.size() < Const.PAGE_SIZE)
                || mList.size() >= MAXSIZE) {
            mIsLoadFinished = true;
        }
        // 还有数据可加载。
        if (list == null)
            return;
        boolean isHava = false;
        for (Camera camera : list) {
            for (Camera preCamera : mList) {
                if (camera.cid.equals(preCamera.cid)) {
                    isHava = true;
                    break;
                }
            }
            if (!isHava) {
                mList.add(camera);
            }
            isHava = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Camera getItem(int position) {
        if (mList != null && mList.size() > position)
            return mList.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        final Camera camera = getItem(position);
        if (convertView == null) {
            CLog.v("getView convertView==null pos:" + position);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.camera_list_item, null);
//            convertView = ((Activity) mContext).getLayoutInflater().inflate(
//                    R.layout.camera_list_item, null);
            holder = new ViewHolder();
            setViewHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            CLog.v("getView convertView!=null pos:" + position);
            holder = (ViewHolder) convertView.getTag();
        }
        holder.cameraHistoryImageView.setVisibility(View.GONE);
        holder.attentionToImageView.setVisibility(View.VISIBLE);
        holder.cameraHistoryImageView.setVisibility(View.GONE);
        holder.settingImageView.setVisibility(View.GONE);
        holder.shareToView.setVisibility(View.GONE);
        holder.mLineIv1.setVisibility(View.GONE);
        holder.mLineIv2.setVisibility(View.GONE);

        holder.camera_place.setSelected(true);
        holder.camera_place.setText(camera.address);
        holder.cameraCaptureImageView.setTag(camera);
        holder.cameraNameTextView.setText(camera.cname);
        holder.attentionToImageView.setText(String.format("看过%s次", camera.total_watched_nums));
        holder.userNameTextView.setText(String.format("@ %s", camera.nickname));
        holder.cameraCaptureImageView.setOnClickListener(mOnItemClickListener);
        setDrawableImg(holder.isOnlineTextView, camera.camera_type == Camera.CAMERA_TYPE_CAMERA
                ? R.drawable.online_camera : R.drawable.online_iphone);
        holder.isOnlineTextView.setText(camera.getIsOnline() ? "直播中" : "离线");
        holder.isOnlineTextView.setBackgroundResource(camera
                .getIsOnline() ? R.drawable.shape_online_bg
                : R.drawable.shape_outline_bg);

        Utils.displayUserIconImageView(holder.userIconImageView, camera.faceimage);
       /* ImageLoader.getInstance().displayImage(camera.cover_url,
                holder.cameraCaptureImageView,
                mLoadRemoteCoverOptions, mAnimateFirstListener);*/
        //内存中缓存了图片地址
        if (camera.camera_type == Camera.CAMERA_TYPE_MOBILE) {
            Utils.displayMobileLiveCaptureView(holder.cameraCaptureImageView,
                    camera.cover_url);
        } else {
            if (mCoverHashMap.containsKey(camera.cid)) {
                String cacheUrl = mCoverHashMap.get(camera.cid);
                if (!cacheUrl.equals(camera.cover_url) ||
                        !camera.cover_url.equals(ImageLoader.getInstance()
                                .getLoadingUriForView(holder.cameraCaptureImageView))) {
                    CLog.v("mCoverMap.containsKey !equals:  cid-" + camera.cid + " url-" + camera.cover_url);
                    ImageLoader.getInstance().displayImage(cacheUrl, holder.cameraCaptureImageView,
                            mLocalCoverOptions, mImageLoadingFromCacheListener);
                } else {
                    CLog.v("mCoverMap.containsKey equals:  cid-" + camera.cid + " url-" + camera.cover_url);
                    ImageLoader.getInstance().displayImage(camera.cover_url,
                            holder.cameraCaptureImageView,
                            mLoadRemoteCoverOptions
                            , mAnimateFirstListener);
                }
            } else {
                CoverUrlRecord coverUrlRecord = CoverUrlRecordWrapper.getInstance().getCoverUrlRecord(camera.cid);
                if (coverUrlRecord == null) {
                    ImageLoader.getInstance().displayImage(camera.cover_url,
                            holder.cameraCaptureImageView,
                            mLoadRemoteCoverWithDefaultOptions
                            , mAnimateFirstListener);
                    CLog.v("coverUrlRecord is null cid- " + camera.cid + " cover_url- " + camera.cover_url);
                } else {
                    ImageLoader.getInstance().displayImage(coverUrlRecord.getCoverUrl(),
                            holder.cameraCaptureImageView,
                            mLocalCoverOptions, mImageLoadingFromCacheListener);
                    CLog.v("coverUrlRecord not null cid- " + camera.cid + " coverUrlRecordURl- "
                            + coverUrlRecord.getCoverUrl());

                }
            }
        }

        return convertView;
    }
    public void stopLoad(){
        ImageLoader.getInstance().stop();
    }

    public void setDrawableImg(TextView tv, int src) {
        Drawable drawable = mContext.getResources().getDrawable(src);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(drawable, null, null, null);
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void setCameraOnClickCallBackListener(
            CameraClickCallBackListener cameraOnClickCallBackListener) {
        mCameraOnClickCallBackListener = cameraOnClickCallBackListener;
    }

    public interface CameraClickCallBackListener {

        void viewLive(Camera camera, String cover);

        void attention(Camera camera);

        void unAttention(Camera camera);
    }

    /**
     * 图片加载第一次显示监听器
     *
     * @author Administrator
     */
    private class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                // 是否第一次显示
                boolean firstDisplay = !displayedImages.contains(imageUri);
                CLog.d("firstDisplay---"+imageUri);
                CLog.d("firstDisplay---"+displayedImages.toString());
                CLog.d("firstDisplay---"+firstDisplay);
                if (firstDisplay) {
                    // 图片淡入效果
                    FadeInBitmapDisplayer.animate(imageView, 1500);
                    displayedImages.add(imageUri);
                }
                final Camera camera = (Camera) view.getTag();
                ThreadPoolManagerNormal.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean save;
                        CoverUrlRecord coverUrlRecord = CoverUrlRecordWrapper.getInstance().getCoverUrlRecord(camera.cid);
                        if (coverUrlRecord == null) {
                            coverUrlRecord = new CoverUrlRecord(
                                    camera.cid, imageUri, SystemClock.elapsedRealtime() + "");
                            save = CoverUrlRecordWrapper.getInstance().saveCoverUrl(coverUrlRecord);
                            CLog.v("saveCoverUrl: " + save + " cid-" + camera.cid + " url-" + imageUri);
                        } else {
                            coverUrlRecord.setCoverUrl(camera.cover_url);
                            save = CoverUrlRecordWrapper.getInstance().updateCoverUrl(coverUrlRecord);
                            CLog.v("updateCoverUrl: " + save + " cid-" + camera.cid + " url-" + imageUri);
                        }
                        if (save) {
                            mCoverHashMap.put(camera.cid, imageUri);
                        }
                    }
                });
            }
        }
    }
}
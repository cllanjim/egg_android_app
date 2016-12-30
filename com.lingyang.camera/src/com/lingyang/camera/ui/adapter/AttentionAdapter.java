package com.lingyang.camera.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.CoverUrlRecordWrapper;
import com.lingyang.camera.db.bean.CoverUrlRecord;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera.CameraOwner;
import com.lingyang.camera.entity.CameraState;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.fragment.HomeFragment;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AttentionAdapter extends BaseAdapter {


    public final String STATE_LIVING = "直播中";
    public final String STATE_OFFLINE = "离线";
    public final String STATE_ONLINE = "在线";
    public final String STATE_UNPREPARED = "未就绪";
    public final String STATE_CONFIGING = "配置中";
    ArrayMap<String, Info> mStatusMap = new ArrayMap<String, Info>();
    List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
    ArrayMap<String, String> mCoverMap = new ArrayMap<String, String>();
    private Context mContext;
    //图片第一次加载的监听器
    private ImageLoadingListener mAnimateFirstListener = new AnimateFirstDisplayListener();
    private DisplayImageOptions mLocalCoverOptions = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.camera)
            .cacheInMemory(true).cacheOnDisk(true)
            .showImageOnLoading(R.drawable.camera)
            .showImageOnFail(R.drawable.camera)
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
    private List<Camera> mCamerasList = new ArrayList<Camera>();
    private CameraClickCallBackListener mCameraOnClickCallBackListener;
    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final Camera camera = (Camera) v.getTag();
            switch (v.getId()) {
                case R.id.iv_camera_capture:
                    if (mCameraOnClickCallBackListener != null) {
                        String url = mStatusMap.get(camera.cid).cameraUrl;
                        Info info = mStatusMap.get(camera.cid);
                        if (info != null) {
                            CLog.d("status  " + info.status);
                            camera.state = info.status;
                            camera.play_addr = TextUtils.isEmpty(info.play_addr) ?
                                    camera.play_addr : info.play_addr;
                        }
                        CLog.d("play_addr---" + camera.play_addr);
                        mCameraOnClickCallBackListener.viewLive(camera, camera.cover_url);
                    }
                    break;
                case R.id.iv_camera_setting:
                    if (mCameraOnClickCallBackListener != null
                            && getPosition(camera) != -1)
                        mCameraOnClickCallBackListener.setting(camera,
                                getPosition(camera));
                    break;
                case R.id.iv_camera_history:
                    if (mCameraOnClickCallBackListener != null) {
                        String url = mStatusMap.get(camera.cid).cameraUrl;
                        mCameraOnClickCallBackListener.viewRecord(camera, url);
                    }
                    break;
                case R.id.tv_record:
                    if (mCameraOnClickCallBackListener != null) {
                        String url = mStatusMap.get(camera.cid).cameraUrl;
                        mCameraOnClickCallBackListener.viewRecord(camera, url);
                    }
                    break;
                case R.id.iv_camera_to_share:
                    if (mCameraOnClickCallBackListener != null)
                        mCameraOnClickCallBackListener.share(camera.cid,
                                camera.cname, camera.shared);
                    break;
                case R.id.tv_cancel_share:
                    mCameraOnClickCallBackListener.unAttention(camera, UnAttentionPublicCameraMgmt.MGMT_UNSHARED);
                    break;
                case R.id.tb_camera_unattention:
                    CLog.v("camera " + camera);
                    mCameraOnClickCallBackListener.unAttention(camera, UnAttentionPublicCameraMgmt.MGMT_UNATTENTION);
                    break;
                default:
                    break;
            }

        }
    };
    private HomeFragment mHomeFragment;
    private Map<String, Camera> mMap = new HashMap<String, Camera>();

    public AttentionAdapter(Context context, HomeFragment homeFragment) {
        super();
        mContext = context;
        mHomeFragment = homeFragment;
    }

    public void localRemove(String cid) {
        Camera c = mMap.get(cid);
        mMap.remove(cid);
        mCamerasList.remove(c);
        notifyDataSetChanged();
    }

    public void deleteShareCamera(String cid) {
        Camera deleteCamera = null;
        for (Camera camera : mCamerasList) {
            if (camera.cid.equals(cid)
                    &&camera.getCameraOwner()==CameraOwner.CAMERA_SHARA_TO_ME) {
                deleteCamera = camera;
            }
        }
        if (deleteCamera!=null) {
            mMap.remove(cid);
            mCamerasList.remove(deleteCamera);
            notifyDataSetChanged();
        }
    }

    public void localUpdate(String cid, String cName, int type) {
        Camera c = mMap.get(cid);
        if (cName != null) {
            c.cname = cName;
        }
        c.type = type;
        mCamerasList.get(mCamerasList.indexOf(c)).type = type;
        notifyDataSetChanged();
    }

    public void localAdd(Camera c) {
        c.setCameraOwner(CameraOwner.CAMERA_PUBLIC);
        mCamerasList.add(c);
        mMap.put(c.cid, c);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCamerasList.size() == 0 ? 1 : mCamerasList.size();
    }

    @Override
    public Camera getItem(int i) {
        if (mCamerasList == null || mCamerasList.isEmpty()) {
            return null;
        }
        return mCamerasList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (mCamerasList.size() == 0) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_attention_empty, null);
            Button pubCameraOperate = (Button) convertView
                    .findViewById(R.id.tv_pub_camera_operate);
            pubCameraOperate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHomeFragment.gotoPublic();
                }
            });
        } else {
            if (convertView == null || (convertView.getTag() == null)) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.camera_list_item, null);
                setViewHolder(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.camera_place.setVisibility(View.GONE);
            holder.camera_place.setSelected(true);
            CameraOwner ownerType = getItemType(position);
            final Camera camera = getItem(position);

            switch (ownerType) {
                case CAMERA_MINE:
                    holder.mLineIv1.setVisibility(View.VISIBLE);
                    holder.mLineIv2.setVisibility(View.VISIBLE);
                    holder.settingImageView.setVisibility(View.VISIBLE);
                    holder.cameraHistoryImageView.setVisibility(View.VISIBLE);
                    holder.shareToView.setVisibility(View.VISIBLE);
                    holder.shareLayout.setVisibility(View.GONE);
                    holder.userNameTextView.setText(String.format("@ %s(我的)", camera.nickname));
                    holder.shareToView.setVisibility(View.VISIBLE);
                    holder.attentionToImageView.setVisibility(View.GONE);
                    holder.shareToView.setText(String.format("已分享给%d人", camera.shared));
                    holder.unattentionTextView.setVisibility(View.GONE);
                    break;
                case CAMERA_PUBLIC:
                case CAMERA_SHARA_TO_ME:
                    holder.mLineIv1.setVisibility(View.GONE);
                    holder.mLineIv2.setVisibility(View.GONE);
                    if (ownerType == CameraOwner.CAMERA_PUBLIC) {
                        holder.attentionToImageView.setVisibility(View.VISIBLE);
                        holder.userNameTextView.setText(String.format("@ %s(公开的)", camera.nickname));
                        holder.attentionToImageView.setText(String.format("%d人关注", camera.followed));
                        holder.shareLayout.setVisibility(View.GONE);
                        CLog.v(camera.cid);
                        holder.camera_place.setVisibility(camera.getIsOnline() ? View.VISIBLE : View.GONE);
                        holder.camera_place.setText(camera.address);
                        holder.unattentionTextView.setVisibility(camera.getIsOnline() ? View.GONE : View.VISIBLE);
                    } else if (ownerType == CameraOwner.CAMERA_SHARA_TO_ME) {
                        holder.unattentionTextView.setVisibility(View.GONE);
                        holder.userNameTextView.setText(String.format("@ %s(分享的)", camera.nickname));
                        holder.shareLayout.setVisibility(View.VISIBLE);
                        holder.attentionToImageView.setVisibility(View.GONE);
                        holder.camera_place.setVisibility(View.GONE);
                    }
                    holder.cameraHistoryImageView.setVisibility(View.GONE);
                    holder.settingImageView.setVisibility(View.GONE);
                    holder.shareToView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            holder.cameraNameTextView.setText(camera.cname);
            holder.camera_place.setText(camera.address);
            holder.settingImageView.setTag(camera);
            holder.pos = position;
            holder.shareToView.setTag(camera);
            holder.cameraCaptureImageView.setTag(camera);
            holder.cameraHistoryImageView.setTag(camera);
            holder.recordTextView.setTag(camera);
            holder.cancelShareTextView.setTag(camera);
            holder.unattentionTextView.setTag(camera);
            if (mCameraOnClickCallBackListener != null) {
                holder.settingImageView.setOnClickListener(mOnClickListener);
                holder.shareToView.setOnClickListener(mOnClickListener);
                holder.cameraCaptureImageView
                        .setOnClickListener(mOnClickListener);
                holder.cameraHistoryImageView
                        .setOnClickListener(mOnClickListener);
                holder.recordTextView
                        .setOnClickListener(mOnClickListener);
                holder.cancelShareTextView.setOnClickListener(mOnClickListener);
                holder.unattentionTextView.setOnClickListener(mOnClickListener);
            }
            if (mStatusMap.containsKey(camera.cid)) {
                Utils.displayUserIconImageView(holder.userIconImageView,
                        camera.faceimage);
                /*if (!TextUtils.isEmpty(camera.cover_url)) {
                    CLog.v("displayCaptureView " + camera.cover_url);
                    Utils.displayCaptureView(holder.cameraCaptureImageView,
                            camera.cover_url, mImageLoadingListener);
                } else {
                    CLog.v("displayLocalCache");
                    displayLocalCache(holder.cameraCaptureImageView, camera);
                }*/
                String text;
                if (ownerType == CameraOwner.CAMERA_MINE
                        || ownerType == CameraOwner.CAMERA_SHARA_TO_ME) {
//                    if (camera.type == Camera.TYPE_PUBLIC) {
//                        text = camera.getStatus(mStatusMap.get(camera.cid).status)
//                                .equals(STATE_ONLINE) ? STATE_LIVING : STATE_OFFLINE;
//                    } else {
                    text = camera.getStatus(mStatusMap.get(camera.cid).status);
//                    }
                    camera.setIsOnline(mStatusMap.get(camera.cid).status
                            != Camera.DEVICE_STATUS_OFFLINE);
                    if (mStatusMap.get(camera.cid).isConfiging) {
                        text = STATE_CONFIGING;
                    }
                    holder.isOnlineTextView.setText(text);
                } else {
                    text = camera.getIsOnline() ? STATE_LIVING : STATE_OFFLINE;
                    if (!holder.isOnlineTextView.getText().equals(text)) {
                        holder.isOnlineTextView.setText(text);
                    }
                }
                if (text.equals(STATE_OFFLINE) || text.equals(STATE_UNPREPARED)
                        || text.equals(STATE_CONFIGING)) {
                    holder.isOnlineTextView
                            .setBackgroundResource(R.drawable.shape_outline_bg);
                } else {
                    holder.isOnlineTextView
                            .setBackgroundResource(R.drawable.shape_online_bg);
                }
                CLog.v("containsKey-camera.cid:" + camera.cid);
            } else {
                Info info = new Info();
                info.type = camera.type;
                // 自己的摄像头在线状态
                if (ownerType == CameraOwner.CAMERA_MINE
                        || ownerType == CameraOwner.CAMERA_SHARA_TO_ME) {
                    int status = camera.state;
                    camera.setIsOnline(status != Camera.DEVICE_STATUS_OFFLINE);
                    CLog.v("camera  cid:" + camera.cid + " -status:" + status);
//                    if (camera.type == Camera.TYPE_PUBLIC) {
////                        holder.isOnlineTextView.setText(camera
////                                .getStatus(status).equals(STATE_ONLINE) ? STATE_LIVING : STATE_OFFLINE);
//                    } else {
                    holder.isOnlineTextView.setText(camera.getStatus(status));
//                    }
                    info.status = status;
                } else {
                    holder.isOnlineTextView
                            .setText(camera.getIsOnline() ? STATE_LIVING : STATE_OFFLINE);
                }
                if (holder.isOnlineTextView.getText().equals(STATE_OFFLINE)
                        || holder.isOnlineTextView.getText().equals(STATE_UNPREPARED)
                        || holder.isOnlineTextView.getText().equals(STATE_CONFIGING)) {
                    holder.isOnlineTextView
                            .setBackgroundResource(R.drawable.shape_outline_bg);
                } else {
                    holder.isOnlineTextView
                            .setBackgroundResource(R.drawable.shape_online_bg);
                }
                info.play_addr = camera.play_addr;
                info.faceUrl = camera.faceimage;
                info.cameraUrl = camera.cover_url;
                mStatusMap.put(camera.cid, info);
                CLog.v("cname:" + camera.cname + "position:" + position
                        + " - cover_url:" + camera.cover_url);

                Utils.displayUserIconImageView(holder.userIconImageView,
                        camera.faceimage);
               /* if (!TextUtils.isEmpty(camera.cover_url)) {
                    CLog.v("displayCaptureView " + camera.cover_url);
                    Utils.displayCaptureView(holder.cameraCaptureImageView,
                            String.format("%s#%s", camera.cover_url, System.currentTimeMillis()), mImageLoadingListener);
                } else {
                    CLog.v("displayLocalCache");
                    displayLocalCache(holder.cameraCaptureImageView, camera);
                }*/
                CLog.v("camera.faceimage:" + camera.faceimage + " position："
                        + position);
            }
            //非私有摄像机
            if (!TextUtils.isEmpty(camera.cover_url)) {
                if (mCoverMap.containsKey(camera.cid)) {
                    String cacheUrl = mCoverMap.get(camera.cid);
                    if (!cacheUrl.equals(camera.cover_url)||
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
            } else {
                CLog.v("displayLocalCache");
                displayLocalCache(holder.cameraCaptureImageView, camera);
            }
        }
        return convertView;
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

        holder.shareLayout = (LinearLayout) convertView
                .findViewById(R.id.layout_share);
        holder.cancelShareTextView = (TextView) convertView
                .findViewById(R.id.tv_cancel_share);
        holder.recordTextView = (TextView) convertView
                .findViewById(R.id.tv_record);
        holder.unattentionTextView = (TextView) convertView
                .findViewById(R.id.tb_camera_unattention);


        holder.mLineIv1 = (ImageView) convertView.findViewById(R.id.iv_line1);
        holder.mLineIv2 = (ImageView) convertView.findViewById(R.id.iv_line2);
    }

    public CameraOwner getItemType(int position) {
        if (mCamerasList != null && mCamerasList.size() > position)
            return getItem(position).getCameraOwner();
        return null;
    }

    @NonNull
    private void displayLocalCache(ImageView view, Camera camera) {
        MyPreference myPreference = MyPreference.getInstance();
        myPreference.setPreferenceKey(camera.cid);
        String cameraUrl = Const.FILE_PROTOCOL
                + myPreference.getString(mContext, MyPreference.SNAPSHOT_OF_COVER, "");
        Utils.displayCaptureView(view, cameraUrl);
        camera.cover_url = cameraUrl;
        CLog.v("display Cache " + myPreference.getString(mContext, MyPreference.SNAPSHOT_OF_COVER, ""));
    }

    public int getPosition(Camera camera) {
        for (Camera temp : mCamerasList) {
            if (camera.cid.equals(temp.cid))
                return mCamerasList.indexOf(temp);
        }
        return -1;
    }

    public void refreshCameraStatus() {
//        boolean refreshFlag = false;
//        for (Camera camera : mCamerasList) {
//            CameraOwner ownerType = getItemType(mCamerasList.indexOf(camera));
//            if (mStatusMap.containsKey(camera.cid)
//                    && (ownerType == CameraOwner.CAMERA_MINE
//                    || ownerType == CameraOwner.CAMERA_SHARA_TO_ME)) {
//                int status = CloudOpenAPI.getInstance().getDeviceStatus(camera.cid);
//                CLog.v("cid: " + camera.cid
//                        + " before state: " + mStatusMap.get(camera.cid).status
//                        + "   now state: " + status);
//                if (mStatusMap.get(camera.cid).status != status
//                        || mStatusMap.get(camera.cid).isConfiging != camera.isConfiging
//                        || mStatusMap.get(camera.cid).type != camera.type) {
//                    mStatusMap.get(camera.cid).status = status;
//                    mStatusMap.get(camera.cid).isConfiging = camera.isConfiging;
//                    mStatusMap.get(camera.cid).type = camera.type;
//                    refreshFlag = true;
//                    CLog.v("status update :camera.cid "
//                            + camera.cid + " -status:" + status + " name:" + camera.cname);
//                }
//            }
//        }
//        if (refreshFlag) {
//            notifyDataSetChanged();
//        }

    }

    public void refresh(List<Camera> list) {
        if (list == null)
            return;
        mCamerasList.clear();
        List<Camera> mOfflineCamerasList = new ArrayList<Camera>();
        List<Camera> mOnlineCamerasList = new ArrayList<Camera>();
        for (Camera camera : list) {
            if (camera.getCameraOwner() == CameraOwner.CAMERA_PUBLIC) {
                if (!camera.is_online) {
                    mOfflineCamerasList.add(camera);
                } else {
                    mOnlineCamerasList.add(camera);
                }
            } else {
                mOnlineCamerasList.add(camera);
            }
        }
        mCamerasList.addAll(mOnlineCamerasList);
        mCamerasList.addAll(mOfflineCamerasList);
        for (int i = 0; i < mCamerasList.size(); i++) {
            Camera c = mCamerasList.get(i);
            mMap.put(c.cid, c);
        }
        mStatusMap.clear();
        notifyDataSetChanged();
    }

    public void removeCamera(Camera camera) {
        mCamerasList.remove(camera);
        notifyDataSetChanged();
    }

    public void removeCamera(int positon) {
        if (mCamerasList != null && mCamerasList.get(positon) != null) {
            mCamerasList.remove(positon);
            notifyDataSetChanged();
        }
    }

    public void refresh(int position, Camera camera) {
        if (mCamerasList != null && mCamerasList.get(position) != null) {
            mCamerasList.set(position, camera);
            notifyDataSetChanged();
        }
    }

    public void refresh(String cid, int shared) {
        for (Camera camera : mCamerasList) {
            if (camera.cid.equals(cid)) {
                camera.shared = shared;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void updateCameraConfig(String cid, boolean isConfiging, Boolean isPublic) {
        for (Camera camera : mCamerasList) {
            if (camera.cid.equals(cid)) {
                camera.isConfiging = isConfiging;
                if (isPublic != null) {
                    camera.type = isPublic ? Camera.TYPE_PUBLIC : Camera.TYPE_PRIVATE;
                }
                CLog.d("updateCameraConfig camera.isConfiging" + isConfiging);
                break;
            }
        }
    }

    public boolean getIsConfiging(String cid) {
        if (mStatusMap.containsKey(cid)) {
            return mStatusMap.get(cid).isConfiging;
        } else {
            return false;
        }

    }

    public void setCameraOnClickCallBackListener(
            CameraClickCallBackListener cameraOnClickCallBackListener) {
        mCameraOnClickCallBackListener = cameraOnClickCallBackListener;
    }

    public void refreshCameraState(CameraState.PayLoad payLoad) {
        if (mStatusMap.containsKey(payLoad.cid)) {
            Info info = mStatusMap.get(payLoad.cid);
            info.play_addr = payLoad.play_addr;
            CLog.d("info.play_addr---" + info.play_addr);
            if (info.status != payLoad.state) {
                info.status = payLoad.state;
                notifyDataSetChanged();
                CLog.v("status update :camera.cid "
                        + payLoad.cid + " -status:" + payLoad.state + " name:");
            }
        }

    }

    public interface CameraClickCallBackListener {

        void share(String cid, String cname, int shared);

        void setting(Camera camera, int pos);

        void viewLive(Camera camera, String imageUri);

        void viewRecord(Camera camera, String imageUri);

        void unAttention(Camera camera, String unAttentionType);

    }

    public static class Info {
        public String cameraUrl;
        public String faceUrl;
        public String play_addr;
        public int status;
        public boolean isConfiging;
        public int type;
    }

    public static class ViewHolder {
        ImageView cameraCaptureImageView, mLineIv1, mLineIv2;
        TextView shareToView;
        TextView settingImageView;
        RoundImageView userIconImageView;
        TextView attentionToImageView;
        TextView cameraHistoryImageView;
        TextView cameraNameTextView;
        TextView userNameTextView;
        TextView isOnlineTextView;
        TextView camera_place;
        LinearLayout shareLayout;
        TextView cancelShareTextView;
        TextView recordTextView;
        TextView unattentionTextView;
        int pos;
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
                CLog.d("firstDisplay---"+imageUri);
                CLog.d("firstDisplay---"+displayedImages.toString());
                final Camera camera = (Camera) view.getTag();
                boolean firstDisplay = !mCoverMap.containsKey(camera.cid);
                CLog.d("firstDisplay---"+firstDisplay);
                if (firstDisplay) {
                    // 图片淡入效果
                    FadeInBitmapDisplayer.animate(imageView, 1500);
                    displayedImages.add(imageUri);
                }
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
                            mCoverMap.put(camera.cid, imageUri);
                        }
                    }
                });
            }
        }
    }
}

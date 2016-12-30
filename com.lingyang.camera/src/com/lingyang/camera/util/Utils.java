package com.lingyang.camera.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.Wifi;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * 提供全局的context JiaApplication里来的
     *
     * @return
     */
    private static Context context;

    private static DisplayImageOptions mIconOptions, mCaptureCoverOptions,
            mLocalMediaCaptureCoverOptions, mMobileLiveCaptureCoverOptions, mDisplayCaptureCoverOptions;
    private static DisplayImageOptions.Builder sBuilder;

    public static boolean isGingerbreadOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean isHCOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    public static Context getContext() {
        if (context == null)
            throw new UnsupportedOperationException();
        return context;
    }

    public static void setContext(Context ctx) {
        context = ctx;
    }

    public static String inputStreamToString(InputStream is) {
        String response = "";
        try {
            byte buffer[] = new byte[1024];
            int n = 0;
            while ((n = is.read(buffer)) != -1) {
                response += new String(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String decodeBase64(String text) {
        try {
            return new String(Base64.decode(text.getBytes(), Base64.DEFAULT),
                    "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        return text;
    }

    public static String encodeBase64(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.NO_WRAP
                | Base64.URL_SAFE);
    }

    public static String getFormatDate(long timemillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timemillis);
        Date d = c.getTime();
        // String date = SimpleDateFormat.getDateInstance().format(d);
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

        // String time = SimpleDateFormat.getTimeInstance().format(d);
        return df.format(d);
    }

    public static String getImageFileCachePathFromUrl(String url,
                                                      Context context) {
        /*
         * String fileName = Uri.parse(cameraUrl).getLastPathSegment(); if
		 * (!TextUtils.isEmpty(fileName)) { if (fileName.endsWith(".jpeg") ||
		 * fileName.endsWith(".jpg")) { fileName = fileName.substring(0,
		 * fileName.lastIndexOf(".")); } CamApplication mCamApplication =
		 * (CamApplication) context .getApplicationContext(); return
		 * mCamApplication.getDevicesFile().getAbsolutePath() + File.separator +
		 * fileName; } else { return null; }
		 */
        return null;
    }

    /**
     * @param fileName
     * @param context
     * @return
     */
    public static String getPreviewPath(String fileName, Context context) {
        /*
         * CamApplication mCamApplication = (CamApplication) context
		 * .getApplicationContext(); return
		 * mCamApplication.getEventFile().getAbsolutePath() + File.separator +
		 * fileName + ".jpeg";
		 */
        return null;
    }

    /**
     * @param fileName
     * @param suffix
     * @return
     */
    public static String getDeviceThumbPath(String fileName, String suffix) {
        return FileUtil.getInstance().getDevicesFile().getAbsolutePath()
                + File.separator + fileName + suffix;
    }

    public static String getUserThumbPath(String fileName) {
        return FileUtil.getInstance().getUserFile().getAbsolutePath()
                + File.separator + fileName;
    }

    public static JSONObject getPreviewParams(String fileName)
            throws JSONException {
        String[] Params = fileName.split("-");
        JSONObject JParams = new JSONObject();
        if (Params.length >= 3) {
            JParams.put("sn", Params[0]);
            JParams.put("time", Params[1]);
            JParams.put("type", Params[2]);
        }
        return JParams;
    }

    public static JSONObject getThumbParams(String fileName)
            throws JSONException {
        JSONObject JParams = new JSONObject();
        JParams.put("sn", fileName);
        return JParams;
    }

    public static int convertDpToPixel(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public static String getBody(String[] pairKeys, String... params) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pairKeys.length; i++)
            sb.append(pairKeys[i] + "=" + params[i] + "&");
        return sb.toString();
    }

    public static void togglePassword(EditText inputView, ImageView indicator) {
        /*
         * int type = inputView.getInputType(); if ((type &
		 * InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) { type &=
		 * ~InputType.TYPE_TEXT_VARIATION_PASSWORD;
		 * indicator.setImageResource(R.drawable.ic_show_password_checked); }
		 * else { type |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
		 * indicator.setImageResource(R.drawable.ic_show_password_default); }
		 * inputView.setInputType(type);
		 * inputView.setSelection(inputView.getText().length());
		 */
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @SuppressWarnings("deprecation")
    public static boolean isWapNetwork(Context context) {
        if (Utils.isMoblieNetwork(context)) {
            String proxyHost = android.net.Proxy.getDefaultHost();
            if (!TextUtils.isEmpty(proxyHost))
                return true;
        }
        return false;
    }

    public static boolean isMoblieNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netinfo = cm.getActiveNetworkInfo();
            if (netinfo == null || netinfo.isAvailable() == false
                    || netinfo.isConnected() == false) {
                return false;
            }
            String type = netinfo.getTypeName().toLowerCase(Locale.US);
            return "wifi".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getIMEI(Context context) {
        TelephonyManager telManage = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telManage.getDeviceId();
    }

    public static boolean IsRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                // nothing
            }
        }
        return true;
    }

    public static int getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        int MetaValue = 1;
        if (context == null || metaKey == null) {
            return 1;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                MetaValue = metaData.getInt(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return MetaValue;
    }

    // wifi 加密类型
    public static int getAuth(Wifi wifi) {
        if (wifi.getAUTH().contains("WPA2")) {
            return Constants.WifiType.WIFI_TYPE_WPA2;
        } else if (wifi.getAUTH().contains("WPA")) {
            return Constants.WifiType.WIFI_TYPE_WPA;
        } else if (wifi.getAUTH().contains("WEP")) {
            return Constants.WifiType.WIFI_TYPE_WEP;
        } else if (wifi.getAUTH().contains("WPS")) {
            return Constants.WifiType.WIFI_TYPE_NOPASSWORD;
        } else if (wifi.getAUTH().contains("Enterprise")) {
            return Constants.WifiType.WIFI_TYPE_UNSUPPORTED;
        } else if (wifi.getAUTH().contains("EAP")) {
            return Constants.WifiType.WIFI_TYPE_UNSUPPORTED;
        } else if (wifi.getAUTH().equals("")) {
            return Constants.WifiType.WIFI_TYPE_NOPASSWORD;
        } else if (wifi.getAUTH().equals("802.1x")) {
            return Constants.WifiType.WIFI_TYPE_802_1X;
        }
        return Constants.WifiType.WIFI_TYPE_UNSUPPORTED;
    }

    // wifi 加密类型
    public static int getAuth(WifiConfiguration config) {
        if (config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.TKIP)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP)
                && config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK)
                && config.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.TKIP)
                && config.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.CCMP)
                && config.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
            return Constants.WifiType.WIFI_TYPE_WPA2;
        } else if (config.allowedAuthAlgorithms
                .get(WifiConfiguration.AuthAlgorithm.OPEN)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.TKIP)
                && config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK)
                && config.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.TKIP)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP)
                && config.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.CCMP)) {
            return Constants.WifiType.WIFI_TYPE_WPA;
        } else if (config.allowedAuthAlgorithms
                .get(WifiConfiguration.AuthAlgorithm.SHARED)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.TKIP)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.WEP40)
                && config.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.WEP104)
                && config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.NONE)) {
            return Constants.WifiType.WIFI_TYPE_WEP;
        } else if (config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.NONE)) {
            return Constants.WifiType.WIFI_TYPE_NOPASSWORD;
        } else if (config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return Constants.WifiType.WIFI_TYPE_802_1X;
        } else
            return 5;
    }

    // wifi 加密类型
    public static int getAuth(String auth) {
        if (auth.contains("Enterprise")) {
            return 4;
        } else if (auth.contains("EAP")) {
            return 4;
        } else if (auth.contains("WPA2")) {
            return 3;
        } else if (auth.contains("WPA")) {
            return 2;
        } else if (auth.contains("WEP")) {
            return 1;
        } else if (auth.equals("")) {
            return 0;
        }
        return 5;
    }

    // 将127.0.0.1形式的IP地址转换成十进制整数
    public static long ipToLong(String strIp) {
        try {
            String[] arr = strIp.split("\\.");
            long[] ip = new long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ip[i] = Long.parseLong(arr[i]);
            }
            return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static long ipToLongR(String strIp) {
        try {
            String[] arr = strIp.split("\\.");
            long[] ip = new long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ip[i] = Long.parseLong(arr[i]);
            }
            return (ip[3] << 24) + (ip[2] << 16) + (ip[1] << 8) + ip[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    public static boolean isChineseChar(String str) {
        if (str.equals("")) {
            return false;
        }
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if ((char) (byte) array[i] != array[i]) {
                return true;
            }
        }
        return false;
    }

    // 是否是数字
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // 判断手机格式是否正确
    public static boolean isMobileNO(String mobiles) {
        String str = "^((13[0-9])|14[57]|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    //判断密码格式，数字，字母和下划线
    public static boolean isPassword(String pwd) {
        String str = "^\\w{6,13}$";
        Pattern pattern = Pattern.compile(str);
        Matcher matcher = pattern.matcher(pwd);
        return matcher.matches();
    }


    // 判断email格式是否正确
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static void startBrowser(Context context, String urlStr) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri urlData = Uri.parse(urlStr);
            intent.setData(urlData);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拼接带头的字符串
     *
     * @param tag      头
     * @param tagColor 头颜色
     * @param text     内容
     * @return
     */
    public static SpannableStringBuilder makeTextWithTag(String tag,
                                                         int tagColor, String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(tag);
        int end = builder.length();
        builder.append(" ");
        if (text != null) {
            builder.append(text);
        }
        builder.setSpan(new ForegroundColorSpan(tagColor), 0, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * 文字后面添加省略号
     *
     * @param text
     * @param txtView
     * @param lines   满lines行添加省略号
     */
    public static void ellipsizeString(final SpannableStringBuilder text,
                                       final TextView txtView, final int lines) {
        ViewTreeObserver observer = txtView.getViewTreeObserver();
        final String ellipsize = "...";
        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(txtView.getTextSize());
        final float ellipsizeWidth = paint.measureText(ellipsize);
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = txtView.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (txtView.getLineCount() > lines) {
                    int lineEndIndex = txtView.getLayout()
                            .getLineEnd(lines - 1);
                    int i = 0;
                    String tmp = "";
                    float width = 0.f;
                    while (width < ellipsizeWidth) {
                        tmp = "" + text.charAt(lineEndIndex - i);
                        width += paint.measureText(tmp);
                        i++;
                    }
                    text.replace(lineEndIndex - i, lineEndIndex, ellipsize);
                    text.delete(lineEndIndex - i + ellipsize.length(),
                            text.length());
                    txtView.setText(text);
                }
            }
        });
    }

    public static int getScreenWidth(Context pContext) {
        return pContext.getResources().getDisplayMetrics().widthPixels;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * dialog设置
     *
     * @param context
     * @param dialog
     * @param widthP
     * @param heightP
     * @param gravity
     */
    public static void setDialogAttributes(Activity context,
                                           final Dialog dialog, float widthP, float heightP, int gravity) {
        Display d = context.getWindowManager().getDefaultDisplay();
        LayoutParams p = dialog.getWindow().getAttributes();

        Point mPoint = new Point();
        d.getSize(mPoint);
        if (heightP != 0)
            p.height = (int) (mPoint.y * heightP);
        if (widthP != 0)
            p.width = (int) (mPoint.x * widthP);
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().setGravity(gravity);
    }

    /**
     * 加载图片
     *
     * @param mImageView
     * @param imageUrl
     */
    public static void displayUserIconImageView(ImageView mImageView, String imageUrl) {
        if (mIconOptions == null) {
            mIconOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.default_face)
                    .showImageOnFail(R.drawable.default_face)
//                    .showImageOnLoading(R.drawable.photo)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(500, true, false, false))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mIconOptions, null);
    }

    public static void displayCaptureView(ImageView mImageView, String imageUrl) {
        CLog.v("displaycover--local" + imageUrl);
        if (mCaptureCoverOptions == null) {
            mCaptureCoverOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.camera)
                    .showImageOnFail(R.drawable.camera)
                    .showImageOnLoading(R.drawable.camera)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(20, RoundedBitmapDisplayer.CORNER_TOP_LEFT
                            | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mCaptureCoverOptions, null);
    }

    public static void displayCaptureView(ImageView mImageView, String imageUrl, ImageLoadingListener imageLoadingListener) {
        CLog.v("displaycover--" + imageUrl);
        if (mCaptureCoverOptions == null) {
            mCaptureCoverOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.camera)
                    .showImageOnFail(R.drawable.camera)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(300, true, false, false))
                    .displayer(new RoundedBitmapDisplayer(20, RoundedBitmapDisplayer.CORNER_TOP_LEFT
                            | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mCaptureCoverOptions, imageLoadingListener);
    }

    public static void displayCaptureView(ImageView mImageView, String imageUrl,
                                          ImageLoadingListener imageLoadingListener,
                                          Drawable cacheDrawable, Handler handler) {
        CLog.v("displaycover--" + imageUrl);

        if (sBuilder == null) {
            sBuilder = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.camera)
                    .showImageOnFail(R.drawable.camera)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(500, true, false, false))
                    .displayer(new RoundedBitmapDisplayer(20, RoundedBitmapDisplayer.CORNER_TOP_LEFT
                            | RoundedBitmapDisplayer.CORNER_TOP_RIGHT))
                    .bitmapConfig(Bitmap.Config.RGB_565).handler(handler);

        }
        if (mDisplayCaptureCoverOptions == null) {
            mDisplayCaptureCoverOptions = sBuilder.build();
        }
        if (cacheDrawable != null) {
            CLog.v("1111 " + cacheDrawable.toString());
            sBuilder.showImageOnLoading(cacheDrawable);
        }

        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mDisplayCaptureCoverOptions, imageLoadingListener);
    }

    public static void displayLocalCaptureView(ImageView mImageView, String imageUrl) {
        if (mLocalMediaCaptureCoverOptions == null) {
            mLocalMediaCaptureCoverOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.camera)
                    .showImageOnFail(R.drawable.camera)
                    .showImageOnLoading(R.drawable.camera)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(20, RoundedBitmapDisplayer.CORNER_ALL))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mLocalMediaCaptureCoverOptions, null);
    }

    public static void displayMobileLiveCaptureView(ImageView mImageView, String imageUrl) {
        if (mMobileLiveCaptureCoverOptions == null) {
            mMobileLiveCaptureCoverOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.camera)
                    .showImageOnFail(R.drawable.camera)
                    .showImageOnLoading(R.drawable.camera)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new MobileLiveCoverDisplayer(20, MobileLiveCoverDisplayer.CORNER_ALL))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, mImageView, mMobileLiveCaptureCoverOptions, null);
    }

    /**
     * 将16进制字符串转换为ASCII码表对应的字符
     *
     * @param hex
     * @return
     */
    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    /**
     * 求两个集合的交集
     *
     * @param list1
     * @param list2
     * @return
     */
    public static List<Camera.Size> getIntersection(List<Camera.Size> list1,
                                                    List<Camera.Size> list2) {
        List<Camera.Size> result = new ArrayList<Camera.Size>();
        for (Camera.Size size : list2) {
            if (list1.contains(size)) {
                result.add(size);
            }
        }
        return result;
    }

    /**
     * 获取当前应用的uid
     *
     * @return
     */
    public static int getAppUid() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ApplicationInfo appinfo = context.getApplicationInfo();
        List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningProcess : run) {
            if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)) {
                return Integer.parseInt(String.valueOf(runningProcess.uid));
            }
        }
        return -1;
    }

    public static int getAppPid() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ApplicationInfo appinfo = context.getApplicationInfo();
        List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningProcess : run) {
            if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)) {
                return Integer.parseInt(String.valueOf(runningProcess.pid));
            }
        }
        return -1;
    }


}

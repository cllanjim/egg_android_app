/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.lingyang.sdk.util;

import android.net.Uri;

/**
 * 文件名: SchemeUtil
 * 描    述: [播放地址协议头]
 * 创建人: 波
 * 创建时间: 2016/1/11
 */
public class SchemeUtil {
    public static boolean isUrlLocalFile(String path) {
        return getPathScheme(path) == null || "file".equals(getPathScheme(path));
    }

    public static String getPathScheme(String path) {
        return Uri.parse(path).getScheme();
    }

    public static String getParamValue(String path,String key) {
        return Uri.parse(path).getQueryParameter(key);
    }

    public static String getHost(String path) {
        return Uri.parse(path).getHost();
    }

    public static int getPort(String path) {
        return Uri.parse(path).getPort();
    }
}

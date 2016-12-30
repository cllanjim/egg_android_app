package com.lingyang.sdk.util;

import android.os.Build;

public class Utils {
    /**
     * Returns whether the current device is running Android 4.4, KitKat, or newer
     * <p/>
     * KitKat is required for certain Kickflip features like Adaptive bitrate streaming
     */
    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

}

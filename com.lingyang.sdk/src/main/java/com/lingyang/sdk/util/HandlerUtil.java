package com.lingyang.sdk.util;

import android.os.Handler;
import android.os.Message;

public class HandlerUtil {
    public static void sendMsgToHandler(Handler handler, int what) {
        if (handler != null) {
            handler.sendEmptyMessage(what);
        }
    }

    public static void sendMsgToHandler(Handler handler, int what, long delayMillis) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    public static void sendMsgToHandler(Handler handler, int what, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage(what, obj);
            handler.sendMessage(msg);
        }
    }

    public static void sendMsgToHandler(Handler handler, int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage(what, arg1, arg2, obj);
            handler.sendMessage(msg);
        }
    }

    public static void sendMsgToHandler(Handler handler, int what, Object obj,
                                        long delayMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage(what, obj);
            handler.sendMessageDelayed(msg, delayMillis);
        }
    }
}

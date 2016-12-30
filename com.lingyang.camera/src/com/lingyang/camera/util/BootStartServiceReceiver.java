package com.lingyang.camera.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.lingyang.camera.service.BootStartService;

/**
 * Created by LiaoLei on 2015/11/25.
 */
public class BootStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("BootStartService:", intent.getAction());
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "BootStartServiceReceiver", Toast.LENGTH_LONG);
            }
        }, 10000);

        Intent it = new Intent();
//        it.setPackage(getPackageName());
        it.setClass(context, BootStartService.class);
        context.startService(it);
    }

    void startCloudService() {

    }
}

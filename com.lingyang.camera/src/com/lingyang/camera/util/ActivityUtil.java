package com.lingyang.camera.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.lingyang.base.utils.CLog;

import java.io.Closeable;
import java.io.IOException;

public class ActivityUtil {

	public static final boolean startActivity(Context ctx, String action) {
		return startActivity(ctx, new Intent(action));
	}

	public static final boolean startActivity(Context ctx, Class<? extends Activity> cls) {
		return startActivity(ctx, new Intent(ctx, cls));
	}

	public static final boolean startActivity(Context ctx, Intent intent) {
		CLog.v("##### start activity: " + intent);
		try {
			// intent.addCategory(Intent.CATEGORY_DEFAULT);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
			// | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ctx.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	

	public static final void close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

package com.lingyang.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.executor.ThreadPoolManager;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.exception.CrashHandler;
import com.lingyang.camera.exception.LogUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.cloud.LYService;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;
import java.util.Stack;

/**
 * 文件名: CameraApplication
 * <p/>
 * 描    述:<ul>
 * <li> 每个类和自建的public方法必须包含Javadoc注释，注释至少要包含描述该类或方法用途的语句。
 * 并且该语句应该用第三人称的动词形式来开头
 * <li>非public的、非static的字段名称以m开头。
 * <li> static字段名称以s开头。
 * <li>其它字段以小写字母开头。
 * <li> public static final字段（常量）全部字母大写并用下划线分隔。
 * <li>每行代码的长度应该不超过100个字符。
 * <li>编写简短的方法,如果方法代码超过了40行，就该考虑是否可以在不损害程序结构的前提下进行分拆
 * <li>对那些临时性的、短期的、够棒但不完美的代码，请使用TODO注释
 * <li>我们的最终想法是：保持一致
 * </ul>
 * demo code
 * <pre class="prettyprint">
 * public class MyClass {
 * public static final int SOME_CONSTANT = 42;
 * public int publicField;
 * private static MyClass sSingleton;
 * int mPackagePrivate;
 * private int mPrivate;
 * protected int mProtected;
 * }</pre>
 * 创建人: 波<p/>
 * 创建时间: 2015/12/9
 */
public class CameraApplication extends Application {

    private static final String TAG_APP = "CameraApplication";
    private static boolean sDemo = false;
    public int demo;
    /**
     * 自定义activity栈
     */
    private Stack<Activity> mActivityStack;
    /**
     * 应用是否退出
     */
    private boolean mApplicationExit = false;
    private double mDouble;

    /**
     * 获取进程名
     *
     * @param cxt
     * @param pid 进程id
     * @return
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LYService.getInstance().setDebuggable(BuildConfig.DEBUG);
        LYService.getInstance().setNativeLoggingEnabled(BuildConfig.DEBUG);
        CLog.setDebuggable(BuildConfig.DEBUG);
//        if (BuildConfig.DEBUG) {
//            LeakCanary.install(this);
//        }
        initImageLoader(getApplicationContext());
        ThreadPoolManager.initThreadPoolManager(getApplicationContext());
        Utils.setContext(getApplicationContext());
        useBugReport();
    }

    /**
     * 使用错误追踪报告
     */
    private void useBugReport() {
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        if (!BuildConfig.DEBUG) {
            LogUtil.getInstance().uploadLog2Server();
            CrashReport.initCrashReport(getApplicationContext(), Const.TENCENT_REPORT_APPID,
                    BuildConfig.DEBUG); // 初始化SDK
        }
    }

    @Override
    public void onLowMemory() {
        CLog.e("App onLowMemory:");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        CLog.e("App onTrimMemory:");
        super.onTrimMemory(level);
    }

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        // 50 MiB
        config.diskCacheSize(50 * 1024 * 1024);
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        ImageLoader.getInstance().init(config.build());
    }

    public boolean isApplicationExit() {
        return mApplicationExit;
    }

    public void setApplicationExit(boolean isCamApplicationExit) {
        this.mApplicationExit = isCamApplicationExit;
    }

    public void addActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<Activity>();
        }
        mActivityStack.add(activity);
    }

    public Activity currentActivity() {
        return mActivityStack.lastElement();
    }

    public void finishActivity() {
        Activity activity = mActivityStack.lastElement();
        finishActivityWithFinish(activity);
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            mActivityStack.remove(activity);
        }
    }

    public void finishActivityWithFinish(Activity activity) {
        if (activity != null) {
            mActivityStack.remove(activity);
            activity.finish();
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivityWithFinish(activity);
            }
        }
    }

    public void AppExit() {
        try {
            finishAllActivity();
        } catch (Exception e) {

        }
    }

    private void finishAllActivity() {
        for (int i = 0, size = mActivityStack.size(); i < size; i++) {
            if (null != mActivityStack.get(i)) {
                mActivityStack.get(i).finish();
            }
        }
        mActivityStack.clear();
    }

    static class Demo {
        public Demo() {
        }
    }

    class DemoInstance {
        public DemoInstance() {
        }
    }

}

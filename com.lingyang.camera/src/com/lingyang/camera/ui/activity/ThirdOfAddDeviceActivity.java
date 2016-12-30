package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.dialog.DialogAddDeviceFailNotify;
import com.lingyang.camera.entity.BindCameraEntity.BindEntity;
import com.lingyang.camera.entity.Group;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.entity.Wifi;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.DeviceRegisterMgmt;
import com.lingyang.camera.ui.adapter.CameraApAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.ApUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.camera.util.WifiAdmin;
import com.lingyang.sdk.cloud.LYService;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 文件名：ThirdOfAddDeviceActivity
 * 描述：搜索周围所有的摄像机列表，选择要添加的摄像机，给此摄像机配置上一步选择的WiFi
 * 具体步骤是：先给摄像机配置WiFi信息去连接此WiFi，然后手机连接摄像机热点，获取摄像机和热点信息，最后手机再切回原本的网络
 * 此类是绑定摄像机的第三步，
 * 创建人：廖蕾
 * 时间：2015/9/19
 */
public class ThirdOfAddDeviceActivity extends AppBaseActivity {

    ListView mWifiList;
    View mEmptyView;
    ImageView mRefreshImageView;
    CircularFillableLoaders mWifiProgressBar;
    TextView mWifiTextView, mTitleTextView;
    Button mAddCameraBtn, mBindButton;
    Wifi mWifi;
    CameraApAdapter mAdapter;
    WifiManager mWifiManage;
    Wifi mConnectAPWifi;
    WifiReceiver receiverWifi;
    WifiConfiguration mApWifiConfiguration;
    WifiAdmin mWifiAdmin;
    Group<Wifi> mWifis;
    IntentFilter mFilter;
    AsyncTask<String, Void, Integer> mWifiStask;
    ImageView mBackImageView;
    NetworkConnectChangedReceiver mNetworkConnectChangedReceiver;
    DeviceRegisterMgmt mDeviceRegisterMgmt;
    String mSN;
    int mPreNetWorkId;
    boolean mChangeWifi = false;
    boolean mIsRefresh = false;
    boolean mIsAdding = false, mIsSetSuc = false;
    String mHashId;
    int mRunCount = 0;
    OnClickListener mOnRefreshClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_refresh) {
                if (!mIsRefresh) {
                    mWifiStask = new GetWifiListTask();
                    mWifiStask.execute();
                } else {
                    showToast(getString(R.string.refreshing));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_third_of_add_device);
        mWifi = (Wifi) getIntent().getSerializableExtra(Const.IntentKeyConst.KEY_WIFI);
        mWifis = new Group<Wifi>();
        receiverWifi = new WifiReceiver();
        mWifiAdmin = new WifiAdmin(this);
        mWifiManage = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mTitleTextView = (TextView) findViewById(R.id.tv_header_title);
        mBackImageView = (ImageView) findViewById(R.id.iv_heder_back);
        mBackImageView.setVisibility(View.VISIBLE);
        mTitleTextView.setText(R.string.add_third_step);
        mAddCameraBtn = (Button) findViewById(R.id.btn_add_camera);
        mAddCameraBtn.setEnabled(false);
        mNetworkConnectChangedReceiver = new NetworkConnectChangedReceiver();
        mDeviceRegisterMgmt = (DeviceRegisterMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(DeviceRegisterMgmt.class);
    }

    @Override
    protected void onDestroy() {
        if (mWifiStask != null && mIsRefresh) {
            mWifiStask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBackImageView.setKeepScreenOn(false);
        unregisterReceiver(receiverWifi);
        unregisterReceiver(mNetworkConnectChangedReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, mFilter);
        mBackImageView.setKeepScreenOn(true);
        ConnectivityManager connManager =
                (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            ShowCameraAPList(null);
        }
        getWifiStateChange();
    }

    public void ShowCameraAPList(View view) {
        mRunCount = 0;
        mWifiList = (ListView) findViewById(R.id.lv_wifi_list);
        mEmptyView = findViewById(R.id.mEmptyView);
        mRefreshImageView = (ImageView) findViewById(R.id.img_refresh);
        mRefreshImageView.setOnClickListener(mOnRefreshClickListener);
        mWifiProgressBar = (CircularFillableLoaders) findViewById(R.id.pb_bar);
        mWifiTextView = (TextView) findViewById(R.id.tv_wifi_ap);
        mAdapter = new CameraApAdapter(this);
        mWifiList.setAdapter(mAdapter);
        mWifiList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parentView, View arg1,
                                    int position, long arg3) {
                Wifi mWifis = (Wifi) parentView.getItemAtPosition(position);
                if (mWifis.getAUTHTYPE() != Constants.WifiType.WIFI_TYPE_UNSUPPORTED) {
                    mConnectAPWifi = mWifis;
                    mAdapter.setSelAP(position);
                    mAddCameraBtn.setEnabled(true);
                } else {
                    showToast(getString(R.string.current_wifi_not_support) + mWifis.getAUTHTYPE());
                }
                CLog.v("onItemClick position-" + position);
            }
        });

        mWifiStask = new GetWifiListTask();
        mWifiStask.execute();
    }

    private void getWifiStateChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkConnectChangedReceiver, filter);
    }

    public void onClick_Back(View view) {
        if (mIsAdding) {
            backNotify();
        } else {
            onBackPressed();
        }
    }

    public void backNotify() {
        new AlertDialog.Builder(ThirdOfAddDeviceActivity.this)
                .setTitle(R.string.system_prompt)
                .setMessage(R.string.is_adding_camera_wait)
                .setNegativeButton(R.string.continue_wait,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToMain();
                        dialog.dismiss();
                        finish();
                    }
                }).create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mIsAdding) {
            backNotify();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 添加摄像机，给摄像机配置WiFi
     *
     * @param view
     */
    public void connectAP(View view) {
        mIsAdding = true;
        mBindButton = (Button) view;
        if (mConnectAPWifi == null) {
            showToast(getResources().getString(R.string.choose_bindcamera));
            mIsAdding = false;
            return;
        }
        mWifiProgressBar.setVisibility(View.VISIBLE);
        mWifiTextView.setText(getString(R.string.add_connecting_camera));
        mBindButton.setEnabled(false);
        if (mWifiManage.getConnectionInfo() == null) {
            showToast(getString(R.string.not_connect_wifi_now));
            mIsAdding = false;
            return;
        }
        //当前网络编号
        mPreNetWorkId = mWifiManage.getConnectionInfo().getNetworkId();
        CLog.v("preNetWorkId:" + mPreNetWorkId);
        CLog.v("mWifi info:" + mWifi.getSSID() + "-" + mWifi.getPASSWORD());

        mApWifiConfiguration = mWifiAdmin.CreateWifiInfo(
                mConnectAPWifi.getSSID(), mConnectAPWifi.getPASSWORD(),
                mConnectAPWifi.getAUTHTYPE());

        WifiConfiguration tempConfig = mWifiAdmin.IsExsits(mConnectAPWifi
                .getSSID());
        if (tempConfig != null) {
            boolean flag = mWifiManage.removeNetwork(tempConfig.networkId);
            CLog.v("removeNetwork flag:" + flag);
        }
//将摄像机热点连接保存到本地，并返回摄像机本地网络编号
        int apNetworkId = mWifiManage.addNetwork(mApWifiConfiguration);
        if (apNetworkId == -1) {
            //保存摄像机热点至本地失败
            DialogAddDeviceFailNotify n = new DialogAddDeviceFailNotify(
                    ThirdOfAddDeviceActivity.this,getText(R.string.add_third_fail_of_save_wifi).toString());
            n.show();
            mBindButton.setEnabled(true);
            return;
        }
        mWifiManage.updateNetwork(mApWifiConfiguration);
        CLog.v("add-ApNetworkId:" + apNetworkId);
        CLog.v("mWifiAdmin info:" + mConnectAPWifi.getSSID() + "-"
                + mConnectAPWifi.getPASSWORD() + "-"
                + mConnectAPWifi.getAUTHTYPE());

        // 根据mac（设备网络号（硬件唯一编号））地址获取设备sn,平台注册
        mSN = ApUtil.getDevIDByMac(mConnectAPWifi.getBSSID());
        CLog.v("ApUtil.getDevIDByMac--sn=" + mSN);
        if (mSN == null || mSN.equals("")) {
            //摄像机sn获取失败
            DialogAddDeviceFailNotify n = new DialogAddDeviceFailNotify(
                    ThirdOfAddDeviceActivity.this,getText(R.string.add_third_fail_getsn).toString());
            n.show();
            mBindButton.setEnabled(true);
        } else {
            deviceRegister(apNetworkId);
        }

    }

    /**
     * 设备注册
     *
     * @param apNetworkId 摄像机网络编号
     */
    public void deviceRegister(final int apNetworkId) {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mDeviceRegisterMgmt.register(ThirdOfAddDeviceActivity.this, mSN,
                new BaseCallBack<BindEntity>() {

                    @Override
                    public void error(ResponseError object) {
                        mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                        mIsAdding = false;
                        if (object == null) {
                            showToast((String) getText(R.string.bind_fail));
                        } else {
                            showToast(object.error_code + object.error_msg);
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mWifiProgressBar.setVisibility(View.GONE);
                                mWifiTextView.setVisibility(View.GONE);
                                mBindButton.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void success(BindEntity t) {
                        mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                        mHashId = t.cid;
                        //断开当前网络
                        mWifiManage.disconnect();
                        //打开并连接摄像机网络
                        boolean enabled = mWifiManage.enableNetwork(apNetworkId, true);
                        boolean saved = mWifiManage.saveConfiguration();
                        boolean reconnect = mWifiManage.reconnect();
                        CLog.v("enabled:" + enabled + "-saved" + saved
                                + " -add-reconnect：" + reconnect);
                        //后续监听网络编号 NetworkConnectChangedReceiver
                        mChangeWifi = true;

                    }
                });
    }

    public int getWifiList() {
        try {
            mWifiManage.setWifiEnabled(true);
            mWifiManage.startScan();
            Thread.sleep(Const.TIMEOUT * 1000);
        } catch (InterruptedException e) {
        }
        return 0;
    }

    public void filterWifiList() {
        HashMap<String, Wifi> map = new HashMap<String, Wifi>();
        for (int i = 0; i < mWifis.size(); i++) {
            Wifi wifi = mWifis.get(i);
            if (!map.containsKey(wifi.getSSID())) {
                map.put(wifi.getSSID(), wifi);
            } else {
                Wifi wifio = map.get(wifi.getSSID());
                if (wifi.getLevel() <= wifio.getLevel()) {
                    map.put(wifio.getSSID(), wifio);
                }
            }
        }
        Iterator<String> iterator = map.keySet().iterator();
        Group<Wifi> filterwifis = new Group<Wifi>();
        while (iterator.hasNext()) {
            filterwifis.add(map.get(iterator.next()));
        }
        mWifis = filterwifis;
        Collections.sort(mWifis);
    }

    /**
     * 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。
     * wifi如果打开，关闭，以及连接上可用的连接都会接到监听。
     */
    public class NetworkConnectChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                    .getAction())) {
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    if (info.isConnected()
                            && info.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (mConnectAPWifi == null || mWifiManage.getConnectionInfo() == null
                                || mWifiManage.getConnectionInfo().getSSID() == null)
                            return;
                        // 已连接摄像机热点
                        if (mWifiManage.getConnectionInfo().getSSID().trim()
                                .replace("\"", "")
                                .equals(mConnectAPWifi.getSSID())) {
                            //将摄像机所要连接的网络和密码发送给摄像机
                            // TODO: 2016/6/1 绑定摄像机
                            CLog.d("---"+mWifi.getSSID());
                            CLog.d("---"+mWifi.getPASSWORD());
                            CLog.d("---"+mWifi.getAUTHTYPE());
                            CLog.d("---"+mHashId);
                            mIsSetSuc = LYService.getInstance().connectAP(
                                    mWifi.getPASSWORD(),
                                    mWifi.getPASSWORD(),
                                    mWifi.getAUTHTYPE(),
                                    mHashId) ;

                            mWifiManage.enableNetwork(mPreNetWorkId, true);
                            mWifiManage.saveConfiguration();
                            boolean reconnect = mWifiManage.reconnect();
                            CLog.v("add-reEnabled:" + reconnect);
                        } else if (mChangeWifi && mWifiManage.getConnectionInfo()
                                .getNetworkId() == mPreNetWorkId) {
                            // 手机重新连回之前的WiFi
                            mChangeWifi = false;
                            if (!mIsSetSuc) {
                                mIsAdding = false;
                                // 摄像机添加失败
                                DialogAddDeviceFailNotify n = new DialogAddDeviceFailNotify(
                                        ThirdOfAddDeviceActivity.this,getText(R.string.add_third_fail_notify_msg).toString());
                                n.show();
                                mBindButton.setEnabled(true);
                                return;
                            }
                            mWifiProgressBar.setVisibility(View.GONE);
                            mBindButton.setEnabled(false);
                            Intent intentAp = new Intent(
                                    Const.Actions.ACTION_ACTIVITY_FOURTH_OF_ADD_DEVICE);
                            intentAp.putExtra(Const.IntentKeyConst.KEY_SN, mSN);
                            ActivityUtil.startActivity(
                                    ThirdOfAddDeviceActivity.this, intentAp);
                            mWifiProgressBar.setVisibility(View.GONE);
                            mConnectAPWifi = null;
                            finish();
                        }

                    }
                }
            }
        }
    }

    class GetWifiListTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            mIsRefresh = true;
            mRunCount++;
            return getWifiList();
        }

        @Override
        protected void onPreExecute() {
            mWifiList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mWifiProgressBar.setVisibility(View.VISIBLE);
            mWifiTextView.setText(getString(R.string.waiting));
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mIsRefresh && result == 0 && !isFinishing()) {
                mIsRefresh = false;
                mEmptyView.setVisibility(View.VISIBLE);
                mWifiTextView
                        .setText(getString(R.string.camera_setting_no_wifi_ap));
                showToast(getString(R.string.wifi_scan_failure));
            } else {
                CLog.v("onPostExecute mWifiTextView");
                mWifiTextView.setText("");
            }
            mWifiProgressBar.setVisibility(View.GONE);
            CLog.v("onPostExecute");
        }
    }

    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> scanResults = mWifiManage.getScanResults();
                if (mWifiStask != null && mIsRefresh) {
                    mWifiStask.cancel(true);
                }
                if (scanResults.size() > 0 && mIsRefresh) {
                    if (scanResults.size() == 1 && mRunCount < 2) {
                        mWifiStask = new GetWifiListTask();
                        mWifiStask.execute();
                        return;
                    }
                    mIsRefresh = false;
                    mWifis.clear();
                    for (ScanResult scanResult : scanResults) {
                        if (scanResult.SSID != null
                                && !scanResult.SSID.equals("")
                                && scanResult.SSID.startsWith("iermu")) {
                            Wifi wifi = new Wifi();
                            wifi.setSSID(scanResult.SSID);
                            wifi.setBSSID(scanResult.BSSID);
                            wifi.setPASSWORD(Const.DEFAULT_AP_PASSWORD);
                            wifi.setLevel(WifiManager.calculateSignalLevel(
                                    scanResult.level, 4));
                            wifi.setAUTH(scanResult.capabilities.replace(
                                    "[ESS]", ""));
                            if (scanResult.frequency < 2405
                                    || scanResult.frequency > 2485) {
                                wifi.setAUTHTYPE(Constants.WifiType.WIFI_TYPE_UNSUPPORTED);
                            } else {
                                wifi.setAUTHTYPE(Utils.getAuth(wifi));
                            }
                            wifi.setTIMESTAMP(System.currentTimeMillis());
                            mWifis.add(wifi);
                        }
                    }
                    filterWifiList();
                    mAdapter.setWifis(mWifis);
                    mWifiList.setVisibility(View.VISIBLE);
                    mWifiProgressBar.setVisibility(View.GONE);
                    mWifiTextView.setText("");
                }
            }
        }
    }

}

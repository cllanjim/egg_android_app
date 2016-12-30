package com.lingyang.camera.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lingyang.base.utils.AsyncTask;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.Group;
import com.lingyang.camera.entity.Wifi;
import com.lingyang.camera.ui.adapter.WifiAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 文件名：SecondOfAddDeviceActivity
 * 描述：搜索周围所有的WiFi列表，选择摄像机即将要连接的WiFi
 * 此类是绑定摄像机的第二步，
 * 创建人：廖蕾
 * 时间：2015/9/19
 */
public class SecondOfAddDeviceActivity extends AppBaseActivity {

    private final int BIND_FOR_WIFI = 1;
    private final int BIND_FOR_VOICE = 2;
    private ListView mWifiList;
    private View mEmptyView;
    private CircularFillableLoaders mWifiProgressBar;
    private TextView mWifiTextView;
    private View mWifiView;
    private Button mConnButton, mVoiceBindButton;
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() > 7) {
                mConnButton.setEnabled(true);
                mConnButton.setTextColor(getResources().getColor(R.color.white));
                mVoiceBindButton.setEnabled(true);
                mVoiceBindButton.setTextColor(getResources().getColor(R.color.white));
            } else {
                mConnButton.setEnabled(false);
                mConnButton.setTextColor(getResources().getColor(R.color.text_dark));
                mVoiceBindButton.setEnabled(false);
                mVoiceBindButton.setTextColor(getResources().getColor(R.color.text_dark));
            }
        }
    };
    private WifiAdapter mWifiAdapter;
    private WifiManager mWifiManager;
    private WifiReceiver mReceiverWifi;
    private AsyncTask<String, Void, Integer> mWifisTask;
    private Group<Wifi> mWifis;
    private Wifi mWifi;
    private TextView mWifiSsidTextView;
    private LinearLayout mPasswordLinearLayout;
    private EditText mPasswordEditText;
    private CheckBox mPasswordCheckBox;
    private boolean mPublicIsRefresh = false;
    private int mRunCount = 0;
    ;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_conn) {
                GenerateQR(BIND_FOR_WIFI);
            }
            if (v.getId() == R.id.btn_voice_conn) {
                GenerateQR(BIND_FOR_VOICE);
            } else {
                if (mPasswordCheckBox.isChecked()) {
                    //设为可见
                    mPasswordEditText.setTransformationMethod
                            (HideReturnsTransformationMethod.getInstance());
                    mPasswordEditText.setSelection
                            (mPasswordEditText.getText().toString().length());
                } else {
                    mPasswordEditText.setTransformationMethod
                            (
                                    PasswordTransformationMethod.getInstance());
                    mPasswordEditText.setSelection
                            (mPasswordEditText.getText().toString().length());
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onClick_Back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_of_add_device);
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (Const.DEBUG && currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
                    .detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifis = new Group<Wifi>();
        mReceiverWifi = new WifiReceiver();
        initView();
    }

    private void initView() {
        TextView titleTextView = (TextView) findViewById(R.id.tv_header_title);
        ImageView backImageView = (ImageView) findViewById(R.id.iv_heder_back);
        mConnButton = (Button) findViewById(R.id.btn_conn);
        mVoiceBindButton = (Button) findViewById(R.id.btn_voice_conn);
        mWifiSsidTextView = (TextView) findViewById(R.id.tv_wifi_ssid);
        mPasswordLinearLayout = (LinearLayout) findViewById(R.id.layout_password);
        mPasswordEditText = (EditText) findViewById(R.id.password_input);
        mWifiView = findViewById(R.id.wifi_list);
        mPasswordCheckBox = (CheckBox) findViewById(R.id.text_password);
        mConnButton.setOnClickListener(mOnClickListener);
        mVoiceBindButton.setOnClickListener(mOnClickListener);

        backImageView.setVisibility(View.VISIBLE);
        titleTextView.setText(R.string.add_second_step);

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                EditText _v = (EditText) view;
                if (!b) {
                    // 失去焦点
                    _v.setHint(_v.getTag().toString());
                } else {
                    String hint = _v.getHint().toString();
                    _v.setTag(hint);
                    _v.setHint("");
                }
            }
        });
        mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    GenerateQR(BIND_FOR_WIFI);
                }
                return false;
            }
        });

        mPasswordEditText.addTextChangedListener(mTextWatcher);
        mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mPasswordCheckBox.setOnClickListener(mOnClickListener);
    }

    public void GenerateQR(int type) {
        String wifi_ssid = mWifiSsidTextView.getText().toString().trim();
        String router_pwd = mPasswordEditText.getText().toString().trim();
        if (!wifi_ssid.equals("") && mWifi != null && mWifi.getSSID() != null
                && !mWifi.getSSID().equals("")
                && mWifi.getAUTHTYPE() != Constants.WifiType.WIFI_TYPE_UNSUPPORTED) {
            mWifi.setPASSWORD(router_pwd);
            if (mWifi.getAUTHTYPE() == Constants.WifiType.WIFI_TYPE_NOPASSWORD) {
                SendPassword(type);
                return;
            }
            if (mWifi.getAUTHTYPE() == Constants.WifiType.WIFI_TYPE_WEP) {
                if (router_pwd.length() < 5) {
                    showToast(getString(R.string.wifi_password_less_than_five));
                    return;
                }
            } else if (mWifi.getAUTHTYPE() == Constants.WifiType.WIFI_TYPE_WPA
                    || mWifi.getAUTHTYPE() == Constants.WifiType.WIFI_TYPE_WPA2) {
                if (router_pwd.length() < 8) {
                    showToast(getString(R.string.wifi_password_less_than_eight));
                    return;
                }
            }
            SendPassword(type);
        } else {
            showToast(getString(R.string.choose_wifi));
        }
    }

    private void SendPassword(int type) {

        Intent intent = new Intent();
        intent.putExtra(Const.IntentKeyConst.KEY_WIFI, mWifi);
        if (type == BIND_FOR_VOICE) {
            intent.setAction(Const.Actions.ACTION_ACTIVITY_SOUNDWAVE);
            ActivityUtil.startActivity(this, intent);
        } else {
            intent.setAction(Const.Actions.ACTION_ACTIVITY_THIRD_OF_ADD_DEVICE);
            ActivityUtil.startActivity(this, intent);
        }
//        finish();
//        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiverWifi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiverWifi, filter);
        ConnectivityManager connManager = (ConnectivityManager)
                getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!mWifi.isConnected()) {
            CLog.v("mWifi.isConnected():" + mWifi.isConnected());
            mPasswordEditText.setText("");
            ShowWiFiList(null);
        } else {
            if (!getWifiSign()) {
                CLog.v("getWifiSign():" + getWifiSign());
                mPasswordEditText.setText("");
                ShowWiFiList(null);
            }
        }
    }

    public void ShowWiFiList(View view) {
        mRunCount = 0;

        mWifiList = (ListView) findViewById(R.id.lv_wifi_list);
        mEmptyView = findViewById(R.id.mEmptyView);

        mWifiProgressBar = (CircularFillableLoaders) findViewById(R.id.pb_bar);
        mWifiTextView = (TextView) findViewById(R.id.tv_wifi_ap);

        mWifiAdapter = new WifiAdapter(this);
        mWifiList.setAdapter(mWifiAdapter);
        mWifiList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parentView, View arg1, int position, long arg3) {
                Wifi mWifis = (Wifi) parentView.getItemAtPosition(position);
                if (mWifis.getAUTHTYPE() != Constants.WifiType.WIFI_TYPE_UNSUPPORTED) {
                    if (mWifis.getAUTHTYPE() == Constants.WifiType.WIFI_TYPE_NOPASSWORD) {
                        mPasswordLinearLayout.setVisibility(View.GONE);
                    } else {
                        mPasswordLinearLayout.setVisibility(View.VISIBLE);
                    }
                    mWifi = mWifis;
                    mWifiSsidTextView.setText(mWifi.getSSID());
                    mWifiSsidTextView.setTextColor(getResources()
                            .getColor(R.color.k_common_text_pressed));
                    mPasswordEditText.setText("");
                    mPasswordEditText.requestFocus();
                    mWifiView.setVisibility(View.GONE);
                } else {
                    showToast(getString(R.string.current_wifi_not_support) + mWifis.getAUTHTYPE());
                }
            }
        });

        findViewById(R.id.btn_wifi_cancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWifiView.setVisibility(View.GONE);
                if (mWifisTask != null && mPublicIsRefresh) {
                    mWifisTask.cancel(true);
                    mPublicIsRefresh = false;
                }
            }
        });
        mWifiView.setVisibility(View.VISIBLE);
        mWifisTask = new GetWifiListTask();
        mWifisTask.execute();
    }

    /**
     * 获取当前wifi连接名称和是否可使用frequency
     *
     * @return
     */
    private boolean getWifiSign() {

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        if (wifiInfo != null) {
            for (ScanResult scanResult : scanResults) {
                if (scanResult.SSID.equals(wifiInfo.getSSID().replace("\"", ""))) {
                    if (scanResult.frequency < 2405 || scanResult.frequency > 2485) {
                        return false;
                    } else if (scanResult.SSID.startsWith("iermu")) {
                        return false;
                    } else {
                        mWifiSsidTextView.setText(scanResult.SSID);
                        mWifiSsidTextView.setTextColor(getResources().getColor(
                                R.color.k_common_text_pressed));
                        mWifi = new Wifi();
                        mWifi.setSSID(scanResult.SSID.trim().replace("\"", ""));
                        mWifi.setBSSID(scanResult.BSSID);
                        mWifi.setLevel(WifiManager.calculateSignalLevel(scanResult.level, 4));
                        mWifi.setAUTH(scanResult.capabilities.replace("[ESS]", ""));
                        CLog.v("mWifi.getAUTH():" + mWifi.getAUTH() + "  Utils.getAuth:"
                                + Utils.getAuth(mWifi));
                        mWifi.setAUTHTYPE(Utils.getAuth(mWifi));
                        mWifi.setTIMESTAMP(System.currentTimeMillis());

                        if (Utils.getAuth(mWifi) == Constants.WifiType.WIFI_TYPE_WEP) {
                            mPasswordLinearLayout.setVisibility(View.GONE);
                        } else {
                            mPasswordLinearLayout.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }

    }

    private int getWifiList() {
        try {
            CLog.v("setWifiEnabled：WifiState" + mWifiManager.getWifiState());
            if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED
                    && WifiManager.WIFI_STATE_ENABLING != mWifiManager.getWifiState()) {
                mWifiManager.setWifiEnabled(true);
            }
            mWifiManager.startScan();
            CLog.v("startScan");
            Thread.sleep(Const.TIMEOUT * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void filterWifiList() {
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
        Group<Wifi> filterWifis = new Group<Wifi>();
        while (iterator.hasNext()) {
            filterWifis.add(map.get(iterator.next()));
        }
        mWifis = filterWifis;
        Collections.sort(mWifis);
    }


    class GetWifiListTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            mPublicIsRefresh = true;
            mRunCount++;
            return getWifiList();
        }

        @Override
        protected void onPreExecute() {
            mWifiList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mWifiProgressBar.setVisibility(View.VISIBLE);
            mWifiTextView.setText(R.string.waiting);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mPublicIsRefresh && result == 0 && mWifiView.getVisibility() == View.VISIBLE
                    && !isFinishing()) {
                mPublicIsRefresh = false;
                mEmptyView.setVisibility(View.VISIBLE);
                mWifiTextView.setText(R.string.camera_setting_no_wifi_ap);
                showToast(getString(R.string.wifi_scan_failure));
            }
            mWifiProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifisTask != null && mPublicIsRefresh) {
            mWifisTask.cancel(true);
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                if (mWifisTask != null && mPublicIsRefresh) {
                    mWifisTask.cancel(true);
                }
                if (scanResults.size() > 0 && mPublicIsRefresh) {
                    if (scanResults.size() == 1 && mRunCount < 2) {
                        mWifisTask = new GetWifiListTask();
                        mWifisTask.execute();
                        return;
                    }
                    mPublicIsRefresh = false;
                    mWifis.clear();
                    for (ScanResult scanResult : scanResults) {
                        if (scanResult.SSID != null && !scanResult.SSID.equals("")
                                && !scanResult.SSID.startsWith("iermu")) {
                            Wifi wifi = new Wifi();
                            wifi.setSSID(scanResult.SSID);
                            wifi.setBSSID(scanResult.BSSID);
                            wifi.setLevel(WifiManager.calculateSignalLevel(scanResult.level, 4));
                            CLog.v("scanResult:SSID-" + scanResult.SSID + " capabilities-"
                                    + scanResult.capabilities);
                            wifi.setAUTH(scanResult.capabilities.replace("[ESS]", ""));
                            if (scanResult.frequency < 2405 || scanResult.frequency > 2485) {
                                wifi.setAUTHTYPE(Constants.WifiType.WIFI_TYPE_UNSUPPORTED);
                            } else {
                                wifi.setAUTHTYPE(Utils.getAuth(wifi));
                            }
                            wifi.setTIMESTAMP(System.currentTimeMillis());
                            // mWifi.setQID(mAccUtil.getQID());
                            mWifis.add(wifi);
                        }
                    }
                    filterWifiList();
                    mWifiAdapter.setWifis(mWifis);
                    mWifiList.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        }
    }
}

package com.android.inputmethod.pinyin.fragments;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.inputmethod.pinyin.R;
import com.android.inputmethod.pinyin.activitys.SettingsActivity;
import com.android.inputmethod.pinyin.activitys.WebViewActivity;
import com.android.inputmethod.pinyin.utils.ConnectivityUtil;
import com.android.inputmethod.pinyin.utils.UpdateTask;

import java.util.List;

public class HongBaoFragment extends Fragment implements AccessibilityManager.AccessibilityStateChangeListener, View.OnClickListener {

    //开关切换按钮
    private TextView pluginStatusText;
    private ImageView pluginStatusIcon;
    //AccessibilityService 管理
    private AccessibilityManager accessibilityManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hongbao, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pluginStatusText = view.findViewById(R.id.layout_control_accessibility_text);
        pluginStatusIcon = view.findViewById(R.id.layout_control_accessibility_icon);
        view.findViewById(R.id.layout_control_community).setOnClickListener(this);
        view.findViewById(R.id.linearLayout2).setOnClickListener(this);
        view.findViewById(R.id.layout_control_accessibility).setOnClickListener(this);
        view.findViewById(R.id.layout_control_settings).setOnClickListener(this);
        view.findViewById(R.id.layout_alipay).setOnClickListener(this);
        handleMaterialStatusBar();

        explicitlyLoadPreferences();
        updateServiceStatus();
    }

    private void explicitlyLoadPreferences() {
        PreferenceManager.setDefaultValues(getActivity(), R.xml.general_preferences, false);
    }

    /**
     * 适配MIUI沉浸状态栏
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleMaterialStatusBar() {
        // Not supported in APK level lower than 21
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        Window window = getActivity().getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(0xffE46C62);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateServiceStatus();
        // Check for update when WIFI is connected or on first time.
        if (ConnectivityUtil.isWifi(getActivity()) || UpdateTask.count == 0)
            new UpdateTask(getActivity(), false).update();
    }

    @Override
    public void onDestroy() {
        //移除监听服务
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_control_community:
            case R.id.linearLayout2:
                openGitHub(v);
                break;
            case R.id.layout_control_accessibility:
                openAccessibility(v);
                break;
            case R.id.layout_control_settings:
                openSettings(v);
                break;
            case R.id.layout_alipay:
                openUber(v);
                break;
        }
    }


    public void openAccessibility(View view) {
        try {
            Toast.makeText(getActivity(), getString(R.string.turn_on_toast) + pluginStatusText.getText(), Toast.LENGTH_SHORT).show();
            Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(accessibleIntent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.turn_on_error_toast), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public void openGitHub(View view) {
        Intent webViewIntent = new Intent(getActivity(), WebViewActivity.class);
        webViewIntent.putExtra("title", getString(R.string.webview_github_title));
        webViewIntent.putExtra("url", "https://github.com/geeeeeeeeek/WeChatLuckyMoney");
        startActivity(webViewIntent);
    }

    public void openUber(View view) {
        Intent webViewIntent = new Intent(getActivity(), WebViewActivity.class);
        webViewIntent.putExtra("title", getString(R.string.webview_alipay_title));
        String[] couponList = new String[]{"https://render.alipay.com/p/f/fd-j6lzqrgm/guiderofmklvtvw.html?shareId=2088422430692204&campStr=p1j%2BdzkZl018zOczaHT4Z5CLdPVCgrEXq89JsWOx1gdt05SIDMPg3PTxZbdPw9dL&sign=DEqbE64SUB0qjRQGtu%2F0BPXN9YsSXM2zqLHT1X2ufDs%3D&scene=offlinePaymentNewSns"};
        startActivity(webViewIntent);
    }

    public void openSettings(View view) {
        Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
        settingsIntent.putExtra("title", getString(R.string.preference));
        settingsIntent.putExtra("frag_id", "GeneralSettingsFragment");
        startActivity(settingsIntent);
    }


    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateServiceStatus();
    }

    /**
     * 更新当前 HongbaoService 显示状态
     */
    private void updateServiceStatus() {
        if (isServiceEnabled()) {
            pluginStatusText.setText(R.string.service_off);
            pluginStatusIcon.setBackgroundResource(R.mipmap.ic_stop);
        } else {
            pluginStatusText.setText(R.string.service_on);
            pluginStatusIcon.setBackgroundResource(R.mipmap.ic_start);
        }
    }

    /**
     * 获取 HongbaoService 是否启用状态
     *
     * @return
     */
    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getActivity().getPackageName() + "/.services.HongbaoService")) {
                return true;
            }
        }
        return false;
    }
}
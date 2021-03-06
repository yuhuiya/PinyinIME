/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.pinyin.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.inputmethod.pinyin.R;
import com.android.inputmethod.pinyin.Settings;

import java.util.List;

/**
 * Setting activity of Pinyin IME.
 */
public class KeyBoardFragment extends Fragment {
    public static final int REQUEST_SETTING = 1102;
    private static String TAG = "SettingsActivity";

    private CheckBoxPreference mKeySoundPref;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mPredictionPref;
    private PreferenceScreen mKeyboardUsePref;
    private AlertDialog mOptionsDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mKeySoundPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_sound_key));
        mVibratePref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_vibrate_key));
        mPredictionPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_prediction_key));
        mKeyboardUsePref = (PreferenceScreen) prefSet.findPreference(getString(R.string.setting_keyboard_use));
        prefSet.setOnPreferenceChangeListener(this);

        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext()));

        updatePreference(prefSet, getString(R.string.setting_advanced_key));

        updateWidgets();
        mKeyboardUsePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showOptionsMenu();
                return true;
            }
        });
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keyboard, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateWidgets();
    }

    @Override
    public void onDestroy() {
        Settings.releaseInstance();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        Settings.setKeySound(mKeySoundPref.isChecked());
        Settings.setVibrate(mVibratePref.isChecked());
        Settings.setPrediction(mPredictionPref.isChecked());

        Settings.writeBack();
    }

    public void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setIcon(R.drawable.app_icon);
        builder.setNegativeButton(android.R.string.cancel, null);
        CharSequence itemSettings = getString(R.string.ime_settings_activity_name);
        CharSequence itemInputMethod = getString(R.string.ime_name);
        builder.setItems(new CharSequence[]{itemSettings, itemInputMethod},
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        switch (position) {
                            case 0:
                                launchIMESettings();
                                break;
                            case 1:
                                InputMethodManager inputMethodMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodMgr.showInputMethodPicker();
                                break;
                        }
                    }
                });
        builder.setTitle(getString(R.string.ime_name));
        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    private void launchIMESettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivityForResult(intent, REQUEST_SETTING);
        } catch (Exception e) {
            try {
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_MAIN);
                ComponentName com = new ComponentName("com.android.settings", "com.android.settings.Settings$AvailableVirtualKeyboardActivity");
                intent2.setComponent(com);
                startActivityForResult(intent2, REQUEST_SETTING);
            } catch (Exception e2) {
                Toast.makeText(getActivity(), getString(R.string.input_setting_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateWidgets() {
        mKeySoundPref.setChecked(Settings.getKeySound());
        mVibratePref.setChecked(Settings.getVibrate());
        mPredictionPref.setChecked(Settings.getPrediction());
    }

    public void updatePreference(PreferenceGroup parentPref, String prefKey) {
        Preference preference = parentPref.findPreference(prefKey);
        if (preference == null) {
            return;
        }
        Intent intent = preference.getIntent();
        if (intent != null) {
            PackageManager pm = getActivity().getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            if (listSize == 0) {
                parentPref.removePreference(preference);
            }
        }
    }
}

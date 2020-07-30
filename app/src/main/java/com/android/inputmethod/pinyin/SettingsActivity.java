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

package com.android.inputmethod.pinyin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/**
 * Setting activity of Pinyin IME.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static String TAG = "SettingsActivity";

    private CheckBoxPreference mKeySoundPref;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mPredictionPref;
    private PreferenceScreen mKeyboardUsePref;
    private AlertDialog mOptionsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mKeySoundPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_sound_key));
        mVibratePref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_vibrate_key));
        mPredictionPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_prediction_key));
        mKeyboardUsePref = (PreferenceScreen)prefSet.findPreference(getString(R.string.setting_keyboard_use));
        prefSet.setOnPreferenceChangeListener(this);
        
        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));

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
    protected void onResume() {
        super.onResume();
        updateWidgets();
    }

    @Override
    protected void onDestroy() {
        Settings.releaseInstance();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.setKeySound(mKeySoundPref.isChecked());
        Settings.setVibrate(mVibratePref.isChecked());
        Settings.setPrediction(mPredictionPref.isChecked());

        Settings.writeBack();
    }

    public void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.app_icon);
        builder.setNegativeButton(android.R.string.cancel, null);
        CharSequence itemSettings = getString(R.string.ime_settings_activity_name);
        CharSequence itemInputMethod = getString(R.string.ime_name);
        builder.setItems(new CharSequence[] {itemSettings, itemInputMethod},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        switch (position) {
                            case 0:
                                launchSettings();
                                break;
                            case 1:
                                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

    private void launchSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
            PackageManager pm = getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            if (listSize == 0) {
                parentPref.removePreference(preference);
            }
        }
    }
}

/*
 * Copyright (C) 2016 The Xperia Open Source Project
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

package com.android.settings.xosp;

import com.android.internal.logging.MetricsLogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;

import android.os.UserHandle;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.Toast;
import com.android.internal.view.RotationPolicy;
import com.android.settings.DropDownPreference.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import static android.provider.Settings.Secure.CAMERA_GESTURE_DISABLED;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;
import static android.provider.Settings.Secure.DOZE_ENABLED;
import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.settings.R;

import com.android.settings.dashboard.DashboardContainerView;

import java.util.ArrayList;
import java.util.List;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.DisplayRotation;
import cyanogenmod.providers.CMSettings;

public class MiscPersonalizations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener{
        
        private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
        private static final String KEY_PROXIMITY_WAKE = "proximity_on_wake";
        private static final String DASHBOARD_SWITCHES = "dashboard_switches";
        private static final String DASHBOARD_COLUMNS = "dashboard_columns";
        private static final String KEY_CATEGORY_MISC = "misc";
        public static final int IMAGE_PICK = 1;
        private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

        private static final String KEY_WALLPAPER_SET = "lockscreen_wallpaper_set";
        private static final String KEY_WALLPAPER_CLEAR = "lockscreen_wallpaper_clear";

        private Preference mSetWallpaper;
        private Preference mClearWallpaper;
        private SwitchPreference mTapToWakePreference;
        private SwitchPreference mProximityCheckOnWakePreference;
        private ListPreference mDashboardSwitches;
        private ListPreference mDashboardColumns;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
        addPreferencesFromResource(R.xml.xosp_misc_cat);
        PreferenceScreen prefSet = getPreferenceScreen();
        
        PreferenceCategory miscPrefs = (PreferenceCategory) findPreference(KEY_CATEGORY_MISC);
        mSetWallpaper = (Preference) findPreference(KEY_WALLPAPER_SET);
        mClearWallpaper = (Preference) findPreference(KEY_WALLPAPER_CLEAR);
        
        mDashboardSwitches = (ListPreference) findPreference(DASHBOARD_SWITCHES);
        mDashboardSwitches.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.DASHBOARD_SWITCHES, 0)));
        mDashboardSwitches.setSummary(mDashboardSwitches.getEntry());
        mDashboardSwitches.setOnPreferenceChangeListener(this);
        
        mDashboardColumns = (ListPreference) findPreference(DASHBOARD_COLUMNS);
        mDashboardColumns.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.DASHBOARD_COLUMNS, DashboardContainerView.mDashboardValue)));
        mDashboardColumns.setSummary(mDashboardColumns.getEntry());
        mDashboardColumns.setOnPreferenceChangeListener(this);
        
        mTapToWakePreference = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        if (mTapToWakePreference != null && isTapToWakeAvailable(getResources())) {
            mTapToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            if (miscPrefs != null && mTapToWakePreference != null) {
                miscPrefs.removePreference(mTapToWakePreference);
            }
        }
        
        mProximityCheckOnWakePreference = (SwitchPreference) findPreference(KEY_PROXIMITY_WAKE);
        boolean proximityCheckOnWake = getResources().getBoolean(
                org.cyanogenmod.platform.internal.R.bool.config_proximityCheckOnWake);
        if (!proximityCheckOnWake) {
            if (miscPrefs != null && mProximityCheckOnWakePreference != null) {
                miscPrefs.removePreference(mProximityCheckOnWakePreference);
            }
            CMSettings.System.putInt(getContentResolver(), CMSettings.System.PROXIMITY_ON_WAKE, 1);
        }

        mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
        mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
        mRecentsClearAll.setOnPreferenceChangeListener(this);

        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

    }
    
    private void updateState() {
        
        // Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSetWallpaper) {
            setKeyguardWallpaper();
            return true;
        } else if (preference == mClearWallpaper) {
            clearKeyguardWallpaper();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Intent intent = new Intent();
                intent.setClassName("com.android.wallpapercropper", "com.android.wallpapercropper.WallpaperCropActivity");
                intent.putExtra("keyguardMode", "1");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    private void setKeyguardWallpaper() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }

    private void clearKeyguardWallpaper() {
        WallpaperManager wallpaperManager = null;
        wallpaperManager = WallpaperManager.getInstance(getActivity());
        wallpaperManager.clearKeyguardWallpaper();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        
        if (preference == mTapToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }
        
        if (preference == mDashboardColumns) {
            Settings.System.putInt(getContentResolver(), Settings.System.DASHBOARD_COLUMNS,
                    Integer.valueOf((String) objValue));
            mDashboardColumns.setValue(String.valueOf(objValue));
            mDashboardColumns.setSummary(mDashboardColumns.getEntry());
            return true;
        }
        
        if (preference == mDashboardSwitches) {
            Settings.System.putInt(getContentResolver(), Settings.System.DASHBOARD_SWITCHES,
                    Integer.valueOf((String) objValue));
            mDashboardSwitches.setValue(String.valueOf(objValue));
            mDashboardSwitches.setSummary(mDashboardSwitches.getEntry());
            return true;
        }

        if (preference == mRecentsClearAll) {
            boolean show = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, show ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }

        if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        }

        return true;
    }
    
    private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }
    
     protected int getMetricsCategory()
     {
        return MetricsLogger.APPLICATION;
     }
}

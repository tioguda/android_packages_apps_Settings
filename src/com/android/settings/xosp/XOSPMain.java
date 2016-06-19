/*
 * Copyright (C) 2016 The Xperia Open Source Project
 * Copyright (C) 2015 crDroid Android
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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.xosp.About;
import com.android.settings.xosp.ButtonsPersonalizations;
import com.android.settings.xosp.MiscPersonalizations;
import com.android.settings.xosp.StatusBarPersonalizations;
import com.android.settings.xosp.NotificationDrawerSettings;
import com.android.settings.xosp.LockScreenPersonalizations;
import com.android.settings.xosp.BlurPersonalizations;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;


import java.util.ArrayList;
import java.util.List;
import com.android.internal.logging.MetricsLogger;

public class XOSPMain extends SettingsPreferenceFragment {

    PagerTabStrip mPagerTabStrip;
    ViewPager mViewPager;

    String titleString[];

    ViewGroup mContainer;

    static Bundle mSavedState;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;

        View view = inflater.inflate(R.layout.xosp_settings, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mPagerTabStrip = (PagerTabStrip) view.findViewById(R.id.pagerTabStrip);
        mPagerTabStrip.setDrawFullUnderline(true);

        StatusBarAdapter StatusBarAdapter = new StatusBarAdapter(getFragmentManager());
        mViewPager.setAdapter(StatusBarAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    class StatusBarAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public StatusBarAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new StatusBarPersonalizations();
            frags[1] = new LockScreenPersonalizations();
            frags[2] = new BlurPersonalizations();
            frags[3] = new ButtonsPersonalizations();
            frags[4] = new MiscPersonalizations();
            frags[5] = new NotificationDrawerSettings();
            frags[6] = new About();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
                    getString(R.string.xosp_settings_statusbar_cat),
                    getString(R.string.xosp_settings_lockscreen_cat),
                    getString(R.string.xosp_settings_blur_cat),
                    getString(R.string.xosp_settings_buttons_cat),
                    getString(R.string.xosp_settings_misc_cat),
                    getString(R.string.notification_drawer_title),
                    getString(R.string.about_xosp)
                    };
        return titleString;
    }
    
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}
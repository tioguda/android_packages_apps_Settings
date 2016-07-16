/*
 * 
 * Copyright 2014 westcrip <westcrip@westcrip-altankrk>
 * Copyright 2015 Xperia Open Source Project 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * 
 */
package com.android.settings.xosp;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.internal.logging.MetricsLogger;
    
public class About extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
			
public static final String TAG = "About";
    
private static final String XOSP_ROM_SHARE = "share";
    
    Preference mSiteUrl;
    Preference mDownloadsSiteUrl;
    Preference mJenkinsSiteUrl;
    Preference mSourceUrl;
    Preference mDevicesSourceUrl;
    Preference mGoogleUrl;
    Preference mFacebookUrl;
    Preference mDonateUrl;
    Preference mTranslationsUrl;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_rom);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();
        mSiteUrl = findPreference("xosp_site");
        mDownloadsSiteUrl = findPreference("xosp_downloads");
        mJenkinsSiteUrl = findPreference("xosp_jenkins");
        mSourceUrl = findPreference("xosp_source");
        mDevicesSourceUrl = findPreference("xosp_devices_source");
        mGoogleUrl = findPreference("xosp_google_plus");
        mFacebookUrl = findPreference("xosp_facebook");
        mDonateUrl = findPreference("xosp_donate");
        mTranslationsUrl = findPreference("xosp_translations");
        PreferenceGroup devsGroup = (PreferenceGroup) findPreference("devs");
        ArrayList<Preference> devs = new ArrayList<Preference>();
        for (int i = 0; i < devsGroup.getPreferenceCount(); i++) {
            devs.add(devsGroup.getPreference(i));
        }
        devsGroup.removeAll();
        devsGroup.setOrderingAsAdded(false);
        Collections.shuffle(devs);
        for(int i = 0; i < devs.size(); i++) {
            Preference p = devs.get(i);
            p.setOrder(i);

            devsGroup.addPreference(p);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        return false;
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://xosp.org");
        } else if (preference == mDownloadsSiteUrl) {
            launchUrl("http://downloads.xosp.org");
        } else if (preference == mJenkinsSiteUrl) {
            launchUrl("http://jenkins.xosp.org");
        } else if (preference == mSourceUrl) {
            launchUrl("https://github.com/XOSP-Project");
        } else if (preference == mDevicesSourceUrl) {
            launchUrl("https://github.com/XOSP-Project-Devices");
        } else if (preference == mGoogleUrl) {
            launchUrl("https://plus.google.com/u/0/communities/117671498272072664538");
        } else if (preference == mFacebookUrl) {
            launchUrl("https://www.facebook.com/xosprom/");
        } else if (preference == mDonateUrl) {
            launchUrl("http://forum.xda-developers.com/donatetome.php?u=4968383");
        } else if (preference == mTranslationsUrl){
            launchUrl("https://os4fvts.oneskyapp.com/admin/project/dashboard/project/136264");
        } else if (preference.getKey().equals(XOSP_ROM_SHARE)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, String.format(
                    getActivity().getString(R.string.share_message)));
            startActivity(Intent.createChooser(intent, getActivity().getString(R.string.share_chooser_title)));
            }  else {
                // If not handled, let preferences handle it.
                return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
         return true; 
    }
    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}

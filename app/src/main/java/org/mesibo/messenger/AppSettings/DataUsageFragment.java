/** Copyright (c) 2019 Mesibo
 * https://mesibo.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the terms and condition mentioned on https://mesibo.com
 * as well as following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions, the following disclaimer and links to documentation and source code
 * repository.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of Mesibo nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Documentation
 * https://mesibo.com/documentation/
 *
 * Source Code Repository
 * https://github.com/mesibo/messenger-app-android
 *
 */
package org.mesibo.messenger.AppSettings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;

import org.mesibo.messenger.SampleAPI;
import org.mesibo.messenger.R;

public class DataUsageFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreferences sharedPreferences;


        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
                //add xml
                final ActionBar ab = ((AppCompatActivity) (getActivity())).getSupportActionBar();
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setTitle("Data usage settings");

                addPreferencesFromResource(R.xml.data_usage);
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                Preference preference = findPreference("auto");
                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
                PreferenceCategory myPrefCatcell = (PreferenceCategory) findPreference("preferenceCategorycell");
                PreferenceCategory myPrefCatwifi = (PreferenceCategory) findPreference("preferenceCategorywifi");
                PreferenceCategory myPrefCatroam = (PreferenceCategory) findPreference("preferenceCategoryroam");

                //temporary
                preferenceScreen.removePreference(myPrefCatcell);
                preferenceScreen.removePreference(myPrefCatwifi);
                preferenceScreen.removePreference(myPrefCatroam);
        }

        @Override
        public void onResume() {
                super.onResume();
                //unregister the preferenceChange listener
                getPreferenceScreen().getSharedPreferences()
                        .registerOnSharedPreferenceChangeListener(this);
                displaySwitches();
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
                PreferenceCategory myPrefCatcell = (PreferenceCategory) findPreference("preferenceCategorycell");
                PreferenceCategory myPrefCatwifi = (PreferenceCategory) findPreference("preferenceCategorywifi");
                PreferenceCategory myPrefCatroam = (PreferenceCategory) findPreference("preferenceCategoryroam");

                Preference preference = findPreference(key);
                if (preference instanceof SwitchPreferenceCompat && key.equalsIgnoreCase("auto")) {
                        SwitchPreferenceCompat datausage = (SwitchPreferenceCompat) preference;

                        boolean enabled = datausage.isChecked();
                        SampleAPI.setMediaAutoDownload(enabled);

                        try {
                                //when this is null
                                myPrefCatcell.setEnabled(!enabled);
                                myPrefCatwifi.setEnabled(!enabled);
                                myPrefCatroam.setEnabled(!enabled);
                        } catch (Exception e) {}


                }

        }

        @Override
        public void onPause() {
                super.onPause();
                //unregister the preference change listener
                getPreferenceScreen().getSharedPreferences()
                        .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
                super.onDestroy();
                //unregister event bus.
        }


        public void displaySwitches() {
                Preference preference = findPreference("auto");

                boolean autoDownload = SampleAPI.getMediaAutoDownload();
                //preference.setEnabled(autoDownload);
                SwitchPreferenceCompat datausage = (SwitchPreferenceCompat) preference;
                datausage.setChecked(autoDownload);
        }

}
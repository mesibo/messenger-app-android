/******************************************************************************
* By accessing or copying this work, you agree to comply with the following   *
* terms:                                                                      *
*                                                                             *
* Copyright (c) 2019-2024 mesibo                                              *
* https://mesibo.com                                                          *
* All rights reserved.                                                        *
*                                                                             *
* Redistribution is not permitted. Use of this software is subject to the     *
* conditions specified at https://mesibo.com . When using the source code,    *
* maintain the copyright notice, conditions, disclaimer, and  links to mesibo * 
* website, documentation and the source code repository.                      *
*                                                                             *
* Do not use the name of mesibo or its contributors to endorse products from  *
* this software without prior written permission.                             *
*                                                                             *
* This software is provided "as is" without warranties. mesibo and its        *
* contributors are not liable for any damages arising from its use.           *
*                                                                             *
* Documentation: https://docs.mesibo.com/                                     *
*                                                                             *
* Source Code Repository: https://github.com/mesibo/                          *
*******************************************************************************/

package org.mesibo.messenger.AppSettings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.mesibo.api.Mesibo;
import org.mesibo.messenger.EditProfileFragment;
import org.mesibo.messenger.R;
import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboUiDefaults;


public class SettingsActivity extends AppCompatActivity {

    Fragment mRequestingFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        MesiboUiDefaults opt = MesiboUI.getUiDefaults();

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setBackgroundDrawable(new ColorDrawable(opt.mToolbarColor));

        ab.setTitle("Settings");

        BasicSettingsFragment accountFragment = new BasicSettingsFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.settings_fragment_place,accountFragment,"null");
        ft.addToBackStack("account");
        ft.commit();



    }

    @Override
    public  void onResume() {
        super.onResume();

        Mesibo.setForegroundContext(this, 0x100, true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Settings");
    }


    @Override
    public void onBackPressed() {

        //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        // imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        if(getSupportFragmentManager().getBackStackEntryCount() > 1)

            getSupportFragmentManager().popBackStackImmediate();
        else
            finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//mEmojiEditText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if(fragment instanceof EditProfileFragment /*&& fragment.isVisible() */)
                fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void setRequestingFragment(Fragment mRequestingFragment) {
        this.mRequestingFragment = mRequestingFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mRequestingFragment!=null)
            mRequestingFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

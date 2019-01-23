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


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboUtils;
import com.mesibo.emojiview.EmojiconTextView;
import org.mesibo.messenger.EditProfileFragment;
import org.mesibo.messenger.SampleAPI;
import org.mesibo.messenger.R;


public class BasicSettingsFragment extends Fragment {


    private EmojiconTextView mUserName;
    private EmojiconTextView mUserStatus;
    private ImageView mUserImage;
    private Mesibo.UserProfile mUser = Mesibo.getSelfProfile();

    public BasicSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_basic_settings, container, false);
        final ActionBar ab = ((AppCompatActivity)(getActivity())).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Settings");

        if(null == mUser) {
            getActivity().finish();
            //TBD show alert
            return v;
        }

        mUserName = (EmojiconTextView) v.findViewById(R.id.set_self_user_name);
        mUserStatus = (EmojiconTextView) v.findViewById(R.id.set_self_status);
        mUserImage = (ImageView) v.findViewById(R.id.set_user_image);



        LinearLayout profileLayout = (LinearLayout) v.findViewById(R.id.set_picture_name_status_layout);
        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditProfileFragment RegFragment = new EditProfileFragment();
                ((SettingsActivity)getActivity()).setRequestingFragment(RegFragment);
                RegFragment.activateInSettingsMode();
                FragmentManager fm =((AppCompatActivity)(getActivity())).getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.settings_fragment_place,RegFragment,"null");
                ft.addToBackStack("profile");
                ft.commit();

            }
        });

        LinearLayout DataUsageLayout = (LinearLayout) v.findViewById(R.id.set_data_layout);
        DataUsageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataUsageFragment dataFragment = new DataUsageFragment();
                FragmentManager fm =((AppCompatActivity)(getActivity())).getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.settings_fragment_place,dataFragment,"null");
                ft.addToBackStack("datausage");
                ft.commit();

            }
        });

        LinearLayout aboutLayout = (LinearLayout) v.findViewById(R.id.set_about_layout);
        aboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutFragment aboutFragment = new AboutFragment();
                FragmentManager fm =((AppCompatActivity)(getActivity())).getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.settings_fragment_place,aboutFragment,"null");
                ft.addToBackStack("about");
                ft.commit();

            }
        });

        LinearLayout logoutLayout = (LinearLayout) v.findViewById(R.id.set_logout_layout);
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SampleAPI.startLogout();
                getActivity().finish();
            }
        });


        return  v;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        mUser = Mesibo.getSelfProfile();
        String imagePath = Mesibo.getUserProfilePicturePath(mUser, Mesibo.FileInfo.TYPE_AUTO);
        if(null != imagePath) {
            Bitmap b = BitmapFactory.decodeFile(imagePath);
            if(null != b)
                mUserImage.setImageDrawable(MesiboUtils.getRoundImageDrawable(b));
        }

        if(!TextUtils.isEmpty(mUser.name)) {
            mUserName.setText(mUser.name);
        }else {
            mUserName.setText("");
        }

        if(!TextUtils.isEmpty(mUser.status)) {
            mUserStatus.setText(mUser.status);
        }else {
            mUserStatus.setText("");
        }
    }
}

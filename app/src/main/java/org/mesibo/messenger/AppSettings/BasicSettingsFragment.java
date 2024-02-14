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


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.mesibo.emojiview.EmojiconTextView;
import com.mesibo.messaging.RoundImageDrawable;

import org.mesibo.messenger.EditProfileFragment;
import org.mesibo.messenger.R;
import org.mesibo.messenger.SampleAPI;


public class BasicSettingsFragment extends Fragment {


    private EmojiconTextView mUserName;
    private EmojiconTextView mUserStatus;
    private ImageView mUserImage;
    private MesiboProfile mUser = Mesibo.getSelfProfile();

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


    @Override
    public void onResume() {
        super.onResume();
        mUser = Mesibo.getSelfProfile();
        Bitmap image = mUser.getImage().getImageOrThumbnail();
        if(null != image) {
            mUserImage.setImageDrawable(new RoundImageDrawable(image));
        }

        mUserName.setText(mUser.getName());
        mUserStatus.setText(mUser.getString("status", ""));

    }
}

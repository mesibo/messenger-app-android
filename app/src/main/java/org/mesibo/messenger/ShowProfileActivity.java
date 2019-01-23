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

package org.mesibo.messenger;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.mesibo.api.Mesibo;

public class ShowProfileActivity extends AppCompatActivity implements ShowProfileFragment.OnFragmentInteractionListener, Mesibo.FileTransferListener, Mesibo.UserProfileUpdateListener {

    SquareImageView mUsermageView;
    Mesibo.UserProfile mUserProfile;
    Toolbar mToolbar;
    AppBarLayout mAppBarLayout;
    CoordinatorLayout mCoordinatorLayout;
    long mGroupId = 0;
    String mPeer = null;
    private String mProfilePicturePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_profile);
        mToolbar = (Toolbar) findViewById(R.id.up_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getIntent().getExtras();
        if (null == args) {
            return;
        }

        mPeer = args.getString("peer");
        mGroupId = args.getLong("groupid");

        mUserProfile = null;

        if (mGroupId > 0) {
            mUserProfile = Mesibo.getUserProfile(mGroupId);
        } else {
            mUserProfile = Mesibo.getUserProfile(mPeer);
        }
        mUsermageView = (SquareImageView) findViewById(R.id.up_image_profile);

        Mesibo.addListener(this);

        mUsermageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIManager.launchImageViewer(ShowProfileActivity.this, mProfilePicturePath);
            }

        });

        TextView userName = (TextView) findViewById(R.id.up_user_name);
        TextView userstatus = (TextView) findViewById(R.id.up_current_status);

        userName.setText(mUserProfile.name);
        long lastSeen = Mesibo.getTimestamp() - mUserProfile.lastActiveTime;
        userstatus.setVisibility(View.VISIBLE);
        if(lastSeen <= 60000) {
            userstatus.setText("Online");
        }
        else {
            String seenStatus = "";
            lastSeen = lastSeen/60000; //miutes
            if(mUserProfile.groupid > 0 || 0 == mUserProfile.lastActiveTime) {
                userstatus.setVisibility(View.GONE);
            }
            else if(lastSeen >= 2*24*60) {
                seenStatus = (int)(lastSeen/(24*60)) + " days ago";
            } else if(lastSeen >= 24*60) {
                seenStatus = "yesterday";
            } else if(lastSeen >= 120 ){
                seenStatus = (int)(lastSeen/(60)) + " hours ago";
            } else if(lastSeen >= 60) {
                seenStatus = "an hour ago";
            } else if(lastSeen >= 2) {
                seenStatus = lastSeen + " minutes ago";
            } else {
                seenStatus = "a few moments before";
            }

            userstatus.setText("Last seen " + seenStatus);

            //userstatus.setVisibility(View.GONE);
        }

        CollapsingToolbarLayout collapsingToolbar =
                findViewById(R.id.up_collapsing_toolbar);
        collapsingToolbar.setTitle("  ");
        mCoordinatorLayout = findViewById(R.id.up_profile_root);
        mAppBarLayout = findViewById(R.id.up_appbar);
        mAppBarLayout.post(new Runnable() {
            @Override
            public void run() {

                setAppBarOffset(-250);
            }
        });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //measuring for alpha
                float Alpha = ((float) (appBarLayout.getTotalScrollRange() - Math.abs(verticalOffset)) / appBarLayout.getTotalScrollRange());
                if (Alpha > 0.4)
                    Alpha = 1;
                else {
                    Alpha = Alpha + (float) 0.6;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    mUsermageView.setAlpha(Alpha);
                else {
                    AlphaAnimation alpha = new AlphaAnimation(Alpha, Alpha);
                    alpha.setDuration(0);
                    alpha.setFillAfter(true);
                    mUsermageView.startAnimation(alpha);
                }


            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ShowProfileFragment showUserProfileDetailsFragment = ShowProfileFragment.newInstance(mUserProfile);
        fragmentTransaction.add(R.id.up_fragment, showUserProfileDetailsFragment, "up");
        fragmentTransaction.commit();
    }

    private void setAppBarOffset(int offsetPx) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setTopAndBottomOffset(-60 - mAppBarLayout.getTotalScrollRange() / 2);
        //behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, null, 0, offsetPx, new int[]{0, 0});
    }

    private void setUserPicture() {
        mProfilePicturePath = Mesibo.getUserProfilePicturePath(mUserProfile, Mesibo.FileInfo.TYPE_AUTO);
        if(null == mProfilePicturePath || !Mesibo.fileExists(mProfilePicturePath))
            return;

        Bitmap b = BitmapFactory.decodeFile(mProfilePicturePath);
        if(null != b) {
            mUsermageView.setImageBitmap(b);
        }
    }

    @Override
    public void Mesibo_onUserProfileUpdated(Mesibo.UserProfile userProfile, int i, boolean refresh) {
        if(!refresh)
            return;

        if(userProfile != mUserProfile)
            return;

        if(Mesibo.isUiThread()) {
            setUserPicture();
            Mesibo.startUserProfilePictureTransfer(mUserProfile, this);
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setUserPicture();
                Mesibo.startUserProfilePictureTransfer(mUserProfile, ShowProfileActivity.this);
            }
        });



    }

    /**
     * @param file
     */
    @Override
    public boolean Mesibo_onFileTransferProgress(Mesibo.FileInfo file) {

        if(100 == file.getProgress())
            setUserPicture();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onResume() {
        super.onResume();

        if(!Mesibo.setAppInForeground(this, 0x102, true)) {
            finish();
            return;
        }

        if(mUserProfile.groupid > 0) {
            TextView userName = (TextView) findViewById(R.id.up_user_name);
            if(null != mUserProfile.name)
                userName.setText(mUserProfile.name);
        }

        setUserPicture();
        Mesibo.startUserProfilePictureTransfer(mUserProfile, this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Mesibo.setAppInForeground(this, 0x102, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mesibo.setAppInForeground(this, 0x102, false);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

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


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboUtils;
import com.mesibo.emojiview.EmojiconEditText;
import com.mesibo.emojiview.EmojiconGridView;
import com.mesibo.emojiview.EmojiconsPopup;
import com.mesibo.emojiview.emoji.Emojicon;
import com.mesibo.mediapicker.MediaPicker;
import com.mesibo.messaging.MesiboActivity;
import org.mesibo.messenger.Utils.AppUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class EditProfileFragment extends android.support.v4.app.Fragment implements MediaPicker.ImageEditorListener, Mesibo.FileTransferListener
{
    public  View mView=null;
    //private RoundedImageView mProfileImage;
    private ImageView mProfileImage;

    private  ImageView  mProfileButton;
    private String mTempFilePath = Mesibo.getFilePath(Mesibo.FileInfo.TYPE_PROFILEIMAGE) + "myProfile.jpg";

    private ProgressDialog mProgressDialog ;
    private static int MAX_NAME_CHAR = 50;
    private static int MIN_NAME_CHAR = 3;
    private static int MAX_STATUS_CHAR = 150;
    private static int MIN_STATUS_CHAR = 3;

    EmojiconEditText mEmojiNameEditText;
    EmojiconEditText mEmojiStatusEditText;

    ImageView mEmojiNameBtn;
    ImageView mEmojiStatusBtn;

    TextView mNameCharCounter;
    TextView mStatusCharCounter;
    private Mesibo.UserProfile mProfile = Mesibo.getSelfProfile();

    Fragment mHost;
    LinearLayout mSaveBtn;
    TextView mPhoneNumber;
    private static Boolean mSettingsMode = false;

    static final int CAMERA_PERMISSION_CODE = 102;

    public static final String TITLE_PERMISON_CAMERA_FAIL = "Permission Denied";
    public static final String MSG_PERMISON_CAMERA_FAIL = "Camera permission was denied by you! Change the permission from settings menu";

    public EditProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MediaPicker.launchPicker(getActivity(), MediaPicker.TYPE_CAMERAIMAGE);

            } else {
                //TBD, show alert that you can't continue
                UIManager.showAlert(getActivity(),TITLE_PERMISON_CAMERA_FAIL, MSG_PERMISON_CAMERA_FAIL);

            }
            return;

        }

        // other 'case' lines to check for other
        // permissions this app might request

    }
    public void activateInSettingsMode() {
        mSettingsMode = true;
    }
    private SampleAPI.ResponseHandler mHandler = new SampleAPI.ResponseHandler() {
        @Override
        public void HandleAPIResponse(SampleAPI.Response response) {
            Log.d(TAG, "Response: " + response);

            //http://stackoverflow.com/questions/22924825/view-not-attached-to-window-manager-crash
            if(null == getActivity())
                return;

            if(/*&& !getActivity().isDestroyed()*/  mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            if (null == response)
                return;


            if (response.op.equals("upload")) {
                if (null != SampleAPI.getToken() && response.result.equals("OK")) {
                    mProfile = Mesibo.getSelfProfile();

                    if(TextUtils.isEmpty(mProfile.picturePath)) {
                        setUserPicture();
                        return;
                    }

                    //TBD, copy original picture so that download is not required - this may be having issue as
                    // we uploaded cropped version
                    String profilePath = Mesibo.getFilePath(Mesibo.FileInfo.TYPE_PROFILEIMAGE) + response.photo;
                    if(Mesibo.renameFile(mTempFilePath, profilePath, true)) {
                    }

                    setUserPicture();
                }

            } else if (response.op.equals("profile")) {
                if (null != SampleAPI.getToken() && response.result.equals("OK")) {
                    mProfile.name = mEmojiNameEditText.getText().toString();
                    mProfile.status = mEmojiStatusEditText.getText().toString();
                    Mesibo.setSelfProfile(mProfile);

                    if(mSettingsMode) {
                        getActivity().onBackPressed();
                    }else {
                        Intent myIntent = new Intent(getActivity(), MesiboActivity.class);
                        myIntent.putExtra("homebtn", true);
                        startActivity(myIntent);
                    }
                }

            }
        }
    };

    @Override
    public boolean Mesibo_onFileTransferProgress(Mesibo.FileInfo file) {
        if(100 == file.getProgress())
            setUserPicture();

        return true;
    }

    void setUserPicture() {
        String filePath = Mesibo.getUserProfilePicturePath(mProfile, Mesibo.FileInfo.TYPE_AUTO);

        Bitmap b;
        if(Mesibo.fileExists(filePath)) {
            b = BitmapFactory.decodeFile(filePath);
            if(null != b) {
                mProfileImage.setImageDrawable(MesiboUtils.getRoundImageDrawable(b));
            }
        } else {
            //TBD, getActivity.getresource crashes sometime if activity is closing
            mProfileImage.setImageDrawable(MesiboUtils.getRoundImageDrawable(BitmapFactory.decodeResource(MainApplication.getAppContext().getResources(), com.mesibo.messaging.R.drawable.default_user_image)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register_new_profile, container, false);

        if(null == mProfile) {
            //TBD, set warning
            getActivity().finish();
            return v;
        }



        final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(null != ab) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Edit profile details");
        }
        mView = v;

        mHost = this;
        mProgressDialog = AppUtils.getProgressDialog(getActivity(), "Please wait...");
        mPhoneNumber = (TextView)v.findViewById(R.id.profile_self_phone);
        mPhoneNumber.setText(mProfile.address);

        mSaveBtn = (LinearLayout) v.findViewById(R.id.register_profile_save);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mEmojiNameEditText.getText().toString();

                if(false && name.length() < MIN_NAME_CHAR) {
                    openDialogue("Name can not be less than 3 characters", "Change Name");
                    return ;
                }

                String status = mEmojiStatusEditText.getText().toString();
                if(false && status.length()  < MIN_STATUS_CHAR) {
                    openDialogue("Status can not be less than 3 characters", "Change Status");
                    return ;
                }

                if(TextUtils.isEmpty(mProfile.name) || !name.equalsIgnoreCase(mProfile.name) || TextUtils.isEmpty(mProfile.status) || !status.equalsIgnoreCase(mProfile.status)) {
                    mProgressDialog.show();
                    mHandler.setContext(getActivity());
                    SampleAPI.setProfile(mEmojiNameEditText.getText().toString(), mEmojiStatusEditText.getText().toString(), 0, mHandler);
                } else {
                    getActivity().finish();
                }

            }
        });

        mProfileImage = (ImageView) v.findViewById(R.id.self_user_image);
        Mesibo.startUserProfilePictureTransfer(mProfile, this);
        setUserPicture();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIManager.launchImageViewer(getActivity(), Mesibo.getUserProfilePicturePath(mProfile, Mesibo.FileInfo.TYPE_AUTO));
            }
        });

        mProfileButton = (ImageView) v.findViewById(R.id.edit_user_image);
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuBuilder menuBuilder = new MenuBuilder(getActivity());
                MenuInflater inflater = new MenuInflater(getActivity());
                inflater.inflate(R.menu.image_source_menu, menuBuilder);
                MenuPopupHelper optionsMenu = new MenuPopupHelper(getActivity(), menuBuilder, v);
                optionsMenu.setForceShowIcon(true);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        if (item.getItemId() == R.id.popup_camera) {
                            if(AppUtils.aquireUserPermission(getActivity(), Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                                MediaPicker.launchPicker(getActivity(), MediaPicker.TYPE_CAMERAIMAGE);
                            }
                            return true;
                        } else if (item.getItemId() == R.id.popup_gallery) {
                            MediaPicker.launchPicker(getActivity(), MediaPicker.TYPE_FILEIMAGE);
                            return true;
                        } else if (item.getItemId() == R.id.popup_remove) {
                            mHandler.setContext(getActivity());
                            SampleAPI.setProfilePicture(null, 0, mHandler);
                            return true;
                        }
                        return false;

                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {

                    }
                });
                optionsMenu.show();

            }
        });


        mNameCharCounter = (TextView) v.findViewById(R.id.name_char_counter);
        mNameCharCounter.setText(String.valueOf(MAX_NAME_CHAR));

        mEmojiNameEditText = (EmojiconEditText) v.findViewById(R.id.name_emoji_edittext);
        if(!TextUtils.isEmpty(mProfile.name))
            mEmojiNameEditText.setText(mProfile.name);
        mEmojiNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_CHAR)});
        mEmojiNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mNameCharCounter.setText(String.valueOf(MAX_NAME_CHAR - (mEmojiNameEditText.getText().length())));

            }
        });

        mStatusCharCounter = (TextView) v.findViewById(R.id.status_char_counter);
        mStatusCharCounter.setText(String.valueOf(MAX_STATUS_CHAR));

        mEmojiStatusEditText = (EmojiconEditText) v.findViewById(R.id.status_emoji_edittext);
        if(!TextUtils.isEmpty(mProfile.status))
            mEmojiStatusEditText.setText(mProfile.status);
        mEmojiStatusEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_STATUS_CHAR)});
        mEmojiStatusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mStatusCharCounter.setText(String.valueOf(MAX_STATUS_CHAR - (mEmojiStatusEditText.getText().length())));

            }
        });

        mEmojiNameBtn = (ImageView) v.findViewById(R.id.name_emoji_btn);
        mEmojiStatusBtn = (ImageView) v.findViewById(R.id.status_emoji_btn);

        FrameLayout rootView = (FrameLayout) v.findViewById(R.id.register_new_profile_rootlayout);
        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, getActivity());

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();


        View.OnClickListener emojilistener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EmojiconEditText mEmojiEditText = mEmojiNameEditText;
                ImageView mEmojiButton = mEmojiNameBtn;

                if (v.getId() == R.id.status_emoji_btn) {
                    mEmojiEditText = mEmojiStatusEditText;
                    mEmojiButton = mEmojiStatusBtn;
                }

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {


                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(mEmojiButton, R.drawable.ic_keyboard);
                    }
                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else {
                        mEmojiEditText.setFocusableInTouchMode(true);
                        mEmojiEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mEmojiEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(mEmojiButton, R.drawable.ic_keyboard);
                    }
                }
                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    popup.dismiss();
                }
            }
        };


        mEmojiNameBtn.setOnClickListener(emojilistener);
        mEmojiStatusBtn.setOnClickListener(emojilistener);

                //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(mEmojiNameBtn, R.drawable.ic_sentiment_satisfied_black_24dp);
                changeEmojiKeyboardIcon(mEmojiStatusBtn, R.drawable.ic_sentiment_satisfied_black_24dp);


            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                EmojiconEditText mEmojiEditText = mEmojiNameEditText;
                if(mEmojiStatusEditText.hasFocus()) {
                    mEmojiEditText = mEmojiStatusEditText;
                }

                if (mEmojiEditText == null || emojicon == null) {
                    return;
                }


                int start = mEmojiEditText.getSelectionStart();
                int end = mEmojiEditText.getSelectionEnd();
                if (start < 0) {
                    mEmojiEditText.append(emojicon.getEmoji());
                } else {
                    mEmojiEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                EmojiconEditText mEmojiEditText = mEmojiNameEditText;
                if(mEmojiStatusEditText.hasFocus()) {
                    mEmojiEditText = mEmojiStatusEditText;
                }
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mEmojiEditText.dispatchKeyEvent(event);
            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("test1", "result2");

        if(RESULT_OK != resultCode)
            return;

        String filePath = MediaPicker.processOnActivityResult(getActivity(), requestCode, resultCode, data);


        if(null == filePath)
            return;

        UIManager.launchImageEditor((AppCompatActivity)getActivity(), MediaPicker.TYPE_FILEIMAGE, -1, null, filePath, false, false, true, true, 600, this);
        //mProgressDialog.show();
    }


    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    public void setImageProfile (Bitmap bmp) {
        mProfileImage.setImageDrawable(MesiboUtils.getRoundImageDrawable(bmp));

    }

    public void openDialogue(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onImageEdit(int i, String s, String filePath, Bitmap bitmap, int status) {
        //SampleAPI.setProfilePicture(mProfileFilePath, 0, mHandler);
        if(0 != status) {
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            return;
        }

        if(!saveBitmpToFilePath(bitmap, mTempFilePath)) {
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            return;
        }

        mHandler.setContext(getActivity());
        SampleAPI.setProfilePicture(mTempFilePath, 0, mHandler);
        setImageProfile(bitmap);
    }

    public static  boolean saveBitmpToFilePath(Bitmap bmp, String filePath) {
        File file = new File(filePath);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if(null != bmp) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

}

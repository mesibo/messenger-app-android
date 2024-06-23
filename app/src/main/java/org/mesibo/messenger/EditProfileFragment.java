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

package org.mesibo.messenger;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.widget.Toast;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.MesiboSelfProfile;
import com.mesibo.emojiview.EmojiconEditText;
import com.mesibo.emojiview.EmojiconGridView;
import com.mesibo.emojiview.EmojiconsPopup;
import com.mesibo.emojiview.emoji.Emojicon;
import com.mesibo.mediapicker.MediaPicker;

import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.RoundImageDrawable;

import static android.app.Activity.RESULT_OK;

import org.mesibo.messenger.Utils.AppUtils;

public class EditProfileFragment extends Fragment implements MediaPicker.ImageEditorListener, MesiboProfile.Listener
{
    public  View mView=null;
    //private RoundedImageView mProfileImage;
    private ImageView mProfileImage;

    private  ImageView  mProfileButton;

    private static int MAX_NAME_CHAR = 50;
    private static int MAX_STATUS_CHAR = 150;

    EmojiconEditText mEmojiNameEditText;
    EmojiconEditText mEmojiStatusEditText;

    ImageView mEmojiNameBtn;
    ImageView mEmojiStatusBtn;

    TextView mNameCharCounter;
    TextView mStatusCharCounter;

    Fragment mHost;
    LinearLayout mSaveBtn;
    TextView mPhoneNumber;
    private static Boolean mSettingsMode = false;
    private long mGroupId = 0;
    private boolean mLaunchMesibo = false;

    static final int CAMERA_PERMISSION_CODE = 102;

    public static final String TITLE_PERMISON_CAMERA_FAIL = "Permission Denied";
    public static final String MSG_PERMISON_CAMERA_FAIL = "Camera permission was denied by you! Grant the permission to continue";

    public static final String PROFILE_SAVE_FAIL = "Failed";
    public static final String PROFILE_SAVE_FAIL_MESSAGE = "Unable to Save Profile. Try again after some time";

    public EditProfileFragment() {
        mGroupId = 0;
    }
    
    public void setGroupId(long groupid) {
        mGroupId = groupid;
    }

    public void setLaunchMesibo(boolean launchMesibo) {
        mLaunchMesibo = launchMesibo;
    }

    public MesiboProfile getProfile() {
        if(mGroupId > 0) return Mesibo.getProfile(mGroupId);
        MesiboSelfProfile selfProfile = Mesibo.getSelfProfile();
        selfProfile.setSearchable(true);
        return selfProfile;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        //mScreen.menu = menu;
        MenuItem.OnMenuItemClickListener menuhandler = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                save();
                return true;
            }
        };

        menu.findItem(R.id.action_save).setOnMenuItemClickListener(menuhandler);
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

    }
    public void activateInSettingsMode() {
        mSettingsMode = true;
    }

    void setUserPicture() {
        MesiboProfile profile = getProfile();
        Bitmap image = profile.getImage().getImageOrThumbnail();

        if(null != image) {
            mProfileImage.setImageDrawable(new RoundImageDrawable(image));
        } else {
            mProfileImage.setImageDrawable(new RoundImageDrawable(BitmapFactory.decodeResource(MainApplication.getAppContext().getResources(), com.mesibo.messaging.R.drawable.default_user_image)));
        }

        if(true) return;

        String url = profile.getImage().getUrl();

        String filePath = getProfile().getImage().getImagePath();

        Bitmap b;
        if(Mesibo.fileExists(filePath)) {
            b = BitmapFactory.decodeFile(filePath);
            if(null != b) {
                mProfileImage.setImageDrawable(new RoundImageDrawable(b));
            }
        } else {
            mProfileImage.setImageDrawable(new RoundImageDrawable(BitmapFactory.decodeResource(MainApplication.getAppContext().getResources(), com.mesibo.messaging.R.drawable.default_user_image)));
        }
    }

    void save() {
        String name = mEmojiNameEditText.getText().toString();
        String status = mEmojiStatusEditText.getText().toString();

        MesiboProfile profile = getProfile();
        if(!TextUtils.isEmpty(name)) {
            profile.setName(name);
            profile.setString("status", status);
            profile.save();
        }

        if(mLaunchMesibo) {
            MesiboUI.MesiboUserListScreenOptions opts = new MesiboUI.MesiboUserListScreenOptions();
            opts.keepRunning = true;
            UIManager.launchMesibo(getActivity(), opts);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register_new_profile, container, false);
        setHasOptionsMenu(true);

        final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(null != ab) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Edit profile details");
        }
        mView = v;

        MesiboProfile profile = getProfile();
        profile.addListener(this);
        profile = getProfile(); // in case profile was updated in between

        mHost = this;
        mPhoneNumber = (TextView) v.findViewById(R.id.profile_self_phone);
	mProfileImage = (ImageView) v.findViewById(R.id.self_user_image);
	setUserPicture();

	mProfileImage.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
                UIManager.launchImageViewer(getActivity(), getProfile().getImage().getImagePath());
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
						setImageProfile(null);
						MesiboProfile profile = getProfile();
                            			profile.deleteImages();
						profile.setImage(null);
						profile.save();
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

        updateUI(profile);

	return v;
    }

    private void updateUI(MesiboProfile profile) {
        if(!profile.isGroup()) {
            mPhoneNumber.setText(profile.getAddress());
        } else {
            mPhoneNumber.setVisibility(View.GONE);
        }

        mEmojiStatusEditText.setText(profile.getString("status", ""));
        mEmojiNameEditText.setText(profile.getName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d("test1", "result2");

	    if(RESULT_OK != resultCode)
		    return;

	    String filePath = MediaPicker.processOnActivityResult(getActivity(), requestCode, resultCode, data);


	    if(null == filePath)
		    return;

	    UIManager.launchImageEditor((AppCompatActivity)getActivity(), MediaPicker.TYPE_FILEIMAGE, -1, null, filePath, false, false, true, true, 1200, this);
    }


    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
	    iconToBeChanged.setImageResource(drawableResourceId);
    }

    public void setImageProfile (Bitmap bmp) {
	    if(null == bmp)
		    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_image);

	    mProfileImage.setImageDrawable(new RoundImageDrawable(bmp));

    }

    public void onImageEdit(int i, String s, String filePath, Bitmap bitmap, int status) {
	    if(0 != status) {
		    return;
	    }

	    MesiboProfile profile = getProfile();
	    profile.setImage(bitmap);
	    profile.save();
	    setImageProfile(bitmap);
    }

    @Override
    public void MesiboProfile_onUpdate(MesiboProfile profile) {
        updateUI(profile);
        setUserPicture();
    }

    @Override
    public void MesiboProfile_onPublish(MesiboProfile mesiboProfile, boolean result) {
        if(!result)
            UIManager.showAlert(getActivity(),PROFILE_SAVE_FAIL, PROFILE_SAVE_FAIL_MESSAGE);
        else {
            Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_LONG).show();
            //getActivity().finish();
        }
    }

    @Override
    public void MesiboProfile_onEndToEndEncryption(MesiboProfile mesiboProfile, int i) {

    }

    @Override
    public void onPause() {
        super.onPause();
        MesiboProfile profile = getProfile();
        profile.removeListener(this);
    }
}

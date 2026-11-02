package org.mesibo.messenger.AppSettings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.RoundImageDrawable;
import org.mesibo.messenger.R;
import org.mesibo.messenger.SampleAPI;
import org.mesibo.messenger.UIManager;

public class BasicSettingsFragment extends Fragment {

    private static final String PRIVACY_URL = "https://mesibo.com/terms-of-use/";

    private TextView mUserName;
    private TextView mUserStatus;
    private ImageView mUserImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_basic_settings, container, false);

        mUserName = v.findViewById(R.id.set_self_user_name);
        mUserStatus = v.findViewById(R.id.set_self_status);
        mUserImage = v.findViewById(R.id.set_user_image);

        v.findViewById(R.id.set_picture_name_status_layout).setOnClickListener(
                b -> UIManager.launchEditProfile(requireActivity(), false));

        SwitchCompat notifyMessages = v.findViewById(R.id.set_notify_messages);
        notifyMessages.setChecked(SampleAPI.getMessageNotification());
        notifyMessages.setOnCheckedChangeListener((b, checked) -> SampleAPI.setMessageNotification(checked));

        SwitchCompat notifyGroups = v.findViewById(R.id.set_notify_groups);
        notifyGroups.setChecked(SampleAPI.getGroupMessageNotification());
        notifyGroups.setOnCheckedChangeListener((b, checked) -> SampleAPI.setGroupMessageNotification(checked));

        TextView themeValue = v.findViewById(R.id.set_theme_value);
        themeValue.setText(UIManager.getThemeName(requireContext()));
        v.findViewById(R.id.set_theme_layout).setOnClickListener(b ->
                UIManager.chooseTheme(requireActivity(), () -> themeValue.setText(UIManager.getThemeName(requireContext()))));

        v.findViewById(R.id.set_about_layout).setOnClickListener(b ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.settings_fragment_place, new AboutFragment())
                        .addToBackStack("about")
                        .commit());

        v.findViewById(R.id.privacy_policy_layout).setOnClickListener(b -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL)));
            } catch (ActivityNotFoundException e) {
                // no browser
            }
        });

        v.findViewById(R.id.set_logout_layout).setOnClickListener(b -> {
            SampleAPI.startLogout();
            requireActivity().finish();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (null != ab) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Settings");
        }

        // refreshed on every resume: the profile may have been edited
        MesiboProfile user = Mesibo.getSelfProfile();
        if (null == user) {
            requireActivity().finish();
            return;
        }

        Bitmap image = user.getImage().getImageOrThumbnail();
        if (null != image)
            mUserImage.setImageDrawable(new RoundImageDrawable(image));
        mUserName.setText(user.getName());
        mUserStatus.setText(user.getString("status", ""));
    }
}

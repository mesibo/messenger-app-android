package org.mesibo.messenger.AppSettings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.mesibo.messenger.BuildConfig;
import org.mesibo.messenger.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about, container, false);
        ((TextView) v.findViewById(R.id.version)).setText("Version " + BuildConfig.BUILD_VERSION);
        ((TextView) v.findViewById(R.id.builddate)).setText("Built " + BuildConfig.BUILD_TIMESTAMP);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("About");
    }

    private void setTitle(String title) {
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (null != ab)
            ab.setTitle(title);
    }
}

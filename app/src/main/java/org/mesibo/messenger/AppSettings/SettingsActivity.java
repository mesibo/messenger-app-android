package org.mesibo.messenger.AppSettings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mesibo.api.Mesibo;
import org.mesibo.messenger.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Settings");

        // colors come from MesiboUiDefaults, same as the mesibo screens
        MesiboToolbarStyle.apply(this, toolbar, findViewById(R.id.settings_appbar),
                findViewById(R.id.settings_fragment_place));

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment_place, new BasicSettingsFragment())
                    .addToBackStack("account")
                    .commit();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1)
                    getSupportFragmentManager().popBackStackImmediate();
                else
                    finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Mesibo.setForegroundContext(this, 0x100, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

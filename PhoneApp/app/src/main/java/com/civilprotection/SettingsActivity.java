package com.civilprotection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        sessionId = Integer.parseInt(readStringSetting("session_id"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) return true;
        return super.onSupportNavigateUp();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Update connection values
        switch (key) {
            case "time_out_time":
                // Add desired handling
                break;
            case "session_id":
                if (isNumeric(readStringSetting(key))) {
                    sessionId = Integer.parseInt(readStringSetting(key));
                } else {
                    setStringSetting(key, String.valueOf(sessionId));
                    finish();
                    startActivity(new Intent(this, com.civilprotection.SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    Toast.makeText(this, "Please provide a numeric ID", Toast.LENGTH_LONG).show();
                }
                break;
            case "last_will_retain":
                // Add desired handling
                break;
            case "ssl":
                // Add desired handling
                break;
            case "dark_theme":
                toggleTheme(key);
                break;
            case "keep_alive_time":
                // Add desired handling
                break;
            case "last_will_payload":
                // Add desired handling
                break;
            case "last_will_topic":
                // Add desired handling
                break;
            case "last_will_qos":
                // Add desired handling
                break;
            case "password":
                // Add desired handling
                break;
            case "qos":
                // Add desired handling
                break;
            case "server_ip":
                finish();
                startActivity(new Intent(this, com.civilprotection.SettingsActivity.class));
                overridePendingTransition(0, 0);
                break;
            case "use_auth":
                // Add desired handling
                break;
            case "server_port":
                if (!isNumeric(readStringSetting(key))) {
                    setStringSetting(key, String.valueOf(1883));
                    finish();
                    startActivity(new Intent(this, com.civilprotection.SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    Toast.makeText(this, "Port number must be an integer", Toast.LENGTH_LONG).show();
                } else {
                    // Add desired handling
                }
                break;
            case "username":
                // Add desired handling
                break;
            default:
                break;
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        public SettingsFragment() {
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    public static class LastWillSettings extends PreferenceFragmentCompat {
        public LastWillSettings() {
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.last_will_preferences, rootKey);
        }
    }

    public static class SecuritySettings extends PreferenceFragmentCompat {
        public SecuritySettings() {
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.security_preferences, rootKey);
        }
    }

    private void toggleTheme(String key) {

        if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, true).apply();
                Toast.makeText(this, "Dark theme enabled", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, false).apply();
                Toast.makeText(this, "Dark theme disabled", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // Retrieves value of string-type setting "key"
    private String readStringSetting(String key) {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(key, "-1");
    }

    // Retrieves value of int-type setting "key"
    private Integer readIntSetting(String key) {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, -1);
    }

    // Retrieves value of switch-type setting "key"
    private Boolean readBooleanSetting(String key) {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false);
    }

    // Sets the "key" string setting to the desired "value"
    private void setStringSetting(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(key, value).apply();
    }

    // Sets the "key" int setting to the desired "value"
    private void setIntSetting(String key, Integer value) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(key, value).apply();
    }

    // Sets the "key" switch setting to the desired "value"
    private void setBooleanSetting(String key, Boolean value) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, value).apply();
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

}
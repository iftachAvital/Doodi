package com.example.iftach.doodi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private EditText nameEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText deviceIdEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nameEdit = findViewById(R.id.name_edit_text);
        usernameEdit = findViewById(R.id.username_edit_text);
        passwordEdit = findViewById(R.id.password_edit_text);
        deviceIdEdit = findViewById(R.id.deviceid_edit_text);
        Button saveButton = findViewById(R.id.button_save);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        nameEdit.setText(preferences.getString(Constants.NAME_EXTRA, ""));
        usernameEdit.setText(preferences.getString(Constants.USERNAME_EXTRA, ""));
        passwordEdit.setText(preferences.getString(Constants.PASSWORD_EXTRA, ""));
        deviceIdEdit.setText(preferences.getString(Constants.DEVICE_ID_EXTRA, ""));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.NAME_EXTRA, nameEdit.getText().toString());
                editor.putString(Constants.USERNAME_EXTRA, usernameEdit.getText().toString());
                editor.putString(Constants.PASSWORD_EXTRA, passwordEdit.getText().toString());
                editor.putString(Constants.DEVICE_ID_EXTRA, deviceIdEdit.getText().toString());
                editor.apply();
                finish();
            }
        });
    }
}

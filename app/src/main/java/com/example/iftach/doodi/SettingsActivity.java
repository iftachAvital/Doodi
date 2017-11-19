package com.example.iftach.doodi;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private MyApplication myApplication;

    private EditText nameEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText deviceIdEdit;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myApplication = (MyApplication) getApplication();

        nameEdit = (EditText) findViewById(R.id.name_edit_text);
        usernameEdit = (EditText) findViewById(R.id.username_edit_text);
        passwordEdit = (EditText) findViewById(R.id.password_edit_text);
        deviceIdEdit = (EditText) findViewById(R.id.deviceid_edit_text);
        saveButton = (Button) findViewById(R.id.button_save);

        nameEdit.setText(myApplication.getName());
        usernameEdit.setText(myApplication.getUsername());
        passwordEdit.setText(myApplication.getPassword());
        deviceIdEdit.setText(myApplication.getDeviceId());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myApplication.setName(nameEdit.getText().toString());
                myApplication.setUsername(usernameEdit.getText().toString());
                myApplication.setPassword(passwordEdit.getText().toString());
                myApplication.setDeviceId(deviceIdEdit.getText().toString());
                finish();
            }
        });
    }
}

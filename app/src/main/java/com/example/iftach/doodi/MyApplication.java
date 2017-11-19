package com.example.iftach.doodi;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by iftach on 14/11/17.
 */

public class MyApplication extends Application {
    private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    private static final String NAME_STRING = "NAME_STRING";
    private static final String USERNAME_STRING = "USERNAME_STRING";
    private static final String PASSWORD_STRING = "PASSWORD_STRING";
    private static final String TOKEN_STRING = "TOKEN_STRING";
    private static final String TOKEN_EXPIRATION_STRING = "TOKEN_EXPIRATION_STRING";
    private static final String DEVICEID_STRING = "DEVICEID_STRING";

    private SharedPreferences sharedPref;
    private String username;
    private String name;
    private String password;
    private String deviceId;

    private ParticleDevice device;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = this.getSharedPreferences(SHARED_PREFERENCES , Context.MODE_PRIVATE);

        name = sharedPref.getString(NAME_STRING, "");
        username = sharedPref.getString(USERNAME_STRING, "");
        password = sharedPref.getString(PASSWORD_STRING, "");
        deviceId = sharedPref.getString(DEVICEID_STRING, "");
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public ParticleDevice getDevice() {
        return device;
    }

    public void setName(String name) {
        this.name = name;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(NAME_STRING, name);
        editor.apply();
    }

    public void setPassword(String password) {
        this.password = password;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PASSWORD_STRING, password);
        editor.apply();
    }

    public void setUsername(String username) {
        this.username = username;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERNAME_STRING, username);
        editor.apply();
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(DEVICEID_STRING, deviceId);
        editor.apply();
    }

    public void setDevice(ParticleDevice device) {
        this.device = device;
    }
}

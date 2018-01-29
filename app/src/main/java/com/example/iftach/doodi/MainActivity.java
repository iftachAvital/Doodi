package com.example.iftach.doodi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private ParticleDevice device;
    private Progress progress;
    private Switch relaySwitch;
    
    private long subscriptionId = -1;
    private String name;
    private String deviceId;

    private GetRelayStatus.Listener getRelayStatusListener = new GetRelayStatus.Listener() {
        @Override
        public void onEvent(ParticleEvent particleEvent) {
            relaySwitch.setChecked(Integer.parseInt(particleEvent.dataPayload) != 0);
        }

        @Override
        public void onStart() {
            progress.showProgress("Getting relay status..");
        }

        @Override
        public void onSuccess(int relayStatus, long aSubscriptionId) {
            progress.dismissProgress();
            relaySwitch.setChecked(relayStatus != 0);
            subscriptionId = aSubscriptionId;
        }

        @Override
        public void onFailure(Exception e) {
            progress.dismissProgress();
        }
    };

    private GetDevice.Listener getDeviceListener = new GetDevice.Listener() {
        @Override
        public void onStart() {
            progress.showProgress("Connecting to device..");
        }

        @Override
        public void onSuccess(ParticleDevice aDevice) {
            progress.dismissProgress();

            device = aDevice;

            Log.i(TAG, device.getStatus());

            if (device.isConnected()) {
                new GetRelayStatus(device, getRelayStatusListener).execute();
            }
            else {
                new DialogMessage(MainActivity.this, "Error", "Device is offline",
                        false, "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        new GetDevice(deviceId, getDeviceListener).execute();
                    }
                }, "Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
            }
        }

        @Override
        public void onFailure(Exception e) {
            progress.dismissProgress();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = new Progress(this);
        relaySwitch = findViewById(R.id.relay_switch);

        ParticleCloudSDK.init(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startSettings();
            return true;
        }

        if (id == R.id.action_logout) {
            // TODO: logout from particle also
            startSettings();
            return true;
        }

        if (id == R.id.action_refresh) {
            new GetDevice(deviceId, getDeviceListener).execute();
            return true;
        }

        if (id == R.id.action_alarms) {
            if (device != null) {
                Intent intent = new Intent(MainActivity.this, AlarmsActivity.class);
                intent.putExtra(Constants.DEVICE_EXTRA, device);
                intent.putExtra(Constants.NAME_EXTRA, name);
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        name = preferences.getString(Constants.NAME_EXTRA, "");
        String username = preferences.getString(Constants.USERNAME_EXTRA, "");
        String password = preferences.getString(Constants.PASSWORD_EXTRA, "");
        deviceId = preferences.getString(Constants.DEVICE_ID_EXTRA, "");

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || deviceId.isEmpty()) {
            Log.d(TAG, "missing settings data, starting data activity");
            startSettings();
        }
        else if (!ParticleCloudSDK.getCloud().isLoggedIn()) {
            new SignInParticle(username, password, new SignInParticle.Listener() {
                @Override
                public void onStart() {
                    progress.showProgress("Logging to the cloud..");
                }

                @Override
                public void onSuccess() {
                    progress.dismissProgress();
                    new GetDevice(deviceId, getDeviceListener).execute();
                }

                @Override
                public void onFailure(Exception e) {
                    progress.dismissProgress();
                    // TODO: show error message
                    startSettings();
                }
            }).execute();
        }
        else {
            Log.d(TAG, "have token in effective");
            new GetDevice(deviceId, getDeviceListener).execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (subscriptionId != -1 && device != null) {
            try {
                device.unsubscribeFromEvents(subscriptionId);
                subscriptionId = -1;
            } catch (ParticleCloudException e) {
                e.printStackTrace();
                Log.e(TAG, e.getBestMessage());
            }
        }

    }

    private void startSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}

package com.example.iftach.doodi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";
    private static final String RELAY_STATUS_VAR_NAME = "relay_stat";
    private MyApplication myApplication;
    private ProgressDialog progress;
    private Switch relaySwitch;
    private long subscriptionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myApplication = (MyApplication) getApplication();
        progress = new ProgressDialog(this);
        relaySwitch = (Switch) findViewById(R.id.relay_switch);

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
            myApplication.setName("");
            myApplication.setPassword("");
            myApplication.setUsername("");
            // TODO: logout from particle also
            startSettings();
            return true;
        }

        if (id == R.id.action_refresh) {
            new GetDevice().execute();
            return true;
        }

        if (id == R.id.action_alarms) {
            Intent intent = new Intent(MainActivity.this, AlarmsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onStart() {
        super.onStart();
        if (myApplication.getName().isEmpty() || myApplication.getUsername().isEmpty() ||
                myApplication.getPassword().isEmpty() || myApplication.getDeviceId().isEmpty()) {
            startSettings();
        }
        else if (!ParticleCloudSDK.getCloud().isLoggedIn()) {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    setProgress("Logging to the cloud..");
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        ParticleCloudSDK.getCloud().logIn(myApplication.getUsername(), myApplication.getPassword());
                        Log.d(TAG, "logged in");
                        return true;
                    } catch (ParticleCloudException e) {
                        Log.e(TAG, e.getBestMessage());
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    progress.dismiss();
                    if (aBoolean) {
                        new GetDevice().execute();
                    }
                    else {
                        // TODO: show error message
                        startSettings();
                    }
                }
            }.execute();
        }
        else {
            Log.d(TAG, "have token in effective");
            new GetDevice().execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (subscriptionId != -1) {
            try {
                myApplication.getDevice().unsubscribeFromEvents(subscriptionId);
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

    private void setProgress(String message) {
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();
    }

    private class GetDevice extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress("Connecting to device..");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ParticleDevice device = ParticleCloudSDK.getCloud().getDevice(myApplication.getDeviceId());
                myApplication.setDevice(device);
                return true;
            } catch (ParticleCloudException e) {
                Log.e(TAG, e.getBestMessage());
                e.printStackTrace();
                // TODO: show error message
                myApplication.setDevice(null);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();

            if (aBoolean) {
                Log.i(TAG, myApplication.getDevice().getStatus());
                if (myApplication.getDevice().isConnected()) {
                    new GetRelayStatus().execute();
                }
                else {
                    new DialogMessage(MainActivity.this, "Error", "Device is offline",
                            false, "Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new GetDevice().execute();
                                }
                            });
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
            else {
                // TODO: show error message
            }
        }
    }

    private class GetRelayStatus extends AsyncTask <Void, Void, Boolean> {

        private int relayStatus;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress("Getting relay status..");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (myApplication.getDevice() != null) {
                try {
                    relayStatus = myApplication.getDevice().getIntVariable(RELAY_STATUS_VAR_NAME);

                    try {
                        subscriptionId = myApplication.getDevice().subscribeToEvents("relay-event", new ParticleEventHandler() {
                            @Override
                            public void onEventError(Exception e) {
                                Log.e(TAG, e.getMessage());
                                // TODO: handle error
                            }

                            @Override
                            public void onEvent(String eventName, final ParticleEvent particleEvent) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        relaySwitch.setChecked(Integer.parseInt(particleEvent.dataPayload) != 0);
                                    }
                                });
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                } catch (ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                    // TODO: show error message
                    return false;
                }
            }
            else {
                // TODO: Device is null handle it..
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();

            if (aBoolean) {
                Log.i(TAG, "relay status=" + relayStatus);
                relaySwitch.setChecked(relayStatus != 0);
            }
            else {
                // TODO: show error message
            }
        }
    }
}

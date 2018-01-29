package com.example.iftach.doodi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;

public class AlarmsActivity extends AppCompatActivity {

    private static final String TAG = "ALARMS";
    private Progress progress;
    private long subscriptionId = -1;
    private ParticleDevice device;
    private String name;

    private GetAlarms.Listener getAlarmsListener = new GetAlarms.Listener() {
        @Override
        public void onEvent(ParticleEvent particleEvent) {
            new GetAlarms(device, getAlarmsListener, subscriptionId).execute();
        }

        @Override
        public void onStart() {
            progress.showProgress("Loading alarms..");
        }

        @Override
        public void onSuccess(String alarmsStr, long aSubscriptionId) {
            progress.dismissProgress();
            Log.d(TAG, alarmsStr);
            subscriptionId = aSubscriptionId;

            String alarmsSplits[] = alarmsStr.split("\n");
            ArrayList<Alarm> alarmArrayList = new ArrayList<>(alarmsSplits.length);

            for (String alarmsSplit : alarmsSplits) {
                Alarm alarm = new Alarm(alarmsSplit);
                if (alarm.getDuration() > 0) {
                    alarmArrayList.add(alarm);
                }
            }

            AlarmAdapter alarmAdapter = new AlarmAdapter(AlarmsActivity.this, alarmArrayList);
            ListView listView = findViewById(R.id.alarms_list_view);
            listView.setAdapter(alarmAdapter);
        }

        @Override
        public void onFailure(Exception e) {
            progress.dismissProgress();
            // TODO: handle error
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = new Progress(this);
        device = getIntent().getParcelableExtra(Constants.DEVICE_EXTRA);
        name = getIntent().getStringExtra(Constants.NAME_EXTRA);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alarms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new_alarm) {
            Intent intent = new Intent(AlarmsActivity.this, NewAlarmActivity.class);
            intent.putExtra(Constants.NAME_EXTRA, name);
            intent.putExtra(Constants.DEVICE_EXTRA, device);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (device == null) {
            finish();
        }
        else {
            new GetAlarms(device, getAlarmsListener, subscriptionId).execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (subscriptionId != -1) {
            try {
                Log.d(TAG, "unsubscribe from alarm-event");
                device.unsubscribeFromEvents(subscriptionId);
                subscriptionId = -1;
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
        }
    }

    private class AlarmAdapter extends ArrayAdapter<Alarm> {

        AlarmAdapter(@NonNull Context context, @NonNull List<Alarm> alarms) {
            super(context, R.layout.alarm_item, alarms);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Alarm alarm = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_item, parent, false);
            }

            TextView nameText = convertView.findViewById(R.id.alarm_name);
            TextView timeText = convertView.findViewById(R.id.alarm_time);
            TextView durText = convertView.findViewById(R.id.alarm_duration);

            if (alarm != null) {
                nameText.setText(alarm.getName());
                final String timeStr = alarm.getTimeString();
                timeText.setText(timeStr);
                durText.setText(String.valueOf(alarm.getDuration()));

                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Log.d(TAG, "clicked on alarm number: " + position);

                        new DialogMessage(AlarmsActivity.this, "Delete Alarm",
                                "Are you sure you want to delete alarm: " + alarm.getName()
                                        + " on " + timeStr + "?", true, "Delete",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        new DeleteAlarm(device, position, new DeleteAlarm.Listener() {
                                            @Override
                                            public void onStart() {
                                                progress.showProgress("Deleting alarm..");
                                            }

                                            @Override
                                            public void onSuccess(int res) {
                                                progress.dismissProgress();
                                                new GetAlarms(device, getAlarmsListener, subscriptionId);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                progress.dismissProgress();
                                            }
                                        }).execute();
                                    }
                                }, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        }).show();
                        return false;
                    }

                });
            }
            return convertView;
        }
    }

//    private class DeleteAlarm extends AsyncTask<Void, Void, Boolean> {
//
//        private int res;
//        private int alarmNum;
//
//        public DeleteAlarm(int alarmNum) {
//            this.alarmNum = alarmNum;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            setProgress("Deleting alarm..");
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            if (device != null) {
//                if (device.isConnected()) {
//                    ArrayList<String> arrayList = new ArrayList<>(1);
//                    arrayList.add(String.valueOf(alarmNum));
//                    try {
//                        res = device.callFunction("deleteAlarm", arrayList);
//                        return true;
//                    } catch (ParticleCloudException | IOException | ParticleDevice.FunctionDoesNotExistException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            progress.dismiss();
//
//            if (aBoolean) {
//                new GetAlarms().execute();
//            }
//            else {
//                finish();
//            }
//        }
//    }
}

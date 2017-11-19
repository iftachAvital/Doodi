package com.example.iftach.doodi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

public class AlarmsActivity extends AppCompatActivity {

    private static final String TAG = "ALARMS";
    private MyApplication myApplication;
    private ProgressDialog progress;
    private long subscriptionId = -1;

    private ParticleEventHandler particleEventHandler = new ParticleEventHandler() {
        @Override
        public void onEventError(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onEvent(String eventName, ParticleEvent particleEvent) {
            Log.d(TAG, "onEvent:" + eventName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new GetAlarms().execute();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myApplication = (MyApplication) getApplication();
        progress = new ProgressDialog(this);
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
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (myApplication.getDevice() == null) {
            finish();
        }
        else {
            new GetAlarms().execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (subscriptionId != -1) {
            try {
                Log.d(TAG, "unsubscribe from alarm-event");
                myApplication.getDevice().unsubscribeFromEvents(subscriptionId);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
        }
    }

    private void setProgress(String message) {
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();
    }

    private class AlarmAdapter extends ArrayAdapter<Alarm> {

        public AlarmAdapter(@NonNull Context context, @NonNull List<Alarm> alarms) {
            super(context, R.layout.alarm_item, alarms);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Alarm alarm = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_item, parent, false);
            }

            TextView timeText = (TextView) convertView.findViewById(R.id.alarm_time);
            TextView nameText = (TextView) convertView.findViewById(R.id.alarm_name);
            TextView durText = (TextView) convertView.findViewById(R.id.alarm_duration);

            String timeStr = String.valueOf(alarm.getHour()) + ":";
            if (alarm.getMinute() < 10) {
                timeStr += "0";
            }
            timeStr += String.valueOf(alarm.getMinute());

            timeText.setText(timeStr);
            nameText.setText(alarm.name);
            durText.setText(String.valueOf(alarm.getDuration()));

            final String finalTimeStr = timeStr;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d(TAG, "clicked on alarm number: " + position);

                    new DialogMessage(AlarmsActivity.this, "Delete Alarm",
                            "Are you sure you want to delete alarm: " + alarm.getName()
                                    + " on " + finalTimeStr + "?", true, "Delete",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    new DeleteAlarm(position).execute();
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
            return convertView;
        }
    }

    private class GetAlarms extends AsyncTask<Void, Void, Boolean> {
        private String alarmsStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress("Loading alarms..");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                alarmsStr = myApplication.getDevice().getStringVariable("alarms_str");
                if (subscriptionId == -1) {
                    Log.d(TAG, "subscribe to alarm-event");
                    subscriptionId = myApplication.getDevice().subscribeToEvents("alarm-event", particleEventHandler);
                }
                return true;
            } catch (ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();

            if (aBoolean) {
                Log.d(TAG, alarmsStr);

                String alarmsSplits[] = alarmsStr.split("\n");
                ArrayList<Alarm> alarmArrayList = new ArrayList<>(alarmsSplits.length);

                for (int i=0; i < alarmsSplits.length; i++) {
                    Alarm alarm = new Alarm(alarmsSplits[i]);
                    if (alarm.getDuration() > 0) {
                        alarmArrayList.add(alarm);
                    }
                }

                AlarmAdapter alarmAdapter = new AlarmAdapter(AlarmsActivity.this, alarmArrayList);
                ListView listView = (ListView) findViewById(R.id.alarms_list_view);
                listView.setAdapter(alarmAdapter);
            }
            else {
                // TODO: handle error
            }
        }
    }

    private class DeleteAlarm extends AsyncTask<Void, Void, Boolean> {

        private int res;
        private int alarmNum;

        public DeleteAlarm(int alarmNum) {
            this.alarmNum = alarmNum;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress("Deleting alarm..");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (myApplication.getDevice() != null) {
                if (myApplication.getDevice().isConnected()) {
                    ArrayList<String> arrayList = new ArrayList<>(1);
                    arrayList.add(String.valueOf(alarmNum));
                    try {
                        res = myApplication.getDevice().callFunction("deleteAlarm", arrayList);
                        return true;
                    } catch (ParticleCloudException | IOException | ParticleDevice.FunctionDoesNotExistException e) {
                        e.printStackTrace();
                    }
                }

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();

            if (aBoolean) {
                new GetAlarms().execute();
            }
            else {
                finish();
            }
        }
    }
}

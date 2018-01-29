package com.example.iftach.doodi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;

public class NewAlarmActivity extends AppCompatActivity {

    private static final String TAG = "NEW_ALARM";
    private TimePicker timePicker;
    private Spinner spinner;
    private Progress progress;
    private ParticleDevice device;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = new Progress(this);
        name = getIntent().getStringExtra(Constants.NAME_EXTRA);
        device = getIntent().getParcelableExtra(Constants.DEVICE_EXTRA);

        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        spinner = findViewById(R.id.spinner);
        List<String> list = new ArrayList<>(120);
        for(int i=10; i < 121; i+=10) {
            list.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(5);

        Button save = findViewById(R.id.new_alarm_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                Calendar alarmCalendar = Calendar.getInstance();
                alarmCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                alarmCalendar.set(Calendar.MINUTE, timePicker.getMinute());

                if (alarmCalendar.getTimeInMillis() < now.getTimeInMillis()) {
                    Log.d(TAG, "adding 24 hours to alarm");
                    alarmCalendar.setTimeInMillis(alarmCalendar.getTimeInMillis() + (1000*60*60*24));
                }

                Alarm alarm = new Alarm(name, alarmCalendar.getTimeInMillis()/1000, (spinner.getSelectedItemPosition() + 1) * 10);
                Log.d(TAG, "set alarm: " + alarm.toString());

                new AddAlarm(device, alarm, new AddAlarm.Listener() {
                    @Override
                    public void onStart() {
                        progress.showProgress("Saving alarm..");
                    }

                    @Override
                    public void onSuccess(int res) {
                        progress.dismissProgress();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progress.dismissProgress();
                    }
                }).execute();
            }
        });
    }
}

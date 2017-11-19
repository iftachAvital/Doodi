package com.example.iftach.doodi;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;

public class NewAlarmActivity extends AppCompatActivity {

    private static final String TAG = "NEW_ALARM";
    private TimePicker timePicker;
    private Spinner spinner;
    private Button save;
    private ProgressDialog progress;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myApplication = (MyApplication) getApplication();
        progress = new ProgressDialog(this);

        timePicker = (TimePicker) findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>(120);
        for(int i=1; i < 121; i++) {
            list.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(59);

        save = (Button) findViewById(R.id.new_alarm_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SaveAlarm().execute();
            }
        });
    }

    private void setProgress(String message) {
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();
    }

    private class SaveAlarm extends AsyncTask<Void, Void, Boolean> {

        private int res;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress("Saving alarm..");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (myApplication.getDevice() != null && myApplication.getDevice().isConnected()) {
                String data = myApplication.getName() + ",";
                data += timePicker.getHour() + ",";
                data += timePicker.getMinute() + ",";
                data += (spinner.getSelectedItemPosition() + 1);

                ArrayList<String> arrayList = new ArrayList<>(1);
                arrayList.add(data);

                try {
                    res = myApplication.getDevice().callFunction("addAlarm", arrayList);
                    return true;
                } catch (ParticleCloudException | IOException | ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                progress.dismiss();
                finish();
            }
            else {
                // TODO: handle error
            }
        }
    }
}

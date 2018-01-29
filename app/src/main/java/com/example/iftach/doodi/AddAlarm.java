package com.example.iftach.doodi;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Created by iftach on 29/01/18.
 */

public class AddAlarm extends AsyncTask<Void, Void, Void> {

    private ParticleDevice device;
    private Alarm alarm;
    private Listener listener;
    private Exception exception = null;

    private int res;

    AddAlarm(ParticleDevice device, Alarm alarm, Listener listener) {
        this.device = device;
        this.alarm = alarm;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            res = device.callFunction("addAlarm", Collections.singletonList(alarm.toString()));
        } catch (ParticleCloudException | IOException | ParticleDevice.FunctionDoesNotExistException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (exception != null) {
            listener.onFailure(exception);
        }
        else {
            listener.onSuccess(res);
        }
    }

    interface Listener {
        void onStart();
        void onSuccess(int res);
        void onFailure(Exception e);
    }
}

package com.example.iftach.doodi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

/**
 * Created by iftach on 29/01/18.
 */

public class GetAlarms extends AsyncTask<Void, Void, Void> {

    private final String TAG = getClass().getSimpleName();

    private ParticleDevice device;
    private Listener listener;
    private Exception exception = null;

    private long subscriptionId;
    private String alarmsStr;

    GetAlarms(ParticleDevice device, Listener listener, long subscriptionId) {
        this.device = device;
        this.listener = listener;
        this.subscriptionId = subscriptionId;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            alarmsStr = device.getStringVariable("alarmsStr");
            if (subscriptionId == -1) {
                Log.d(TAG, "subscribe to alarm-event");
                subscriptionId = device.subscribeToEvents("alarm-event", new ParticleEventHandler() {
                    @Override
                    public void onEventError(Exception e) {
                        // TODO: handle error
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onEvent(String eventName, final ParticleEvent particleEvent) {
                        listener.onEvent(particleEvent);
                    }
                });
            }
        } catch (ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
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
            listener.onSuccess(alarmsStr, subscriptionId);
        }
    }

    interface Listener {
        void onEvent(ParticleEvent particleEvent);
        void onStart();
        void onSuccess(String alarmsStr, long subscriptionId);
        void onFailure(Exception e);
    }
}

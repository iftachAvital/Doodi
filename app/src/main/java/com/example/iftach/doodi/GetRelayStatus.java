package com.example.iftach.doodi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

/**
 * Created by iftach on 28/01/18.
 */

public class GetRelayStatus extends AsyncTask<Void, Void, Void> {

    private final String TAG = getClass().getSimpleName();

    private ParticleDevice device;
    private Listener listener;
    private Exception exception = null;
    private int relayStatus;
    private long subscriptionId;

    GetRelayStatus(ParticleDevice device, Listener listener) {
        this.device = device;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            relayStatus = device.getIntVariable("relayStatus");
            subscriptionId = device.subscribeToEvents("relay-event", new ParticleEventHandler() {
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
        } catch (ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
            e.printStackTrace();
            exception = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (exception != null) {
            listener.onFailure(exception);
        }
        else {
            listener.onSuccess(relayStatus, subscriptionId);
        }
    }

    interface Listener {
        void onEvent(ParticleEvent particleEvent);
        void onStart();
        void onSuccess(int relayStatus, long subscriptionId);
        void onFailure(Exception e);
    }
}

package com.example.iftach.doodi;

import android.os.AsyncTask;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by iftach on 20/01/18.
 */

public class GetDevice extends AsyncTask<Void, Void, ParticleDevice> {
    private final String TAG = getClass().getSimpleName();

    private String deviceId;

    private Listener listener = null;
    private Exception exception = null;

    GetDevice(String deviceId, Listener listener) {
        this.deviceId = deviceId;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected ParticleDevice doInBackground(Void... voids) {
        try {
            return ParticleCloudSDK.getCloud().getDevice(deviceId);
        } catch (ParticleCloudException e) {
            exception = e;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ParticleDevice particleDevice) {
        if (particleDevice != null) {
            listener.onSuccess(particleDevice);
        }
        else if (exception != null) {
            listener.onFailure(exception);
        }
        super.onPostExecute(particleDevice);
    }

    interface Listener {
        void onStart();
        void onSuccess(ParticleDevice device);
        void onFailure(Exception e);
    }
}

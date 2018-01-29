package com.example.iftach.doodi;

import android.os.AsyncTask;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;

/**
 * Created by iftach on 21/01/18.
 */

public class SignInParticle extends AsyncTask<Void, Void, Boolean> {

    private String username;
    private String password;
    private Listener listener = null;
    private Exception exception = null;

    SignInParticle(String username, String password, Listener listener) {
        this.username = username;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            ParticleCloudSDK.getCloud().logIn(username, password);
            return true;
        } catch (ParticleCloudException e) {
            exception = e;
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            listener.onSuccess();
        }
        else if (exception != null) {
            listener.onFailure(exception);
        }
    }

    interface Listener {
        void onStart();
        void onSuccess();
        void onFailure(Exception e);
    }
}

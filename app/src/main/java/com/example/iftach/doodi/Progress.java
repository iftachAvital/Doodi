package com.example.iftach.doodi;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by iftach on 20/11/17.
 */

class Progress {
    private ProgressDialog progress;

    Progress(Context context) {
        progress = new ProgressDialog(context);
    }

    void showProgress(String message) {
        progress.setCancelable(false);
        progress.setMessage(message);
        progress.show();
    }

    void dismissProgress() {
        progress.dismiss();
    }
}

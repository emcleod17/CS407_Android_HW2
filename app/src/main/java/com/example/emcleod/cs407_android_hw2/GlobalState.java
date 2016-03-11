package com.example.emcleod.cs407_android_hw2;

import android.app.Application;
import android.content.res.Configuration;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by emcleod on 3/10/16.
 */
public class GlobalState extends Application {
    private GoogleAccountCredential credential;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public GoogleAccountCredential getCredential() {
        return credential;
    }

    public void setCredential(GoogleAccountCredential cred) {
        credential = cred;
    }


}
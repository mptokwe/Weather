package com.mpho.instantweather;

import android.app.Application;

public class WeatherApp extends Application {

    private static WeatherApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized WeatherApp getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(InternetVerifierBroadcastReceiver.ConnectivityReceiverListener listener) {
        InternetVerifierBroadcastReceiver.connectivityReceiverListener = listener;
    }
}
package com.app.parkinglocator;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.app.parkinglocator.helper.SharedPreferenceHelper;
import com.google.android.gms.ads.MobileAds;
import com.raizlabs.android.dbflow.config.FlowManager;

public class MyApp extends MultiDexApplication {

    static LocalBroadcastManager localBroadcastManager;

    private static MyApp sInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
        SharedPreferenceHelper.initialize(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        MobileAds.initialize(this, getString(R.string.admob_id));
    }

    public static LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public MyApp() {
        sInstance = this;
    }

    public static MyApp get() {
        return sInstance;
    }
}

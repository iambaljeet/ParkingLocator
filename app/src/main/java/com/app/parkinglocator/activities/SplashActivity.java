package com.app.parkinglocator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.app.parkinglocator.R;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.helper.SharedPreferenceHelper;
import com.app.parkinglocator.utility.AppUtils;

public class SplashActivity extends AppCompatActivity {

    boolean move_to_main = false;
    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mActivity = this;

        if (AppUtils.checkGPSEnabled(mActivity) && AppUtils.checkIsConnectedToInternet(mActivity)
                && AppUtils.checkLocationPermission(mActivity)) {
            move_to_main = true;
        }

        if (!SharedPreferenceHelper.contains(AppConstants.SHOW_NOTIFICATION)) {
            SharedPreferenceHelper.saveBoolean(AppConstants.SHOW_NOTIFICATION, true);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (move_to_main) {
                    startDrawerActivity();
                } else {
                    startStartActivity();
                }
            }
        }, 1000);
    }

    private void startStartActivity() {
        Intent startIntent = new Intent(mActivity, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void startDrawerActivity() {
        Intent drawerIntent = new Intent(mActivity, DrawerActivity.class);
        startActivity(drawerIntent);
        finish();
    }
}

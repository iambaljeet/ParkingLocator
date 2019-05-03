package com.app.parkinglocator.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.app.parkinglocator.R;
import com.app.parkinglocator.utility.AppUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = StartActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 125;
    private ImageView mImageLocation;
    private AppCompatButton mPermissionsAllow;
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            mPermissionsAllow.setEnabled(false);
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        }
    };
    private AppCompatButton mOnGpsButtonTurn;
    private AppCompatButton mButtonDone;
    private AppCompatButton mOnInternetButtonTurn;
    private AppCompatActivity mActivity;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (AppUtils.checkGPSEnabled(mActivity) && AppUtils.checkIsConnectedToInternet(mActivity)
//                && AppUtils.checkLocationPermission(mActivity)) {
//            locationUtils.getLocation(LocationUtils.Method.NETWORK_THEN_GPS, new LocationUtils.Listener() {
//                @Override
//                public void onLocationFound(Location location) {
//                    Log.e(TAG, "location: " + location.toString());
//                    Intent drawerIntent = new Intent(mActivity, DrawerActivity.class);
//                    drawerIntent.putExtra(AppConstants.LATITUDE, location.getLatitude());
//                    drawerIntent.putExtra(AppConstants.LONGITUDE, location.getLongitude());
//                    startActivity(drawerIntent);
//                    finish();
//                }
//
//                @Override
//                public void onLocationNotFound() {
//                    Log.e(TAG, "Location not found.");
//                }
//            });
//        }

        if (AppUtils.checkGPSEnabled(mActivity)) {
            mOnGpsButtonTurn.setEnabled(false);
        } else {
            mOnGpsButtonTurn.setEnabled(true);
        }
        if (AppUtils.checkIsConnectedToInternet(mActivity)) {
            mOnInternetButtonTurn.setEnabled(false);
        } else {
            mOnInternetButtonTurn.setEnabled(true);
        }
        if (AppUtils.checkLocationPermission(mActivity)) {
            mPermissionsAllow.setEnabled(false);
        } else {
            mPermissionsAllow.setEnabled(true);
        }
    }

    private void initView() {
        mActivity = this;

        googleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        mImageLocation = findViewById(R.id.location_image);
        mPermissionsAllow = findViewById(R.id.allow_permissions);
        mPermissionsAllow.setOnClickListener(this);
        mOnGpsButtonTurn = findViewById(R.id.turn_on_gps_button);
        mOnGpsButtonTurn.setOnClickListener(this);
        mButtonDone = findViewById(R.id.done_button);
        mButtonDone.setOnClickListener(this);
        mOnInternetButtonTurn = (AppCompatButton) findViewById(R.id.turn_on_internet_button);
        mOnInternetButtonTurn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.allow_permissions:
                TedPermission.with(this)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .check();
                break;
            case R.id.turn_on_gps_button:
                AppUtils.turnOnLocation(googleApiClient, mActivity);
//                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;
            case R.id.done_button:
                if (validateAll()) {
                    Intent drawerIntent = new Intent(mActivity, DrawerActivity.class);
                    startActivity(drawerIntent);
                    finish();
                }
                break;
            case R.id.turn_on_internet_button:
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            default:
                break;
        }
    }

    private boolean validateAll() {
        if (mPermissionsAllow.isEnabled()) {
            AppUtils.showAlert(mActivity, getString(R.string.error_title), getString(R.string.enable_all_options_message));
            return false;
        }
        if (mOnInternetButtonTurn.isEnabled()) {
            AppUtils.showAlert(mActivity, getString(R.string.error_title), getString(R.string.enable_all_options_message));
            return false;
        }
        if (mOnGpsButtonTurn.isEnabled()) {
            AppUtils.showAlert(mActivity, getString(R.string.error_title), getString(R.string.enable_all_options_message));
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspendedd: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult);
    }
}
package com.app.parkinglocator.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.model.LocationTable;
import com.app.parkinglocator.model.LocationTable_Table;
import com.app.parkinglocator.utility.AppUtils;
import com.app.parkinglocator.views.ButtonRegular;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.UUID;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private static final String TAG = LocationActivity.class.getSimpleName();
    Marker lastOpenned = null;
    private SupportMapFragment mapFragment;
    private FloatingActionButton mBackFab;
    private FloatingActionButton mOptionsFab;
    private RelativeLayout mOptionsContainerTop;
    private FrameLayout mMainContainer;
    private GoogleMap mMap;
    private ButtonRegular mNavigateButton;
    private FloatingActionButton mShareButton;
    private UUID location_id;
    private AppCompatActivity mActivity;
    private double latitude;
    private double longitude;
    private double my_latitude;
    private double my_longitude;
    private String location_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initView();
    }

    private void initView() {
        mActivity = this;

        String locationId;

        locationId = getIntent().getStringExtra(AppConstants.LOCATION_ID);

        location_id = UUID.fromString(locationId);

        Log.e(TAG, "locationId: " + locationId);
        Log.e(TAG, "location_id: " + location_id);

        mMainContainer = findViewById(R.id.main_container);
        mBackFab = findViewById(R.id.fab_back);
        mBackFab.setOnClickListener(this);
        mOptionsFab = findViewById(R.id.fab_options);
        mOptionsFab.setOnClickListener(this);
        mOptionsContainerTop = findViewById(R.id.top_options_container);
        mNavigateButton = findViewById(R.id.navigate_button);
        mShareButton = findViewById(R.id.share_button);
        mNavigateButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);

        int status_bar_height = AppUtils.getStatusBarHeight(mActivity);

        ViewGroup.MarginLayoutParams userlayoutParams =
                (ViewGroup.MarginLayoutParams) mOptionsContainerTop.getLayoutParams();
        userlayoutParams.setMargins(0, status_bar_height, 0, 0);
        mOptionsContainerTop.requestLayout();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_back:
                finish();
                break;
            case R.id.fab_options:
                break;
            case R.id.navigate_button:
                if (AppUtils.isGoogleMapsInstalled(mActivity)) {
                    AppUtils.launchGoogleMapsNavitaion(mActivity, latitude, longitude);
                }
                break;
            case R.id.share_button:
                String message = getString(R.string.share_prefix_message);
                String geoUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + latitude + " (" + location_name + ")";
                String share_message = message + " " + geoUri;
                AppUtils.launchUrlShareIntent(mActivity, share_message);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapFragment.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Check if there is an open info window
                if (lastOpenned != null) {
                    // Close the info window
                    lastOpenned.hideInfoWindow();

                    // Is the marker the same marker that was already open
                    if (lastOpenned.equals(marker)) {
                        // Nullify the lastOpenned object
                        lastOpenned = null;
                        // Return so that the info window isn't openned again
                        return true;
                    }
                }

                // Open the info window for the marker
                marker.showInfoWindow();
                // Re-assign the last openned such that we can close it later
                lastOpenned = marker;

                // Event was handled by our code do not launch default behaviour.
                return true;
            }
        });

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));


            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        getLocationListFromDatabase(location_id);
    }

    private void getLocationListFromDatabase(UUID location_id) {
        SQLite.select()
                .from(LocationTable.class)
                .where(LocationTable_Table.locationId.eq(location_id))
                .async().querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<LocationTable>() {
            @Override
            public void onSingleQueryResult(QueryTransaction transaction, @Nullable LocationTable locationTable) {
                Log.e(TAG, "locationTable: " + locationTable);

                latitude = locationTable.latitude;
                longitude = locationTable.longitude;

                location_name = locationTable.locationTitle;

                if (mMap != null && locationTable != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(locationTable.latitude, locationTable.longitude)).zoom(15f).build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                    mMap.moveCamera(cameraUpdate);

                    Marker newmarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(locationTable.latitude, locationTable.longitude)).title(location_name));

                    newmarker.showInfoWindow();

                    Log.e(TAG, "location: " + locationTable);

                    int container_height = mMainContainer.getHeight() / 6;
                    mMap.animateCamera(CameraUpdateFactory.scrollBy(0, container_height));

                    if (AppUtils.checkIsConnectedToInternet(mActivity) &&
                            AppUtils.checkGPSEnabled(mActivity)) {
//                                getCurrentLocation();
                    }
                }
            }
        }).execute();
    }
}
package com.app.parkinglocator.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.callbacks.AlertSaveLocationCallback;
import com.app.parkinglocator.callbacks.DelayedCallback;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.model.LocationTable;
import com.app.parkinglocator.model.LocationTable_Table;
import com.app.parkinglocator.utility.AppUtils;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
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
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class DrawerActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, View.OnTouchListener,
        NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = DrawerActivity.class.getSimpleName();

    private double TENSION = 800;
    private double DAMPER = 20; //friction
    Marker lastOpenned = null;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private double latitude, longitude;
    private RelativeLayout mLocationMy;
    private RelativeLayout mTopOptionsContainer;
    private FloatingActionButton fab_menu;
    private FloatingActionButton fab_options;
    private DrawerLayout drawer;
    private AppCompatActivity mActivity;
    private CardView park_car_container;
    private CardView share_location_container;
    private CardView sos_container;
    private NavigationView nav_view;
    private Marker mUserMarker;
    private SpringSystem mSpringSystem;
    private Spring mSpring_park_car;
    private Spring mSpring_share_location;
    private Spring mSpring_sos;

    private List<String> stringInventoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initView();
    }

    private void initView() {
        mActivity = this;

        loadIAPProducstsInList();

        mLocationMy = findViewById(R.id.my_location);
        mLocationMy.setOnClickListener(this);

        drawer = findViewById(R.id.drawer_layout);

        fab_menu = findViewById(R.id.fab_menu);
        fab_options = findViewById(R.id.fab_options);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        latitude = getIntent().getLongExtra(AppConstants.LATITUDE, 0);
        longitude = getIntent().getLongExtra(AppConstants.LONGITUDE, 0);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mTopOptionsContainer = findViewById(R.id.top_options_container);

        int status_bar_height = AppUtils.getStatusBarHeight(DrawerActivity.this);

        ViewGroup.MarginLayoutParams userlayoutParams =
                (ViewGroup.MarginLayoutParams) mTopOptionsContainer.getLayoutParams();
        userlayoutParams.setMargins(0, status_bar_height, 0, 0);
        mTopOptionsContainer.requestLayout();

        park_car_container = findViewById(R.id.park_car_container);
        share_location_container = findViewById(R.id.share_location_container);
        sos_container = findViewById(R.id.sos_container);
        park_car_container.setOnTouchListener(this);
        share_location_container.setOnTouchListener(this);
        sos_container.setOnTouchListener(this);

        fab_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDrawer();
            }
        });

        fab_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.showOptionsPopup(mActivity, fab_options);
            }
        });

        mSpringSystem = SpringSystem.create();

        mSpring_park_car = mSpringSystem.createSpring();
        mSpring_park_car.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = 1f - (value * 0.5f);
                park_car_container.setScaleX(scale);
                park_car_container.setScaleY(scale);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

        mSpring_share_location = mSpringSystem.createSpring();
        mSpring_share_location.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = 1f - (value * 0.5f);
                share_location_container.setScaleX(scale);
                share_location_container.setScaleY(scale);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

        mSpring_sos = mSpringSystem.createSpring();
        mSpring_sos.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = 1f - (value * 0.5f);
                sos_container.setScaleX(scale);
                sos_container.setScaleY(scale);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        mSpring_park_car.setSpringConfig(config);
        mSpring_share_location.setSpringConfig(config);
        mSpring_sos.setSpringConfig(config);
    }

    private void loadIAPProducstsInList() {
        stringInventoryList = new ArrayList<>();

        stringInventoryList.add("sku_20");
        stringInventoryList.add("sku_50");
        stringInventoryList.add("sku_100");
        stringInventoryList.add("sku_150");
        stringInventoryList.add("sku_200");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.moveCamera(cameraUpdate);

                Log.e(TAG, "location: " + latLng.toString());

                if (mUserMarker == null) {
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Your Location"));

                    int container_width = AppUtils.getScreenWidth(mActivity) / 5;
                    int container_height = drawer.getHeight() / 5;
                    mMap.animateCamera(CameraUpdateFactory.scrollBy(-container_width, container_height));
                } else {

                    mUserMarker.remove();
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Your Location"));

                    int container_width = AppUtils.getScreenWidth(mActivity) / 5;
                    int container_height = drawer.getHeight() / 5;
                    mMap.animateCamera(CameraUpdateFactory.scrollBy(-container_width, container_height));
                }
            }
        });

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

        AppUtils.makeDelay(500, new DelayedCallback() {
            @Override
            public void onDelayCompleted() {
                openBottomSheet();
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
    }

    private void openBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.get_location_bottomsheet, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final LinearLayout close_bottomsheet_container = view.findViewById(R.id.close_bottomsheet_container);
        close_bottomsheet_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        SmartLocation.with(mActivity).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        mMap.moveCamera(cameraUpdate);

                        Log.e(TAG, "location: " + location.toString());

                        if (mUserMarker == null) {
                            mUserMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude)).title("Your Location"));

                            int container_width = AppUtils.getScreenWidth(mActivity) / 5;
                            int container_height = drawer.getHeight() / 5;
                            mMap.animateCamera(CameraUpdateFactory.scrollBy(-container_width, container_height));
                        } else {

                            mUserMarker.remove();
                            mUserMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude)).title("Your Location"));

                            int container_width = AppUtils.getScreenWidth(mActivity) / 5;
                            int container_height = drawer.getHeight() / 5;
                            mMap.animateCamera(CameraUpdateFactory.scrollBy(-container_width, container_height));
                        }

                        dialog.dismiss();
                    }
                });
    }

    private void toggleDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location:
                openBottomSheet();
                break;
            default:
                break;
        }
    }

    private void startLocationListActivity() {
        Intent locationListIntent = new Intent(mActivity, LocationListActivity.class);
        startActivity(locationListIntent);
    }

    private void openSaveLocationDialog() {
        AppUtils.saveLocationDialog(mActivity, new AlertSaveLocationCallback() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onSave(Bundle bundle) {
                String name = bundle.getString(AppConstants.LOCATION_TITLE);
                long currentTimeMillis = bundle.getLong(AppConstants.LOCATION_TIME);

                saveLocationToDatabase(name, currentTimeMillis);
            }
        });
    }

    private void saveLocationToDatabase(String name, long currentTimeMillis) {
        LocationTable locationTable = new LocationTable();

        locationTable.latitude = latitude;
        locationTable.longitude = longitude;
        locationTable.locationId = UUID.randomUUID();
        locationTable.locationTitle = name;
        locationTable.timestamp = currentTimeMillis;

        ModelAdapter<LocationTable> adapter = FlowManager.getModelAdapter(LocationTable.class);
        adapter.insert(locationTable);
    }

    private void getLocationListFromDatabase(String name, long currentTimeMillis) {
        LocationTable locationTable = new LocationTable();

        locationTable.latitude = latitude;
        locationTable.longitude = longitude;
        locationTable.locationId = UUID.randomUUID();
        locationTable.locationTitle = name;
        locationTable.timestamp = currentTimeMillis;

        ModelAdapter<LocationTable> adapter = FlowManager.getModelAdapter(LocationTable.class);
        adapter.insert(locationTable);
    }

    private void deleteLocationFromDatabase(UUID uuid) {
        LocationTable locationTable = new LocationTable();

        SQLite.delete(LocationTable.class)
                .where(LocationTable_Table.locationId.eq(uuid))
                .async()
                .execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapFragment != null) {
            mapFragment.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapFragment != null) {
            mapFragment.onResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapFragment != null) {
            mapFragment.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapFragment != null) {
            mapFragment.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapFragment != null) {
            mapFragment.onDestroy();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.park_car_container:
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mSpring_park_car.setEndValue(1f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSpring_park_car.setEndValue(0f);
                        AppUtils.makeDelay(200, new DelayedCallback() {
                            @Override
                            public void onDelayCompleted() {
                                if (latitude > 0 || longitude > 0) {
                                    openSaveLocationDialog();
                                } else {
                                    AppUtils.showAlert(mActivity, "Location not found.", "Your location not available." +
                                            " Please enable your location or select on Map by tapping on it.");
                                }
                            }
                        });
                        return true;
                }
                break;
            case R.id.share_location_container:
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mSpring_share_location.setEndValue(1f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSpring_share_location.setEndValue(0f);
                        AppUtils.makeDelay(200, new DelayedCallback() {
                            @Override
                            public void onDelayCompleted() {
                                startLocationListActivity();
                            }
                        });
                        return true;
                }
                break;
            case R.id.sos_container:
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mSpring_sos.setEndValue(1f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSpring_sos.setEndValue(0f);
                        AppUtils.makeDelay(200, new DelayedCallback() {
                            @Override
                            public void onDelayCompleted() {
                                AppUtils.callEmergencyNumber(mActivity);
//                                sendEmergencySOS();
                            }
                        });
                        return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        closeDrawer();
        switch (menuItem.getItemId()) {
            case R.id.item_info:
                startActivity(InfoActivity.class);
                break;
            case R.id.item_share:
                AppUtils.shareApp(mActivity);
                break;
            case R.id.item_rate:
                AppUtils.rateUs(mActivity);
                break;
            case R.id.item_settings:
                startActivity(SettingsActivity.class);
                break;
        }
        return false;
    }

    private void startActivity(Class mClass) {
        Intent activityIntent = new Intent(mActivity, mClass);
        startActivity(activityIntent);
    }

    private void closeDrawer() {
        drawer.closeDrawer(Gravity.START);
    }

}
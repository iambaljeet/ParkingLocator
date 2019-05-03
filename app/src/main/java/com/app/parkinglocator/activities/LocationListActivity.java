package com.app.parkinglocator.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.adapters.LocationListAdapter;
import com.app.parkinglocator.model.LocationTable;
import com.app.parkinglocator.model.LocationTable_Table;
import com.app.parkinglocator.utility.AppUtils;
import com.app.parkinglocator.views.MyDividerItemDecoration;
import com.app.parkinglocator.views.RippleBackground;
import com.app.parkinglocator.views.TextViewBold;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocationListActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = LocationListActivity.class.getSimpleName();
    private FloatingActionButton mBackFab;
    private FloatingActionButton mOptionsFab;
    private RelativeLayout mOptionsContainerTop;
    private RecyclerView mLocationsRecyclerViewSaved;
    private TextViewBold mDataTextNo;
    private LinearLayout mDataContainerNo;
    private RippleBackground mPulsatorNoData;

    private AppCompatActivity mActivity;

    private List<LocationTable> locationTableArrayList;
    private LocationListAdapter locationListAdapter;

    private int layout_height;
    private int status_bar_height;
    private int final_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        initView();
    }

    private void initView() {
        mActivity = this;

        locationTableArrayList = new ArrayList<>();

        mOptionsContainerTop = findViewById(R.id.top_options_container);

        status_bar_height = AppUtils.getStatusBarHeight(mActivity);

        ViewGroup.MarginLayoutParams userlayoutParams =
                (ViewGroup.MarginLayoutParams) mOptionsContainerTop.getLayoutParams();
        userlayoutParams.setMargins(0, status_bar_height, 0, 0);
        mOptionsContainerTop.requestLayout();

        mBackFab = findViewById(R.id.fab_back);
        mBackFab.setOnClickListener(this);
        mOptionsFab = findViewById(R.id.fab_options);
        mOptionsFab.setOnClickListener(this);
        mOptionsContainerTop = findViewById(R.id.top_options_container);
        mLocationsRecyclerViewSaved = findViewById(R.id.saved_locations_recyclerView);
        mDataTextNo = findViewById(R.id.no_data_text);
        mDataContainerNo = findViewById(R.id.no_data_container);
        mPulsatorNoData = findViewById(R.id.pulsator_no_data);
        mPulsatorNoData.setOnClickListener(this);

        ViewTreeObserver viewTreeObserver = mOptionsContainerTop.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout_height = mOptionsContainerTop.getMeasuredHeight();
                mOptionsContainerTop.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final_height = status_bar_height + layout_height;

                AppUtils.printErrorLog(TAG, "layout_height: " + layout_height);

                AppUtils.printErrorLog(TAG, "final_height: " + final_height);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
                locationListAdapter = new LocationListAdapter(mActivity, locationTableArrayList);
                mLocationsRecyclerViewSaved.setLayoutManager(linearLayoutManager);
                mLocationsRecyclerViewSaved.addItemDecoration(new MyDividerItemDecoration(mActivity,
                        LinearLayoutManager.VERTICAL, 5, 5,
                        final_height));

                mLocationsRecyclerViewSaved.setAdapter(locationListAdapter);

                getLocationListFromDatabase();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_back:
                finish();
                break;
            case R.id.fab_options:
                // TODO 18/08/04
                break;
            case R.id.pulsator_no_data:
                finish();
                break;
            default:
                break;
        }
    }

    private void getLocationListFromDatabase() {
        SQLite.select()
                .from(LocationTable.class)
                .async()
                .queryResultCallback(new QueryTransaction.QueryResultCallback<LocationTable>() {
                    @Override
                    public void onQueryResult(@NonNull QueryTransaction<LocationTable> transaction, @NonNull CursorResult<LocationTable> tResult) {
                        locationTableArrayList = tResult.toList();
                        AppUtils.printErrorLog(TAG, "List Size: " + locationTableArrayList.size());

                        if (locationTableArrayList.size() > 0) {
                            setData();
                        } else {
                            noData();
                        }
                    }
                }).execute();
    }

    public void deleteLocationListFromDatabase(UUID locationId, final int position) {
        SQLite.delete()
                .from(LocationTable.class)
                .where(LocationTable_Table.locationId.eq(locationId))
                .async()
                .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<LocationTable>() {
                    @Override
                    public void onSingleQueryResult(QueryTransaction transaction, @Nullable LocationTable locationTable) {
                        locationListAdapter.locationDataRemoved(position);
                    }
                }).execute();
    }

    public void noData() {
        mLocationsRecyclerViewSaved.setVisibility(View.GONE);
        mDataContainerNo.setVisibility(View.VISIBLE);

        mPulsatorNoData.startRippleAnimation();
    }

    public void setData() {
        mLocationsRecyclerViewSaved.setVisibility(View.VISIBLE);
        mDataContainerNo.setVisibility(View.GONE);
        locationListAdapter.newDataInserted(locationTableArrayList);
    }
}

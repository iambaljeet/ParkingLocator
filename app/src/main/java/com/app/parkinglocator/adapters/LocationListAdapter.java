package com.app.parkinglocator.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.activities.LocationActivity;
import com.app.parkinglocator.activities.LocationListActivity;
import com.app.parkinglocator.callbacks.LocationListOptionCallback;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.model.LocationTable;
import com.app.parkinglocator.utility.AppUtils;
import com.app.parkinglocator.views.TextViewBold;
import com.app.parkinglocator.views.TextViewRegular;
import com.google.android.gms.maps.MapView;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.MyViewHolder> {
    protected HashSet<MapView> mMapViews = new HashSet<>();
    private Context mContext;
    private List<LocationTable> locationTableArrayList;

    public LocationListAdapter(Context mContext, List<LocationTable> locationTableArrayList) {
        this.mContext = mContext;
        this.locationTableArrayList = locationTableArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_locations, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final LocationTable locationTable = locationTableArrayList.get(position);
        final MyViewHolder myViewHolder = holder;

        String formattedDate = AppUtils.getFormattedDateFromMillies(locationTable.timestamp);

        myViewHolder.location_title.setText(locationTable.locationTitle);
        myViewHolder.location_date.setText(formattedDate);
        myViewHolder.main_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationActivity(locationTable.locationId);
            }
        });
        myViewHolder.more_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.showLocationListOptionsPopup(mContext, myViewHolder.more_options, locationTable.locationId
                        , new LocationListOptionCallback() {
                            @Override
                            public void onDeleteOptionSelected(UUID locationId) {
                                if (mContext instanceof LocationListActivity) {
                                    ((LocationListActivity) mContext).deleteLocationListFromDatabase(locationTable.locationId,
                                            myViewHolder.getAdapterPosition());
                                }
                            }
                        });
            }
        });
    }

    private void startLocationActivity(UUID locationId) {
        Intent locationDrawer = new Intent(mContext, LocationActivity.class);
        locationDrawer.putExtra(AppConstants.LOCATION_ID, String.valueOf(locationId));
        mContext.startActivity(locationDrawer);
    }

    @Override
    public int getItemCount() {
        return locationTableArrayList == null ? 0 : locationTableArrayList.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    public void newDataInserted(List<LocationTable> locationTableArrayList) {
        this.locationTableArrayList = locationTableArrayList;
        notifyDataSetChanged();
    }

    public void locationDataRemoved(int position) {
        locationTableArrayList.remove(position);
        notifyItemRemoved(position);

        if (locationTableArrayList != null && locationTableArrayList.size() <= 0) {
            if (mContext instanceof LocationListActivity) {
                ((LocationListActivity) mContext).noData();
            }
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextViewBold location_title;
        private TextViewRegular location_date;
        private LinearLayout main_container;
        private ImageView more_options;

        public MyViewHolder(View itemView) {
            super(itemView);

            main_container = itemView.findViewById(R.id.main_container);
            location_title = itemView.findViewById(R.id.location_title);
            location_date = itemView.findViewById(R.id.location_date);
            more_options = itemView.findViewById(R.id.more_options);
        }
    }

}

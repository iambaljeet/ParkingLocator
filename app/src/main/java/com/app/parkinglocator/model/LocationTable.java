package com.app.parkinglocator.model;

import com.app.parkinglocator.database.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.UUID;

@Table(database = AppDatabase.class)

public class LocationTable {

    @PrimaryKey
    public UUID locationId;

    @Column
    public String locationTitle;

    @Column
    public double latitude;

    @Column
    public double longitude;

    @Column
    public long timestamp;
}

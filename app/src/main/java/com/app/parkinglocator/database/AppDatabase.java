package com.app.parkinglocator.database;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)

public class AppDatabase {
    public static final String NAME = "LocationDatabase";

    public static final int VERSION = 1;

}

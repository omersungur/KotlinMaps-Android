package com.omersungur.kotlinmaps_android.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omersungur.kotlinmaps_android.model.Place

@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao() : PlaceDAO
}
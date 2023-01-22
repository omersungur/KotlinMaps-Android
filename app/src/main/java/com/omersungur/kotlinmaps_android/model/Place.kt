package com.omersungur.kotlinmaps_android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Place(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,
) : java.io.Serializable{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
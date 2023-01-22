package com.omersungur.kotlinmaps_android.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.omersungur.kotlinmaps_android.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDAO {

    /*@Query("SELECT * FROM Place WHERE id = :id") id'ye göre veri çekmek için
    fun getPlace(id: String) : Place*/

    @Query("SELECT * FROM Place")
    fun getAll() : Flowable<List<Place>>

    @Insert
    fun insert(place : Place) : Completable // Completable > bir şey döndürmeyecek fonksiyonlar için kullanıyoruz. Asenkron işlem yapmamıza olanak tanır.

    @Delete
    fun delete(place : Place) : Completable
}
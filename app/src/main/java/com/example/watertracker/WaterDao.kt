package com.example.watertracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WaterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: WaterRecord)

    @Query("SELECT * FROM water_table WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date ASC")
    fun getRecordsByDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<WaterRecord>>

    @Query("SELECT SUM(amount) FROM water_table WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTotalWaterByDateRange(startOfDay: Long, endOfDay: Long): LiveData<Int>

    @Query("DELETE FROM water_table WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
}

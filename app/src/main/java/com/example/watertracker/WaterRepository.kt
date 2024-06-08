package com.example.watertracker

import androidx.lifecycle.LiveData

class WaterRepository(private val waterDao: WaterDao) {

    fun getRecordsByDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<WaterRecord>> {
        return waterDao.getRecordsByDateRange(startOfDay, endOfDay)
    }

    fun getTotalWaterByDateRange(startOfDay: Long, endOfDay: Long): LiveData<Int> {
        return waterDao.getTotalWaterByDateRange(startOfDay, endOfDay)
    }

    suspend fun deleteRecordById(id: Int) {
        waterDao.deleteRecordById(id)
    }

    suspend fun insert(record: WaterRecord) {
        waterDao.insert(record)
    }
}

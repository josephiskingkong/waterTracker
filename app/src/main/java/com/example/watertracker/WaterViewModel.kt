package com.example.watertracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WaterRepository

    init {
        val waterDao = WaterDatabase.getDatabase(application).waterDao()
        repository = WaterRepository(waterDao)
    }

    fun getRecordsForDate(date: Long): LiveData<List<WaterRecord>> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        return repository.getRecordsByDateRange(startOfDay, endOfDay)
    }

    fun deleteRecord(id: Int) = viewModelScope.launch {
        repository.deleteRecordById(id)
    }

    fun insertRecord(record: WaterRecord) = viewModelScope.launch {
        repository.insert(record)
    }

    fun getTotalWaterForDate(date: Long): LiveData<Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        return repository.getTotalWaterByDateRange(startOfDay, endOfDay)
    }

    fun getRecordsByDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<WaterRecord>> {
        return repository.getRecordsByDateRange(startOfDay, endOfDay)
    }

    fun updateRecord(record: WaterRecord) = viewModelScope.launch {
        repository.update(record)
    }
}


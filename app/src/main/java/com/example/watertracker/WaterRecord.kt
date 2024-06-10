package com.example.watertracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_table")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var amount: Int,
    val date: Long
)

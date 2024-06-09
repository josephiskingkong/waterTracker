package com.example.watertracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity(), CalendarActivity.OnDateSelectedListener {

    private lateinit var waterViewModel: WaterViewModel
    private lateinit var adapter: WaterRecordAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddWater: Button
    private lateinit var btnViewStats: Button
    private lateinit var fabCalendar: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

    private val calendarRequestCode = 1
    private var selectedDate: Long = Calendar.getInstance().timeInMillis
    private var dailyGoal: Int = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnAddWater = findViewById(R.id.btnAddWater)
        btnViewStats = findViewById(R.id.btnViewStats)
        fabCalendar = findViewById(R.id.fabCalendar)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)

        val sharedPreferences = getSharedPreferences("WaterTrackerPreferences", Context.MODE_PRIVATE)
        dailyGoal = sharedPreferences.getInt("dailyGoal", 2000)

        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "dailyGoal") {
                dailyGoal = sharedPreferences.getInt("dailyGoal", 2000)
                updateProgressBar()
            }
        }

        adapter = WaterRecordAdapter { record ->
            waterViewModel.deleteRecord(record.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        waterViewModel = ViewModelProvider(this).get(WaterViewModel::class.java)
        updateProgressBar()

        waterViewModel.getRecordsForDate(selectedDate).observe(this, Observer { records ->
            adapter.submitList(records)
            updateProgressBar()
        })

        btnAddWater.setOnClickListener {
            val bottomSheetFragment = AddWaterActivity()
            val args = Bundle()
            args.putLong("selectedDate", selectedDate)
            bottomSheetFragment.arguments = args
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        btnViewStats.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        fabCalendar.setOnClickListener {
            val bottomSheetFragment = CalendarActivity.newInstance(selectedDate)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("WaterTrackerPreferences", Context.MODE_PRIVATE)
        dailyGoal = sharedPreferences.getInt("dailyGoal", 2000)
        updateProgressBar()
    }

    private fun updateProgressBar() {
        waterViewModel.getTotalWaterForDate(selectedDate).observe(this, Observer { totalWater ->
            val totalWaterNonNull = totalWater ?: 0
            val progress = if (dailyGoal != 0) (totalWaterNonNull * 100 / dailyGoal).toInt() else 0
            progressBar.progress = progress
            tvProgress.text = "Выпито: ${totalWaterNonNull} мл / $dailyGoal мл"
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == calendarRequestCode && resultCode == RESULT_OK) {
            selectedDate = data?.getLongExtra("selectedDate", selectedDate) ?: selectedDate
            waterViewModel.getRecordsForDate(selectedDate).observe(this, Observer { records ->
                adapter.submitList(records)
                updateProgressBar()
            })
        }
    }

    override fun onDateSelected(newSelectedDate: Long) {
        selectedDate = newSelectedDate
        waterViewModel.getRecordsForDate(selectedDate).observe(this, Observer { records ->
            adapter.submitList(records)
            updateProgressBar()
        })
    }
}
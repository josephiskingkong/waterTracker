package com.example.watertracker

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var waterViewModel: WaterViewModel
    private lateinit var lineChart: LineChart
    private lateinit var tvAverage: TextView
    private lateinit var tvGoal: TextView
    private var dailyGoal: Int = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        waterViewModel = ViewModelProvider(this).get(WaterViewModel::class.java)

        lineChart = findViewById(R.id.lineChart)
        tvAverage = findViewById(R.id.tvAverage)
        tvGoal = findViewById(R.id.tvGoal)

        val sharedPreferences = getSharedPreferences("WaterTrackerPreferences", Context.MODE_PRIVATE)
        dailyGoal = sharedPreferences.getInt("dailyGoal", 2000)
        tvGoal.text = "Целевая норма: $dailyGoal мл"

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startOfWeek = calendar.timeInMillis

        waterViewModel.getRecordsByDateRange(startOfWeek, Calendar.getInstance().timeInMillis).observe(this, Observer { records ->
            val entries = mutableListOf<Entry>()
            var totalWater = 0f
            var daysWithData = 0

            for (dayOffset in 0..6) {
                calendar.timeInMillis = startOfWeek + dayOffset * 24 * 60 * 60 * 1000
                val date = calendar.timeInMillis
                val waterForDay = records.filter { it.date >= date && it.date < date + 24 * 60 * 60 * 1000 }
                    .sumOf { it.amount }
                entries.add(Entry(dayOffset.toFloat(), waterForDay.toFloat()))
                if (waterForDay > 0) {
                    totalWater += waterForDay
                    daysWithData++
                }
            }

            val averageWater = if (daysWithData > 0) totalWater / daysWithData else 0f
            tvAverage.text = "Среднее количество воды: ${averageWater.toInt()} мл"

            val dataSet = LineDataSet(entries, "Выпитая вода за последние 7 дней")
            dataSet.setDrawCircles(true)
            dataSet.setDrawValues(true)
            dataSet.color = getColor(R.color.teal_700)
            dataSet.valueTextColor = getColor(R.color.black)
            dataSet.circleRadius = 5f
            dataSet.setCircleColor(getColor(R.color.teal_700))
            dataSet.lineWidth = 3f

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            lineChart.invalidate()
        })

        findViewById<Button>(R.id.btnSetGoal).setOnClickListener {
            val newGoal = findViewById<EditText>(R.id.etGoal).text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                dailyGoal = newGoal
                sharedPreferences.edit().putInt("dailyGoal", dailyGoal).apply()
                tvGoal.text = "Целевая норма: $dailyGoal мл"

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }
}

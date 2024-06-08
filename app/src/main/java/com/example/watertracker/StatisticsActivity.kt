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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
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
        val currentDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startOfWeek = calendar.timeInMillis

        waterViewModel.getRecordsByDateRange(startOfWeek, currentDate).observe(this, Observer { records ->
            val entries = mutableListOf<Entry>()
            var totalWater = 0f
            var daysWithData = 0

            val dateLabels = mutableListOf<String>()
            val dateFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())

            for (dayOffset in 0..6) {
                calendar.timeInMillis = startOfWeek + dayOffset * 24 * 60 * 60 * 1000
                val date = calendar.timeInMillis
                dateLabels.add(dateFormatter.format(Date(date + 24 * 60 * 60 * 1000)))
                val waterForDay = records.filter { it.date >= date && it.date < date + 24 * 60 * 60 * 1000 }
                    .sumOf { it.amount }
                entries.add(Entry(dayOffset.toFloat(), waterForDay.toFloat()))
                if (waterForDay > 0) {
                    totalWater += waterForDay
                    daysWithData++
                }
            }

            val averageWater = if (daysWithData > 0) totalWater / daysWithData else 0
            tvAverage.text = "Среднее количество воды: ${averageWater.toInt()} мл"

            val dataSet = LineDataSet(entries, "Выпитая вода за последние 7 дней")
            dataSet.setDrawCircles(true)
            dataSet.setDrawValues(true)
            dataSet.color = getColor(R.color.primary)
            dataSet.valueTextColor = getColor(R.color.black)
            dataSet.circleRadius = 5f
            dataSet.setCircleColor(getColor(R.color.primary))
            dataSet.lineWidth = 3f
            dataSet.valueTextSize = 8f

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            val xAxis = lineChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            xAxis.labelCount = 7

            lineChart.description.text = ""

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

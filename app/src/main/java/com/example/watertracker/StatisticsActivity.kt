package com.example.watertracker

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
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

        findViewById<Button>(R.id.btnCalculateGoal).setOnClickListener {
            showCalculateDialog()
        }
    }

    private fun showCalculateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.calculate_layout, null)
        val etWeight: EditText = dialogView.findViewById(R.id.editText_weight)
        val etActivity: EditText = dialogView.findViewById(R.id.editText_daily_activity)
        val cbMale: CheckBox = dialogView.findViewById(R.id.checkbox_male)
        val cbFemale: CheckBox = dialogView.findViewById(R.id.checkbox_female)
        val btnCalculate: Button = dialogView.findViewById(R.id.button_calculate)

        cbMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbFemale.isChecked = false
            }
        }

        cbFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbMale.isChecked = false
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Рассчитать целевую норму")
            .setView(dialogView)
            .create()

        btnCalculate.setOnClickListener {
            val weight = etWeight.text.toString().toIntOrNull()
            val activity = etActivity.text.toString().toIntOrNull()
            val sex = if (cbMale.isChecked) "male" else if (cbFemale.isChecked) "female" else null

            if (weight != null && activity != null && sex != null) {
                sendPostRequest(weight, activity, sex)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun sendPostRequest(weight: Int, activity: Int, sex: String) {
        val client = OkHttpClient()

        val json = """
        {
            "weight": $weight,
            "activity": $activity,
            "sex": "$sex"
        }
    """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:3000/getWaterIntake")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Ошибка: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val responseString = it.body?.string()
                        if (responseString != null) {
                            val jsonObject = JSONObject(responseString)
                            val waterIntakes = jsonObject.getString("waterIntake")
                            runOnUiThread {
                                findViewById<EditText>(R.id.etGoal).setText(waterIntakes)
                            }
                        }
                    }
                }
            }
        })
    }
}

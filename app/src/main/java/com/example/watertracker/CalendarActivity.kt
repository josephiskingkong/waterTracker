package com.example.watertracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private var selectedDate: Long = Calendar.getInstance().timeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)

        selectedDate = intent.getLongExtra("selectedDate", Calendar.getInstance().timeInMillis)

        calendarView.date = selectedDate

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis

            val resultIntent = Intent()
            resultIntent.putExtra("selectedDate", selectedDate)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

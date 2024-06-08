package com.example.watertracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AddWaterActivity : AppCompatActivity() {

    private lateinit var waterViewModel: WaterViewModel
    private var selectedDate: Long = Calendar.getInstance().timeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_water)

        waterViewModel = ViewModelProvider(this).get(WaterViewModel::class.java)

        selectedDate = intent.getLongExtra("selectedDate", Calendar.getInstance().timeInMillis)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val etWaterAmount = findViewById<EditText>(R.id.etWaterAmount)

        btnSave.setOnClickListener {
            val amountText = etWaterAmount.text.toString()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toInt()
                val waterRecord = WaterRecord(date = selectedDate, amount = amount)
                waterViewModel.insertRecord(waterRecord)
                finish()
            } else {
                Snackbar.make(btnSave, "Пожалуйста, введите количество воды", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

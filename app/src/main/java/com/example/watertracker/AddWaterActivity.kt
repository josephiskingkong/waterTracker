package com.example.watertracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AddWaterActivity : BottomSheetDialogFragment() {

    private lateinit var waterViewModel: WaterViewModel
    private var selectedDate: Long = Calendar.getInstance().timeInMillis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_add_water, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        waterViewModel = ViewModelProvider(this).get(WaterViewModel::class.java)

        selectedDate = arguments?.getLong("selectedDate") ?: Calendar.getInstance().timeInMillis

        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnAdd250ml = view.findViewById<Button>(R.id.button_add_250_ml)
        val btnAdd330ml = view.findViewById<Button>(R.id.button_add_330_ml)
        val btnAdd500ml = view.findViewById<Button>(R.id.button_add_500_ml)
        val etWaterAmount = view.findViewById<EditText>(R.id.etWaterAmount)

        btnAdd250ml.setOnClickListener {
            etWaterAmount.setText("250")
        }

        btnAdd330ml.setOnClickListener {
            etWaterAmount.setText("330")
        }

        btnAdd500ml.setOnClickListener {
            etWaterAmount.setText("500")
        }

        btnSave.setOnClickListener {
            val amountText = etWaterAmount.text.toString()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toInt()
                val waterRecord = WaterRecord(date = selectedDate, amount = amount)
                waterViewModel.insertRecord(waterRecord)
                dismiss()
            } else {
                Snackbar.make(btnSave, "Пожалуйста, введите количество воды", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
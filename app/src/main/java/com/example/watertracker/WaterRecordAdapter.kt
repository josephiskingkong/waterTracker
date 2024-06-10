package com.example.watertracker

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.watertracker.WaterRecord

class WaterRecordAdapter(
    private val context: Context,
    private val onDeleteClick: (WaterRecord) -> Unit,
    private val onUpdateAmount: (WaterRecord) -> Unit,
) : RecyclerView.Adapter<WaterRecordAdapter.WaterRecordViewHolder>() {

    private var records: List<WaterRecord> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterRecordViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_water_record, parent, false)
        return WaterRecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WaterRecordViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record)
    }

    override fun getItemCount() = records.size

    fun submitList(newRecords: List<WaterRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    inner class WaterRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(record: WaterRecord) {
            tvAmount.text = "${record.amount} мл"

            btnDelete.setOnClickListener {
                onDeleteClick(record)
            }

            tvAmount.setOnClickListener {
                showEditAmountDialog(record)
            }
        }

        private fun showEditAmountDialog(record: WaterRecord) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_amount, null)
            val etAmount: EditText = dialogView.findViewById(R.id.etAmount)
            etAmount.setText(record.amount.toString())

            val dialog = AlertDialog.Builder(context)
                .setTitle("Изменить количество")
                .setView(dialogView)
                .setPositiveButton("Сохранить") { _, _ ->
                    val newAmount = etAmount.text.toString().toIntOrNull()
                    if (newAmount != null && newAmount > 0) {
                        record.amount = newAmount
                        onUpdateAmount(record)
                    } else {
                        Toast.makeText(context, "Пожалуйста, введите корректное значение", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Отмена", null)
                .create()

            dialog.show()
        }
    }
}

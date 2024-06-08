package com.example.watertracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WaterRecordAdapter(private val onDeleteClick: (WaterRecord) -> Unit) :
    RecyclerView.Adapter<WaterRecordAdapter.WaterRecordViewHolder>() {

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
        }
    }
}

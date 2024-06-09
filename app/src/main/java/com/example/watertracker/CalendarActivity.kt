package com.example.watertracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class CalendarActivity : BottomSheetDialogFragment() {

    private lateinit var calendarView: CalendarView
    private var selectedDate: Long = Calendar.getInstance().timeInMillis
    private var listener: OnDateSelectedListener? = null

    interface OnDateSelectedListener {
        fun onDateSelected(newSelectedDate: Long)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDateSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnDateSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)

        selectedDate = arguments?.getLong("selectedDate") ?: Calendar.getInstance().timeInMillis

        calendarView.date = selectedDate

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis

            listener?.onDateSelected(selectedDate)
            dismiss()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        fun newInstance(selectedDate: Long): CalendarActivity {
            return CalendarActivity().apply {
                arguments = Bundle().apply {
                    putLong("selectedDate", selectedDate)
                }
            }
        }
    }
}

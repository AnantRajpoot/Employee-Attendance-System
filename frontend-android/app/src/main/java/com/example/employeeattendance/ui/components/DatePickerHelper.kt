package com.example.employeeattendance.ui.components

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun showDatePicker(
    context: Context,
    initialDate: String? = null,
    onDateSelected: (String) -> Unit
) {

    val calendar = Calendar.getInstance()

    if (!initialDate.isNullOrBlank()) {

        try {

            val formatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )

            formatter.parse(initialDate)?.let {
                calendar.time = it
            }

        } catch (_: Exception) {
        }
    }

    DatePickerDialog(

        context,

        { _, year, month, day ->

            val selected =
                Calendar.getInstance()

            selected.set(
                year,
                month,
                day
            )

            val formatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )

            onDateSelected(
                formatter.format(selected.time)
            )
        },

        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)

    ).show()
}
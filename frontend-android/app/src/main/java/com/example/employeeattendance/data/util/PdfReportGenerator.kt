package com.example.employeeattendance.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.employeeattendance.data.model.AttendanceResponse
import com.example.employeeattendance.data.model.EmployeeResponse
import com.example.employeeattendance.data.model.HrLeaveResponse
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateMonthlyReport(
        context: Context,
        monthText: String,
        employees: List<EmployeeResponse>,
        attendance: List<AttendanceResponse>,
        leaves: List<HrLeaveResponse>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 20f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 12f
            }

            val headerPaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 14f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 11f
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            var y = 50f

            // Title
            canvas.drawText("Employee Attendance & Leave Report", 40f, y, titlePaint)
            y += 20f
            canvas.drawText("Period: $monthText", 40f, y, subtitlePaint)
            y += 30f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Summary Stats
            val presentCount = attendance.count { it.status == "PRESENT" || it.status == "CHECKED IN" }
            val absentCount = attendance.count { it.status == "ABSENT" }
            val leaveCount = leaves.count { it.status == "APPROVED" }

            canvas.drawText("Summary Statistics", 40f, y, headerPaint)
            y += 20f
            canvas.drawText("• Total Employees: ${employees.size}", 50f, y, bodyPaint)
            y += 18f
            canvas.drawText("• Total Attendance Records: ${attendance.size}", 50f, y, bodyPaint)
            y += 18f
            canvas.drawText("• Total Present Records: $presentCount", 50f, y, bodyPaint)
            y += 18f
            canvas.drawText("• Total Absent Records: $absentCount", 50f, y, bodyPaint)
            y += 18f
            canvas.drawText("• Total Approved Leaves: $leaveCount", 50f, y, bodyPaint)
            y += 30f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Employee List Header
            canvas.drawText("Employee Roster", 40f, y, headerPaint)
            y += 20f

            // Table Header
            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#2563EB")
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("ID", 40f, y, tableHeaderPaint)
            canvas.drawText("Name", 90f, y, tableHeaderPaint)
            canvas.drawText("Email", 240f, y, tableHeaderPaint)
            canvas.drawText("Role", 410f, y, tableHeaderPaint)
            canvas.drawText("Department", 490f, y, tableHeaderPaint)
            y += 15f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 18f

            employees.take(25).forEach { emp ->
                canvas.drawText("#${emp.id}", 40f, y, bodyPaint)
                canvas.drawText(emp.name, 90f, y, bodyPaint)
                canvas.drawText(emp.email, 240f, y, bodyPaint)
                canvas.drawText(emp.role, 410f, y, bodyPaint)
                canvas.drawText(emp.department ?: "—", 490f, y, bodyPaint)
                y += 20f

                if (y > 780f) {
                    return@forEach
                }
            }

            pdfDocument.finishPage(page)

            // Save PDF File
            val fileName = "Attendance_Report_${monthText.replace(" ", "_")}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "Report downloaded: $fileName", Toast.LENGTH_LONG).show()

            // Open PDF Intent
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Ignore if no PDF viewer app is available
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

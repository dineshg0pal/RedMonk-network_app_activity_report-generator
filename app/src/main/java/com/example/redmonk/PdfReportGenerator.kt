package com.example.redmonk

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generate(context: Context, list: List<AppUsage>) {

        val pdf = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        var y = 40

        // ===== HEADER =====
        canvas.drawText("RedMonk – Network Privacy Forensic Report", 40f, y.toFloat(), titlePaint)
        y += 30

        val date =
            SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $date", 40f, y.toFloat(), paint)
        y += 30

        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
        y += 20

        // ===== SUMMARY =====
        val totalSent = list.sumOf { it.sent }
        val totalReceived = list.sumOf { it.received }

        canvas.drawText("Summary", 40f, y.toFloat(), titlePaint)
        y += 25
        canvas.drawText("Total Upload: ${totalSent / 1024} KB", 40f, y.toFloat(), paint)
        y += 18
        canvas.drawText("Total Download: ${totalReceived / 1024} KB", 40f, y.toFloat(), paint)
        y += 25

        // ===== TOP APPS =====
        canvas.drawText("Top Network-Consuming Apps", 40f, y.toFloat(), titlePaint)
        y += 25

        val topApps = list
            .groupBy { it.appName }
            .mapValues { it.value.sumOf { app -> app.sent + app.received } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        if (topApps.isEmpty()) {
            canvas.drawText("No significant network activity detected.", 40f, y.toFloat(), paint)
            y += 20
        } else {
            for ((name, bytes) in topApps) {
                canvas.drawText(
                    "• $name  →  ${bytes / 1024} KB",
                    40f,
                    y.toFloat(),
                    paint
                )
                y += 18
            }
        }

        y += 20

        // ===== SMART RISK ANALYSIS (NEW) =====
        canvas.drawText("Detected Anomalies & Suspicious Behaviour", 40f, y.toFloat(), titlePaint)
        y += 25

        val riskyApps = list.filter { it.riskLevel != "LOW" }

        if (riskyApps.isEmpty()) {
            canvas.drawText("No anomalies detected.", 40f, y.toFloat(), paint)
            y += 20
        } else {
            for (app in riskyApps) {
                canvas.drawText(
                    "• ${app.appName} – ${app.riskLevel}",
                    40f,
                    y.toFloat(),
                    paint
                )
                y += 16

                canvas.drawText(
                    app.riskReason,
                    60f,
                    y.toFloat(),
                    paint
                )
                y += 18
            }
        }

        y += 20

        // ===== FOOTER =====
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
        y += 20
        canvas.drawText(
            "This report is generated locally on device. No data leaves the phone.",
            40f,
            y.toFloat(),
            paint
        )

        pdf.finishPage(page)

        // ===== SAVE FILE =====
        val fileName =
            "RedMonk_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"

        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val file = File(dir, fileName)

        try {
            pdf.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdf.close()
        }
    }
}

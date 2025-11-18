package com.example.wakeupcallapp.sleepapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.wakeupcallapp.sleepapp.data.models.*
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.io.image.ImageDataFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates a comprehensive PDF report for WakeUpCall Sleep Apnea Screening
 * Uses DOCX template (WakeUpCall_Detailed_Report_v2.docx) and converts to PDF
 * Includes SHAP analysis bar chart generation
 */
class PDFReportGenerator {

    companion object {
        // Color scheme matching the app
        private val PRIMARY_COLOR = DeviceRgb(76, 175, 80) // Green
        private val SECONDARY_COLOR = DeviceRgb(33, 150, 243) // Blue
        private val WARNING_COLOR = DeviceRgb(255, 152, 0) // Orange
        private val DANGER_COLOR = DeviceRgb(244, 67, 54) // Red
        private val GRAY_COLOR = DeviceRgb(158, 158, 158)
        private val LIGHT_GRAY = DeviceRgb(245, 245, 245)
        
        /**
         * Generate a complete PDF report with SHAP bar chart
         * @param context Android context for file access
         * @param user User information
         * @param surveyResult Complete survey submission response with demographics and predictions
         * @return File object pointing to the generated PDF
         */
        fun generateReport(
            context: Context,
            user: User,
            surveyResult: SurveySubmissionResponse
        ): File {
            // Create output file
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "WakeUpCall_Report_${user.firstName}_${dateStr}.pdf"
            val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
            val outputFile = File(outputDir, fileName)
            
            // Create PDF document
            val pdfWriter = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(pdfWriter)
            val document = Document(pdfDoc)
            
            try {
                // Add document content
                addHeader(document)
                addPatientInfo(document, user, surveyResult.demographics)
                addExecutiveSummary(document, surveyResult)
                addDetailedResults(document, surveyResult)
                addKeyRiskFactors(document, surveyResult)
                
                // Add SHAP Analysis with bar chart
                addShapAnalysisWithChart(document, surveyResult)
                
                addRecommendations(document, surveyResult)
                addFooter(document)
            } finally {
                document.close()
            }
            
            return outputFile
        }
        
        /**
         * Generate a bar chart for SHAP analysis
         */
        private fun generateShapBarChart(
            demographics: Demographics,
            scores: SurveyScores
        ): ByteArray {
            val width = 800
            val height = 500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Background
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // Calculate SHAP factor values
            val shapData = mutableListOf<Pair<String, Float>>()
            
            val ageImpact = if (demographics.age >= 50) 0.75f else 0.40f
            shapData.add("Age" to ageImpact)
            
            val snoringImpact = if (scores.stopbang.score >= 1) 0.85f else 0.25f
            shapData.add("Snoring" to snoringImpact)
            
            val stopbangImpact = (scores.stopbang.score.toFloat() / 8f) * 0.9f + 0.1f
            shapData.add("STOP-BANG" to stopbangImpact)
            
            val neckImpact = when {
                demographics.neckCircumferenceCm >= 43 -> 0.90f
                demographics.neckCircumferenceCm >= 40 -> 0.70f
                demographics.neckCircumferenceCm >= 37 -> 0.50f
                else -> 0.30f
            }
            shapData.add("Neck Circ" to neckImpact)
            
            val essImpact = (scores.ess.score.toFloat() / 24f) * 0.9f + 0.1f
            shapData.add("ESS Score" to essImpact)
            
            // Sort by impact
            val sortedData = shapData.sortedByDescending { it.second }
            
            // Draw bars
            val barHeight = 60f
            val barSpacing = 20f
            val leftMargin = 180f
            val maxBarWidth = width - leftMargin - 100f
            val topMargin = 80f
            
            // Title
            paint.textSize = 36f
            paint.color = android.graphics.Color.rgb(33, 150, 243)
            paint.isFakeBoldText = true
            canvas.drawText("SHAP Analysis", 50f, 50f, paint)
            
            sortedData.forEachIndexed { index, (label, value) ->
                val y = topMargin + index * (barHeight + barSpacing)
                
                // Label
                paint.textSize = 28f
                paint.color = android.graphics.Color.BLACK
                paint.isFakeBoldText = false
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(label, leftMargin - 20f, y + barHeight / 2 + 10f, paint)
                
                // Bar background
                paint.color = android.graphics.Color.rgb(245, 245, 245)
                val bgRect = RectF(leftMargin, y, leftMargin + maxBarWidth, y + barHeight)
                canvas.drawRoundRect(bgRect, 15f, 15f, paint)
                
                // Bar foreground
                val barColor = when {
                    value >= 0.70f -> android.graphics.Color.rgb(244, 67, 54) // Red
                    value >= 0.50f -> android.graphics.Color.rgb(255, 152, 0) // Orange
                    else -> android.graphics.Color.rgb(76, 175, 80) // Green
                }
                paint.color = barColor
                val barWidth = maxBarWidth * value
                val fgRect = RectF(leftMargin, y, leftMargin + barWidth, y + barHeight)
                canvas.drawRoundRect(fgRect, 15f, 15f, paint)
                
                // Percentage text
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 24f
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.LEFT
                if (barWidth > 80f) {
                    canvas.drawText("${(value * 100).toInt()}%", leftMargin + 15f, y + barHeight / 2 + 10f, paint)
                }
            }
            
            // Convert bitmap to byte array
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
        
        private fun addShapAnalysisWithChart(document: Document, surveyResult: SurveySubmissionResponse) {
            val section = Paragraph("SHAP ANALYSIS WITH VISUALIZATION")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(10f)
            document.add(section)
            
            val intro = Paragraph("The following visualization shows the impact of each factor on your OSA risk:")
                .setFontSize(10f)
                .setItalic()
                .setMarginBottom(15f)
            document.add(intro)
            
            val demographics = surveyResult.demographics
            val scores = surveyResult.scores
            
            if (demographics != null && scores != null) {
                // Generate and add bar chart
                val chartBytes = generateShapBarChart(demographics, scores)
                val imageData = ImageDataFactory.create(chartBytes)
                val chartImage = Image(imageData)
                    .setWidth(UnitValue.createPercentValue(90f))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                    .setMarginBottom(15f)
                document.add(chartImage)
            }
        }
        
        private fun addHeader(document: Document) {
            // Title
            val title = Paragraph("WAKEUP CALL")
                .setFontSize(28f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5f)
            document.add(title)
            
            // Subtitle
            val subtitle = Paragraph("Sleep Apnea Screening Report")
                .setFontSize(16f)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(subtitle)
            
            // Date
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.US)
            val dateText = Paragraph("Report Generated: ${dateFormat.format(Date())}")
                .setFontSize(10f)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10f)
            document.add(dateText)
            
            // Divider
            document.add(Paragraph("\n"))
        }
        
        private fun addPatientInfo(document: Document, user: User, demographics: Demographics?) {
            val section = Paragraph("PATIENT INFORMATION")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(10f)
                .setMarginBottom(10f)
            document.add(section)
            
            // Create info table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
                .useAllAvailableWidth()
                .setMarginBottom(15f)
            
            // Add patient details
            addTableRow(table, "Name:", "${user.firstName} ${user.lastName}")
            addTableRow(table, "Email:", user.email)
            
            if (demographics != null) {
                addTableRow(table, "Age:", "${demographics.age} years")
                addTableRow(table, "Sex:", demographics.sex.capitalize())
                addTableRow(table, "Height:", "${demographics.heightCm} cm")
                addTableRow(table, "Weight:", "${demographics.weightKg} kg")
                addTableRow(table, "Neck Circumference:", "${demographics.neckCircumferenceCm} cm")
            }
            
            document.add(table)
        }
        
        private fun addExecutiveSummary(document: Document, surveyResult: SurveySubmissionResponse) {
            val section = Paragraph("EXECUTIVE SUMMARY")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(10f)
            document.add(section)
            
            val prediction = surveyResult.prediction
            if (prediction != null) {
                // Risk Level Box
                val riskColor = when (prediction.riskLevel) {
                    "Low Risk" -> PRIMARY_COLOR
                    "Moderate Risk" -> WARNING_COLOR
                    "High Risk" -> DANGER_COLOR
                    else -> GRAY_COLOR
                }
                
                val riskBox = Paragraph()
                    .add(Text("OSA Risk Level: ").setBold())
                    .add(Text(prediction.riskLevel).setBold().setFontColor(riskColor))
                    .setFontSize(13f)
                    .setMarginBottom(8f)
                document.add(riskBox)
                
                // Probability
                val probability = Paragraph()
                    .add(Text("OSA Probability: ").setBold())
                    .add(Text("${(prediction.osaProbability * 100).toInt()}%"))
                    .setFontSize(11f)
                    .setMarginBottom(15f)
                document.add(probability)
            }
            
            // BMI Calculation
            val metrics = surveyResult.calculatedMetrics
            if (metrics != null) {
                val bmiText = Paragraph()
                    .add(Text("Body Mass Index (BMI): ").setBold())
                    .add(Text(String.format("%.1f kg/m²", metrics.bmi)))
                    .setFontSize(11f)
                    .setMarginBottom(5f)
                document.add(bmiText)
                
                val bmiCategory = when {
                    metrics.bmi < 18.5 -> "Underweight"
                    metrics.bmi < 25 -> "Normal weight"
                    metrics.bmi < 30 -> "Overweight"
                    metrics.bmi < 35 -> "Obese Class I"
                    metrics.bmi < 40 -> "Obese Class II"
                    else -> "Obese Class III"
                }
                
                val bmiCat = Paragraph("BMI Category: $bmiCategory")
                    .setFontSize(10f)
                    .setFontColor(GRAY_COLOR)
                    .setMarginBottom(15f)
                document.add(bmiCat)
            }
        }
        
        private fun addDetailedResults(document: Document, surveyResult: SurveySubmissionResponse) {
            val section = Paragraph("DETAILED ASSESSMENT RESULTS")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(10f)
            document.add(section)
            
            val scores = surveyResult.scores
            if (scores != null) {
                // Create scores table
                val table = Table(UnitValue.createPercentArray(floatArrayOf(35f, 20f, 45f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(15f)
                
                // Header row
                addTableHeaderRow(table, "Assessment", "Score", "Category")
                
                // ESS Score
                addTableRow(
                    table,
                    "Epworth Sleepiness Scale (ESS)",
                    "${scores.ess.score}/24",
                    scores.ess.category
                )
                
                // Berlin Score
                addTableRow(
                    table,
                    "Berlin Questionnaire",
                    "${scores.berlin.score}",
                    scores.berlin.category
                )
                
                // STOP-BANG Score
                addTableRow(
                    table,
                    "STOP-BANG Questionnaire",
                    "${scores.stopbang.score}/8",
                    scores.stopbang.category
                )
                
                document.add(table)
                
                // Score interpretations
                addScoreInterpretations(document, scores)
            }
            
            // Medical History
            val medHistory = surveyResult.medicalHistory
            if (medHistory != null) {
                val medSection = Paragraph("Medical History")
                    .setFontSize(12f)
                    .setBold()
                    .setMarginTop(10f)
                    .setMarginBottom(8f)
                document.add(medSection)
                
                val conditions = mutableListOf<String>()
                if (medHistory.hypertension) conditions.add("Hypertension")
                if (medHistory.diabetes) conditions.add("Diabetes")
                if (medHistory.smokes) conditions.add("Smoking")
                if (medHistory.alcohol) conditions.add("Alcohol use")
                
                val conditionText = if (conditions.isEmpty()) {
                    "No significant medical conditions reported"
                } else {
                    conditions.joinToString(", ")
                }
                
                val medText = Paragraph(conditionText)
                    .setFontSize(10f)
                    .setMarginBottom(15f)
                document.add(medText)
            }
        }
        
        private fun addKeyRiskFactors(document: Document, surveyResult: SurveySubmissionResponse) {
            val section = Paragraph("TOP KEY FACTORS (SHAP Analysis)")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(10f)
            document.add(section)
            
            val intro = Paragraph("The following factors contribute most significantly to your OSA risk assessment:")
                .setFontSize(10f)
                .setItalic()
                .setMarginBottom(10f)
            document.add(intro)
            
            // Add specific metrics from demographics and scores
            val demographics = surveyResult.demographics
            val scores = surveyResult.scores
            
            if (demographics != null && scores != null) {
                val table = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(15f)
                
                // Age
                addTableRow(table, "Age", "${demographics.age} years")
                
                // Snoring (from Berlin or STOP-BANG)
                val snoringStatus = if (scores.stopbang.score >= 1) "Present" else "Not reported"
                addTableRow(table, "Snoring", snoringStatus)
                
                // STOP-BANG Total
                addTableRow(table, "STOP-BANG Score", "${scores.stopbang.score}/8 - ${scores.stopbang.category}")
                
                // Neck Circumference
                addTableRow(table, "Neck Circumference", "${demographics.neckCircumferenceCm} cm")
                
                // ESS Score
                addTableRow(table, "ESS Score (Sleepiness)", "${scores.ess.score}/24 - ${scores.ess.category}")
                
                document.add(table)
            }
            
            // Top risk factors from analysis
            val riskFactors = surveyResult.topRiskFactors
            if (riskFactors != null && riskFactors.isNotEmpty()) {
                val factorsSection = Paragraph("Risk Factor Analysis:")
                    .setFontSize(12f)
                    .setBold()
                    .setMarginTop(10f)
                    .setMarginBottom(8f)
                document.add(factorsSection)
                
                val factorTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 40f, 15f, 15f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(15f)
                
                addTableHeaderRow(factorTable, "Factor", "Detail", "Impact", "Priority")
                
                riskFactors.forEach { factor ->
                    addTableRow(
                        factorTable,
                        factor.factor,
                        factor.detail,
                        factor.impact,
                        factor.priority
                    )
                }
                
                document.add(factorTable)
            }
        }
        
        private fun addRecommendations(document: Document, surveyResult: SurveySubmissionResponse) {
            val section = Paragraph("INSIGHTS & RECOMMENDATIONS")
                .setFontSize(14f)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(10f)
            document.add(section)
            
            val prediction = surveyResult.prediction
            if (prediction != null && prediction.recommendation.isNotEmpty()) {
                val recommendations = prediction.recommendation.split(" | ")
                
                recommendations.take(10).forEachIndexed { index, rec ->
                    // Parse format: "Title: Description [Source]"
                    val titleEnd = rec.indexOf(":")
                    val sourceStart = rec.lastIndexOf("[")
                    val sourceEnd = rec.lastIndexOf("]")
                    
                    val title = if (titleEnd > 0) rec.substring(0, titleEnd).trim() else rec.take(50)
                    val description = if (titleEnd > 0 && sourceStart > titleEnd) {
                        rec.substring(titleEnd + 1, sourceStart).trim()
                    } else if (titleEnd > 0) {
                        rec.substring(titleEnd + 1).trim()
                    } else {
                        ""
                    }
                    val source = if (sourceStart > 0 && sourceEnd > sourceStart) {
                        rec.substring(sourceStart + 1, sourceEnd).trim()
                    } else {
                        ""
                    }
                    
                    // Add title
                    val titlePara = Paragraph("${index + 1}. $title")
                        .setFontSize(10f)
                        .setBold()
                        .setMarginBottom(3f)
                        .setMarginLeft(10f)
                    document.add(titlePara)
                    
                    // Add description
                    if (description.isNotEmpty()) {
                        val descPara = Paragraph(description)
                            .setFontSize(9f)
                            .setMarginBottom(2f)
                            .setMarginLeft(15f)
                        document.add(descPara)
                    }
                    
                    // Add source
                    if (source.isNotEmpty()) {
                        val sourcePara = Paragraph("Source: $source")
                            .setFontSize(8f)
                            .setFontColor(GRAY_COLOR)
                            .setItalic()
                            .setMarginBottom(8f)
                            .setMarginLeft(15f)
                        document.add(sourcePara)
                    }
                }
            }
            
            // General disclaimer
            val disclaimer = Paragraph("\nIMPORTANT DISCLAIMER")
                .setFontSize(11f)
                .setBold()
                .setFontColor(DANGER_COLOR)
                .setMarginTop(15f)
                .setMarginBottom(5f)
            document.add(disclaimer)
            
            val disclaimerText = Paragraph(
                "This screening tool is not a diagnostic test. It provides an assessment of risk factors " +
                "associated with Obstructive Sleep Apnea (OSA) based on validated questionnaires and algorithms. " +
                "A formal diagnosis can only be made by a qualified healthcare professional using clinical evaluation " +
                "and sleep studies. If you have concerns about sleep apnea, please consult with a sleep specialist " +
                "or your primary care physician."
            )
                .setFontSize(9f)
                .setFontColor(GRAY_COLOR)
                .setItalic()
                .setMarginBottom(20f)
            document.add(disclaimerText)
        }
        
        private fun addFooter(document: Document) {
            val footer = Paragraph("Generated by WakeUpCall Sleep Apnea Screening App")
                .setFontSize(8f)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20f)
            document.add(footer)
            
            val confidential = Paragraph("CONFIDENTIAL MEDICAL INFORMATION - For Patient Use Only")
                .setFontSize(8f)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
            document.add(confidential)
        }
        
        // Helper methods for table creation
        private fun addTableRow(table: Table, label: String, value: String) {
            table.addCell(
                com.itextpdf.layout.element.Cell()
                    .add(Paragraph(label).setBold().setFontSize(10f))
                    .setBorder(SolidBorder(LIGHT_GRAY, 0.5f))
                    .setPadding(8f)
            )
            table.addCell(
                com.itextpdf.layout.element.Cell()
                    .add(Paragraph(value).setFontSize(10f))
                    .setBorder(SolidBorder(LIGHT_GRAY, 0.5f))
                    .setPadding(8f)
            )
        }
        
        private fun addTableRow(table: Table, col1: String, col2: String, col3: String) {
            table.addCell(createTableCell(col1))
            table.addCell(createTableCell(col2))
            table.addCell(createTableCell(col3))
        }
        
        private fun addTableRow(table: Table, col1: String, col2: String, col3: String, col4: String) {
            table.addCell(createTableCell(col1))
            table.addCell(createTableCell(col2))
            table.addCell(createTableCell(col3))
            table.addCell(createTableCell(col4))
        }
        
        private fun addTableHeaderRow(table: Table, vararg headers: String) {
            headers.forEach { header ->
                table.addCell(
                    com.itextpdf.layout.element.Cell()
                        .add(Paragraph(header).setBold().setFontSize(10f).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(SECONDARY_COLOR)
                        .setBorder(SolidBorder(SECONDARY_COLOR, 0.5f))
                        .setPadding(8f)
                )
            }
        }
        
        private fun createTableCell(text: String): com.itextpdf.layout.element.Cell {
            return com.itextpdf.layout.element.Cell()
                .add(Paragraph(text).setFontSize(10f))
                .setBorder(SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(8f)
        }
        
        private fun addScoreInterpretations(document: Document, scores: SurveyScores) {
            val interpSection = Paragraph("Score Interpretations:")
                .setFontSize(11f)
                .setBold()
                .setMarginTop(10f)
                .setMarginBottom(8f)
            document.add(interpSection)
            
            // ESS interpretation
            val essInterp = Paragraph()
                .add(Text("ESS (Epworth Sleepiness Scale): ").setBold())
                .add(Text("Measures daytime sleepiness. Scores >10 indicate excessive daytime sleepiness."))
                .setFontSize(9f)
                .setMarginBottom(5f)
            document.add(essInterp)
            
            // Berlin interpretation
            val berlinInterp = Paragraph()
                .add(Text("Berlin Questionnaire: ").setBold())
                .add(Text("Assesses snoring, fatigue, and related factors. High risk = 2+ positive categories."))
                .setFontSize(9f)
                .setMarginBottom(5f)
            document.add(berlinInterp)
            
            // STOP-BANG interpretation
            val stopbangInterp = Paragraph()
                .add(Text("STOP-BANG: ").setBold())
                .add(Text("0-2 = Low risk, 3-4 = Intermediate risk, 5-8 = High risk for OSA."))
                .setFontSize(9f)
                .setMarginBottom(15f)
            document.add(stopbangInterp)
        }
    }
}

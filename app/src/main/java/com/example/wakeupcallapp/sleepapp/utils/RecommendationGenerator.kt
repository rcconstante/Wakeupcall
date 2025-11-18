package com.example.wakeupcallapp.sleepapp.utils

import com.example.wakeupcallapp.sleepapp.data.models.Demographics
import com.example.wakeupcallapp.sleepapp.data.models.MedicalHistory
import com.example.wakeupcallapp.sleepapp.data.models.GoogleFitData
import com.example.wakeupcallapp.sleepapp.data.models.SurveyScores

/**
 * Data class representing a single recommendation with title, description, and source.
 */
data class Recommendation(
    val title: String,
    val description: String,
    val source: String,
    val priority: Int = 0 // Higher value = higher priority
)

/**
 * Generates comprehensive, evidence-based recommendations for sleep apnea risk
 * based on multiple factors including demographics, medical history, survey scores,
 * and physical activity data.
 *
 * Algorithm based on CDC, AASM, NSF, WHO, and other authoritative sources.
 */
object RecommendationGenerator {

    /**
     * Generate all applicable recommendations based on user data.
     */
    fun generateRecommendations(
        demographics: Demographics,
        medicalHistory: MedicalHistory,
        scores: SurveyScores,
        googleFit: GoogleFitData?,
        bmi: Double
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        // Calculate sleep duration from Google Fit or estimate
        val sleepDuration = googleFit?.sleepDurationHours ?: estimateSleepDuration(scores)
        val physicalActivityMinutes = calculateActivityMinutes(googleFit?.dailySteps ?: 0)
        val activityType = determineActivityType(physicalActivityMinutes)
        val activityTime = "morning" // Default, could be enhanced with actual data

        // ============================================================
        // 1. SINGLE-FACTOR RULES
        // ============================================================

        // Sleep Duration
        if (sleepDuration < 7) {
            recommendations.add(
                Recommendation(
                    title = "Increase Your Total Sleep Time",
                    description = "You reported sleeping less than 7 hours per night. Adults typically need 7–9 hours of sleep. Gradually move your bedtime earlier by about 15 minutes every few days to reduce sleep debt.",
                    source = "Centers for Disease Control and Prevention (CDC) – Sleep Duration Recommendations; American Academy of Sleep Medicine (AASM).",
                    priority = 8
                )
            )
        }

        if (sleepDuration >= 9) {
            recommendations.add(
                Recommendation(
                    title = "Monitor Oversleeping and Sleep Quality",
                    description = "You reported sleeping 9 hours or more. Oversleeping can sometimes reflect poor sleep quality or fragmented sleep. Pay attention to how refreshed you feel during the day.",
                    source = "American Academy of Sleep Medicine (AASM) – Sleep Quality Guidance.",
                    priority = 5
                )
            )
        }

        // Snoring
        if (scores.stopbang.score >= 1) { // Snoring is part of STOP-BANG
            recommendations.add(
                Recommendation(
                    title = "Manage Snoring and Airway Obstruction",
                    description = "You reported regular snoring, which can be a sign of partial airway obstruction during sleep. Side-sleeping, using a supportive pillow, and avoiding heavy meals close to bedtime may help reduce snoring.",
                    source = "National Sleep Foundation – Snoring and Sleep; American Academy of Sleep Medicine (AASM) – Snoring and OSA.",
                    priority = 7
                )
            )
        }

        // Epworth Sleepiness Score
        if (scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Address Excessive Daytime Sleepiness",
                    description = "Your Epworth Sleepiness Score is elevated, which suggests excessive daytime sleepiness. This often reflects poor sleep quality or fragmented sleep at night.",
                    source = "Johns MW, Epworth Sleepiness Scale (1991); AASM – Daytime Sleepiness Guidance.",
                    priority = 9
                )
            )
        }

        // BMI
        if (bmi >= 30) {
            recommendations.add(
                Recommendation(
                    title = "Consider Weight's Impact on Breathing",
                    description = "Your BMI falls in a range that can increase narrowing of the upper airway during sleep. Even modest weight changes may help improve breathing and sleep quality over time.",
                    source = "World Health Organization (WHO) – BMI Classification; AASM – Obesity and OSA Risk.",
                    priority = 8
                )
            )
        }

        // Neck Circumference
        if (demographics.neckCircumferenceCm >= 40) {
            recommendations.add(
                Recommendation(
                    title = "Neck Size and Airway Narrowing",
                    description = "A neck circumference of 40 cm or more is associated with a higher chance of airway narrowing during sleep, which can contribute to snoring or sleep apnea.",
                    source = "Chung F. et al., STOP-Bang Questionnaire Guidelines.",
                    priority = 7
                )
            )
        }

        // Hypertension
        if (medicalHistory.hypertension) {
            recommendations.add(
                Recommendation(
                    title = "Hypertension and Sleep-Disordered Breathing",
                    description = "You reported hypertension. High blood pressure is commonly linked with undiagnosed sleep-disordered breathing and may be worsened by poor sleep.",
                    source = "American Heart Association (AHA) – OSA and Hypertension.",
                    priority = 8
                )
            )
        }

        // Diabetes
        if (medicalHistory.diabetes) {
            recommendations.add(
                Recommendation(
                    title = "Diabetes and Sleep Quality",
                    description = "You reported diabetes. Blood sugar imbalance is often associated with disrupted sleep patterns, and sleep apnea is more frequent among people with diabetes.",
                    source = "American Diabetes Association (ADA); AASM – Sleep and Metabolic Health.",
                    priority = 7
                )
            )
        }

        // Alcohol
        if (medicalHistory.alcohol) {
            recommendations.add(
                Recommendation(
                    title = "Reduce Alcohol Intake Near Bedtime",
                    description = "Since you reported alcohol use, especially if taken in the evening, it can relax the upper airway muscles, worsen snoring, and increase breathing pauses during sleep. Try to avoid alcohol at least 3–4 hours before bed.",
                    source = "American Academy of Sleep Medicine (AASM) – Alcohol and Sleep Quality.",
                    priority = 6
                )
            )
        }

        // STOP-BANG
        if (scores.stopbang.score >= 5) {
            recommendations.add(
                Recommendation(
                    title = "High STOP-Bang Score and OSA Risk",
                    description = "Your STOP-Bang score falls in a range associated with higher risk of obstructive sleep apnea. Monitoring your nighttime symptoms and daytime sleepiness is especially important.",
                    source = "Chung F. et al., STOP-Bang Questionnaire Validation Studies.",
                    priority = 10
                )
            )
        }

        // Physical Activity
        if (physicalActivityMinutes < 30) {
            recommendations.add(
                Recommendation(
                    title = "Increase Daily Physical Activity",
                    description = "You reported less than 30 minutes of physical activity per day. Increasing daily movement to at least 30 minutes can help improve sleep quality, reduce sleep latency, and support overall health.",
                    source = "CDC Physical Activity Guidelines; Harvard Medical School – Division of Sleep Medicine (Exercise and Sleep).",
                    priority = 7
                )
            )
        }

        if (physicalActivityMinutes >= 150) {
            recommendations.add(
                Recommendation(
                    title = "You Meet Activity Recommendations",
                    description = "Your reported activity matches or exceeds commonly recommended weekly activity levels. Regular movement is associated with deeper sleep and better daytime energy.",
                    source = "CDC Physical Activity Guidelines; Sleep Foundation – Exercise and Sleep Quality.",
                    priority = 3
                )
            )
        }

        // Activity Type
        when (activityType) {
            "light" -> recommendations.add(
                Recommendation(
                    title = "Light Activity and Sleep Support",
                    description = "You indicated mostly light activity. While light movement supports general health, adding some moderate-intensity exercise may have a stronger positive effect on sleep depth and quality.",
                    source = "Sleep Foundation – Exercise Intensity and Sleep.",
                    priority = 5
                )
            )
            "moderate" -> recommendations.add(
                Recommendation(
                    title = "Moderate Exercise and Deeper Sleep",
                    description = "Your activity level is in the moderate range. Regular moderate exercise is associated with improved deep sleep and reduced daytime fatigue.",
                    source = "American Academy of Sleep Medicine (AASM) – Physical Activity and Sleep.",
                    priority = 4
                )
            )
            "vigorous" -> recommendations.add(
                Recommendation(
                    title = "Timing Vigorous Exercise Wisely",
                    description = "You reported vigorous activity. Vigorous exercise can benefit sleep overall, but if done too close to bedtime, it may temporarily increase alertness and make it harder to fall asleep.",
                    source = "National Sleep Foundation – Vigorous Exercise and Sleep Onset.",
                    priority = 5
                )
            )
        }

        // Activity Timing
        when (activityTime) {
            "morning" -> recommendations.add(
                Recommendation(
                    title = "Morning Exercise and Body Clock",
                    description = "You usually exercise in the morning. Morning activity can help strengthen your sleep-wake cycle and support easier sleep onset at night.",
                    source = "Sleep Foundation – Morning Exercise and Circadian Rhythm.",
                    priority = 3
                )
            )
            "afternoon" -> recommendations.add(
                Recommendation(
                    title = "Afternoon Movement and Stress Relief",
                    description = "You usually exercise in the afternoon. Exercise at this time can help relieve stress and stabilize energy, which can support more restful sleep later.",
                    source = "American Academy of Sleep Medicine – Exercise Timing and Sleep.",
                    priority = 3
                )
            )
            "evening" -> recommendations.add(
                Recommendation(
                    title = "Evening Exercise and Sleep Onset",
                    description = "You usually exercise in the evening. For many people, light to moderate evening exercise is still compatible with good sleep, but vigorous activity too close to bedtime may delay sleep onset.",
                    source = "National Sleep Foundation – Evening Exercise and Sleep.",
                    priority = 4
                )
            )
            "night" -> recommendations.add(
                Recommendation(
                    title = "Nighttime Activity and Alertness",
                    description = "You reported exercising at night. Intense activity late at night can raise heart rate and alertness, which may make it harder to fall asleep soon after.",
                    source = "Harvard Medical School – Division of Sleep Medicine (Exercise Timing).",
                    priority = 5
                )
            )
        }

        // ============================================================
        // 2. TWO-FACTOR COMBINATION RULES
        // ============================================================

        // Alcohol + Hypertension
        if (medicalHistory.alcohol && medicalHistory.hypertension) {
            recommendations.add(
                Recommendation(
                    title = "Alcohol and Hypertension During Sleep",
                    description = "Combining alcohol use with hypertension can increase cardiovascular strain and contribute to more unstable breathing during sleep. Reducing evening alcohol intake can be especially beneficial for blood pressure and sleep.",
                    source = "American Academy of Sleep Medicine (AASM); American Heart Association (AHA).",
                    priority = 9
                )
            )
        }

        // Snoring + Hypertension
        if (scores.stopbang.score >= 1 && medicalHistory.hypertension) {
            recommendations.add(
                Recommendation(
                    title = "Snoring and Blood Pressure Risk",
                    description = "Snoring together with high blood pressure may increase strain on your heart and blood vessels during sleep. This pattern is often seen in individuals with undiagnosed sleep apnea.",
                    source = "American Heart Association (AHA); AASM – OSA and Cardiovascular Risk.",
                    priority = 9
                )
            )
        }

        // Snoring + High BMI
        if (scores.stopbang.score >= 1 && bmi >= 30) {
            recommendations.add(
                Recommendation(
                    title = "Snoring and Weight-Related Airway Narrowing",
                    description = "Snoring combined with a higher BMI increases the likelihood that your upper airway becomes narrowed or collapses during sleep, contributing to louder snoring or breathing pauses.",
                    source = "AASM – Obstructive Sleep Apnea Risk Factors; WHO – Obesity and Respiratory Function.",
                    priority = 9
                )
            )
        }

        // Snoring + High ESS
        if (scores.stopbang.score >= 1 && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Snoring and Excessive Daytime Sleepiness",
                    description = "Snoring plus significant daytime sleepiness suggests that your sleep may be fragmented or non-restorative, possibly due to repeated airway obstruction during the night.",
                    source = "Epworth Sleepiness Scale (Johns, 1991); AASM – Snoring and Sleep Fragmentation.",
                    priority = 10
                )
            )
        }

        // High BMI + Large Neck
        if (bmi >= 30 && demographics.neckCircumferenceCm >= 40) {
            recommendations.add(
                Recommendation(
                    title = "Body Habitus and Airway Structure",
                    description = "A combination of higher BMI and larger neck circumference is strongly associated with upper airway narrowing, which increases the risk of obstructed breathing during sleep.",
                    source = "STOP-Bang Guidelines (Chung F. et al.); WHO – BMI and OSA.",
                    priority = 9
                )
            )
        }

        // Low Sleep + High ESS
        if (sleepDuration < 7 && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Sleep Debt and Daytime Sleepiness",
                    description = "Short sleep combined with elevated daytime sleepiness suggests that you are accumulating sleep debt and your sleep is not fully restorative.",
                    source = "CDC – Sleep Duration and Health; Epworth Sleepiness Scale (Johns, 1991).",
                    priority = 10
                )
            )
        }

        // High STOP-BANG + Large Neck
        if (scores.stopbang.score >= 5 && demographics.neckCircumferenceCm >= 40) {
            recommendations.add(
                Recommendation(
                    title = "High-Risk Screening and Neck Anatomy",
                    description = "Your screening score and neck circumference together suggest a high likelihood of airway narrowing during sleep, which is characteristic of obstructive sleep apnea.",
                    source = "Chung F. et al., STOP-Bang Questionnaire Clinical Pathways.",
                    priority = 10
                )
            )
        }

        // Hypertension + Diabetes
        if (medicalHistory.hypertension && medicalHistory.diabetes) {
            recommendations.add(
                Recommendation(
                    title = "Metabolic and Blood Pressure Risks During Sleep",
                    description = "The combination of hypertension and diabetes is frequently seen in people with sleep-disordered breathing. Improving sleep quality can support overall cardiometabolic health.",
                    source = "American Heart Association (AHA); American Diabetes Association (ADA).",
                    priority = 9
                )
            )
        }

        // Alcohol + Snoring
        if (medicalHistory.alcohol && scores.stopbang.score >= 1) {
            recommendations.add(
                Recommendation(
                    title = "Alcohol's Effect on Snoring",
                    description = "Alcohol relaxes the muscles in the throat and can significantly worsen snoring intensity and frequency. Avoiding alcohol close to bedtime may reduce snoring.",
                    source = "American Academy of Sleep Medicine (AASM) – Alcohol and Airway Tone.",
                    priority = 8
                )
            )
        }

        // Vigorous Night Exercise
        if (activityType == "vigorous" && activityTime == "night") {
            recommendations.add(
                Recommendation(
                    title = "Vigorous Night Exercise and Sleep Onset",
                    description = "Doing vigorous exercise at night can increase heart rate and alertness, potentially delaying your ability to fall asleep, especially if your bedtime is soon after your workout.",
                    source = "National Sleep Foundation – Evening and Nighttime Exercise Studies.",
                    priority = 6
                )
            )
        }

        // Low Activity + High ESS
        if (physicalActivityMinutes < 30 && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Low Movement and Daytime Sleepiness",
                    description = "Low daily physical activity combined with significant daytime sleepiness suggests you may benefit from gradually increasing your activity to support better sleep and alertness.",
                    source = "CDC Physical Activity Guidelines; ESS Research on Fatigue.",
                    priority = 8
                )
            )
        }

        // Moderate Afternoon Exercise + Snoring
        if (activityType == "moderate" && activityTime == "afternoon" && scores.stopbang.score >= 1) {
            recommendations.add(
                Recommendation(
                    title = "Moderate Afternoon Exercise and Snoring",
                    description = "Moderate exercise in the afternoon can help improve nighttime sleep efficiency and may indirectly support better airway muscle tone, which can help with snoring.",
                    source = "AASM – Physical Activity and Sleep-Disordered Breathing.",
                    priority = 5
                )
            )
        }

        // Morning Exercise + High STOP-BANG
        if (activityTime == "morning" && scores.stopbang.score >= 5) {
            recommendations.add(
                Recommendation(
                    title = "Morning Activity for High-Risk Sleep Patterns",
                    description = "With a higher STOP-Bang score, maintaining a consistent morning exercise routine can help stabilize your circadian rhythm and support better nighttime sleep.",
                    source = "Sleep Foundation – Morning Exercise and Sleep-Wake Regulation; Chung F. et al., STOP-Bang Research.",
                    priority = 6
                )
            )
        }

        // High BMI + Low Activity
        if (bmi >= 30 && physicalActivityMinutes < 30) {
            recommendations.add(
                Recommendation(
                    title = "Weight and Inactivity Effects on Breathing",
                    description = "Higher body weight combined with low activity levels can contribute to reduced respiratory function and airway narrowing during sleep. Gradual increases in movement can be beneficial.",
                    source = "World Health Organization (WHO); AASM – Weight, Activity, and OSA.",
                    priority = 9
                )
            )
        }

        // Evening Exercise + Short Sleep
        if (activityTime == "evening" && sleepDuration < 7) {
            recommendations.add(
                Recommendation(
                    title = "Adjusting Evening Exercise to Improve Sleep",
                    description = "Since you are sleeping less than 7 hours and often exercise in the evening, shifting some workouts earlier in the day may help you wind down more easily at night.",
                    source = "Harvard Medical School – Division of Sleep Medicine, Exercise Timing and Sleep.",
                    priority = 7
                )
            )
        }

        // ============================================================
        // 3. THREE-FACTOR (OR MORE) HIGH-RISK RULES
        // ============================================================

        // Snoring + High BMI + High ESS
        if (scores.stopbang.score >= 1 && bmi >= 30 && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Strong Pattern of Possible Sleep-Disordered Breathing",
                    description = "The combination of snoring, higher BMI, and significant daytime sleepiness strongly suggests fragmented or disrupted sleep, possibly due to repeated breathing interruptions at night.",
                    source = "American Academy of Sleep Medicine (AASM); ESS Research (Johns, 1991).",
                    priority = 11
                )
            )
        }

        // Large Neck + High BMI + High STOP-BANG
        if (demographics.neckCircumferenceCm >= 40 && bmi >= 30 && scores.stopbang.score >= 5) {
            recommendations.add(
                Recommendation(
                    title = "Multiple Anatomical and Screening Indicators of OSA",
                    description = "Your neck size, weight, and STOP-Bang score together indicate a high probability of obstructive sleep apnea. This pattern is commonly seen in individuals with significant airway narrowing during sleep.",
                    source = "Chung F. et al., STOP-Bang Questionnaire Clinical Validation; WHO – Obesity and OSA.",
                    priority = 11
                )
            )
        }

        // Hypertension + Snoring + High ESS
        if (medicalHistory.hypertension && scores.stopbang.score >= 1 && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Cardiovascular Strain from Poor Sleep",
                    description = "High blood pressure combined with snoring and daytime sleepiness may indicate that your heart and blood vessels are under extra strain during sleep, often seen in people with sleep apnea.",
                    source = "American Heart Association (AHA); AASM – OSA and Cardiovascular Outcomes.",
                    priority = 11
                )
            )
        }

        // Short Sleep + High ESS + High STOP-BANG
        if (sleepDuration < 7 && scores.ess.score >= 11 && scores.stopbang.score >= 5) {
            recommendations.add(
                Recommendation(
                    title = "Sleep Debt and High Apnea Risk",
                    description = "Short sleep, significant daytime sleepiness, and a high STOP-Bang score together suggest that your sleep may be both insufficient and disrupted by breathing problems.",
                    source = "CDC – Sleep Duration; Epworth Sleepiness Scale; Chung F. et al., STOP-Bang.",
                    priority = 11
                )
            )
        }

        // Diabetes + Hypertension + Snoring
        if (medicalHistory.diabetes && medicalHistory.hypertension && scores.stopbang.score >= 1) {
            recommendations.add(
                Recommendation(
                    title = "Metabolic, Blood Pressure, and Airway Red Flags",
                    description = "The combination of diabetes, hypertension, and snoring is frequently observed in individuals with underlying sleep apnea. Addressing sleep quality can be an important part of overall health management.",
                    source = "American Heart Association (AHA); American Diabetes Association (ADA); AASM – Sleep and Cardiometabolic Health.",
                    priority = 11
                )
            )
        }

        // Low Activity + High BMI + Snoring
        if (physicalActivityMinutes < 30 && bmi >= 30 && scores.stopbang.score >= 1) {
            recommendations.add(
                Recommendation(
                    title = "Activity, Weight, and Breathing Difficulties",
                    description = "Low daily movement combined with higher BMI and snoring may indicate increased airway resistance and reduced respiratory fitness. Gradual increases in physical activity can help support better breathing and sleep.",
                    source = "AASM – OSA and Lifestyle; WHO; CDC Physical Activity Guidelines.",
                    priority = 10
                )
            )
        }

        // Moderate Morning Exercise + High ESS + Short Sleep
        if (activityType == "moderate" && activityTime == "morning" && 
            scores.ess.score >= 11 && sleepDuration < 7) {
            recommendations.add(
                Recommendation(
                    title = "Strengthening Your Sleep-Wake Cycle",
                    description = "You already benefit from morning moderate exercise, but your high daytime sleepiness and short sleep duration suggest your sleep-wake cycle may still be disrupted. Extending sleep time and keeping a consistent schedule can help.",
                    source = "Sleep Foundation – Morning Exercise; Epworth Sleepiness Scale; CDC – Sleep Duration.",
                    priority = 9
                )
            )
        }

        // Vigorous Night Exercise + High ESS
        if (activityType == "vigorous" && activityTime == "night" && scores.ess.score >= 11) {
            recommendations.add(
                Recommendation(
                    title = "Nighttime Intensity and Next-Day Tiredness",
                    description = "Vigorous exercise late at night, combined with significant daytime sleepiness, suggests that your workouts might be too close to bedtime or disrupting your ability to wind down.",
                    source = "National Sleep Foundation – Evening Exercise; ESS Research on Sleepiness.",
                    priority = 8
                )
            )
        }

        // High STOP-BANG + Low Activity + Hypertension
        if (scores.stopbang.score >= 5 && physicalActivityMinutes < 30 && medicalHistory.hypertension) {
            recommendations.add(
                Recommendation(
                    title = "High-Risk Profile with Low Activity",
                    description = "A high STOP-Bang score, low physical activity, and hypertension together indicate an increased cardiometabolic and sleep-related risk profile. Improving activity levels and sleep quality may have meaningful health benefits.",
                    source = "Chung F. et al., STOP-Bang; American Heart Association; CDC Physical Activity Guidelines.",
                    priority = 11
                )
            )
        }

        // ============================================================
        // HIGH RISK: Add professional consultation at the top
        // ============================================================
        
        // Check if user meets HIGH RISK criteria
        val isHighRisk = scores.stopbang.score >= 5 || 
            scores.berlin.category.contains("High", ignoreCase = true) ||
            (scores.stopbang.score >= 3 && bmi >= 30) ||
            (scores.ess.score >= 16) ||
            (scores.stopbang.score >= 3 && medicalHistory.hypertension && demographics.neckCircumferenceCm >= 40)
        
        if (isHighRisk) {
            recommendations.add(
                Recommendation(
                    title = "High Risk: Professional Sleep Evaluation Recommended",
                    description = "Based on your assessment results, you show multiple indicators strongly associated with sleep-disordered breathing. We strongly recommend consulting with a sleep specialist or healthcare provider for a comprehensive evaluation. A sleep study (polysomnography) may be necessary for accurate diagnosis and treatment planning.",
                    source = "American Academy of Sleep Medicine (AASM); Centers for Disease Control and Prevention (CDC); National Sleep Foundation.",
                    priority = 12
                )
            )
        }

        // Sort recommendations by priority (highest first) and return
        return recommendations.sortedByDescending { it.priority }
    }

    /**
     * Estimate sleep duration based on ESS and Berlin scores if Google Fit data unavailable.
     */
    private fun estimateSleepDuration(scores: SurveyScores): Double {
        val berlinScore = if (scores.berlin.category.contains("High", ignoreCase = true)) 2 else 0
        val essScore = scores.ess.score

        return if (berlinScore >= 2) {
            // High snoring/sleep problems
            5.5 + (essScore.toDouble() / 24.0) * 2.0 // 5.5-7.5 hours
        } else {
            // Lower sleep problems
            6.5 + ((24.0 - essScore) / 24.0) * 1.5 // 6.5-8 hours
        }.coerceIn(4.0, 10.0)
    }

    /**
     * Calculate physical activity minutes from daily steps.
     */
    private fun calculateActivityMinutes(dailySteps: Int): Int {
        // Rough estimate: 100 steps ≈ 1 minute of activity
        return (dailySteps / 100).coerceIn(0, 300)
    }

    /**
     * Determine activity type based on minutes.
     */
    private fun determineActivityType(minutes: Int): String {
        return when {
            minutes < 20 -> "light"
            minutes < 60 -> "moderate"
            else -> "vigorous"
        }
    }

    /**
     * Format recommendations for API response (pipe-separated format).
     * Each recommendation is formatted as "Title: Description (Source)"
     */
    fun formatForApi(recommendations: List<Recommendation>): String {
        return recommendations.joinToString(" | ") { rec ->
            "${rec.title}: ${rec.description} [${rec.source}]"
        }
    }

    /**
     * Format recommendations for display in the app (keeping only top N).
     */
    fun formatForDisplay(recommendations: List<Recommendation>, maxCount: Int = 10): List<Recommendation> {
        return recommendations.take(maxCount)
    }
}

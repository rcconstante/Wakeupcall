package com.example.wakeupcallapp.sleepapp.utils

object SurveyCalculator {
    
    // ============ EPWORTH SLEEPINESS SCALE (ESS) ============
    
    /**
     * Calculate ESS score from 8 responses (0-3 each)
     * 
     * @param responses List of 8 integers (0-3)
     * @return Pair of (score, category)
     */
    fun calculateESSScore(responses: List<Int>): Pair<Int, String> {
        require(responses.size == 8) { "ESS requires exactly 8 responses" }
        require(responses.all { it in 0..3 }) { "Each ESS response must be between 0 and 3" }
        
        val score = responses.sum()
        
        val category = when (score) {
            in 0..5 -> "Low daytime sleepiness (normal)"
            in 6..10 -> "High daytime sleepiness (normal)"
            in 11..12 -> "Mild excessive daytime sleepiness"
            in 13..15 -> "Moderate excessive daytime sleepiness"
            in 16..24 -> "Severe excessive daytime sleepiness"
            else -> "Invalid"
        }
        
        return Pair(score, category)
    }
    
    // ============ BERLIN QUESTIONNAIRE ============
    
    /**
     * Calculate Berlin Questionnaire score
     * 
     * Category 1: items 2, 3, 4, 5, and 6
     * - Item 2: if 'Yes', assign 1 point
     * - Item 3: if either of the last two options, assign 1 point
     * - Item 4: if either of the first two options, assign 1 point
     * - Item 5: if 'Yes', assign 1 point
     * - Item 6: if either of the first two options, assign 2 points
     * Category 1 is positive if total score is 2 or more points.
     * 
     * Category 2: items 7, 8, and 9
     * - Item 7: if either of the first two options, assign 1 point
     * - Item 8: if either of the first two options, assign 1 point
     * - Item 9: if 'Yes', assign 1 point
     * Category 2 is positive if total score is 2 or more points.
     * 
     * Category 3: positive if answer to item 10 is 'Yes' or if BMI > 30
     * 
     * High Risk: if there are 2 or more categories where the score is positive
     * Low Risk: if there is only 1 or no categories where the score is positive
     * 
     * @param category1Points Map of category 1 items (item2 to item6) to points
     * @param category2Points Map of category 2 items (item7 to item9) to points
     * @param category3Sleepy Whether item 10 is 'Yes'
     * @param bmi The person's BMI
     * @return Pair of (positive categories count, risk category)
     */
    fun calculateBerlinScore(
        category1Points: Map<String, Boolean>,
        category2Points: Map<String, Boolean>,
        category3Sleepy: Boolean,
        bmi: Double
    ): Pair<Int, String> {
        var positiveCategories = 0
        
        // Category 1: Sum points from items 2-6
        val cat1Score = category1Points.values.count { it }
        if (cat1Score >= 2) {
            positiveCategories++
        }
        
        // Category 2: Sum points from items 7-9
        val cat2Score = category2Points.values.count { it }
        if (cat2Score >= 2) {
            positiveCategories++
        }
        
        // Category 3: Sleepy or BMI > 30
        if (category3Sleepy || bmi > 30) {
            positiveCategories++
        }
        
        val riskCategory = if (positiveCategories >= 2) "High Risk" else "Low Risk"
        
        return Pair(positiveCategories, riskCategory)
    }
    
    // ============ STOP-BANG QUESTIONNAIRE ============
    
    /**
     * Calculate STOP-BANG score
     * 
     * STOP questions:
     * - S: Snoring
     * - T: Tired
     * - O: Observed apnea
     * - P: Pressure (high blood pressure)
     * 
     * BANG questions:
     * - B: BMI > 35
     * - A: Age > 50
     * - N: Neck circumference >= 40cm (16 inches)
     * - G: Gender (male)
     * 
     * Risk levels:
     * - Low Risk: 0-2 yes answers
     * - Intermediate Risk: 3-4 yes answers
     * - High Risk: 5-8 yes answers OR
     *   2+ STOP questions + male OR
     *   2+ STOP questions + BMI > 35 OR
     *   2+ STOP questions + neck >= 40cm
     * 
     * @return Pair of (score, risk category)
     */
    fun calculateStopBangScore(
        snoring: Boolean,
        tired: Boolean,
        observedApnea: Boolean,
        hypertension: Boolean,
        bmi: Double,
        age: Int,
        neckCircumference: Double,
        isMale: Boolean
    ): Pair<Int, String> {
        var score = 0
        var stopScore = 0
        
        // STOP questions
        if (snoring) {
            score++
            stopScore++
        }
        if (tired) {
            score++
            stopScore++
        }
        if (observedApnea) {
            score++
            stopScore++
        }
        if (hypertension) {
            score++
            stopScore++
        }
        
        // BANG questions
        if (bmi > 35) score++
        if (age > 50) score++
        if (neckCircumference >= 40.0) score++
        if (isMale) score++
        
        // Determine risk category
        val riskCategory = when {
            score >= 5 -> "High Risk"
            score in 3..4 -> "Intermediate Risk"
            score in 0..2 -> {
                // Check special high-risk conditions
                if (stopScore >= 2 && (isMale || bmi > 35 || neckCircumference >= 40.0)) {
                    "High Risk"
                } else {
                    "Low Risk"
                }
            }
            else -> "Low Risk"
        }
        
        return Pair(score, riskCategory)
    }
    
    // ============ HELPER FUNCTIONS ============
    
    /**
     * Calculate BMI from height and weight
     * 
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @return BMI value
     */
    fun calculateBMI(heightCm: Double, weightKg: Double): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }
    
    /**
     * Estimate sleep quality from ESS score (inverse relationship)
     * 
     * @param essScore ESS score (0-24)
     * @return Sleep quality proxy (0-10)
     */
    fun estimateSleepQuality(essScore: Int): Int {
        return (10 - (essScore / 3)).coerceIn(1, 10)
    }
    
    /**
     * Estimate activity level from daily steps
     * 
     * @param dailySteps Number of daily steps
     * @return Activity level (1-5)
     */
    fun estimateActivityLevel(dailySteps: Int, age: Int, hasHealthIssues: Boolean): Int {
        var level = when {
            dailySteps < 3000 -> 1  // Sedentary
            dailySteps < 6000 -> 2  // Low active
            dailySteps < 8000 -> 3  // Moderate
            dailySteps < 10000 -> 4 // Active
            else -> 5               // Very active
        }
        
        // Adjust for age and health
        if (age > 60) level = maxOf(1, level - 1)
        if (hasHealthIssues) level = maxOf(1, level - 1)
        
        return level
    }
}

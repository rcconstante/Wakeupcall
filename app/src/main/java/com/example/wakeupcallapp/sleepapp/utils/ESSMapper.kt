package com.example.wakeupcallapp.sleepapp.utils

/**
 * Utility object for mapping ESS (Epworth Sleepiness Scale) responses
 */
object ESSMapper {
    /**
     * Maps ESS dozing choice text to score (0-3)
     * 
     * @param choice The text choice selected by the user
     * @return Score from 0 (no chance) to 3 (high chance)
     */
    fun mapESSDozing(choice: String): Int {
        return when(choice) {
            "No chance of dozing" -> 0
            "Slight chance of dozing" -> 1
            "Moderate chance of dozing" -> 2
            "High chance of dozing" -> 3
            else -> 0
        }
    }
    
    /**
     * Reverse maps ESS score (0-3) to choice text
     * Used for restoring saved choices when navigating back
     * 
     * @param score ESS score from 0-3
     * @return The text choice corresponding to the score
     */
    fun scoreToChoice(score: Int): String {
        return when(score) {
            0 -> "No chance of dozing"
            1 -> "Slight chance of dozing"
            2 -> "Moderate chance of dozing"
            3 -> "High chance of dozing"
            else -> ""
        }
    }
}

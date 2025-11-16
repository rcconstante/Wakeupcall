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
}

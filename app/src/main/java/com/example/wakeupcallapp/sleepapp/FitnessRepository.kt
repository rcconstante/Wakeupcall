package com.example.wakeupcallapp.sleepapp

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FitnessRepository(private val context: Context) {

    private val TAG = "FitnessRepository"

    // Build the FitnessOptions required to access steps & sleep data
    fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()
    }

    // Check if Google Fit permissions are granted
    fun hasPermissions(): Boolean {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, getFitnessOptions())
            val result = GoogleSignIn.hasPermissions(account, getFitnessOptions())
            Log.d(TAG, "Google Fit permission check: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Fit permissions", e)
            false
        }
    }

    // Get today's total step count
    suspend fun getStepCount(startDate: Calendar): Int? {
        try {
            val endDate = Calendar.getInstance()
            val account = GoogleSignIn.getAccountForExtension(context, getFitnessOptions())

            Log.d(TAG, "Fetching step count from: ${startDate.time} → ${endDate.time}")

            val response = Fitness.getHistoryClient(context, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .await()

            if (response.dataPoints.isNotEmpty()) {
                val steps = response.dataPoints.first().getValue(
                    DataType.TYPE_STEP_COUNT_DELTA.fields[0]
                ).asInt()
                Log.d(TAG, "Daily step count retrieved: $steps")
                return steps
            } else {
                Log.w(TAG, "No step data available in Fit for the current day.")
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while accessing Google Fit (check ACTIVITY_RECOGNITION and location)", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error while fetching step count from Fit", e)
        }
        return null
    }

    // Average daily steps based on total steps
    suspend fun getAverageDailySteps(days: Int): Int? {
        val totalSteps = getStepCount(Calendar.getInstance())
        return if (totalSteps != null && days > 0) {
            totalSteps / days
        } else null
    }

    // Get sleep duration (hours)
    suspend fun getSleepDuration(startDate: Calendar): Double? {
        try {
            val endDate = Calendar.getInstance()
            val account = GoogleSignIn.getAccountForExtension(context, getFitnessOptions())

            Log.d(TAG, "Fetching sleep data from: ${startDate.time} → ${endDate.time}")

            val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_SLEEP_SEGMENT)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(
                    startDate.timeInMillis,
                    endDate.timeInMillis,
                    TimeUnit.MILLISECONDS
                )
                .build()

            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()

            var totalSleepMillis = 0L

            for (bucket in response.buckets) {
                for (dataSet: DataSet in bucket.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        val duration = dp.getEndTime(TimeUnit.MILLISECONDS) -
                                dp.getStartTime(TimeUnit.MILLISECONDS)
                        totalSleepMillis += duration
                    }
                }
            }

            if (totalSleepMillis > 0) {
                val totalSleepHours = totalSleepMillis.toDouble() / (1000 * 60 * 60)
                Log.d(TAG, "Sleep data retrieved: $totalSleepHours hours total.")
                return totalSleepHours
            } else {
                Log.w(TAG, "No sleep data found in Google Fit.")
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while accessing sleep data.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sleep data from Fit", e)
        }
        return null
    }

    suspend fun getAverageSleepDuration(days: Int): Double? {
        val total = getSleepDuration(Calendar.getInstance())
        return if (total != null && days > 0) total / days else null
    }
}

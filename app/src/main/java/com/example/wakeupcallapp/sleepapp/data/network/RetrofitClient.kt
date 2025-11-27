package com.example.wakeupcallapp.sleepapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Singleton object to manage Retrofit API client
 * 
 * IMPORTANT: Update BASE_URL with your computer's IP address
 * To find your IP: Open PowerShell and run "ipconfig"
 * Look for IPv4 Address (e.g., 192.168.1.100)
 * 
 * For Android 13+ (API 33+), make sure network_security_config.xml allows cleartext traffic
 */
object RetrofitClient {
    
    // TODO: Replace with your computer's IP address
    // Example: "http://127.0.0.1.5:5000/"
    private const val BASE_URL = "http://192.168.1.100:5000/" // Physical device - YOUR COMPUTER'S IP
    // For emulator only: "http://10.0.2.2:5000/"
    
    // Use HEADERS level to avoid consuming response body stream for large files
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }
    
    // Retry interceptor for connection issues
    private val retryInterceptor = Interceptor { chain ->
        var request = chain.request()
        var response: Response? = null
        var exception: Exception? = null
        var tryCount = 0
        val maxTries = 3
        
        while (tryCount < maxTries && response == null) {
            try {
                response = chain.proceed(request)
                // If response is not successful, retry
                if (!response.isSuccessful && tryCount < maxTries - 1) {
                    response.close()
                    response = null
                    tryCount++
                    Thread.sleep(1000L * tryCount) // Exponential backoff
                }
            } catch (e: SocketTimeoutException) {
                exception = e
                tryCount++
                if (tryCount < maxTries) {
                    Thread.sleep(1000L * tryCount)
                }
            } catch (e: ConnectException) {
                exception = e
                tryCount++
                if (tryCount < maxTries) {
                    Thread.sleep(1000L * tryCount)
                }
            } catch (e: UnknownHostException) {
                // Don't retry for unknown host - network issue
                throw e
            }
        }
        
        response ?: throw (exception ?: ConnectException("Failed to connect after $maxTries attempts"))
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .connectTimeout(90, TimeUnit.SECONDS)  // Increased for slower networks on Android 13
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)  // Enable automatic retry
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

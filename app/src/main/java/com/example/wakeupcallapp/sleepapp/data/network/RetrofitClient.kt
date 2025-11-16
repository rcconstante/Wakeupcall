package com.example.wakeupcallapp.sleepapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to manage Retrofit API client
 * 
 * IMPORTANT: Update BASE_URL with your computer's IP address
 * To find your IP: Open PowerShell and run "ipconfig"
 * Look for IPv4 Address (e.g., 192.168.1.100)
 */
object RetrofitClient {
    
    // TODO: Replace with your computer's IP address
    // Example: "http://192.168.1.100:5000/"
    private const val BASE_URL = "http://10.0.2.2:5000/" // Android Emulator localhost
    // For physical device: "http://YOUR_IP_ADDRESS:5000/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

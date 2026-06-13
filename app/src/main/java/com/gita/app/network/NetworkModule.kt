package com.gita.app.network

import android.util.Log
import com.gita.app.data.GeminiRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Singleton object to provide a shared OkHttpClient for all API requests.
 * Includes exponential backoff retry logic for resilience.
 */
object NetworkModule {
    private const val TAG = "NetworkModule"
    private const val MAX_RETRIES = 2

    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var retryCount = 0

        while (retryCount <= MAX_RETRIES) {
            try {
                if (retryCount > 0) {
                    val delayMs = (1000 * Math.pow(2.0, (retryCount - 1).toDouble())).toLong()
                    Log.d(TAG, "Retrying request in ${delayMs}ms (Attempt ${retryCount + 1})")
                    Thread.sleep(delayMs)
                }
                
                // If we already have a response from a previous failed attempt, close it
                response?.close()
                
                response = chain.proceed(request)
                
                // If successful or it's a client error (4xx) that shouldn't be retried
                if (response.isSuccessful || (response.code in 400..499 && response.code != 408 && response.code != 429)) {
                    return@Interceptor response
                }
                
                Log.w(TAG, "Request failed with code: ${response.code}")
            } catch (e: IOException) {
                exception = e
                Log.w(TAG, "Network request failed: ${e.message}")
            }
            
            retryCount++
        }
        
        // If we get here, all retries failed
        response ?: throw exception ?: IOException("Unknown network error")
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(retryInterceptor)
            .build()
    }

    private val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val geminiApi: GeminiApi by lazy {
        geminiRetrofit.create(GeminiApi::class.java)
    }

    fun geminiRepository(apiKey: String): GeminiRepository =
        GeminiRepository(geminiApi, apiKey)
}

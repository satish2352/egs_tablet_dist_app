package com.sipl.egstabdistribution.webservice

import android.content.Context
import com.sipl.egstabdistribution.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val BASE_URL = BuildConfig.BASE_URL
    val loggingInterceptor = HttpLoggingInterceptor()

    private fun getAuthInterceptor(context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    fun create(context: Context): ApiService {
        if(BuildConfig.DEBUG){
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }else{
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(getAuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(300,TimeUnit.SECONDS)
            .readTimeout(300,TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
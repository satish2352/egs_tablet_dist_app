package com.sipl.egstabdistribution.webservice

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {


    private const val BASE_URL = "https://egstabletdistribution.sumagotest.in/api/"

    val loggingInterceptor = HttpLoggingInterceptor()

    private fun getAuthInterceptor(context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    fun create(context: Context): ApiService {
        /*val properties = Properties()
        try {
            context.assets.open("local.properties")
                .use { inputStream -> properties.load(inputStream) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("mytag",""+properties.getProperty("BASE_URL"))*/

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
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
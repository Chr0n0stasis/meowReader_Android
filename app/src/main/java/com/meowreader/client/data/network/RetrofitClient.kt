package com.meowreader.client.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getService(context: android.content.Context): ApiService {
        val prefs = context.getSharedPreferences("reader_prefs", android.content.Context.MODE_PRIVATE)
        val baseUrl = prefs.getString("backend_url", "https://raw.githubusercontent.com/Chr0n0stasis/meowReader_server/main/") ?: ""
        
        return retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

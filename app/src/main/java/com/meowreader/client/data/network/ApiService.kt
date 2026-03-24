package com.meowreader.client.data.network

import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET("index.json")
    suspend fun getIndex(): List<IndexResponse>

    @GET
    suspend fun getPaperGroup(@Url url: String): PaperGroupResponse
}

package com.example.starter.core.network

import com.example.starter.core.network.model.SampleItemResponse
import retrofit2.http.GET

interface SampleApi {
    // Example endpoint: replace with your own API.
    @GET("sample/items")
    suspend fun getItems(): List<SampleItemResponse>
}

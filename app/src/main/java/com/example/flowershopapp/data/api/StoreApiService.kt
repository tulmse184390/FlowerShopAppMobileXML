package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.StoreLocationDto
import retrofit2.Response
import retrofit2.http.GET

interface StoreApiService {
    @GET("api/Store")
    suspend fun getStores(): Response<List<StoreLocationDto>>
}

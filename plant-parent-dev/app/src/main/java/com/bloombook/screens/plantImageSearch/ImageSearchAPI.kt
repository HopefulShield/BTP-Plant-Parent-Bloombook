package com.bloombook.screens.plantImageSearch

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ImageSearchApi {
    @Multipart
    @POST("/v2/identify/{project}")
    suspend fun identifyPlant(
        @Path("project") project: String,
        @Query("api-key") apiKey: String,
        @Part images: List<MultipartBody.Part>,
    ): Response<PlantMatchResponse>
}

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://my-api.plantnet.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val imageSearchApi: ImageSearchApi = retrofit.create(ImageSearchApi::class.java)
}


data class PlantMatchResponse(
    val results: List<PlantMatch>
)

data class PlantMatch(
    val score: Double,
    val species: PlantSpecies,
    val images: List<PlantImage>
)

data class PlantSpecies(
    val scientificName: String,
    val commonNames: List<String>
)

data class PlantImage(
    val url: UrlInfo
)

data class UrlInfo(
    val o: String
)
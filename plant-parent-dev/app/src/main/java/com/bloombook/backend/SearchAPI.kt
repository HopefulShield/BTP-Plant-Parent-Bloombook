package com.bloombook.backend
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class SearchResult(
    val plant: String,
    val similarityScore: Double
)

interface PlantSearchApi {
    @GET("/api/search_plants/{query}")
    suspend fun searchPlants(@Path("query") query: String): List<SearchResult>
}

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://us-central1-credible-acre-394621.cloudfunctions.net")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val plantSearchApi: PlantSearchApi = retrofit.create(PlantSearchApi::class.java)
}
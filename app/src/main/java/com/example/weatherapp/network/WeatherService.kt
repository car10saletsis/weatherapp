package com.example.weatherapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeatherById(
        //@Query("id") lon: Long,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String?,
        @Query("lang") lang: String?, //Idioma
        @Query("appid") appid: String): WeatherEntity


}
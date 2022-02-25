package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.network.WeatherEntity
import com.example.weatherapp.network.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityMainBinding.inflate(layoutInflater)
       setContentView(binding.root)

        setupActionBar()
      }

    private fun setupActionBar() {
        lifecycleScope.launch {
            formatResponse(getWeather())
        }
    }

        private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO)
        {
            setUpTitle("Consultando")
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit.create(WeatherService::class.java)

            service.getWeatherById(3530597L, "metric", "aea11b5fadb63b272924320f5f10e4b3")
       }



        private fun setUpTitle(newTitle: String){
        supportActionBar?.let{ title = newTitle}
        }

    private fun formatResponse(weatherEntity: WeatherEntity){
        val temp = "${weatherEntity.main.temp} C"
        val name = weatherEntity.name
        val country = weatherEntity.sys.country
        setUpTitle("$temp in $name, $country")
    }







    }


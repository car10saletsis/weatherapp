package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.network.WeatherEntity
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.utils.checkForInternet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityMainBinding.inflate(layoutInflater)
       setContentView(binding.root)

        setUpViewData()
      }

    private fun setUpViewData() {

        if(checkForInternet(this)) {
            lifecycleScope.launch{
                formatResponse(getWeather())
            }
        } else{
            showError("Sin aceeso a internet")
            //Se pone invisible los detalles del clima
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

            service.getWeatherById(3530597L, "metric", "sp", "aea11b5fadb63b272924320f5f10e4b3")
       }



        private fun setUpTitle(newTitle: String){
        supportActionBar?.let{ title = newTitle}
        }

    private fun formatResponse(weatherEntity: WeatherEntity){
       try{
           val temp = "${weatherEntity.main.temp} C"
           val name = weatherEntity.name
           val country = weatherEntity.sys.country
           val address = "$name, $country"
           val dateNow = Calendar.getInstance().time
           val tempMin = "Min: ${weatherEntity.main.temp_min.toInt()}~"
           val tempMax = "${weatherEntity.main.temp_max.toInt()}~"
           val status = weatherEntity.weather[0].description.uppercase()
           val dt = weatherEntity.dt
           val updateAt = "Actualizado: ${SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(dt*1000))}"
           val sunrise = weatherEntity.sys.sunrise
           val sunriseFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
           val sunset = weatherEntity.sys.sunset
           val sunsetFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
           val wind = "${weatherEntity.wind.speed} km//hr"
           val pressure = "${weatherEntity.main.pressure} mb"
           val humidity = "${weatherEntity.main.feels_like.toInt()}~"
           val icon = weatherEntity.weather[0].icon
           val iconUrl = "https://openweathermap.org/img/w/$icon.png"
           val feelsLike = "Sensacion: ${weatherEntity.main.feels_like.toInt()}"



           binding.apply {
               addressTextView.text = address
               dateTextView.text = dateNow.toString()
               temperatureTextView.text = temp
               statusTextView.text = status
               tempMinTextView.text = tempMin
               tempMaxTextView.text = tempMax
               sunriseTextView.text = sunriseFormat
               sunsetTextView.text = sunsetFormat
               windTextView.text = wind
               humidityTextView.text = humidity
               pressureTextView.text = pressure
               feelsLikeTextView.text = feelsLike
               iconImageView.load(iconUrl)

           }
            showIndicator(false)

       } catch (exception: Exception){
           showError("Ha ocurrido un error")
           Log.e("Error format", "Ha ocurrido un error")
       }






    }

    private fun showError(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible

    }


}


package com.example.weatherapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.weatherapp.BuildConfig.APPLICATION_ID
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.network.WeatherEntity
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.utils.checkForInternet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityError"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34


    private var latitude = ""
    private var longitude = ""


    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityMainBinding.inflate(layoutInflater)
       setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setUpViewData()
      }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_actualizar -> {
                Toast.makeText(this, "Mebnu seleccionado", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
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
            Log.e(TAG, "CORR Lat: $latitude Long: $longitude")
            showIndicator(true)
            //setUpTitle("Consultando")
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit.create(WeatherService::class.java)

            service.getWeatherById(latitude, longitude, "metric", "sp", "aea11b5fadb63b272924320f5f10e4b3")
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

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION),
        REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    private fun requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)){
            Log.i(TAG, "Muestra explicacion de porque se requiere el permiso")
            showSnackbar(R.string.permission_rationale, R.string.ok){
                startLocationPermissionRequest()
            }
        }else {
            Log.i(TAG, "Solicitando Permiso")
            startLocationPermissionRequest()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        fusedLocationClient.lastLocation
        .addOnCompleteListener{ taskLocation->
            if (taskLocation.isSuccessful && taskLocation.result != null){
                val location = taskLocation.result

                latitude = location?.latitude.toString()
                longitude = location?.longitude.toString()
                Log.d(TAG, "GetLasLoc Lat: $latitude Long: $longitude")
            } else {
                Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                showSnackbar(R.string.no_location_detected)
            }
        }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE){
            when{
                grantResults.isEmpty() -> Log.i(TAG, "Operacion cancelada")
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()

                else ->{
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings)
                    {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showSnackbar(
        snackStrid: Int,
        actionStrid: Int = 0,
        listener: View.OnClickListener? = null
    ){
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrid),
        LENGTH_INDEFINITE)
        if (actionStrid != 0 && listener !=null) {
            snackbar.setAction(getString(actionStrid), listener)
        }
        snackbar.show()
    }
    }



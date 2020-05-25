package com.example.myyelp

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.jar.Manifest

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "kt6wID76f5q-EJGwUzDAUcPzL2UvG0fHX6ojsFUXFBs_lTMdb7ESRskPa8I2nZRGjC33xPNRfmV8o6jxaBeFE-lpptQoNiUFv5tF-EuDkC7m3XeHHxs-imlhCp-8XnYx"
private  const val REQUEST_LOCATION_CODE = 13
//these will be updated as program runs and user inputs data - it is used to fill parameters of yelpService request
private var givenTerm: String = ""
private var givenLocation: String = ""
private var lat : Double? = null
private var long : Double? = null
class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set default search terms for yelp API and search yelp accordingly
        givenTerm = "Avocado Toast"
        givenLocation = "New York"
        searchYelp(givenTerm, givenLocation)

        //setup the speed dial to recieve user input
        fabSD.setMenuListener(object: SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {
                if (menuItem != null) {
                    if(menuItem.itemId == R.id.search) {
                        showTermDialog()
                    }

                    if(menuItem.itemId == R.id.location) {
                        Log.i(TAG, "selected $menuItem")
                        showLocationDialog()
                    }
                }
                return super.onMenuItemSelected(menuItem)
            }
        })

        //create Fused Location Provider Client instance
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocation() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "we are good to get location!")
                getLastKnownLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Toast.makeText(this, "You must enable location to use \"My Location\" feature!", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)
            }
        }
    }

    private fun getLastKnownLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                lat = location?.latitude
                long = location?.longitude
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "we got 'em boys")
                    getLastKnownLocation()
                } else {
                    Toast.makeText(this, "To use \"My Location\" funcationality, please enable location permissions!", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
    private fun showTermDialog() {
        val searchFormView = LayoutInflater.from(this).inflate(R.layout.search_term, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Search Businesses")
            .setView(searchFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val entry = dialog.findViewById<EditText>(R.id.etSearch)?.text.toString()
            givenTerm = entry
            searchYelp(givenTerm, givenLocation)
            dialog.dismiss()
        }
    }

    private fun showLocationDialog() {
        val searchFormView = LayoutInflater.from(this).inflate(R.layout.search_term, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Specify Location")
            .setView(searchFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .setNeutralButton("My Location", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            Log.i(TAG, "get my location!")
            requestLocation()
            if (lat != null && long != null) {
                Log.i(TAG, "location = $lat, $long")
                givenLocation = "$lat, $long"
                searchYelp(givenTerm, givenLocation)
            } else {
                Toast.makeText(this, "Couldn't get location. Sorry!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val entry = dialog.findViewById<EditText>(R.id.etSearch)?.text.toString()
            givenLocation = entry
            searchYelp(givenTerm, givenLocation)
            dialog.dismiss()
        }
    }

    private fun searchYelp(searchTerm: String, location: String) {
        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        //create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, location)
            .enqueue(object : Callback<YelpSearchResult> {
                override fun onResponse(
                    call: Call<YelpSearchResult>,
                    response: Response<YelpSearchResult>
                ) {
                    Log.i(TAG, "onResponse: $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFailure: $t")
                }
            })
    }

}

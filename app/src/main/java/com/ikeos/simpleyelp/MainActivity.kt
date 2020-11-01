package com.ikeos.simpleyelp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import android.widget.Toast
import java.io.IOException


private const val TAG = "MainActivity"
private const val CLIENT_ID = "08NFRsUxJj_dA68lJhJwnA"
private const val API_KEY = "cZDEFvbEBwFFQDmR4T0dZsU8GTGJdKKZ1G_B5SlUtfP9MOHity3nM7BWJCoRKlzF2ED9i2XXVEJsn7k3qon4cIu7HWZCjMN9ht3JARmLwT_gmjEkzekgY2zUxuudX3Yx"
private const val BASE_URL =  "https://api.yelp.com/v3/"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //define vars for the recycler view display
        val restaurants = mutableListOf<YelpRestaurant>();
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        //create an instance of retrofit
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY", "Avocado Toast", "New York")
            .enqueue(object: Callback<YelpSearchResult> {
                override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                    val body = response.body()
                    if(body == null){
                        Log.i(TAG, "Response $response")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    if(isNetworkAvailable()){
                        Toast.makeText(this@MainActivity, "It appears your network connection is broken!", Toast.LENGTH_LONG).show()
                    } else if (!isOnline()) {
                        Toast.makeText(this@MainActivity, "It appears you have no internet connection", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Sorry, request to yelp api wasn't successful!", Toast.LENGTH_LONG).show()
                    }
                }
            })

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    private fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }
}

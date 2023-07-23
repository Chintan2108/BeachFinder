package com.example.beachfinder1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.api.load
import coil.transform.RoundedCornersTransformation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Locale

class BeachDetailsActivity: AppCompatActivity() {

    private val TAG = "BeachDetailsActivity"


    companion object {
        private const val EXTRA_BEACH_NAME = "EXTRA_BEACH_NAME"
    }

    private var beachNameString: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beach_details)

        beachNameString = savedInstanceState?.getString(EXTRA_BEACH_NAME)

        if (beachNameString == null) {
            beachNameString = intent.getStringExtra(EXTRA_BEACH_NAME)
        }

        initBackBtn()

        beachNameString?.let { setBeachName(it) }
        beachNameString?.let { makeNetworkCall(it) }
        setupNavigator()
        loadBgImage()
    }

    private fun loadBgImage() {

        val imageurl: String? = when(beachNameString) {
            "Santa Barbara Beach" -> "https://beachsideinn.com/wp-content/uploads/2021/03/IMG_7459-1024x679-1.jpg"
            "Malibu Beach" -> "https://malibuluxuryrealty.com/wp-content/uploads/2019/05/Latigo-Beach-palm-trees-Malibu-1-1.jpg"
            "Venice Beach" -> "https://a.cdn-hotels.com/gdcs/production103/d1593/995f6282-43fe-464d-ba3d-2b646a8f7ec3.jpg"
            "Huntington Beach" -> "https://assets.simpleviewinc.com/simpleview/image/upload/c_limit,h_1200,q_75,w_1200/v1/clients/surfcityusa/DJI_0387_1__2a9cbdab-8c22-4a75-bc70-ff743394f11d.jpg"
            "Laguna Beach" -> "https://cdn.britannica.com/69/175869-050-DFF34225/Crescent-Bay-Beach-Laguna-California.jpg"
            "La Jolla Beach" -> "https://lajollamom.com/wp-content/uploads/2018/05/la-jolla-cove-guide.jpg"
            else -> null
        }

        val imageView = findViewById<ImageView>(R.id.iv_beach)
        imageView.load(imageurl) {
            crossfade(true)
            crossfade(500)
            placeholder(R.drawable.tropical_beach_74190_188)
            transformations(RoundedCornersTransformation())
            error(R.drawable.tropical_beach_74190_188)
            fallback(R.drawable.tropical_beach_74190_188)
        }
    }

    private fun setBeachName(beachName: String) {
        val tvName = findViewById<TextView>(R.id.tv_name)
        tvName.text = beachName
    }

    private fun initBackBtn() {
//        val backBtn = findViewById<ImageView>(R.id.imageViewBack)
//        backBtn.setOnClickListener {
//            finish()
//        }
    }

    private fun makeNetworkCall(beachName: String) {
        // Make the GET request
        val beachQueryName = beachName.replace(" ","").lowercase(Locale.ROOT)
//        Log.d("Beachnames", beachQueryName)

        val laptopIpAddress = "10.150.121.2" // Replace with the laptop's IP address
        val port = 5000
        val queryPart = "score?beach="
        val url = "http://$laptopIpAddress:$port/$queryPart$beachQueryName"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val jsonObject = JSONObject(responseBody)

                            // Extract individual variables from the JSON object
                            val beachName = jsonObject.optString("beach_name")
                            val waterLevel = jsonObject.optString("water_level")
                            val waterTemp = jsonObject.optString("water_temp")
                            val wind = jsonObject.optString("wind")
                            val fee = jsonObject.optString("fee")
                            val parking = jsonObject.optString("parking")
                            val disabledAccess = jsonObject.optString("disabled_access")
                            val restrooms = jsonObject.optString("restrooms")
                            val algalScore = jsonObject.optString("algal_score")
                            val beachScore = jsonObject.optString("beach_score")

                            // Now you have all the variables parsed from the JSON response
                            // You can use them as needed in your application

                            Log.d(TAG, "Beach Name: $beachName")
                            Log.d(TAG, "Water Level: $waterLevel")
                            Log.d(TAG, "Water Temperature: $waterTemp")
                            Log.d(TAG, "Wind: $wind")
                            Log.d(TAG, "Fee: $fee")
                            Log.d(TAG, "Parking: $parking")
                            Log.d(TAG, "Disabled Access: $disabledAccess")
                            Log.d(TAG, "Restrooms: $restrooms")
                            Log.d(TAG, "Algal score: $algalScore")
                            Log.d(TAG, "Beach score: $beachScore")

                            // Update your UI or perform other operations with the extracted values
                            runOnUiThread {
                                updateUI(
                                    waterLevel,
                                    waterTemp,
                                    wind,
                                    fee,
                                    parking,
                                    disabledAccess,
                                    restrooms,
                                    algalScore,
                                    beachScore
                                )
                            }
                        } catch (e: Exception) {
                            // Handle JSON parsing error
                            Log.e(TAG, "Error parsing JSON: ${e.message}")
                        }
                    }
                } else {
                    // Handle the error here
                    Log.e(TAG, "Error: ${response.code} ${response.message}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle failure here
                Log.e(TAG, "Error: ${e.message}", e)
            }
        })
    }

    private fun updateUI(
        waterLevel: String,
        waterTemp: String,
        wind: String,
        fee: String,
        parking: String,
        disabledAccess: String,
        restrooms: String,
        algalScore: String,
        beachScore: String
    ) {
        val waterLevelValue = findViewById<TextView>(R.id.waterLevelValue)
        val waterTempValue = findViewById<TextView>(R.id.waterTempValue)
        val windValue = findViewById<TextView>(R.id.windValue)
        val feeValue = findViewById<TextView>(R.id.feeValue)
        val parkingValue = findViewById<TextView>(R.id.parkingValue)
        val disabledValue = findViewById<TextView>(R.id.disabledValue)
        val restValue = findViewById<TextView>(R.id.restValue)
        val algalScoreValue = findViewById<TextView>(R.id.algalScoreValue)
        val beachScoreValue = findViewById<TextView>(R.id.beachScoreValue)

        waterLevelValue.text = waterLevel
        waterTempValue.text = waterTemp
        windValue.text = wind
        feeValue.text = fee
        parkingValue.text = parking
        disabledValue.text = disabledAccess
        restValue.text = restrooms
        algalScoreValue.text = algalScore
        beachScoreValue.text = beachScore

        val beachScoreDouble = beachScore.toDouble()
        if (beachScoreDouble >= 6.0) {
            beachScoreValue.setBackgroundColor(getColor(R.color.green))
        } else if (beachScoreDouble >= 5.0) {
            beachScoreValue.setBackgroundColor(getColor(R.color.orange))
        } else {
            beachScoreValue.setBackgroundColor(getColor(R.color.red))
        }
    }


    private fun setupNavigator() {
        val navBtn = findViewById<Button>(R.id.bt_navigate)
        navBtn.setOnClickListener {
//            val intent = Intent(this, NavigatorActivity::class.java)
//            intent.putExtra("EXTRA_X_COORDINATE", xCoordinate)
//            intent.putExtra("EXTRA_Y_COORDINATE", yCoordinate)
//            startActivity(intent)
            val lat = getLatForBeach()
            val long = getLongForBeach()
            if (lat != null) {
                if (long != null) {
                    navigateToBeach(lat, long)
                }
            }
        }
    }

    private fun getLatForBeach(): Double? {
        return when(beachNameString) {
            "Santa Barbara Beach" -> 34.4164
            "Malibu Beach" -> 34.0381
            "Venice Beach" -> 33.9850
            "Huntington Beach" -> 33.6595
            "Laguna Beach" -> 33.5427
            "La Jolla Beach" -> 32.8571
            else -> null
        }
    }

    private fun getLongForBeach(): Double? {
        return when(beachNameString) {
            "Santa Barbara Beach" -> -119.6706
            "Malibu Beach" -> -118.6923
            "Venice Beach" -> -118.4695
            "Huntington Beach" -> -117.9988
            "Laguna Beach" -> -117.7854
            "La Jolla Beach" -> -117.2575
            else -> null
        }
    }



    private fun navigateToBeach(latitude: Double, longitude: Double) {
        // Create a Uri with the destination latitude and longitude
//        val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")

        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=d")

        // Create an Intent with the Uri to open Google Maps
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps") // Ensure Google Maps is the default app

        // Check if there is a suitable app to handle the intent
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // If Google Maps is not installed, you can handle the situation here
            // For example, show a Toast or alert to inform the user to install Google Maps
        }
    }


}

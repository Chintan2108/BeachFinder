package com.example.beachfinder1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.Feature
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.mapping.view.ScreenCoordinate
import com.example.beachfinder1.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity: AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }


    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }


    private val beachesPointsLayer = "https://services8.arcgis.com/LLNIdHmmdjO2qQ5q/arcgis/rest/services/California_BasemapTEST_WFL1/FeatureServer/0"

    // create service feature table and a feature layer from it
    private val serviceFeatureTable = ServiceFeatureTable(beachesPointsLayer)
    private val featureLayer = FeatureLayer.createWithFeatureTable(serviceFeatureTable)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(mapView)

        setApiKey()

        setupMap()

        setupReport()
    }

    private fun setApiKey() {
        // It is not best practice to store API keys in source code. We have you insert one here
        // to streamline this tutorial.

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK25597a3bf3d644ada48c0336a764828aMG-y2LXN4sP3WFduPajIhgvQVBuzOqFYfKPGME9pfV8bCi190REbhS81NZu35yc5")
    }


    private fun setupMap() {

        // create a map with the base map type
        val baseMapStyle = ArcGISMap(BasemapStyle.ArcGISStreets).apply {
            // add the feature layer to the map's operational layers
            operationalLayers.add(featureLayer)
        }

        lifecycleScope.launch {

            mapView.apply {

                map = baseMapStyle

                // set an initial view point
                setViewpoint(
                    Viewpoint(
                        Envelope(
                            -13350000.0,
                            3890000.0,
                            -13020000.0,
                            4100000.0
                        )
                    )
                )

                // give any item selected on the map view a red selection halo
                selectionProperties.color = Color.red

                onSingleTapConfirmed.collect { tapEvent ->
                    // get the tapped coordinate
                    val screenCoordinate = tapEvent.screenCoordinate
                    getSelectedFeature(screenCoordinate)
                }
            }


        }
    }

    private suspend fun getSelectedFeature(screenCoordinate: ScreenCoordinate) {
        featureLayer.clearSelection()

        val tolerance = 25.0

        // create a IdentifyLayerResult using the screen coordinate
        val identifyLayerResult =
            mapView.identifyLayer(featureLayer, screenCoordinate, tolerance, false, 1)

        identifyLayerResult.apply {
            onSuccess { identifyLayerResult ->
                // get the elements in the selection that are features
                val features = identifyLayerResult.geoElements.filterIsInstance<Feature>()

                // add the features to the current feature layer selection
                featureLayer.selectFeatures(features)

                if (features.isNotEmpty()) {
                    val selectedFeature = features.first()
                    val beachName = selectedFeature.attributes["NAME"].toString()

                    navigateToDetailsActivity(beachName)
                }
            }
        }
    }


    private fun navigateToDetailsActivity(beachName: String) {
        val intent = Intent(this, BeachDetailsActivity::class.java)
        intent.putExtra("EXTRA_BEACH_NAME", beachName)
        startActivity(intent)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupReport() {
        val reportBtn = findViewById<Button>(R.id.bt_report)

        reportBtn.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            startActivity(intent)
        }
    }

}

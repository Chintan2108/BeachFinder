package com.example.beachfinder1

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.LocationDisplay
import com.arcgismaps.tasks.networkanalysis.DirectionManeuver
import com.arcgismaps.tasks.networkanalysis.RouteParameters
import com.arcgismaps.tasks.networkanalysis.RouteResult
import com.arcgismaps.tasks.networkanalysis.RouteTask
import com.arcgismaps.tasks.networkanalysis.Stop
import com.example.beachfinder1.databinding.ActivityNavigatorBinding
import kotlinx.coroutines.launch

class NavigatorActivity(): AppCompatActivity() {

    private var xCoordinate: Double? = null
    private var yCoordinate: Double? = null

    private lateinit var location: Location


    private val activityNavigatorBinding: ActivityNavigatorBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_navigator)
    }

    private val mapView by lazy {
        activityNavigatorBinding.mapView
    }

    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }


    private val directionsList: MutableList<String> by lazy {
        mutableListOf()
    }


    private val routeStops: MutableList<Stop> by lazy {
        mutableListOf()
    }

    private val graphicsOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(mapView)

        xCoordinate = intent.getDoubleExtra("EXTRA_X_COORDINATE", 0.0)
        yCoordinate = intent.getDoubleExtra("EXTRA_Y_COORDINATE", 0.0)

        // some parts of the API require an Android Context to properly interact with Android system
        // features, such as LocationProvider and application resources
        ArcGISEnvironment.applicationContext = applicationContext

        setApiKey()

        setupMap()
    }

    private fun findCurrentLocationAsPoint() {
        val point1 = locationDisplay.mapLocation
        if (point1 != null) {
            Log.d("Points", "point1 - ${point1.x} , ${point1.y}")
        }
        point1?.let { Stop(it) }?.let { addStop(it) }

        val point2 = xCoordinate?.let { yCoordinate?.let { it1 -> Point(it, it1) } }
        if (point2 != null) {
            Log.d("Points", "point2 - ${point2.x} , ${point2.y}")
        }
        point2?.let { Stop(it) }?.let { addStop(it) }
    }

    private fun setApiKey() {
        // It is not best practice to store API keys in source code. We have you insert one here
        // to streamline this tutorial.

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK25597a3bf3d644ada48c0336a764828aMG-y2LXN4sP3WFduPajIhgvQVBuzOqFYfKPGME9pfV8bCi190REbhS81NZu35yc5")
    }

    private fun setupMap() {
        // create a map with the BasemapStyle Topographic
        val map = ArcGISMap(BasemapStyle.ArcGISStreets)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.apply {

            graphicsOverlays.add(graphicsOverlay)

            lifecycleScope.launch {

                findCurrentLocationAsPoint()
            }

        }

        // LocationProvider requires an Android Context to properly interact with Android system
        ArcGISEnvironment.applicationContext = applicationContext

        // set the autoPanMode
//        locationDisplay.setAutoPanMode(LocationDisplayAutoPanMode.Recenter)

        lifecycleScope.launch {
            // start the map view's location display
            locationDisplay.dataSource.start()
                .onFailure {
                    // check permissions to see if failure may be due to lack of permissions
                    requestPermissions()
                }

            val point3 = locationDisplay.mapLocation
            if (point3 != null) {
                Log.d("Points", "point3 - ${point3.x} , ${point3.y}")
            }
        }
    }

    private fun requestPermissions() {
        // coarse location permission
        val permissionCheckCoarseLocation =
            ContextCompat.checkSelfPermission(
                this@NavigatorActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED
        // fine location permission
        val permissionCheckFineLocation =
            ContextCompat.checkSelfPermission(
                this@NavigatorActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED

        // if permissions are not already granted, request permission from the user
        if (!(permissionCheckCoarseLocation && permissionCheckFineLocation)) {
            ActivityCompat.requestPermissions(
                this@NavigatorActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                2
            )
        } else {
            // permission already granted, so start the location display
            lifecycleScope.launch {
                locationDisplay.dataSource.start()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if request is cancelled, the results array is empty
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch {
                locationDisplay.dataSource.start()
            }
        }

        else {
            Log.d("NavigatorActivity", "Permission denied")
        }

    }

    private fun addStop(stop: Stop) {

        routeStops.add(stop)

        // create a green circle symbol for the stop
        val stopMarker = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, Color.green, 20f)
        // get the stop's geometry
        val routeStopGeometry = stop.geometry
        // add graphic to graphics overlay
        graphicsOverlay.graphics.add(Graphic(routeStopGeometry, stopMarker))

    }

    private fun findRoute() {

        val routeTask = RouteTask(
            "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World"
        )

        lifecycleScope.launch {
            val routeParameters: RouteParameters = routeTask.createDefaultParameters().getOrElse { error ->
                return@launch
            }
            routeParameters.setStops(routeStops)
            routeParameters.returnDirections = true


            // get the route and display it
            val routeResult: RouteResult = routeTask.solveRoute(routeParameters).getOrElse { error ->
                return@launch
            }

            val routes = routeResult.routes
            if (routes.isNotEmpty()) {
                val route = routes[0]

                val shape = route.routeGeometry
                val routeGraphic = Graphic(
                    shape,
                    SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.green, 2f)
                )
                graphicsOverlay.graphics.add(routeGraphic)

                // get the direction text for each maneuver and display it as a list in the UI
                directionsList.clear()
                route.directionManeuvers.forEach { directionManeuver: DirectionManeuver ->
                    directionsList.add(directionManeuver.directionText)
                }

            }
        }

    }
}

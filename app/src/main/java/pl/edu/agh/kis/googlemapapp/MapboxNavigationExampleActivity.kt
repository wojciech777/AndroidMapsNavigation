package pl.edu.agh.kis.googlemapapp

import android.annotation.SuppressLint
import android.location.Location
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.widget.Button
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.android.core.permissions.PermissionsManager
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.activity_mapbox_navigation_example.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapboxNavigationExampleActivity: FragmentActivity(), OnMapReadyCallback, PermissionsListener,
        MapboxMap.OnMapClickListener, LocationEngineListener {
    /** MARK: - Properties */
    /** Adding location layer properties */
    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private var originLocation: Location? = null
    /** Adding a marker properties */
    private var destinationMarker: Marker? = null
    private var originCoordinate: LatLng? = null
    private var destinationCoordinate: LatLng? = null
    /** Calculating and drawing a route properties */
    private var originPoint: Point? = null
    private var destinationPoint: Point? = null
    private var currentRoute: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    /** View properties */
    private val mapView: MapView
        get() = mapboxViewXml
    private val startNavigationButton: Button
        get() = startNavigationButtonXml
    /** Listener flags properties */
    private var isSetMapClickListener = false
    /** MARK: - Activity Lifecycle */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_mapbox_navigation_example)
        mapView.onCreate(savedInstanceState)
    }
    public override fun onStart() {
        super.onStart()
        configureActions()
        configureMapView()
        mapView.onStart()
    }
    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
    /** MARK: - Private Action Methods */
    private fun startNavigationButtonAction() {
        val simulateRoute = true
        val options = NavigationLauncherOptions.builder().directionsRoute(currentRoute)
                .shouldSimulateRoute(simulateRoute).build()
        NavigationLauncher.startNavigation(this, options)
    }
    /** MARK: - Private Configure Methods */
    private fun configureActions() {
        startNavigationButton.setOnClickListener { startNavigationButtonAction() }
    }
    private fun configureMapView() {
        mapView.getMapAsync(this)
    }
    /** MARK: - Private Location Methods */
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(this)
            locationComponent.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }
    /** MARK: - Get Route Methods */
    private fun getRoute(origin: Point, destination: Point) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken()!!)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(object : Callback<DirectionsResponse> {
                    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                        // You can get the generic HTTP info about the response
                        val responseBody = response.body() ?: return
                        if (responseBody.routes().size >= 1) else return
                        currentRoute = responseBody.routes().first()
                        // Draw the route on the map
                        if (navigationMapRoute == null) {
                            navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute)
                        } else {
                            navigationMapRoute?.removeRoute()
                        }
                        navigationMapRoute?.addRoute(currentRoute)
                        // Activate start navigation button
                        startNavigationButton.isEnabled = true
                        startNavigationButton.setBackgroundResource(R.color.mapboxBlue)
                    }
                    override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {}
                })
    }
    /** MARK: - INTERFACE REALIZATION */
    /** MARK: - PermissionsListener */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }
    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }
    /** MARK: - LocationEngineListener */
    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            originLocation = location
            // Origin Location configuring
            originLocation?.let {
                originCoordinate = LatLng(it.latitude, it.longitude)
                if (!isSetMapClickListener) {
                    isSetMapClickListener = true
                    mapboxMap.addOnMapClickListener(this)
                }
            }
        }
    }

    override fun onConnected() {}
    /** MARK: - OnMapReadyCallback */
    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap != null) else { return }
        this.mapboxMap = mapboxMap
        enableLocationComponent()
        mapboxMap.locationComponent.locationEngine?.addLocationEngineListener(this)
    }
    /** MARK: - MapboxMap.OnMapClickListener */
    override fun onMapClick(clickCoorditate: LatLng) {
        if (destinationMarker != null) {
            mapboxMap.removeMarker(destinationMarker!!)
        }
        destinationCoordinate = clickCoorditate
        destinationMarker = mapboxMap.addMarker(MarkerOptions().position(destinationCoordinate))
        originCoordinate?.let { coordinate ->
            val tempOriginPoint = Point.fromLngLat(coordinate.longitude, coordinate.latitude)
            val tempDestinationPoint  =  Point.fromLngLat(clickCoorditate.longitude, clickCoorditate.latitude)
            originPoint = tempOriginPoint
            destinationPoint = tempDestinationPoint
            getRoute(tempOriginPoint, tempDestinationPoint)
        }
    }
}
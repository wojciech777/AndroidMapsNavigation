package pl.edu.agh.kis.googlemapapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {
    /** MARK: - Properties Maps */
    private var map: GoogleMap? = null
    private var animatePoints: ArrayList<LatLng> = arrayListOf()
    /** MARK: - Properties Timer */
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var currentAnimateIndex = 0
    /** MARK: - Properties permission */
    private var PERMISSIONS_REQUEST_LOCATION_FINE = 100
    private val isGrantedLocationPermission: Boolean
        get() {
            val fineLocationPermission = ActivityCompat.checkSelfPermission(this, permissionFineLocation)
            val coarseLocationPermission = ActivityCompat.checkSelfPermission(this, permissionCoarseLocation)
            return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        }
    private val permissionFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    /** MARK: - Activity Lifecycle */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    /** MARK: - MapFragment Lifecycle */
    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in Sydney and move the camera
        googleMap.setMinZoomPreference(6.0f)
        googleMap.setMaxZoomPreference(12.0f)
        /// Sydney Marker
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        /// Wellington Marker
        val wellington = LatLng(-41.0, 174.0)
        googleMap.addMarker(MarkerOptions().position(wellington).title("Marker in Wellington"))
        /// Animate to Sydney
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f), 5000, null)
        map = googleMap
        if (isGrantedLocationPermission) {
            configureMyLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(permissionFineLocation, permissionCoarseLocation),
                    PERMISSIONS_REQUEST_LOCATION_FINE)
        }
        /// Start delayed animation to Wellington
        startAnimationWith(arrayListOf(wellington, sydney))
    }
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }
    /** MARK: - Request Permission Methods */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION_FINE && isGrantedLocationPermission) {
            configureMyLocation()
        }
    }
    /** MARK: - Private Configure Methods */
    @SuppressLint("MissingPermission")
    private fun configureMyLocation() {
        map?.isMyLocationEnabled = true
        map?.setOnMyLocationButtonClickListener(this)
    }
    /** MARK: - Private Animate Methods */
    private fun startAnimationWith(points: ArrayList<LatLng>) {
        animatePoints = points
        timer = Timer()
        timerTask = object: TimerTask() {
            override fun run() {
                val point = animatePoints[currentAnimateIndex]
                incrementIndex()
                runOnUiThread {
                    map?.animateCamera(CameraUpdateFactory.newLatLng(point))
                }
            }
        }
        timer?.schedule(timerTask, 10000)
    }
    private fun incrementIndex() {
        val newIndex = currentAnimateIndex+1
        currentAnimateIndex = if (newIndex >= animatePoints.count()) 0 else newIndex
    }
}


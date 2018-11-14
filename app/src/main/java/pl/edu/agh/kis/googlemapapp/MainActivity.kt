package pl.edu.agh.kis.googlemapapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import android.widget.Button

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: FragmentActivity() {
    /** MARK: - Properties */
    private val androidMapsButton: Button
        get() = androidMapsButtonXml
    private val googleMapsIntentButton: Button
        get() = googleMapsIntentButtonXml
    private val mapboxNavigationButton: Button
        get() = mapboxNavigationButtonXml
    /** MARK: - Activity Lifecycle */
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
    }
    override fun onStart() {
        super.onStart()
        configureButtonActions()
    }
    /** MARK: - Action Methods */
    private fun applicationMapsExampleButtonAction() {
        startActivityByClass(MapsActivity::class.java)
    }
    private fun googleMapsIntentButtonAction() {
        startActivityByClass(GoogleMapsIntentExampleActivity::class.java)
    }
    private fun mapboxNavigationButtonAction() {

    }
    /** MARK: - Private Configuration Methods */
    private fun configureButtonActions() {
        androidMapsButton.setOnClickListener { applicationMapsExampleButtonAction() }
        googleMapsIntentButton.setOnClickListener { googleMapsIntentButtonAction() }
        mapboxNavigationButton.setOnClickListener { mapboxNavigationButtonAction() }
    }
    /** MARK: - Private Help Methods */
    private fun <T: Activity> startActivityByClass(clazz: Class<T>) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }
}
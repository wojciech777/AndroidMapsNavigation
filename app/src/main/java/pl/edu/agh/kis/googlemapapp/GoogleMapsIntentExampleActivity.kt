package pl.edu.agh.kis.googlemapapp

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_google_maps_example.*
import android.content.Intent
import android.net.Uri

/** MARK: Sealed classes */
sealed class ModeType {
    object Driving: ModeType()
    object Walking: ModeType()
    object Bicycling: ModeType()
    val modeKey: String
        get() {
            return when (this) {
                Driving -> "d"
                Walking -> "w"
                Bicycling -> "b"
            }
        }
}
sealed class AvoidType {
    object Tolls: AvoidType()
    object Highways: AvoidType()
    object Ferries: AvoidType()
    val avoidKey: String
        get() {
            return when (this) {
                Tolls -> "t"
                Highways -> "h"
                Ferries -> "f"
            }
        }
}

class GoogleMapsIntentExampleActivity: FragmentActivity() {
    /** MARK: - Properties */
    /** Views */
    /** Edit Text Views */
    private val addressEditText: EditText get() = addressEditTextXml
    private val longitudeEditText: EditText get() = longitudeEditTextXml
    private val latitudeEditText: EditText get() = latitudeEditTextXml
    /** Radio Button Views */
    private val drivingRadioButton: RadioButton get() = drivingRadioButtonXml
    private val walkingRadioButton: RadioButton get() = walkingRadioButtonXml
    private val bicyclingRadioButton: RadioButton get() = bicyclingRadioButtonXml
    private val modeRadioButtons: ArrayList<RadioButton>
        get() = arrayListOf(drivingRadioButton, walkingRadioButton, bicyclingRadioButton)
    /** Check Box Views */
    private val tollsCheckBox: CheckBox get() = tollsCheckBoxXml
    private val highwaysCheckBox: CheckBox get() = highwaysCheckBoxXml
    private val ferriesCheckBox: CheckBox get() = ferriesCheckBoxXml
    private val avoidCheckBoxes: ArrayList<CheckBox>
        get() = arrayListOf(tollsCheckBox, highwaysCheckBox, ferriesCheckBox)
    /** Buttons Views */
    private val navigateByAddressButton: Button get() = navigateAddressButtonXml
    private val navigateByCoordinatesButton: Button get() = navigateCoordinatesButtonXml
    /** Private Properties */
    private val uriBase: String = "google.navigation:q="
    private var modeType: ModeType = ModeType.Driving
    private var avoidType: MutableMap<AvoidType, Boolean> = mutableMapOf()
    /** MARK: - Activity Lifecycle */
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_google_maps_example)
    }
    override fun onStart() {
        super.onStart()
        configureButtonActions()
    }
    /** MARK: - Action Methods */
    private fun navigateByAddressButtonAction() {
        startGoogleMapsIntent(buildUriForAddress())
    }
    private fun navigateByCoordinatesButtonAction() {
        startGoogleMapsIntent(buildUriForCoordinates())
    }
    private fun modeRadioButtonChecked(button: RadioButton, isChecked: Boolean) {
        when (button) {
            drivingRadioButton -> modeType = ModeType.Driving
            walkingRadioButton -> modeType = if (isChecked) ModeType.Walking else ModeType.Driving
            bicyclingRadioButton -> modeType = if (isChecked) ModeType.Bicycling else ModeType.Driving
        }
    }
    private fun avoidCheckBoxChecked(button: CheckBox, isChecked: Boolean) {
        when (button) {
            tollsCheckBox -> avoidType[AvoidType.Tolls] = isChecked
            highwaysCheckBox -> avoidType[AvoidType.Highways] = isChecked
            ferriesCheckBox -> avoidType[AvoidType.Ferries] = isChecked
        }
    }
    /** MARK: - Private Configuration Methods */
    private fun configureButtonActions() {
        navigateByAddressButton.setOnClickListener { navigateByAddressButtonAction() }
        navigateByCoordinatesButton.setOnClickListener { navigateByCoordinatesButtonAction() }
        modeRadioButtons.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                val radioButton = buttonView as RadioButton
                modeRadioButtonChecked(radioButton, isChecked)
            }
        }
        avoidCheckBoxes.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                val checkBox = buttonView as CheckBox
                avoidCheckBoxChecked(checkBox, isChecked)
            }
        }
    }
    /** MARK: - Private Help Methods */
    private fun startGoogleMapsIntent(uriString: String) {
        val gmmIntentUri = Uri.parse(uriString)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
    private fun buildUriForAddress(): String {
        val address = addressEditText.text
                .split(" ")
                .joinToString("+")
        return uriBase + address + modeQueryBuild() + avoidQueryBuild()
    }
    private fun buildUriForCoordinates(): String {
        val coordinatesString = "${latitudeEditText.text},${longitudeEditText.text}"
        return uriBase + coordinatesString + modeQueryBuild() + avoidQueryBuild()
    }
    private fun modeQueryBuild(): String {
        return "&mode=${modeType.modeKey}"
    }
    private fun avoidQueryBuild(): String {
        var avoidBase = ""
        avoidType.forEach {
            if (it.value) { avoidBase += it.key.avoidKey }
        }
        return if (avoidBase.isNotEmpty()) { "&avoid=$avoidBase" } else { "" }
    }
}
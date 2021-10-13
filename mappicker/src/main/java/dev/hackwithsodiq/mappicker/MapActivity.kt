package dev.hackwithsodiq.mappicker

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*


class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private var mLocation:Location? = null
    private var mMap: GoogleMap? = null
    private var geoCoder:Geocoder? = null
    private var mapLocationMarker:Marker? = null
    private val CAMERA_ZOOM_LEVEL = 16F
    private var activityResultLauncher:ActivityResultLauncher<Intent>? = null
    private var autoCompleteIntent:Intent? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mGCPApiKey = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        val app = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        mGCPApiKey = bundle.getString("com.google.android.geo.API_KEY") ?: ""
        if (mGCPApiKey.isNullOrEmpty()){
            returnResult(false, getString(R.string.mappicker_no_api_key), null)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        geoCoder = Geocoder(this, Locale.getDefault())

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val placeData = Autocomplete.getPlaceFromIntent(it.data ?: return@registerForActivityResult)
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val latLng = placeData.latLng ?: return@registerForActivityResult
                    mLocation = Location(latLng, placeData.name?:"", placeData.address?:"")
                    showMarkerOnMap(mLocation)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val intentData = it.data ?: return@registerForActivityResult
                    val status = Autocomplete.getStatusFromIntent(intentData)
                    Toast.makeText(this, status.statusMessage, Toast.LENGTH_LONG).show()
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                    Toast.makeText(this, getString(R.string.mappicker_operation_cancelled), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Initialize the SDK
        if(!Places.isInitialized()) Places.initialize(this, mGCPApiKey)
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        autoCompleteIntent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful){
                    val response = it.result
                    mLocation = getLocationWithLatLong(LatLng(response.latitude, response.longitude))
                    showMarkerOnMap(mLocation)
                }
            }
        }

        findViewById<Button>(R.id.btn_select_location).setOnClickListener {
            val isOkay = mLocation != null
            val errorString = if (!isOkay) getString(R.string.mappicker_error_fetching_location) else ""
            returnResult(isOkay, errorString, mLocation)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search_location){
            activityResultLauncher?.launch (autoCompleteIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setOnMarkerDragListener(this)
        val startPoint = LatLng(0.0, 0.0)
        showMarkerOnMap(Location(startPoint, "", ""))
    }

    override fun onMarkerDragStart(marker: Marker) {}

    override fun onMarkerDrag(marker: Marker) {}

    override fun onMarkerDragEnd(marker: Marker) {
        val markerLatLng = marker.position
        mLocation = getLocationWithLatLong(markerLatLng)
        showMarkerOnMap(mLocation)
    }

    private fun returnResult(isOkay:Boolean, errorString:String, data:Location?){
        val callerIntent = Intent()
        if (isOkay){
            callerIntent.putExtra(K.LOCATION_DATA, data)
            setResult(Activity.RESULT_OK, callerIntent)
        }else{
            callerIntent.putExtra(K.LOCATION_DATA, errorString)
            setResult(Activity.RESULT_CANCELED, callerIntent)
        }
        finish()
    }

    private fun showMarkerOnMap(location:Location?){
        val locationInfo = location?: return
        mapLocationMarker?.remove()
        mMap?.clear()
        val markerOption = MarkerOptions().position(locationInfo.latLng)
        mMap?.setInfoWindowAdapter(MarkerInfoWindowAdapter(this, locationInfo.name, locationInfo.address))
        mapLocationMarker = mMap?.addMarker(markerOption ?: return)?.apply {
            isDraggable = true
            showInfoWindow()
        }
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(locationInfo.latLng, CAMERA_ZOOM_LEVEL))
    }

    private fun getLocationWithLatLong(latLng: LatLng):Location{
        val info = geoCoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)?.get(0)
        return Location(latLng, info?.featureName?:"", info?.getAddressLine(0)?:"")
    }
}
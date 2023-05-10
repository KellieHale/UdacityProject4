package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private val TAG = SelectLocationFragment::class.java.simpleName
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var homeLatLng = LatLng(latitude, longitude)
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { checkLocationPermissions() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        checkLocationPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions() {
        if (isPermissionGranted()) {
            val zoomLevel = 20f
            map.addMarker(MarkerOptions().position(homeLatLng))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
            showMyLocationOnMap()
            map.isMyLocationEnabled = true
            map.setOnMyLocationClickListener(this)
            map.setOnMyLocationButtonClickListener(this)

        } else if (!isPermissionGranted()) {
            enableMyLocation()
            }
        isLocationAvailableOnDevice()
        setPoiClick(map)
        setLongClickPoi(map)
        setMapStyle(map)
        getLocationUpdates()
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (isLocationAvailableOnDevice()) {
            Snackbar.make(binding.root, getString(R.string.current_location), Snackbar.LENGTH_SHORT).show()
        }
        else {
            Snackbar.make(binding.root, "Current Location Unavailable. Please Enable Location", Snackbar.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Snackbar.make(binding.root, "Current Location is \n$location", Snackbar.LENGTH_SHORT).show()
    }
    private fun setLongClickPoi(map: GoogleMap) {
        map.setOnMapLongClickListener { latlng ->
            val randomMarker = map.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .title(getString(R.string.poi))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )
            randomMarker?.showInfoWindow()
            onLocationSelected(requireContext(), latlng.toString())
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
            poiMarker?.showInfoWindow()
            onLocationSelected(requireContext(), poi.name)
        }
    }

    private fun onLocationSelected(context: Context, poiName: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Location Selected")
        builder.setMessage("You have chosen $poiName for a Location Reminder.")
        builder.setNeutralButton("Deny", null)
        builder.setPositiveButton("Accept") { dialog, _ ->
            dialog.dismiss()
            setFragmentResult("location", bundleOf("location" to poiName, "latitude" to latitude, "longitude" to longitude))
            activity?.supportFragmentManager?.popBackStack()
        }
        builder.show()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.mapstyle
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    //Permission Requests
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun showMyLocationOnMap() {
        if (isLocationAvailableOnDevice()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    val currentLocation = LatLng(latitude, longitude)
                    map.isMyLocationEnabled = true
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5f))
                }
        }
    }

    private fun isLocationAvailableOnDevice(): Boolean {
        val locationManager: LocationManager = requireActivity().
        getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            showMyLocationOnMap()
        }
        else {
            requestMultiplePermissions.launch(
                arrayOf(
                    ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.intervalMillis
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 100f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.locations.isNotEmpty()) {
                    val location = p0.lastLocation
                    location?.latitude
                    location?.longitude
                }
            }
        }
    }


}

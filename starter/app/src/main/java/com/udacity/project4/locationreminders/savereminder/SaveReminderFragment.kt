package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.Builder
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.viewModel = _viewModel

        setFragmentResultListener("location") { _, bundle ->
            Log.d("SaveReminderFragment", "fragmentResultListener")
            val location = bundle.getString("location")
            Log.d("SaveReminderFragment", "Location: $location")
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            if (location!!.isNotEmpty()) {
                _viewModel.reminderSelectedLocationStr.value = location
                _viewModel.latitude.value = latitude
                _viewModel.longitude.value = longitude
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            if (latitude != null && longitude != null) {
                val latLng = LatLng(latitude, longitude)



                createGeofence(latLng, ReminderDataItem(title, description, location, latitude, longitude))
            }
        }

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun createGeofence(
        latLng: LatLng,
        reminderDataItem: ReminderDataItem) {

        val geofence = Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        Log.e("Add Geofence", geofencingRequest.geofences.toString())
                        Toast.makeText(requireActivity(), "Geofence Added!", Toast.LENGTH_SHORT).show()
                        _viewModel.validateAndSaveReminder(reminderDataItem)
                    }
                    addOnFailureListener {
                        it.printStackTrace()
                        Toast.makeText(
                            requireActivity(),
                            resources.getString(R.string.geofences_not_added),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }




    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }
}

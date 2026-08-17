package com.rescuex.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rescuex.data.model.LocationData
import kotlinx.coroutines.tasks.await

class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? {
        return try {
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            location?.let {
                LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    address = "Lat: ${it.latitude}, Lon: ${it.longitude}" // Placeholder for reverse geocoding
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

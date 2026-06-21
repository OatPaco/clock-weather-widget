package com.oat.clockweather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    suspend fun getLocation(context: Context): Location? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        return suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location)
                    } else {
                        // Fallback to last known
                        client.lastLocation.addOnSuccessListener { last ->
                            cont.resume(last)
                        }.addOnFailureListener {
                            cont.resume(null)
                        }
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }

            cont.invokeOnCancellation { cts.cancel() }
        }
    }

    fun getCityName(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Unknown"
            } else {
                // Fallback to Open-Meteo timezone parsing
                WeatherService.reverseGeocode(lat, lon)
            }
        } catch (e: Exception) {
            WeatherService.reverseGeocode(lat, lon)
        }
    }
}

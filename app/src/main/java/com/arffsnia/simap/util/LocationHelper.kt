package com.arffsnia.simap.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale

/** Baca lokasi GPS terakhir (tanpa Play Services, pakai LocationManager). */
object LocationHelper {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun lastKnown(context: Context): Pair<Double, Double>? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val providers = mutableListOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        if (Build.VERSION.SDK_INT >= 31) providers.add(0, LocationManager.FUSED_PROVIDER)

        var best: Pair<Double, Double>? = null
        for (p in providers) {
            try {
                val loc = lm.getLastKnownLocation(p) ?: continue
                best = loc.latitude to loc.longitude
                if (loc.accuracy <= 200f) break // sudah cukup akurat
            } catch (_: Exception) {
            }
        }
        return best
    }

    fun format(pair: Pair<Double, Double>?): String =
        if (pair == null) "-"
        else String.format(Locale.US, "%.6f, %.6f", pair.first, pair.second)
}
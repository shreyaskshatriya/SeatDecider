package com.example.bestseatfinder.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    /**
     * Checks if a network connection is available.
     * Requires ACCESS_NETWORK_STATE permission.
     *
     * @param context Context to access system services.
     * @return True if a network connection is available, false otherwise.
     */
    @Suppress("DEPRECATION") // For activeNetworkInfo on older APIs
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            ?: return false // Should not happen, but good to be safe

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            // For older APIs (below Marshmallow)
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected &&
                   (networkInfo.type == ConnectivityManager.TYPE_WIFI ||
                    networkInfo.type == ConnectivityManager.TYPE_MOBILE)
        }
    }
}

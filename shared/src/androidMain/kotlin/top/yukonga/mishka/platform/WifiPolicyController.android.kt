package top.yukonga.mishka.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

actual class WifiPolicyController actual constructor(private val context: PlatformContext) {

    actual fun hasRequiredPermission(): Boolean = AndroidWifiPolicy.hasRequiredPermission(context)

    actual fun currentSsid(): String? = AndroidWifiPolicy.currentSsid(context)

    actual fun startMonitor() {
        context.startForegroundService(buildIntent(WifiPolicyIntents.ACTION_START))
    }

    actual fun stopMonitor() {
        context.startService(buildIntent(WifiPolicyIntents.ACTION_STOP))
    }

    actual fun evaluateNow() {
        context.startForegroundService(buildIntent(WifiPolicyIntents.ACTION_EVALUATE))
    }

    private fun buildIntent(action: String): Intent = Intent().apply {
        setClassName(context.packageName, "top.yukonga.mishka.service.WifiPolicyMonitorService")
        this.action = action
    }
}

object AndroidWifiPolicy {
    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun hasRequiredPermission(context: Context): Boolean =
        requiredPermissions().all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

    fun currentSsid(context: Context): String? {
        if (!hasRequiredPermission(context)) return null
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = connectivity?.getNetworkCapabilities(connectivity.activeNetwork)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiInfo = capabilities.transportInfo as? WifiInfo
            normalizeSsid(wifiInfo?.ssid)?.let { return it }
        }
        @Suppress("DEPRECATION")
        val fallback = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
            ?.connectionInfo
            ?.ssid
        return normalizeSsid(fallback)
    }

    fun normalizeSsid(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        if (trimmed == WifiManager.UNKNOWN_SSID || trimmed.equals("<unknown ssid>", ignoreCase = true)) return null
        return if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }.takeIf { it.isNotEmpty() }
    }
}

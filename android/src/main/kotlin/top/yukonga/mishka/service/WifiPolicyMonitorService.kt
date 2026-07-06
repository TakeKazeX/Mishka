package top.yukonga.mishka.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import top.yukonga.mishka.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.mishka.platform.AndroidWifiPolicy
import top.yukonga.mishka.platform.PlatformStorage
import top.yukonga.mishka.platform.ProxyServiceBridge
import top.yukonga.mishka.platform.ProxyState
import top.yukonga.mishka.platform.ProxyServiceController
import top.yukonga.mishka.platform.StorageKeys
import top.yukonga.mishka.platform.WifiPolicyAction
import top.yukonga.mishka.platform.WifiPolicyIntents

class WifiPolicyMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val storage by lazy { PlatformStorage(this) }
    private val controller by lazy { ProxyServiceController(this) }
    private val connectivity by lazy { getSystemService(ConnectivityManager::class.java) }

    private var networkCallbackRegistered = false
    private var evaluateJob: Job? = null
    private var pendingModeRestart: String? = null
    private var pendingModeNotificationResId: Int? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = scheduleEvaluate()
        override fun onLost(network: Network) = scheduleEvaluate()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = scheduleEvaluate()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        if (!startMonitorForeground()) {
            stopSelf()
            return
        }
        updateMonitorNotificationVisibility()
        registerNetworkCallback()
        scope.launch {
            ProxyServiceBridge.state.collect { status ->
                if (status.state == ProxyState.Running) {
                    if (consumePendingModeRestart()) return@collect
                    clearRestoredRuleOverrideIfNeeded()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            WifiPolicyIntents.ACTION_STOP -> {
                scope.launch { disablePolicyAndStop() }
                return START_NOT_STICKY
            }

            WifiPolicyIntents.ACTION_START,
            WifiPolicyIntents.ACTION_EVALUATE,
            null -> {
                updateMonitorNotificationVisibility()
                scheduleEvaluate(delayMs = 0)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterNetworkCallback()
        evaluateJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun registerNetworkCallback() {
        if (networkCallbackRegistered) return
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        runCatching {
            connectivity?.registerNetworkCallback(request, networkCallback)
            networkCallbackRegistered = true
        }.onFailure { Log.w(TAG, "registerNetworkCallback failed", it) }
    }

    private fun unregisterNetworkCallback() {
        if (!networkCallbackRegistered) return
        runCatching { connectivity?.unregisterNetworkCallback(networkCallback) }
        networkCallbackRegistered = false
    }

    private fun scheduleEvaluate(delayMs: Long = 300) {
        evaluateJob?.cancel()
        evaluateJob = scope.launch {
            if (delayMs > 0) delay(delayMs)
            evaluatePolicy()
        }
    }

    private fun startMonitorForeground(): Boolean {
        return try {
            val notification = NotificationHelper.buildWifiPolicyServiceNotification(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_WIFI_POLICY,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID_WIFI_POLICY, notification)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
            false
        }
    }

    private fun updateMonitorNotificationVisibility() {
        val hideNotification = storage.getString(
            StorageKeys.WIFI_POLICY_HIDE_MONITOR_NOTIFICATION,
            "false",
        ) == "true"
        if (hideNotification) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            startMonitorForeground()
        }
    }

    private suspend fun evaluatePolicy() {
        if (storage.getString(StorageKeys.WIFI_POLICY_ENABLED, "false") != "true") {
            if (storage.getString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false") != "true") {
                stopSelf()
            }
            return
        }
        if (!AndroidWifiPolicy.hasRequiredPermission(this)) {
            clearMatchedState(restoreService = true)
            return
        }

        val currentSsid = AndroidWifiPolicy.currentSsid(this)
        val matchedSsids = storage.getStringSet(StorageKeys.WIFI_POLICY_SSIDS, emptySet())
        val matched = currentSsid != null && currentSsid in matchedSsids
        val wasMatched = storage.getString(StorageKeys.WIFI_POLICY_MATCHED, "false") == "true"
        val action = WifiPolicyAction.fromStorage(storage.getString(StorageKeys.WIFI_POLICY_ACTION, ""))
        val previousAction = WifiPolicyAction.fromStorage(storage.getString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, ""))

        if (matched && (!wasMatched || previousAction != action)) {
            if (wasMatched && previousAction != action) {
                leaveMatched(previousAction, restoreService = false)
            }
            storage.putString(StorageKeys.WIFI_POLICY_MATCHED, "true")
            storage.putString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, action.storageValue)
            enterMatched(action)
        } else if (!matched && wasMatched) {
            clearMatchedState(restoreService = true)
        } else if (matched && action == WifiPolicyAction.DirectMode) {
            if (storage.getString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "") != "direct") {
                ensureDirectMode()
            }
        }
    }

    private suspend fun clearMatchedState(restoreService: Boolean) {
        val wasMatched = storage.getString(StorageKeys.WIFI_POLICY_MATCHED, "false") == "true"
        if (!wasMatched) return
        val previousAction = WifiPolicyAction.fromStorage(storage.getString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, ""))
        storage.putString(StorageKeys.WIFI_POLICY_MATCHED, "false")
        storage.putString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, "")
        leaveMatched(previousAction, restoreService)
    }

    private suspend fun enterMatched(action: WifiPolicyAction) {
        when (action) {
            WifiPolicyAction.StopService -> {
                if (isProxyActive()) {
                    storage.putString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "true")
                    controller.stop()
                    notifySwitch(R.string.notification_wifi_policy_stop_enter)
                }
            }

            WifiPolicyAction.DirectMode -> {
                storage.putString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false")
                if (ensureDirectMode(R.string.notification_wifi_policy_direct_enter)) {
                    notifySwitch(R.string.notification_wifi_policy_direct_enter)
                }
            }
        }
    }

    private suspend fun leaveMatched(action: WifiPolicyAction, restoreService: Boolean) {
        when (action) {
            WifiPolicyAction.StopService -> {
                if (restoreService && storage.getString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false") == "true") {
                    storage.putString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false")
                    controller.start()
                    notifySwitch(R.string.notification_wifi_policy_stop_leave)
                } else {
                    storage.putString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false")
                }
            }

            WifiPolicyAction.DirectMode -> {
                storage.putString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "rule")
                if (restoreService && isProxyActive()) {
                    if (restartOrQueueModeRestart("rule", R.string.notification_wifi_policy_direct_leave)) {
                        notifySwitch(R.string.notification_wifi_policy_direct_leave)
                    }
                }
            }
        }
    }

    private suspend fun ensureDirectMode(notificationResId: Int? = null): Boolean {
        storage.putString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "direct")
        if (!isProxyActive()) return false
        return restartOrQueueModeRestart("direct", notificationResId)
    }

    private fun restartOrQueueModeRestart(mode: String, notificationResId: Int?): Boolean {
        val status = ProxyServiceBridge.state.value
        if (status.state == ProxyState.Running) {
            pendingModeRestart = null
            pendingModeNotificationResId = null
            return restartRunningServiceForWifiMode(mode)
        }
        if (status.state == ProxyState.Starting || status.state == ProxyState.Stopping) {
            pendingModeRestart = mode
            pendingModeNotificationResId = notificationResId
            Log.i(TAG, "Queued service restart for Wi-Fi policy mode=$mode while state=${status.state}")
        }
        return false
    }

    private fun consumePendingModeRestart(): Boolean {
        val mode = pendingModeRestart ?: return false
        val runtimeMode = storage.getString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "")
        if (runtimeMode != mode) {
            pendingModeRestart = null
            pendingModeNotificationResId = null
            return false
        }
        val notificationResId = pendingModeNotificationResId
        pendingModeRestart = null
        pendingModeNotificationResId = null
        val restarted = restartRunningServiceForWifiMode(mode)
        if (restarted && notificationResId != null) {
            notifySwitch(notificationResId)
        }
        return restarted
    }

    private fun restartRunningServiceForWifiMode(mode: String): Boolean {
        val status = ProxyServiceBridge.state.value
        if (status.state != ProxyState.Running) return false
        controller.restart()
        Log.i(TAG, "Restarted service for Wi-Fi policy mode=$mode")
        return true
    }

    private fun clearRestoredRuleOverrideIfNeeded() {
        val matched = storage.getString(StorageKeys.WIFI_POLICY_MATCHED, "false") == "true"
        val runtimeMode = storage.getString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "")
        if (!matched && runtimeMode == "rule") {
            storage.putString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "")
        }
    }

    private suspend fun disablePolicyAndStop() {
        val wasMatched = storage.getString(StorageKeys.WIFI_POLICY_MATCHED, "false") == "true"
        val previousAction = WifiPolicyAction.fromStorage(storage.getString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, ""))
        storage.putString(StorageKeys.WIFI_POLICY_ENABLED, "false")
        storage.putString(StorageKeys.WIFI_POLICY_MATCHED, "false")
        storage.putString(StorageKeys.WIFI_POLICY_MATCHED_ACTION, "")
        storage.putString(StorageKeys.WIFI_POLICY_PENDING_RESTART, "false")
        storage.putString(StorageKeys.WIFI_POLICY_RUNTIME_MODE, "")
        if (wasMatched && previousAction == WifiPolicyAction.DirectMode && isProxyActive()) {
            restartRunningServiceForWifiMode("user")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun isProxyActive(): Boolean = when (ProxyServiceBridge.state.value.state) {
        ProxyState.Running, ProxyState.Starting, ProxyState.Stopping -> true
        ProxyState.Stopped, ProxyState.Error -> false
    }

    private fun notifySwitch(contentResId: Int) {
        if (storage.getString(StorageKeys.WIFI_POLICY_NOTIFY_SWITCH, "true") == "false") return
        NotificationHelper.notifyWifiPolicyEvent(this, contentResId)
    }

    companion object {
        private const val TAG = "WifiPolicyMonitor"
    }
}

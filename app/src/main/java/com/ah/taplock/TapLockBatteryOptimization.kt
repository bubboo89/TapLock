package com.ah.taplock

import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

object TapLockBatteryOptimization {
    // User-initiated recovery option; rationale and evidence limits are in
    // docs/BATTERY_EXEMPTION.md. Keep the policy inspection visible.
    const val REQUEST_ACTION = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    const val SETTINGS_ACTION = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS

    fun packageUriString(packageName: String): String = "package:$packageName"

    fun requestIntent(packageName: String): Intent =
        Intent(REQUEST_ACTION).apply {
            data = packageUriString(packageName).toUri()
        }

    fun settingsIntent(): Intent = Intent(SETTINGS_ACTION)
}

package com.ah.taplock

internal object TapLockSystemUi {
    // Resource IDs, rather than translated labels, also work in non-English SystemUI.
    private val lockScreenIds = setOf(
        "com.android.systemui:id/keyguard_header",
        "com.android.systemui:id/keyguard_status_view",
        "com.android.systemui:id/keyguard_clock_container"
    )
    private val shadeIds = setOf(
        "com.android.systemui:id/quick_qs_panel",
        "com.android.systemui:id/quick_settings_panel",
        "com.android.systemui:id/qs_panel",
        "com.android.systemui:id/shade_header_root"
    )

    private val credentialIds = setOf(
        "element:BouncerContent",
        "com.android.systemui:id/keyguard_security_container",
        "com.android.systemui:id/keyguard_bouncer",
        "com.android.systemui:id/pinEntry",
        "com.android.systemui:id/passwordEntry",
        "com.android.systemui:id/lockPatternView"
    )

    fun hasLockScreenContent(visibleIds: Set<String>): Boolean =
        visibleIds.any { it in lockScreenIds }

    fun isPanelOpen(
        keyguardLocked: Boolean,
        className: String,
        title: String,
        visibleIds: Set<String>
    ): Boolean {
        // During a transition both scenes can be visible. Give SystemUI the touches.
        if (visibleIds.any { it in shadeIds || it in credentialIds }) return true
        if (keyguardLocked && hasLockScreenContent(visibleIds)) return false
        // Retain the fallback for older/OEM shade implementations and the PIN screen.
        return listOf(className, title).any { identifier ->
            identifier.contains("NotificationShade", ignoreCase = true) ||
                identifier.contains("NotificationPanel", ignoreCase = true) ||
                identifier.contains("QuickSettings", ignoreCase = true)
        }
    }
}

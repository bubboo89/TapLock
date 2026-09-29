package com.ah.taplock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapLockSystemUiTest {
    private fun classify(vararg ids: String, locked: Boolean = true) =
        TapLockSystemUi.isPanelOpen(
            locked, "android.widget.FrameLayout", "NotificationShade", ids.toSet()
        )

    @Test
    fun sharedWindowWithLockScreenScene_isNotExpandedShade() {
        assertFalse(classify("element:lockscreen", "com.android.systemui:id/keyguard_header"))
    }

    @Test
    fun emptyLockScreenSceneBehindCredentials_keepsSystemUiTouchable() {
        assertTrue(classify("element:lockscreen"))
    }

    @Test
    fun legacyKeyguardClock_isNotExpandedShade() {
        assertFalse(classify("com.android.systemui:id/keyguard_clock_container"))
    }

    @Test
    fun quickSettingsOverKeyguard_keepsSystemUiTouchable() {
        assertTrue(classify("element:lockscreen", "com.android.systemui:id/quick_qs_panel"))
    }

    @Test
    fun pinScreenOverLockScreenScene_keepsSystemUiTouchable() {
        assertTrue(classify("element:lockscreen", "element:BouncerContent"))
    }

    @Test
    fun unlockedShade_doesNotTreatStaleClockAsKeyguard() {
        assertTrue(classify("com.android.systemui:id/keyguard_clock_container", locked = false))
    }

    @Test
    fun unrelatedSystemWindow_isNotExpandedShade() {
        assertFalse(
            TapLockSystemUi.isPanelOpen(false, "android.widget.FrameLayout", "Status bar", emptySet())
        )
    }
}

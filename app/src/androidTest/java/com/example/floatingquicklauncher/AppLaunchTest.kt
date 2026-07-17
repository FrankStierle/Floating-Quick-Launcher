package com.example.floatingquicklauncher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deniedPermissionsShowActions() {
        var overlaySettingsOpened = false
        var notificationPermissionRequested = false

        composeRule.setContent {
            LauncherScreen(
                overlayPermissionGranted = false,
                notificationPermissionRequired = true,
                notificationPermissionGranted = false,
                onOpenOverlaySettings = { overlaySettingsOpened = true },
                onRequestNotificationPermission = { notificationPermissionRequested = true },
                onStartService = {},
                onStopService = {},
            )
        }

        composeRule.onNodeWithText("Floating Quick Launcher").assertIsDisplayed()
        composeRule.onNodeWithText("Open overlay settings").performClick()
        composeRule.onNodeWithText("Allow notifications").performClick()

        assertTrue(overlaySettingsOpened)
        assertTrue(notificationPermissionRequested)
    }

    @Test
    fun notificationActionIsHiddenWhenPermissionIsNotRequired() {
        var serviceStarted = false
        var serviceStopped = false

        composeRule.setContent {
            LauncherScreen(
                overlayPermissionGranted = true,
                notificationPermissionRequired = false,
                notificationPermissionGranted = true,
                onOpenOverlaySettings = {},
                onRequestNotificationPermission = {},
                onStartService = { serviceStarted = true },
                onStopService = { serviceStopped = true },
            )
        }

        composeRule.onNodeWithText("Not required on this Android version").assertIsDisplayed()
        composeRule.onNodeWithText("Start service").performClick()
        composeRule.onNodeWithText("Stop service").performClick()

        assertTrue(serviceStarted)
        assertTrue(serviceStopped)
    }
}

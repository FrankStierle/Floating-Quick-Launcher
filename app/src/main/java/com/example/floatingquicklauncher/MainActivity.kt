package com.example.floatingquicklauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.floatingquicklauncher.ui.theme.FloatingQuickLauncherTheme

class MainActivity : ComponentActivity() {
    private var overlayPermissionGranted by mutableStateOf(false)
    private var notificationPermissionGranted by mutableStateOf(false)

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshPermissionState()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshPermissionState()
        setContent {
            FloatingQuickLauncherTheme {
                LauncherScreen(
                    overlayPermissionGranted = overlayPermissionGranted,
                    notificationPermissionRequired = isNotificationPermissionRequired,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onOpenOverlaySettings = ::openOverlaySettings,
                    onRequestNotificationPermission = ::requestNotificationPermission,
                    onStartService = ::startOverlayService,
                    onStopService = ::stopOverlayService,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private val isNotificationPermissionRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun refreshPermissionState() {
        overlayPermissionGranted = Settings.canDrawOverlays(this)
        notificationPermissionGranted = !isNotificationPermissionRequired ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun requestNotificationPermission() {
        if (isNotificationPermissionRequired) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startOverlayService() {
        if (Settings.canDrawOverlays(this)) {
            ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
        }
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
    }
}

@Composable
internal fun LauncherScreen(
    overlayPermissionGranted: Boolean,
    notificationPermissionRequired: Boolean,
    notificationPermissionGranted: Boolean,
    onOpenOverlaySettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
            )

            PermissionStatus(
                title = stringResource(R.string.overlay_permission_title),
                isGranted = overlayPermissionGranted,
            )
            Button(onClick = onOpenOverlaySettings) {
                Text(text = stringResource(R.string.open_overlay_settings))
            }

            PermissionStatus(
                title = stringResource(R.string.notification_permission_title),
                isGranted = notificationPermissionGranted,
                isRequired = notificationPermissionRequired,
            )
            if (notificationPermissionRequired && !notificationPermissionGranted) {
                Button(onClick = onRequestNotificationPermission) {
                    Text(text = stringResource(R.string.request_notification_permission))
                }
            }

            Text(
                text = stringResource(R.string.service_controls_title),
                style = MaterialTheme.typography.titleMedium,
            )
            if (!overlayPermissionGranted) {
                Text(text = stringResource(R.string.service_requires_overlay_permission))
            }
            Button(
                onClick = onStartService,
                enabled = overlayPermissionGranted,
            ) {
                Text(text = stringResource(R.string.start_service))
            }
            OutlinedButton(onClick = onStopService) {
                Text(text = stringResource(R.string.stop_service))
            }
        }
    }
}

@Composable
private fun PermissionStatus(
    title: String,
    isGranted: Boolean,
    isRequired: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = when {
                !isRequired -> stringResource(R.string.permission_not_required)
                isGranted -> stringResource(R.string.permission_granted)
                else -> stringResource(R.string.permission_not_granted)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LauncherScreenPreview() {
    FloatingQuickLauncherTheme {
        LauncherScreen(
            overlayPermissionGranted = false,
            notificationPermissionRequired = true,
            notificationPermissionGranted = false,
            onOpenOverlaySettings = {},
            onRequestNotificationPermission = {},
            onStartService = {},
            onStopService = {},
        )
    }
}

package com.yash.Speachr.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yash.Speachr.ui.components.MainViewModel
import com.yash.Speachr.ui.components.OverlayPermissionTextProvider
import com.yash.Speachr.ui.components.PermissionDialog
import com.yash.Speachr.ui.components.RecordAudioPermissionTextProvider
import com.yash.Speachr.ui.components.isPermanentlyDeclined
import com.yash.Speachr.ui.theme.SpeachrTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var overlayGranted by remember {
        mutableStateOf(
            Settings.canDrawOverlays(context)
        )
    }

    val viewModel = viewModel<MainViewModel>()
    val dialogQueue = viewModel.visiblePermissionDialogQueue


    val multiPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            perms.keys.forEach { permission ->
                micGranted = perms[permission] == true
                viewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpeachrTheme.colors.background)
            .padding(SpeachrTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))
        Text(
            text = "Welcome to Speachr",
            style = MaterialTheme.typography.headlineMedium,
            color = SpeachrTheme.colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Convert your speech to text instantly",
            style = MaterialTheme.typography.bodyLarge,
            color = SpeachrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Permission Box (Dummy UI)
        AnimatedVisibility(
            visible = !micGranted,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            PermissionCard(
                onClick = {
                    multiPermissionResultLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                        )
                    )
                },
                "Audio Permission Required",
                "Speachr needs access to your microphone to transcribe your voice."
            )
        }

        AnimatedVisibility(
            visible = !overlayGranted,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            PermissionCard(
                onClick = {
                    context.openSettingsPage(Manifest.permission.SYSTEM_ALERT_WINDOW)

                },
                "Overlay Permission Required.",
                "This permission is required to display the app bubble at all times."
            )
        }

        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))

        // Recording Button Section
        RecordingButton(
            isEnabled = micGranted,
            onClick = { /* Handle actual recording logic here */ }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionTextProvider()
//                Manifest.permission.SYSTEM_ALERT_WINDOW -> OverlayPermissionTextProvider()
                else -> return@forEach
            },
            isPermanentlyDeclined = isPermanentlyDeclined(
                Manifest.permission.RECORD_AUDIO,
                activity,
                context
            ),
            onDismiss = viewModel::dismissDialog,
            onOkClick = {
                viewModel.dismissDialog()
                multiPermissionResultLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.SYSTEM_ALERT_WINDOW
                    )
                )
            },
            onGoToAppSettingsClick = {
                viewModel.dismissDialog()
                context.openSettingsPage(permission)
            }
        )
    }
}

@Composable
fun PermissionCard(
    onClick: () -> Unit,
    title: String,
    description: String,
    btnText: String = "Grant Permission"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpeachrTheme.spacing.small),
        colors = CardDefaults.cardColors(
            containerColor = SpeachrTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(SpeachrTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = SpeachrTheme.colors.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.small))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.extraSmall))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = SpeachrTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.medium))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(btnText)
            }
        }
    }
}

@Composable
fun RecordingButton(isEnabled: Boolean, onClick: () -> Unit) {
    val containerColor =
        if (isEnabled) SpeachrTheme.colors.primary else SpeachrTheme.colors.onSurfaceVariant.copy(
            alpha = 0.3f
        )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(containerColor)
                .then(if (isEnabled) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Record",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.small))
        Text(
            text = if (isEnabled) "Tap to Start" else "Permission Needed",
            style = MaterialTheme.typography.labelLarge,
            color = if (isEnabled) SpeachrTheme.colors.primary else SpeachrTheme.colors.onSurfaceVariant
        )
    }
}

fun Context.openSettingsPage(permission: String) {
    when (permission) {
        Manifest.permission.RECORD_AUDIO -> {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                // Points the intent target exactly to your application's package name
                data = Uri.fromParts("package", packageName, null)
                // Required if calling startActivity outside an Activity context context wrapper
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }

        else -> Unit
    }

}
package com.yash.Speachr.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yash.Speachr.ui.components.MainViewModel
import com.yash.Speachr.ui.components.PermissionDialog
import com.yash.Speachr.ui.components.RecordAudioPermissionTextProvider
import com.yash.Speachr.ui.components.isPermanentlyDeclined
import com.yash.Speachr.ui.theme.SpeachrTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var isPermissionGranted by remember { mutableStateOf(false) }

    val viewModel = viewModel<MainViewModel>()
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    val context = LocalContext.current
    val activity = context as? Activity

    val audioPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPermissionGranted = isGranted
            viewModel.onPermissionResult(
                permission = Manifest.permission.RECORD_AUDIO,
                isGranted = isGranted
            )
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
            visible = !isPermissionGranted,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            PermissionCard(
                onClick = {
                    audioPermissionResultLauncher.launch(
                        Manifest.permission.RECORD_AUDIO
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))

        // Recording Button Section
        RecordingButton(
            isEnabled = isPermissionGranted,
            onClick = { /* Handle actual recording logic here */ }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionTextProvider()
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
                audioPermissionResultLauncher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            },
            onGoToAppSettingsClick = {
                viewModel.dismissDialog()
                context.openAppSystemSettings()
            }
        )
    }
}

@Composable
fun PermissionCard(onClick: () -> Unit) {
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
                text = "Audio Permission Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.extraSmall))
            Text(
                text = "Speachr needs access to your microphone to transcribe your voice.",
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
                Text("Grant Permission")
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

fun Context.openAppSystemSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        // Points the intent target exactly to your application's package name
        data = Uri.fromParts("package", packageName, null)
        // Required if calling startActivity outside an Activity context context wrapper
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
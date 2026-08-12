package com.yash.Speachr.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yash.Speachr.service.PasteAccessibilityService
import com.yash.Speachr.ui.components.AccessibilityPermissionTextProvider
import com.yash.Speachr.ui.components.MainViewModel
import com.yash.Speachr.ui.components.OverlayPermissionTextProvider
import com.yash.Speachr.ui.components.PermissionDialog
import com.yash.Speachr.ui.components.RecordAudioPermissionTextProvider
import com.yash.Speachr.ui.components.isPermanentlyDeclined
import com.yash.Speachr.ui.theme.SpeachrTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var overlayGranted by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    var accessibilityGranted by remember {
        mutableStateOf(context.isAccessibilityServiceEnabled())
    }

    var batteryIgnored by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    // Refresh permissions when app comes to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                micGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                overlayGranted = Settings.canDrawOverlays(context)
                accessibilityGranted = context.isAccessibilityServiceEnabled()
                batteryIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val viewModel = viewModel<MainViewModel>()
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    val multiPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = { perms ->
            micGranted = perms[Manifest.permission.RECORD_AUDIO] == true
            perms.forEach { (permission, isGranted) ->
                viewModel.onPermissionResult(permission = permission, isGranted = isGranted)
            }
        })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpeachrTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(SpeachrTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))

        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = SpeachrTheme.colors.primary,
            modifier = Modifier
                .size(64.dp)
                .background(SpeachrTheme.colors.primaryContainer, CircleShape)
                .padding(SpeachrTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.medium))

        Text(
            text = "Speachr",
            style = MaterialTheme.typography.headlineMedium,
            color = SpeachrTheme.colors.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Voice to text, simplified.",
            style = MaterialTheme.typography.bodyLarge,
            color = SpeachrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.extraLarge))

        // Requirements Checklist Card
        RequirementsCard(
            micGranted = micGranted,
            overlayGranted = overlayGranted,
            accessibilityGranted = accessibilityGranted,
            batteryIgnored = batteryIgnored,
            onMicClick = {
                multiPermissionResultLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            },
            onOverlayClick = {
                context.openSettingsPage(Manifest.permission.SYSTEM_ALERT_WINDOW)
            },
            onAccessibilityClick = {
                context.openSettingsPage(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
            },
            onBatteryClick = {
                context.openSettingsPage(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))

        // Recording Button Section
        val allPermissionsGranted = micGranted && overlayGranted && accessibilityGranted && batteryIgnored
        RecordingButton(
            isEnabled = allPermissionsGranted,
            onClick = { /* Handle actual recording logic here */ }
        )

        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.large))
    }

    // Handle Permission Dialogs
    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionTextProvider()
                Manifest.permission.SYSTEM_ALERT_WINDOW -> OverlayPermissionTextProvider()
                Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> AccessibilityPermissionTextProvider()
                else -> return@forEach
            },
            isPermanentlyDeclined = isPermanentlyDeclined(permission, activity, context),
            onDismiss = viewModel::dismissDialog,
            onOkClick = {
                viewModel.dismissDialog()
                if (permission == Manifest.permission.RECORD_AUDIO) {
                    multiPermissionResultLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                } else {
                    context.openSettingsPage(permission)
                }
            },
            onGoToAppSettingsClick = {
                viewModel.dismissDialog()
                context.openSettingsPage(permission)
            }
        )
    }
}

@Composable
fun RequirementsCard(
    micGranted: Boolean,
    overlayGranted: Boolean,
    accessibilityGranted: Boolean,
    batteryIgnored: Boolean,
    onMicClick: () -> Unit,
    onOverlayClick: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onBatteryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpeachrTheme.colors.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(SpeachrTheme.spacing.medium)) {
            Text(
                text = "Setup Requirements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = SpeachrTheme.spacing.medium)
            )

            RequirementItem(
                title = "Microphone Access",
                description = "Required for transcribing voice",
                isGranted = micGranted,
                onClick = onMicClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpeachrTheme.spacing.small),
                color = SpeachrTheme.colors.outlineVariant.copy(alpha = 0.5f)
            )

            RequirementItem(
                title = "Display Over Apps",
                description = "Needed for the floating control bubble",
                isGranted = overlayGranted,
                onClick = onOverlayClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpeachrTheme.spacing.small),
                color = SpeachrTheme.colors.outlineVariant.copy(alpha = 0.5f)
            )

            RequirementItem(
                title = "Accessibility Service",
                description = "Enables automatic text pasting",
                isGranted = accessibilityGranted,
                onClick = onAccessibilityClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpeachrTheme.spacing.small),
                color = SpeachrTheme.colors.outlineVariant.copy(alpha = 0.5f)
            )

            RequirementItem(
                title = "Background Activity",
                description = "Ensures app stays active during recording",
                isGranted = batteryIgnored,
                onClick = onBatteryClick
            )
        }
    }
}

@Composable
fun RequirementItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isGranted) { onClick() }
            .padding(SpeachrTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF10B981) else SpeachrTheme.colors.error,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(SpeachrTheme.spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isGranted) SpeachrTheme.colors.onSurface else SpeachrTheme.colors.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SpeachrTheme.colors.onSurfaceVariant
            )
        }

        if (!isGranted) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Grant",
                tint = SpeachrTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RecordingButton(isEnabled: Boolean, onClick: () -> Unit) {
    val containerColor = if (isEnabled) SpeachrTheme.colors.primary else SpeachrTheme.colors.onSurfaceVariant.copy(alpha = 0.1f)
    val contentColor = if (isEnabled) Color.White else SpeachrTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (isEnabled) 1f else 0.7f)
    ) {
        Surface(
            onClick = { if (isEnabled) onClick() else Unit },
            shape = CircleShape,
            color = containerColor,
            tonalElevation = 8.dp,
            shadowElevation = if (isEnabled) 4.dp else 0.dp,
            border = if (!isEnabled) BorderStroke(1.dp, SpeachrTheme.colors.outlineVariant) else null
        ) {
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record",
                    tint = contentColor,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(SpeachrTheme.spacing.medium))
        Text(
            text = if (isEnabled) "Tap to Start" else "Setup Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) SpeachrTheme.colors.primary else SpeachrTheme.colors.onSurfaceVariant
        )
    }
}

fun Context.openSettingsPage(permission: String) {
    when (permission) {
        Manifest.permission.RECORD_AUDIO -> {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            // Direct link to the app's specific overlay permission toggle
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        else -> Unit
    }
}

fun Context.isAccessibilityServiceEnabled(): Boolean {
    val expectedComponentName = "$packageName/${PasteAccessibilityService::class.java.name}"
    val settingValue = Settings.Secure.getString(
        contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return settingValue?.contains(expectedComponentName) == true
}

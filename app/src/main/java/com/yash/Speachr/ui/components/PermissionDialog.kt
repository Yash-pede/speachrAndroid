package com.yash.Speachr.ui.components

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss, confirmButton = {
            TextButton(onClick = onGoToAppSettingsClick) {
                Text(
                    text = "Grant Permission", fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onOkClick) {
                Text(
                    text = "Cancel", fontWeight = FontWeight.Bold
                )
            }
        }, title = {
            Text(text = "Permission required")
        }, text = {
            Text(
                text = permissionTextProvider.getDescription(
                    isPermanentlyDeclined = isPermanentlyDeclined
                )
            )
        }, modifier = modifier
    )
}


interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class RecordAudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems like you have permanently declined audio permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to you microphone to translate your speech"
        }
    }
}

class OverlayPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems like you have permanently declined Display over other apps permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs this permission to show the app bubble"
        }
    }
}

fun isPermanentlyDeclined(permission: String, activity: Activity?, context: Context): Boolean {
    val isDenied =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED

    // Check if rationale should be shown (Requires Activity Context)
    val shouldShowRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
    } ?: false

    // Permanently declined means: It is denied, but system says DO NOT show rationale
    return isDenied && !shouldShowRationale
}

package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionManager {
    private const val PREFS_NAME = "appsc_permissions_prefs"
    private const val KEY_PERMISSIONS_REQUESTED = "has_requested_initial_permissions"

    /**
     * Returns the list of runtime permissions genuinely required during initial app startup:
     * - Notifications (POST_NOTIFICATIONS on Android 13+)
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        // 1. Notifications (Android 13+ / API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.toTypedArray()
    }

    /**
     * Checks if the initial setup permission request dialog has already been presented.
     */
    fun hasAlreadyRequestedPermissions(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMISSIONS_REQUESTED, false)
    }

    /**
     * Stores the state indicating that the initial setup permission request flow has been presented.
     */
    fun setPermissionsRequested(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PERMISSIONS_REQUESTED, true).apply()
    }

    /**
     * Resets the permission requested state (useful for testing or admin reset).
     */
    fun resetPermissionState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PERMISSIONS_REQUESTED).apply()
    }

    /**
     * Checks whether a specific permission is granted.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns a map of permission label to granted state.
     */
    fun getPermissionStatuses(context: Context): Map<String, Boolean> {
        val statuses = mutableMapOf<String, Boolean>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            statuses["Push Notifications"] = isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            statuses["Push Notifications"] = true // Granted at install time for API < 33
        }

        statuses["Camera Access"] = isPermissionGranted(context, Manifest.permission.CAMERA)
        statuses["Location Services"] = isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)

        return statuses
    }

    /**
     * Opens system settings page for APPSC GROUP-2 app where user can configure permissions.
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

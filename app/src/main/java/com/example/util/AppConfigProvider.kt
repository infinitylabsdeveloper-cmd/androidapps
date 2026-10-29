package com.example.util

import com.example.BuildConfig

/**
 * Resolves the active tenant/app identifier dynamically.
 * Priority:
 * 1. Custom runtime-selected app in SampleData (for Admin preview / Multi-exam switching)
 * 2. BuildConfig.APP_ID (if defined by Gradle build variant / flavor)
 * 3. Matched package name from ExamAppsData/SampleData
 * 4. Default fallback to preserve backward compatibility
 */
object AppConfigProvider {
    val appId: String
        get() = try {
            val activeSample = com.example.data.SampleData.currentActiveAppId.value
            val buildAppId = try { BuildConfig.APP_ID } catch (e: Exception) { "" }
            if (activeSample.isNotBlank()) {
                activeSample
            } else if (buildAppId.isNotBlank() && buildAppId != "andhra-pradesh-appsc-group-2") {
                buildAppId
            } else {
                resolveFromPackage(BuildConfig.APPLICATION_ID)
            }
        } catch (e: Exception) {
            "andhra-pradesh-appsc-group-2"
        }

    fun resolveFromPackage(pkg: String): String {
        val cleanPkg = pkg.lowercase().trim()
        val matchedApp = com.example.data.SampleData.appsListState.find {
            it.packageName.lowercase() == cleanPkg || it.id.lowercase() == cleanPkg
        }
        return matchedApp?.id ?: when {
            cleanPkg.contains("aptet") -> "andhra-pradesh-ap-tet"
            cleanPkg.contains("apdsc") -> "andhra-pradesh-ap-dsc"
            cleanPkg.contains("appscgroup1") -> "andhra-pradesh-appsc-group-1"
            cleanPkg.contains("appscgroup2") -> "andhra-pradesh-appsc-group-2"
            else -> "andhra-pradesh-appsc-group-2"
        }
    }
}

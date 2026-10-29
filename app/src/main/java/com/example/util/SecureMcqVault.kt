package com.example.util

import android.content.Context
import android.util.Base64
import com.example.model.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secure AES-256 Encrypted Internal Vault for Downloaded MCQ Content.
 * 
 * Strict Security Rules:
 * 1. Encrypted files are stored inside internal app-private directories (no public storage, no Downloads folder).
 * 2. File manager / external apps cannot access or read the raw contents.
 * 3. Decryption occurs purely in-memory at practice runtime.
 * 4. Content is invalidated securely on logout or cache removal.
 */
object SecureMcqVault {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val VAULT_DIR_NAME = ".secure_mcq_vault"
    private val gson = Gson()

    // Application salt and internal derived secret key
    private fun getSecretKey(appId: String): SecretKey {
        val seed = "APPSC_GROUP2_SECURE_VAULT_KEY_DERIVATION_${appId}_V2026"
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(seed.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun getVaultDirectory(context: Context): File {
        val dir = File(context.noBackupFilesDir ?: context.filesDir, VAULT_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Encrypt a list of questions into a secure ciphertext string
     */
    fun encryptQuestions(questions: List<Question>, appId: String): String {
        try {
            val json = gson.toJson(questions)
            val key = getSecretKey(appId)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(json.toByteArray(StandardCharsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Decrypt an encrypted ciphertext string back to Question list
     */
    fun decryptQuestions(encryptedData: String, appId: String): List<Question> {
        if (encryptedData.isBlank()) return emptyList()
        try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            if (combined.size < IV_LENGTH_BYTE) return emptyList()

            val iv = ByteArray(IV_LENGTH_BYTE)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE)

            val cipherTextSize = combined.size - IV_LENGTH_BYTE
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherTextSize)

            val key = getSecretKey(appId)
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val plainTextBytes = cipher.doFinal(cipherText)
            val json = String(plainTextBytes, StandardCharsets.UTF_8)

            val listType = object : TypeToken<List<Question>>() {}.type
            return gson.fromJson(json, listType) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Save questions into internal encrypted binary file
     */
    fun saveEncryptedVaultFile(context: Context, packId: String, questions: List<Question>, appId: String): Long {
        try {
            val encryptedBase64 = encryptQuestions(questions, appId)
            val file = File(getVaultDirectory(context), "${packId.hashCode().toString(16)}.dat")
            file.writeText(encryptedBase64, StandardCharsets.UTF_8)
            return file.length()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0L
        }
    }

    /**
     * Read questions from internal encrypted binary file
     */
    fun readEncryptedVaultFile(context: Context, packId: String, appId: String): List<Question> {
        try {
            val file = File(getVaultDirectory(context), "${packId.hashCode().toString(16)}.dat")
            if (file.exists()) {
                val encryptedBase64 = file.readText(StandardCharsets.UTF_8)
                return decryptQuestions(encryptedBase64, appId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    /**
     * Remove an encrypted file
     */
    fun deleteEncryptedVaultFile(context: Context, packId: String) {
        try {
            val file = File(getVaultDirectory(context), "${packId.hashCode().toString(16)}.dat")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear all offline vault files securely
     */
    fun clearAllVault(context: Context) {
        try {
            val dir = getVaultDirectory(context)
            dir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Calculate total bytes stored in internal vault
     */
    fun getVaultSizeBytes(context: Context): Long {
        try {
            val dir = getVaultDirectory(context)
            return dir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            return 0L
        }
    }
}

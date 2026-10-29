package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.model.Question
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure AES-256 GCM encrypted local storage for offline MCQs and question packs.
 * 
 * Guarantees:
 * 1. Downloaded MCQs are encrypted on device and saved in app-private storage (`filesDir/.secure_vault`).
 * 2. Cannot be viewed or opened by File Managers or Android Downloads app.
 * 3. Cannot be accessed by other applications.
 * 4. Only decrypted in memory when practicing within this app.
 */
object SecureOfflineStorage {

    private const val KEY_ALIAS = "AppscGroup2_MCQ_SecureKey_v1"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val VAULT_DIR_NAME = ".secure_mcq_vault"

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val keyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return keyEntry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getVaultDirectory(context: Context): File {
        val dir = File(context.filesDir, VAULT_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Encrypt and store question list into an encrypted binary blob in app-private storage
     */
    fun saveEncryptedQuestions(context: Context, packId: String, questions: List<Question>): Boolean {
        return try {
            val jsonArray = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("appId", q.appId)
                    put("subjectId", q.subjectId)
                    put("unitId", q.unitId)
                    put("topicId", q.topicId)
                    put("subtopic", q.subtopic)
                    put("questionText", q.questionText)
                    put("correctOption", q.correctOption)
                    put("explanation", q.explanation ?: "")
                    put("setName", q.setName)
                    put("difficulty", q.difficulty)
                    put("language", q.language)
                    put("imageUrl", q.imageUrl ?: "")
                    put("year", q.year ?: "")
                    put("exam", q.exam ?: "")
                    put("marks", q.marks)
                    put("negativeMarks", q.negativeMarks)

                    val optsObj = JSONObject()
                    q.options.forEach { (k, v) -> optsObj.put(k, v) }
                    put("options", optsObj)
                }
                jsonArray.put(obj)
            }

            val plainBytes = jsonArray.toString().toByteArray(Charsets.UTF_8)
            val secretKey = getOrCreateSecretKey()

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainBytes)

            val safeFileName = "pack_${packId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.enc"
            val targetFile = File(getVaultDirectory(context), safeFileName)

            FileOutputStream(targetFile).use { fos ->
                fos.write(iv.size)
                fos.write(iv)
                fos.write(cipherText)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decrypt and load question list from the encrypted binary blob
     */
    fun loadDecryptedQuestions(context: Context, packId: String): List<Question>? {
        return try {
            val safeFileName = "pack_${packId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.enc"
            val targetFile = File(getVaultDirectory(context), safeFileName)
            if (!targetFile.exists()) return null

            val secretKey = getOrCreateSecretKey()
            FileInputStream(targetFile).use { fis ->
                val ivSize = fis.read()
                val iv = ByteArray(ivSize)
                fis.read(iv)

                val cipherBytes = fis.readBytes()
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                val plainBytes = cipher.doFinal(cipherBytes)
                val jsonString = String(plainBytes, Charsets.UTF_8)
                val jsonArray = JSONArray(jsonString)

                val list = mutableListOf<Question>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val optionsMap = mutableMapOf<String, String>()
                    val optsJson = obj.optJSONObject("options")
                    if (optsJson != null) {
                        val keys = optsJson.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            optionsMap[k] = optsJson.getString(k)
                        }
                    }

                    list.add(
                        Question(
                            id = obj.getString("id"),
                            appId = obj.optString("appId", "appsc-group2"),
                            subjectId = obj.optString("subjectId", ""),
                            unitId = obj.optString("unitId", ""),
                            topicId = obj.optString("topicId", ""),
                            subtopic = obj.optString("subtopic", ""),
                            questionText = obj.getString("questionText"),
                            options = optionsMap,
                            correctOption = obj.getString("correctOption"),
                            explanation = obj.optString("explanation").takeIf { it.isNotBlank() },
                            setName = obj.optString("setName", ""),
                            difficulty = obj.optString("difficulty", "Medium"),
                            language = obj.optString("language", "English / Telugu"),
                            imageUrl = obj.optString("imageUrl").takeIf { it.isNotBlank() },
                            year = obj.optString("year").takeIf { it.isNotBlank() },
                            exam = obj.optString("exam").takeIf { it.isNotBlank() },
                            marks = obj.optDouble("marks", 1.0),
                            negativeMarks = obj.optDouble("negativeMarks", 0.33)
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete an encrypted pack file
     */
    fun deleteEncryptedPack(context: Context, packId: String): Boolean {
        return try {
            val safeFileName = "pack_${packId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.enc"
            val targetFile = File(getVaultDirectory(context), safeFileName)
            if (targetFile.exists()) {
                targetFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Total encrypted vault storage bytes
     */
    fun getVaultSizeBytes(context: Context): Long {
        return try {
            val dir = getVaultDirectory(context)
            dir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clear all offline encrypted files (e.g. on user logout / clear storage)
     */
    fun clearAllEncryptedStorage(context: Context) {
        try {
            val dir = getVaultDirectory(context)
            dir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

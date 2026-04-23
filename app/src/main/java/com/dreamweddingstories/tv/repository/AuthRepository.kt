package com.dreamweddingstories.tv.repository

import android.content.SharedPreferences
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val prefs: SharedPreferences
) {

    // ── Session helpers ──────────────────────────────────────────────────────

    fun getSavedCode(): String? = prefs.getString(Constants.SESSION_KEY_CODE, null)
        ?.takeIf { it.isNotBlank() }

    fun saveSession(code: String) {
        prefs.edit().putString(Constants.SESSION_KEY_CODE, code.uppercase()).apply()
    }

    fun clearSession() {
        prefs.edit().remove(Constants.SESSION_KEY_CODE).apply()
    }

    fun hasActiveSession(): Boolean = getSavedCode() != null

    // ── Code-based sign-in ───────────────────────────────────────────────────

    /**
     * Looks up the wedding document in Firestore using the code as document ID.
     * Firestore path: weddings/{CODE}  →  { coupleNames, assignedVideoIds, ... }
     */
    suspend fun signInWithCode(rawCode: String): Result<User> {
        val code = rawCode.uppercase().trim()

        // ── Demo shortcut (no Firestore needed) ──
        if (code == "DEMO") {
            val demoUser = User(
                uid = "demo_uid",
                accessCode = "DEMO",
                displayName = "Demo Wedding",
                assignedVideoIds = listOf("demo_1", "demo_2", "demo_3")
            )
            saveSession(code)
            return Result.success(demoUser)
        }

        // ── Vanshika shortcut (no Firestore needed) ──
        if (code == "V4N2") {
            val vanshikaUser = User(
                uid = "vanshika_uid",
                accessCode = "V4N2",
                displayName = "Vanshika & Nitin",
                assignedVideoIds = listOf(
                    "vanshika_trailer",
                    "vanshika_wedding_film",
                    "vanshika_sangeet",
                    "vanshika_reel_1",
                    "vanshika_reel_2",
                    "vanshika_reel_3",
                    "vanshika_reel_4"
                )
            )
            saveSession(code)
            return Result.success(vanshikaUser)
        }

        // ── Generic Firestore lookup ──
        return try {
            val doc = firestore
                .collection(Constants.FIREBASE_WEDDINGS_COLLECTION)
                .document(code)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("Invalid wedding code. Please try again."))
            }

            @Suppress("UNCHECKED_CAST")
            val videoIds = (doc.get("assignedVideoIds") as? List<String>) ?: emptyList()
            val coupleNames = doc.getString("coupleNames") ?: doc.getString("displayName") ?: "Happy Couple"

            val user = User(
                uid = code,
                accessCode = code,
                displayName = coupleNames,
                assignedVideoIds = videoIds
            )
            saveSession(code)
            Result.success(user)
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Result.failure(Exception("Permission denied. Check your Firebase Security Rules."))
            } else {
                Result.failure(Exception("Database error: ${e.message}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not connect. Check your internet connection."))
        }
    }

    /**
     * Restores a user session from a previously saved code.
     * Called during splash screen to skip login.
     */
    suspend fun restoreSession(): Result<User> {
        val code = getSavedCode()
            ?: return Result.failure(Exception("No saved session"))
        return signInWithCode(code)
    }
}

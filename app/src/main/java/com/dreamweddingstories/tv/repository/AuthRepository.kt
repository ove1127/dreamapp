package com.dreamweddingstories.tv.repository

import android.content.SharedPreferences
import com.dreamweddingstories.tv.model.ClientEvent
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.model.WeddingVideo
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
     * Firestore path: weddings/{CODE}  →  { coupleNames, assignedVideoIds, events, ... }
     */
    suspend fun signInWithCode(rawCode: String): Result<User> {
        val code = rawCode.uppercase().trim()

        // ── Demo shortcut (no Firestore needed) ──
        if (code == "DEMO") {
            val vimeoId = "1034440538"
            val demoEvents = listOf(
                ClientEvent(
                    eventName = "Pre-Wedding Shoot",
                    eventDate = "November 20, 2025",
                    thumbnailUrl = "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=800&q=80",
                    videos = listOf(
                        WeddingVideo("demo_1", vimeoId, "Anjali & Rahul", "November 20, 2025",
                            "A cinematic pre-wedding shoot in the golden light of Udaipur.",
                            "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=800&q=80",
                            "05:24", "Main Film", "demo_uid"),
                    )
                ),
                ClientEvent(
                    eventName = "Main Wedding",
                    eventDate = "December 12, 2025",
                    thumbnailUrl = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
                    videos = listOf(
                        WeddingVideo("demo_2", vimeoId, "Anjali & Rahul", "December 12, 2025",
                            "The full cinematic wedding film — every vow, every tear, every smile.",
                            "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
                            "45:00", "Main Film", "demo_uid"),
                        WeddingVideo("demo_3", "910245084", "Anjali & Rahul", "December 12, 2025",
                            "Wedding Highlights Reel — the most beautiful moments in 60 seconds.",
                            "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                            "01:00", "Reel", "demo_uid"),
                    )
                ),
                ClientEvent(
                    eventName = "Sangeet Night",
                    eventDate = "December 11, 2025",
                    thumbnailUrl = "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                    videos = listOf(
                        WeddingVideo("demo_4", vimeoId, "Anjali & Rahul", "December 11, 2025",
                            "An electric night of music, dance and pure celebration.",
                            "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                            "95:00", "Main Film", "demo_uid"),
                    )
                )
            )
            val demoUser = User(
                uid = "demo_uid",
                accessCode = "DEMO",
                displayName = "Anjali & Rahul",
                assignedVideoIds = listOf("demo_1", "demo_2", "demo_3"),
                events = demoEvents
            )
            saveSession(code)
            return Result.success(demoUser)
        }

        // ── Vanshika shortcut (no Firestore needed) ──
        if (code == "V4N2") {
            val vimeoId = "1183011948?h=f46b555875"
            val thumb = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80"
            val names = "Vanshika & Nitin"
            val uid = "vanshika_uid"

            val vanshikaEvents = listOf(
                ClientEvent(
                    eventName = "Pre-Wedding Shoot",
                    eventDate = "February 10, 2026",
                    thumbnailUrl = "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=800&q=80",
                    videos = listOf(
                        WeddingVideo("vanshika_trailer", vimeoId, names, "February 10, 2026",
                            "A cinematic glimpse into our beautiful journey together.",
                            "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=800&q=80",
                            "04:30", "Trailer", uid)
                    )
                ),
                ClientEvent(
                    eventName = "Haldi & Mehendi",
                    eventDate = "February 13, 2026",
                    thumbnailUrl = "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                    videos = listOf(
                        WeddingVideo("vanshika_reel_1", vimeoId, names, "February 13, 2026",
                            "Haldi Highlights — Vibrant colors and pure happiness.",
                            "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                            "00:55", "Reel", uid),
                        WeddingVideo("vanshika_reel_2", vimeoId, names, "February 13, 2026",
                            "Mehendi Magic — Intricate designs and musical beats.",
                            "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                            "00:45", "Reel", uid),
                    )
                ),
                ClientEvent(
                    eventName = "Sangeet Night",
                    eventDate = "February 13, 2026",
                    thumbnailUrl = thumb,
                    videos = listOf(
                        WeddingVideo("vanshika_sangeet", vimeoId, names, "February 13, 2026",
                            "The grand Sangeet celebration with incredible performances.",
                            thumb, "95:00", "Main Film", uid),
                        WeddingVideo("vanshika_reel_3", vimeoId, names, "February 13, 2026",
                            "Sangeet Teaser — Dance, joy, and non-stop celebration.",
                            thumb, "01:00", "Reel", uid),
                    )
                ),
                ClientEvent(
                    eventName = "Main Wedding",
                    eventDate = "February 14, 2026",
                    thumbnailUrl = thumb,
                    videos = listOf(
                        WeddingVideo("vanshika_wedding_film", vimeoId, names, "February 14, 2026",
                            "The complete wedding ceremony captured in cinematic quality.",
                            thumb, "45:00", "Main Film", uid),
                        WeddingVideo("vanshika_reel_4", vimeoId, names, "February 14, 2026",
                            "Bridal Entry — The moment everyone waited for.",
                            thumb, "00:50", "Reel", uid),
                    )
                )
            )
            val vanshikaUser = User(
                uid = "vanshika_uid",
                accessCode = "V4N2",
                displayName = "Vanshika & Nitin",
                assignedVideoIds = listOf(
                    "vanshika_trailer", "vanshika_wedding_film", "vanshika_sangeet",
                    "vanshika_reel_1", "vanshika_reel_2", "vanshika_reel_3", "vanshika_reel_4"
                ),
                events = vanshikaEvents
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

            // ── Parse events from Firestore ──
            @Suppress("UNCHECKED_CAST")
            val rawEvents = (doc.get("events") as? List<Map<String, Any>>) ?: emptyList()
            val events = rawEvents.mapIndexed { eventIdx, evMap ->
                @Suppress("UNCHECKED_CAST")
                val rawVideos = (evMap["videos"] as? List<Map<String, Any>>) ?: emptyList()
                val videos = rawVideos.mapIndexed { videoIdx, vMap ->
                    val vimeoId = (vMap["vimeo_id"] as? String) ?: ""
                    WeddingVideo(
                        id = "${code.lowercase()}_e${eventIdx}_v${videoIdx}",
                        vimeoVideoId = vimeoId,
                        coupleNames = coupleNames,
                        weddingDate = (evMap["event_date"] as? String) ?: "",
                        description = (vMap["title"] as? String) ?: "",
                        thumbnailUrl = (vMap["thumbnail_url"] as? String) ?: "",
                        duration = (vMap["duration"] as? String) ?: "",
                        category = (vMap["category"] as? String) ?: "Main Film",
                        userId = code
                    )
                }
                ClientEvent(
                    eventName = (evMap["event_name"] as? String) ?: "Event",
                    eventDate = (evMap["event_date"] as? String) ?: "",
                    thumbnailUrl = (evMap["event_thumbnail_url"] as? String)
                        ?: videos.firstOrNull()?.thumbnailUrl ?: "",
                    videos = videos
                )
            }

            val user = User(
                uid = code,
                accessCode = code,
                displayName = coupleNames,
                assignedVideoIds = videoIds,
                events = events
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

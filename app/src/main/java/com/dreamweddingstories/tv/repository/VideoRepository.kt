package com.dreamweddingstories.tv.repository

import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.utils.Constants
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getVideosByIds(videoIds: List<String>): Result<List<WeddingVideo>> = runCatching {
        if (videoIds.isEmpty()) return@runCatching emptyList()

        if (videoIds.contains("demo_1")) {
            return@runCatching getDemoVideos()
        }
        
        if (videoIds.any { it.startsWith("vanshika_") }) {
            return@runCatching getVanshikaVideos()
        }

        videoIds
            .distinct()
            .chunked(10)
            .flatMap { chunk ->
                firestore.collection(Constants.FIREBASE_VIDEOS_COLLECTION)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(WeddingVideo::class.java)?.copy(id = doc.id)
                    }
            }
            .sortedByDescending { it.weddingDate }
    }

    suspend fun getVideoById(videoId: String): Result<WeddingVideo> = runCatching {
        if (videoId.startsWith("demo_")) {
            return@runCatching getDemoVideos().first { it.id == videoId }
        }

        if (videoId.startsWith("vanshika_")) {
            return@runCatching getVanshikaVideos().first { it.id == videoId }
        }

        val doc = firestore.collection(Constants.FIREBASE_VIDEOS_COLLECTION)
            .document(videoId)
            .get()
            .await()

        val video = doc.toObject(WeddingVideo::class.java)
            ?: throw IllegalStateException("Video not found")
        video.copy(id = doc.id)
    }

    private fun getDemoVideos(): List<WeddingVideo> {
        return listOf(
            WeddingVideo(
                id = "demo_1",
                vimeoVideoId = "1034440538",
                coupleNames = "Anjali & Rahul",
                weddingDate = "December 12, 2025",
                description = "A beautiful cinematic wedding ceremony captured in the heart of Udaipur. Witness the magic of their union.",
                thumbnailUrl = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
                    duration = "05:24",
                    category = "Trailer",
                    userId = "demo_uid"
                ),
                WeddingVideo(
                    id = "demo_2",
                    vimeoVideoId = "910245084",
                    coupleNames = "Sneha & Amit",
                    weddingDate = "November 20, 2025",
                    description = "An intimate beach wedding filled with love, laughter, and a stunning sunset. The perfect beginning to their forever.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=800&q=80",
                    duration = "03:45",
                    category = "Main Film",
                    userId = "demo_uid"
                ),
                WeddingVideo(
                    id = "demo_3",
                    vimeoVideoId = "394747201",
                    coupleNames = "Priya & Vikram",
                    weddingDate = "January 15, 2026",
                    description = "A grand celebration of love and tradition. From the vibrant Mehendi to the emotional Sangeet, every moment tells a story.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=800&q=80",
                    duration = "08:12",
                    category = "Main Film",
                    userId = "demo_uid"
                )
            )
        }

        private fun getVanshikaVideos(): List<WeddingVideo> {
            val vimeoId = "1183011948?h=f46b555875"
            val names = "Vanshika & Nitin"
            val date = "February 14, 2026"
            val thumb = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80"
            val uId = "vanshika_uid"

            return listOf(
                WeddingVideo("vanshika_trailer", vimeoId, names, date, "A cinematic glimpse into our beautiful journey and wedding celebration.", thumb, "04:30", "Trailer", uId),
                WeddingVideo("vanshika_wedding_film", vimeoId, names, date, "The complete wedding ceremony captured beautifully in cinematic quality.", thumb, "45:00", "Main Film", uId),
                WeddingVideo("vanshika_sangeet", vimeoId, names, date, "The grand Sangeet celebration with incredible performances and emotional moments.", thumb, "95:00", "Main Film", uId),
                WeddingVideo("vanshika_reel_1", vimeoId, names, date, "Haldi Highlights - Vibrant colors and pure happiness.", thumb, "00:55", "Reel", uId),
                WeddingVideo("vanshika_reel_2", vimeoId, names, date, "Mehendi Magic - Intricate designs and musical beats.", thumb, "00:45", "Reel", uId),
                WeddingVideo("vanshika_reel_3", vimeoId, names, date, "Sangeet Teaser - Dance, joy, and non-stop celebration.", thumb, "00:60", "Reel", uId),
                WeddingVideo("vanshika_reel_4", vimeoId, names, date, "Bridal Entry - The moment everyone waited for.", thumb, "00:50", "Reel", uId)
            )
        }
    }

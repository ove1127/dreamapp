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
                userId = "demo_uid"
            )
        )
    }
}

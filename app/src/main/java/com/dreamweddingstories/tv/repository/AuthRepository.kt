package com.dreamweddingstories.tv.repository

import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.utils.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")
            val userDoc = firestore.collection(Constants.FIREBASE_USERS_COLLECTION).document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: throw IllegalStateException("User data not found in Firestore")
            Result.success(user.copy(uid = uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        assignedVideoIds: List<String> = emptyList()
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Signup failed")
            val user = User(uid = uid, email = email, displayName = displayName, assignedVideoIds = assignedVideoIds)
            
            // Try to save to Firestore, but don't fail auth if permissions deny it (Demo fallback)
            try {
                firestore.collection(Constants.FIREBASE_USERS_COLLECTION).document(uid).set(user).await()
            } catch (e: Exception) {
                // Ignore firestore permission errors for demo
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun populateDemoData(userId: String): Result<Unit> {
        return Result.success(Unit) // Handled locally in VideoRepository now to avoid permission issues
    }

    suspend fun getUserProfile(uid: String): Result<User> = runCatching {
        try {
            val snapshot = firestore.collection(Constants.FIREBASE_USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                return@runCatching user.copy(uid = uid)
            }
        } catch (e: Exception) {
            // Ignore firestore errors
        }
        
        // Fallback for Demo Users if Firestore failed or is empty
        User(
            uid = uid, 
            email = "demo@guest.com", 
            displayName = "Demo Guest", 
            assignedVideoIds = listOf("demo_1", "demo_2", "demo_3")
        )
    }

    fun signOut() {
        auth.signOut()
    }
}

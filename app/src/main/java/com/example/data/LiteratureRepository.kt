package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "LiteratureRepository"

class LiteratureRepository(context: Context) {
    private val dbId = context.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(dbId)

    // Documents
    fun observeDocuments(userId: String): Flow<List<DocumentItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("documents")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DocumentItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveDocument(userId: String, item: DocumentItem): Result<Unit> = try {
        val col = firestore.collection("documents")
        val wordCount = item.content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val charCount = item.content.length
        val readingTime = maxOf(1, wordCount / 200)

        val map = hashMapOf(
            "userId" to userId,
            "title" to item.title,
            "content" to item.content,
            "language" to item.language,
            "category" to item.category,
            "genre" to item.genre,
            "wordCount" to wordCount,
            "characterCount" to charCount,
            "readingTimeMins" to readingTime,
            "isFavorite" to item.isFavorite,
            "isArchived" to item.isArchived,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (item.id.isBlank()) {
            map["createdAt"] = FieldValue.serverTimestamp()
            col.add(map).await()
        } else {
            col.document(item.id).set(map, com.google.firebase.firestore.SetOptions.merge()).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save document", e)
        Result.failure(e)
    }

    suspend fun deleteDocument(docId: String): Result<Unit> = try {
        firestore.collection("documents").document(docId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Books
    fun observeBooks(userId: String): Flow<List<BookItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("books")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BookItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBook(userId: String, item: BookItem): Result<Unit> = try {
        val col = firestore.collection("books")
        val map = hashMapOf(
            "userId" to userId,
            "title" to item.title,
            "description" to item.description,
            "language" to item.language,
            "genre" to item.genre,
            "targetWordCount" to item.targetWordCount,
            "currentWordCount" to item.currentWordCount,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (item.id.isBlank()) {
            map["createdAt"] = FieldValue.serverTimestamp()
            col.add(map).await()
        } else {
            col.document(item.id).set(map, com.google.firebase.firestore.SetOptions.merge()).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Seed sample multilingual literature if first time
    suspend fun seedSampleDataIfNeeded(userId: String) {
        try {
            val docs = firestore.collection("documents").whereEqualTo("userId", userId).get().await()
            if (docs.isEmpty) {
                val sampleDocs = listOf(
                    DocumentItem(
                        userId = userId,
                        title = "The Weight of Unfinished Sentences",
                        content = "It was the autumn of twilight in Kyoto when Kenji realized that words were not merely vessels of meaning, but living entities that breathed when spoken aloud. In Bengali, Rabindranath Tagore once wrote of endless skies; here under the cedar trees, silence spoke louder than ink.",
                        language = "English",
                        category = "Creative Literature",
                        genre = "Short Story",
                        isFavorite = true
                    ),
                    DocumentItem(
                        userId = userId,
                        title = "मेघदूत का आधुनिक स्वरूप (Modern Meghdoot)",
                        content = "आधुनिक युग में यक्ष का संदेश केवल बादलों से नहीं, बल्कि डिजिटल तरंगों के माध्यम से प्रवाहित होता है। कालिदास की कविता आज भी उतनी ही जीवंत है जितनी प्राचीन उज्जयिनी में थी।",
                        language = "Hindi",
                        category = "Traditional & Regional Literature",
                        genre = "Classical Essay",
                        isFavorite = true
                    ),
                    DocumentItem(
                        userId = userId,
                        title = "Echoes in the Casbah",
                        content = "La brise de la Méditerranée murmurait des histoires oubliées aux murs blancs d'Alger. Chaque ruelle pavée gardait la mémoire des poètes andalous.",
                        language = "French",
                        category = "Creative Literature",
                        genre = "Travelogue",
                        isFavorite = false
                    )
                )
                for (doc in sampleDocs) {
                    saveDocument(userId, doc)
                }

                // Seed sample book
                val book = BookItem(
                    userId = userId,
                    title = "Chronicles of the Silk Road",
                    description = "An epic novel traversing ancient trade routes, cross-cultural dialogues, and timeless human aspirations.",
                    language = "English",
                    genre = "Novel",
                    targetWordCount = 80000,
                    currentWordCount = 12450
                )
                saveBook(userId, book)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Seeding failed", e)
        }
    }
}

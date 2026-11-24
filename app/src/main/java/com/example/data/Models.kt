package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class DocumentItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val language: String = "English",
    val category: String = "Creative Literature", // Creative Literature, Academic & Nonfiction, Traditional & Regional
    val genre: String = "Short Story",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingTimeMins: Int = 1,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BookItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "English",
    val genre: String = "Novel",
    val targetWordCount: Int = 50000,
    val currentWordCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChapterItem(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val chapterNumber: Int = 1,
    val wordCount: Int = 0,
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CharacterItem(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val name: String = "",
    val role: String = "Protagonist",
    val description: String = "",
    val traits: String = ""
)

data class WritingStats(
    val totalDocuments: Int = 0,
    val totalWords: Int = 0,
    val totalCharacters: Int = 0,
    val totalChapters: Int = 0,
    val writingStreakDays: Int = 6,
    val averageDailyOutput: Int = 1250,
    val completionRate: Float = 0.42f
)

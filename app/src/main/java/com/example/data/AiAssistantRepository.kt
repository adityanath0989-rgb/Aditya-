package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.*
import kotlinx.coroutines.withTimeout

private const val TAG = "AiAssistantRepository"

class AiAssistantRepository {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun getLiteraryAssistance(prompt: String, taskType: String): Result<String> = try {
        val systemInstruction = when (taskType) {
            "plot" -> "You are an expert novel and story architect. Provide creative, compelling plot developments and twists."
            "grammar" -> "You are an expert literary copyeditor. Improve sentence structure, fix grammar, and elevate prose while strictly preserving the author's original voice."
            "translate" -> "You are a master multilingual literary translator. Translate the text with high fidelity to native poetic idioms and nuance."
            "characters" -> "You are a character development specialist. Create deep, nuanced character profiles including backstory, motivations, and arcs."
            "summary" -> "You are a literary critic. Provide a concise, insightful summary and thematic analysis of the text."
            else -> "You are a versatile AI literary assistant for World Literature Studio, helping writers in any language."
        }

        val fullPrompt = "$systemInstruction\n\nUser request:\n$prompt"

        val response = withTimeout(60_000L) {
            model.generateContent(fullPrompt)
        }
        val text = response.text
        if (!text.isNullOrBlank()) {
            Result.success(text)
        } else {
            Result.failure(Exception("AI returned empty response."))
        }
    } catch (e: Exception) {
        Log.e(TAG, "AI assistance failed", e)
        Result.failure(e)
    }
}

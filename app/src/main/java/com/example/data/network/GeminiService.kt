package com.example.data.network

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String // Base64 encoding
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    @Json(name = "mimeType") val mimeType: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    @Json(name = "text") val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseFormat") val responseFormat: ResponseFormat? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiManager {

    // Helper to convert Bitmap to Base64
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun askTutor(prompt: String, contextBookText: String? = null, notepadBitmap: Bitmap? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "⚠️ Gemini API Key is missing or default. Please add your key in the AI Studio Secrets tab to trigger live replies. Self-test advice:\n\nYou are asking: \"$prompt\""
        }

        val partsList = mutableListOf<Part>()

        // 1. Add context textbook readings if available
        if (!contextBookText.isNullOrEmpty()) {
            partsList.add(Part(text = "Reference study materials / book context:\n$contextBookText\n\n"))
        }

        // 2. Add handwritten drawing as canvas image if available
        if (notepadBitmap != null) {
            val base64Img = notepadBitmap.toBase64()
            partsList.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img)))
            partsList.add(Part(text = "The user has uploaded a snapshot of their handwritten drawing notepad above. Please read, perform OCR on formulas or structures, and reconcile it with their question.\n"))
        }

        // 3. User's main query text
        partsList.add(Part(text = prompt))

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = partsList)),
            systemInstruction = Content(parts = listOf(
                Part(text = "You are an expert scientific academic AI study tutor. Provide high-fidelity, precise explanations with nice spacing. Utilize professional academic notation or formulas. Keep explanations clear and helpful.")
            )),
            generationConfig = GenerationConfig(temperature = 0.4f)
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No text response generated."
        } catch (e: Exception) {
            "Error calling Gemini: ${e.localizedMessage ?: e.toString()}"
        }
    }

    suspend fun generateFlashcards(sourceText: String): List<Pair<String, String>> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Return some default generated cards when API key isn't provided/working
            return listOf(
                "Active Recall question: limits" to "Concept definition: f(x) approaches L as x approaches c",
                "Self-Study test" to "Enter Gemini key in Secrets to auto-generate from any textbook chapter!"
            )
        }

        val prompt = """
            Based on the following textbook snippet or note content, generate a list of exactly 4 active-recall study flashcards as a semi-colon separated string of Back-And-Front values.
            Use this exact format for each card:
            Question 1 | Answer 1 ; Question 2 | Answer 2 ; Question 3 | Answer 3 ; Question 4 | Answer 4
            
            Keep the questions concise and highly focused on key facts or formulas. Keep answers accurate and short.
            
            Content:
            $sourceText
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(
                Part(text = "You are an automated flashcard generator. Output only the requested semi-colon separated cards, no conversational text or codeblocks.")
            )),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return emptyList()

            // Parse response formatting: Question 1 | Answer 1 ; Question 2 | Answer 2
            val cards = mutableListOf<Pair<String, String>>()
            val parts = responseText.split(";")
            for (p in parts) {
                if (p.contains("|")) {
                    val sides = p.split("|", limit = 2)
                    val q = sides[0].trim()
                    val a = sides[1].trim()
                    if (q.isNotEmpty() && a.isNotEmpty()) {
                        cards.add(q to a)
                    }
                }
            }
            cards
        } catch (e: Exception) {
            emptyList()
        }
    }
}

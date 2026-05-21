package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Gemini REST API Contracts ---

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
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
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

// --- Our Custom Aether Domain Contract for Structured Response ---

@JsonClass(generateAdapter = true)
data class AetherSatelliteResponse(
    @Json(name = "thoughts") val thoughts: List<AetherSatelliteJson> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AetherSatelliteJson(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "planetName") val planetName: String, // Zenith, Ignis, Musa, Terra
    @Json(name = "importance") val importance: Float // 0.8 to 1.4
)

// --- Retrofit & Moshi Setup ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun analyzeStreamOfConsciousness(text: String): List<AetherSatelliteJson> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is not configured. Please add GEMINI_API_KEY to AI Studio Secrets panel.")
        }

        val systemPrompt = """
            You are Aether Core, the superconscious AI of a highly elegant cognitive orbital task/thought planner.
            Your job is to parse a raw stream-of-consciousness entry from a user into discrete, organized, and actionable tasks or ideas (satellites).
            Each satellite must be mapped to exactly one of our 4 planets representing cognitive domains:
            - "Zenith": Core priorities, high-importance deadlines, direct critical duties, work, chores.
            - "Ignis": Creative ambitions, personal projects, hobbies, dreams, fire passions, sports.
            - "Musa": Transient creative musings, fast captures, poems, journaling items, ambient focus thoughts.
            - "Terra": Daily systems, routines, personal learning/reading, habit loops, house maintenance.

            You MUST reply ONLY with a single JSON object matching this structure:
            {
              "thoughts": [
                {
                  "title": "Clean, short, actionable title (max 5 words)",
                  "content": "A beautiful 1-2 sentence descriptive summary, elaborating or highlighting key action items",
                  "planetName": "Zenith",
                  "importance": 1.2
                }
              ]
            }

            The "importance" field must be a floating number from 0.8 (minor task) to 1.4 (extremely key core priority) based on urgency and scale.
            Ensure planetName matches exactly one of: "Zenith", "Ignis", "Musa", "Terra".
            Divide the user text carefully: if they talk about multiple things, return multiple items. If they talk about only one thing, return one item.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = text)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.5f
            )
        )

        val response = service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Aether Core.")

        // Parse JSON
        val adapter = moshi.adapter(AetherSatelliteResponse::class.java)
        val result = adapter.fromJson(responseText)
        return result?.thoughts ?: emptyList()
    }
}

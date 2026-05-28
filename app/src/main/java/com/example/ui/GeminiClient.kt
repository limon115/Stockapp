// Architected by Khalid Hasan Limon
package com.example.ui

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Data Classes for UPCitemdb ---
data class UpcItem(
    val title: String?,
    val brand: String? = null,
    val description: String? = null
)

data class UpcResponse(
    val code: String?,
    val items: List<UpcItem>?
)

// --- Data Classes for Gemini API REST payloads ---
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiGenerationConfig(val responseMimeType: String? = "application/json", val temperature: Float? = 0.5f)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

data class SkuProductSuggestion(
    val name: String,
    val category: String,
    val cost: Double,
    val explanation: String,
    val hasAiIntelligence: Boolean = true
)

// --- Retrofit API Interfaces ---
interface UpcItemDbApiService {
    @GET("prod/trial/lookup")
    suspend fun lookupUpc(
        @Query("upc") upc: String
    ): UpcResponse
}

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val UPC_BASE_URL = "https://api.upcitemdb.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private val upcApiService: UpcItemDbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(UPC_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UpcItemDbApiService::class.java)
    }

    /**
     * Looks up the barcode title on UPCitemdb trial API.
     */
    private suspend fun lookupUpcTitle(sku: String): String? {
        return try {
            val response = upcApiService.lookupUpc(sku)
            if (response.code == "OK" && !response.items.isNullOrEmpty()) {
                response.items.firstOrNull()?.title
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "UPCitemdb list lookup failed: ${e.message}", e)
            null
        }
    }

    /**
     * Attempts to resolve product details from the SKU using the hybrid two-step pipeline.
     */
    suspend fun suggestProductForSku(sku: String): SkuProductSuggestion {
        val trimmedSku = sku.trim()
        if (trimmedSku.isEmpty()) {
            return SkuProductSuggestion(
                name = "Unknown SKU",
                category = "Manual Entry",
                cost = 0.0,
                explanation = "Not found in global database. (Empty SKU passed)"
            )
        }

        // Step 1: UPCitemdb Lookup
        val foundTitle = lookupUpcTitle(trimmedSku)
        
        if (foundTitle.isNullOrBlank()) {
            Log.d(TAG, "SKU '$trimmedSku' lookup failed or not found. Returning negative lookup default.")
            return SkuProductSuggestion(
                name = "Unknown SKU",
                category = "Manual Entry",
                cost = 0.0,
                explanation = "Not found in global database."
            )
        }

        Log.d(TAG, "UPC resolved successfully! Title: '$foundTitle'. Passing to Gemini for classification.")

        // Step 2: The Gemini Formatting
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check for placeholder or blank API key
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is unconfigured. Returning raw found item from database.")
            return SkuProductSuggestion(
                name = foundTitle,
                category = "Unclassified",
                cost = 0.0,
                explanation = "Resolved from global UPC database. (Gemini API Key unconfigured for classification)",
                hasAiIntelligence = false
            )
        }

        val prompt = """
            You are an expert commercial retail lookup and classification system.
            We have scanned or entered a barcode/SKU and successfully resolved its official global catalog title to: "$foundTitle".
            Your task is to classify this exact product name and strictly format it into our existing JSON structure.
            Provide:
            1. "name": Use the exact same name or a slightly cleaned/nicer version of "$foundTitle".
            2. "category": A broad, industry-standard category (e.g. "Beverages", "Spices & Condiments", "Cosmetics", "Electronics Accessories").
            3. "cost": Estimate a realistic wholesale item-cost (floating-point double in Bangladesh Taka/BDT).
            4. "explanation": A 1-sentence brand/product explanation of why this fits. Specify that it was "Resolved from global UPC database".
            
            Respond only with a single, valid JSON object in the exact structure below. Avoid markdown, wrap, trailing commas, or any extra text.
            {
              "name": "Product Name",
              "category": "Product Category",
              "cost": 150.00,
              "explanation": "Specific brand description matching typical barcode standards."
            }
        """.trimIndent()

        try {
            val requestPayload = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(prompt))
                    )
                )
            )

            val response = apiService.generateContent(apiKey, requestPayload)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (!jsonText.isNullOrBlank()) {
                val adapter = moshi.adapter(SkuProductSuggestion::class.java).failOnUnknown()
                // Clean markdown code blocks if the model ignored responseMimeType setting
                val cleanedJsonText = cleanMarkdownFences(jsonText)
                val parsed = adapter.fromJson(cleanedJsonText)
                if (parsed != null) {
                    return parsed.copy(hasAiIntelligence = true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API service invocation failed: ${e.message}", e)
        }

        // Smart fallback on Gemini failure with successful UPC lookup
        return SkuProductSuggestion(
            name = foundTitle,
            category = "Unclassified",
            cost = 120.00,
            explanation = "Resolved from global UPC database. (Gemini classification failed)",
            hasAiIntelligence = false
        )
    }

    suspend fun suggestProductForName(productName: String): SkuProductSuggestion {
        val trimmedName = productName.trim()
        if (trimmedName.isEmpty()) {
            return SkuProductSuggestion(
                name = "Unknown Product",
                category = "Manual Entry",
                cost = 0.0,
                explanation = "Empty product name passed.",
                hasAiIntelligence = false
            )
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is unconfigured. Returning default classification.")
            return SkuProductSuggestion(
                name = trimmedName,
                category = "Unclassified",
                cost = 0.0,
                explanation = "Manual entry. (Gemini API Key unconfigured)",
                hasAiIntelligence = false
            )
        }

        val prompt = """
            You are an expert commercial retail classification system.
            The user has manually entered the product name: "$trimmedName" without a SKU.
            Your task is to classify this exact product name and strictly format it into our existing JSON structure.
            Provide:
            1. "name": Use a slightly cleaned/nicer version of "$trimmedName" if needed.
            2. "category": A broad, industry-standard category (e.g. "Beverages", "Spices", "Electronics").
            3. "cost": Estimate a realistic wholesale item-cost (floating-point double in Bangladesh Taka/BDT).
            4. "explanation": A 1-sentence explanation of why it fits this category based on the name.
            
            Respond only with a single, valid JSON object in the exact structure below. Avoid markdown, wrap, trailing commas, or any extra text.
            {
              "name": "Product Name",
              "category": "Product Category",
              "cost": 150.00,
              "explanation": "Specific category matching based on name."
            }
        """.trimIndent()

        try {
            val requestPayload = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(prompt))
                    )
                )
            )

            val response = apiService.generateContent(apiKey, requestPayload)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (!jsonText.isNullOrBlank()) {
                val adapter = moshi.adapter(SkuProductSuggestion::class.java).failOnUnknown()
                val cleanedJsonText = cleanMarkdownFences(jsonText)
                val parsed = adapter.fromJson(cleanedJsonText)
                if (parsed != null) {
                    return parsed.copy(hasAiIntelligence = true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API service invocation failed for product name: ${e.message}", e)
        }

        return SkuProductSuggestion(
            name = trimmedName,
            category = "Unclassified",
            cost = 0.0,
            explanation = "Manual prediction failed.",
            hasAiIntelligence = false
        )
    }

    private fun cleanMarkdownFences(rawText: String): String {
        var clean = rawText.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }
}

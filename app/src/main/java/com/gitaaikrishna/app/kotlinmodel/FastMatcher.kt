package com.gitaaikrishna.app.kotlinmodel

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Fast keyword-based verse matcher - NO API CALLS, instant results
 */
class FastMatcher(private val context: Context) {
    
    private var verses: List<EnrichedVerse> = emptyList()
    private var initialized = false
    
    // Emotion keywords mapping
    private val emotionKeywords = mapOf(
        "anxiety" to listOf("worried", "anxious", "nervous", "stress", "tension", "fear", "panic", "overthink", "restless", "uneasy", "scared", "चिंता", "घबराहट"),
        "grief" to listOf("sad", "grief", "loss", "death", "mourning", "crying", "tears", "miss", "gone", "died", "दुख", "शोक", "मृत्यु"),
        "anger" to listOf("angry", "rage", "furious", "frustrated", "irritated", "mad", "hate", "annoyed", "क्रोध", "गुस्सा"),
        "confusion" to listOf("confused", "lost", "don't know", "uncertain", "unclear", "doubt", "which path", "what to do", "भ्रम", "संशय"),
        "fear" to listOf("afraid", "scared", "terrified", "fear", "phobia", "worry", "डर", "भय"),
        "loneliness" to listOf("lonely", "alone", "isolated", "no one", "abandoned", "अकेला", "एकांत"),
        "hopelessness" to listOf("hopeless", "give up", "no hope", "pointless", "meaningless", "निराशा", "हताश"),
        "burnout" to listOf("tired", "exhausted", "burnout", "drained", "overwhelmed", "थका", "थकान"),
        "guilt" to listOf("guilty", "regret", "mistake", "wrong", "sorry", "ashamed", "अपराध", "पछतावा"),
        "attachment" to listOf("attached", "let go", "holding on", "can't leave", "obsessed", "मोह", "आसक्ति")
    )
    
    // Therapeutic responses for each emotion
    val therapeuticResponses = mapOf(
        "anxiety" to TherapeuticResponse(
            english = "I hear you. Anxiety can feel like carrying a heavy weight that never goes away. But remember - this moment will pass. Let's take a breath together and see what Krishna says about finding peace amidst chaos...",
            hindi = "मैं आपकी बात सुन रहा हूं। चिंता ऐसे लगती है जैसे कोई भारी बोझ जो कभी नहीं हटता। लेकिन याद रखें - यह पल बीत जाएगा। आइए साथ में एक गहरी सांस लें और देखें कृष्ण ने शांति के बारे में क्या कहा..."
        ),
        "grief" to TherapeuticResponse(
            english = "I'm so sorry for your pain. Grief is the price we pay for love, and it's okay to feel this deeply. You don't have to be strong right now. Krishna spoke about the eternal nature of the soul to comfort those who mourn...",
            hindi = "आपके दर्द के लिए मुझे दुख है। शोक वह कीमत है जो हम प्रेम के लिए चुकाते हैं। अभी मजबूत होने की जरूरत नहीं है। कृष्ण ने आत्मा की अमरता के बारे में बताया..."
        ),
        "anger" to TherapeuticResponse(
            english = "Your anger is valid. It often comes from feeling hurt or unheard. Let's not push it away, but understand what it's trying to tell us. Krishna spoke about transforming this fire within...",
            hindi = "आपका गुस्सा सही है। यह अक्सर दर्द या अनसुना महसूस करने से आता है। इसे दूर न धकेलें, समझें यह क्या कह रहा है। कृष्ण ने इस आग को बदलने का मार्ग बताया..."
        ),
        "confusion" to TherapeuticResponse(
            english = "It's okay to not have all the answers. Confusion is often the doorway to clarity. The fact that you're seeking shows wisdom. Let me share what Krishna told Arjuna when he too stood confused...",
            hindi = "सभी जवाब न होना ठीक है। भ्रम अक्सर स्पष्टता का द्वार होता है। आपका खोजना ही बुद्धिमानी है। देखिए कृष्ण ने भ्रमित अर्जुन को क्या कहा..."
        ),
        "fear" to TherapeuticResponse(
            english = "Fear is our mind trying to protect us, but sometimes it holds us back. You're brave for facing it. Krishna gave Arjuna courage with these words...",
            hindi = "भय हमारे मन की रक्षा करने की कोशिश है, लेकिन कभी-कभी यह हमें रोकता है। इसका सामना करना बहादुरी है। कृष्ण ने अर्जुन को साहस दिया..."
        ),
        "loneliness" to TherapeuticResponse(
            english = "Loneliness can feel so heavy. But reaching out right now shows your strength. You are not truly alone - there's a presence that's always with you. Krishna promised this...",
            hindi = "अकेलापन बहुत भारी लग सकता है। लेकिन अभी संपर्क करना आपकी ताकत है। आप वास्तव में अकेले नहीं हैं। कृष्ण ने यह वादा किया..."
        ),
        "hopelessness" to TherapeuticResponse(
            english = "When hope feels far away, please know that darkness always precedes dawn. You reaching out is itself a spark of hope. Krishna spoke about finding light in the darkest moments...",
            hindi = "जब आशा दूर लगे, याद रखें अंधेरे के बाद ही सुबह आती है। आपका संपर्क करना ही आशा की किरण है। कृष्ण ने अंधेरे में रोशनी खोजने की बात कही..."
        ),
        "burnout" to TherapeuticResponse(
            english = "You've been giving so much. It's okay to rest. You deserve care too. Let me share what Krishna said about action without exhaustion...",
            hindi = "आपने बहुत कुछ दिया है। आराम करना ठीक है। आप भी देखभाल के हकदार हैं। देखिए कृष्ण ने थकान के बिना कर्म के बारे में क्या कहा..."
        ),
        "guilt" to TherapeuticResponse(
            english = "Guilt shows you have a conscience. But carrying it forever doesn't serve you. Krishna spoke about moving forward, even after mistakes...",
            hindi = "अपराधबोध दिखाता है कि आपकी अंतरात्मा जागृत है। लेकिन इसे हमेशा ढोना जरूरी नहीं। कृष्ण ने गलतियों के बाद आगे बढ़ने की बात कही..."
        ),
        "attachment" to TherapeuticResponse(
            english = "Letting go is one of the hardest things. Our attachments come from love. Krishna taught the art of loving without clinging...",
            hindi = "छोड़ना सबसे कठिन है। हमारा मोह प्रेम से आता है। कृष्ण ने बिना बंधे प्रेम करने की कला सिखाई..."
        )
    )
    
    data class TherapeuticResponse(
        val english: String,
        val hindi: String
    )
    
    data class MatchResult(
        val verse: EnrichedVerse,
        val emotion: String,
        val therapeuticResponse: TherapeuticResponse,
        val confidence: Float
    )
    
    fun initialize() {
        if (initialized) return
        
        try {
            val json = context.assets.open("KotlinModel/enriched_gita_formatted.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<EnrichedVerse>>() {}.type
            verses = Gson().fromJson(json, type)
            initialized = true
            Log.i("FastMatcher", "Loaded ${verses.size} verses for fast matching")
        } catch (e: Exception) {
            Log.e("FastMatcher", "Failed to load verses", e)
        }
    }
    
    /**
     * FAST matching - no API calls, instant results
     */
    fun match(query: String): MatchResult? {
        if (!initialized) initialize()
        if (verses.isEmpty()) return null
        
        val lowerQuery = query.lowercase()
        
        // Detect emotion from keywords
        var detectedEmotion = "confusion" // default
        var maxKeywordMatches = 0
        
        for ((emotion, keywords) in emotionKeywords) {
            val matches = keywords.count { keyword -> lowerQuery.contains(keyword) }
            if (matches > maxKeywordMatches) {
                maxKeywordMatches = matches
                detectedEmotion = emotion
            }
        }
        
        // Map emotion to verse category
        val categoryMap = mapOf(
            "anxiety" to "Anxiety",
            "grief" to "Grief",
            "anger" to "Anger",
            "confusion" to "Intellectual Doubt",
            "fear" to "Anxiety",
            "loneliness" to "Grief",
            "hopelessness" to "Grief",
            "burnout" to "Burnout",
            "guilt" to "Moral Dilemma",
            "attachment" to "Attachment"
        )
        
        val category = categoryMap[detectedEmotion] ?: "Anxiety"
        
        // Find verses matching this category
        val matchingVerses = verses.filter { 
            it.emotion_category?.contains(category, ignoreCase = true) == true 
        }
        
        // Pick a random verse from matching ones (or first 5 for variety)
        val selectedVerse = if (matchingVerses.isNotEmpty()) {
            matchingVerses.shuffled().first()
        } else {
            verses.shuffled().first()
        }
        
        val therapeutic = therapeuticResponses[detectedEmotion] ?: TherapeuticResponse(
            english = "I understand what you're going through. Let me share what Krishna said...",
            hindi = "मैं समझता हूं। देखिए कृष्ण ने क्या कहा..."
        )
        
        return MatchResult(
            verse = selectedVerse,
            emotion = detectedEmotion,
            therapeuticResponse = therapeutic,
            confidence = if (maxKeywordMatches > 0) 0.8f else 0.5f
        )
    }
}


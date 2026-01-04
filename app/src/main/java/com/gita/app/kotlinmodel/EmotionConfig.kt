package com.gita.app.kotlinmodel

import androidx.compose.ui.graphics.Color

/**
 * Comprehensive emotion configuration with rich metadata for better matching and beautiful UI.
 * Each emotion has:
 * - Contextual phrases for better embedding-based matching
 * - Visual properties (emoji, colors, gradients, glow effects)
 * - Sub-emotions for nuanced understanding
 * - Intensity levels for multi-dimensional matching
 */
object EmotionConfig {
    
    data class EmotionData(
        val id: String,
        val displayName: String,
        val emoji: String,
        val primaryColor: Long,      // Hex color for primary accent
        val secondaryColor: Long,    // Hex color for secondary/gradient
        val gradientColors: List<Long>, // Full gradient for backgrounds
        val contextualPhrases: List<String>, // Rich phrases for better embedding matching
        val subEmotions: List<String>,
        val comfortingMessage: String, // Short message to show with detection
        val icon: String = "🔮",  // Default icon
        val glowColor: Long = primaryColor, // Color for glow effects
        val intensity: Float = 0.7f, // Default intensity for visual effects
        val relatedEmotions: List<String> = emptyList(), // Emotions often co-occurring
        val sanskritName: String = "", // Sanskrit name for the emotion
        val healingMantra: String = "" // Short mantra for meditation
    )
    
    // Warm, earthy palette inspired by ancient manuscripts
    private val WARM_GOLD = 0xFFC9A227L
    private val DEEP_SAFFRON = 0xFFFF6F00L
    private val SACRED_RED = 0xFFC62828L
    private val CALM_TEAL = 0xFF00897BL
    private val WISDOM_PURPLE = 0xFF5E35B1L
    private val PEACEFUL_BLUE = 0xFF1976D2L
    private val EARTH_BROWN = 0xFF6D4C41L
    private val LOTUS_PINK = 0xFFD81B60L
    private val FOREST_GREEN = 0xFF2E7D32L
    private val TWILIGHT_INDIGO = 0xFF303F9FL
    
    val emotions: Map<String, EmotionData> = mapOf(
        "Anxiety" to EmotionData(
            id = "Anxiety",
            displayName = "Anxiety",
            emoji = "😰",
            primaryColor = 0xFF7E57C2L,  // Soft purple
            secondaryColor = 0xFFB39DDBL,
            gradientColors = listOf(0xFFEDE7F6L, 0xFFD1C4E9L, 0xFFB39DDBL, 0xFF7E57C2L),
            contextualPhrases = listOf(
                "I feel anxious and worried about the future",
                "My mind is racing with fearful thoughts",
                "I can't stop worrying about what might happen",
                "I feel overwhelmed by uncertainty",
                "Nervous energy is consuming me",
                "I'm stressed and can't find peace",
                "What if everything goes wrong",
                "I'm constantly on edge and can't relax",
                "The unknown terrifies me",
                "I feel paralyzed by anxious thoughts",
                "My heart races when I think about tomorrow",
                "I'm drowning in worry and stress"
            ),
            subEmotions = listOf("worry", "nervousness", "stress", "unease", "restlessness", "panic", "dread", "tension", "apprehension"),
            comfortingMessage = "Your worries are heard. Let wisdom guide you to stillness.",
            icon = "🌊",
            glowColor = 0xFF9575CDL,
            relatedEmotions = listOf("Fear", "Confusion", "Burnout"),
            sanskritName = "चिन्ता (Chintā)",
            healingMantra = "ॐ शान्ति (Om Shanti)"
        ),
        
        "Grief" to EmotionData(
            id = "Grief",
            displayName = "Grief",
            emoji = "😢",
            primaryColor = 0xFF5C6BC0L,  // Indigo blue
            secondaryColor = 0xFF9FA8DAL,
            gradientColors = listOf(0xFFE8EAF6L, 0xFFC5CAE9L, 0xFF9FA8DAL, 0xFF5C6BC0L),
            contextualPhrases = listOf(
                "I am grieving a deep loss",
                "My heart is broken and heavy with sorrow",
                "I feel profound sadness that won't lift",
                "I miss someone or something deeply",
                "I'm mourning what I've lost",
                "The weight of loss is crushing me",
                "How do I cope with this unbearable pain",
                "I feel empty inside after losing them",
                "Life seems meaningless without them",
                "I can't accept that they're gone",
                "My soul aches with this loss",
                "The grief never seems to end"
            ),
            subEmotions = listOf("sorrow", "mourning", "heartbreak", "loss", "melancholy", "desolation", "bereavement", "anguish"),
            comfortingMessage = "In the depths of grief, eternal wisdom offers solace.",
            icon = "🕯️",
            glowColor = 0xFF7986CBL,
            relatedEmotions = listOf("Loneliness", "Hopelessness", "Attachment"),
            sanskritName = "शोक (Śoka)",
            healingMantra = "ॐ नमः शिवाय"
        ),
        
        "Anger" to EmotionData(
            id = "Anger",
            displayName = "Anger",
            emoji = "😤",
            primaryColor = SACRED_RED,
            secondaryColor = 0xFFEF5350L,
            gradientColors = listOf(0xFFFFEBEEL, 0xFFFFCDD2L, 0xFFEF9A9AL, 0xFFC62828L),
            contextualPhrases = listOf(
                "I am filled with rage and frustration",
                "Anger is burning inside me",
                "I feel resentful and bitter",
                "I can't control my temper",
                "I'm furious about injustice",
                "Wrath is consuming my peace",
                "I want to explode with rage",
                "How do I let go of this burning anger",
                "I feel betrayed and furious",
                "My blood boils when I think about it",
                "I'm so mad I can't think straight",
                "The injustice makes me want to scream"
            ),
            subEmotions = listOf("rage", "frustration", "resentment", "irritation", "fury", "bitterness", "wrath", "hostility", "indignation"),
            comfortingMessage = "Transform the fire of anger into the light of understanding.",
            icon = "🔥",
            glowColor = 0xFFE53935L,
            relatedEmotions = listOf("Jealousy", "Pride", "Impatience"),
            sanskritName = "क्रोध (Krodha)",
            healingMantra = "ॐ शान्ति शान्ति शान्तिः"
        ),
        
        "Attachment" to EmotionData(
            id = "Attachment",
            displayName = "Attachment",
            emoji = "🔗",
            primaryColor = LOTUS_PINK,
            secondaryColor = 0xFFF48FB1L,
            gradientColors = listOf(0xFFFCE4ECL, 0xFFF8BBD0L, 0xFFF48FB1L, 0xFFD81B60L),
            contextualPhrases = listOf(
                "I'm too attached to outcomes and people",
                "I can't let go of what I desire",
                "My clinging is causing me suffering",
                "I'm dependent on external things for happiness",
                "Possessiveness is controlling my mind",
                "I fear losing what I love",
                "How do I detach without becoming cold",
                "I cling to relationships out of fear",
                "My desires are consuming me",
                "I need certain outcomes to be happy",
                "I'm obsessed with things I can't control",
                "Letting go feels like losing myself"
            ),
            subEmotions = listOf("clinging", "possessiveness", "dependency", "desire", "craving", "obsession", "neediness", "grasping"),
            comfortingMessage = "True freedom comes from loving without chains.",
            icon = "🦋",
            glowColor = 0xFFEC407AL,
            relatedEmotions = listOf("Fear", "Grief", "Result-Obsession"),
            sanskritName = "आसक्ति (Āsakti)",
            healingMantra = "ॐ वैराग्याय नमः"
        ),
        
        "Burnout" to EmotionData(
            id = "Burnout",
            displayName = "Burnout",
            emoji = "🔋",
            primaryColor = 0xFF78909CL,  // Blue grey
            secondaryColor = 0xFFB0BEC5L,
            gradientColors = listOf(0xFFECEFF1L, 0xFFCFD8DCL, 0xFFB0BEC5L, 0xFF78909CL),
            contextualPhrases = listOf(
                "I am completely exhausted and depleted",
                "I have nothing left to give",
                "I'm burned out from constant work",
                "Fatigue has taken over my life",
                "I feel empty and drained",
                "I've lost motivation for everything",
                "I'm running on empty and can't stop",
                "Every task feels like climbing a mountain",
                "I've forgotten what rest feels like",
                "My energy is completely depleted",
                "I'm exhausted but can't slow down",
                "Working hard but getting nowhere"
            ),
            subEmotions = listOf("exhaustion", "depletion", "fatigue", "emptiness", "overwhelm", "weariness", "tiredness", "apathy"),
            comfortingMessage = "Rest is sacred. Even the cosmos pauses between breaths.",
            icon = "🌙",
            glowColor = 0xFF90A4AEL,
            relatedEmotions = listOf("Hopelessness", "Anxiety", "Confusion"),
            sanskritName = "थकान (Thakān)",
            healingMantra = "ॐ विश्राम (Om Viśrāma)"
        ),
        
        "Identity Crisis" to EmotionData(
            id = "Identity Crisis",
            displayName = "Identity Crisis",
            emoji = "🪞",
            primaryColor = WISDOM_PURPLE,
            secondaryColor = 0xFF9575CDL,
            gradientColors = listOf(0xFFF3E5F5L, 0xFFE1BEE7L, 0xFFCE93D8L, 0xFF5E35B1L),
            contextualPhrases = listOf(
                "I don't know who I am anymore",
                "I've lost my sense of self",
                "I'm questioning my purpose and identity",
                "I feel disconnected from myself",
                "I don't recognize the person I've become",
                "My values and beliefs are shaken",
                "What is my true nature and purpose",
                "I feel like I'm living someone else's life",
                "Who am I beneath all these roles",
                "I've lost touch with my authentic self",
                "My life path seems unclear and uncertain",
                "I feel like a stranger to myself"
            ),
            subEmotions = listOf("self-doubt", "disconnection", "confusion about self", "purposelessness", "existential questioning", "emptiness", "meaninglessness"),
            comfortingMessage = "Beyond roles and masks lies your eternal, unchanging Self.",
            icon = "💫",
            glowColor = 0xFF7E57C2L,
            relatedEmotions = listOf("Confusion", "Loneliness", "Intellectual Doubt"),
            sanskritName = "आत्म-संशय (Ātma-saṃśaya)",
            healingMantra = "ॐ तत् त्वम् असि (Tat Tvam Asi)"
        ),
        
        "Intellectual Doubt" to EmotionData(
            id = "Intellectual Doubt",
            displayName = "Intellectual Doubt",
            emoji = "🤔",
            primaryColor = CALM_TEAL,
            secondaryColor = 0xFF4DB6ACL,
            gradientColors = listOf(0xFFE0F2F1L, 0xFFB2DFDB, 0xFF80CBC4L, 0xFF00897BL),
            contextualPhrases = listOf(
                "I'm questioning everything I believed in",
                "I have doubts about my faith and path",
                "Skepticism is overwhelming my mind",
                "I can't find answers that satisfy me",
                "I'm intellectually confused and uncertain",
                "My beliefs are being challenged",
                "Does God exist and does life have meaning",
                "I can't reconcile logic with spirituality",
                "Science and faith seem incompatible",
                "I need proof but none satisfies me",
                "My rational mind questions ancient wisdom",
                "Philosophy leaves me more confused"
            ),
            subEmotions = listOf("skepticism", "questioning", "uncertainty", "disbelief", "philosophical confusion", "agnosticism", "rationalism"),
            comfortingMessage = "Doubt is the doorway to deeper understanding.",
            icon = "🔍",
            glowColor = 0xFF26A69AL,
            relatedEmotions = listOf("Confusion", "Identity Crisis", "Hopelessness"),
            sanskritName = "संशय (Saṃśaya)",
            healingMantra = "ॐ ज्ञानं परमं ध्येयम्"
        ),
        
        "Loneliness" to EmotionData(
            id = "Loneliness",
            displayName = "Loneliness",
            emoji = "🏝️",
            primaryColor = PEACEFUL_BLUE,
            secondaryColor = 0xFF64B5F6L,
            gradientColors = listOf(0xFFE3F2FDL, 0xFFBBDEFBL, 0xFF90CAF9L, 0xFF1976D2L),
            contextualPhrases = listOf(
                "I feel completely alone in this world",
                "No one understands me or my struggles",
                "I'm isolated and disconnected from others",
                "I feel abandoned and forgotten",
                "Deep loneliness is eating at my soul",
                "I long for connection and belonging",
                "Even in crowds I feel utterly alone",
                "I have no one to share my deepest feelings",
                "The silence of solitude is deafening",
                "I feel invisible to everyone around me",
                "My heart aches for true companionship",
                "I'm surrounded by people but still lonely"
            ),
            subEmotions = listOf("isolation", "abandonment", "disconnection", "solitude", "alienation", "neglect", "estrangement"),
            comfortingMessage = "You are never alone. The Divine dwells within you always.",
            icon = "🤍",
            glowColor = 0xFF42A5F5L,
            relatedEmotions = listOf("Grief", "Hopelessness", "Identity Crisis"),
            sanskritName = "एकाकित्व (Ekākitva)",
            healingMantra = "ॐ एकत्वं सर्वभूतेषु"
        ),
        
        "Moral Dilemma" to EmotionData(
            id = "Moral Dilemma",
            displayName = "Moral Dilemma",
            emoji = "⚖️",
            primaryColor = EARTH_BROWN,
            secondaryColor = 0xFF8D6E63L,
            gradientColors = listOf(0xFFEFEBE9L, 0xFFD7CCC8L, 0xFFBCAAA4L, 0xFF6D4C41L),
            contextualPhrases = listOf(
                "I don't know what's right or wrong",
                "I'm torn between two paths",
                "My ethics are being tested",
                "I face a difficult moral choice",
                "I'm conflicted about what I should do",
                "Dharma and personal desire are clashing",
                "Should I choose duty or my heart's desire",
                "Every choice seems to hurt someone",
                "I'm stuck between loyalty and truth",
                "What is the right thing to do here",
                "My conscience pulls me in two directions",
                "Doing right feels wrong and vice versa"
            ),
            subEmotions = listOf("ethical conflict", "inner conflict", "conscience struggle", "duty vs desire", "moral confusion", "righteousness"),
            comfortingMessage = "When torn between paths, look to your highest truth.",
            icon = "🧭",
            glowColor = 0xFF795548L,
            relatedEmotions = listOf("Guilt", "Confusion", "Intellectual Doubt"),
            sanskritName = "धर्म-संकट (Dharma-saṅkaṭa)",
            healingMantra = "ॐ धर्मो रक्षति रक्षितः"
        ),
        
        "Pride" to EmotionData(
            id = "Pride",
            displayName = "Pride",
            emoji = "👑",
            primaryColor = WARM_GOLD,
            secondaryColor = 0xFFFFD54FL,
            gradientColors = listOf(0xFFFFF8E1L, 0xFFFFECB3L, 0xFFFFE082L, 0xFFC9A227L),
            contextualPhrases = listOf(
                "My ego is getting in my way",
                "I'm too proud to accept help",
                "Arrogance is blinding me",
                "I think I'm better than others",
                "My pride is causing suffering",
                "I can't humble myself",
                "I deserve more recognition than I get",
                "Why can't others see my worth",
                "I refuse to admit when I'm wrong",
                "My achievements define my value",
                "I look down on those less accomplished",
                "Ego prevents me from growing"
            ),
            subEmotions = listOf("ego", "arrogance", "vanity", "superiority", "hubris", "self-importance", "narcissism", "conceit"),
            comfortingMessage = "True greatness lies in humble service, not in acclaim.",
            icon = "🪷",
            glowColor = 0xFFFFCA28L,
            relatedEmotions = listOf("Anger", "Jealousy", "Result-Obsession"),
            sanskritName = "अहंकार (Ahaṅkāra)",
            healingMantra = "ॐ नम इति"
        ),
        
        "Result-Obsession" to EmotionData(
            id = "Result-Obsession",
            displayName = "Result-Obsession",
            emoji = "🎯",
            primaryColor = DEEP_SAFFRON,
            secondaryColor = 0xFFFFB74DL,
            gradientColors = listOf(0xFFFFF3E0L, 0xFFFFE0B2L, 0xFFFFCC80L, 0xFFFF6F00L),
            contextualPhrases = listOf(
                "I'm obsessed with achieving specific outcomes",
                "I can't let go of expectations",
                "Success and failure consume my thoughts",
                "I'm too focused on results, not the journey",
                "I need to control every outcome",
                "Fear of failure is paralyzing me",
                "What if I don't succeed after all this effort",
                "I measure my worth by my achievements",
                "The process means nothing without results",
                "I can't enjoy the journey only the destination",
                "My happiness depends on specific outcomes",
                "Failure is not an option for me"
            ),
            subEmotions = listOf("perfectionism", "control", "expectation anxiety", "success obsession", "fear of failure", "achievement addiction", "outcome fixation"),
            comfortingMessage = "Surrender the fruits. Find freedom in the action itself.",
            icon = "🎋",
            glowColor = 0xFFFFA726L,
            relatedEmotions = listOf("Anxiety", "Pride", "Attachment"),
            sanskritName = "फल-आसक्ति (Phala-āsakti)",
            healingMantra = "कर्मण्येवाधिकारस्ते"
        ),
        
        // New emotions from extended dataset
        "Fear" to EmotionData(
            id = "Fear",
            displayName = "Fear",
            emoji = "😨",
            primaryColor = 0xFF455A64L,  // Dark blue grey
            secondaryColor = 0xFF90A4AEL,
            gradientColors = listOf(0xFFECEFF1L, 0xFFCFD8DCL, 0xFFB0BEC5L, 0xFF455A64L),
            contextualPhrases = listOf(
                "I am paralyzed by fear",
                "I'm terrified of what might happen",
                "Fear is controlling my life",
                "I feel constant dread and apprehension",
                "I'm scared to take any action",
                "Terror grips my heart",
                "I'm afraid of the unknown future",
                "Fear prevents me from taking risks",
                "I freeze when faced with challenges",
                "The fear of failure haunts me",
                "I'm scared of losing everything",
                "Phobia and anxiety rule my decisions"
            ),
            subEmotions = listOf("terror", "dread", "apprehension", "phobia", "horror", "fright", "panic", "cowardice", "timidity"),
            comfortingMessage = "Fear dissolves in the light of true knowledge.",
            icon = "🛡️",
            glowColor = 0xFF607D8BL,
            relatedEmotions = listOf("Anxiety", "Attachment", "Hopelessness"),
            sanskritName = "भय (Bhaya)",
            healingMantra = "ॐ अभयं सर्वतो दिशम्"
        ),
        
        "Jealousy" to EmotionData(
            id = "Jealousy",
            displayName = "Jealousy",
            emoji = "💚",
            primaryColor = FOREST_GREEN,
            secondaryColor = 0xFF66BB6AL,
            gradientColors = listOf(0xFFE8F5E9L, 0xFFC8E6C9L, 0xFFA5D6A7L, 0xFF2E7D32L),
            contextualPhrases = listOf(
                "I'm envious of others' success",
                "Jealousy is eating me from inside",
                "I resent what others have",
                "I compare myself to everyone",
                "I can't be happy for others",
                "Envy poisons my relationships",
                "Why do others have what I deserve",
                "I feel bitter about their achievements",
                "Comparison is stealing my joy",
                "I can't stop measuring myself against others",
                "Their success feels like my failure",
                "Jealousy is destroying my peace"
            ),
            subEmotions = listOf("envy", "resentment", "covetousness", "bitterness", "comparison", "rivalry", "spite"),
            comfortingMessage = "Your path is unique. Another's treasure doesn't diminish yours.",
            icon = "✨",
            glowColor = 0xFF43A047L,
            relatedEmotions = listOf("Anger", "Pride", "Attachment"),
            sanskritName = "ईर्ष्या (Īrṣyā)",
            healingMantra = "ॐ मुदिता सर्वभूतेषु"
        ),
        
        "Guilt" to EmotionData(
            id = "Guilt",
            displayName = "Guilt",
            emoji = "😔",
            primaryColor = 0xFF5D4037L,  // Dark brown
            secondaryColor = 0xFF8D6E63L,
            gradientColors = listOf(0xFFEFEBE9L, 0xFFD7CCC8L, 0xFFBCAAA4L, 0xFF5D4037L),
            contextualPhrases = listOf(
                "I feel guilty for what I've done",
                "Shame is weighing me down",
                "I can't forgive myself",
                "I've made terrible mistakes",
                "Remorse haunts me constantly",
                "I feel unworthy of forgiveness",
                "My past actions haunt me every day",
                "I carry the burden of my sins",
                "How do I atone for my wrongs",
                "Self-condemnation is crushing my spirit",
                "I don't deserve happiness after what I did",
                "The guilt never lets me rest"
            ),
            subEmotions = listOf("shame", "remorse", "regret", "self-blame", "unworthiness", "contrition", "self-punishment"),
            comfortingMessage = "Every moment is a chance for renewal. Forgiveness begins within.",
            icon = "🕊️",
            glowColor = 0xFF6D4C41L,
            relatedEmotions = listOf("Grief", "Moral Dilemma", "Hopelessness"),
            sanskritName = "पश्चाताप (Paścātāpa)",
            healingMantra = "ॐ क्षमा प्रधानं तपः"
        ),
        
        "Hopelessness" to EmotionData(
            id = "Hopelessness",
            displayName = "Hopelessness",
            emoji = "🌑",
            primaryColor = TWILIGHT_INDIGO,
            secondaryColor = 0xFF7986CBL,
            gradientColors = listOf(0xFFE8EAF6L, 0xFFC5CAE9L, 0xFF9FA8DAL, 0xFF303F9FL),
            contextualPhrases = listOf(
                "I've lost all hope",
                "Nothing will ever get better",
                "I see no way out of this darkness",
                "Life feels meaningless and futile",
                "I'm in complete despair",
                "There's no point in trying anymore",
                "Why bother when nothing changes",
                "I've given up on ever being happy",
                "The future looks bleak and empty",
                "I feel like a failure with no redemption",
                "Hope has abandoned me completely",
                "Despair is my constant companion"
            ),
            subEmotions = listOf("despair", "nihilism", "defeat", "futility", "despondency", "bleakness", "desolation"),
            comfortingMessage = "Even in deepest darkness, dawn is already approaching.",
            icon = "🌅",
            glowColor = 0xFF5C6BC0L,
            relatedEmotions = listOf("Grief", "Burnout", "Loneliness"),
            sanskritName = "निराशा (Nirāśā)",
            healingMantra = "ॐ आशा जीवनं प्राणः"
        ),
        
        "Confusion" to EmotionData(
            id = "Confusion",
            displayName = "Confusion",
            emoji = "😵‍💫",
            primaryColor = 0xFF7B1FA2L,  // Purple
            secondaryColor = 0xFFBA68C8L,
            gradientColors = listOf(0xFFF3E5F5L, 0xFFE1BEE7L, 0xFFCE93D8L, 0xFF7B1FA2L),
            contextualPhrases = listOf(
                "I'm completely lost and confused",
                "I don't know what to do",
                "My mind is in chaos",
                "I can't think clearly",
                "Everything is overwhelming and unclear",
                "I need clarity and direction",
                "Too many choices paralyze me",
                "I don't know which path to take",
                "My thoughts are scattered and unfocused",
                "I can't make sense of my situation",
                "Decision paralysis is crippling me",
                "I'm drowning in mental fog"
            ),
            subEmotions = listOf("bewilderment", "disorientation", "perplexity", "mental fog", "uncertainty", "indecision", "overwhelm"),
            comfortingMessage = "Clarity emerges when the mind becomes still like a lake.",
            icon = "🔮",
            glowColor = 0xFF9C27B0L,
            relatedEmotions = listOf("Anxiety", "Identity Crisis", "Moral Dilemma"),
            sanskritName = "मोह (Moha)",
            healingMantra = "ॐ प्रज्ञानं ब्रह्म"
        ),
        
        "Impatience" to EmotionData(
            id = "Impatience",
            displayName = "Impatience",
            emoji = "⏰",
            primaryColor = 0xFFE65100L,  // Deep orange
            secondaryColor = 0xFFFF9800L,
            gradientColors = listOf(0xFFFFF3E0L, 0xFFFFE0B2L, 0xFFFFCC80L, 0xFFE65100L),
            contextualPhrases = listOf(
                "I can't wait any longer",
                "I want results now",
                "I'm frustrated by slow progress",
                "Time is running out",
                "I need things to happen faster",
                "Waiting is unbearable",
                "Why is everything taking so long",
                "I hate being stuck waiting",
                "Patience is something I completely lack",
                "The delay is driving me crazy",
                "I want instant gratification",
                "Slow progress feels like no progress"
            ),
            subEmotions = listOf("restlessness", "agitation", "urgency", "frustration", "haste", "irritability", "intolerance"),
            comfortingMessage = "Divine timing unfolds perfectly. Trust the journey.",
            icon = "⏳",
            glowColor = 0xFFF57C00L,
            relatedEmotions = listOf("Anger", "Result-Obsession", "Anxiety"),
            sanskritName = "अधीरता (Adhīratā)",
            healingMantra = "ॐ धैर्यं सर्वत्र साधनम्"
        )
    )
    
    /**
     * Get emotion data by ID (case-insensitive)
     */
    fun getEmotion(emotionId: String): EmotionData? {
        return emotions[emotionId] ?: emotions.entries.find { 
            it.key.equals(emotionId, ignoreCase = true) 
        }?.value
    }
    
    /**
     * Get all contextual phrases for better embedding-based matching
     */
    fun getAllContextualPhrasesFor(emotionId: String): List<String> {
        return getEmotion(emotionId)?.contextualPhrases ?: emptyList()
    }
    
    /**
     * Get a representative phrase for embedding (combines emotion name with context)
     */
    fun getEmbeddingPhraseFor(emotionId: String): String {
        val emotion = getEmotion(emotionId) ?: return emotionId
        val phrase = emotion.contextualPhrases.firstOrNull() ?: ""
        return "Feeling $emotionId: $phrase"
    }
    
    /**
     * Get multiple embedding phrases for better multi-phrase matching.
     * Returns top N phrases weighted for semantic diversity.
     */
    fun getMultipleEmbeddingPhrasesFor(emotionId: String, count: Int = 4): List<String> {
        val emotion = getEmotion(emotionId) ?: return listOf(emotionId)
        val phrases = emotion.contextualPhrases.take(count)
        return phrases.map { phrase -> "Feeling ${emotion.displayName}: $phrase" }
    }
    
    /**
     * Get all sub-emotions that should also match this emotion
     */
    fun getSubEmotionsFor(emotionId: String): List<String> {
        return getEmotion(emotionId)?.subEmotions ?: emptyList()
    }
    
    /**
     * Get related emotions for contextual matching
     */
    fun getRelatedEmotionsFor(emotionId: String): List<String> {
        return getEmotion(emotionId)?.relatedEmotions ?: emptyList()
    }
    
    /**
     * Get primary Color from emotion (for Compose)
     */
    fun getPrimaryColor(emotionId: String): Color {
        val colorLong = getEmotion(emotionId)?.primaryColor ?: 0xFF7E57C2L
        return Color(colorLong.toInt().toLong() and 0xFFFFFFFFL)
    }
    
    /**
     * Get glow color for visual effects
     */
    fun getGlowColor(emotionId: String): Color {
        val colorLong = getEmotion(emotionId)?.glowColor ?: 0xFF7E57C2L
        return Color(colorLong.toInt().toLong() and 0xFFFFFFFFL)
    }
    
    /**
     * Get gradient colors for background
     */
    fun getGradientColors(emotionId: String): List<Color> {
        val colors = getEmotion(emotionId)?.gradientColors ?: listOf(0xFFF5F5F5L, 0xFFE0E0E0L)
        return colors.map { Color(it.toInt().toLong() and 0xFFFFFFFFL) }
    }
    
    /**
     * Get Sanskrit name for display
     */
    fun getSanskritName(emotionId: String): String? {
        return getEmotion(emotionId)?.sanskritName?.takeIf { it.isNotBlank() }
    }
    
    /**
     * Get healing mantra for meditation/guidance
     */
    fun getHealingMantra(emotionId: String): String? {
        return getEmotion(emotionId)?.healingMantra?.takeIf { it.isNotBlank() }
    }
    
    /**
     * Check if a query might match this emotion based on sub-emotions
     */
    fun matchesSubEmotion(emotionId: String, query: String): Boolean {
        val subEmotions = getSubEmotionsFor(emotionId)
        val queryLower = query.lowercase()
        return subEmotions.any { sub -> queryLower.contains(sub.lowercase()) }
    }
    
    /**
     * Get emotion by matching sub-emotion keywords in text
     */
    fun findEmotionBySubEmotionKeyword(text: String): EmotionData? {
        val textLower = text.lowercase()
        for ((_, emotion) in emotions) {
            for (subEmotion in emotion.subEmotions) {
                if (textLower.contains(subEmotion.lowercase())) {
                    return emotion
                }
            }
        }
        return null
    }
    
    /**
     * All emotion IDs for iteration
     */
    val allEmotionIds: List<String> = emotions.keys.toList()
    
    /**
     * Get all emotions sorted by visual similarity for UI display
     */
    val emotionsByCategory: Map<String, List<EmotionData>> by lazy {
        mapOf(
            "Struggle" to listOf("Anxiety", "Fear", "Burnout", "Confusion", "Impatience"),
            "Pain" to listOf("Grief", "Loneliness", "Hopelessness", "Guilt"),
            "Conflict" to listOf("Anger", "Jealousy", "Moral Dilemma"),
            "Ego" to listOf("Pride", "Attachment", "Result-Obsession"),
            "Seeking" to listOf("Identity Crisis", "Intellectual Doubt")
        ).mapValues { (_, ids) -> ids.mapNotNull { getEmotion(it) } }
    }
}


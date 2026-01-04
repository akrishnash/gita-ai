"""
Emotion Matching Test Script
Tests the enhanced emotion matching algorithm with multi-phrase weighted averaging.

Usage:
    python test_emotion_matching.py           # Interactive CLI mode
    python test_emotion_matching.py --web     # Launch simple web UI
    python test_emotion_matching.py --batch   # Run batch tests
"""

import json
import os
import sys
from pathlib import Path
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple
import numpy as np
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# ============================================================================
# EMOTION CONFIGURATION (Mirrored from Kotlin EmotionConfig)
# ============================================================================

@dataclass
class EmotionData:
    id: str
    display_name: str
    emoji: str
    contextual_phrases: List[str]
    sub_emotions: List[str]
    comforting_message: str
    icon: str
    related_emotions: List[str]
    sanskrit_name: str
    healing_mantra: str
    empathetic_response: str = ""  # Human-like empathetic response

# Complete emotion configuration matching the Kotlin version
EMOTIONS: Dict[str, EmotionData] = {
    "Anxiety": EmotionData(
        id="Anxiety",
        display_name="Anxiety",
        emoji="😰",
        contextual_phrases=[
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
        ],
        sub_emotions=["worry", "nervousness", "stress", "unease", "restlessness", "panic", "dread", "tension", "apprehension"],
        comforting_message="Your worries are heard. Let wisdom guide you to stillness.",
        icon="🌊",
        related_emotions=["Fear", "Confusion", "Burnout"],
        sanskrit_name="चिन्ता (Chintā)",
        healing_mantra="ॐ शान्ति (Om Shanti)",
        empathetic_response="I understand how overwhelming anxiety can feel - that constant worry that keeps your mind racing. Please know that you're not alone in this. Take a deep breath with me. Krishna understood this very feeling when Arjuna stood paralyzed on the battlefield, and He offered this timeless wisdom..."
    ),
    "Grief": EmotionData(
        id="Grief",
        display_name="Grief",
        emoji="😢",
        contextual_phrases=[
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
        ],
        sub_emotions=["sorrow", "mourning", "heartbreak", "loss", "melancholy", "desolation", "bereavement", "anguish"],
        comforting_message="In the depths of grief, eternal wisdom offers solace.",
        icon="🕯️",
        related_emotions=["Loneliness", "Hopelessness", "Attachment"],
        sanskrit_name="शोक (Śoka)",
        healing_mantra="ॐ नमः शिवाय",
        empathetic_response="I'm so sorry for what you're going through. Grief is one of the most profound pains we can experience, and it's okay to feel this deeply. Your love for what you've lost is reflected in your sorrow. Krishna spoke to Arjuna about the eternal nature of the soul and finding peace through understanding. Here's what He shared..."
    ),
    "Anger": EmotionData(
        id="Anger",
        display_name="Anger",
        emoji="😤",
        contextual_phrases=[
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
        ],
        sub_emotions=["rage", "frustration", "resentment", "irritation", "fury", "bitterness", "wrath", "hostility", "indignation"],
        comforting_message="Transform the fire of anger into the light of understanding.",
        icon="🔥",
        related_emotions=["Jealousy", "Pride", "Impatience"],
        sanskrit_name="क्रोध (Krodha)",
        healing_mantra="ॐ शान्ति शान्ति शान्तिः",
        empathetic_response="I hear the fire in your words, and that anger is valid. Something has hurt you deeply, and it's natural to feel this way. But I also sense that this anger is weighing on you. Krishna spoke about transforming anger into clarity. Let me share what He told Arjuna when he too was burning with emotion..."
    ),
    "Attachment": EmotionData(
        id="Attachment",
        display_name="Attachment",
        emoji="🔗",
        contextual_phrases=[
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
        ],
        sub_emotions=["clinging", "possessiveness", "dependency", "desire", "craving", "obsession", "neediness", "grasping"],
        comforting_message="True freedom comes from loving without chains.",
        icon="🦋",
        related_emotions=["Fear", "Grief", "Result-Obsession"],
        sanskrit_name="आसक्ति (Āsakti)",
        healing_mantra="ॐ वैराग्याय नमः",
        empathetic_response="I understand how hard it is when we hold on so tightly to something or someone. That attachment comes from love, but it can also bring pain. You're not wrong for feeling this way - it shows how deeply you care. Krishna taught about loving freely while finding inner peace. Here's His wisdom..."
    ),
    "Burnout": EmotionData(
        id="Burnout",
        display_name="Burnout",
        emoji="🔋",
        contextual_phrases=[
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
        ],
        sub_emotions=["exhaustion", "depletion", "fatigue", "emptiness", "overwhelm", "weariness", "tiredness", "apathy"],
        comforting_message="Rest is sacred. Even the cosmos pauses between breaths.",
        icon="🌙",
        related_emotions=["Hopelessness", "Anxiety", "Confusion"],
        sanskrit_name="थकान (Thakān)",
        healing_mantra="ॐ विश्राम (Om Viśrāma)",
        empathetic_response="I can feel how tired you are - that deep exhaustion that goes beyond just physical tiredness. You've been giving so much of yourself, and now you're running on empty. Please be gentle with yourself. Even Krishna taught that rest and balance are essential parts of life's journey. Here's what He said..."
    ),
    "Identity Crisis": EmotionData(
        id="Identity Crisis",
        display_name="Identity Crisis",
        emoji="🪞",
        contextual_phrases=[
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
        ],
        sub_emotions=["self-doubt", "disconnection", "confusion about self", "purposelessness", "existential questioning", "emptiness", "meaninglessness"],
        comforting_message="Beyond roles and masks lies your eternal, unchanging Self.",
        icon="💫",
        related_emotions=["Confusion", "Loneliness", "Intellectual Doubt"],
        sanskrit_name="आत्म-संशय (Ātma-saṃśaya)",
        healing_mantra="ॐ तत् त्वम् असि (Tat Tvam Asi)",
        empathetic_response="It takes courage to ask 'Who am I really?' Many people never pause to question their identity. What you're feeling - this sense of not knowing yourself - is actually the beginning of a deeper awakening. Krishna revealed to Arjuna the true nature of the Self. Let me share..."
    ),
    "Intellectual Doubt": EmotionData(
        id="Intellectual Doubt",
        display_name="Intellectual Doubt",
        emoji="🤔",
        contextual_phrases=[
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
        ],
        sub_emotions=["skepticism", "questioning", "uncertainty", "disbelief", "philosophical confusion", "agnosticism", "rationalism"],
        comforting_message="Doubt is the doorway to deeper understanding.",
        icon="🔍",
        related_emotions=["Confusion", "Identity Crisis", "Hopelessness"],
        sanskrit_name="संशय (Saṃśaya)",
        healing_mantra="ॐ ज्ञानं परमं ध्येयम्",
        empathetic_response="Your questioning mind is a gift, not a problem. Doubt, when approached with sincerity, leads to the deepest truths. Arjuna himself questioned everything before Krishna revealed the highest knowledge. Your search for understanding is honored. Here's what Krishna taught..."
    ),
    "Loneliness": EmotionData(
        id="Loneliness",
        display_name="Loneliness",
        emoji="🏝️",
        contextual_phrases=[
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
        ],
        sub_emotions=["isolation", "abandonment", "disconnection", "solitude", "alienation", "neglect", "estrangement"],
        comforting_message="You are never alone. The Divine dwells within you always.",
        icon="🤍",
        related_emotions=["Grief", "Hopelessness", "Identity Crisis"],
        sanskrit_name="एकाकित्व (Ekākitva)",
        healing_mantra="ॐ एकत्वं सर्वभूतेषु",
        empathetic_response="I hear you, and that ache of loneliness is real. But please know this - you reaching out right now shows your strength, not weakness. The truth is, the Divine presence is always with you, even in your most solitary moments. Krishna promised He never abandons those who seek Him. Here's His comfort..."
    ),
    "Moral Dilemma": EmotionData(
        id="Moral Dilemma",
        display_name="Moral Dilemma",
        emoji="⚖️",
        contextual_phrases=[
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
        ],
        sub_emotions=["ethical conflict", "inner conflict", "conscience struggle", "duty vs desire", "moral confusion", "righteousness"],
        comforting_message="When torn between paths, look to your highest truth.",
        icon="🧭",
        related_emotions=["Guilt", "Confusion", "Intellectual Doubt"],
        sanskrit_name="धर्म-संकट (Dharma-saṅkaṭa)",
        healing_mantra="ॐ धर्मो रक्षति रक्षितः",
        empathetic_response="Being torn between what's right and what feels right is one of life's hardest struggles. Arjuna faced exactly this - duty pulling one way, heart pulling another. Your conscience is awake, and that itself is wisdom. Let me share what Krishna revealed about navigating such difficult choices..."
    ),
    "Pride": EmotionData(
        id="Pride",
        display_name="Pride",
        emoji="👑",
        contextual_phrases=[
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
        ],
        sub_emotions=["ego", "arrogance", "vanity", "superiority", "hubris", "self-importance", "narcissism", "conceit"],
        comforting_message="True greatness lies in humble service, not in acclaim.",
        icon="🪷",
        related_emotions=["Anger", "Jealousy", "Result-Obsession"],
        sanskrit_name="अहंकार (Ahaṅkāra)",
        healing_mantra="ॐ नम इति",
        empathetic_response="I appreciate you being honest about this - recognizing ego is harder than it sounds. The Gita teaches that true confidence comes from knowing your inner worth, not from external validation. Even the greatest warriors had to learn humility. Here's Krishna's teaching on this..."
    ),
    "Result-Obsession": EmotionData(
        id="Result-Obsession",
        display_name="Result-Obsession",
        emoji="🎯",
        contextual_phrases=[
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
        ],
        sub_emotions=["perfectionism", "control", "expectation anxiety", "success obsession", "fear of failure", "achievement addiction", "outcome fixation"],
        comforting_message="Surrender the fruits. Find freedom in the action itself.",
        icon="🎋",
        related_emotions=["Anxiety", "Pride", "Attachment"],
        sanskrit_name="फल-आसक्ति (Phala-āsakti)",
        healing_mantra="कर्मण्येवाधिकारस्ते",
        empathetic_response="I understand that drive - wanting to see your efforts succeed, measuring yourself by outcomes. It's so common in our achievement-focused world. But this pressure you feel? Krishna spoke directly about it. His most famous verse is about doing your best and releasing attachment to results. Here it is..."
    ),
    "Fear": EmotionData(
        id="Fear",
        display_name="Fear",
        emoji="😨",
        contextual_phrases=[
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
        ],
        sub_emotions=["terror", "dread", "apprehension", "phobia", "horror", "fright", "panic", "cowardice", "timidity"],
        comforting_message="Fear dissolves in the light of true knowledge.",
        icon="🛡️",
        related_emotions=["Anxiety", "Attachment", "Hopelessness"],
        sanskrit_name="भय (Bhaya)",
        healing_mantra="ॐ अभयं सर्वतो दिशम्",
        empathetic_response="Fear can feel so paralyzing, I know. Your body is trying to protect you, but sometimes it holds you back. Please know that courage isn't the absence of fear - it's acting despite it. Arjuna was terrified too, and Krishna gave him the knowledge that dissolves fear. Here's that wisdom..."
    ),
    "Jealousy": EmotionData(
        id="Jealousy",
        display_name="Jealousy",
        emoji="💚",
        contextual_phrases=[
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
        ],
        sub_emotions=["envy", "resentment", "covetousness", "bitterness", "comparison", "rivalry", "spite"],
        comforting_message="Your path is unique. Another's treasure doesn't diminish yours.",
        icon="✨",
        related_emotions=["Anger", "Pride", "Attachment"],
        sanskrit_name="ईर्ष्या (Īrṣyā)",
        healing_mantra="ॐ मुदिता सर्वभूतेषु",
        empathetic_response="Thank you for being honest about this - jealousy is something we all feel but rarely admit. It comes from forgetting that each of us has our own unique journey. What someone else has doesn't diminish your worth or your path. Krishna taught about finding contentment within. Here's His guidance..."
    ),
    "Guilt": EmotionData(
        id="Guilt",
        display_name="Guilt",
        emoji="😔",
        contextual_phrases=[
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
        ],
        sub_emotions=["shame", "remorse", "regret", "self-blame", "unworthiness", "contrition", "self-punishment"],
        comforting_message="Every moment is a chance for renewal. Forgiveness begins within.",
        icon="🕊️",
        related_emotions=["Grief", "Moral Dilemma", "Hopelessness"],
        sanskrit_name="पश्चाताप (Paścātāpa)",
        healing_mantra="ॐ क्षमा प्रधानं तपः",
        empathetic_response="Carrying guilt is such a heavy burden, and the fact that you feel it shows your conscience is alive. But please hear this - holding onto guilt forever doesn't undo the past, it only darkens your present. Krishna taught that sincere transformation is always possible. Here's what He said about renewal..."
    ),
    "Hopelessness": EmotionData(
        id="Hopelessness",
        display_name="Hopelessness",
        emoji="🌑",
        contextual_phrases=[
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
        ],
        sub_emotions=["despair", "nihilism", "defeat", "futility", "despondency", "bleakness", "desolation"],
        comforting_message="Even in deepest darkness, dawn is already approaching.",
        icon="🌅",
        related_emotions=["Grief", "Burnout", "Loneliness"],
        sanskrit_name="निराशा (Nirāśā)",
        healing_mantra="ॐ आशा जीवनं प्राणः",
        empathetic_response="I'm so sorry you're in this dark place right now. When hope fades, even getting through the day feels impossible. But please know - you reaching out is itself a spark of hope, even if it doesn't feel like it. Arjuna sat down on his chariot in complete despair, just like you feel now. And then Krishna spoke these words..."
    ),
    "Confusion": EmotionData(
        id="Confusion",
        display_name="Confusion",
        emoji="😵‍💫",
        contextual_phrases=[
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
        ],
        sub_emotions=["bewilderment", "disorientation", "perplexity", "mental fog", "uncertainty", "indecision", "overwhelm"],
        comforting_message="Clarity emerges when the mind becomes still like a lake.",
        icon="🔮",
        related_emotions=["Anxiety", "Identity Crisis", "Moral Dilemma"],
        sanskrit_name="मोह (Moha)",
        healing_mantra="ॐ प्रज्ञानं ब्रह्म",
        empathetic_response="When everything feels like a fog and you can't see the path ahead - I understand. That mental overwhelm can be exhausting. The good news is that clarity does come, often when we stop trying so hard to find it. Krishna spoke to Arjuna when his mind was in complete chaos. Here's the clarity He offered..."
    ),
    "Impatience": EmotionData(
        id="Impatience",
        display_name="Impatience",
        emoji="⏰",
        contextual_phrases=[
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
        ],
        sub_emotions=["restlessness", "agitation", "urgency", "frustration", "haste", "irritability", "intolerance"],
        comforting_message="Divine timing unfolds perfectly. Trust the journey.",
        icon="⏳",
        related_emotions=["Anger", "Result-Obsession", "Anxiety"],
        sanskrit_name="अधीरता (Adhīratā)",
        healing_mantra="ॐ धैर्यं सर्वत्र साधनम्",
        empathetic_response="I feel that urgency in your words - the need for things to move faster, for results to come now. In our instant-everything world, waiting feels unbearable. But some of life's greatest gifts need time to unfold. Krishna taught about the beauty of patient action. Here's His wisdom on trusting the timing..."
    ),
}


# ============================================================================
# VERSE DATA & MATCHING
# ============================================================================

@dataclass
class VerseData:
    """Represents a Gita verse with its metadata."""
    id: str
    chapter: int
    verse: int
    sanskrit_text: str
    english_translation: str
    emotion_category: str
    wisdom_nugget: str
    modern_problem_match: str
    arjuna_despair_link: str = ""
    hindi_translation: str = ""
    transliteration: str = ""


# Global verse storage
ENRICHED_VERSES: List[VerseData] = []
VERSE_EMBEDDINGS: Dict[str, np.ndarray] = {}


def load_enriched_verses() -> bool:
    """Load enriched verses from JSON file."""
    global ENRICHED_VERSES
    
    if ENRICHED_VERSES:
        return True
    
    # Try multiple paths (Windows and Unix compatible)
    script_dir = Path(__file__).parent.resolve()
    possible_paths = [
        script_dir / "app" / "src" / "main" / "assets" / "KotlinModel" / "enriched_gita_formatted.json",
        script_dir / "app" / "src" / "main" / "java" / "com" / "gita" / "app" / "data" / "enriched_gita_formatted.json",
        Path("E:\\gita ai\\app\\src\\main\\assets\\KotlinModel\\enriched_gita_formatted.json"),
        Path("E:/gita ai/app/src/main/assets/KotlinModel/enriched_gita_formatted.json"),
        Path("app/src/main/assets/KotlinModel/enriched_gita_formatted.json"),
    ]
    
    print(f"Looking for verses in: {script_dir}")
    
    for verse_path in possible_paths:
        print(f"Checking path: {verse_path} - exists: {verse_path.exists()}")
        if verse_path.exists():
            try:
                with open(verse_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                
                for v in data:
                    verse = VerseData(
                        id=v.get('id', ''),
                        chapter=v.get('chapter_number', 0),
                        verse=v.get('verse_number', 0),
                        sanskrit_text=v.get('sanskrit_text', ''),
                        english_translation=v.get('english_translation', ''),
                        emotion_category=v.get('emotion_category', ''),
                        wisdom_nugget=v.get('wisdom_nugget', ''),
                        modern_problem_match=v.get('modern_problem_match', ''),
                        arjuna_despair_link=v.get('arjuna_despair_link', ''),
                        hindi_translation=v.get('hindi_translation', ''),
                        transliteration=v.get('transliteration', ''),
                    )
                    ENRICHED_VERSES.append(verse)
                
                print(f"Loaded {len(ENRICHED_VERSES)} verses from {verse_path}")
                return True
            except Exception as e:
                print(f"Error loading verses from {verse_path}: {e}")
                continue
    
    print("Warning: Could not load enriched verses from any path")
    return False


def get_verses_for_emotion(emotion_id: str) -> List[VerseData]:
    """Get all verses matching a specific emotion category."""
    load_enriched_verses()
    return [v for v in ENRICHED_VERSES if v.emotion_category == emotion_id]


def find_best_healing_verse(
    query: str, 
    emotion_id: str, 
    api_key: str,
    top_k: int = 3
) -> List[Tuple[VerseData, float]]:
    """
    Find the best healing verse for a given query and emotion.
    Uses semantic similarity to match the query with verse wisdom.
    """
    global VERSE_EMBEDDINGS
    
    load_enriched_verses()
    
    # Get verses for this emotion
    emotion_verses = get_verses_for_emotion(emotion_id)
    if not emotion_verses:
        # Fallback: get all verses
        emotion_verses = ENRICHED_VERSES[:50]  # Limit to first 50
    
    # Get query embedding
    query_embedding = get_embedding(query, api_key)
    if query_embedding is None:
        # Return top verses by default
        return [(v, 0.5) for v in emotion_verses[:top_k]]
    
    # Score each verse
    scored_verses = []
    for verse in emotion_verses:
        # Create a combined text for matching
        verse_text = f"{verse.wisdom_nugget} {verse.modern_problem_match}"
        cache_key = f"verse_{verse.id}"
        
        # Check cache
        if cache_key in VERSE_EMBEDDINGS:
            verse_embedding = VERSE_EMBEDDINGS[cache_key]
        else:
            verse_embedding = get_embedding(verse_text, api_key)
            if verse_embedding is not None:
                VERSE_EMBEDDINGS[cache_key] = verse_embedding
        
        if verse_embedding is not None:
            score = cosine_similarity(query_embedding, verse_embedding)
            scored_verses.append((verse, score))
    
    # Sort by score and return top k
    scored_verses.sort(key=lambda x: x[1], reverse=True)
    return scored_verses[:top_k]


def find_best_healing_verse_fast(
    emotion_id: str,
    top_k: int = 3
) -> List[VerseData]:
    """
    Fast version: find healing verses for emotion without API calls.
    Uses pre-categorized emotion_category field.
    """
    load_enriched_verses()
    
    # Map detected emotions to dataset emotion categories
    # Dataset has: Anxiety, Grief, Anger, Attachment, Identity Crisis, 
    #              Burnout, Loneliness, Pride, Result-Obsession, 
    #              Intellectual Doubt, Moral Dilemma
    emotion_mapping = {
        "Anxiety": "Anxiety",
        "Grief": "Grief", 
        "Anger": "Anger",
        "Attachment": "Attachment",
        "Identity Crisis": "Identity Crisis",
        "Burnout": "Burnout",
        "Loneliness": "Loneliness",
        "Pride": "Pride",
        "Result-Obsession": "Result-Obsession",
        # Map new emotions to closest dataset categories
        "Fear": "Anxiety",  # Fear maps to Anxiety
        "Jealousy": "Attachment",  # Jealousy relates to Attachment
        "Guilt": "Moral Dilemma",  # Guilt relates to Moral Dilemma
        "Hopelessness": "Grief",  # Hopelessness maps to Grief
        "Confusion": "Intellectual Doubt",  # Confusion maps to Intellectual Doubt
        "Impatience": "Result-Obsession",  # Impatience maps to Result-Obsession
    }
    
    # Get mapped emotion category
    mapped_emotion = emotion_mapping.get(emotion_id, emotion_id)
    print(f"Looking for verses: detected={emotion_id}, mapped={mapped_emotion}")
    
    emotion_verses = get_verses_for_emotion(mapped_emotion)
    
    # If no verses found, try original emotion or fallback to Anxiety
    if not emotion_verses:
        emotion_verses = get_verses_for_emotion(emotion_id)
    if not emotion_verses:
        print(f"No verses for {emotion_id}, falling back to Anxiety")
        emotion_verses = get_verses_for_emotion("Anxiety")
    if not emotion_verses:
        # Last resort: return first 3 verses
        print("Fallback: returning first 3 verses")
        return ENRICHED_VERSES[:top_k]
    
    print(f"Found {len(emotion_verses)} verses for {mapped_emotion}")
    
    # Return a diverse sample - pick from different chapters
    seen_chapters = set()
    diverse_verses = []
    for verse in emotion_verses:
        if verse.chapter not in seen_chapters or len(diverse_verses) < top_k:
            diverse_verses.append(verse)
            seen_chapters.add(verse.chapter)
            if len(diverse_verses) >= top_k:
                break
    
    return diverse_verses


# ============================================================================
# EMBEDDING & MATCHING LOGIC
# ============================================================================

def get_embedding(text: str, api_key: str) -> Optional[np.ndarray]:
    """Get OpenAI embedding for text."""
    try:
        from openai import OpenAI
        client = OpenAI(api_key=api_key)
        
        response = client.embeddings.create(
            model="text-embedding-3-small",
            input=text
        )
        return np.array(response.data[0].embedding, dtype=np.float32)
    except Exception as e:
        print(f"Error getting embedding: {e}")
        return None


def get_embeddings_batch(texts: List[str], api_key: str) -> Optional[List[np.ndarray]]:
    """Get OpenAI embeddings for multiple texts in one API call."""
    try:
        from openai import OpenAI
        client = OpenAI(api_key=api_key)
        
        response = client.embeddings.create(
            model="text-embedding-3-small",
            input=texts
        )
        return [np.array(e.embedding, dtype=np.float32) for e in response.data]
    except Exception as e:
        print(f"Error getting batch embeddings: {e}")
        return None


def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    """Calculate cosine similarity between two vectors."""
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))


# Global cache for phrase embeddings
PHRASE_EMBEDDINGS_CACHE: Dict[str, np.ndarray] = {}


def ensure_phrase_embeddings_cached(api_key: str, num_phrases: int = 4, verbose: bool = True):
    """Pre-compute and cache all phrase embeddings in batch calls."""
    global PHRASE_EMBEDDINGS_CACHE
    
    # Check if already cached
    cache_key = f"_cached_{num_phrases}"
    if cache_key in PHRASE_EMBEDDINGS_CACHE:
        return True
    
    if verbose:
        print("Pre-computing phrase embeddings (first time only)...")
    
    # Collect all phrases that need embeddings
    all_phrases = []
    phrase_keys = []
    
    for emotion_id, emotion_data in EMOTIONS.items():
        phrases = emotion_data.contextual_phrases[:num_phrases]
        for i, phrase in enumerate(phrases):
            embedding_text = f"Feeling {emotion_data.display_name}: {phrase}"
            key = f"{emotion_id}_{i}"
            if key not in PHRASE_EMBEDDINGS_CACHE:
                all_phrases.append(embedding_text)
                phrase_keys.append(key)
    
    if not all_phrases:
        PHRASE_EMBEDDINGS_CACHE[cache_key] = True
        return True
    
    if verbose:
        print(f"Fetching {len(all_phrases)} phrase embeddings in batch...")
    
    # Batch in groups of 100 (OpenAI limit is 2048)
    batch_size = 100
    for i in range(0, len(all_phrases), batch_size):
        batch_phrases = all_phrases[i:i+batch_size]
        batch_keys = phrase_keys[i:i+batch_size]
        
        embeddings = get_embeddings_batch(batch_phrases, api_key)
        if embeddings:
            for key, emb in zip(batch_keys, embeddings):
                PHRASE_EMBEDDINGS_CACHE[key] = emb
    
    PHRASE_EMBEDDINGS_CACHE[cache_key] = True
    if verbose:
        print("Phrase embeddings cached successfully!")
    return True


def detect_emotion_multi_phrase(
    query: str, 
    api_key: str, 
    num_phrases: int = 4,
    verbose: bool = True
) -> Tuple[Optional[str], float, Dict[str, float]]:
    """
    Detect emotion using multi-phrase weighted averaging.
    Returns (best_emotion, best_score, all_scores)
    """
    # Ensure phrase embeddings are cached
    ensure_phrase_embeddings_cached(api_key, num_phrases, verbose)
    
    if verbose:
        print("\nGetting embedding for query...")
    
    query_embedding = get_embedding(query, api_key)
    if query_embedding is None:
        return None, 0.0, {}
    
    if verbose:
        print("Computing emotion scores...")
    
    emotion_scores: Dict[str, List[float]] = {eid: [] for eid in EMOTIONS}
    
    for emotion_id, emotion_data in EMOTIONS.items():
        phrases = emotion_data.contextual_phrases[:num_phrases]
        
        for i, phrase in enumerate(phrases):
            key = f"{emotion_id}_{i}"
            phrase_embedding = PHRASE_EMBEDDINGS_CACHE.get(key)
            
            if phrase_embedding is not None:
                score = cosine_similarity(query_embedding, phrase_embedding)
                # Weight earlier phrases higher
                weight = 1.0 - (i * 0.15)
                emotion_scores[emotion_id].append(score * weight)
    
    # Calculate blended scores (60% max + 40% avg)
    all_scores: Dict[str, float] = {}
    best_emotion = None
    best_score = float('-inf')
    
    for emotion_id, scores in emotion_scores.items():
        if not scores:
            continue
        
        avg_score = np.mean(scores)
        max_score = np.max(scores)
        blended_score = (max_score * 0.6) + (avg_score * 0.4)
        
        all_scores[emotion_id] = blended_score
        
        if blended_score > best_score:
            best_score = blended_score
            best_emotion = emotion_id
    
    # Apply related emotion boosting
    if best_emotion:
        related = EMOTIONS[best_emotion].related_emotions
        for rel_emotion in related:
            if rel_emotion in all_scores:
                all_scores[rel_emotion] *= 1.05
    
    return best_emotion, best_score, all_scores


def normalize_score(score: float) -> float:
    """Normalize score to 0-1 range for display."""
    # Cosine similarity is already in [-1, 1], shift to [0, 1]
    return (score + 1) / 2


# ============================================================================
# CLI INTERFACE
# ============================================================================

def print_emotion_result(
    query: str,
    emotion_id: Optional[str],
    score: float,
    all_scores: Dict[str, float]
):
    """Print emotion detection results in a beautiful format."""
    print("\n" + "=" * 70)
    print("                      EMOTION DETECTION RESULTS")
    print("=" * 70)
    
    print(f"\nQuery: \"{query}\"")
    
    if emotion_id and emotion_id in EMOTIONS:
        emotion = EMOTIONS[emotion_id]
        confidence = normalize_score(score) * 100
        
        print(f"\n+{'-' * 68}+")
        print(f"| {emotion.emoji} DETECTED EMOTION: {emotion.display_name:<40}   |")
        print(f"|    Sanskrit: {emotion.sanskrit_name:<46}   |")
        print(f"|    Confidence: {confidence:.1f}%{' ' * 44}|")
        print(f"+{'-' * 68}+")
        
        print(f"\nMessage: \"{emotion.comforting_message}\"")
        print(f"\nMantra: {emotion.healing_mantra}")
        
        # Top 5 scores
        print(f"\nTop 5 Emotion Scores:")
        sorted_scores = sorted(all_scores.items(), key=lambda x: x[1], reverse=True)[:5]
        for eid, sc in sorted_scores:
            e = EMOTIONS[eid]
            conf = normalize_score(sc) * 100
            bar_len = int(conf / 5)
            bar = "#" * bar_len + "-" * (20 - bar_len)
            marker = " <- Primary" if eid == emotion_id else ""
            print(f"   {e.emoji} {e.display_name:<20} [{bar}] {conf:5.1f}%{marker}")
        
        # Sub-emotions
        print(f"\nRelated feelings: {', '.join(emotion.sub_emotions[:5])}")
        print(f"Related emotions: {', '.join(emotion.related_emotions)}")
    else:
        print("\nNo emotion detected")
    
    print("\n" + "=" * 70)


def run_cli():
    """Run interactive CLI mode."""
    api_key = os.getenv('OPENAI_API_KEY')
    if not api_key:
        print("Error: OPENAI_API_KEY not found in environment or .env file")
        print("Please set your OpenAI API key:")
        print("  export OPENAI_API_KEY='your-key-here'")
        sys.exit(1)
    
    print("\n" + "=" * 70)
    print("   GITA AI - EMOTION MATCHING TESTER")
    print("   Enhanced Multi-Phrase Weighted Averaging Algorithm")
    print("=" * 70)
    print("\nType your feelings or situation to detect the matching emotion.")
    print("Commands: 'quit' to exit, 'list' to see all emotions, 'test' for examples")
    print("=" * 70)
    
    while True:
        try:
            query = input("\nYour feelings: ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\n\nNamaste! May you find peace.")
            break
        
        if not query:
            continue
        
        if query.lower() in ['quit', 'exit', 'q']:
            print("\nNamaste! May you find peace.")
            break
        
        if query.lower() == 'list':
            print("\nAvailable Emotions:")
            for eid, e in EMOTIONS.items():
                print(f"   {e.emoji} {e.display_name:<20} - {e.sanskrit_name}")
            continue
        
        if query.lower() == 'test':
            run_test_examples(api_key)
            continue
        
        emotion_id, score, all_scores = detect_emotion_multi_phrase(query, api_key)
        print_emotion_result(query, emotion_id, score, all_scores)


def run_test_examples(api_key: str):
    """Run a set of test examples."""
    test_queries = [
        "I can't stop worrying about my exam tomorrow",
        "I lost my grandmother last week and feel empty",
        "My coworker got promoted and I'm still stuck here",
        "I don't know who I am or what I want anymore",
        "I'm so angry at my friend for betraying me",
        "I've been working 70 hours a week and I'm exhausted",
        "I fear death and the unknown",
        "I feel so guilty for hurting my parents",
        "Nothing makes sense anymore, why even try",
        "I need to get this done NOW but everything is slow"
    ]
    
    print("\n" + "=" * 70)
    print("   RUNNING TEST EXAMPLES")
    print("=" * 70)
    
    for i, query in enumerate(test_queries, 1):
        print(f"\n--- Test {i}/{len(test_queries)} ---")
        emotion_id, score, all_scores = detect_emotion_multi_phrase(query, api_key, verbose=False)
        
        if emotion_id and emotion_id in EMOTIONS:
            emotion = EMOTIONS[emotion_id]
            conf = normalize_score(score) * 100
            print(f"Query: \"{query[:50]}...\"" if len(query) > 50 else f"Query: \"{query}\"")
            print(f"   -> {emotion.emoji} {emotion.display_name} ({conf:.1f}%)")
        else:
            print(f"Query: \"{query[:50]}...\"" if len(query) > 50 else f"Query: \"{query}\"")
            print(f"   -> No emotion detected")


# ============================================================================
# SIMPLE WEB UI (using Flask)
# ============================================================================

def run_web_ui():
    """Run a simple web UI for testing."""
    try:
        from flask import Flask, render_template_string, request, jsonify
    except ImportError:
        print("❌ Flask not installed. Installing...")
        os.system(f"{sys.executable} -m pip install flask")
        from flask import Flask, render_template_string, request, jsonify
    
    api_key = os.getenv('OPENAI_API_KEY')
    if not api_key:
        print("❌ Error: OPENAI_API_KEY not found")
        sys.exit(1)
    
    app = Flask(__name__)
    
    HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gita AI - Spiritual Guidance</title>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;500;600&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #FAFAF8;
            --bg-secondary: #FFFFFF;
            --bg-tertiary: #F5F5F0;
            --text-primary: #1A1A1A;
            --text-secondary: #4A4A4A;
            --text-muted: #8A8A8A;
            --accent: #8B7355;
            --accent-light: #C4A77D;
            --border: #E8E8E0;
            --card-shadow: 0 2px 20px rgba(0,0,0,0.06);
            --verse-bg: #FAF8F5;
            --wisdom-bg: #F0EDE8;
        }
        
        [data-theme="dark"] {
            --bg-primary: #0D0D0D;
            --bg-secondary: #1A1A1A;
            --bg-tertiary: #252525;
            --text-primary: #F5F5F5;
            --text-secondary: #B0B0B0;
            --text-muted: #707070;
            --accent: #C4A77D;
            --accent-light: #8B7355;
            --border: #333333;
            --card-shadow: 0 2px 20px rgba(0,0,0,0.3);
            --verse-bg: #1F1F1F;
            --wisdom-bg: #2A2A2A;
        }
        
        * { box-sizing: border-box; margin: 0; padding: 0; }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            min-height: 100vh;
            transition: background 0.3s, color 0.3s;
        }
        
        /* Login Screen */
        .login-screen {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }
        
        .login-logo {
            font-family: 'Cormorant Garamond', serif;
            font-size: 3rem;
            font-weight: 500;
            color: var(--accent);
            margin-bottom: 8px;
            letter-spacing: 0.05em;
        }
        
        .login-tagline {
            color: var(--text-muted);
            font-size: 0.95rem;
            margin-bottom: 60px;
            letter-spacing: 0.1em;
            text-transform: uppercase;
        }
        
        .login-buttons {
            display: flex;
            flex-direction: column;
            gap: 16px;
            width: 100%;
            max-width: 320px;
        }
        
        .btn-google {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            padding: 14px 24px;
            background: var(--bg-secondary);
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 15px;
            font-weight: 500;
            color: var(--text-primary);
            cursor: pointer;
            transition: all 0.2s;
        }
        
        .btn-google:hover {
            border-color: var(--accent);
            box-shadow: var(--card-shadow);
        }
        
        .btn-guest {
            padding: 14px 24px;
            background: var(--accent);
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 500;
            color: white;
            cursor: pointer;
            transition: all 0.2s;
        }
        
        .btn-guest:hover {
            background: var(--accent-light);
        }
        
        .login-divider {
            display: flex;
            align-items: center;
            gap: 16px;
            color: var(--text-muted);
            font-size: 13px;
            margin: 8px 0;
        }
        
        .login-divider::before,
        .login-divider::after {
            content: '';
            flex: 1;
            height: 1px;
            background: var(--border);
        }
        
        /* Main App */
        .app-screen {
            display: none;
            min-height: 100vh;
        }
        
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 24px;
            border-bottom: 1px solid var(--border);
            background: var(--bg-secondary);
        }
        
        .header-logo {
            font-family: 'Cormorant Garamond', serif;
            font-size: 1.5rem;
            font-weight: 500;
            color: var(--accent);
            letter-spacing: 0.05em;
        }
        
        .header-actions {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        
        .theme-toggle {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            border: 1px solid var(--border);
            background: var(--bg-tertiary);
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            transition: all 0.2s;
        }
        
        .theme-toggle:hover {
            border-color: var(--accent);
        }
        
        .container {
            max-width: 680px;
            margin: 0 auto;
            padding: 40px 24px;
        }
        
        .input-section {
            background: var(--bg-secondary);
            border-radius: 16px;
            padding: 24px;
            border: 1px solid var(--border);
            margin-bottom: 32px;
        }
        
        textarea {
            width: 100%;
            padding: 16px;
            border: 1px solid var(--border);
            border-radius: 12px;
            font-size: 16px;
            font-family: inherit;
            resize: vertical;
            min-height: 120px;
            background: var(--bg-tertiary);
            color: var(--text-primary);
            transition: border-color 0.2s;
        }
        
        textarea::placeholder {
            color: var(--text-muted);
        }
        
        textarea:focus {
            outline: none;
            border-color: var(--accent);
        }
        
        .btn-primary {
            width: 100%;
            padding: 16px;
            background: var(--accent);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 500;
            cursor: pointer;
            margin-top: 16px;
            transition: background 0.2s;
        }
        
        .btn-primary:hover {
            background: var(--accent-light);
        }
        
        .btn-primary:disabled {
            background: var(--text-muted);
            cursor: not-allowed;
        }
        
        .loading {
            text-align: center;
            padding: 60px 20px;
            display: none;
        }
        
        .loading-spinner {
            width: 40px;
            height: 40px;
            border: 3px solid var(--border);
            border-top-color: var(--accent);
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 16px;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        
        .loading-text {
            color: var(--text-muted);
            font-size: 14px;
        }
        
        .result {
            display: none;
        }
        
        /* Response Card */
        .response-card {
            background: var(--bg-secondary);
            border-radius: 16px;
            padding: 28px;
            border: 1px solid var(--border);
            margin-bottom: 24px;
        }
        
        .response-text {
            font-size: 17px;
            line-height: 1.8;
            color: var(--text-secondary);
            font-style: italic;
        }
        
        /* Verse Cards */
        .verses-header {
            font-family: 'Cormorant Garamond', serif;
            font-size: 1.3rem;
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 20px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--border);
        }
        
        .verse-card {
            background: var(--verse-bg);
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 16px;
            border-left: 3px solid var(--accent);
        }
        
        .verse-ref {
            font-size: 13px;
            font-weight: 500;
            color: var(--accent);
            margin-bottom: 16px;
            letter-spacing: 0.05em;
        }
        
        .verse-sanskrit {
            font-size: 15px;
            line-height: 1.9;
            color: var(--text-muted);
            margin-bottom: 16px;
            padding: 16px;
            background: var(--bg-tertiary);
            border-radius: 8px;
        }
        
        .verse-translation {
            font-size: 15px;
            line-height: 1.7;
            color: var(--text-secondary);
            margin-bottom: 16px;
            padding-left: 16px;
            border-left: 2px solid var(--accent-light);
        }
        
        .verse-wisdom {
            background: var(--wisdom-bg);
            padding: 16px;
            border-radius: 8px;
        }
        
        .wisdom-label {
            font-size: 12px;
            font-weight: 600;
            color: var(--accent);
            text-transform: uppercase;
            letter-spacing: 0.1em;
            margin-bottom: 8px;
        }
        
        .wisdom-text {
            font-size: 14px;
            line-height: 1.6;
            color: var(--text-secondary);
        }
        
        /* New conversation button */
        .new-conversation {
            display: block;
            width: 100%;
            padding: 14px;
            background: transparent;
            border: 1px solid var(--border);
            border-radius: 10px;
            font-size: 14px;
            font-weight: 500;
            color: var(--text-secondary);
            cursor: pointer;
            margin-top: 24px;
            transition: all 0.2s;
        }
        
        .new-conversation:hover {
            border-color: var(--accent);
            color: var(--accent);
        }
    </style>
</head>
<body>
    <!-- Login Screen -->
    <div class="login-screen" id="login-screen">
        <div class="login-logo">Gita AI</div>
        <div class="login-tagline">Timeless Wisdom for Modern Life</div>
        
        <div class="login-buttons">
            <button class="btn-google" onclick="loginWithGoogle()">
                <svg width="18" height="18" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Continue with Google
            </button>
            <div class="login-divider">or</div>
            <button class="btn-guest" onclick="continueAsGuest()">Continue as Guest</button>
        </div>
    </div>
    
    <!-- Main App Screen -->
    <div class="app-screen" id="app-screen">
        <header class="header">
            <div class="header-logo">Gita AI</div>
            <div class="header-actions">
                <button class="theme-toggle" onclick="toggleTheme()" id="theme-btn" title="Toggle dark mode">
                    <span id="theme-icon">☀️</span>
                </button>
            </div>
        </header>
        
        <div class="container">
            <div class="input-section">
                <textarea id="query" placeholder="What's on your mind? Share your thoughts, feelings, or challenges..."></textarea>
                <button class="btn-primary" onclick="detectEmotion()" id="btn">Seek Guidance</button>
            </div>
            
            <div class="loading" id="loading">
                <div class="loading-spinner"></div>
                <div class="loading-text">Reflecting on your words...</div>
            </div>
            
            <div class="result" id="result">
                <div class="response-card">
                    <div class="response-text" id="response-text"></div>
                </div>
                
                <div class="verses-header">From the Bhagavad Gita</div>
                <div id="verses-list"></div>
                
                <button class="new-conversation" onclick="newConversation()">Start New Conversation</button>
            </div>
        </div>
    </div>
    
    <script>
        // Theme management
        function toggleTheme() {
            const body = document.body;
            const icon = document.getElementById('theme-icon');
            if (body.getAttribute('data-theme') === 'dark') {
                body.removeAttribute('data-theme');
                icon.textContent = '☀️';
                localStorage.setItem('theme', 'light');
            } else {
                body.setAttribute('data-theme', 'dark');
                icon.textContent = '🌙';
                localStorage.setItem('theme', 'dark');
            }
        }
        
        // Load saved theme
        if (localStorage.getItem('theme') === 'dark') {
            document.body.setAttribute('data-theme', 'dark');
            document.getElementById('theme-icon').textContent = '🌙';
        }
        
        // Login functions
        function loginWithGoogle() {
            // For now, just continue to app (Google OAuth would be implemented here)
            alert('Google login would be implemented with Firebase Auth. Continuing as guest for now.');
            continueAsGuest();
        }
        
        function continueAsGuest() {
            document.getElementById('login-screen').style.display = 'none';
            document.getElementById('app-screen').style.display = 'block';
            localStorage.setItem('loggedIn', 'true');
        }
        
        // Check if already logged in
        if (localStorage.getItem('loggedIn') === 'true') {
            document.getElementById('login-screen').style.display = 'none';
            document.getElementById('app-screen').style.display = 'block';
        }
        
        function newConversation() {
            document.getElementById('query').value = '';
            document.getElementById('result').style.display = 'none';
            document.getElementById('query').focus();
        }
        
        async function detectEmotion() {
            const query = document.getElementById('query').value.trim();
            if (!query) return;
            
            document.getElementById('btn').disabled = true;
            document.getElementById('loading').style.display = 'block';
            document.getElementById('result').style.display = 'none';
            
            try {
                const response = await fetch('/detect', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ query })
                });
                
                const data = await response.json();
                
                if (data.error) {
                    alert('Error: ' + data.error);
                    return;
                }
                
                // Update response text (empathetic response)
                document.getElementById('response-text').textContent = data.empathetic_response;
                
                // Update healing verses - clean design without emojis
                let versesHtml = '';
                if (data.healing_verses && data.healing_verses.length > 0) {
                    for (const verse of data.healing_verses) {
                        versesHtml += `
                            <div class="verse-card">
                                <div class="verse-ref">Chapter ${verse.chapter}, Verse ${verse.verse}</div>
                                <div class="verse-sanskrit">${verse.sanskrit}</div>
                                <div class="verse-translation">${verse.translation}</div>
                                <div class="verse-wisdom">
                                    <div class="wisdom-label">Insight</div>
                                    <div class="wisdom-text">${verse.wisdom}</div>
                                </div>
                            </div>
                        `;
                    }
                    document.getElementById('verses-list').innerHTML = versesHtml;
                }
                
                document.getElementById('result').style.display = 'block';
            } catch (e) {
                alert('Error: ' + e.message);
            } finally {
                document.getElementById('btn').disabled = false;
                document.getElementById('loading').style.display = 'none';
            }
        }
        
        // Enter key to submit
        document.getElementById('query').addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && e.ctrlKey) {
                detectEmotion();
            }
        });
    </script>
</body>
</html>
    '''
    
    @app.route('/')
    def index():
        return render_template_string(HTML_TEMPLATE)
    
    @app.route('/detect', methods=['POST'])
    def detect():
        data = request.json
        query = data.get('query', '')
        
        if not query:
            return jsonify({'error': 'No query provided'})
        
        emotion_id, score, all_scores = detect_emotion_multi_phrase(query, api_key, verbose=False)
        
        if not emotion_id or emotion_id not in EMOTIONS:
            return jsonify({'error': 'Could not detect emotion'})
        
        emotion = EMOTIONS[emotion_id]
        confidence = normalize_score(score) * 100
        
        # Get top 5 scores normalized
        sorted_scores = sorted(all_scores.items(), key=lambda x: x[1], reverse=True)[:5]
        top_scores = {eid: normalize_score(sc) * 100 for eid, sc in sorted_scores}
        all_emojis = {eid: EMOTIONS[eid].emoji for eid in EMOTIONS}
        
        # Find healing verses for this emotion
        healing_verses = find_best_healing_verse_fast(emotion_id, top_k=3)
        verses_data = []
        for verse in healing_verses:
            verses_data.append({
                'id': verse.id,
                'chapter': verse.chapter,
                'verse': verse.verse,
                'sanskrit': verse.sanskrit_text[:200] + '...' if len(verse.sanskrit_text) > 200 else verse.sanskrit_text,
                'translation': verse.english_translation,
                'wisdom': verse.wisdom_nugget,
                'modern_match': verse.modern_problem_match,
            })
        
        return jsonify({
            'emotion_id': emotion_id,
            'display_name': emotion.display_name,
            'emoji': emotion.emoji,
            'sanskrit_name': emotion.sanskrit_name,
            'confidence': confidence,
            'comforting_message': emotion.comforting_message,
            'healing_mantra': emotion.healing_mantra,
            'empathetic_response': emotion.empathetic_response or f"I understand you're feeling {emotion.display_name.lower()}. Krishna spoke about this in the Gita...",
            'top_scores': top_scores,
            'all_emojis': all_emojis,
            'gradient': 'linear-gradient(135deg, #EDE7F6, #D1C4E9)',
            'healing_verses': verses_data
        })
    
    print("\n" + "=" * 70)
    print("   GITA AI - WEB UI")
    print("=" * 70)
    print("\nStarting web server...")
    print("Open your browser to: http://localhost:5000")
    print("\nPress Ctrl+C to stop the server")
    print("=" * 70)
    
    app.run(host='0.0.0.0', port=5000, debug=False)


# ============================================================================
# MAIN
# ============================================================================

if __name__ == '__main__':
    if '--web' in sys.argv:
        run_web_ui()
    elif '--batch' in sys.argv:
        api_key = os.getenv('OPENAI_API_KEY')
        if api_key:
            run_test_examples(api_key)
        else:
            print("❌ OPENAI_API_KEY not found")
    else:
        run_cli()


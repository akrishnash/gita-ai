package com.gita.app.data

// Curated, real Bhagavad Gita verses for deterministic offline use
// Translation style: modern, neutral, non-preachy

object GitaMap {
    val themes: List<Theme> = listOf(
        // THEME 1: FEAR
        Theme(
            id = "fear",
            label = "Fear",
            subthemes = listOf(
                SubTheme(
                    id = "fear_of_failure",
                    label = "Fear of Failure",
                    keywords = listOf("fail", "failure", "career", "job", "result", "success"),
                    verses = listOf(
                        VerseEntry(
                            id = "2.47",
                            chapter = 2,
                            verse = 47,
                            sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन ।",
                            transliteration = "Karmanye vadhikaraste mā phaleṣu kadācana",
                            translation = "You have a right to action alone, never to its results.",
                            context = "Arjuna fears the consequences of action. Krishna reframes action as responsibility without attachment to outcomes.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Fear grows when we imagine outcomes instead of attending to the task in front of us.",
                                ReflectionAngle.ACTION to "The Gita does not remove uncertainty; it removes our obligation to control results.",
                                ReflectionAngle.DETACHMENT to "Releasing outcomes frees effort rather than weakening it.",
                                ReflectionAngle.COMPASSION to "Fear of failure is human. The response offered is steadiness, not denial.",
                                ReflectionAngle.SELFTRUST to "Focus on the work you can do now. Let clarity follow action."
                            ),
                            anchorLines = listOf(
                                "Do the work. Release the outcome.",
                                "Action is yours. Results are not."
                            )
                        )
                    )
                ),
                SubTheme(
                    id = "fear_of_loss",
                    label = "Fear of Loss",
                    keywords = listOf("lose", "loss", "afraid", "ending", "change"),
                    verses = listOf(
                        VerseEntry(
                            id = "2.14",
                            chapter = 2,
                            verse = 14,
                            sanskrit = "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः ।",
                            transliteration = "Mātrā-sparśās tu kaunteya śītoṣṇa-sukha-duḥkha-dāḥ",
                            translation = "Pleasure and pain come and go; endure them patiently.",
                            context = "Krishna explains that experiences are temporary and should be met with steadiness.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Fear intensifies when we treat passing experiences as permanent.",
                                ReflectionAngle.ACTION to "Endurance is not passivity; it is emotional strength.",
                                ReflectionAngle.DETACHMENT to "Seeing experiences as temporary loosens fear's grip.",
                                ReflectionAngle.COMPASSION to "Pain is acknowledged here, not dismissed.",
                                ReflectionAngle.SELFTRUST to "You have survived change before. This is not the first time."
                            ),
                            anchorLines = listOf(
                                "This too will pass.",
                                "Endurance is strength."
                            )
                        )
                    )
                )
            )
        ),
        
        // THEME 2: CONFUSION
        Theme(
            id = "confusion",
            label = "Confusion",
            subthemes = listOf(
                SubTheme(
                    id = "decision_paralysis",
                    label = "Decision Paralysis",
                    keywords = listOf("confused", "decision", "choice", "stuck", "unsure"),
                    verses = listOf(
                        VerseEntry(
                            id = "2.7",
                            chapter = 2,
                            verse = 7,
                            sanskrit = "कार्पण्यदोषोपहतस्वभावः",
                            transliteration = "Kārpaṇya-doṣopahata-svabhāvaḥ",
                            translation = "My understanding is clouded; I ask what is right.",
                            context = "Arjuna openly admits confusion and asks for guidance.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Confusion often appears when values collide, not when they are absent.",
                                ReflectionAngle.ACTION to "Admitting uncertainty is a form of clarity.",
                                ReflectionAngle.DETACHMENT to "Clarity comes when we stop demanding certainty.",
                                ReflectionAngle.COMPASSION to "The Gita treats confusion with respect, not judgment.",
                                ReflectionAngle.SELFTRUST to "Honesty about doubt is the first step forward."
                            ),
                            anchorLines = listOf(
                                "Clarity begins with honesty.",
                                "Doubt is not failure."
                            )
                        )
                    )
                )
            )
        ),
        
        // THEME 3: ATTACHMENT
        Theme(
            id = "attachment",
            label = "Attachment",
            subthemes = listOf(
                SubTheme(
                    id = "emotional_attachment",
                    label = "Emotional Attachment",
                    keywords = listOf("attached", "cling", "obsessed", "relationship", "need"),
                    verses = listOf(
                        VerseEntry(
                            id = "2.62",
                            chapter = 2,
                            verse = 62,
                            sanskrit = "ध्यायतो विषयान्पुंसः सङ्गस्तेषूपजायते ।",
                            transliteration = "Dhyāyato viṣayān puṁsaḥ saṅgas teṣūpajāyate",
                            translation = "Attachment arises from dwelling on objects of desire.",
                            context = "Krishna traces suffering back to uncontrolled attachment.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "What we repeatedly dwell on quietly shapes our emotions.",
                                ReflectionAngle.ACTION to "Attention is the first place where freedom begins.",
                                ReflectionAngle.DETACHMENT to "Letting go starts with where the mind rests.",
                                ReflectionAngle.COMPASSION to "Attachment grows subtly, not through weakness.",
                                ReflectionAngle.SELFTRUST to "You can redirect attention gently, not forcefully."
                            ),
                            anchorLines = listOf(
                                "Notice where the mind lingers.",
                                "Attention shapes attachment."
                            )
                        )
                    )
                )
            )
        ),
        
        // THEME 4: GRIEF
        Theme(
            id = "grief",
            label = "Grief",
            subthemes = listOf(
                SubTheme(
                    id = "loss",
                    label = "Loss and Grief",
                    keywords = listOf("grief", "loss", "death", "gone", "sad"),
                    verses = listOf(
                        VerseEntry(
                            id = "2.13",
                            chapter = 2,
                            verse = 13,
                            sanskrit = "देहिनोऽस्मिन्यथा देहे",
                            transliteration = "Dehino 'smin yathā dehe",
                            translation = "Just as the body passes through stages, so does life move onward.",
                            context = "Krishna reframes death as transition rather than annihilation.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Grief resists impermanence, even when we understand it intellectually.",
                                ReflectionAngle.ACTION to "Understanding loss does not erase pain, but it softens it.",
                                ReflectionAngle.DETACHMENT to "Impermanence gives grief a wider horizon.",
                                ReflectionAngle.COMPASSION to "Sorrow is honored here, not denied.",
                                ReflectionAngle.SELFTRUST to "Healing unfolds in its own time."
                            ),
                            anchorLines = listOf(
                                "Loss is change, not erasure.",
                                "Grief moves at its own pace."
                            )
                        )
                    )
                )
            )
        ),
        
        // THEME 5: DUTY vs DESIRE
        Theme(
            id = "duty_vs_desire",
            label = "Duty vs Desire",
            subthemes = listOf(
                SubTheme(
                    id = "inner_conflict",
                    label = "Inner Conflict",
                    keywords = listOf("duty", "want", "should", "responsibility", "desire"),
                    verses = listOf(
                        VerseEntry(
                            id = "3.35",
                            chapter = 3,
                            verse = 35,
                            sanskrit = "श्रेयान्स्वधर्मो विगुणः",
                            transliteration = "Śreyān sva-dharmo viguṇaḥ",
                            translation = "It is better to follow one's own path imperfectly than another's perfectly.",
                            context = "Krishna emphasizes authenticity over imitation.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Conflict arises when we live borrowed values.",
                                ReflectionAngle.ACTION to "Imperfect authenticity is more sustainable than perfect imitation.",
                                ReflectionAngle.DETACHMENT to "Let go of comparison to find steadiness.",
                                ReflectionAngle.COMPASSION to "Struggle is part of finding one's path.",
                                ReflectionAngle.SELFTRUST to "Your path need not look like anyone else's."
                            ),
                            anchorLines = listOf(
                                "Your path is enough.",
                                "Authenticity over perfection."
                            )
                        )
                    )
                )
            )
        ),
        
        // THEME 6: EXHAUSTION
        Theme(
            id = "exhaustion",
            label = "Exhaustion",
            subthemes = listOf(
                SubTheme(
                    id = "burnout",
                    label = "Burnout",
                    keywords = listOf("tired", "exhausted", "burnout", "empty", "overwhelmed"),
                    verses = listOf(
                        VerseEntry(
                            id = "6.5",
                            chapter = 6,
                            verse = 5,
                            sanskrit = "उद्धरेदात्मनाऽत्मानं",
                            transliteration = "Uddhared ātmanā 'tmānaṁ",
                            translation = "One must uplift oneself by oneself.",
                            context = "Krishna speaks about self-regulation and inner balance.",
                            reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to "Burnout often comes from neglecting inner limits.",
                                ReflectionAngle.ACTION to "Rest is not escape; it is restoration.",
                                ReflectionAngle.DETACHMENT to "You are allowed to pause without quitting.",
                                ReflectionAngle.COMPASSION to "Exhaustion is a signal, not a failure.",
                                ReflectionAngle.SELFTRUST to "Small care restores strength over time."
                            ),
                            anchorLines = listOf(
                                "Rest is part of the path.",
                                "Pause without guilt."
                            )
                        )
                    )
                )
            )
        )
    )
}



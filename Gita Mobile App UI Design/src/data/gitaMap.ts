// src/data/gitaMap.ts
// Curated, real Bhagavad Gita verses for deterministic offline use
// Translation style: modern, neutral, non-preachy

export type ReflectionAngle =
  | "psychological"
  | "action"
  | "detachment"
  | "compassion"
  | "selfTrust";

export interface VerseEntry {
  id: string;
  chapter: number;
  verse: number;
  sanskrit: string;
  transliteration: string;
  translation: string;
  context: string;
  reflections: Record<ReflectionAngle, string>;
  anchorLines: string[];
}

export interface SubTheme {
  id: string;
  label: string;
  keywords: string[];
  verses: VerseEntry[];
}

export interface Theme {
  id: string;
  label: string;
  subthemes: SubTheme[];
}

export const gitaMap: Theme[] = [
  /* =========================================================
     THEME 1: FEAR
     ========================================================= */
  {
    id: "fear",
    label: "Fear",
    subthemes: [
      {
        id: "fear_of_failure",
        label: "Fear of Failure",
        keywords: ["fail", "failure", "career", "job", "result", "success"],
        verses: [
          {
            id: "2.47",
            chapter: 2,
            verse: 47,
            sanskrit:
              "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन ।",
            transliteration:
              "Karmanye vadhikaraste mā phaleṣu kadācana",
            translation:
              "You have a right to action alone, never to its results.",
            context:
              "Arjuna fears the consequences of action. Krishna reframes action as responsibility without attachment to outcomes.",
            reflections: {
              psychological:
                "Fear grows when we imagine outcomes instead of attending to the task in front of us.",
              action:
                "The Gita does not remove uncertainty; it removes our obligation to control results.",
              detachment:
                "Releasing outcomes frees effort rather than weakening it.",
              compassion:
                "Fear of failure is human. The response offered is steadiness, not denial.",
              selfTrust:
                "Focus on the work you can do now. Let clarity follow action."
            },
            anchorLines: [
              "Do the work. Release the outcome.",
              "Action is yours. Results are not."
            ]
          }
        ]
      },
      {
        id: "fear_of_loss",
        label: "Fear of Loss",
        keywords: ["lose", "loss", "afraid", "ending", "change"],
        verses: [
          {
            id: "2.14",
            chapter: 2,
            verse: 14,
            sanskrit:
              "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः ।",
            transliteration:
              "Mātrā-sparśās tu kaunteya śītoṣṇa-sukha-duḥkha-dāḥ",
            translation:
              "Pleasure and pain come and go; endure them patiently.",
            context:
              "Krishna explains that experiences are temporary and should be met with steadiness.",
            reflections: {
              psychological:
                "Fear intensifies when we treat passing experiences as permanent.",
              action:
                "Endurance is not passivity; it is emotional strength.",
              detachment:
                "Seeing experiences as temporary loosens fear’s grip.",
              compassion:
                "Pain is acknowledged here, not dismissed.",
              selfTrust:
                "You have survived change before. This is not the first time."
            },
            anchorLines: [
              "This too will pass.",
              "Endurance is strength."
            ]
          }
        ]
      }
    ]
  },

  /* =========================================================
     THEME 2: CONFUSION
     ========================================================= */
  {
    id: "confusion",
    label: "Confusion",
    subthemes: [
      {
        id: "decision_paralysis",
        label: "Decision Paralysis",
        keywords: ["confused", "decision", "choice", "stuck", "unsure"],
        verses: [
          {
            id: "2.7",
            chapter: 2,
            verse: 7,
            sanskrit:
              "कार्पण्यदोषोपहतस्वभावः",
            transliteration:
              "Kārpaṇya-doṣopahata-svabhāvaḥ",
            translation:
              "My understanding is clouded; I ask what is right.",
            context:
              "Arjuna openly admits confusion and asks for guidance.",
            reflections: {
              psychological:
                "Confusion often appears when values collide, not when they are absent.",
              action:
                "Admitting uncertainty is a form of clarity.",
              detachment:
                "Clarity comes when we stop demanding certainty.",
              compassion:
                "The Gita treats confusion with respect, not judgment.",
              selfTrust:
                "Honesty about doubt is the first step forward."
            },
            anchorLines: [
              "Clarity begins with honesty.",
              "Doubt is not failure."
            ]
          }
        ]
      }
    ]
  },

  /* =========================================================
     THEME 3: ATTACHMENT
     ========================================================= */
  {
    id: "attachment",
    label: "Attachment",
    subthemes: [
      {
        id: "emotional_attachment",
        label: "Emotional Attachment",
        keywords: ["attached", "cling", "obsessed", "relationship", "need"],
        verses: [
          {
            id: "2.62",
            chapter: 2,
            verse: 62,
            sanskrit:
              "ध्यायतो विषयान्पुंसः सङ्गस्तेषूपजायते ।",
            transliteration:
              "Dhyāyato viṣayān puṁsaḥ saṅgas teṣūpajāyate",
            translation:
              "Attachment arises from dwelling on objects of desire.",
            context:
              "Krishna traces suffering back to uncontrolled attachment.",
            reflections: {
              psychological:
                "What we repeatedly dwell on quietly shapes our emotions.",
              action:
                "Attention is the first place where freedom begins.",
              detachment:
                "Letting go starts with where the mind rests.",
              compassion:
                "Attachment grows subtly, not through weakness.",
              selfTrust:
                "You can redirect attention gently, not forcefully."
            },
            anchorLines: [
              "Notice where the mind lingers.",
              "Attention shapes attachment."
            ]
          }
        ]
      }
    ]
  },

  /* =========================================================
     THEME 4: GRIEF
     ========================================================= */
  {
    id: "grief",
    label: "Grief",
    subthemes: [
      {
        id: "loss",
        label: "Loss and Grief",
        keywords: ["grief", "loss", "death", "gone", "sad"],
        verses: [
          {
            id: "2.13",
            chapter: 2,
            verse: 13,
            sanskrit:
              "देहिनोऽस्मिन्यथा देहे",
            transliteration:
              "Dehino ’smin yathā dehe",
            translation:
              "Just as the body passes through stages, so does life move onward.",
            context:
              "Krishna reframes death as transition rather than annihilation.",
            reflections: {
              psychological:
                "Grief resists impermanence, even when we understand it intellectually.",
              action:
                "Understanding loss does not erase pain, but it softens it.",
              detachment:
                "Impermanence gives grief a wider horizon.",
              compassion:
                "Sorrow is honored here, not denied.",
              selfTrust:
                "Healing unfolds in its own time."
            },
            anchorLines: [
              "Loss is change, not erasure.",
              "Grief moves at its own pace."
            ]
          }
        ]
      }
    ]
  },

  /* =========================================================
     THEME 5: DUTY vs DESIRE
     ========================================================= */
  {
    id: "duty_vs_desire",
    label: "Duty vs Desire",
    subthemes: [
      {
        id: "inner_conflict",
        label: "Inner Conflict",
        keywords: ["duty", "want", "should", "responsibility", "desire"],
        verses: [
          {
            id: "3.35",
            chapter: 3,
            verse: 35,
            sanskrit:
              "श्रेयान्स्वधर्मो विगुणः",
            transliteration:
              "Śreyān sva-dharmo viguṇaḥ",
            translation:
              "It is better to follow one’s own path imperfectly than another’s perfectly.",
            context:
              "Krishna emphasizes authenticity over imitation.",
            reflections: {
              psychological:
                "Conflict arises when we live borrowed values.",
              action:
                "Imperfect authenticity is more sustainable than perfect imitation.",
              detachment:
                "Let go of comparison to find steadiness.",
              compassion:
                "Struggle is part of finding one’s path.",
              selfTrust:
                "Your path need not look like anyone else’s."
            },
            anchorLines: [
              "Your path is enough.",
              "Authenticity over perfection."
            ]
          }
        ]
      }
    ]
  },

  /* =========================================================
     THEME 6: EXHAUSTION
     ========================================================= */
  {
    id: "exhaustion",
    label: "Exhaustion",
    subthemes: [
      {
        id: "burnout",
        label: "Burnout",
        keywords: ["tired", "exhausted", "burnout", "empty", "overwhelmed"],
        verses: [
          {
            id: "6.5",
            chapter: 6,
            verse: 5,
            sanskrit:
              "उद्धरेदात्मनाऽत्मानं",
            transliteration:
              "Uddhared ātmanā ’tmānaṁ",
            translation:
              "One must uplift oneself by oneself.",
            context:
              "Krishna speaks about self-regulation and inner balance.",
            reflections: {
              psychological:
                "Burnout often comes from neglecting inner limits.",
              action:
                "Rest is not escape; it is restoration.",
              detachment:
                "You are allowed to pause without quitting.",
              compassion:
                "Exhaustion is a signal, not a failure.",
              selfTrust:
                "Small care restores strength over time."
            },
            anchorLines: [
              "Rest is part of the path.",
              "Pause without guilt."
            ]
          }
        ]
      }
    ]
  }
];

#!/usr/bin/env python3
"""
Script to enrich verses with emotions and reflections using OpenAI API (GPT-5 nano).
For each verse:
1. Assigns 3-4 emotions in decreasing order
2. Creates an in-depth reflection of about 100 words
"""

import json
import os
import time
from pathlib import Path
from openai import OpenAI
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Initialize OpenAI client
API_KEY = os.getenv('OPENAI_API_KEY')
if not API_KEY:
    raise ValueError("OPENAI_API_KEY not set in environment or .env file")

client = OpenAI(api_key=API_KEY)
# Note: If gpt-5-nano is not available, will try gpt-4o-mini as fallback
MODEL = "gpt-5-nano"  # Using GPT-5 nano as requested
FALLBACK_MODEL = "gpt-4o-mini"

# Input and output paths
INPUT_FILE = Path("app/src/main/java/com/gita/app/data/enriched_gita_formatted.json")
OUTPUT_FILE = INPUT_FILE  # Save to same file

# Emotion categories to choose from
EMOTION_CATEGORIES = [
    "Anxiety", "Grief", "Anger", "Attachment", "Burnout",
    "Identity Crisis", "Intellectual Doubt", "Loneliness",
    "Moral Dilemma", "Pride", "Result-Obsession"
]

def get_emotions_for_verse(verse_data):
    """Get 3-4 emotions in decreasing order for a verse using GPT-5 nano."""
    sanskrit = verse_data.get("sanskrit_text", "")
    transliteration = verse_data.get("transliteration", "")
    english = verse_data.get("english_translation", "")
    context = verse_data.get("arjuna_despair_link", "")
    
    # Use only English text to avoid encoding issues
    prompt = f"""Given this Bhagavad Gita verse, assign 3-4 emotions from this list in decreasing order of relevance:
{', '.join(EMOTION_CATEGORIES)}

Verse:
Transliteration: {transliteration}
English: {english}
Context: {context}

Return ONLY a JSON array of 3-4 emotion strings in decreasing order of relevance, like:
["Anxiety", "Grief", "Anger"]

Do not include any explanation, just the JSON array."""

    try:
        # Try gpt-5-nano first, fallback to gpt-4o-mini if not available
        try:
            response = client.chat.completions.create(
                model=MODEL,
                messages=[
                    {"role": "system", "content": "You are a helpful assistant that analyzes Bhagavad Gita verses and assigns relevant emotions. Always return valid JSON arrays only."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.7,
                max_tokens=100
            )
        except Exception as e:
            if "gpt-5-nano" in str(e).lower() or "model" in str(e).lower():
                try:
                    print(f"  ⚠️  {MODEL} not available, using {FALLBACK_MODEL}")
                except:
                    pass
                response = client.chat.completions.create(
                    model=FALLBACK_MODEL,
                    messages=[
                        {"role": "system", "content": "You are a helpful assistant that analyzes Bhagavad Gita verses and assigns relevant emotions. Always return valid JSON arrays only."},
                        {"role": "user", "content": prompt}
                    ],
                    temperature=0.7,
                    max_tokens=100
                )
            else:
                raise
        
        content = response.choices[0].message.content.strip()
        # Remove markdown code blocks if present
        if content.startswith("```"):
            content = content.split("```")[1]
            if content.startswith("json"):
                content = content[4:]
        content = content.strip()
        
        # Parse JSON
        emotions = json.loads(content)
        if isinstance(emotions, list) and 3 <= len(emotions) <= 4:
            return emotions
        elif isinstance(emotions, list):
            return emotions[:4] if len(emotions) > 4 else emotions
        else:
            return [emotions] if emotions else []
    except Exception as e:
        try:
            print(f"Error getting emotions for verse {verse_data.get('id')}: {str(e)[:100]}")
        except:
            pass
        return []

def get_reflection_for_verse(verse_data):
    """Get an in-depth reflection of about 100 words for a verse using GPT-5 nano."""
    transliteration = verse_data.get("transliteration", "")
    english = verse_data.get("english_translation", "")
    context = verse_data.get("arjuna_despair_link", "")
    wisdom = verse_data.get("wisdom_nugget", "")
    
    # Use only English text to avoid encoding issues
    prompt = f"""Write an in-depth reflection (approximately 100 words) for this Bhagavad Gita verse.

Verse:
Transliteration: {transliteration}
English: {english}
Context: {context}
Wisdom: {wisdom}

Write a thoughtful, personal reflection that:
- Connects the verse to modern life experiences
- Is approximately 100 words
- Is calm, human, non-religious, non-authoritative
- Allows for doubt and uncertainty
- Does not quote scripture or speak as Krishna
- Focuses on practical wisdom and personal growth

Return ONLY the reflection text, no explanations or formatting."""

    try:
        # Try gpt-5-nano first, fallback to gpt-4o-mini if not available
        try:
            response = client.chat.completions.create(
                model=MODEL,
                messages=[
                    {"role": "system", "content": "You are a thoughtful reflection writer for Bhagavad Gita verses. Write calm, personal, non-religious reflections of approximately 100 words."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.7,
                max_tokens=200
            )
        except Exception as e:
            if "gpt-5-nano" in str(e).lower() or "model" in str(e).lower():
                try:
                    print(f"  ⚠️  {MODEL} not available, using {FALLBACK_MODEL}")
                except:
                    pass
                response = client.chat.completions.create(
                    model=FALLBACK_MODEL,
                    messages=[
                        {"role": "system", "content": "You are a thoughtful reflection writer for Bhagavad Gita verses. Write calm, personal, non-religious reflections of approximately 100 words."},
                        {"role": "user", "content": prompt}
                    ],
                    temperature=0.7,
                    max_tokens=200
                )
            else:
                raise
        
        reflection = response.choices[0].message.content.strip()
        # Remove markdown formatting if present
        if reflection.startswith("```"):
            reflection = reflection.split("```")[1]
            if reflection.startswith("text") or reflection.startswith("markdown"):
                reflection = reflection[4:] if reflection.startswith("text") else reflection[8:]
        reflection = reflection.strip()
        
        return reflection
    except Exception as e:
        try:
            print(f"Error getting reflection for verse {verse_data.get('id')}: {str(e)[:100]}")
        except:
            pass
        return ""

def process_verses(verses, start_index=0):
    """Process all verses, adding emotions and reflections."""
    total = len(verses)
    processed = 0
    
    for i, verse in enumerate(verses[start_index:], start=start_index):
        verse_id = verse.get("id", f"unknown_{i}")
        print(f"\n[{i+1}/{total}] Processing verse {verse_id}...")
        
        # Get emotions (3-4 in decreasing order)
        if "emotions" not in verse or not verse.get("emotions"):
            print(f"  Getting emotions...")
            emotions = get_emotions_for_verse(verse)
            verse["emotions"] = emotions
            print(f"  Emotions: {emotions}")
            time.sleep(0.5)  # Rate limiting
        
        # Get reflection (~100 words)
        if "in_depth_reflection" not in verse or not verse.get("in_depth_reflection"):
            print(f"  Getting reflection...")
            reflection = get_reflection_for_verse(verse)
            verse["in_depth_reflection"] = reflection
            print(f"  Reflection: {reflection[:80]}...")
            time.sleep(0.5)  # Rate limiting
        
        processed += 1
        
        # Save progress every 10 verses
        if processed % 10 == 0:
            print(f"\n💾 Saving progress... ({processed}/{total} processed)")
            save_verses(verses)
    
    return verses

def save_verses(verses):
    """Save verses to JSON file without formatting (preserve current format)."""
    try:
        # Read the original file to preserve formatting
        with open(INPUT_FILE, 'r', encoding='utf-8') as f:
            original_content = f.read()
        
        # Parse and update
        data = json.loads(original_content)
        
        # Update with new data
        for i, verse in enumerate(verses):
            if i < len(data):
                data[i].update(verse)
        
        # Write back without extra formatting
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        
        print(f"Saved {len(verses)} verses to {OUTPUT_FILE}")
    except Exception as e:
        print(f"Error saving verses: {e}")
        raise

def main():
    """Main function."""
    print("=" * 60)
    print("Bhagavad Gita Verse Enrichment Script")
    print("Using GPT-5 nano model")
    print("=" * 60)
    
    # Load verses
    print(f"\nLoading verses from {INPUT_FILE}...")
    try:
        with open(INPUT_FILE, 'r', encoding='utf-8') as f:
            verses = json.load(f)
        print(f"Loaded {len(verses)} verses")
    except Exception as e:
        print(f"❌ Error loading verses: {e}")
        return
    
    # Check which verses need processing
    verses_to_process = []
    for i, verse in enumerate(verses):
        needs_emotions = "emotions" not in verse or not verse.get("emotions")
        needs_reflection = "in_depth_reflection" not in verse or not verse.get("in_depth_reflection")
        if needs_emotions or needs_reflection:
            verses_to_process.append((i, verse))
    
    if not verses_to_process:
        print("\nAll verses already have emotions and reflections!")
        return
    
    print(f"\nFound {len(verses_to_process)} verses that need processing")
    
    # Process verses
    try:
        for idx, verse in verses_to_process:
            verse_id = verse.get("id", f"unknown_{idx}")
            print(f"\n[{idx+1}/{len(verses)}] Processing verse {verse_id}...")
            
            # Get emotions if needed
            if "emotions" not in verse or not verse.get("emotions"):
                print(f"  Getting emotions...")
                emotions = get_emotions_for_verse(verse)
                verse["emotions"] = emotions
                verses[idx] = verse
                print(f"  Emotions: {emotions}")
                time.sleep(0.5)  # Rate limiting
            
            # Get reflection if needed
            if "in_depth_reflection" not in verse or not verse.get("in_depth_reflection"):
                print(f"  Getting reflection...")
                reflection = get_reflection_for_verse(verse)
                verse["in_depth_reflection"] = reflection
                verses[idx] = verse
                print(f"  Reflection: {reflection[:80]}...")
                time.sleep(0.5)  # Rate limiting
            
            # Save progress every 5 verses
            if (idx + 1) % 5 == 0:
                print(f"\nSaving progress... ({idx+1}/{len(verses)} processed)")
                save_verses(verses)
        
        # Final save
        print(f"\nSaving final results...")
        save_verses(verses)
        print(f"\nDone! Processed {len(verses_to_process)} verses")
        
    except KeyboardInterrupt:
        print("\n\nInterrupted by user. Saving progress...")
        save_verses(verses)
        print("Progress saved!")
    except Exception as e:
        print(f"\nError during processing: {e}")
        import traceback
        traceback.print_exc()
        print("\nAttempting to save progress...")
        save_verses(verses)

if __name__ == "__main__":
    main()


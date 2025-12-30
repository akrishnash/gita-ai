import json
import os
import sys
from openai import OpenAI
from typing import List, Dict, Any
import time

def get_emotions_and_reflection(verse: Dict[str, Any], client: OpenAI) -> tuple[List[str], str]:
    """
    Get 1-2 emotions and in-depth reflection for a verse using GPT-4o-mini
    """
    prompt = f"""Analyze this verse from the Bhagavad Gita and provide:

1. 1-2 primary emotions (in decreasing order of prominence) that this verse evokes or addresses
2. An in-depth reflection of approximately 100 words that explores the deeper meaning and relevance of this verse

Verse ID: {verse['id']}
Sanskrit: {verse.get('sanskrit_text', '')}
English Translation: {verse.get('english_translation', '')}

Please respond in the following JSON format:
{{
  "emotions": ["emotion1", "emotion2"],
  "reflection": "Your reflection here (approximately 100 words)"
}}

If only one emotion is strongly present, provide only one emotion in the array."""

    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are an expert in analyzing spiritual texts and identifying emotional themes. Provide thoughtful, insightful analysis."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.7,
            response_format={"type": "json_object"}
        )
        
        result = json.loads(response.choices[0].message.content)
        emotions = result.get("emotions", [])
        reflection = result.get("reflection", "")
        
        return emotions, reflection
    
    except Exception as e:
        print(f"Error processing verse {verse['id']}: {e}")
        # Return defaults if API call fails
        return ["Uncertainty"], "An error occurred while generating reflection for this verse."

def update_verses(input_file: str, output_file: str, client: OpenAI, num_verses: int = 100):
    """
    Update the first N verses with new emotions and reflections
    """
    print(f"Reading JSON file: {input_file}")
    
    # Read the original JSON file
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    print(f"Total verses in file: {len(data)}")
    print(f"Processing first {num_verses} verses...")
    
    # Process first 100 verses
    for i, verse in enumerate(data[:num_verses], 1):
        print(f"\nProcessing verse {i}/{num_verses}: {verse['id']}")
        
        # Get emotions and reflection from OpenAI
        emotions, reflection = get_emotions_and_reflection(verse, client)
        
        # Update the verse
        verse['emotions'] = emotions
        verse['in_depth_reflection'] = reflection
        
        print(f"  Emotions: {emotions}")
        print(f"  Reflection length: {len(reflection)} words")
        
        # Small delay to avoid rate limiting
        time.sleep(0.5)
    
    print(f"\nSaving updated JSON to: {output_file}")
    
    # Save without formatting (preserve original structure)
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, separators=(',', ':'))
    
    print("Done!")

if __name__ == "__main__":
    input_file = "app/src/main/java/com/gita/app/data/enriched_gita_formatted.json"
    output_file = "app/src/main/java/com/gita/app/data/enriched_gita_formatted.json"
    
    # Get API key from command line argument or environment variable
    api_key = None
    if len(sys.argv) > 1:
        api_key = sys.argv[1]
    else:
        api_key = os.getenv("OPENAI_API_KEY")
    
    if not api_key:
        print("Error: OpenAI API key is required.")
        print("Usage: python update_verses_emotions.py <your-openai-api-key>")
        print("Or set environment variable: export OPENAI_API_KEY='your-api-key' (Linux/Mac)")
        print("Or: set OPENAI_API_KEY=your-api-key (Windows)")
        exit(1)
    
    # Initialize OpenAI client
    client = OpenAI(api_key=api_key)
    
    update_verses(input_file, output_file, client, num_verses=100)


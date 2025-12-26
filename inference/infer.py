"""
Inference script for verse and story matching.
Takes a user phrase, gets its embedding, and returns the best matching verse and story.
"""
import json
import numpy as np
from pathlib import Path
import sys
from dotenv import load_dotenv
import os

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from tinygrad import Tensor
from query_key.bi_encoder_verse import KeyQueryModel as VerseModel
from query_key.bi_encoder_story import KeyQueryModel as StoryModel
from query_key.utils import get_phrase_embedding, project_story_embeddings
from openai import OpenAI

# Load environment variables
load_dotenv()

# Initialize OpenAI client
API_KEY = os.getenv('OPENAI_API_KEY')
if not API_KEY:
    raise ValueError("OPENAI_API_KEY not set in environment or .env file")
client = OpenAI(api_key=API_KEY)

# Inference hyperparameters
TEMPERATURE = 1.0  # Higher temperature for softer, less confident predictions (training uses 0.07)
TOP_K = 1  # Number of top results to return

# Paths relative to inference folder
INFERENCE_DIR = Path(__file__).parent
VERSE_MODEL_PATH = INFERENCE_DIR / "models" / "verse_model" / "last_model.pkl"
STORY_MODEL_PATH = INFERENCE_DIR / "models" / "story_model" / "last_model.pkl"
VERSE_EMBEDDINGS_PATH = INFERENCE_DIR / "verse_embeddings_dict.json"
STORY_EMBEDDINGS_PATH = INFERENCE_DIR / "story_embeddings_dict.json"
VERSES_JSON_PATH = INFERENCE_DIR / "verses.json"
STORIES_JSON_PATH = INFERENCE_DIR / "stories.json"


def load_model(checkpoint_path: Path, model_class):
    """Load a model from checkpoint."""
    import pickle
    
    with open(checkpoint_path, 'rb') as f:
        state_dict = pickle.load(f)
    
    # Initialize model
    model = model_class(input_dim=1536, proj_dim=256, hidden_dim=32)
    
    # Load weights
    model.query_proj.weight.data = Tensor(state_dict['query_proj.weight'])
    model.key_fc1.weight.data = Tensor(state_dict['key_fc1.weight'])
    if state_dict.get('key_fc1.bias') is not None:
        model.key_fc1.bias.data = Tensor(state_dict['key_fc1.bias'])
    model.key_fc2.weight.data = Tensor(state_dict['key_fc2.weight'])
    if state_dict.get('key_fc2.bias') is not None:
        model.key_fc2.bias.data = Tensor(state_dict['key_fc2.bias'])
    
    return model


def load_verse_embeddings():
    """Load verse embeddings (1536-dim)."""
    with open(VERSE_EMBEDDINGS_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data.get('embeddings', {})


def load_story_embeddings():
    """Load story embeddings (3072-dim) and project to 1536-dim."""
    with open(STORY_EMBEDDINGS_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)
    story_embeddings_3072 = data.get('embeddings', {})
    # Project to 1536-dim
    return project_story_embeddings(story_embeddings_3072)


def load_verses():
    """Load verses JSON to get verse text by ID."""
    with open(VERSES_JSON_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)
    verses = data.get('verses', [])
    # Create dict mapping verse ID to verse data
    return {verse['id']: verse for verse in verses}


def load_stories():
    """Load stories JSON to get story text by key."""
    with open(STORIES_JSON_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)
    stories = data.get('stories', [])
    # Create dict mapping story key to story data
    return {story['key']: story for story in stories}


def find_best_verse(phrase: str, verse_model, verse_embeddings, verses_dict, temperature=TEMPERATURE, top_k=TOP_K):
    """Find the best matching verse(s) for a phrase using temperature scaling."""
    # Get phrase embedding
    phrase_emb = get_phrase_embedding(phrase)
    
    # Encode query
    query_tensor = Tensor(np.array([phrase_emb], dtype=np.float32))
    query_encoded = verse_model.encode_query(query_tensor)
    
    # Batch encode all verse keys
    verse_ids = list(verse_embeddings.keys())
    verse_embs_array = np.array([verse_embeddings[vid] for vid in verse_ids], dtype=np.float32)
    keys_tensor = Tensor(verse_embs_array)
    keys_encoded = verse_model.encode_key(keys_tensor)
    
    # Compute similarity scores for all verses at once
    scores = verse_model.score(query_encoded, keys_encoded)
    scores_array = scores.numpy().flatten()
    
    # Apply temperature scaling
    scaled_scores = scores_array / temperature
    
    # Get top-K results
    top_indices = np.argsort(scaled_scores)[-top_k:][::-1]  # Sort descending, take top-K
    
    results = []
    for idx in top_indices:
        verse_id = verse_ids[idx]
        score = scores_array[idx]  # Original score
        scaled_score = scaled_scores[idx]  # Temperature-scaled score
        if verse_id in verses_dict:
            results.append((verses_dict[verse_id], score, scaled_score))
    
    return results


def find_best_story(phrase: str, story_model, story_embeddings, stories_dict, temperature=TEMPERATURE, top_k=TOP_K):
    """Find the best matching story(ies) for a phrase using temperature scaling."""
    # Get phrase embedding
    phrase_emb = get_phrase_embedding(phrase)
    
    # Encode query
    query_tensor = Tensor(np.array([phrase_emb], dtype=np.float32))
    query_encoded = story_model.encode_query(query_tensor)
    
    # Batch encode all story keys
    story_keys = list(story_embeddings.keys())
    story_embs_array = np.array([story_embeddings[sk] for sk in story_keys], dtype=np.float32)
    keys_tensor = Tensor(story_embs_array)
    keys_encoded = story_model.encode_key(keys_tensor)
    
    # Compute similarity scores for all stories at once
    scores = story_model.score(query_encoded, keys_encoded)
    scores_array = scores.numpy().flatten()
    
    # Apply temperature scaling
    scaled_scores = scores_array / temperature
    
    # Get top-K results
    top_indices = np.argsort(scaled_scores)[-top_k:][::-1]  # Sort descending, take top-K
    
    results = []
    for idx in top_indices:
        story_key = story_keys[idx]
        score = scores_array[idx]  # Original score
        scaled_score = scaled_scores[idx]  # Temperature-scaled score
        if story_key in stories_dict:
            results.append((stories_dict[story_key], score, scaled_score))
    
    return results


def main():
    """Main inference function."""
    print("Loading models and data...")
    
    # Load models
    print("  Loading verse model...")
    verse_model = load_model(VERSE_MODEL_PATH, VerseModel)
    print("  Loading story model...")
    story_model = load_model(STORY_MODEL_PATH, StoryModel)
    
    # Load embeddings
    print("  Loading verse embeddings...")
    verse_embeddings = load_verse_embeddings()
    print(f"    Loaded {len(verse_embeddings)} verse embeddings")
    
    print("  Loading story embeddings...")
    story_embeddings = load_story_embeddings()
    print(f"    Loaded {len(story_embeddings)} story embeddings (projected to 1536-dim)")
    
    # Load verse/story text data
    print("  Loading verses and stories...")
    verses_dict = load_verses()
    stories_dict = load_stories()
    print(f"    Loaded {len(verses_dict)} verses and {len(stories_dict)} stories")
    
    print("\n✓ All models and data loaded!\n")
    
    # Interactive loop
    print("=" * 60)
    print("GitaAI Inference - Enter phrases to find matching verses and stories")
    print("Type 'quit' or 'exit' to stop")
    print("=" * 60)
    
    while True:
        # Get user input
        phrase = input("\nEnter your phrase: ").strip()
        
        if phrase.lower() in ['quit', 'exit', 'q']:
            print("\nGoodbye!")
            break
        
        if not phrase:
            print("Please enter a valid phrase.")
            continue
        
        print(f"\nProcessing: '{phrase}'...")
        print("Getting embedding from OpenAI...")
        
        # Find best verse
        print("Finding best matching verse...")
        verse_results = find_best_verse(phrase, verse_model, verse_embeddings, verses_dict, temperature=TEMPERATURE, top_k=TOP_K)
        
        # Find best story
        print("Finding best matching story...")
        story_results = find_best_story(phrase, story_model, story_embeddings, stories_dict, temperature=TEMPERATURE, top_k=TOP_K)
        
        # Display results
        print("\n" + "=" * 60)
        print("RESULTS")
        print(f"(Temperature: {TEMPERATURE})")
        print("=" * 60)
        
        if verse_results:
            for i, (verse, score, scaled_score) in enumerate(verse_results, 1):
                print(f"\n📖 MATCHING VERSE #{i} (Score: {score:.4f}, Scaled: {scaled_score:.4f})")
                print(f"   ID: {verse['id']}")
                print(f"   Translation: {verse.get('translation', 'N/A')}")
                print(f"   Sanskrit: {verse.get('sanskrit', 'N/A')}")
                print(f"   Context: {verse.get('context', 'N/A')}")
                if verse.get('explanation'):
                    print(f"   Explanation: {verse['explanation']}")
        else:
            print("\n❌ No verse found")
        
        if story_results:
            for i, (story, score, scaled_score) in enumerate(story_results, 1):
                print(f"\n📚 MATCHING STORY #{i} (Score: {score:.4f}, Scaled: {scaled_score:.4f})")
                print(f"   Key: {story['key']}")
                print(f"   Title: {story.get('title', 'N/A')}")
                print(f"   Text: {story.get('text', 'N/A')}")
        else:
            print("\n❌ No story found")
        
        print("\n" + "=" * 60)


if __name__ == "__main__":
    main()


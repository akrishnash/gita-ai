#!/usr/bin/env python3
"""Check progress of verse enrichment."""

import json
from pathlib import Path

INPUT_FILE = Path("app/src/main/java/com/gita/app/data/enriched_gita_formatted.json")

with open(INPUT_FILE, 'r', encoding='utf-8') as f:
    data = json.load(f)

total = len(data)
with_emotions = sum(1 for v in data if "emotions" in v and v.get("emotions"))
with_reflections = sum(1 for v in data if "in_depth_reflection" in v and v.get("in_depth_reflection"))

print(f"Total verses: {total}")
print(f"Verses with emotions: {with_emotions} ({with_emotions/total*100:.1f}%)")
print(f"Verses with reflections: {with_reflections} ({with_reflections/total*100:.1f}%)")
print(f"Remaining: {total - max(with_emotions, with_reflections)}")


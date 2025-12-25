"""
Converts the TinyGrad bi-encoder pickle checkpoints into a compact binary format
that can be loaded directly by the Android (Kotlin-only) runtime.
"""

from __future__ import annotations

import pickle
import struct
from pathlib import Path


def _write_matrix_f32_be(f, mat) -> None:
    # mat is a numpy ndarray (rows, cols)
    rows, cols = mat.shape
    f.write(struct.pack(">II", rows, cols))
    # write row-major float32
    for r in range(rows):
        row = mat[r]
        for v in row:
            f.write(struct.pack(">f", float(v)))


def _write_vector_f32_be(f, vec) -> None:
    # vec is a numpy ndarray (n,)
    n = int(vec.shape[0])
    f.write(struct.pack(">I", n))
    for v in vec:
        f.write(struct.pack(">f", float(v)))


def convert_model_to_binary(pickle_path: Path, out_path: Path) -> None:
    sd = pickle.load(open(pickle_path, "rb"))

    with open(out_path, "wb") as f:
        # Magic + version
        f.write(b"GITA_MDL")
        f.write(struct.pack(">I", 1))

        # Required tensors
        _write_matrix_f32_be(f, sd["query_proj.weight"])  # (256,1536)
        _write_matrix_f32_be(f, sd["key_fc1.weight"])     # (32,1536)
        _write_vector_f32_be(f, sd["key_fc1.bias"])       # (32,)
        _write_matrix_f32_be(f, sd["key_fc2.weight"])     # (256,32)
        _write_vector_f32_be(f, sd["key_fc2.bias"])       # (256,)


def main() -> None:
    inference_dir = Path(__file__).parent
    verse_pkl = inference_dir / "models" / "verse_model" / "last_model.pkl"
    story_pkl = inference_dir / "models" / "story_model" / "last_model.pkl"

    verse_out = inference_dir / "models" / "verse_model" / "model.bin"
    story_out = inference_dir / "models" / "story_model" / "model.bin"

    convert_model_to_binary(verse_pkl, verse_out)
    convert_model_to_binary(story_pkl, story_out)

    print(f"Wrote: {verse_out} ({verse_out.stat().st_size/1024:.1f} KB)")
    print(f"Wrote: {story_out} ({story_out.stat().st_size/1024:.1f} KB)")


if __name__ == "__main__":
    main()



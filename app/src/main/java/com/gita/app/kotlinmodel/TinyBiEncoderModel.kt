package com.gita.app.kotlinmodel

import android.content.Context
import android.util.Log
import java.io.DataInputStream
import kotlin.math.max

/**
 * Kotlin-only runtime for the TinyGrad bi-encoder checkpoints.
 *
 * Binary format is produced by `inference/convert_models_to_binary.py`:
 * - magic "GITA_MDL" + version u32
 * - query_proj.weight: (rows u32, cols u32) + rows*cols float32 (big-endian)
 * - key_fc1.weight:    (rows u32, cols u32) + rows*cols float32
 * - key_fc1.bias:      (n u32) + n float32
 * - key_fc2.weight:    (rows u32, cols u32) + rows*cols float32
 * - key_fc2.bias:      (n u32) + n float32
 */
class TinyBiEncoderModel(
    context: Context,
    assetPath: String
) {
    companion object {
        private const val TAG = "TinyBiEncoderModel"
        const val INPUT_DIM = 1536
        const val PROJ_DIM = 256
        const val HIDDEN_DIM = 32
    }

    private val queryProj: Array<FloatArray>  // [256][1536]
    private val keyFc1W: Array<FloatArray>    // [32][1536]
    private val keyFc1B: FloatArray           // [32]
    private val keyFc2W: Array<FloatArray>    // [256][32]
    private val keyFc2B: FloatArray           // [256]

    init {
        context.assets.open(assetPath).use { raw ->
            val input = DataInputStream(raw)

            // header
            val magic = ByteArray(8)
            input.readFully(magic)
            val magicStr = String(magic, Charsets.US_ASCII)
            require(magicStr == "GITA_MDL") { "Invalid model magic: $magicStr" }
            val version = input.readInt()
            require(version == 1) { "Unsupported model version: $version" }

            queryProj = readMatrix(input)
            keyFc1W = readMatrix(input)
            keyFc1B = readVector(input)
            keyFc2W = readMatrix(input)
            keyFc2B = readVector(input)

            Log.d(TAG, "Loaded $assetPath (queryProj=${queryProj.size}x${queryProj[0].size})")
        }
    }

    fun encodeQuery(x: FloatArray): FloatArray {
        require(x.size == INPUT_DIM) { "Query embedding must be $INPUT_DIM-dim, got ${x.size}" }
        val out = FloatArray(PROJ_DIM)
        for (i in 0 until PROJ_DIM) {
            var sum = 0f
            val row = queryProj[i]
            for (j in 0 until INPUT_DIM) sum += row[j] * x[j]
            out[i] = sum
        }
        return out
    }

    fun encodeKey(x: FloatArray): FloatArray {
        require(x.size == INPUT_DIM) { "Key embedding must be $INPUT_DIM-dim, got ${x.size}" }

        // fc1 + relu
        val hidden = FloatArray(HIDDEN_DIM)
        for (i in 0 until HIDDEN_DIM) {
            var sum = keyFc1B[i]
            val row = keyFc1W[i]
            for (j in 0 until INPUT_DIM) sum += row[j] * x[j]
            hidden[i] = relu(sum)
        }

        // fc2
        val out = FloatArray(PROJ_DIM)
        for (i in 0 until PROJ_DIM) {
            var sum = keyFc2B[i]
            val row = keyFc2W[i]
            for (j in 0 until HIDDEN_DIM) sum += row[j] * hidden[j]
            out[i] = sum
        }
        return out
    }

    fun scoreDot(encodedQuery: FloatArray, encodedKey: FloatArray): Float {
        var dot = 0f
        for (i in 0 until PROJ_DIM) dot += encodedQuery[i] * encodedKey[i]
        return dot
    }

    private fun relu(v: Float): Float = max(0f, v)

    /**
     * The model binaries are written as big-endian float32; Java/Kotlin DataInputStream
     * reads big-endian primitives, so `readFloat()` matches the writer.
     */
    private fun readMatrix(input: DataInputStream): Array<FloatArray> {
        val rows = input.readInt()
        val cols = input.readInt()
        val m = Array(rows) { FloatArray(cols) }
        for (r in 0 until rows) {
            val row = m[r]
            for (c in 0 until cols) row[c] = input.readFloat()
        }
        return m
    }

    private fun readVector(input: DataInputStream): FloatArray {
        val n = input.readInt()
        val v = FloatArray(n)
        for (i in 0 until n) v[i] = input.readFloat()
        return v
    }
}



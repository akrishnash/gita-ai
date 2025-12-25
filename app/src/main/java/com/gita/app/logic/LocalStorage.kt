package com.gita.app.logic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gita.app.data.ReflectionAngle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gita_storage")

class LocalStorage(private val context: Context) {
    companion object {
        private val SEEN_VERSE_IDS_KEY = stringSetPreferencesKey("seen_verse_ids")
        private val LAST_VERSE_ID_KEY = stringPreferencesKey("last_verse_id")
        private val AI_API_KEY_KEY = stringPreferencesKey("ai_api_key")
        private val HISTORY_ENTRIES_KEY = stringSetPreferencesKey("history_entries")
        private fun getReflectionAngleKey(verseId: String) = stringPreferencesKey("reflection_angle_$verseId")
    }
    
    suspend fun addSeenVerseId(verseId: String) {
        try {
            context.dataStore.edit { preferences ->
                val currentSet = preferences[SEEN_VERSE_IDS_KEY] ?: emptySet()
                preferences[SEEN_VERSE_IDS_KEY] = currentSet + verseId
            }
        } catch (e: IllegalStateException) {
            // DataStore might be closed or context invalid
            android.util.Log.e("LocalStorage", "DataStore error in addSeenVerseId", e)
        } catch (e: Exception) {
            android.util.Log.e("LocalStorage", "Unexpected error in addSeenVerseId", e)
            // Continue without crashing
        }
    }
    
    suspend fun getSeenVerseIdsSuspend(): Set<String> {
        return try {
            context.dataStore.data.first()[SEEN_VERSE_IDS_KEY] ?: emptySet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet() // Return empty set on error
        }
    }
    
    fun getSeenVerseIds(): Set<String> {
        return runBlocking {
            context.dataStore.data.first()[SEEN_VERSE_IDS_KEY] ?: emptySet()
        }
    }
    
    suspend fun setLastVerseId(verseId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LAST_VERSE_ID_KEY] = verseId
            }
        } catch (e: IllegalStateException) {
            android.util.Log.e("LocalStorage", "DataStore error in setLastVerseId", e)
        } catch (e: Exception) {
            android.util.Log.e("LocalStorage", "Unexpected error in setLastVerseId", e)
            // Continue without crashing
        }
    }
    
    suspend fun getLastVerseIdSuspend(): String? {
        return try {
            context.dataStore.data.first()[LAST_VERSE_ID_KEY]
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null on error
        }
    }
    
    fun getLastVerseId(): String? {
        return runBlocking {
            context.dataStore.data.first()[LAST_VERSE_ID_KEY]
        }
    }
    
    suspend fun setLastReflectionAngle(verseId: String, angle: ReflectionAngle) {
        try {
            context.dataStore.edit { preferences ->
                preferences[getReflectionAngleKey(verseId)] = angle.name
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Continue without crashing
        }
    }
    
    suspend fun getLastReflectionAngleSuspend(verseId: String): ReflectionAngle? {
        return try {
            val angleName = context.dataStore.data.first()[getReflectionAngleKey(verseId)]
            angleName?.let { 
                try {
                    ReflectionAngle.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null on error
        }
    }
    
    fun getLastReflectionAngle(verseId: String): ReflectionAngle? {
        val angleName = runBlocking {
            context.dataStore.data.first()[getReflectionAngleKey(verseId)]
        }
        return angleName?.let { 
            try {
                ReflectionAngle.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    suspend fun setAiApiKey(key: String?) {
        try {
            context.dataStore.edit { preferences ->
                if (key != null && key.isNotBlank()) {
                    preferences[AI_API_KEY_KEY] = key
                } else {
                    preferences.remove(AI_API_KEY_KEY)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Continue without crashing
        }
    }
    
    suspend fun getAiApiKey(): String? {
        return try {
            context.dataStore.data.first()[AI_API_KEY_KEY]
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null on error
        }
    }
    
    suspend fun addHistoryEntry(entry: HistoryEntry) {
        try {
            context.dataStore.edit { preferences ->
                val currentSet = preferences[HISTORY_ENTRIES_KEY] ?: emptySet()
                val entryString = "${entry.timestamp}|${entry.userInput}|${entry.verseId}|${entry.anchorLine}"
                val newList = (currentSet + entryString).toList().takeLast(100) // Keep last 100
                preferences[HISTORY_ENTRIES_KEY] = newList.toSet()
            }
        } catch (e: IllegalStateException) {
            android.util.Log.e("LocalStorage", "DataStore error in addHistoryEntry", e)
        } catch (e: Exception) {
            android.util.Log.e("LocalStorage", "Unexpected error in addHistoryEntry", e)
            // Continue without crashing - history is non-critical
        }
    }
    
    suspend fun getHistoryEntries(): List<HistoryEntry> {
        return try {
            val entries = context.dataStore.data.first()[HISTORY_ENTRIES_KEY] ?: emptySet()
            entries.mapNotNull { entryString ->
                val parts = entryString.split("|")
                if (parts.size >= 4) {
                    try {
                        HistoryEntry(
                            timestamp = parts[0].toLongOrNull() ?: 0L,
                            userInput = parts[1],
                            verseId = parts[2],
                            anchorLine = parts[3]
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return empty list on error
        }
    }
}

data class HistoryEntry(
    val timestamp: Long,
    val userInput: String,
    val verseId: String,
    val anchorLine: String
)



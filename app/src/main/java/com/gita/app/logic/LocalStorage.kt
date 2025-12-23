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
        context.dataStore.edit { preferences ->
            val currentSet = preferences[SEEN_VERSE_IDS_KEY] ?: emptySet()
            preferences[SEEN_VERSE_IDS_KEY] = currentSet + verseId
        }
    }
    
    fun getSeenVerseIds(): Set<String> {
        return runBlocking {
            context.dataStore.data.first()[SEEN_VERSE_IDS_KEY] ?: emptySet()
        }
    }
    
    suspend fun setLastVerseId(verseId: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_VERSE_ID_KEY] = verseId
        }
    }
    
    fun getLastVerseId(): String? {
        return runBlocking {
            context.dataStore.data.first()[LAST_VERSE_ID_KEY]
        }
    }
    
    suspend fun setLastReflectionAngle(verseId: String, angle: ReflectionAngle) {
        context.dataStore.edit { preferences ->
            preferences[getReflectionAngleKey(verseId)] = angle.name
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
        context.dataStore.edit { preferences ->
            if (key != null && key.isNotBlank()) {
                preferences[AI_API_KEY_KEY] = key
            } else {
                preferences.remove(AI_API_KEY_KEY)
            }
        }
    }
    
    suspend fun getAiApiKey(): String? {
        return context.dataStore.data.first()[AI_API_KEY_KEY]
    }
    
    suspend fun addHistoryEntry(entry: HistoryEntry) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[HISTORY_ENTRIES_KEY] ?: emptySet()
            val entryString = "${entry.timestamp}|${entry.userInput}|${entry.verseId}|${entry.anchorLine}"
            val newSet = (currentSet + entryString).takeLast(100).toSet()
            preferences[HISTORY_ENTRIES_KEY] = newSet
        }
    }
    
    suspend fun getHistoryEntries(): List<HistoryEntry> {
        val entries = context.dataStore.data.first()[HISTORY_ENTRIES_KEY] ?: emptySet()
        return entries.mapNotNull { entryString ->
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
    }
}

data class HistoryEntry(
    val timestamp: Long,
    val userInput: String,
    val verseId: String,
    val anchorLine: String
)


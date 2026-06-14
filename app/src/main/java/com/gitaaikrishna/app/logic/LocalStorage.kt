package com.gitaaikrishna.app.logic

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gitaaikrishna.app.data.BookmarkedVerse
import com.gitaaikrishna.app.data.ReflectionAngle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gita_storage")

/**
 * Persistent local storage using Jetpack DataStore.
 * 
 * All operations are suspend-only — no blocking calls on the main thread.
 * History entries are serialized as JSON to avoid delimiter-based corruption.
 */
class LocalStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalStorage"
        private val SEEN_VERSE_IDS_KEY = stringSetPreferencesKey("seen_verse_ids")
        private val LAST_VERSE_ID_KEY = stringPreferencesKey("last_verse_id")
        private val AI_API_KEY_KEY = stringPreferencesKey("ai_api_key")
        private val HISTORY_ENTRIES_KEY = stringPreferencesKey("history_entries_json")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val DAILY_VERSE_ENABLED_KEY = booleanPreferencesKey("daily_verse_enabled")
        private val DAILY_VERSE_HOUR_KEY = intPreferencesKey("daily_verse_hour")
        private val BOOKMARKS_KEY = stringPreferencesKey("bookmarked_verses")
        private fun getReflectionAngleKey(verseId: String) = stringPreferencesKey("reflection_angle_$verseId")
    }
    
    private val gson = Gson()
    
    // ═══════════════════════════════════════════════════════════════
    // Verse tracking
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun addSeenVerseId(verseId: String) {
        try {
            context.dataStore.edit { preferences ->
                val currentSet = preferences[SEEN_VERSE_IDS_KEY] ?: emptySet()
                preferences[SEEN_VERSE_IDS_KEY] = currentSet + verseId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save seen verse ID", e)
        }
    }
    
    suspend fun getSeenVerseIds(): Set<String> {
        return try {
            context.dataStore.data.first()[SEEN_VERSE_IDS_KEY] ?: emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load seen verse IDs", e)
            emptySet()
        }
    }
    
    suspend fun setLastVerseId(verseId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LAST_VERSE_ID_KEY] = verseId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save last verse ID", e)
        }
    }
    
    suspend fun getLastVerseId(): String? {
        return try {
            context.dataStore.data.first()[LAST_VERSE_ID_KEY]
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load last verse ID", e)
            null
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Reflection angle tracking
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun setLastReflectionAngle(verseId: String, angle: ReflectionAngle) {
        try {
            context.dataStore.edit { preferences ->
                preferences[getReflectionAngleKey(verseId)] = angle.name
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save reflection angle", e)
        }
    }
    
    suspend fun getLastReflectionAngle(verseId: String): ReflectionAngle? {
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
            Log.e(TAG, "Failed to load reflection angle", e)
            null
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // API key
    // ═══════════════════════════════════════════════════════════════
    
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
            Log.e(TAG, "Failed to save API key", e)
        }
    }
    
    suspend fun getAiApiKey(): String? {
        return try {
            context.dataStore.data.first()[AI_API_KEY_KEY]
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load API key", e)
            null
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // History — JSON serialized for safety
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun addHistoryEntry(entry: HistoryEntry) {
        try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[HISTORY_ENTRIES_KEY] ?: "[]"
                val currentList = try {
                    val type = object : TypeToken<MutableList<HistoryEntry>>() {}.type
                    gson.fromJson<MutableList<HistoryEntry>>(currentJson, type) ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
                
                currentList.add(0, entry) // Add to front (newest first)
                
                // Keep last 100 entries
                val trimmed = if (currentList.size > 100) {
                    currentList.subList(0, 100)
                } else {
                    currentList
                }
                
                preferences[HISTORY_ENTRIES_KEY] = gson.toJson(trimmed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save history entry", e)
        }
    }
    
    suspend fun getHistoryEntries(): List<HistoryEntry> {
        return try {
            val json = context.dataStore.data.first()[HISTORY_ENTRIES_KEY] ?: return emptyList()
            val type = object : TypeToken<List<HistoryEntry>>() {}.type
            gson.fromJson<List<HistoryEntry>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load history entries", e)
            emptyList()
        }
    }
    
    suspend fun deleteHistoryEntry(timestamp: Long) {
        try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[HISTORY_ENTRIES_KEY] ?: return@edit
                val type = object : TypeToken<MutableList<HistoryEntry>>() {}.type
                val currentList = gson.fromJson<MutableList<HistoryEntry>>(currentJson, type) ?: return@edit
                currentList.removeAll { it.timestamp == timestamp }
                preferences[HISTORY_ENTRIES_KEY] = gson.toJson(currentList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete history entry", e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // User preferences — dark mode & language
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun setDarkMode(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = enabled
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save dark mode preference", e)
        }
    }
    
    suspend fun getDarkMode(): Boolean {
        return try {
            context.dataStore.data.first()[DARK_MODE_KEY] ?: true // Default dark
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load dark mode preference", e)
            true
        }
    }
    
    suspend fun setLanguage(languageCode: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save language preference", e)
        }
    }
    
    suspend fun getLanguage(): String {
        return try {
            context.dataStore.data.first()[LANGUAGE_KEY] ?: "en" // Default English
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load language preference", e)
            "en"
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Daily verse notification preferences
    // ═══════════════════════════════════════════════════════════════

    suspend fun setDailyVerseEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { it[DAILY_VERSE_ENABLED_KEY] = enabled }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save daily verse enabled", e)
        }
    }

    suspend fun getDailyVerseEnabled(): Boolean {
        return try {
            context.dataStore.data.first()[DAILY_VERSE_ENABLED_KEY] ?: true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load daily verse enabled", e)
            true
        }
    }

    suspend fun setDailyVerseHour(hour: Int) {
        try {
            context.dataStore.edit { it[DAILY_VERSE_HOUR_KEY] = hour }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save daily verse hour", e)
        }
    }

    suspend fun getDailyVerseHour(): Int {
        return try {
            context.dataStore.data.first()[DAILY_VERSE_HOUR_KEY] ?: 8
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load daily verse hour", e)
            8
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Bookmarks
    // ═══════════════════════════════════════════════════════════════

    suspend fun saveBookmark(verse: BookmarkedVerse) {
        try {
            context.dataStore.edit { prefs ->
                val list = parseBookmarks(prefs[BOOKMARKS_KEY]).toMutableList()
                list.removeAll { it.verseId == verse.verseId }
                list.add(0, verse)
                prefs[BOOKMARKS_KEY] = gson.toJson(list)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bookmark", e)
        }
    }

    suspend fun getBookmarks(): List<BookmarkedVerse> {
        return try {
            parseBookmarks(context.dataStore.data.first()[BOOKMARKS_KEY])
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bookmarks", e)
            emptyList()
        }
    }

    suspend fun removeBookmark(verseId: String) {
        try {
            context.dataStore.edit { prefs ->
                val list = parseBookmarks(prefs[BOOKMARKS_KEY]).toMutableList()
                list.removeAll { it.verseId == verseId }
                prefs[BOOKMARKS_KEY] = gson.toJson(list)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove bookmark", e)
        }
    }

    suspend fun isBookmarked(verseId: String): Boolean {
        return try {
            parseBookmarks(context.dataStore.data.first()[BOOKMARKS_KEY]).any { it.verseId == verseId }
        } catch (e: Exception) {
            false
        }
    }

    private fun parseBookmarks(json: String?): List<BookmarkedVerse> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<BookmarkedVerse>>() {}.type
            gson.fromJson<List<BookmarkedVerse>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * A single history entry representing one user interaction.
 */
data class HistoryEntry(
    val timestamp: Long,
    val userInput: String,
    val verseId: String,
    val anchorLine: String
)

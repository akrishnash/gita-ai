package com.gita.app.logic

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakManager {
    private const val PREFS = "streak_prefs"
    private const val KEY_LAST_DATE = "streak_last_date"
    private const val KEY_CURRENT = "streak_current"
    private const val KEY_LONGEST = "streak_longest"
    private val FMT = DateTimeFormatter.ISO_LOCAL_DATE

    fun recordActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now()
        val todayStr = today.format(FMT)
        val lastStr = prefs.getString(KEY_LAST_DATE, null)

        val current = when {
            lastStr == null -> 1
            lastStr == todayStr -> prefs.getInt(KEY_CURRENT, 1)
            LocalDate.parse(lastStr, FMT).plusDays(1) == today -> prefs.getInt(KEY_CURRENT, 0) + 1
            else -> 1
        }
        val longest = maxOf(prefs.getInt(KEY_LONGEST, 0), current)

        prefs.edit()
            .putString(KEY_LAST_DATE, todayStr)
            .putInt(KEY_CURRENT, current)
            .putInt(KEY_LONGEST, longest)
            .apply()
    }

    fun getCurrentStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastStr = prefs.getString(KEY_LAST_DATE, null) ?: return 0
        val last = runCatching { LocalDate.parse(lastStr, FMT) }.getOrNull() ?: return 0
        val today = LocalDate.now()
        // Streak is live only if last activity was today or yesterday
        return if (last == today || last.plusDays(1) == today) {
            prefs.getInt(KEY_CURRENT, 0)
        } else {
            0
        }
    }

    fun getLongestStreak(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LONGEST, 0)
}

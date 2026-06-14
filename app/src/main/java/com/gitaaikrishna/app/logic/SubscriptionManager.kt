package com.gitaaikrishna.app.logic

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

enum class SubscriptionTier { FREE, SADHAK, GYANI }

object SubscriptionManager {

    private const val PREFS_NAME = "gita_subscription"
    private const val KEY_TIER = "sub_tier"
    private const val KEY_EXPIRY = "sub_expiry"
    private const val KEY_QUERY_DATE = "query_count_date"
    private const val KEY_QUERY_COUNT = "query_count"
    private const val KEY_LAST_PAYMENT_ID = "last_payment_id"
    private const val KEY_IS_GUEST = "is_guest"
    private const val GUEST_QUERY_LIMIT = 1

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentTier(context: Context): SubscriptionTier {
        if (!isSubscriptionActive(context)) return SubscriptionTier.FREE
        val name = prefs(context).getString(KEY_TIER, null) ?: return SubscriptionTier.FREE
        return try { SubscriptionTier.valueOf(name) } catch (_: Exception) { SubscriptionTier.FREE }
    }

    fun getDailyLimit(tier: SubscriptionTier): Int = when (tier) {
        SubscriptionTier.FREE -> 3
        SubscriptionTier.SADHAK -> 15
        SubscriptionTier.GYANI -> Int.MAX_VALUE
    }

    fun setGuest(context: Context, isGuest: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_GUEST, isGuest).apply()
    }

    fun isGuest(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_GUEST, false)

    fun getRemainingQueries(context: Context): Int {
        val limit = if (isGuest(context)) GUEST_QUERY_LIMIT
                    else getDailyLimit(getCurrentTier(context))
        if (limit == Int.MAX_VALUE) return Int.MAX_VALUE
        return maxOf(0, limit - getTodayCount(context))
    }

    // Increment the day's query count. Call BEFORE processing the query.
    fun recordQuery(context: Context) {
        val today = dateFmt.format(Date())
        val p = prefs(context)
        val storedDate = p.getString(KEY_QUERY_DATE, null)
        val count = if (storedDate == today) p.getInt(KEY_QUERY_COUNT, 0) else 0
        p.edit()
            .putString(KEY_QUERY_DATE, today)
            .putInt(KEY_QUERY_COUNT, count + 1)
            .apply()
    }

    fun activateSubscription(
        context: Context,
        tier: SubscriptionTier,
        expiryTimestamp: Long,
        paymentId: String = ""
    ) {
        prefs(context).edit()
            .putString(KEY_TIER, tier.name)
            .putLong(KEY_EXPIRY, expiryTimestamp)
            .putString(KEY_LAST_PAYMENT_ID, paymentId)
            .apply()
    }

    fun isSubscriptionActive(context: Context): Boolean =
        prefs(context).getLong(KEY_EXPIRY, 0L) > System.currentTimeMillis()

    fun getExpiryTimestamp(context: Context): Long =
        prefs(context).getLong(KEY_EXPIRY, 0L)

    fun getLastPaymentId(context: Context): String =
        prefs(context).getString(KEY_LAST_PAYMENT_ID, "") ?: ""

    private fun getTodayCount(context: Context): Int {
        val today = dateFmt.format(Date())
        val p = prefs(context)
        val storedDate = p.getString(KEY_QUERY_DATE, null)
        return if (storedDate == today) p.getInt(KEY_QUERY_COUNT, 0) else 0
    }
}

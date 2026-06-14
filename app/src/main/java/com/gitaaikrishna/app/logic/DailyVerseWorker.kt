package com.gitaaikrishna.app.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gitaaikrishna.app.MainActivity

private data class DailyVerse(
    val chapterVerse: String,
    val sanskrit: String,
    val english: String
)

private val DAILY_VERSES = listOf(
    DailyVerse(
        "2.47",
        "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।",
        "You have a right to perform your prescribed duties, but you are not entitled to the fruits of your actions."
    ),
    DailyVerse(
        "2.14",
        "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः।",
        "The contact between the senses and sense objects gives rise to cold, heat, pleasure and pain. These are temporary — they come and go. Bear them patiently."
    ),
    DailyVerse(
        "4.7",
        "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत।",
        "Whenever there is decay of righteousness and rise of unrighteousness, O Arjuna, I manifest myself."
    ),
    DailyVerse(
        "6.5",
        "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।",
        "Let a person uplift themselves by their own self; let them not degrade themselves. The self alone is the friend of the self, and the self alone is the enemy of the self."
    ),
    DailyVerse(
        "9.22",
        "अनन्याश्चिन्तयन्तो मां ये जनाः पर्युपासते।",
        "For those who worship me with devotion, meditating on my transcendental form, I carry what they lack and preserve what they have."
    ),
    DailyVerse(
        "12.13",
        "अद्वेष्टा सर्वभूतानां मैत्रः करुण एव च।",
        "One who is not envious, who is a kind friend to all creatures, free from false ego, equal in distress and joy — such a devotee is very dear to me."
    ),
    DailyVerse(
        "2.20",
        "न जायते म्रियते वा कदाचिन्नायं भूत्वा भविता वा न भूयः।",
        "The soul is never born nor dies at any time. It has not come into being, does not come into being, and will not come into being. It is unborn, eternal, ever-existing and primeval."
    ),
    DailyVerse(
        "18.66",
        "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज।",
        "Abandon all varieties of religion and just surrender unto me. I shall deliver you from all sinful reactions. Do not fear."
    ),
    DailyVerse(
        "3.19",
        "तस्मादसक्तः सततं कार्यं कर्म समाचर।",
        "Therefore, always perform your duty without attachment. By doing so, a person attains the Supreme."
    ),
    DailyVerse(
        "6.35",
        "असंशयं महाबाहो मनो दुर्निग्रहं चलम्।",
        "O mighty-armed Arjuna, the mind is undoubtedly restless and difficult to control. But it can be subdued by constant practice and detachment."
    )
)

class DailyVerseWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_verse"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            ensureChannel()
            val verse = DAILY_VERSES.random()
            showNotification(verse)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Verse",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Your daily Bhagavad Gita verse"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showNotification(verse: DailyVerse) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val preview = if (verse.english.length > 80)
            verse.english.take(80) + "…"
        else
            verse.english

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("॥ Your verse for today ॥")
            .setContentText(preview)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${verse.sanskrit}\n\n${verse.english}\n\n— BG ${verse.chapterVerse}")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

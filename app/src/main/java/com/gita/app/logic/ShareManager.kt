package com.gita.app.logic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import com.gita.app.data.VerseEntry
import com.gita.app.ui.components.ShareCard
import java.io.File

object ShareManager {
    private const val TAG = "ShareManager"
    private const val CARD_PX = 1080
    // Force density=3 so ShareCard dp values render at 3× scale → 1080px output
    private const val RENDER_DENSITY = 3f

    fun shareVerseAsImage(context: Context, verse: VerseEntry, translation: String) {
        val activity = context as? Activity
        if (activity == null) {
            Log.e(TAG, "Context is not an Activity — cannot share as image")
            shareAsText(context, verse, translation)
            return
        }

        Toast.makeText(context, "Preparing card…", Toast.LENGTH_SHORT).show()

        // ComposeView sized in pixels; Compose sees dp = px / density
        val composeView = ComposeView(context).apply {
            setContent {
                CompositionLocalProvider(
                    LocalDensity provides Density(density = RENDER_DENSITY, fontScale = 1f)
                ) {
                    ShareCard(verse = verse)
                }
            }
        }

        val rootView = activity.window.decorView as ViewGroup
        val params = FrameLayout.LayoutParams(CARD_PX, CARD_PX)
        rootView.addView(composeView, params)
        // Shift off-screen so the user never sees it
        composeView.translationX = -(CARD_PX + 100).toFloat()

        composeView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    composeView.viewTreeObserver.removeOnPreDrawListener(this)
                    try {
                        val bitmap = composeView.drawToBitmap(Bitmap.Config.ARGB_8888)
                        rootView.removeView(composeView)
                        launchShareIntent(context, verse, translation, bitmap)
                    } catch (e: Exception) {
                        Log.e(TAG, "drawToBitmap failed", e)
                        rootView.removeView(composeView)
                        shareAsText(context, verse, translation)
                    }
                    return true
                }
            }
        )
    }

    private fun launchShareIntent(
        context: Context,
        verse: VerseEntry,
        translation: String,
        bitmap: Bitmap
    ) {
        try {
            val file = File(context.cacheDir, "share_card.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(
                context,
                "com.gita.app.fileprovider",
                file
            )

            val shareText = buildShareText(verse, translation)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share verse"))
        } catch (e: Exception) {
            Log.e(TAG, "Share intent failed", e)
            shareAsText(context, verse, translation)
        }
    }

    private fun shareAsText(context: Context, verse: VerseEntry, translation: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildShareText(verse, translation))
        }
        context.startActivity(Intent.createChooser(intent, "Share verse"))
    }

    private fun buildShareText(verse: VerseEntry, translation: String): String =
        "॥ Chapter ${verse.chapter}, Verse ${verse.verse} ॥\n\n" +
        "$translation\n\n" +
        "— Bhagavad Gita\n\n" +
        "Shared via गीता AI"
}

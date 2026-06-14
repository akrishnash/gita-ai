package com.gitaaikrishna.app.logic

import android.app.Activity
import com.gitaaikrishna.app.BuildConfig
import com.razorpay.Checkout
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

object RazorpayManager {

    // Held across the Activity callback gap; cleared after each payment attempt.
    private var pendingTier: SubscriptionTier? = null
    private var pendingOnSuccess: ((tier: SubscriptionTier, paymentId: String) -> Unit)? = null
    private var pendingOnFailure: ((String) -> Unit)? = null

    fun startPayment(
        activity: Activity,
        tier: SubscriptionTier,
        onSuccess: (tier: SubscriptionTier, paymentId: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        pendingTier = tier
        pendingOnSuccess = onSuccess
        pendingOnFailure = onFailure

        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)

        val amountPaise = if (tier == SubscriptionTier.SADHAK) 9900 else 24900
        val tierLabel = if (tier == SubscriptionTier.SADHAK) "साधक" else "ज्ञानी"

        val options = JSONObject().apply {
            put("name", "गीता AI")
            put("description", "$tierLabel Subscription")
            put("currency", "INR")
            put("amount", amountPaise)
            // Prefill email from Firebase Auth if available
            FirebaseAuth.getInstance().currentUser?.email?.let { email ->
                put("prefill", JSONObject().put("email", email))
            }
        }

        try {
            checkout.open(activity, options)
        } catch (e: Exception) {
            cleanup()
            onFailure("Payment failed to open: ${e.message}")
        }
    }

    // Called from MainActivity.onPaymentSuccess
    fun handleSuccess(paymentId: String) {
        val tier = pendingTier ?: return
        pendingOnSuccess?.invoke(tier, paymentId)
        cleanup()
    }

    // Called from MainActivity.onPaymentError
    fun handleError(errorCode: Int, errorDescription: String) {
        pendingOnFailure?.invoke(errorDescription)
        cleanup()
    }

    private fun cleanup() {
        pendingTier = null
        pendingOnSuccess = null
        pendingOnFailure = null
    }
}

package com.gitaaikrishna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gitaaikrishna.app.logic.RazorpayManager
import com.gitaaikrishna.app.ui.AppNavigation
import com.gitaaikrishna.app.ui.theme.GitaTheme
import com.gitaaikrishna.app.viewmodel.MainViewModel
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for immersive experience
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )

            // Use user-controlled dark mode, not system default
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            GitaTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    // ── Razorpay callbacks ────────────────────────────────────────────────────

    override fun onPaymentSuccess(razorpayPaymentId: String?, data: PaymentData?) {
        RazorpayManager.handleSuccess(razorpayPaymentId ?: "")
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, data: PaymentData?) {
        RazorpayManager.handleError(errorCode, errorDescription ?: "Payment failed")
    }
}

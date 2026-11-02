package com.unknownrex.sensa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.unknownrex.sensa.core.capture.NotificationAccessChecker
import com.unknownrex.sensa.ui.SensaApp
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val notificationAccessChecker: NotificationAccessChecker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SensaApp()
        }
    }

    override fun onResume() {
        super.onResume()
        // After an update the grant survives but the listener can stay unbound until reboot.
        notificationAccessChecker.reconnectIfNeeded()
    }
}

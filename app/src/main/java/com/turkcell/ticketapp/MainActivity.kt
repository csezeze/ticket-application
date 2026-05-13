package com.turkcell.ticketapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.turkcell.core.ui.theme.TicketAppTheme
import com.turkcell.ticketapp.navigation.AppNavHost


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketAppTheme {
                AppNavHost()
            }
        }
    }
}
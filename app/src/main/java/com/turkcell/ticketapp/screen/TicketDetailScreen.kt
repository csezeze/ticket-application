package com.turkcell.ticketapp.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.MyTicket
import com.turkcell.core.util.formatEventDate
import com.turkcell.ticketapp.util.createQrBitmap
import com.turkcell.ticketapp.viewmodel.TicketDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBackClick: () -> Unit,
    viewModel: TicketDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KeepScreenBright()

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            state.isLoading -> {
                LoadingTicketDetail()
            }

            state.errorMessage != null -> {
                MessageTicketDetail(
                    message = state.errorMessage ?: "Bilet yuklenemedi.",
                    isError = true
                )
            }

            state.ticket == null -> {
                MessageTicketDetail(message = "Bilet bulunamadi.")
            }

            else -> {
                TicketDetailContent(
                    ticket = state.ticket!!,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun KeepScreenBright() {
    val activity = LocalContext.current as? Activity

    DisposableEffect(activity) {
        val window = activity?.window
        val originalBrightness = window?.attributes?.screenBrightness

        if (window != null) {
            val updatedAttributes = window.attributes
            updatedAttributes.screenBrightness = 1f
            window.attributes = updatedAttributes
        }

        onDispose {
            if (window != null && originalBrightness != null) {
                val restoredAttributes = window.attributes
                restoredAttributes.screenBrightness = originalBrightness
                window.attributes = restoredAttributes
            }
        }
    }
}

@Composable
private fun TicketDetailContent(
    ticket: MyTicket,
    onBackClick: () -> Unit
) {
    val qrBitmap = remember(ticket.qrCode) {
        createQrBitmap(content = ticket.qrCode, size = 768)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = onBackClick
        ) {
            Text(text = "Geri")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = ticket.ticketType.event.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = ticket.ticketType.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (ticket.ticketType.event.startsAt.isNotBlank()) {
            Text(
                text = formatEventDate(ticket.ticketType.event.startsAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .background(Color.White)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Bilet QR kodu",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Durum: ${ticket.status}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Kod: ${ticket.qrCode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingTicketDetail() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageTicketDetail(
    message: String,
    isError: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
    }
}

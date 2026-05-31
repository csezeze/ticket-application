package com.turkcell.ticketapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.MyTicket
import com.turkcell.ticketapp.util.createQrBitmap
import com.turkcell.ticketapp.viewmodel.MyTicketsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyTicketsScreen(
    onBackClick: () -> Unit,
    onTicketClick: (String) -> Unit,
    viewModel: MyTicketsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshTickets,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    TextButton(onClick = onBackClick) {
                        Text(text = "Geri")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (state.tickets.isEmpty()) {
                            "Biletlerim"
                        } else {
                            "Biletlerim (${state.tickets.size})"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                when {
                    state.isLoading -> {
                        item {
                            LoadingTickets()
                        }
                    }

                    state.errorMessage != null && state.tickets.isEmpty() -> {
                        item {
                            MessageTickets(
                                message = state.errorMessage ?: "Biletler yuklenemedi.",
                                isError = true
                            )
                        }
                    }

                    state.tickets.isEmpty() -> {
                        item {
                            MessageTickets(message = "Henuz biletin yok.")
                        }
                    }

                    else -> {
                        itemsIndexed(
                            items = state.tickets,
                            key = { _, ticket -> ticket.id }
                        ) { index, ticket ->
                            TicketCard(
                                ticket = ticket,
                                ticketNumber = index + 1,
                                onClick = { onTicketClick(ticket.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: MyTicket,
    ticketNumber: Int,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = ticket.ticketType.event.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bilet #$ticketNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tur: ${ticket.ticketType.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val venue = ticket.ticketType.event.venue
                if (venue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Konum: $venue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Durum: ${ticket.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kod: ${ticket.qrCode.takeLast(8)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            QrCodeImage(content = ticket.qrCode)
        }
    }
}

@Composable
private fun QrCodeImage(content: String) {
    val qrBitmap = remember(content) {
        createQrBitmap(content = content, size = 384)
    }

    Box(
        modifier = Modifier
            .size(112.dp)
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Bilet QR kodu",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LoadingTickets() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageTickets(
    message: String,
    isError: Boolean = false
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(top = 16.dp)
    )
}

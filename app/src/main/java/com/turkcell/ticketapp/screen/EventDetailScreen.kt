package com.turkcell.ticketapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.Event
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.TicketType
import com.turkcell.core.util.formatEventDate
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.EventDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EventDetailScreen(
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: EventDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isPaymentComplete) {
        if (state.isPaymentComplete) {
            onPaymentSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            state.isLoading -> {
                LoadingContent()
            }

            state.errorMessage != null -> {
                MessageContent(
                    message = state.errorMessage ?: stringResource(R.string.event_load_error),
                    isError = true
                )
            }

            state.event == null -> {
                MessageContent(message = stringResource(R.string.event_not_found))
            }

            else -> {
                EventDetailContent(
                    event = state.event!!,
                    quantities = state.quantities,
                    totalCents = state.totalCents,
                    canPurchase = state.canPurchase,
                    isCreatingPurchase = state.isCreatingPurchase,
                    purchaseErrorMessage = state.purchaseErrorMessage,
                    paymentSuccessMessage = state.paymentSuccessMessage,
                    onBackClick = onBackClick,
                    onIncrease = viewModel::increaseQuantity,
                    onDecrease = viewModel::decreaseQuantity,
                    onPurchaseClick = viewModel::createPurchase
                )
            }
        }

        if (state.showPaymentDialog && state.purchase != null) {
            PaymentConfirmationDialog(
                purchase = state.purchase!!,
                isPaying = state.isPaying,
                onConfirm = viewModel::payPurchase,
                onDismiss = viewModel::dismissPaymentDialog
            )
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    quantities: Map<String, Int>,
    totalCents: Int,
    canPurchase: Boolean,
    isCreatingPurchase: Boolean,
    purchaseErrorMessage: String?,
    paymentSuccessMessage: String?,
    onBackClick: () -> Unit,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onPurchaseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBackClick) {
            Text(text = stringResource(R.string.back))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = event.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.venue,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatEventDate(event.startsAt),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        event.description?.takeIf { it.isNotBlank() }?.let { description ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.ticket_types),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (event.ticketTypes.isEmpty()) {
            Text(
                text = stringResource(R.string.ticket_types_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            event.ticketTypes.forEach { ticketType ->
                TicketTypeRow(
                    ticketType = ticketType,
                    quantity = quantities[ticketType.id] ?: 0,
                    onIncrease = { onIncrease(ticketType.id) },
                    onDecrease = { onDecrease(ticketType.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.total_label, formatPrice(totalCents)),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        purchaseErrorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        paymentSuccessMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onPurchaseClick,
            enabled = canPurchase,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isCreatingPurchase) {
                    stringResource(R.string.purchase_loading)
                } else {
                    stringResource(R.string.purchase_button)
                }
            )
        }
    }
}

@Composable
private fun PaymentConfirmationDialog(
    purchase: Purchase,
    isPaying: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isPaying) {
                onDismiss()
            }
        },
        title = {
            Text(text = stringResource(R.string.payment_confirm_title))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.payment_total, formatPrice(purchase.totalCents)))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.payment_mock_info))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isPaying
            ) {
                Text(
                    text = if (isPaying) {
                        stringResource(R.string.paying)
                    } else {
                        stringResource(R.string.pay)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPaying
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun TicketTypeRow(
    ticketType: TicketType,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val maxQuantity = minOf(20, ticketType.remaining)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticketType.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.remaining_capacity,
                        ticketType.remaining,
                        ticketType.capacity
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPrice(ticketType.priceCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onDecrease,
                    enabled = quantity > 0
                ) {
                    Text(text = "-")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onIncrease,
                    enabled = quantity < maxQuantity
                ) {
                    Text(text = "+")
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(
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

private fun formatPrice(priceCents: Int): String {
    val lira = priceCents / 100
    val kurus = priceCents % 100
    return "TL $lira,$kurus".replace(",$kurus", ",${kurus.toString().padStart(2, '0')}")
}

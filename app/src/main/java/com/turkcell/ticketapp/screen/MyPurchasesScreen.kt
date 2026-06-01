package com.turkcell.ticketapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseStatus
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.MyPurchasesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyPurchasesScreen(
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: MyPurchasesViewModel = koinViewModel()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = onBackClick) {
                    Text(text = stringResource(R.string.back))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (state.purchases.isEmpty()) {
                        stringResource(R.string.my_purchases)
                    } else {
                        stringResource(R.string.my_purchases_with_count, state.purchases.size)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                state.isLoading -> {
                    item { LoadingPurchases() }
                }

                state.errorMessage != null && state.purchases.isEmpty() -> {
                    item {
                        MessagePurchases(
                            message = state.errorMessage ?: stringResource(R.string.purchases_load_error),
                            isError = true
                        )
                    }
                }

                state.purchases.isEmpty() -> {
                    item {
                        MessagePurchases(message = stringResource(R.string.purchases_empty))
                    }
                }

                else -> {
                    state.errorMessage?.let { message ->
                        item {
                            MessagePurchases(
                                message = message,
                                isError = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    state.paymentSuccessMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    itemsIndexed(
                        items = state.purchases,
                        key = { _, purchase -> purchase.id }
                    ) { index, purchase ->
                        PurchaseCard(
                            purchase = purchase,
                            purchaseNumber = index + 1,
                            isPaying = state.payingPurchaseId == purchase.id,
                            onContinuePayment = {
                                viewModel.continuePayment(purchase.id)
                            }
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

@Composable
private fun PurchaseCard(
    purchase: Purchase,
    purchaseNumber: Int,
    isPaying: Boolean,
    onContinuePayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.purchase_number, purchaseNumber),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatPrice(purchase.totalCents),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.purchase_status, purchase.status.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.purchase_item_count, purchase.items.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            purchase.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.purchase_created_at, createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            purchase.paidAt?.takeIf { it.isNotBlank() }?.let { paidAt ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.purchase_paid_at, paidAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (purchase.status == PurchaseStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onContinuePayment,
                    enabled = !isPaying,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPaying) {
                            stringResource(R.string.paying)
                        } else {
                            stringResource(R.string.continue_payment)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingPurchases() {
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
private fun MessagePurchases(
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

private fun formatPrice(priceCents: Int): String {
    val lira = priceCents / 100
    val kurus = priceCents % 100
    return "TL $lira,$kurus".replace(",$kurus", ",${kurus.toString().padStart(2, '0')}")
}

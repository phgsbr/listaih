package com.listaih.wear.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import com.listaih.wear.R

val PaymentOptions = listOf(
    PaymentOption("", "Sem pagamento"),
    PaymentOption("DINHEIRO", "Dinheiro"),
    PaymentOption("DEBITO", "Débito"),
    PaymentOption("CREDITO", "Crédito"),
    PaymentOption("PIX", "PIX"),
    PaymentOption("VR", "VR"),
    PaymentOption("VA", "VA")
)

data class PaymentOption(val token: String, val label: String)

@Composable
private fun paymentLabel(token: String): String {
    val res = when (token) {
        "" -> R.string.pay_no_payment
        "DINHEIRO" -> R.string.pay_cash
        "DEBITO" -> R.string.pay_debit
        "CREDITO" -> R.string.pay_credit
        "PIX" -> R.string.pay_pix
        "VR" -> R.string.pay_vr
        "VA" -> R.string.pay_va
        else -> R.string.pay_no_payment
    }
    return stringResource(res)
}

@Composable
fun WearCheckoutScreen(
    listName: String,
    checkedCount: Int,
    estimatedTotal: Double,
    initialPayment: String,
    onConfirm: (String, String, Double) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    var paymentMethod by remember { mutableStateOf(initialPayment) }
    var totalAmount by remember { mutableStateOf(String.format("%.2f", estimatedTotal)) }
    val total = totalAmount.toDoubleOrNull() ?: 0.0

    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.wear_checkout_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.wear_checkout_summary, checkedCount, estimatedTotal),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.wear_checkout_payment_method),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        PaymentOptions.forEach { option ->
            item {
                PaymentRow(
                    option = option,
                    selected = paymentMethod == option.token,
                    onClick = { paymentMethod = option.token }
                )
            }
        }

        item {
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { new ->
                    totalAmount = new.filter { c -> c.isDigit() || c == '.' }
                },
                label = { Text(stringResource(R.string.wear_checkout_total_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        item {
            Button(
                onClick = { onConfirm(paymentMethod, totalAmount, total) },
                enabled = total > 0,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = stringResource(R.string.wear_checkout_confirm), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        item {
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text(text = stringResource(R.string.btn_back), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
        PositionIndicator(scalingLazyListState = listState)
    }
}

@Composable
fun PaymentRow(option: PaymentOption, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Text(
            text = paymentLabel(option.token),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
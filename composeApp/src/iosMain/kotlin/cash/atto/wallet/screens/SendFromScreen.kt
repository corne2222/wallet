package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_background
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.uistate.send.SendUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendFromScreen(
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit,
    viewModel: SendTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppBar(onBackNavigation = onBackNavigation)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Send Atto",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = recipientAddress,
                    onValueChange = { recipientAddress = it },
                    label = { Text("Recipient Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                AttoTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AttoButton(
                    onClick = {
                        viewModel.sendTransaction(recipientAddress, amount)
                        onSendClicked()
                    },
                    text = "Send",
                    enabled = recipientAddress.isNotBlank() && amount.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import cash.atto.wallet.components.common.AttoReceiveButton
import cash.atto.wallet.components.common.AttoSendButton
import cash.atto.wallet.components.overview.OverviewHeader
import cash.atto.wallet.components.overview.ReceiveAttoBottomSheet
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onSettingsClicked: () -> Unit,
    onSendClicked: () -> Unit,
    viewModel: OverviewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = Res.drawable.atto_overview_background,
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            OverviewHeader(
                balance = uiState.balance,
                balanceChange = uiState.balanceChange,
                onSettingsClicked = onSettingsClicked
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AttoReceiveButton(
                    modifier = Modifier.weight(1f),
                    onClick = { /* Handle receive */ }
                )
                AttoSendButton(
                    modifier = Modifier.weight(1f),
                    onClick = onSendClicked
                )
            }

            TransactionsList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transactions = uiState.transactions,
                onTransactionClick = { /* Handle transaction click */ }
            )
        }
    }
}
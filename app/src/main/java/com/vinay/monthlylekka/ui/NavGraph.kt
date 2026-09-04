package com.vinay.monthlylekka.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HelpNavDestination(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    HelpScreen(
        onBack = onBack,
        modifier = modifier
    )
}

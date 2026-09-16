package com.vinay.monthlylekka.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme

/**
 * Google AdMob Banner Ad View Component - 100% Ad-Free Offline Mode.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") adUnitId: String = "ca-app-pub-4120760179761356/2871652402"
) {
    Spacer(modifier = modifier.height(0.dp))
}

@Preview(showBackground = true)
@Composable
fun BannerAdViewPreview() {
    MonthlyLekkaTheme {
        BannerAdView()
    }
}

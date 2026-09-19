package com.vinay.monthlylekka.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme

/**
 * Banner Ad View Component configured cleanly for Amazon Appstore release distribution.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") adUnitId: String = "ca-app-pub-4120760179761356/2871652402"
) {
    AmazonBannerAdView(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun BannerAdViewPreview() {
    MonthlyLekkaTheme {
        BannerAdView()
    }
}

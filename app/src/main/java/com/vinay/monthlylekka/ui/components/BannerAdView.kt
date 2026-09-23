package com.vinay.monthlylekka.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme

/**
 * Banner Ad View Component active using Google Mobile Ads SDK (AdMob).
 *
 * @param modifier Optional [Modifier] for customizing layout.
 * @param adUnitId Production or test AdMob Banner Ad Unit ID.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-4120760179761356/2871652402",
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun BannerAdViewPreview() {
    MonthlyLekkaTheme {
        BannerAdView()
    }
}

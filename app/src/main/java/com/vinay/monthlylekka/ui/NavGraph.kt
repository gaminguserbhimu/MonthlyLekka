package com.vinay.monthlylekka.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SplashNavDestination(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    SplashScreen(
        onSplashFinished = onSplashFinished,
        modifier = modifier
    )
}

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

@Composable
fun OnboardingNavDestination(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingScreen(
        onComplete = onComplete,
        modifier = modifier
    )
}

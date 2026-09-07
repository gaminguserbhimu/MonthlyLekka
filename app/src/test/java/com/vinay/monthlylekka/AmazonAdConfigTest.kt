package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.AmazonAdConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmazonAdConfigTest {

    @Test
    fun testAmazonAdConfigValues() {
        assertEquals("sample-amazon-app-key-12345", AmazonAdConfig.amazonAppKey)
        assertTrue(AmazonAdConfig.isTestMode)
    }
}

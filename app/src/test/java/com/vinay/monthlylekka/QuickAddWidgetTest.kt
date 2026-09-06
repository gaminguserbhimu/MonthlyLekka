package com.vinay.monthlylekka

import com.vinay.monthlylekka.widget.QuickAddWidgetReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QuickAddWidgetTest {

    @Test
    fun testWidgetConstants() {
        assertEquals("START_DESTINATION", QuickAddWidgetReceiver.EXTRA_START_DESTINATION)
        assertEquals("add_expense", QuickAddWidgetReceiver.DESTINATION_ADD_EXPENSE)
    }

    @Test
    fun testQuickAddWidgetReceiver_instantiation() {
        val receiver = QuickAddWidgetReceiver()
        assertNotNull(receiver)
    }
}

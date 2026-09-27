package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CENTSIBLE", appName)
  }

  @Test
  fun `test upi sms parser extracts amount and merchant`() {
    val sms = "Dear SBI User, A/C 4892 debited by Rs 450.00 on 24Sep26 transfer to Swiggy UPI/627192837482. Bal: Rs 45,210"
    val result = com.example.data.upi.UpiSmsParser.parse(sms)
    org.junit.Assert.assertNotNull(result)
    assertEquals(450.0, result!!.amount, 0.01)
    assertEquals("INR", result.currency)
    assertEquals("EXPENSE", result.type)
    assertEquals("Food & Dining", com.example.data.upi.UpiSmsParser.inferCategory(result.merchant, result.rawText))
  }

  @Test
  fun `test currency conversion rates`() {
    val usdAmount = 100.0
    val inrAmount = com.example.data.model.CurrencyManager.convert(usdAmount, "USD", "INR")
    org.junit.Assert.assertTrue(inrAmount > 8000.0)
  }
}

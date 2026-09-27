package com.example.data.upi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        var onUpiSmsReceived: ((String) -> Unit)? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.displayMessageBody ?: continue
                if (UpiSmsParser.isFinancialOrUpiMessage(body)) {
                    Log.d("CentsibleSms", "Financial SMS detected: $body")
                    onUpiSmsReceived?.invoke(body)
                }
            }
        }
    }
}

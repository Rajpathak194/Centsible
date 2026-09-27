package com.example.data.upi

import com.example.data.model.UpiParseResult
import java.util.regex.Pattern

object UpiSmsParser {

    private val amountRegex = Pattern.compile(
        "(?:(?:Rs\\.?|INR|₹|USD|EUR|GBP)\\s*([0-9,]+(?:\\.[0-9]{1,2})?))|(?:([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:Rs\\.?|INR|₹))",
        Pattern.CASE_INSENSITIVE
    )

    private val refRegex = Pattern.compile(
        "(?:UPI[/:\\s-]*([0-9a-zA-Z]{8,16}))|(?:txn[/:\\s-]*([0-9a-zA-Z]{8,16}))|(?:Ref\\.?\\s*(?:no\\.?)?\\s*([0-9a-zA-Z]{8,16}))",
        Pattern.CASE_INSENSITIVE
    )

    private val payeeRegex = Pattern.compile(
        "(?:(?:paid|transfer|sent|to|vpa|at)\\s+([a-zA-Z0-9@._\\s]{3,24}?)(?:\\s+via|\\s+on|\\s+Ref|\\s+UPI|\\s+txn|\\.|,))",
        Pattern.CASE_INSENSITIVE
    )

    fun isFinancialOrUpiMessage(body: String): Boolean {
        val lower = body.lowercase()
        return (lower.contains("upi") || lower.contains("debited") || lower.contains("credited") ||
                lower.contains("paid") || lower.contains("sent rs") || lower.contains("a/c") ||
                lower.contains("txn") || lower.contains("spent") || lower.contains("payment")) &&
                (lower.contains("rs") || lower.contains("inr") || lower.contains("₹") || lower.contains("$") || lower.contains("eur"))
    }

    fun parse(smsBody: String): UpiParseResult? {
        val lower = smsBody.lowercase()
        val isIncome = lower.contains("credited") || lower.contains("received") || lower.contains("refund")
        val isExpense = lower.contains("debited") || lower.contains("paid") || lower.contains("sent") ||
                lower.contains("spent") || (!isIncome && lower.contains("to "))

        var amount = 0.0
        var currency = "INR"
        if (smsBody.contains("$") || lower.contains("usd")) currency = "USD"
        else if (smsBody.contains("€") || lower.contains("eur")) currency = "EUR"
        else if (smsBody.contains("£") || lower.contains("gbp")) currency = "GBP"

        val matcher = amountRegex.matcher(smsBody)
        if (matcher.find()) {
            val rawAmt = matcher.group(1) ?: matcher.group(2)
            if (rawAmt != null) {
                val clean = rawAmt.replace(",", "")
                amount = clean.toDoubleOrNull() ?: 0.0
            }
        }

        if (amount <= 0.0) {
            // Fallback general decimal search
            val fallbackMatcher = Pattern.compile("([0-9]+\\.[0-9]{2})").matcher(smsBody)
            if (fallbackMatcher.find()) {
                amount = fallbackMatcher.group(1)?.toDoubleOrNull() ?: 0.0
            }
        }

        if (amount <= 0.0) return null

        var upiRef = ""
        val refMatcher = refRegex.matcher(smsBody)
        if (refMatcher.find()) {
            upiRef = refMatcher.group(1) ?: refMatcher.group(2) ?: refMatcher.group(3) ?: ""
        }
        if (upiRef.isEmpty()) {
            upiRef = "UPI/" + System.currentTimeMillis().toString().takeLast(8)
        }

        var merchant = ""
        val payeeMatcher = payeeRegex.matcher(smsBody)
        if (payeeMatcher.find()) {
            val candidate = payeeMatcher.group(1)?.trim() ?: ""
            if (candidate.isNotEmpty() && !candidate.equals("a/c", ignoreCase = true) && !candidate.equals("your", ignoreCase = true)) {
                merchant = candidate
            }
        }

        if (merchant.isEmpty()) {
            // Heuristic known merchants
            val known = listOf("Swiggy", "Zomato", "Uber", "Ola", "Amazon", "Flipkart", "Shell", "Starbucks", "Netflix", "Blinkit", "Zepto", "Apollo")
            for (k in known) {
                if (lower.contains(k.lowercase())) {
                    merchant = k
                    break
                }
            }
        }

        if (merchant.isEmpty()) {
            merchant = if (isIncome) "Direct Transfer" else "UPI Merchant"
        }

        return UpiParseResult(
            amount = amount,
            currency = currency,
            merchant = merchant.replaceFirstChar { it.uppercase() },
            upiRef = upiRef,
            type = if (isIncome) "INCOME" else "EXPENSE",
            rawText = smsBody
        )
    }

    fun inferCategory(merchant: String, title: String): String {
        val combined = "$merchant $title".lowercase()
        return when {
            combined.contains("swiggy") || combined.contains("zomato") || combined.contains("dining") ||
                    combined.contains("cafe") || combined.contains("restaurant") || combined.contains("food") ||
                    combined.contains("starbucks") || combined.contains("mcdonald") -> "Food & Dining"

            combined.contains("blinkit") || combined.contains("zepto") || combined.contains("grocery") ||
                    combined.contains("market") || combined.contains("supermarket") || combined.contains("mart") ||
                    combined.contains("whole foods") -> "Groceries"

            combined.contains("uber") || combined.contains("ola") || combined.contains("shell") ||
                    combined.contains("fuel") || combined.contains("petrol") || combined.contains("gas") ||
                    combined.contains("metro") || combined.contains("transit") || combined.contains("airline") -> "Transport"

            combined.contains("amazon") || combined.contains("flipkart") || combined.contains("myntra") ||
                    combined.contains("apple") || combined.contains("shopping") || combined.contains("store") -> "Shopping"

            combined.contains("netflix") || combined.contains("spotify") || combined.contains("prime") ||
                    combined.contains("cinema") || combined.contains("movie") || combined.contains("hotstar") -> "Entertainment"

            combined.contains("electricity") || combined.contains("water") || combined.contains("power") ||
                    combined.contains("airtel") || combined.contains("jio") || combined.contains("bill") ||
                    combined.contains("recharge") || combined.contains("broadband") -> "Bills & Utilities"

            combined.contains("pharmacy") || combined.contains("hospital") || combined.contains("doctor") ||
                    combined.contains("apollo") || combined.contains("health") || combined.contains("gym") -> "Health & Wellness"

            combined.contains("zerodha") || combined.contains("groww") || combined.contains("vanguard") ||
                    combined.contains("invest") || combined.contains("etf") || combined.contains("mutual fund") -> "Investments"

            combined.contains("salary") || combined.contains("payroll") || combined.contains("bonus") -> "Salary"

            else -> "Other"
        }
    }
}

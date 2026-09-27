package com.example.data.model

data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToUsd: Double // 1 USD = X Currency
)

object CurrencyManager {
    val supportedCurrencies = listOf(
        CurrencyRate("USD", "US Dollar", "$", 1.0),
        CurrencyRate("EUR", "Euro", "€", 0.92),
        CurrencyRate("GBP", "British Pound", "£", 0.78),
        CurrencyRate("INR", "Indian Rupee", "₹", 86.50),
        CurrencyRate("JPY", "Japanese Yen", "¥", 152.00),
        CurrencyRate("CAD", "Canadian Dollar", "C$", 1.38),
        CurrencyRate("AUD", "Australian Dollar", "A$", 1.52),
        CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34)
    )

    fun getSymbol(code: String): String {
        return supportedCurrencies.find { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        val fromRate = supportedCurrencies.find { it.code.equals(fromCurrency, ignoreCase = true) }?.rateToUsd ?: 1.0
        val toRate = supportedCurrencies.find { it.code.equals(toCurrency, ignoreCase = true) }?.rateToUsd ?: 1.0
        // Convert to USD first, then to target
        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }

    fun format(amount: Double, currencyCode: String): String {
        val sym = getSymbol(currencyCode)
        return String.format("%s%,.2f", sym, amount)
    }
}

data class MarketTickerItem(
    val symbol: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val currency: String = "USD",
    val type: String // INDEX, CRYPTO, COMMODITY, FOREX
)

data class UpiParseResult(
    val amount: Double,
    val currency: String,
    val merchant: String,
    val upiRef: String,
    val type: String, // EXPENSE or INCOME
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReceiptOcrResult(
    val merchant: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val category: String,
    val taxAmount: Double = 0.0,
    val lineItems: List<String> = emptyList(),
    val confidence: Float = 0.95f
)

data class InvestmentRecommendation(
    val title: String,
    val assetClass: String,
    val riskLevel: String, // Conservative, Moderate, Aggressive
    val expectedReturnAnnual: Double,
    val rationalDescription: String,
    val tickerSymbol: String
)

data class UserProfile(
    val email: String = "alex.morgan@finmail.com",
    val name: String = "Alex Morgan",
    val preferredCurrency: String = "USD",
    val isBiometricEnabled: Boolean = true,
    val pinHash: String = "1234",
    val isCloudSyncEnabled: Boolean = true,
    val lastCloudSyncMillis: Long = System.currentTimeMillis() - 15 * 60 * 1000,
    val notificationBudgetAlerts: Boolean = true,
    val notificationDailySummary: Boolean = true,
    val notificationUpiDetection: Boolean = true,
    val notificationMarketAlerts: Boolean = false,
    val isAuthenticated: Boolean = true
)

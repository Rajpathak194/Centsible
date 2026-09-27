package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiFinancialService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.CurrencyManager
import com.example.data.model.MarketTickerItem
import com.example.data.model.ReceiptOcrResult
import com.example.data.model.UserProfile
import com.example.data.repository.FinanceRepository
import com.example.data.upi.SmsReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialSummary(
    val totalNetWorth: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val monthlySavingsRate: Double,
    val preferredCurrency: String,
    val budgetAlerts: List<String>
)

data class CategorySpend(
    val category: String,
    val totalSpent: Double,
    val percentageOfTotal: Float,
    val budgetLimit: Double? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val geminiService = GeminiFinancialService()
    val repository = FinanceRepository(database.financeDao(), geminiService)

    val accounts: StateFlow<List<AccountEntity>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.budgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val isAppLocked: StateFlow<Boolean> = repository.isAppLocked
    val marketTickers: StateFlow<List<MarketTickerItem>> = repository.marketTickers
    val syncStatusMessage: StateFlow<String> = repository.syncStatusMessage

    // Active AI processing states
    private val _isAdvisorThinking = MutableStateFlow(false)
    val isAdvisorThinking: StateFlow<Boolean> = _isAdvisorThinking.asStateFlow()

    private val _isOcrScanning = MutableStateFlow(false)
    val isOcrScanning: StateFlow<Boolean> = _isOcrScanning.asStateFlow()

    private val _lastOcrResult = MutableStateFlow<ReceiptOcrResult?>(null)
    val lastOcrResult: StateFlow<ReceiptOcrResult?> = _lastOcrResult.asStateFlow()

    private val _upiDetectionNotification = MutableStateFlow<String?>(null)
    val upiDetectionNotification: StateFlow<String?> = _upiDetectionNotification.asStateFlow()

    // Transaction filters
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")
    val selectedAccountFilter = MutableStateFlow<Long?>(null)

    // Filtered transactions
    val filteredTransactions = combine(
        transactions,
        searchQuery,
        selectedCategoryFilter,
        selectedAccountFilter
    ) { txList, query, cat, accId ->
        txList.filter { tx ->
            val matchesQuery = query.isBlank() || tx.title.contains(query, ignoreCase = true) ||
                    tx.merchant.contains(query, ignoreCase = true) || tx.category.contains(query, ignoreCase = true)
            val matchesCategory = cat == "All" || tx.category.equals(cat, ignoreCase = true)
            val matchesAccount = accId == null || tx.accountId == accId
            matchesQuery && matchesCategory && matchesAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Overview calculation
    val financialSummary: StateFlow<FinancialSummary> = combine(
        accounts,
        transactions,
        budgets,
        userProfile
    ) { accList, txList, budgetList, profile ->
        val prefCurr = profile.preferredCurrency

        // Net worth in preferred currency
        val netWorth = accList.sumOf { acc ->
            CurrencyManager.convert(acc.balance, acc.currency, prefCurr)
        }

        // Monthly income and expense (all transactions)
        var totalInc = 0.0
        var totalExp = 0.0
        val categoryExpenseMap = mutableMapOf<String, Double>()

        for (tx in txList) {
            val convertedAmt = CurrencyManager.convert(tx.amount, tx.currency, prefCurr)
            if (tx.type == "INCOME") {
                totalInc += convertedAmt
            } else if (tx.type == "EXPENSE") {
                totalExp += convertedAmt
                val existing = categoryExpenseMap[tx.category] ?: 0.0
                categoryExpenseMap[tx.category] = existing + convertedAmt
            }
        }

        val savingsRate = if (totalInc > 0) {
            ((totalInc - totalExp) / totalInc * 100).coerceIn(0.0, 100.0)
        } else 0.0

        // Check budget thresholds for alerts
        val alerts = mutableListOf<String>()
        for (b in budgetList) {
            val spent = categoryExpenseMap[b.category] ?: 0.0
            val limit = CurrencyManager.convert(b.monthlyLimit, b.currency, prefCurr)
            val percentUsed = if (limit > 0) (spent / limit) * 100 else 0.0
            if (percentUsed >= 100.0) {
                alerts.add("🚨 Budget exceeded: ${b.category} (${String.format("%.0f", percentUsed)}% used)")
            } else if (percentUsed >= b.alertThresholdPercent) {
                alerts.add("⚠️ Budget alert: ${b.category} (${String.format("%.0f", percentUsed)}% used)")
            }
        }

        FinancialSummary(
            totalNetWorth = netWorth,
            monthlyIncome = totalInc,
            monthlyExpense = totalExp,
            monthlySavingsRate = savingsRate,
            preferredCurrency = prefCurr,
            budgetAlerts = alerts
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialSummary(0.0, 0.0, 0.0, 0.0, "USD", emptyList())
    )

    // Category breakdown
    val categoryBreakdown: StateFlow<List<CategorySpend>> = combine(
        transactions,
        budgets,
        userProfile
    ) { txList, budgetList, profile ->
        val prefCurr = profile.preferredCurrency
        val spendMap = mutableMapOf<String, Double>()
        var totalSpend = 0.0

        for (tx in txList) {
            if (tx.type == "EXPENSE") {
                val amt = CurrencyManager.convert(tx.amount, tx.currency, prefCurr)
                totalSpend += amt
                spendMap[tx.category] = (spendMap[tx.category] ?: 0.0) + amt
            }
        }

        val budgetMap = budgetList.associate {
            it.category to CurrencyManager.convert(it.monthlyLimit, it.currency, prefCurr)
        }

        spendMap.entries.map { (cat, spent) ->
            CategorySpend(
                category = cat,
                totalSpent = spent,
                percentageOfTotal = if (totalSpend > 0) (spent / totalSpend).toFloat() else 0f,
                budgetLimit = budgetMap[cat]
            )
        }.sortedByDescending { it.totalSpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Register listener for SMS receiver
        SmsReceiver.onUpiSmsReceived = { smsBody ->
            processUpiSms(smsBody)
        }
    }

    fun processUpiSms(smsBody: String) {
        viewModelScope.launch {
            val created = repository.processIncomingUpiSms(smsBody)
            if (created != null) {
                _upiDetectionNotification.value = "Auto-tracked: ${created.title} (${CurrencyManager.format(created.amount, created.currency)})"
            }
        }
    }

    fun dismissUpiNotification() {
        _upiDetectionNotification.value = null
    }

    // Add / Edit Transaction
    fun saveTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: Long,
        currency: String,
        notes: String = "",
        isUpi: Boolean = false,
        upiRef: String = "",
        merchant: String = ""
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title.ifBlank { "Transaction" },
                amount = amount,
                type = type,
                category = category,
                dateMillis = System.currentTimeMillis(),
                accountId = accountId,
                currency = currency,
                notes = notes,
                isUpi = isUpi,
                upiRef = upiRef,
                merchant = merchant.ifBlank { title }
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Savings Goals
    fun addSavingsGoal(title: String, targetAmount: Double, category: String, currency: String = "USD") {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                category = category,
                currency = currency
            )
            repository.addSavingsGoal(goal)
        }
    }

    fun depositToSavingsGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            repository.depositToGoal(goal, amount)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Budgets
    fun saveBudget(category: String, limit: Double, currency: String = "USD", alertThreshold: Int = 80) {
        viewModelScope.launch {
            val existing = budgets.value.find { it.category.equals(category, ignoreCase = true) }
            if (existing != null) {
                repository.updateBudget(existing.copy(monthlyLimit = limit, currency = currency, alertThresholdPercent = alertThreshold))
            } else {
                repository.addBudget(BudgetEntity(category = category, monthlyLimit = limit, currency = currency, alertThresholdPercent = alertThreshold))
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Bank Account creation & sync
    fun addBankAccount(name: String, institution: String, currency: String, balance: Double, accountType: String) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name,
                institution = institution,
                accountNumberMasked = "•••• " + (1000..9999).random(),
                currency = currency,
                balance = balance,
                accountType = accountType
            )
            repository.addAccount(account)
        }
    }

    fun syncAllBanks() {
        viewModelScope.launch {
            repository.syncAllInternationalBanks()
        }
    }

    // Chat Advisor
    fun sendAdvisorMessage(prompt: String) {
        if (prompt.isBlank() || _isAdvisorThinking.value) return
        viewModelScope.launch {
            _isAdvisorThinking.value = true
            try {
                val summary = financialSummary.value
                val context = "Net Worth: ${CurrencyManager.format(summary.totalNetWorth, summary.preferredCurrency)}, Monthly Expenses: ${CurrencyManager.format(summary.monthlyExpense, summary.preferredCurrency)}, Savings Rate: ${String.format("%.1f", summary.monthlySavingsRate)}%."
                repository.sendChatMessage(prompt, context)
            } finally {
                _isAdvisorThinking.value = false
            }
        }
    }

    fun clearAdvisorChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // OCR Receipt Scanning
    fun scanReceipt(base64: String?, sampleSnippet: String?) {
        viewModelScope.launch {
            _isOcrScanning.value = true
            try {
                val result = repository.scanReceipt(base64, sampleSnippet)
                _lastOcrResult.value = result
            } finally {
                _isOcrScanning.value = false
            }
        }
    }

    fun clearOcrResult() {
        _lastOcrResult.value = null
    }

    // Security & User Profile
    fun setPreferredCurrency(curr: String) {
        val updated = userProfile.value.copy(preferredCurrency = curr)
        repository.updateProfile(updated)
    }

    fun toggleBiometric(enabled: Boolean) {
        val updated = userProfile.value.copy(isBiometricEnabled = enabled)
        repository.updateProfile(updated)
    }

    fun toggleNotification(type: String, enabled: Boolean) {
        val current = userProfile.value
        val updated = when (type) {
            "budget" -> current.copy(notificationBudgetAlerts = enabled)
            "daily" -> current.copy(notificationDailySummary = enabled)
            "upi" -> current.copy(notificationUpiDetection = enabled)
            "market" -> current.copy(notificationMarketAlerts = enabled)
            else -> current
        }
        repository.updateProfile(updated)
    }

    fun unlockApp(pin: String): Boolean {
        return repository.unlockWithPin(pin)
    }

    fun lockApp() {
        repository.setAppLocked(true)
    }

    fun logout() {
        val updated = userProfile.value.copy(isAuthenticated = false)
        repository.updateProfile(updated)
    }

    fun login(email: String) {
        val updated = userProfile.value.copy(email = email, isAuthenticated = true)
        repository.updateProfile(updated)
        repository.setAppLocked(false)
    }
}

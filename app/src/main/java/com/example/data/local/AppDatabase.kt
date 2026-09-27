package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FinanceDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finaura_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.financeDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: FinanceDao) {
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L

            // 1. Multi-currency international accounts
            val accChase = AccountEntity(
                id = 1,
                name = "Chase Premier Checking",
                institution = "JPMorgan Chase US",
                accountNumberMasked = "•••• 4892",
                currency = "USD",
                balance = 12450.80,
                accountType = "CHECKING",
                colorHex = 0xFF1E40AF
            )
            val accBarclays = AccountEntity(
                id = 2,
                name = "Barclays Direct Savings",
                institution = "Barclays UK",
                accountNumberMasked = "•••• 7310",
                currency = "GBP",
                balance = 8320.50,
                accountType = "SAVINGS",
                colorHex = 0xFF0284C7
            )
            val accHdfc = AccountEntity(
                id = 3,
                name = "HDFC Salary & UPI Account",
                institution = "HDFC Bank India",
                accountNumberMasked = "•••• 9144",
                currency = "INR",
                balance = 348200.00,
                accountType = "CHECKING",
                colorHex = 0xFF0D9488
            )
            val accRevolut = AccountEntity(
                id = 4,
                name = "Revolut Multi-Currency Vault",
                institution = "Revolut EU",
                accountNumberMasked = "•••• 5521",
                currency = "EUR",
                balance = 4150.00,
                accountType = "WALLET",
                colorHex = 0xFF7C3AED
            )
            val accVanguard = AccountEntity(
                id = 5,
                name = "Vanguard Growth ETF Portfolio",
                institution = "Vanguard Investments",
                accountNumberMasked = "•••• 8092",
                currency = "USD",
                balance = 28940.00,
                accountType = "INVESTMENT",
                colorHex = 0xFF059669
            )

            dao.insertAccounts(listOf(accChase, accBarclays, accHdfc, accRevolut, accVanguard))

            // 2. Realistic initial transactions with diverse categories & UPI support
            val initialTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Salary Deposit",
                    amount = 6200.0,
                    type = "INCOME",
                    category = "Salary",
                    dateMillis = now - 2 * day,
                    accountId = 1,
                    currency = "USD",
                    notes = "Direct deposit from Tech Corp",
                    merchant = "Tech Corp Inc."
                ),
                TransactionEntity(
                    title = "Whole Foods Organic Market",
                    amount = 142.30,
                    type = "EXPENSE",
                    category = "Groceries",
                    dateMillis = now - 1 * day,
                    accountId = 1,
                    currency = "USD",
                    notes = "Weekly groceries & fresh produce",
                    merchant = "Whole Foods"
                ),
                TransactionEntity(
                    title = "Swiggy Gourmet Delivery",
                    amount = 840.0,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    dateMillis = now - 3 * day,
                    accountId = 3,
                    currency = "INR",
                    isUpi = true,
                    upiRef = "UPI/627192837482",
                    merchant = "Swiggy",
                    notes = "Dinner with friends"
                ),
                TransactionEntity(
                    title = "Shell Gas & EV Charging",
                    amount = 64.50,
                    type = "EXPENSE",
                    category = "Transport",
                    dateMillis = now - 4 * day,
                    accountId = 1,
                    currency = "USD",
                    merchant = "Shell"
                ),
                TransactionEntity(
                    title = "Netflix & Spotify Premium",
                    amount = 26.98,
                    type = "EXPENSE",
                    category = "Entertainment",
                    dateMillis = now - 5 * day,
                    accountId = 1,
                    currency = "USD",
                    merchant = "Streaming Services"
                ),
                TransactionEntity(
                    title = "Coffee at Blue Bottle",
                    amount = 8.50,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    dateMillis = now - 6 * day,
                    accountId = 1,
                    currency = "USD",
                    merchant = "Blue Bottle Coffee"
                ),
                TransactionEntity(
                    title = "Apple Store Hardware Care",
                    amount = 199.00,
                    type = "EXPENSE",
                    category = "Shopping",
                    dateMillis = now - 8 * day,
                    accountId = 1,
                    currency = "USD",
                    merchant = "Apple Inc."
                ),
                TransactionEntity(
                    title = "Electric & Water Utilities",
                    amount = 135.40,
                    type = "EXPENSE",
                    category = "Bills & Utilities",
                    dateMillis = now - 10 * day,
                    accountId = 1,
                    currency = "USD",
                    merchant = "City Power Grid"
                ),
                TransactionEntity(
                    title = "Amazon Electronics Order",
                    amount = 2350.0,
                    type = "EXPENSE",
                    category = "Shopping",
                    dateMillis = now - 11 * day,
                    accountId = 3,
                    currency = "INR",
                    isUpi = true,
                    upiRef = "UPI/719283019283",
                    merchant = "Amazon India"
                ),
                TransactionEntity(
                    title = "S&P 500 Index Auto-Invest",
                    amount = 500.0,
                    type = "EXPENSE",
                    category = "Investments",
                    dateMillis = now - 12 * day,
                    accountId = 5,
                    currency = "USD",
                    merchant = "Vanguard"
                )
            )
            dao.insertTransactions(initialTransactions)

            // 3. Budgets
            val initialBudgets = listOf(
                BudgetEntity(category = "Food & Dining", monthlyLimit = 500.0, currency = "USD", alertThresholdPercent = 80),
                BudgetEntity(category = "Groceries", monthlyLimit = 600.0, currency = "USD", alertThresholdPercent = 85),
                BudgetEntity(category = "Shopping", monthlyLimit = 400.0, currency = "USD", alertThresholdPercent = 80),
                BudgetEntity(category = "Transport", monthlyLimit = 250.0, currency = "USD", alertThresholdPercent = 75),
                BudgetEntity(category = "Bills & Utilities", monthlyLimit = 350.0, currency = "USD", alertThresholdPercent = 90),
                BudgetEntity(category = "Entertainment", monthlyLimit = 150.0, currency = "USD", alertThresholdPercent = 80)
            )
            dao.insertBudgets(initialBudgets)

            // 4. Savings Goals
            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "6-Month Emergency Fund",
                    targetAmount = 15000.0,
                    currentAmount = 9800.0,
                    currency = "USD",
                    category = "Emergency Fund",
                    colorHex = 0xFF10B981,
                    targetDateMillis = now + 120 * day
                ),
                SavingsGoalEntity(
                    title = "European Summer Journey",
                    targetAmount = 4500.0,
                    currentAmount = 2850.0,
                    currency = "EUR",
                    category = "Vacation",
                    colorHex = 0xFF3B82F6,
                    targetDateMillis = now + 200 * day
                ),
                SavingsGoalEntity(
                    title = "Electric Vehicle Down Payment",
                    targetAmount = 12000.0,
                    currentAmount = 4600.0,
                    currency = "USD",
                    category = "Vehicle",
                    colorHex = 0xFFF59E0B,
                    targetDateMillis = now + 360 * day
                )
            )
            dao.insertSavingsGoals(initialGoals)

            // 5. Initial Chat Advisor Welcome Message
            val welcomeMsg = ChatMessageEntity(
                sender = "assistant",
                text = "Hello! I am Aura, your personal AI financial advisor. I analyze your spending habits across all accounts, track budget thresholds, parse UPI messages, and provide real-time investment strategies. How can I help optimize your wealth today?",
                suggestionChips = "Analyze my spending,How to cut monthly expenses,Smart investment advice,Review my savings goals",
                timestamp = now
            )
            dao.insertChatMessage(welcomeMsg)
        }
    }
}

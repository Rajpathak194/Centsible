package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val institution: String,
    val accountNumberMasked: String,
    val currency: String, // USD, EUR, GBP, INR, JPY, CAD, AUD
    val balance: Double,
    val accountType: String, // CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT, WALLET
    val colorHex: Long = 0xFF10B981,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val category: String, // Food & Dining, Groceries, Shopping, Transport, Bills & Utilities, Entertainment, Health & Wellness, Investments, Salary, Freelance, Other
    val dateMillis: Long = System.currentTimeMillis(),
    val accountId: Long,
    val currency: String,
    val notes: String = "",
    val isUpi: Boolean = false,
    val upiRef: String = "",
    val merchant: String = "",
    val receiptUri: String? = null,
    val tags: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val currency: String = "USD",
    val periodMonth: String = "2026-09",
    val alertThresholdPercent: Int = 80 // alert at 80%
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String = "USD",
    val targetDateMillis: Long = System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000, // 6 months default
    val category: String = "Savings", // Emergency Fund, Vacation, House, Car, Retirement
    val colorHex: Long = 0xFF3B82F6
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestionChips: String = "" // comma-separated suggestions
)

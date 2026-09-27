package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.ReceiptLong)
    object BudgetsGoals : Screen("budgets_goals", "Budgets & Goals", Icons.Default.PieChart)
    object AdvisorChat : Screen("advisor_chat", "Aura AI", Icons.Default.Chat)
    object ReceiptScanner : Screen("receipt_scanner", "OCR Scanner", Icons.Default.DocumentScanner)
    object AccountsSync : Screen("accounts_sync", "Accounts", Icons.Default.AccountBalance)
    object Reports : Screen("reports", "Reports", Icons.Default.AutoGraph)
    object Settings : Screen("settings", "Security", Icons.Default.Security)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.BudgetsGoals,
    Screen.AdvisorChat,
    Screen.Reports
)

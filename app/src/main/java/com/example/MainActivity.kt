package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.AccountsSyncScreen
import com.example.ui.screens.AdvisorChatScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetsGoalsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReceiptScannerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsSecurityScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.CentsibleTheme
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireLight

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CentsibleTheme(darkTheme = true) {
                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

                if (!userProfile.isAuthenticated) {
                    AuthScreen(
                        onLoginSuccess = { email ->
                            viewModel.login(email)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "CENTSIBLE",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = EmeraldPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(EmeraldLight)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "VAULT",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldLight,
                                                        letterSpacing = 1.sp
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                navController.navigate(Screen.ReceiptScanner.route) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            modifier = Modifier.testTag("topbar_scanner_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DocumentScanner,
                                                contentDescription = "Scan Receipt",
                                                tint = SapphireLight
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                navController.navigate(Screen.AccountsSync.route) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            modifier = Modifier.testTag("topbar_accounts_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = "Accounts",
                                                tint = SapphireLight
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                navController.navigate(Screen.Settings.route) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            modifier = Modifier.testTag("topbar_settings_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    bottomNavItems.forEach { screen ->
                                        val isSelected = currentRoute == screen.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                selectedTextColor = EmeraldLight,
                                                indicatorColor = EmeraldPrimary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag("nav_item_${screen.route}")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Dashboard.route) {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                                        onNavigateToScanReceipt = { navController.navigate(Screen.ReceiptScanner.route) },
                                        onNavigateToAdvisor = { navController.navigate(Screen.AdvisorChat.route) },
                                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                                        onNavigateToBudgets = { navController.navigate(Screen.BudgetsGoals.route) }
                                    )
                                }

                                composable(Screen.Transactions.route) {
                                    BackHandler { navController.popBackStack() }
                                    TransactionsScreen(viewModel = viewModel)
                                }

                                composable(Screen.BudgetsGoals.route) {
                                    BackHandler { navController.popBackStack() }
                                    BudgetsGoalsScreen(viewModel = viewModel)
                                }

                                composable(Screen.AdvisorChat.route) {
                                    BackHandler { navController.popBackStack() }
                                    AdvisorChatScreen(viewModel = viewModel)
                                }

                                composable(Screen.ReceiptScanner.route) {
                                    BackHandler { navController.popBackStack() }
                                    ReceiptScannerScreen(
                                        viewModel = viewModel,
                                        onTransactionAdded = {
                                            navController.navigate(Screen.Transactions.route)
                                        }
                                    )
                                }

                                composable(Screen.AccountsSync.route) {
                                    BackHandler { navController.popBackStack() }
                                    AccountsSyncScreen(viewModel = viewModel)
                                }

                                composable(Screen.Reports.route) {
                                    BackHandler { navController.popBackStack() }
                                    ReportsScreen(viewModel = viewModel)
                                }

                                composable(Screen.Settings.route) {
                                    BackHandler { navController.popBackStack() }
                                    SettingsSecurityScreen(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            }
                        }

                        // Encrypted Biometric Lock Screen Overlay
                        if (isLocked) {
                            BiometricLockOverlay(
                                onUnlock = { pin -> viewModel.unlockApp(pin) },
                                onBiometricUnlock = { viewModel.unlockApp("1234") }
                            )
                        }
                    }
                }
            }
        }
    }
}

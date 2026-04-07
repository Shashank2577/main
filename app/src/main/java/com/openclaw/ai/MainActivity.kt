package com.openclaw.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openclaw.ai.data.repository.ThemeMode
import com.openclaw.ai.ui.chat.ChatScreen
import com.openclaw.ai.ui.drawer.AppNavigationDrawer
import com.openclaw.ai.ui.drawer.DrawerViewModel
import com.openclaw.ai.ui.filebrowser.FileBrowserScreen
import com.openclaw.ai.ui.modelpicker.ModelPickerSheet
import com.openclaw.ai.ui.navigation.BottomSheet
import com.openclaw.ai.ui.navigation.Screen
import com.openclaw.ai.ui.onboarding.OnboardingScreen
import com.openclaw.ai.ui.settings.PerChatSettingsSheet
import com.openclaw.ai.ui.settings.SettingsScreen
import com.openclaw.ai.ui.theme.OpenClawAITheme
import com.openclaw.ai.ui.voice.VoiceConversationScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Wait until onboarding state is resolved before composing nav host
            if (onboardingComplete != null) {
                OpenClawAITheme(darkTheme = darkTheme, dynamicColor = true) {
                    OpenClawNavHost(
                        startDestination = if (onboardingComplete == true) Screen.Chat else Screen.Onboarding,
                        mainViewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenClawNavHost(
    startDestination: Screen,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val navController: NavHostController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerViewModel: DrawerViewModel = hiltViewModel()

    var activeBottomSheet by remember { mutableStateOf<BottomSheet?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onComplete = {
                    mainViewModel.onOnboardingComplete()
                    navController.navigate(Screen.Chat) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.Chat> {
            AppNavigationDrawer(
                drawerState = drawerState,
                viewModel = drawerViewModel,
                onConversationSelected = { conversationId ->
                    mainViewModel.setCurrentConversation(conversationId)
                },
                onNewChat = { conversationId ->
                    mainViewModel.setCurrentConversation(conversationId)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onNavigateToVoice = {
                    navController.navigate(Screen.VoiceConversation)
                },
                onNavigateToFiles = {
                    navController.navigate(Screen.FileBrowser)
                },
            ) {
                ChatScreen(
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onOpenModelPicker = {
                        activeBottomSheet = BottomSheet.ModelPicker
                    },
                    onOpenPerChatSettings = {
                        val conversationId = drawerViewModel.currentConversationId.value
                        if (conversationId != null) {
                            activeBottomSheet = BottomSheet.PerChatSettings(conversationId)
                        }
                    },
                    onNavigateToVoice = {
                        navController.navigate(Screen.VoiceConversation)
                    },
                )
            }
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.VoiceConversation> {
            VoiceConversationScreen(
                onBack = { navController.popBackStack() },
                onSwitchToText = {
                    navController.navigate(Screen.Chat) {
                        popUpTo(Screen.Chat) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.FileBrowser> {
            val spaceId = drawerViewModel.currentSpaceId.collectAsStateWithLifecycle().value
            FileBrowserScreen(
                spaceId = spaceId,
                spaceName = "Files",
                onBack = { navController.popBackStack() },
            )
        }
    }

    // Bottom sheets rendered outside NavHost so they overlay any screen
    when (val sheet = activeBottomSheet) {
        BottomSheet.ModelPicker -> {
            ModelPickerSheet(
                onDismiss = { activeBottomSheet = null },
                onManageModels = {
                    activeBottomSheet = null
                    navController.navigate(Screen.Settings)
                },
            )
        }
        is BottomSheet.PerChatSettings -> {
            PerChatSettingsSheet(
                conversationId = sheet.conversationId,
                onDismiss = { activeBottomSheet = null },
            )
        }
        null -> Unit
    }
}

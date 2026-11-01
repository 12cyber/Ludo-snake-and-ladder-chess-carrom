package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActiveScreen
import com.example.data.GameType
import com.example.data.MainNavTab
import com.example.data.PlayverseViewModel
import com.example.games.CarromGameScreen
import com.example.games.ChessGameScreen
import com.example.games.LudoGameScreen
import com.example.games.SnakesAndLaddersScreen
import com.example.ui.components.LiquidGlassBottomNav
import com.example.ui.components.PlayverseAnimatedBackground
import com.example.ui.screens.GameModeLobbyScreen
import com.example.ui.screens.GamesCatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PawnCollectionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.PlayverseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayverseApp()
        }
    }
}

@Composable
fun PlayverseApp() {
    val context = LocalContext.current
    val viewModel = remember { PlayverseViewModel(context) }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val statsMap by viewModel.statsMap.collectAsStateWithLifecycle()
    val recentMatches by viewModel.recentMatches.collectAsStateWithLifecycle()
    val continueSession by viewModel.continueSession.collectAsStateWithLifecycle()
    val launchConfig by viewModel.launchConfig.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.soundManager.stopBackgroundMusic()
        }
    }

    PlayverseTheme(darkTheme = settings.darkMode) {
        PlayverseAnimatedBackground(darkMode = settings.darkMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Crossfade(
                    targetState = activeScreen,
                    animationSpec = tween(220),
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        ActiveScreen.MAIN_TABS -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Crossfade(
                                    targetState = selectedTab,
                                    animationSpec = tween(180),
                                    label = "tab_crossfade"
                                ) { tab ->
                                    when (tab) {
                                        MainNavTab.HOME -> {
                                            HomeScreen(
                                                profile = profile,
                                                totalGames = viewModel.getTotalGamesPlayed(),
                                                totalWins = viewModel.getTotalWins(),
                                                winPercentage = viewModel.getWinPercentage(),
                                                continueSession = continueSession,
                                                onLaunchGame = { viewModel.openGameLobby(it) },
                                                onOpenSettings = { viewModel.navigateToScreen(ActiveScreen.SETTINGS) },
                                                onOpenProfileEdit = { viewModel.selectTab(MainNavTab.PROFILE) },
                                                onOpenPawnVault = { viewModel.navigateToScreen(ActiveScreen.PAWN_COLLECTION) }
                                            )
                                        }

                                        MainNavTab.GAMES -> {
                                            GamesCatalogScreen(
                                                profile = profile,
                                                onLaunchGame = { viewModel.openGameLobby(it) },
                                                onOpenPawnVault = { viewModel.navigateToScreen(ActiveScreen.PAWN_COLLECTION) },
                                                onSelectSnakesTheme = { viewModel.selectSnakesTheme(it) },
                                                onSelectCarromThemes = { b, c -> viewModel.selectCarromThemes(b, c) },
                                                onSelectChessThemes = { b, p -> viewModel.selectChessThemes(b, p) }
                                            )
                                        }

                                        MainNavTab.STATISTICS -> {
                                            StatisticsScreen(
                                                statsMap = statsMap,
                                                achievements = viewModel.getAchievements(),
                                                recentMatches = recentMatches,
                                                favoriteGame = viewModel.getFavoriteGame()
                                            )
                                        }

                                        MainNavTab.PROFILE -> {
                                            ProfileScreen(
                                                profile = profile,
                                                totalWins = viewModel.getTotalWins(),
                                                onSaveProfile = { name, avId, flag ->
                                                    viewModel.updateProfile(name, avId, flag)
                                                },
                                                onOpenPawnVault = { viewModel.navigateToScreen(ActiveScreen.PAWN_COLLECTION) },
                                                onOpenSettings = { viewModel.navigateToScreen(ActiveScreen.SETTINGS) }
                                            )
                                        }
                                    }
                                }

                                LiquidGlassBottomNav(
                                    selectedTab = selectedTab,
                                    onSelectTab = { viewModel.selectTab(it) },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }

                        ActiveScreen.GAME_LOBBY -> {
                            GameModeLobbyScreen(
                                config = launchConfig,
                                profile = profile,
                                onUpdateConfig = { viewModel.updateLaunchConfig(it) },
                                onSelectSnakesTheme = { viewModel.selectSnakesTheme(it) },
                                onSelectCarromThemes = { b, c -> viewModel.selectCarromThemes(b, c) },
                                onSelectChessThemes = { b, p -> viewModel.selectChessThemes(b, p) },
                                onOpenPawnVault = { viewModel.navigateToScreen(ActiveScreen.PAWN_COLLECTION) },
                                onStartMatch = { viewModel.startConfiguredMatch() },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.SNAKES_GAME -> {
                            SnakesAndLaddersScreen(
                                playerUsername = profile.username,
                                initialTheme = profile.snakesTheme,
                                launchConfig = launchConfig,
                                diceSpeed = settings.diceSpeed,
                                soundManager = viewModel.soundManager,
                                onSelectTheme = { viewModel.selectSnakesTheme(it) },
                                onSaveSession = { mode, status ->
                                    viewModel.saveContinueSession(GameType.SNAKES, mode, status)
                                },
                                onMatchFinished = { won, modeLabel, opp, details, ladders ->
                                    viewModel.recordMatchResult(
                                        gameType = GameType.SNAKES,
                                        won = won,
                                        modeLabel = modeLabel,
                                        opponentLabel = opp,
                                        details = details,
                                        specialMetricDelta = ladders
                                    )
                                },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.LUDO_GAME -> {
                            LudoGameScreen(
                                playerUsername = profile.username,
                                selectedPawnSkinId = profile.selectedPawnSkinId,
                                launchConfig = launchConfig,
                                diceSpeed = settings.diceSpeed,
                                soundManager = viewModel.soundManager,
                                onOpenPawnVault = { viewModel.navigateToScreen(ActiveScreen.PAWN_COLLECTION) },
                                onSaveSession = { mode, status ->
                                    viewModel.saveContinueSession(GameType.LUDO, mode, status)
                                },
                                onMatchFinished = { won, modeLabel, opp, details, captures ->
                                    viewModel.recordMatchResult(
                                        gameType = GameType.LUDO,
                                        won = won,
                                        modeLabel = modeLabel,
                                        opponentLabel = opp,
                                        details = details,
                                        specialMetricDelta = captures
                                    )
                                },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.CARROM_GAME -> {
                            CarromGameScreen(
                                playerUsername = profile.username,
                                initialBoardTheme = profile.carromBoardTheme,
                                initialCoinStyle = profile.carromCoinStyle,
                                launchConfig = launchConfig,
                                soundManager = viewModel.soundManager,
                                onSelectThemes = { b, c -> viewModel.selectCarromThemes(b, c) },
                                onSaveSession = { mode, status ->
                                    viewModel.saveContinueSession(GameType.CARROM, mode, status)
                                },
                                onMatchFinished = { won, modeLabel, opp, details, queens ->
                                    viewModel.recordMatchResult(
                                        gameType = GameType.CARROM,
                                        won = won,
                                        modeLabel = modeLabel,
                                        opponentLabel = opp,
                                        details = details,
                                        specialMetricDelta = queens
                                    )
                                },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.CHESS_GAME -> {
                            ChessGameScreen(
                                playerUsername = profile.username,
                                initialBoardTheme = profile.chessBoardTheme,
                                initialPieceSet = profile.chessPieceSet,
                                launchConfig = launchConfig,
                                soundManager = viewModel.soundManager,
                                onSelectThemes = { b, p -> viewModel.selectChessThemes(b, p) },
                                onSaveSession = { mode, status ->
                                    viewModel.saveContinueSession(GameType.CHESS, mode, status)
                                },
                                onMatchFinished = { won, isDraw, modeLabel, opp, details, mates ->
                                    viewModel.recordMatchResult(
                                        gameType = GameType.CHESS,
                                        won = won,
                                        isDraw = isDraw,
                                        modeLabel = modeLabel,
                                        opponentLabel = opp,
                                        details = details,
                                        specialMetricDelta = mates
                                    )
                                },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.PAWN_COLLECTION -> {
                            PawnCollectionScreen(
                                selectedPawnId = profile.selectedPawnSkinId,
                                totalWins = viewModel.getTotalWins(),
                                onSelectPawn = { viewModel.selectPawnSkin(it) },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }

                        ActiveScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                onUpdateSettings = { viewModel.updateSettings(it) },
                                onResetStats = { viewModel.resetStatistics() },
                                onClearAllData = { viewModel.clearAllSavedData() },
                                onBack = { viewModel.returnToMainTabs() }
                            )
                        }
                    }
                }
            }
        }
    }
}

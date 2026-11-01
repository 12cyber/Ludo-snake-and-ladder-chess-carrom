package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActiveScreen
import com.example.data.AiDifficulty
import com.example.data.GameType
import com.example.data.MainNavTab
import com.example.data.PlayModeType
import com.example.data.PlayverseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Playverse", appName)
    }

    @Test
    fun `viewModel records match results and persists profile`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = PlayverseViewModel(context)
        vm.clearAllSavedData()

        vm.updateProfile("Commander Kai", 2, "🇮🇳 India")
        assertEquals("Commander Kai", vm.profile.value.username)

        vm.recordMatchResult(
            gameType = GameType.SNAKES,
            won = true,
            modeLabel = "Vs Computer",
            opponentLabel = "AI Bot",
            details = "Reached 100",
            specialMetricDelta = 3
        )
        assertEquals(1, vm.getTotalGamesPlayed())
        assertEquals(1, vm.getTotalWins())
        assertEquals(100, vm.getWinPercentage())
        assertTrue(vm.getAchievements().first { it.id == "first_victory" }.isUnlocked)

        vm.selectTab(MainNavTab.STATISTICS)
        assertEquals(MainNavTab.STATISTICS, vm.selectedTab.value)
    }

    @Test
    fun `game lobby configures local room and vs computer modes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = PlayverseViewModel(context)

        vm.openGameLobby(GameType.CARROM)
        assertEquals(ActiveScreen.GAME_LOBBY, vm.activeScreen.value)
        assertEquals(GameType.CARROM, vm.launchConfig.value.gameType)

        // Configure Mode 1: Local Room (Offline Pass & Play)
        vm.updateLaunchConfig { it.copy(playMode = PlayModeType.LOCAL_ROOM, playerCount = 2) }
        assertEquals(PlayModeType.LOCAL_ROOM, vm.launchConfig.value.playMode)
        vm.startConfiguredMatch()
        assertEquals(ActiveScreen.CARROM_GAME, vm.activeScreen.value)

        // Configure Mode 2: Vs Computer AI for Ludo
        vm.openGameLobby(GameType.LUDO)
        assertEquals(ActiveScreen.GAME_LOBBY, vm.activeScreen.value)
        vm.updateLaunchConfig {
            it.copy(
                playMode = PlayModeType.VS_COMPUTER,
                playerCount = 4,
                aiDifficulty = AiDifficulty.EXPERT
            )
        }
        vm.startConfiguredMatch()
        assertEquals(ActiveScreen.LUDO_GAME, vm.activeScreen.value)
        assertEquals(4, vm.launchConfig.value.playerCount)
        assertEquals(AiDifficulty.EXPERT, vm.launchConfig.value.aiDifficulty)
    }
}

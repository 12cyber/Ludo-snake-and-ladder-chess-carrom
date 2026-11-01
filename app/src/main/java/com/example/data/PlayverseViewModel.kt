package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayverseViewModel(context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("playverse_local_store", Context.MODE_PRIVATE)
    val soundManager = SoundManager(context.applicationContext)

    private val _selectedTab = MutableStateFlow(MainNavTab.HOME)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.MAIN_TABS)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    private val _launchConfig = MutableStateFlow(GameLaunchConfig())
    val launchConfig: StateFlow<GameLaunchConfig> = _launchConfig.asStateFlow()

    private val _profile = MutableStateFlow(PlayerProfileState())
    val profile: StateFlow<PlayerProfileState> = _profile.asStateFlow()

    private val _settings = MutableStateFlow(AppSettingsState())
    val settings: StateFlow<AppSettingsState> = _settings.asStateFlow()

    private val _statsMap = MutableStateFlow<Map<GameType, GameSpecificStats>>(
        mapOf(
            GameType.SNAKES to GameSpecificStats(GameType.SNAKES, specialMetricName = "Ladders Climbed"),
            GameType.LUDO to GameSpecificStats(GameType.LUDO, specialMetricName = "Opponents Captured"),
            GameType.CARROM to GameSpecificStats(GameType.CARROM, specialMetricName = "Queens Pocketed"),
            GameType.CHESS to GameSpecificStats(GameType.CHESS, specialMetricName = "Checkmates Delivered")
        )
    )
    val statsMap: StateFlow<Map<GameType, GameSpecificStats>> = _statsMap.asStateFlow()

    private val _recentMatches = MutableStateFlow<List<MatchHistoryItem>>(emptyList())
    val recentMatches: StateFlow<List<MatchHistoryItem>> = _recentMatches.asStateFlow()

    private val _continueSession = MutableStateFlow<ContinueGameSession?>(null)
    val continueSession: StateFlow<ContinueGameSession?> = _continueSession.asStateFlow()

    init {
        loadFromStorage()
        val s = _settings.value
        soundManager.updateSettings(s.soundEffects, s.backgroundMusic, s.vibration)
    }

    fun selectTab(tab: MainNavTab) {
        soundManager.playClick()
        _selectedTab.value = tab
        _activeScreen.value = ActiveScreen.MAIN_TABS
    }

    fun navigateToScreen(screen: ActiveScreen) {
        soundManager.playClick()
        _activeScreen.value = screen
    }

    /**
     * Opens the Full-Screen Pre-Game Mode Selection Lobby when a user taps a game card.
     */
    fun openGameLobby(gameType: GameType) {
        soundManager.playClick()
        _launchConfig.update {
            it.copy(
                gameType = gameType,
                playMode = PlayModeType.LOCAL_ROOM,
                playerCount = 2,
                aiDifficulty = _settings.value.defaultAiDifficulty,
                exactRollSnakes = _settings.value.snakesExactRoll
            )
        }
        _activeScreen.value = ActiveScreen.GAME_LOBBY
    }

    fun updateLaunchConfig(transform: (GameLaunchConfig) -> GameLaunchConfig) {
        soundManager.playClick()
        _launchConfig.update(transform)
    }

    /**
     * Starts the actual board match using the configured Mode (Local Room vs Computer AI).
     */
    fun startConfiguredMatch(config: GameLaunchConfig = _launchConfig.value) {
        soundManager.playClick()
        _launchConfig.value = config
        if (_settings.value.saveLastPlayedGame) {
            saveContinueSession(
                gameType = config.gameType,
                modeSummary = config.modeDisplayName,
                statusSummary = "In Progress • Tap to resume board"
            )
        }
        _activeScreen.value = when (config.gameType) {
            GameType.SNAKES -> ActiveScreen.SNAKES_GAME
            GameType.LUDO -> ActiveScreen.LUDO_GAME
            GameType.CARROM -> ActiveScreen.CARROM_GAME
            GameType.CHESS -> ActiveScreen.CHESS_GAME
        }
    }

    /**
     * Direct resume from Continue Playing card.
     */
    fun resumeGameDirect(gameType: GameType) {
        soundManager.playClick()
        _activeScreen.value = when (gameType) {
            GameType.SNAKES -> ActiveScreen.SNAKES_GAME
            GameType.LUDO -> ActiveScreen.LUDO_GAME
            GameType.CARROM -> ActiveScreen.CARROM_GAME
            GameType.CHESS -> ActiveScreen.CHESS_GAME
        }
    }

    fun returnToMainTabs() {
        soundManager.playClick()
        _activeScreen.value = ActiveScreen.MAIN_TABS
    }

    fun saveContinueSession(gameType: GameType, modeSummary: String, statusSummary: String) {
        if (!_settings.value.saveLastPlayedGame) return
        _continueSession.value = ContinueGameSession(
            gameType = gameType,
            modeSummary = modeSummary,
            statusSummary = statusSummary,
            updatedAtMs = System.currentTimeMillis()
        )
        persistToStorage()
    }

    fun updateProfile(username: String, avatarId: Int, countryFlag: String) {
        soundManager.playClick()
        _profile.update {
            it.copy(
                username = username.trim().ifEmpty { "Player One" },
                avatarId = avatarId,
                countryFlag = countryFlag
            )
        }
        persistToStorage()
    }

    fun selectPawnSkin(skinId: String) {
        soundManager.playClick()
        _profile.update { it.copy(selectedPawnSkinId = skinId) }
        persistToStorage()
    }

    fun selectSnakesTheme(theme: SnakesBoardTheme) {
        soundManager.playClick()
        _profile.update { it.copy(snakesTheme = theme) }
        persistToStorage()
    }

    fun selectCarromThemes(board: CarromBoardTheme, coins: CarromCoinStyle) {
        soundManager.playClick()
        _profile.update { it.copy(carromBoardTheme = board, carromCoinStyle = coins) }
        persistToStorage()
    }

    fun selectChessThemes(board: ChessBoardTheme, pieces: ChessPieceSet) {
        soundManager.playClick()
        _profile.update { it.copy(chessBoardTheme = board, chessPieceSet = pieces) }
        persistToStorage()
    }

    fun updateSettings(transform: (AppSettingsState) -> AppSettingsState) {
        soundManager.playClick()
        _settings.update(transform)
        val s = _settings.value
        soundManager.updateSettings(s.soundEffects, s.backgroundMusic, s.vibration)
        if (!s.saveLastPlayedGame) {
            _continueSession.value = null
        }
        persistToStorage()
    }

    fun recordMatchResult(
        gameType: GameType,
        won: Boolean,
        isDraw: Boolean = false,
        modeLabel: String,
        opponentLabel: String,
        details: String,
        specialMetricDelta: Int = 0
    ) {
        if (won) soundManager.playVictory() else soundManager.playClick()
        val currentMap = _statsMap.value.toMutableMap()
        val old = currentMap[gameType] ?: GameSpecificStats(gameType)
        val newStreak = if (won) old.currentStreak + 1 else 0
        val updated = old.copy(
            gamesPlayed = old.gamesPlayed + 1,
            wins = old.wins + (if (won) 1 else 0),
            losses = old.losses + (if (!won && !isDraw) 1 else 0),
            draws = old.draws + (if (isDraw) 1 else 0),
            currentStreak = newStreak,
            bestStreak = maxOf(old.bestStreak, newStreak),
            specialMetricValue = old.specialMetricValue + specialMetricDelta
        )
        currentMap[gameType] = updated
        _statsMap.value = currentMap

        val xpGain = when {
            won -> 120
            isDraw -> 60
            else -> 35
        }
        _profile.update { it.copy(xp = it.xp + xpGain) }

        val timeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        val matchItem = MatchHistoryItem(
            id = System.currentTimeMillis(),
            gameType = gameType,
            modeLabel = modeLabel,
            outcome = when {
                won -> "VICTORY"
                isDraw -> "DRAW"
                else -> "DEFEAT"
            },
            opponentLabel = opponentLabel,
            details = details,
            timestampLabel = timeFormat.format(Date())
        )
        _recentMatches.update { (listOf(matchItem) + it).take(25) }
        persistToStorage()
    }

    fun resetStatistics() {
        soundManager.playClick()
        _statsMap.value = mapOf(
            GameType.SNAKES to GameSpecificStats(GameType.SNAKES, specialMetricName = "Ladders Climbed"),
            GameType.LUDO to GameSpecificStats(GameType.LUDO, specialMetricName = "Opponents Captured"),
            GameType.CARROM to GameSpecificStats(GameType.CARROM, specialMetricName = "Queens Pocketed"),
            GameType.CHESS to GameSpecificStats(GameType.CHESS, specialMetricName = "Checkmates Delivered")
        )
        _recentMatches.value = emptyList()
        persistToStorage()
    }

    fun clearAllSavedData() {
        soundManager.playClick()
        prefs.edit().clear().apply()
        _profile.value = PlayerProfileState()
        _settings.value = AppSettingsState()
        _continueSession.value = null
        resetStatistics()
    }

    fun getTotalGamesPlayed(): Int = _statsMap.value.values.sumOf { it.gamesPlayed }
    fun getTotalWins(): Int = _statsMap.value.values.sumOf { it.wins }
    fun getTotalLosses(): Int = _statsMap.value.values.sumOf { it.losses }
    fun getWinPercentage(): Int {
        val total = getTotalGamesPlayed()
        return if (total > 0) ((getTotalWins() * 100f) / total).toInt() else 0
    }

    fun getFavoriteGame(): GameType {
        return _statsMap.value.values.maxByOrNull { it.gamesPlayed }?.let {
            if (it.gamesPlayed > 0) it.gameType else GameType.LUDO
        } ?: GameType.LUDO
    }

    fun getAchievements(): List<AchievementItem> {
        val totalGames = getTotalGamesPlayed()
        val totalWins = getTotalWins()
        val snakesWins = _statsMap.value[GameType.SNAKES]?.wins ?: 0
        val ludoWins = _statsMap.value[GameType.LUDO]?.wins ?: 0
        val carromWins = _statsMap.value[GameType.CARROM]?.wins ?: 0
        val chessWins = _statsMap.value[GameType.CHESS]?.wins ?: 0
        val bestStreak = _statsMap.value.values.maxOfOrNull { it.bestStreak } ?: 0

        return listOf(
            AchievementItem(
                id = "first_victory",
                title = "First Victory",
                description = "Win your first match in any Playverse game",
                iconSymbol = "🏆",
                currentProgress = totalWins.coerceAtMost(1),
                targetProgress = 1,
                accentColor = Color(0xFFF59E0B)
            ),
            AchievementItem(
                id = "ten_games",
                title = "10 Games Played",
                description = "Complete 10 matches across the Playverse arena",
                iconSymbol = "🎮",
                currentProgress = totalGames.coerceAtMost(10),
                targetProgress = 10,
                accentColor = Color(0xFF06B6D4)
            ),
            AchievementItem(
                id = "snake_champion",
                title = "Snake & Ladder Champion",
                description = "Reach tile 100 and claim 3 Snake & Ladders victories",
                iconSymbol = "🐍",
                currentProgress = snakesWins.coerceAtMost(3),
                targetProgress = 3,
                accentColor = Color(0xFF10B981)
            ),
            AchievementItem(
                id = "ludo_champion",
                title = "Ludo Champion",
                description = "Guide all 4 pawns home to win 3 Ludo Royal matches",
                iconSymbol = "🎲",
                currentProgress = ludoWins.coerceAtMost(3),
                targetProgress = 3,
                accentColor = Color(0xFF3B82F6)
            ),
            AchievementItem(
                id = "carrom_expert",
                title = "Carrom Expert",
                description = "Master physics angles and win 3 Carrom board matches",
                iconSymbol = "🎯",
                currentProgress = carromWins.coerceAtMost(3),
                targetProgress = 3,
                accentColor = Color(0xFFF97316)
            ),
            AchievementItem(
                id = "chess_master",
                title = "Chess Master",
                description = "Outsmart opponents in 3 Grandmaster Chess matches",
                iconSymbol = "♔",
                currentProgress = chessWins.coerceAtMost(3),
                targetProgress = 3,
                accentColor = Color(0xFF8B5CF6)
            ),
            AchievementItem(
                id = "streak_legend",
                title = "Unstoppable Streak",
                description = "Achieve a 3-win streak in any board game",
                iconSymbol = "🔥",
                currentProgress = bestStreak.coerceAtMost(3),
                targetProgress = 3,
                accentColor = Color(0xFFEF4444)
            ),
            AchievementItem(
                id = "all_rounder",
                title = "Playverse Grandmaster",
                description = "Reach Player Level 5 through multi-game mastery",
                iconSymbol = "👑",
                currentProgress = _profile.value.level.coerceAtMost(5),
                targetProgress = 5,
                accentColor = Color(0xFFEAB308)
            )
        )
    }

    private fun persistToStorage() {
        try {
            val root = JSONObject()
            val p = _profile.value
            root.put("username", p.username)
            root.put("avatarId", p.avatarId)
            root.put("countryFlag", p.countryFlag)
            root.put("xp", p.xp)
            root.put("selectedPawnSkinId", p.selectedPawnSkinId)
            root.put("snakesTheme", p.snakesTheme.name)
            root.put("carromBoardTheme", p.carromBoardTheme.name)
            root.put("carromCoinStyle", p.carromCoinStyle.name)
            root.put("chessBoardTheme", p.chessBoardTheme.name)
            root.put("chessPieceSet", p.chessPieceSet.name)

            val s = _settings.value
            val settingsObj = JSONObject().apply {
                put("soundEffects", s.soundEffects)
                put("backgroundMusic", s.backgroundMusic)
                put("vibration", s.vibration)
                put("animationQuality", s.animationQuality)
                put("graphicsQuality", s.graphicsQuality)
                put("darkMode", s.darkMode)
                put("language", s.language)
                put("saveLastPlayedGame", s.saveLastPlayedGame)
                put("snakesExactRoll", s.snakesExactRoll)
                put("diceSpeed", s.diceSpeed)
                put("defaultAiDifficulty", s.defaultAiDifficulty.name)
            }
            root.put("settings", settingsObj)

            val statsArr = JSONArray()
            for ((type, st) in _statsMap.value) {
                statsArr.put(
                    JSONObject().apply {
                        put("type", type.name)
                        put("gamesPlayed", st.gamesPlayed)
                        put("wins", st.wins)
                        put("losses", st.losses)
                        put("draws", st.draws)
                        put("bestStreak", st.bestStreak)
                        put("currentStreak", st.currentStreak)
                        put("specialMetricValue", st.specialMetricValue)
                    }
                )
            }
            root.put("stats", statsArr)

            val matchesArr = JSONArray()
            for (m in _recentMatches.value) {
                matchesArr.put(
                    JSONObject().apply {
                        put("id", m.id)
                        put("gameType", m.gameType.name)
                        put("modeLabel", m.modeLabel)
                        put("outcome", m.outcome)
                        put("opponentLabel", m.opponentLabel)
                        put("details", m.details)
                        put("timestampLabel", m.timestampLabel)
                    }
                )
            }
            root.put("matches", matchesArr)

            _continueSession.value?.let { cs ->
                root.put(
                    "continueSession",
                    JSONObject().apply {
                        put("gameType", cs.gameType.name)
                        put("modeSummary", cs.modeSummary)
                        put("statusSummary", cs.statusSummary)
                        put("updatedAtMs", cs.updatedAtMs)
                    }
                )
            }

            prefs.edit().putString("state_json", root.toString()).apply()
        } catch (_: Exception) {
        }
    }

    private fun loadFromStorage() {
        val raw = prefs.getString("state_json", null) ?: return
        try {
            val root = JSONObject(raw)
            _profile.value = PlayerProfileState(
                username = root.optString("username", "Aria Vance"),
                avatarId = root.optInt("avatarId", 0),
                countryFlag = root.optString("countryFlag", "🇮🇳 India"),
                xp = root.optInt("xp", 450),
                selectedPawnSkinId = root.optString("selectedPawnSkinId", "crown"),
                snakesTheme = runCatching {
                    SnakesBoardTheme.valueOf(root.optString("snakesTheme", "CLASSIC"))
                }.getOrDefault(SnakesBoardTheme.CLASSIC),
                carromBoardTheme = runCatching {
                    CarromBoardTheme.valueOf(root.optString("carromBoardTheme", "CLASSIC_WOOD"))
                }.getOrDefault(CarromBoardTheme.CLASSIC_WOOD),
                carromCoinStyle = runCatching {
                    CarromCoinStyle.valueOf(root.optString("carromCoinStyle", "CLASSIC"))
                }.getOrDefault(CarromCoinStyle.CLASSIC),
                chessBoardTheme = runCatching {
                    ChessBoardTheme.valueOf(root.optString("chessBoardTheme", "CLASSIC_WOOD"))
                }.getOrDefault(ChessBoardTheme.CLASSIC_WOOD),
                chessPieceSet = runCatching {
                    ChessPieceSet.valueOf(root.optString("chessPieceSet", "STAUNTON"))
                }.getOrDefault(ChessPieceSet.STAUNTON)
            )

            root.optJSONObject("settings")?.let { sObj ->
                _settings.value = AppSettingsState(
                    soundEffects = sObj.optBoolean("soundEffects", true),
                    backgroundMusic = sObj.optBoolean("backgroundMusic", false),
                    vibration = sObj.optBoolean("vibration", true),
                    animationQuality = sObj.optString("animationQuality", "Ultra"),
                    graphicsQuality = sObj.optString("graphicsQuality", "Ultra 3D"),
                    darkMode = sObj.optBoolean("darkMode", true),
                    language = sObj.optString("language", "English"),
                    saveLastPlayedGame = sObj.optBoolean("saveLastPlayedGame", true),
                    snakesExactRoll = sObj.optBoolean("snakesExactRoll", true),
                    diceSpeed = sObj.optString("diceSpeed", "Normal"),
                    defaultAiDifficulty = runCatching {
                        AiDifficulty.valueOf(sObj.optString("defaultAiDifficulty", "MEDIUM"))
                    }.getOrDefault(AiDifficulty.MEDIUM)
                )
            }

            root.optJSONArray("stats")?.let { arr ->
                val map = _statsMap.value.toMutableMap()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val type = runCatching { GameType.valueOf(item.getString("type")) }.getOrNull() ?: continue
                    val defaultMetric = map[type]?.specialMetricName ?: "Highlights"
                    map[type] = GameSpecificStats(
                        gameType = type,
                        gamesPlayed = item.optInt("gamesPlayed", 0),
                        wins = item.optInt("wins", 0),
                        losses = item.optInt("losses", 0),
                        draws = item.optInt("draws", 0),
                        bestStreak = item.optInt("bestStreak", 0),
                        currentStreak = item.optInt("currentStreak", 0),
                        specialMetricName = defaultMetric,
                        specialMetricValue = item.optInt("specialMetricValue", 0)
                    )
                }
                _statsMap.value = map
            }

            root.optJSONArray("matches")?.let { arr ->
                val list = mutableListOf<MatchHistoryItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val type = runCatching { GameType.valueOf(item.getString("gameType")) }.getOrNull() ?: continue
                    list.add(
                        MatchHistoryItem(
                            id = item.optLong("id", i.toLong()),
                            gameType = type,
                            modeLabel = item.optString("modeLabel", "Vs Computer"),
                            outcome = item.optString("outcome", "VICTORY"),
                            opponentLabel = item.optString("opponentLabel", "AI Bot"),
                            details = item.optString("details", ""),
                            timestampLabel = item.optString("timestampLabel", "Recent")
                        )
                    )
                }
                _recentMatches.value = list
            }

            root.optJSONObject("continueSession")?.let { csObj ->
                val type = runCatching { GameType.valueOf(csObj.getString("gameType")) }.getOrNull()
                if (type != null) {
                    _continueSession.value = ContinueGameSession(
                        gameType = type,
                        modeSummary = csObj.optString("modeSummary", "Vs Computer"),
                        statusSummary = csObj.optString("statusSummary", "Tap to resume"),
                        updatedAtMs = csObj.optLong("updatedAtMs", System.currentTimeMillis())
                    )
                }
            }
        } catch (_: Exception) {
        }
    }
}

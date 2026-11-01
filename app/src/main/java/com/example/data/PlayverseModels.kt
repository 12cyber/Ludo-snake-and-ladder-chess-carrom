package com.example.data

import androidx.compose.ui.graphics.Color

enum class MainNavTab(val label: String) {
    HOME("Home"),
    GAMES("Games"),
    STATISTICS("Statistics"),
    PROFILE("Profile")
}

enum class ActiveScreen {
    MAIN_TABS,
    GAME_LOBBY,
    SNAKES_GAME,
    LUDO_GAME,
    CARROM_GAME,
    CHESS_GAME,
    PAWN_COLLECTION,
    SETTINGS
}

enum class PlayModeType(val title: String, val subtitle: String) {
    LOCAL_ROOM(
        title = "Mode 1: Local Room (Offline)",
        subtitle = "Play with friends & family in the same room on this device"
    ),
    VS_COMPUTER(
        title = "Mode 2: Play vs Computer",
        subtitle = "Battle our tactical offline AI opponent across 5 difficulty tiers"
    ),
    PRACTICE(
        title = "Practice & Free Play",
        subtitle = "Experiment with shots, angles, hints & unranked board mastery"
    ),
    PUZZLE(
        title = "Grandmaster Tactical Puzzles",
        subtitle = "Solve curated checkmate and fork challenges"
    )
}

data class GameLaunchConfig(
    val gameType: GameType = GameType.CARROM,
    val playMode: PlayModeType = PlayModeType.VS_COMPUTER,
    val playerCount: Int = 2,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val exactRollSnakes: Boolean = true
) {
    val isVsComputer: Boolean get() = playMode == PlayModeType.VS_COMPUTER
    val modeDisplayName: String
        get() = when (playMode) {
            PlayModeType.LOCAL_ROOM -> "Local ${playerCount}P Room"
            PlayModeType.VS_COMPUTER -> "Vs Computer (${aiDifficulty.label})"
            PlayModeType.PRACTICE -> "Practice Mode"
            PlayModeType.PUZZLE -> "Puzzle Mode"
        }
}

enum class GameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val accentColor: Color,
    val secondaryColor: Color
) {
    SNAKES(
        id = "snakes",
        title = "Snake & Ladders",
        subtitle = "100-tile 3D serpentine quest with animated snakes & 8 worlds",
        badge = "Local 2-4P • Vs AI",
        accentColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669)
    ),
    LUDO(
        id = "ludo",
        title = "Ludo Royal",
        subtitle = "Tactical 4-quadrant showdown with 3D dice & 13 custom pawns",
        badge = "Local 2-4P • Smart AI",
        accentColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFFEF4444)
    ),
    CARROM(
        id = "carrom",
        title = "Carrom Pro 3D",
        subtitle = "Direct finger slide & pull-back physics striker with Queen cover rules",
        badge = "Touch Slingshot • 2D Physics",
        accentColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFB45309)
    ),
    CHESS(
        id = "chess",
        title = "Grandmaster Chess",
        subtitle = "Complete FIDE legal engine, Alpha-Beta AI, clock & tactical puzzles",
        badge = "Local 2P • 5 AI Tiers",
        accentColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFF6D28D9)
    )
}

enum class AiDifficulty(val label: String, val multiplier: Int) {
    BEGINNER("Beginner", 1),
    EASY("Easy", 2),
    MEDIUM("Medium", 3),
    HARD("Hard", 4),
    EXPERT("Expert", 5)
}

enum class SnakesBoardTheme(
    val displayName: String,
    val tagline: String,
    val tileEven: Color,
    val tileOdd: Color,
    val boardBgTop: Color,
    val boardBgBottom: Color,
    val snakePrimary: Color,
    val snakeSecondary: Color,
    val ladderColor: Color
) {
    CLASSIC(
        "Classic Snakes & Ladders",
        "Royal ivory & crimson heritage board",
        Color(0xFFFEF3C7),
        Color(0xFFFDE68A),
        Color(0xFF1E293B),
        Color(0xFF0F172A),
        Color(0xFF10B981),
        Color(0xFFEF4444),
        Color(0xFFD97706)
    ),
    JUNGLE(
        "Jungle Adventure",
        "Lush rainforest vines & bamboo ladders",
        Color(0xFFD1FAE5),
        Color(0xFFA7F3D0),
        Color(0xFF064E3B),
        Color(0xFF022C22),
        Color(0xFF84CC16),
        Color(0xFF15803D),
        Color(0xFFB45309)
    ),
    OCEAN(
        "Ocean Adventure",
        "Coral reef depths & sea-serpent currents",
        Color(0xFFCFFAFE),
        Color(0xFFA5F3FC),
        Color(0xFF0C4A6E),
        Color(0xFF082F49),
        Color(0xFF06B6D4),
        Color(0xFFEC4899),
        Color(0xFFF59E0B)
    ),
    SPACE(
        "Space Adventure",
        "Nebula starlight tiles & laser bridges",
        Color(0xFF1E1B4B),
        Color(0xFF312E81),
        Color(0xFF090D16),
        Color(0xFF1E1B4B),
        Color(0xFFA855F7),
        Color(0xFF22D3EE),
        Color(0xFF38BDF8)
    ),
    FANTASY(
        "Fantasy Kingdom",
        "Enchanted castle stones & golden stairways",
        Color(0xFFF3E8FF),
        Color(0xFFE9D5FF),
        Color(0xFF3B0764),
        Color(0xFF1E1B4B),
        Color(0xFFEC4899),
        Color(0xFF8B5CF6),
        Color(0xFFFBBF24)
    ),
    NEON_CYBER(
        "Neon Cyber World",
        "Synthwave dark grid & holographic rails",
        Color(0xFF0F172A),
        Color(0xFF1E293B),
        Color(0xFF020617),
        Color(0xFF0F172A),
        Color(0xFF00F5D4),
        Color(0xFFFF007F),
        Color(0xFFFEE440)
    ),
    CANDY(
        "Candy World",
        "Sweet pastel wafer tiles & peppermint ladders",
        Color(0xFFFCE7F3),
        Color(0xFFFBCFE8),
        Color(0xFF831843),
        Color(0xFF500724),
        Color(0xFFF43F5E),
        Color(0xFFA855F7),
        Color(0xFFF59E0B)
    ),
    ANCIENT_TEMPLE(
        "Ancient Temple",
        "Carved sandstone relics & bronze steps",
        Color(0xFFFEF08A),
        Color(0xFFFDE047),
        Color(0xFF451A03),
        Color(0xFF271001),
        Color(0xFFD97706),
        Color(0xFFDC2626),
        Color(0xFFB45309)
    )
}

data class LudoPawnSkin(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val requiredWins: Int,
    val accentColor: Color
)

val ALL_LUDO_PAWN_SKINS = listOf(
    LudoPawnSkin("classic", "Classic Sovereign", "Classic", "Traditional polished glass pedestal token", 0, Color(0xFF3B82F6)),
    LudoPawnSkin("crown", "Imperial Crown", "Royal", "24K gold royal monarch crown with jewel tips", 0, Color(0xFFF59E0B)),
    LudoPawnSkin("knight", "Valiant Knight", "Royal", "Chivalric warhorse crest with silver visor", 0, Color(0xFF94A3B8)),
    LudoPawnSkin("robot", "Mecha Unit X", "Robot", "Cybernetic android core with glowing visor", 0, Color(0xFF06B6D4)),
    LudoPawnSkin("dragon", "Ember Dragon", "Fantasy", "Winged drake crest breathing crimson fire", 1, Color(0xFFEF4444)),
    LudoPawnSkin("lion", "Savanna Lion", "Animal", "Regal golden mane guardian emblem", 1, Color(0xFFFBBF24)),
    LudoPawnSkin("tiger", "Bengal Striker", "Animal", "Fierce striped apex predator totem", 2, Color(0xFFF97316)),
    LudoPawnSkin("penguin", "Frost Penguin", "Cartoon", "Playful antarctic tuxedo explorer", 0, Color(0xFF38BDF8)),
    LudoPawnSkin("alien", "Nebula Voyager", "Space", "Extraterrestrial star-cruiser commander", 2, Color(0xFF10B981)),
    LudoPawnSkin("crystal", "Aether Crystal", "Crystal", "Prismatic multifaceted arcane shard", 3, Color(0xFFA855F7)),
    LudoPawnSkin("trophy", "Golden Chalice", "Metallic", "Championship solid gold victory cup", 4, Color(0xFFEAB308)),
    LudoPawnSkin("helmet", "Astro Helmet", "Space", "Deep-space visored cosmonaut rig", 2, Color(0xFF60A5FA)),
    LudoPawnSkin("football", "Pro Striker Ball", "Sports", "Hexagonal championship match sphere", 0, Color(0xFF22C55E))
)

enum class CarromBoardTheme(
    val displayName: String,
    val surfaceColor: Color,
    val borderColor: Color,
    val lineColor: Color,
    val pocketColor: Color
) {
    CLASSIC_WOOD("Classic Rosewood", Color(0xFFF4D7A7), Color(0xFF3E1C0A), Color(0xFF5C2D16), Color(0xFF140B05)),
    DARK_WOOD("Royal Teak", Color(0xFFD8AF7F), Color(0xFF261106), Color(0xFF3D1C0A), Color(0xFF100703)),
    ROYAL_GOLD("Imperial Gold", Color(0xFFFDF2D0), Color(0xFF6B2F0B), Color(0xFFB45309), Color(0xFF241002)),
    MODERN_BLACK("Obsidian Pro", Color(0xFF1E293B), Color(0xFF090D16), Color(0xFF38BDF8), Color(0xFF020617)),
    MARBLE("Carrara Marble", Color(0xFFF1F5F9), Color(0xFF273449), Color(0xFF475569), Color(0xFF0F172A)),
    NEON("Cyber Arena", Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF00F5D4), Color(0xFF020617)),
    TOURNAMENT("Championship Birch", Color(0xFFF7E2BC), Color(0xFF18181B), Color(0xFF8B0000), Color(0xFF09090B))
}

enum class CarromCoinStyle(
    val displayName: String,
    val whiteCoin: Color,
    val blackCoin: Color,
    val queenCoin: Color,
    val rimColor: Color
) {
    CLASSIC("Classic Ivory & Ebony", Color(0xFFFFF8E7), Color(0xFF27272A), Color(0xFFDC2626), Color(0xFFD97706)),
    WOODEN("Hand-Turned Wood", Color(0xFFFDE68A), Color(0xFF451A03), Color(0xFFB91C1C), Color(0xFF92400E)),
    METALLIC("Brushed Titanium", Color(0xFFE2E8F0), Color(0xFF334155), Color(0xFFEF4444), Color(0xFF94A3B8)),
    GOLD("24K Gold & Bronze", Color(0xFFFEF08A), Color(0xFF713F12), Color(0xFFF43F5E), Color(0xFFF59E0B)),
    SILVER("Sterling Silver", Color(0xFFF8FAFC), Color(0xFF1E293B), Color(0xFFEC4899), Color(0xFFCBD5E1)),
    MARBLE("Polished Onyx", Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0xFFE11D48), Color(0xFF64748B)),
    CRYSTAL("Prismatic Quartz", Color(0xFFCFFAFE), Color(0xFF312E81), Color(0xFFFF007F), Color(0xFF22D3EE))
}

enum class ChessBoardTheme(
    val displayName: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val borderColor: Color
) {
    CLASSIC_WOOD("Classic Wood", Color(0xFFF3D9B1), Color(0xFFB58863), Color(0xFF3E220D)),
    DARK_WALNUT("Dark Walnut", Color(0xFFDCC3A1), Color(0xFF6E472B), Color(0xFF26150A)),
    MARBLE("Italian Marble", Color(0xFFF8FAFC), Color(0xFF94A3B8), Color(0xFF1E293B)),
    ROYAL_GOLD("Royal Gold", Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFF451A03)),
    MODERN_MINIMAL("Modern Slate", Color(0xFFE2E8F0), Color(0xFF475569), Color(0xFF0F172A)),
    MIDNIGHT_BLUE("Midnight Sapphire", Color(0xFFCBD5E1), Color(0xFF1E3A8A), Color(0xFF0B1536)),
    TOURNAMENT_GREEN("Tournament Green", Color(0xFFEEEED2), Color(0xFF769656), Color(0xFF253218))
}

enum class ChessPieceSet(
    val displayName: String,
    val whiteFill: Color,
    val whiteStroke: Color,
    val blackFill: Color,
    val blackStroke: Color
) {
    STAUNTON("Classic Staunton", Color(0xFFFFFBEB), Color(0xFF78350F), Color(0xFF1E293B), Color(0xFFF59E0B)),
    ROYAL_GOLD("Royal Gold", Color(0xFFFEF08A), Color(0xFFB45309), Color(0xFF27272A), Color(0xFFFBBF24)),
    MODERN("Modern Studio", Color(0xFFF8FAFC), Color(0xFF334155), Color(0xFF0F172A), Color(0xFF38BDF8)),
    MARBLE("Sculpted Marble", Color(0xFFFFFFFF), Color(0xFF64748B), Color(0xFF1E293B), Color(0xFFCBD5E1)),
    GLASS("Frosted Glass", Color(0xFFE0F2FE), Color(0xFF0284C7), Color(0xFF1E1B4B), Color(0xFF818CF8)),
    METALLIC("Forged Chrome", Color(0xFFE2E8F0), Color(0xFF475569), Color(0xFF18181B), Color(0xFFA1A1AA)),
    FANTASY("Arcane Sovereign", Color(0xFFFDF4FF), Color(0xFFA21CAF), Color(0xFF2E1065), Color(0xFFE879F9))
}

data class PlayerAvatar(
    val id: Int,
    val name: String,
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val symbol: String
)

val PLAYER_AVATARS = listOf(
    PlayerAvatar(0, "Grandmaster", "Royal Tactician", Color(0xFFF59E0B), Color(0xFF78350F), "♔"),
    PlayerAvatar(1, "Cyber Sovereign", "Neon Strategist", Color(0xFF06B6D4), Color(0xFF1E3A8A), "⚡"),
    PlayerAvatar(2, "Emerald Viper", "Serpent Ruler", Color(0xFF10B981), Color(0xFF064E3B), "🐉"),
    PlayerAvatar(3, "Crimson Striker", "Carrom Ace", Color(0xFFEF4444), Color(0xFF7F1D1D), "🎯"),
    PlayerAvatar(4, "Astral Knight", "Star Commander", Color(0xFF8B5CF6), Color(0xFF3B0764), "♞"),
    PlayerAvatar(5, "Golden Phoenix", "Immortal Champion", Color(0xFFF97316), Color(0xFF7C2D12), "🦅"),
    PlayerAvatar(6, "Sapphire Queen", "Crown Guardian", Color(0xFF3B82F6), Color(0xFF1E3A8A), "♕"),
    PlayerAvatar(7, "Crystal Oracle", "Mystic Sage", Color(0xFFEC4899), Color(0xFF831843), "💎")
)

val COUNTRY_FLAGS = listOf(
    "🌍 Global", "🇮🇳 India", "🇺🇸 USA", "🇬🇧 UK", "🇨🇦 Canada",
    "🇦🇺 Australia", "🇩🇪 Germany", "🇫🇷 France", "🇯🇵 Japan", "🇧🇷 Brazil"
)

data class GameSpecificStats(
    val gameType: GameType,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val specialMetricName: String = "Highlights",
    val specialMetricValue: Int = 0
) {
    val winRate: Int
        get() = if (gamesPlayed > 0) ((wins * 100f) / gamesPlayed).toInt() else 0
}

data class MatchHistoryItem(
    val id: Long,
    val gameType: GameType,
    val modeLabel: String,
    val outcome: String,
    val opponentLabel: String,
    val details: String,
    val timestampLabel: String
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val accentColor: Color
) {
    val isUnlocked: Boolean get() = currentProgress >= targetProgress
}

data class ContinueGameSession(
    val gameType: GameType,
    val modeSummary: String,
    val statusSummary: String,
    val updatedAtMs: Long
)

data class AppSettingsState(
    val soundEffects: Boolean = true,
    val backgroundMusic: Boolean = false,
    val vibration: Boolean = true,
    val animationQuality: String = "Ultra",
    val graphicsQuality: String = "Ultra 3D",
    val darkMode: Boolean = true,
    val language: String = "English",
    val saveLastPlayedGame: Boolean = true,
    val snakesExactRoll: Boolean = true,
    val diceSpeed: String = "Normal",
    val defaultAiDifficulty: AiDifficulty = AiDifficulty.MEDIUM
)

data class PlayerProfileState(
    val username: String = "Aria Vance",
    val avatarId: Int = 0,
    val countryFlag: String = "🇮🇳 India",
    val xp: Int = 450,
    val selectedPawnSkinId: String = "crown",
    val snakesTheme: SnakesBoardTheme = SnakesBoardTheme.CLASSIC,
    val carromBoardTheme: CarromBoardTheme = CarromBoardTheme.CLASSIC_WOOD,
    val carromCoinStyle: CarromCoinStyle = CarromCoinStyle.CLASSIC,
    val chessBoardTheme: ChessBoardTheme = ChessBoardTheme.CLASSIC_WOOD,
    val chessPieceSet: ChessPieceSet = ChessPieceSet.STAUNTON
) {
    val level: Int get() = (xp / 200) + 1
    val xpInCurrentLevel: Int get() = xp % 200
}

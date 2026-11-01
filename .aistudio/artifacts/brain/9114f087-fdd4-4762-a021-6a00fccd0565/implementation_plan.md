# Playverse — Realistic Visual Overhaul, Pre-Game Mode Lobby & Direct Touch Carrom

This update transforms Playverse into a tactile, photorealistic mobile board-gaming experience featuring a dedicated **Pre-Game Mode Selection Lobby** for every game, **direct finger slide-and-pull-back Carrom controls** (eliminating sliders and strike buttons), **60fps continuous token and physics interpolation**, and **multi-layered 3D wood, marble, and metallic board rendering**.

### User Review & Critical Decisions

> [!IMPORTANT]
> All three core design and gameplay preferences from your clarification answers have been locked into this plan:

- **Confirmed Decision 1 — Direct Hand Controls in Carrom**: Removed the Strike button and sliders completely. Players touch the striker to slide it horizontally along their baseline, then pull back like a slingshot to aim and set power (with a live glowing trajectory & rebound guide ring), and lift their finger to release the shot.
- **Confirmed Decision 2 — Full-Screen Pre-Game Mode Lobby**: Tapping any of the 4 games on the Home or Games screen opens a dedicated **Mode Selection Lobby** where players choose between **Mode 1: Local Pass & Play (Offline Room with 2P/3P/4P options)** and **Mode 2: Play vs Computer AI** (with AI difficulty and board/piece theme selection) before launching into the match.
- **Confirmed Decision 3 — Realistic 3D Visuals & 60fps Motion**: Upgraded all four boards and game pieces with procedural wood-grain planks, bevelled 3D frames, recessed pockets with netting, multi-stop specular lighting, ambient drop shadows, and continuous 60fps animated coordinates for token gliding, dice tumbling, and rigid-body collisions.

---

### 1. Overview & Core Concept

- **What It Does**: Elevates Playverse from stylized flat vector boards to a realistic, responsive tabletop gaming suite where every interaction—sliding and pulling back the Carrom striker with one finger, watching tokens glide smoothly across 3D tiles, and choosing between Local Room multiplayer and Computer AI in a dedicated lobby—feels authentic and immediate.
- **Target Audience / Persona**: Mobile board game players on Android phones and tablets who expect realistic tabletop textures (rosewood, walnut, marble, ivory, and gold), zero-clutter touch gestures, and smooth 60fps animations in both solo AI matches and local offline room sessions with friends.
- **Key Value**: Removes clunky secondary UI controls (such as sliders and manual strike buttons), introduces a clear pre-game mode selection gateway for every game, and delivers tactile 3D board realism across Snake & Ladders, Ludo, Carrom, and Chess.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Home Arena to Pre-Game Mode Lobby**: Tapping any of the 4 showcase game cards (*Snake & Ladders*, *Ludo*, *Carrom*, *Chess*) transitions smoothly into a full-screen **Pre-Game Mode Selection Lobby** tailored to that game.
  2. **Mode Selection Flow**:
     - **Mode 1: Local Room (Offline Pass & Play)** — Play with friends in the same room on one device (supports 2, 3, or 4 players for Snake & Ladders and Ludo; 2 or 4 players for Carrom; 2 players for Chess).
     - **Mode 2: Play vs Computer** — Play offline against the game's dedicated AI engine with selectable difficulty (*Beginner*, *Easy*, *Medium*, *Hard*, *Expert*) and board/piece theme customization.
     - **Special Modes** — *Practice Mode* (Carrom & Chess) and *Tactical Puzzle Mode* (Chess) remain accessible directly from the lobby.
  3. **Carrom Direct Hand-Touch Flow**:
     - **Step 1 (Position on Baseline)**: Touch and drag horizontally along the glowing baseline zone to slide the striker smoothly between the left and right base circles; real-time red/green indicator shows if the striker overlaps another coin.
     - **Step 2 (Pull Back to Aim & Power)**: Drag backward away from the target direction (or pull from the striker) to stretch a dynamic 3D power ring and forward aiming laser line with wall-rebound prediction.
     - **Step 3 (Release to Strike)**: Lift your finger from the screen to immediately fire the striker with realistic acoustic wood impact and 60fps rigid-body physics.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Luxury Tactile Tabletop & Liquid Glass** — combining deep royal sapphire/indigo ambient rooms (`#070C1F` to `#121E4A`) with hyper-detailed physical board materials (polished rosewood frames, birch playing surfaces, walnut & maple inlaid chess squares, and bevelled ceramic/gold game tokens).
  - *Color Palette & Mood*:
    - `--background`: Deep Royal Obsidian (`#070C1F`) with radial ambient spotlighting behind active boards.
    - `--surface`: Translucent Liquid Glass panels (`rgba(26, 39, 88, 0.84)`) with specular top-edge refraction highlights.
    - `--primary`: Imperial Gold (`#F59E0B` / `#FBBF24`) for brass corner brackets, crowns, and active turn halos.
    - `--accent`: Electric Cyan (`#22D3EE`) and Emerald (`#10B981`) for aiming lasers, legal move indicators, and trajectory guides.
  - *Typography & Hierarchy*: Bundled *Outfit* ExtraBold/Bold for game titles and mode lobby headers, *Plus Jakarta Sans* for player HUD cards, and *JetBrains Mono* for chess clocks and match telemetry.
  - *Realistic Board & Piece Rendering*:
    - **Carrom**: Thick 3D rosewood outer bumper frame with metallic corner brackets, recessed woven-net corner pockets, fine birch wood grain lines, traditional red/black baseline arrows and center rosette star, and lathe-turned concentric grooves on every wooden coin and striker.
    - **Chess**: Bevelled walnut/marble frame, inner brass border trim, realistic multi-layered sculpted Staunton piece silhouettes (crown finials, horse-head knight contours, notched rooks, mitred bishops) with 3D directional shading, base pedestals, and soft cast shadows.
    - **Ludo**: 3D recessed corner yards with glass-domed token slots, metallic star safe zones, raised home-stretch tiles, and smooth continuous X/Y coordinate interpolation as pawns hop from square to square.
    - **Snake & Ladders**: 3D bevelled stone/wood numbered tiles with inner highlights, realistic shaded multi-pattern scales and forked tongues on animated snakes, and cylindrical 3D wooden/brass rungs with drop shadows on ladders.

- **Interactive Feedback & Motion**:
  - **60fps Continuous Token Gliding**: In Ludo, Chess, and Snake & Ladders, moving pieces interpolate their exact `(x, y)` canvas coordinates at 60fps with an arc lift (`scale` + vertical hop shadow) rather than snapping discretely from cell to cell.
  - **Touch Ripple & Tension Ring**: In Carrom, pulling back the striker expands a segmented radial tension ring around the striker that shifts from Emerald to Gold to Crimson as shot power increases.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Unified Two-Phase Touch Gesture on the Carrom Canvas**
  - *Chosen Approach*: Combine baseline positioning and pull-back slingshot aiming directly on the Carrom board canvas using pointer gesture tracking (`awaitEachGesture`). Dragging near the baseline slides the striker left/right; pulling back beyond the baseline threshold engages the aiming arrow and tension ring; releasing (`up`) triggers the physics impulse if pull distance exceeds the minimum cancel radius.
  - *Why*: Eliminates the need for external sliders or a "Strike" button, matching real-world finger-flick Carrom intuition and freeing screen space so the board can be larger and more immersive.
  - *Alternatives Considered*: Separate on-screen virtual joysticks were rejected because they clutter the UI and feel unnatural for Carrom.

- **Decision 2: Dedicated Pre-Game Mode Lobby Screen**
  - *Chosen Approach*: Insert a dedicated `GAME_MODE_LOBBY` state between the Home/Games catalog and the active game screen, pre-configured for the selected `GameType`.
  - *Why*: Gives players an immediate, unmistakable choice between **Mode 1: Local Pass & Play (Offline Room)** and **Mode 2: Play vs Computer AI** (plus player count, AI difficulty, and board theme) every time they tap a game card, while still allowing one-tap "Resume" from the *Continue Playing* banner.
  - *Alternatives Considered*: Hiding mode switching inside a small gear icon inside the board screen made it hard to discover Local vs. Computer modes.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         PlayverseViewModel (State)                        │
│  • ActiveScreen (MAIN_TABS, GAME_LOBBY, SNAKES, LUDO, CARROM, CHESS...)   │
│  • SelectedGameConfig (GameType, Mode: LOCAL_ROOM | VS_AI, Players, AI)   │
│  • PlayerProfile, GameSpecificStats, Achievements, SoundManager           │
└───────────────────┬───────────────────────────────────────┬───────────────┘
                    │                                       │
                    ▼                                       ▼
┌───────────────────────────────────────┐ ┌─────────────────────────────────┐
│      Main Hub & Liquid Glass Nav      │ │   Pre-Game Mode Selection Lobby │
│  • HomeScreen (3D Showcase Cards)     │ │  • Mode 1: Local Room (2P-4P)   │
│  • GamesCatalog / Stats / Profile     │ │  • Mode 2: Vs Computer (5 Tiers)│
│  • PawnCollectionVault / Settings     │ │  • Board & Piece Theme Picker   │
└───────────────────────────────────────┘ └─────────────────┬───────────────┘
                                                            │ Launch Match
        ┌───────────────────┬───────────────────┬───────────┴───────────┐
        ▼                   ▼                   ▼                       ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────────┐   ┌───────────────┐
│Snake & Ladders│   │  Ludo Royal   │   │  Carrom Pro 3D    │   │  Grandmaster  │
│ • 60fps Hop   │   │ • 60fps Glide │   │ • Direct Hand     │   │     Chess     │
│   Interpolator│   │   Interpolator│   │   Baseline Slide  │   │ • Sculpted 3D │
│ • 3D Scales & │   │ • Recessed 3D │   │   & Slingshot Pull│   │   Staunton    │
│   Wood Rungs  │   │   Yards & Star│   │ • 240Hz Substep   │   │   Vector Art  │
│ • Bouncing    │   │   Safe Zones  │   │   Rigid Physics   │   │ • Smooth Piece│
│   3D Tiles    │   │ • Tactical AI │   │ • Rosewood/Netting│   │   Slide Anim  │
└───────────────┘   └───────────────┘   └───────────────────┘   └───────────────┘
```

- **Data Model & State**:
  - `GameLaunchConfig`: Holds `gameType: GameType`, `isVsComputer: Boolean` (Mode 1 Local Room vs. Mode 2 Vs Computer), `playerCount: Int` (2, 3, or 4), `aiDifficulty: AiDifficulty`, `isPracticeOrPuzzle: String?` so the chosen mode in the Pre-Game Lobby initializes the exact player roster and AI behavior upon entering any board.
  - `CarromGestureState`: Tracks `IDLE`, `SLIDING_BASELINE`, and `AIMING_PULLBACK` with `pullStartOffset`, `currentDragOffset`, `aimAngleRad`, and `tensionRatio (0f..1f)`.

- **Interactive Component & State Mapping**:
  - **Pre-Game Lobby Selection**: Tapping a game card sets `pendingGameType` and navigates to `ActiveScreen.GAME_LOBBY`. Selecting *Mode 1: Local Room (Offline)* reveals player count pills (`2 Players`, `3 Players`, `4 Players`); selecting *Mode 2: Vs Computer* reveals the 5 AI difficulty tiers (`Beginner` through `Expert`). Tapping **Start Match** commits `GameLaunchConfig` and transitions to the corresponding game board.
  - **Carrom Slingshot Gesture State Machine**:
    - Touching within the active player's baseline strip or near the striker enters `SLIDING_BASELINE`, updating `striker.x` smoothly in real time while checking for coin overlap.
    - Pulling the finger away from the baseline (> `18.dp` radial distance) transitions to `AIMING_PULLBACK`, locking the striker's baseline `x` position, computing the opposite launch vector `(dx, dy)`, rendering the 3D tension ring and dotted rebound ray, and firing `fireShot()` immediately when the pointer is lifted (`onDragEnd`). Dragging back inside the striker radius before lifting cancels the shot safely.
  - **Smooth 60fps Board Animations**:
    - All four games use `Animatable` and `withFrameMillis` interpolation for piece movement so tokens glide smoothly between coordinates with elevation shadow scaling rather than jumping instantaneously.

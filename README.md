# Chain Reaction - Android Game

A strategic territory control game built with Kotlin and Jetpack Compose where players compete to fill a grid by expanding their colored circles through chain reactions.

## 📱 Features

### Implemented ✅

- **Core Game Mechanics**
  - Grid-based gameplay (5x5, 6x6, 7x7, 8x8, 10x10)
  - Dot placement with automatic explosions
  - Chain reaction system
  - Win condition detection
  - Territory tracking

- **Game Modes**
  - **Local Multiplayer**: Pass-and-play for 2 players
  - **Bot AI**: Play against computer with 3 difficulty levels
    - Easy: Random valid moves
    - Medium: Defensive strategy with territory control
    - Hard: Minimax algorithm with alpha-beta pruning (depth 3)

- **User Interface**
  - Material Design 3
  - Clean, modern UI with rounded components
  - Dynamic color scheme
  - Grid size selection
  - Bot difficulty selection
  - Real-time score tracking
  - Turn indicator

- **Data Persistence**
  - Room database for game statistics
  - Settings storage
  - Game history tracking

- **Architecture**
  - MVVM pattern
  - Clean Architecture layers
  - Hilt dependency injection
  - Kotlin Coroutines
  - StateFlow for reactive UI

### Planned 🚧

- Dot addition animations with scale effects
- Explosion particle effects
- Color transition animations
- Turn pulse effects
- Sound effects
- Haptic feedback
- Victory celebration screen
- Settings screen
- Dark mode enhancements

## 🎮 How to Play

1. **Select Game Mode**: Choose Local Multiplayer or Bot AI
2. **Configure Settings**: 
   - Pick grid size (5x5 to 10x10)
   - Select bot difficulty (if playing vs bot)
3. **Play**:
   - Tap empty cells or your own cells to add dots
   - When a cell reaches its critical mass (2 for corners, 3 for edges, 4 for center), it explodes
   - Explosions spread dots to adjacent cells and capture opponent's cells
   - Chain reactions occur when explosions trigger more explosions
4. **Win**: Control all cells on the board!

## 🏗️ Technical Architecture

### Project Structure
```
app/src/main/java/com/blueedge/chainreaction/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entities/     # Database entities
│   │   └── GameDatabase.kt
│   ├── model/            # Data models
│   └── repository/       # Data repositories
├── domain/
│   └── GameEngine.kt     # Core game logic
├── presentation/
│   ├── main/             # Main menu
│   ├── setup/            # Game setup
│   ├── game/             # Game board & ViewModel
│   │   └── components/   # UI components (Grid, Cells, etc.)
│   ├── end/              # Game over screen
│   ├── theme/            # Material theme
│   └── navigation/       # Navigation setup
├── ai/                   # Bot strategies
└── di/                   # Hilt modules
```

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room
- **Async**: Coroutines + Flow
- **Navigation**: Compose Navigation
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## 🧪 Testing

- Comprehensive unit tests for GameEngine (15 tests)
- Tests cover:
  - Move validation
  - Explosion mechanics
  - Chain reactions
  - Win conditions
  - Edge cases (corners, edges, center cells)

Run tests:
```bash
./gradlew test
```

## 🚀 Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run on device/emulator
./gradlew installDebug
```

## 📦 Dependencies

```kotlin
// Core Android
androidx.core:core-ktx
androidx.lifecycle:lifecycle-runtime-ktx
androidx.activity:activity-compose

// Compose
androidx.compose.material3:material3
androidx.compose.ui:ui
androidx.navigation:navigation-compose

// Hilt
com.google.dagger:hilt-android
androidx.hilt:hilt-navigation-compose

// Room
androidx.room:room-runtime
androidx.room:room-ktx

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android

// DataStore
androidx.datastore:datastore-preferences
```

## 🎯 Game Rules

### Critical Mass
- **Corner cells**: 2 dots
- **Edge cells**: 3 dots
- **Center cells**: 4 dots

### Explosion Mechanics
1. When a cell reaches critical mass, it explodes
2. Each adjacent cell (up, down, left, right) receives 1 dot
3. The exploding cell becomes empty
4. Adjacent cells change to the exploding player's color
5. If any adjacent cell now reaches critical mass, it also explodes (chain reaction)

### Winning
- The first player to control all occupied cells wins
- Minimum 2 cells must be occupied for a win

## 🤖 Bot AI Details

### Easy Bot
- Selects random valid moves
- No strategy
- Good for beginners

### Medium Bot
- Evaluates moves based on multiple factors:
  - Territory gain (30 points per cell)
  - Capturing opponent cells (50 points per captured cell)
  - Blocking opponent's dangerous cells (40 points)
  - Building up own cells (10 points per dot)
  - Strategic positioning (5 points for corners/edges early game)

### Hard Bot
- Uses Minimax algorithm with alpha-beta pruning
- Looks 3 moves ahead
- Evaluation function considers:
  - Territory control
  - Total dots (explosion potential)
  - Cells close to explosion (threat assessment)
  - Strategic positioning
- Very challenging - requires strategic thinking to beat!

## 📝 License

This project is created as part of a development task.

## 👥 Credits

Developed as a comprehensive Android game implementation demonstrating:
- Modern Android development practices
- Clean Architecture principles
- Jetpack Compose UI
- Game AI algorithms
- State management patterns

---

**Note**: This is a complete, functional implementation of Chain Reaction with local multiplayer and intelligent bot opponents. The core gameplay is fully working, with some polish features (animations, sounds) planned for future updates.

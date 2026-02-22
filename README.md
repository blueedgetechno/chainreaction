# Chain Reaction 💥

A classic **strategy board game** for Android — place dots, trigger explosive chain reactions, and be the last player standing!

Built with **Jetpack Compose** and **Material 3**.

<p align="center">
  <img src="app/src/main/res/drawable/banner.jpeg" alt="Chain Reaction Banner" width="600"/>
</p>

## How It Works

Players take turns placing dots on a grid. Each cell has a **critical mass** based on its neighbors. When a cell reaches critical mass, it **explodes** — sending dots to adjacent cells and capturing them for the current player. Explosions can cascade into satisfying **chain reactions** that flip the entire board. The last player remaining wins!

## Features

- **Local Multiplayer** — 2 to 6 players on a single device
- **VS Bot** — Play against an AI opponent with 3 difficulty levels:
  - *Easy* — Random moves
  - *Medium* — Heuristic-based strategy
  - *Hard* — Minimax with alpha-beta pruning
- **Customizable Grid** — 5×5 up to 10×10
- **Explosion Animations** — Wave-by-wave BFS rendering with smooth dot transitions
- **Sound & Music** — Background music, tap/explosion SFX (toggleable)
- **Haptic Feedback** — Vibration on explosions (toggleable)
- **Game Statistics** — End-of-game stats: captured cells, total moves, game duration
- **Custom Fonts** — 4 font options: Default, DynaPuff, Sour Gummy, Comic Relief
- **How to Play** — Built-in tutorial explaining the rules
- **Smooth Transitions** — Fade + scale navigation animations between screens

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose, Material 3, Material Icons Extended |
| **Navigation** | Navigation Compose 2.7.7 |
| **Architecture** | ViewModel + State, Lifecycle-aware Compose |
| **AI** | Minimax with alpha-beta pruning (depth 3) |
| **Audio** | Android `MediaPlayer` / `SoundPool` |
| **Analytics** | Firebase Analytics |
| **Language** | Kotlin |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

## Project Structure

```
app/src/main/java/com/blueedge/chainreaction/
├── ai/                  # Bot strategies (Easy, Medium, Hard)
├── audio/               # Sound & music management
├── data/                # Data models & game config
├── domain/              # Game engine & core logic
├── ui/
│   ├── components/      # Reusable UI components (3D buttons, etc.)
│   ├── screens/         # App screens
│   │   ├── SplashScreen
│   │   ├── MainMenuScreen
│   │   ├── GameSetupScreen
│   │   ├── GameBoardScreen
│   │   ├── GameEndScreen
│   │   ├── SettingsScreen
│   │   └── HowToPlayScreen
│   └── theme/           # Colors, typography, theming
├── utils/               # Constants & helpers
└── MainActivity.kt      # Entry point
```

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 36

### Build & Run

```bash
# Clone the repo
git clone https://github.com/blueedgetechno/ChainReaction.git
cd ChainReaction

# Build and install on a connected device
./gradlew installDebug

# Or build a release AAB
./gradlew bundleRelease
```

## License

Copyright © 2025 Blue Edge. All rights reserved.

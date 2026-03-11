package com.blueedge.chainreaction.data

/**
 * Centralized localization object. All user-visible strings go here.
 * Reads [GameConfig.language] (a Compose mutableStateOf) so every
 * composable that reads a property automatically recomposes when the
 * language changes.
 */
object Strings {

    // ── helper ──────────────────────────────────────────────────────────
    private val translations: Map<String, Map<String, String>> by lazy { buildTranslations() }

    private fun t(key: String): String {
        val lang = GameConfig.language
        return translations[key]?.get(lang)
            ?: translations[key]?.get("English")
            ?: key
    }

    /** Translate an arbitrary key at runtime (e.g. tutorial step keys). */
    fun tr(key: String): String = t(key)

    // ── Main Menu ───────────────────────────────────────────────────────
    val howToPlay: String get() = t("How to Play")
    val playWFriends: String get() = t("PLAY W/ FRIENDS")
    val playWBot: String get() = t("PLAY W/ BOT")
    val playWith: String get() = t("PLAY W/")
    val friends: String get() = t("FRIENDS")
    val bot: String get() = t("BOT")

    // ── Settings ────────────────────────────────────────────────────────
    val settings: String get() = t("Settings")
    val gameSettings: String get() = t("Game Settings")
    val music: String get() = t("Music")
    val sound: String get() = t("Sound")
    val vibration: String get() = t("Vibration")
    val back: String get() = t("Back")
    val restartGame: String get() = t("Restart Game")
    val exitToMenu: String get() = t("Exit to Menu")
    val termsOfService: String get() = t("Terms of Service")
    val privacyPolicy: String get() = t("Privacy Policy")
    val version: String get() = t("Version")
    val madeWithLove: String get() = t("Made with ❤️ by Blue")

    // ── Confirmation dialogs ────────────────────────────────────────────
    val restartGameQ: String get() = t("Restart Game?")
    val restartMessage: String get() = t("Are you sure you want to restart? Your current game progress will be lost.")
    val restart: String get() = t("Restart")
    val exitToMenuQ: String get() = t("Exit to Menu?")
    val exitMessage: String get() = t("Are you sure you want to exit? Your game progress will be lost.")
    val exit: String get() = t("Exit")
    val exitGameQ: String get() = t("Exit Game?")
    val cancel: String get() = t("Cancel")

    // ── Game Setup ──────────────────────────────────────────────────────
    val mode: String get() = t("Mode:")
    val gridSize: String get() = t("Grid Size:")
    val players: String get() = t("Players:")
    val difficulty: String get() = t("Difficulty:")
    val play: String get() = t("Play")
    val simple: String get() = t("Simple")
    val classic: String get() = t("Classic")
    val easy: String get() = t("Easy")
    val medium: String get() = t("Medium")
    val hard: String get() = t("Hard")

    // ── Game Board ──────────────────────────────────────────────────────
    val yourTurn: String get() = t("Your turn")
    val botsTurn: String get() = t("Bot's turn")
    fun playerTurn(colorName: String): String = "$colorName${t("'s turn")}"
    val playAgain: String get() = t("Play Again")
    val menu: String get() = t("Menu")

    // ── Victory / Game End ──────────────────────────────────────────────
    val won: String get() = t("WON!")
    val wins: String get() = t("WINS!")
    val you: String get() = t("You")
    val gameStatistics: String get() = t("Game Statistics")
    val finalScore: String get() = t("Final Score")
    val totalMoves: String get() = t("Total Moves")
    val duration: String get() = t("Duration")
    val gridSizeLabel: String get() = t("Grid Size")
    val botDifficulty: String get() = t("Bot Difficulty")
    val mainMenu: String get() = t("Main Menu")

    // ── How to Play ─────────────────────────────────────────────────────
    val tapToPlace: String get() = t("Tap to Place")
    val tapToPlaceDesc: String get() = t("Tap any cell to place a dot.\nOnce a cell has 4 dots, it explodes!")
    val captureCells: String get() = t("Capture Cells")
    val captureCellsDesc: String get() = t("When your dots explode into a neighbor, you take over that cell — even if it belongs to someone else!")
    val winTheGame: String get() = t("Win the Game")
    val winTheGameDesc: String get() = t("Eliminate every opponent by\ncapturing all their cells. Last player standing wins!")
    val simpleVsClassic: String get() = t("Simple vs Classic")
    val firstMove: String get() = t("First Move")
    val firstMoveSimple: String get() = t("Place 3 dots on any empty cell.")
    val firstMoveClassic: String get() = t("Place 1 dot on any empty cell.")
    val placement: String get() = t("Placement")
    val placementSimple: String get() = t("You can place a dot on your own cells only.")
    val placementClassic: String get() = t("You can place a dot anywhere — on empty cells or your own cells at anytime.")
    val criticalMass: String get() = t("Critical Mass")
    val criticalMassSimple: String get() = t("Every cell explodes at 4 dots, regardless of position.")
    val criticalMassClassic: String get() = t("Corners explode at 2, edges at 3, and interior cells at 4 dots.")
    val explosions: String get() = t("Explosions")
    val explosionsSimple: String get() = t("Exploding cell is emptied completely.")
    val explosionsClassic: String get() = t("Excess dots are preserved — only the critical mass is subtracted.")

    /** Returns the translated color name for a given index. */
    fun colorName(index: Int): String {
        val key = listOf("Blue", "Red", "Green", "Orange", "Purple", "Teal", "Pink", "Brown")
            .getOrElse(index) { "Player ${index + 1}" }
        return t(key)
    }

    // ── Language names (always displayed in their native form) ──────────
    // These are not translated — they stay in the target language for readability.
    val supportedLanguages = listOf("English", "Hindi", "Spanish", "French", "German", "Japanese")

    private val languageNativeNames = mapOf(
        "English" to "English",
        "Hindi" to "Hindi (हिन्दी)",
        "Spanish" to "Spanish (Español)",
        "French" to "French (Français)",
        "German" to "German (Deutsch)",
        "Japanese" to "Japanese (日本語)"
    )

    val supportedLanguageDisplay: List<String>
        get() = supportedLanguages.map { languageNativeNames[it] ?: it }

    fun languageDisplayName(key: String): String = languageNativeNames[key] ?: key

    fun languageKeyFromDisplay(display: String): String =
        languageNativeNames.entries.firstOrNull { it.value == display }?.key ?: display

    // ═══════════════════════════════════════════════════════════════════
    //  TRANSLATION TABLE
    // ═══════════════════════════════════════════════════════════════════
    private fun buildTranslations(): Map<String, Map<String, String>> {
        val m = mutableMapOf<String, Map<String, String>>()
        fun add(key: String, en: String, hi: String, es: String, fr: String, de: String, ja: String) {
            m[key] = mapOf(
                "English" to en,
                "Hindi" to hi,
                "Spanish" to es,
                "French" to fr,
                "German" to de,
                "Japanese" to ja
            )
        }

        // Main Menu
        add("How to Play",        "How to Play",        "कैसे खेलें",         "CÓMO JUGAR",         "COMMENT JOUER",      "SPIELANLEITUNG",     "遊び方")
        add("PLAY W/ FRIENDS",    "PLAY W/ FRIENDS",    "दोस्तों के साथ खेलें", "JUGAR CON AMIGOS",   "JOUER AVEC AMIS",    "MIT FREUNDEN",       "友達と遊ぶ")
        add("PLAY W/ BOT",        "PLAY W/ BOT",        "बॉट के साथ खेलें",    "JUGAR CON BOT",      "JOUER CONTRE BOT",   "GEGEN BOT",          "ボットと遊ぶ")
        add("PLAY W/",            "PLAY W/",            "साथ खेलें",           "JUGAR CON",          "JOUER AVEC",         "SPIELEN MIT",        "と遊ぶ")
        add("FRIENDS",            "FRIENDS",            "दोस्त",              "AMIGOS",             "AMIS",               "FREUNDE",            "友達")
        add("BOT",                "BOT",                "बॉट",                "BOT",                "BOT",                "BOT",                "ボット")

        // Settings
        add("Settings",           "Settings",           "सेटिंग्स",            "Ajustes",            "Paramètres",         "Einstellungen",      "設定")
        add("Game Settings",      "Game Settings",      "गेम सेटिंग्स",        "Ajustes del Juego",  "Paramètres du Jeu",  "Spieleinstellungen", "ゲーム設定")
        add("Music",              "Music",              "संगीत",              "Música",             "Musique",            "Musik",              "音楽")
        add("Sound",              "Sound",              "ध्वनि",              "Sonido",             "Son",                "Sound",              "効果音")
        add("Vibration",          "Vibration",          "कंपन",               "Vibración",          "Vibration",          "Vibration",          "振動")
        add("Back",               "Back",               "वापस",               "Atrás",              "Retour",             "Zurück",             "戻る")
        add("Restart Game",       "Restart Game",       "पुनः आरंभ करें",       "Reiniciar Juego",    "Relancer la Partie", "Spiel Neustarten",   "ゲーム再開")
        add("Exit to Menu",       "Exit to Menu",       "मेनू पर जाएं",        "Salir al Menú",      "Retour au Menu",     "Zum Menü",           "メニューへ戻る")
        add("Terms of Service",   "Terms of Service",   "सेवा की शर्तें",       "Términos de Servicio","Conditions d'Utilisation","Nutzungsbedingungen","利用規約")
        add("Privacy Policy",     "Privacy Policy",     "गोपनीयता नीति",       "Política de Privacidad","Politique de Confidentialité","Datenschutz", "プライバシーポリシー")
        add("Version",            "Version",            "संस्करण",             "Versión",             "Version",            "Version",            "バージョン")

        // Confirmation dialogs
        add("Restart Game?",      "Restart Game?",      "पुनः आरंभ करें?",     "¿Reiniciar Juego?",  "Relancer la Partie ?","Spiel Neustarten?",  "ゲームを再開しますか？")
        add("Are you sure you want to restart? Your current game progress will be lost.",
            "Are you sure you want to restart? Your current game progress will be lost.",
            "क्या आप पुनः आरंभ करना चाहते हैं? आपकी वर्तमान प्रगति खो जाएगी।",
            "¿Estás seguro de que quieres reiniciar? Se perderá tu progreso actual.",
            "Êtes-vous sûr de vouloir relancer ? Votre progression sera perdue.",
            "Möchten Sie wirklich neu starten? Ihr Spielfortschritt geht verloren.",
            "本当に再開しますか？現在のゲームの進行状況が失われます。")
        add("Restart",            "Restart",            "पुनः आरंभ",           "Reiniciar",          "Relancer",           "Neustart",           "再開")
        add("Exit to Menu?",      "Exit to Menu?",      "मेनू पर जाएं?",       "¿Salir al Menú?",    "Retour au Menu ?",   "Zum Menü?",          "メニューへ戻りますか？")
        add("Are you sure you want to exit? Your game progress will be lost.",
            "Are you sure you want to exit? Your game progress will be lost.",
            "क्या आप बाहर जाना चाहते हैं? आपकी गेम प्रगति खो जाएगी।",
            "¿Estás seguro de que quieres salir? Se perderá tu progreso.",
            "Êtes-vous sûr de vouloir quitter ? Votre progression sera perdue.",
            "Möchten Sie wirklich beenden? Ihr Spielfortschritt geht verloren.",
            "本当に終了しますか？ゲームの進行状況が失われます。")
        add("Exit",               "Exit",               "बाहर जाएं",           "Salir",              "Quitter",            "Beenden",            "終了")
        add("Exit Game?",         "Exit Game?",         "गेम से बाहर?",        "¿Salir del Juego?",  "Quitter le Jeu ?",   "Spiel Beenden?",     "ゲームを終了しますか？")
        add("Cancel",             "Cancel",             "रद्द करें",            "Cancelar",           "Annuler",            "Abbrechen",          "キャンセル")

        // Game Setup
        add("Mode:",              "Mode:",              "मोड:",               "Modo:",              "Mode :",             "Modus:",             "モード:")
        add("Grid Size:",         "Grid Size:",         "ग्रिड आकार:",          "Tamaño:",            "Taille :",           "Gittergröße:",       "グリッドサイズ:")
        add("Players:",           "Players:",           "खिलाड़ी:",             "Jugadores:",         "Joueurs :",          "Spieler:",           "プレイヤー:")
        add("Difficulty:",        "Difficulty:",        "कठिनाई:",             "Dificultad:",        "Difficulté :",       "Schwierigkeit:",     "難易度:")
        add("Play",               "Play",               "खेलें",               "Jugar",              "Jouer",              "Spielen",            "プレイ")
        add("Simple",             "Simple",             "सरल",                "Simple",             "Simple",             "Einfach",            "シンプル")
        add("Classic",            "Classic",            "क्लासिक",             "Clásico",            "Classique",          "Klassisch",          "クラシック")
        add("Easy",               "Easy",               "आसान",               "Fácil",              "Facile",             "Leicht",             "かんたん")
        add("Medium",             "Medium",             "मध्यम",              "Medio",              "Moyen",              "Mittel",             "ふつう")
        add("Hard",               "Hard",               "कठिन",               "Difícil",            "Difficile",          "Schwer",             "むずかしい")
        add("Play w/ Friends",    "Play w/ Friends",    "दोस्तों के साथ खेलें", "Jugar con Amigos",   "Jouer avec Amis",    "Mit Freunden Spielen","友達と遊ぶ")
        add("Play w/ Bot",        "Play w/ Bot",        "बॉट के साथ खेलें",    "Jugar con Bot",      "Jouer contre Bot",   "Gegen Bot Spielen",  "ボットと遊ぶ")

        // Game Board
        add("Your turn",          "Your turn",          "आपकी बारी",           "Tu turno",           "Votre tour",         "Dein Zug",           "あなたの番")
        add("Bot's turn",         "Bot's turn",         "बॉट की बारी",         "Turno del Bot",      "Tour du Bot",        "Bot ist dran",       "ボットの番")
        add("'s turn",            "'s turn",            " की बारी",            " turno",             " joue",              " ist dran",          "の番")
        add("Play Again",         "Play Again",         "फिर से खेलें",        "Jugar de Nuevo",     "Rejouer",            "Nochmal Spielen",    "もう一度")
        add("Menu",               "Menu",               "मेनू",               "Menú",               "Menu",               "Menü",               "メニュー")

        // Victory / Game End
        add("WON!",               "WON!",               "जीते!",              "¡GANÓ!",             "GAGNÉ !",            "GEWONNEN!",          "勝利！")
        add("WINS!",              "WINS!",              "जीता!",              "¡GANA!",             "GAGNE !",            "GEWINNT!",           "勝利！")
        add("You",                "You",                "आप",                 "Tú",                 "Vous",               "Du",                 "あなた")
        add("Game Statistics",    "Game Statistics",    "गेम आंकड़े",          "Estadísticas",       "Statistiques",       "Spielstatistiken",   "ゲーム統計")
        add("Final Score",        "Final Score",        "अंतिम स्कोर",         "Puntuación Final",   "Score Final",        "Endstand",           "最終スコア")
        add("Total Moves",        "Total Moves",        "कुल चालें",           "Movimientos Totales","Coups Totaux",       "Gesamtzüge",         "合計手数")
        add("Duration",           "Duration",           "अवधि",               "Duración",           "Durée",              "Dauer",              "プレイ時間")
        add("Grid Size",          "Grid Size",          "ग्रिड आकार",          "Tamaño de Cuadrícula","Taille de Grille",  "Gittergröße",        "グリッドサイズ")
        add("Bot Difficulty",     "Bot Difficulty",     "बॉट कठिनाई",         "Dificultad del Bot", "Difficulté du Bot",  "Bot Schwierigkeit",  "ボットの難易度")
        add("Main Menu",          "Main Menu",          "मुख्य मेनू",          "Menú Principal",     "Menu Principal",     "Hauptmenü",          "メインメニュー")

        // How to Play
        add("How to Play",        "How to Play",        "कैसे खेलें",          "Cómo Jugar",         "Comment Jouer",      "Spielanleitung",     "遊び方")
        add("Tap to Place",       "Tap to Place",       "रखने के लिए टैप करें",  "Toca para Colocar",  "Touchez pour Placer","Tippen zum Setzen",  "タップして置く")
        add("Tap any cell to place a dot.\nOnce a cell has 4 dots, it explodes!",
            "Tap any cell to place a dot.\nOnce a cell has 4 dots, it explodes!",
            "डॉट रखने के लिए किसी भी सेल पर टैप करें।\nएक बार सेल में 4 डॉट हो जाने पर, वह फट जाता है!",
            "Toca cualquier celda para colocar un punto.\nCuando una celda tiene 4 puntos, ¡explota!",
            "Touchez une case pour placer un point.\nQuand une case a 4 points, elle explose !",
            "Tippe auf eine Zelle, um einen Punkt zu setzen.\nBei 4 Punkten explodiert die Zelle!",
            "セルをタップしてドットを置きます。\nセルに4つのドットが溜まると爆発します！")
        add("Capture Cells",      "Capture Cells",      "सेल कैपचर करें",      "Capturar Celdas",    "Capturer des Cases", "Zellen Erobern",     "セルを奪う")
        add("When your dots explode into a neighbor, you take over that cell — even if it belongs to someone else!",
            "When your dots explode into a neighbor, you take over that cell — even if it belongs to someone else!",
            "जब आपके डॉट पड़ोसी में फटते हैं, तो आप उस सेल पर कब्जा कर लेते हैं — भले ही वह किसी और की हो!",
            "Cuando tus puntos explotan hacia un vecino, ¡te apoderas de esa celda, aunque pertenezca a otro!",
            "Quand vos points explosent vers un voisin, vous prenez cette case — même si elle appartient à quelqu'un d'autre !",
            "Wenn deine Punkte in eine Nachbarzelle explodieren, übernimmst du sie — auch wenn sie jemand anderem gehört!",
            "ドットが隣のセルに爆発すると、そのセルを奪えます — 相手のセルでも！")
        add("Win the Game",        "Win the Game",       "गेम जीतें",           "Ganar el Juego",     "Gagner la Partie",   "Spiel Gewinnen",     "ゲームに勝つ")
        add("Eliminate every opponent by\ncapturing all their cells. Last player standing wins!",
            "Eliminate every opponent by\ncapturing all their cells. Last player standing wins!",
            "सभी विरोधियों को हटाएं\nउनकी सभी सेल कैपचर करके। अंतिम खिलाड़ी जीतता है!",
            "Elimina a todos los oponentes\ncapturando todas sus celdas. ¡El último en pie gana!",
            "Éliminez chaque adversaire en\ncapturant toutes ses cases. Le dernier joueur debout gagne !",
            "Besiege jeden Gegner, indem du\nalle seine Zellen eroberst. Der letzte Spieler gewinnt!",
            "すべての対戦相手を排除して\nすべてのセルを奪いましょう。最後に残ったプレイヤーの勝ちです！")

        // How to Play – Simple vs Classic
        add("Simple vs Classic",   "Simple vs Classic",  "सरल बनाम क्लासिक",    "Simple vs Clásico",  "Simple vs Classique","Einfach vs Klassisch","シンプル vs クラシック")
        add("First Move",          "First Move",         "पहली चाल",            "Primer Movimiento",  "Premier Coup",       "Erster Zug",         "最初の手")
        add("Place 3 dots on any empty cell.",
            "Place 3 dots on any empty cell.",
            "किसी भी खाली सेल पर 3 डॉट रखें।",
            "Coloca 3 puntos en cualquier celda vacía.",
            "Placez 3 points sur une case vide.",
            "Setze 3 Punkte auf eine leere Zelle.",
            "空いているセルに3つのドットを置きます。")
        add("Place 1 dot on any empty cell.",
            "Place 1 dot on any empty cell.",
            "किसी भी खाली सेल पर 1 डॉट रखें।",
            "Coloca 1 punto en cualquier celda vacía.",
            "Placez 1 point sur une case vide.",
            "Setze 1 Punkt auf eine leere Zelle.",
            "空いているセルに1つのドットを置きます。")
        add("Placement",           "Placement",          "प्लेसमेंट",            "Colocación",         "Placement",          "Platzierung",        "配置")
        add("You can place a dot on your own cells only.",
            "You can place a dot on your own cells only.",
            "आप केवल अपनी सेल पर डॉट रख सकते हैं।",
            "Solo puedes colocar un punto en tus propias celdas.",
            "Vous ne pouvez placer un point que sur vos propres cases.",
            "Du kannst nur auf eigene Zellen setzen.",
            "自分のセルにのみドットを置けます。")
        add("You can place a dot anywhere — on empty cells or your own cells at anytime.",
            "You can place a dot anywhere — on empty cells or your own cells at anytime.",
            "आप कहीं भी डॉट रख सकते हैं — खाली सेल या अपनी सेल पर कभी भी।",
            "Puedes colocar un punto en cualquier lugar — en celdas vacías o propias en cualquier momento.",
            "Vous pouvez placer un point n'importe où — cases vides ou les vôtres, à tout moment.",
            "Du kannst überall setzen — auf leere oder eigene Zellen, jederzeit.",
            "空いているセルや自分のセルにいつでもドットを置けます。")
        add("Critical Mass",       "Critical Mass",      "क्रिटिकल मास",        "Masa Crítica",       "Masse Critique",     "Kritische Masse",    "臨界量")
        add("Every cell explodes at 4 dots, regardless of position.",
            "Every cell explodes at 4 dots, regardless of position.",
            "हर सेल 4 डॉट पर फटती है, स्थिति की परवाह किए बिना।",
            "Cada celda explota con 4 puntos, sin importar la posición.",
            "Chaque case explose à 4 points, quelle que soit sa position.",
            "Jede Zelle explodiert bei 4 Punkten, unabhängig von der Position.",
            "すべてのセルは位置に関係なく4つのドットで爆発します。")
        add("Corners explode at 2, edges at 3, and interior cells at 4 dots.",
            "Corners explode at 2, edges at 3, and interior cells at 4 dots.",
            "कोने 2, किनारे 3, और आंतरिक सेल 4 डॉट पर फटते हैं।",
            "Las esquinas explotan con 2, los bordes con 3 y las celdas interiores con 4 puntos.",
            "Les coins explosent à 2, les bords à 3 et les cases intérieures à 4 points.",
            "Ecken explodieren bei 2, Kanten bei 3 und innere Zellen bei 4 Punkten.",
            "角は2、辺は3、内側のセルは4つのドットで爆発します。")
        add("Explosions",          "Explosions",         "विस्फोट",             "Explosiones",        "Explosions",         "Explosionen",        "爆発")
        add("Exploding cell is emptied completely.",
            "Exploding cell is emptied completely.",
            "फटने वाली सेल पूरी तरह खाली हो जाती है।",
            "La celda que explota se vacía completamente.",
            "La case qui explose est vidée complètement.",
            "Die explodierende Zelle wird vollständig geleert.",
            "爆発したセルは完全に空になります。")
        add("Excess dots are preserved — only the critical mass is subtracted.",
            "Excess dots are preserved — only the critical mass is subtracted.",
            "अतिरिक्त डॉट संरक्षित रहते हैं — केवल क्रिटिकल मास घटाया जाता है।",
            "Los puntos sobrantes se conservan — solo se resta la masa crítica.",
            "Les points excédentaires sont conservés — seule la masse critique est soustraite.",
            "Überschüssige Punkte bleiben erhalten — nur die kritische Masse wird abgezogen.",
            "余分なドットは保持されます — 臨界量のみ差し引かれます。")

        // Footer
        add("Made with ❤️ by Blue",
            "Made with ❤️ by Blue",
            "❤️ से Blue द्वारा बनाया गया",
            "Hecho con ❤️ por Blue",
            "Fait avec ❤️ par Blue",
            "Mit ❤️ von Blue",
            "❤️ を込めて Blue が作りました")

        // Player color names
        add("Blue",    "Blue",    "नीला",    "Azul",      "Bleu",    "Blau",   "青")
        add("Red",     "Red",     "लाल",     "Rojo",      "Rouge",   "Rot",    "赤")
        add("Green",   "Green",   "हरा",     "Verde",     "Vert",    "Grün",   "緑")
        add("Orange",  "Orange",  "नारंगी",   "Naranja",   "Orange",  "Orange", "オレンジ")
        add("Purple",  "Purple",  "बैंगनी",   "Morado",    "Violet",  "Lila",   "紫")
        add("Teal",    "Teal",    "टील",     "Verde Azulado","Sarcelle","Türkis","ティール")
        add("Pink",    "Pink",    "गुलाबी",   "Rosa",      "Rose",    "Rosa",   "ピンク")
        add("Brown",   "Brown",   "भूरा",     "Marrón",    "Marron",  "Braun",  "茶色")

        // Player fallback
        add("Player",  "Player",  "खिलाड़ी",  "Jugador",   "Joueur",  "Spieler","プレイヤー")

        return m
    }
}

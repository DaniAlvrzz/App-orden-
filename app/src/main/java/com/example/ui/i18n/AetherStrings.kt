package com.example.ui.i18n

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    SPANISH("es", "Español (Castellano)", "🇪🇸"),
    ENGLISH("en", "English", "🇬🇧")
}

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val summary: String,
    val bulletPoints: List<String>,
    val bioPrinciple: String,
    val targetTab: Int? = null
)

class StringsProvider(val language: AppLanguage) {

    // General & Navigation
    val appTitle: String get() = "AETHER"
    val appSubtitle: String get() = if (language == AppLanguage.SPANISH) "Motor de Gestión de Vida" else "Life Management Engine"
    val tabNexus: String get() = if (language == AppLanguage.SPANISH) "Nexus" else "Nexus"
    val tabBacklog: String get() = if (language == AppLanguage.SPANISH) "Bandeja" else "Backlog"
    val tabNutrition: String get() = if (language == AppLanguage.SPANISH) "Nutrición" else "Nutrition"
    val tabNutritionNav: String get() = if (language == AppLanguage.SPANISH) "Nutrición" else "Nutrition"
    val tabHabits: String get() = if (language == AppLanguage.SPANISH) "Hábitos" else "Habits"
    val tabAi: String get() = if (language == AppLanguage.SPANISH) "Núcleo IA" else "Aether AI"
    
    val btnOrchestrate: String get() = if (language == AppLanguage.SPANISH) "Orquestar" else "Orchestrate"
    val btnOrchestrating: String get() = if (language == AppLanguage.SPANISH) "Alineando..." else "Aligning..."
    val btnAligning: String get() = if (language == AppLanguage.SPANISH) "Alineando..." else "Aligning..."
    val btnSettings: String get() = if (language == AppLanguage.SPANISH) "Ajustes" else "Settings"
    val btnTutorial: String get() = if (language == AppLanguage.SPANISH) "Tutorial" else "Tutorial"
    val btnHelp: String get() = if (language == AppLanguage.SPANISH) "Guía del Sistema" else "System Guide"
    val btnClose: String get() = if (language == AppLanguage.SPANISH) "Cerrar" else "Close"
    val btnSave: String get() = if (language == AppLanguage.SPANISH) "Guardar" else "Save"
    val btnCancel: String get() = if (language == AppLanguage.SPANISH) "Cancelar" else "Cancel"
    val btnDelete: String get() = if (language == AppLanguage.SPANISH) "Eliminar" else "Delete"
    val btnCapture: String get() = if (language == AppLanguage.SPANISH) "Capturar" else "Capture"
    val btnReframe: String get() = if (language == AppLanguage.SPANISH) "Reencuadre" else "Reframe"
    val btnNext: String get() = if (language == AppLanguage.SPANISH) "Siguiente" else "Next"
    val btnPrev: String get() = if (language == AppLanguage.SPANISH) "Anterior" else "Previous"
    val btnFinishTutorial: String get() = if (language == AppLanguage.SPANISH) "Comenzar a usar Aether OS" else "Start using Aether OS"
    val btnExploreSection: String get() = if (language == AppLanguage.SPANISH) "Explorar este Menú" else "Explore this Menu"
    val btnAddTask: String get() = if (language == AppLanguage.SPANISH) "Añadir Tarea" else "Add Task"
    val btnAddTimeBlock: String get() = if (language == AppLanguage.SPANISH) "Añadir Bloque" else "Add Time Block"
    val btnFocusTimer: String get() = if (language == AppLanguage.SPANISH) "Temporizador Focus" else "Focus Timer"
    val btnPauseTimer: String get() = if (language == AppLanguage.SPANISH) "Pausar" else "Pause"
    val btnStartTimer: String get() = if (language == AppLanguage.SPANISH) "Iniciar Focus" else "Engage Focus"
    val btnResetTimer: String get() = if (language == AppLanguage.SPANISH) "Reiniciar" else "Reset"
    val btnApplyGrace: String get() = if (language == AppLanguage.SPANISH) "Usar Grace Day" else "Apply Grace Day"
    val btnReplan: String get() = if (language == AppLanguage.SPANISH) "Re-Planificar" else "Re-Plan"
    val btnGenerateReframe: String get() = if (language == AppLanguage.SPANISH) "Reencuadrar con IA" else "Generate AI Reframe"
    val btnAddPantry: String get() = if (language == AppLanguage.SPANISH) "Añadir a Despensa" else "Add Item"

    // Nexus Screen
    val engineSub: String get() = if (language == AppLanguage.SPANISH) "Motor de Gestión de Vida" else "Life Management Engine"
    val nexusHeaderSub: String get() = if (language == AppLanguage.SPANISH) 
        "Centro de Mando Circadiano • Cero Fatiga de Decisión" 
    else 
        "Circadian Command Hub • Zero Decision Fatigue"
    
    val recoveryBannerTitle: String get() = if (language == AppLanguage.SPANISH) 
        "PROTOCOLO DE RECUPERACIÓN ACTIVADO" 
    else 
        "RECOVERY MODE PROTOCOL ENGAGED"
        
    val recoveryBannerDesc: String get() = if (language == AppLanguage.SPANISH) 
        "Nivel biológico < 60%. Tareas Tipo A (Frog) pausadas. Enfoque exclusivo en recuperación y micro-tareas." 
    else 
        "Biological score < 60%. Type A (Frog) tasks paused. Exclusive focus on gentle recovery and low-effort logistics."

    val recoveryBannerSub: String get() = recoveryBannerDesc

    val readinessTitle: String get() = if (language == AppLanguage.SPANISH) "PREPARACIÓN BIOMÉTRICA" else "BIOMETRIC READINESS"
    val readinessScoreLabel: String get() = if (language == AppLanguage.SPANISH) "Puntuación de Energía" else "Energy Score"
    val chronotypeLabel: String get() = if (language == AppLanguage.SPANISH) "Cronotipo" else "Chronotype"
    val chronotypeSelectorTitle: String get() = if (language == AppLanguage.SPANISH) "ALINEACIÓN DE CRONOTIPO" else "CHRONOTYPE ALIGNMENT"
    val systemModeLabel: String get() = if (language == AppLanguage.SPANISH) "Modo del Sistema" else "System Mode"
    val modeHighPerf: String get() = if (language == AppLanguage.SPANISH) "Alto Rendimiento" else "High Performance"
    val modeRecovery: String get() = if (language == AppLanguage.SPANISH) "Recuperación" else "Recovery Mode"
    val recoveryModeChipActive: String get() = if (language == AppLanguage.SPANISH) "Recuperación ACTIVA" else "Recovery ON"
    val recoveryModeChipInactive: String get() = if (language == AppLanguage.SPANISH) "Cambiar a Recuperación" else "Shift to Recovery"
    
    val chronoLion: String get() = if (language == AppLanguage.SPANISH) "León (Madrugador - Pico 08:00)" else "Lion (Early Bird - Peak 08:00)"
    val chronoBear: String get() = if (language == AppLanguage.SPANISH) "Oso (Solar - Pico 10:00-14:00)" else "Bear (Solar Peak 10:00-14:00)"
    val chronoWolf: String get() = if (language == AppLanguage.SPANISH) "Lobo (Vespertino - Pico 17:00)" else "Wolf (Night Owl - Peak 17:00)"
    val chronoDolphin: String get() = if (language == AppLanguage.SPANISH) "Delfín (Sensible - Pico 15:00)" else "Dolphin (Sensitive - Peak 15:00)"

    val circadianCurveTitle: String get() = if (language == AppLanguage.SPANISH) "CURVA DE ENERGÍA CIRCADIANA" else "CIRCADIAN ENERGY CURVE"
    val circadianCurveSub: String get() = if (language == AppLanguage.SPANISH) "Alineación de tareas según picos hormonales y cronobiología" else "Task alignment based on hormonal peaks & chronobiology"
    val peakHoursPrefix: String get() = if (language == AppLanguage.SPANISH) "Pico" else "Peak"
    val bioReadyLabel: String get() = if (language == AppLanguage.SPANISH) "Bio-Listo" else "Bio-Ready"
    
    // Cognitive Ceiling Gauge
    val cognitiveCeilingTitle: String get() = if (language == AppLanguage.SPANISH) "LEY DEL TECHO COGNITIVO" else "COGNITIVE CEILING LAW"
    val cognitiveCeilingSub: String get() = if (language == AppLanguage.SPANISH) "Límite neurobiológico estricto: Máximo 3.5h (210 min) al día" else "Strict neurobiological limit: Maximum 3.5h (210 min) per day"
    val ceilingMaxSuffix: String get() = if (language == AppLanguage.SPANISH) "Máx" else "Max"
    val ceilingExceededWarning: String get() = if (language == AppLanguage.SPANISH) "Sobrecarga cognitiva detectada (>3.5h). Ley de energía violada: pasa a recuperación." else "Cognitive overload detected (>3.5h). Energy law violated: downshift to recovery."
    val ceilingNormalInfo: String get() = if (language == AppLanguage.SPANISH) "Deep Work limitado a 3.5h/día para evitar fatiga neuronal y burnout." else "Deep work capped at 3.5h/day to prevent neural fatigue & burnout."
    val ceilingSafeStatus: String get() = if (language == AppLanguage.SPANISH) "Capacidad dentro del umbral saludable" else "Capacity within healthy biological threshold"
    val ceilingWarningStatus: String get() = if (language == AppLanguage.SPANISH) "¡Sobrecarga cognitiva detectada! Reduce bloques de Deep Work" else "Cognitive overload detected! Reduce Deep Work blocks"
    val minutesAllocated: String get() = if (language == AppLanguage.SPANISH) "min asignados" else "min allocated"
    val remainingCapacity: String get() = if (language == AppLanguage.SPANISH) "minutos de reserva restante" else "minutes remaining buffer"

    // 1-3-5 Priority Matrix
    val priorityMatrixTitle: String get() = if (language == AppLanguage.SPANISH) "Matriz de Prioridades 1-3-5" else "1-3-5 Priorities Matrix"
    val prioritiesMatrixTitle: String get() = if (language == AppLanguage.SPANISH) "PRIORIDADES BIOMÉTRICAS 1-3-5" else "1-3-5 BIOMETRIC PRIORITIES"
    val priorityMatrixSub: String get() = if (language == AppLanguage.SPANISH) "1 Tarea Frog (Alta Demanda) • 3 Medias • 5 Micro-victorias" else "1 Frog Task (High Demand) • 3 Medium • 5 Quick Wins"
    val frogSectionTitle: String get() = if (language == AppLanguage.SPANISH) "🐸 TAREA FROG (MÁX. 1 AL DÍA - TIPO A)" else "🐸 THE FROG (MAX 1 PER DAY - TYPE A)"
    val mediumSectionTitle: String get() = if (language == AppLanguage.SPANISH) "⚡ TAREAS DE DEMANDA MEDIA (TIPO B - MÁX 3)" else "⚡ MEDIUM DEMAND TASKS (TYPE B - MAX 3)"
    val quickSectionTitle: String get() = if (language == AppLanguage.SPANISH) "✨ MICRO-VICTORIAS & LOGÍSTICA (TIPO C - HASTA 5)" else "✨ QUICK WINS & LOGISTICS (TYPE C - UP TO 5)"
    val mediumTasksHeader: String get() = if (language == AppLanguage.SPANISH) "3 TAREAS DE DEMANDA MEDIA (TIPO B)" else "3 MEDIUM DEMAND TASKS (TYPE B)"
    val quickWinsHeader: String get() = if (language == AppLanguage.SPANISH) "5 MICRO-VICTORIAS / TRIAJE (TIPO C)" else "5 QUICK WINS / MICRO-ADMIN (TYPE C)"
    val recoveryZeroFrogsTitle: String get() = if (language == AppLanguage.SPANISH) "PROTOCOLO: CERO FROGS HOY" else "PROTOCOL: ZERO FROGS TODAY"
    val recoveryZeroFrogsDesc: String get() = if (language == AppLanguage.SPANISH) "Preparación < 60. Las tareas Tipo A de alta demanda se eliminan para prevenir agotamiento suprarrenal." else "Readiness < 60. High-demand Type A tasks are eliminated to prevent adrenal exhaustion."
    val frogBadge: String get() = if (language == AppLanguage.SPANISH) "🔥 FROG DE HOY (TIPO A)" else "🔥 TODAY'S 1 FROG (TYPE A)"
    val deepFocusSuffix: String get() = if (language == AppLanguage.SPANISH) "Enfoque Profundo" else "Deep Focus"
    val assignFrogPlaceholder: String get() = if (language == AppLanguage.SPANISH) "Asignar la 1 Tarea Frog de Hoy (Tipo A)" else "Assign Today's 1 Frog Task (Type A)"
    val frogPausedNotice: String get() = if (language == AppLanguage.SPANISH) "Modo Recuperación: Tarea Frog pausada para evitar estrés suprarrenal." else "Recovery Mode: Frog task paused to prevent adrenal strain."
    val btnStartFocus: String get() = if (language == AppLanguage.SPANISH) "Iniciar Focus" else "Start Focus"

    // Time Block Timeline
    val timelineTitle: String get() = if (language == AppLanguage.SPANISH) "Línea de Tiempo Circadiana" else "Circadian Time Blocks"
    val timeBlocksTitle: String get() = if (language == AppLanguage.SPANISH) "BLOQUES DE TIEMPO CIRCADIANOS" else "CIRCADIAN TIME BLOCKS"
    val timelineSub: String get() = if (language == AppLanguage.SPANISH) "Bloques estructurados sin fricción de decisión" else "Frictionless structured blocks aligned to energy"
    val btnAddBlock: String get() = if (language == AppLanguage.SPANISH) "+ Añadir Bloque" else "+ Add Block"
    val emptyTimeBlocks: String get() = if (language == AppLanguage.SPANISH) "No hay bloques programados. Toca 'Orquestar' para generar el horario circadiano." else "No time blocks scheduled. Tap 'Orchestrate' to generate circadian schedule."

    // Backlog Screen
    val backlogHeader: String get() = if (language == AppLanguage.SPANISH) "BANDEJA DE ENERGÍA" else "ENERGY BACKLOG"
    val backlogSub: String get() = if (language == AppLanguage.SPANISH) "Captura sin fricción • Filtra tareas por tu capacidad biológica actual" else "Zero-friction capture • Match tasks to biological capacity"
    val backlogHeaderTitle: String get() = backlogHeader
    val backlogHeaderSub: String get() = backlogSub
    
    val searchPlaceholder: String get() = if (language == AppLanguage.SPANISH) "Buscar en la bandeja..." else "Filter backlog tasks..."
    val searchTasksPlaceholder: String get() = searchPlaceholder
    val filterAll: String get() = if (language == AppLanguage.SPANISH) "Todas" else "All"
    val filterHigh: String get() = if (language == AppLanguage.SPANISH) "Alta Energía (Tipo A)" else "High Energy (Type A)"
    val filterMed: String get() = if (language == AppLanguage.SPANISH) "Media (Tipo B)" else "Medium (Type B)"
    val filterLow: String get() = if (language == AppLanguage.SPANISH) "Baja (Tipo C / Micro)" else "Low (Type C / Micro)"
    val filterAllEnergy: String get() = filterAll
    val filterHighEnergy: String get() = filterHigh
    val filterMedEnergy: String get() = filterMed
    val filterLowEnergy: String get() = filterLow
    val emptyBacklog: String get() = if (language == AppLanguage.SPANISH) "No hay tareas en esta categoría de energía." else "No tasks matching this energy filter."
    
    val focusTimerTitle: String get() = if (language == AppLanguage.SPANISH) "TEMPORIZADOR FOCUS (DEEP WORK)" else "DEEP WORK FOCUS TIMER"
    val focusProtocolTitle: String get() = if (language == AppLanguage.SPANISH) "PROTOCOLO DE ENFOQUE PROFUNDO" else "DEEP FOCUS PROTOCOL"
    val focusDefaultTask: String get() = if (language == AppLanguage.SPANISH) "Bloque de Trabajo Profundo Monotarea" else "Single-Tasking Deep Work Block"
    val focusTimerActive: String get() = if (language == AppLanguage.SPANISH) "Sesión Activa:" else "Active Session:"
    val focusTimerIdle: String get() = if (language == AppLanguage.SPANISH) "Listo para sesión de concentración" else "Ready for deep focus session"
    val btnTimerStart: String get() = if (language == AppLanguage.SPANISH) "Iniciar" else "Start"
    val btnTimerPause: String get() = if (language == AppLanguage.SPANISH) "Pausar" else "Pause"
    val btnTimerReset: String get() = if (language == AppLanguage.SPANISH) "Reiniciar (25m)" else "Reset (25m)"
    
    val btnMakeFrog: String get() = if (language == AppLanguage.SPANISH) "Convertir en Frog" else "Set as Frog"

    // Nutrition Screen
    val nutritionHeader: String get() = if (language == AppLanguage.SPANISH) "NUTRICIÓN RELACIONAL" else "RELATIONAL NUTRITION"
    val nutritionSub: String get() = if (language == AppLanguage.SPANISH) "Bases de Batch Cooking • Cero desperdicio • Combustible sin fatiga" else "Batch cooking bases • Zero waste • Frictionless fuel"
    val nutritionHeaderTitle: String get() = nutritionHeader
    val nutritionHeaderSub: String get() = nutritionSub
        
    val tabDailyMeals: String get() = if (language == AppLanguage.SPANISH) "Comidas Diarias" else "Daily Meals"
    val tabPantry: String get() = if (language == AppLanguage.SPANISH) "Despensa & Stock" else "Pantry & Stock"
    val tabBatchBases: String get() = if (language == AppLanguage.SPANISH) "Bases Batch Cooking" else "Batch Bases"
    val tabNutritionMeals: String get() = tabDailyMeals
    val tabNutritionPantry: String get() = tabPantry
    val tabNutritionBatch: String get() = tabBatchBases
    val btnAddPantryItem: String get() = if (language == AppLanguage.SPANISH) "Añadir a Despensa" else "Stock Item"
    val shoppingListTitle: String get() = if (language == AppLanguage.SPANISH) "LISTA DE LA COMPRA / STOCK AGOTADO" else "SHOPPING LIST / MISSING STOCK"
    val batchCookTitle: String get() = if (language == AppLanguage.SPANISH) "SISTEMA DE BASES BATCH COOKING" else "BATCH COOKING SYSTEM BASES"
    val batchCookDesc: String get() = if (language == AppLanguage.SPANISH) "Prepara estas bases 1 o 2 veces por semana para armar platos en menos de 8 minutos con cero fatiga de decisión." else "Prepare these bases 1-2x weekly to assemble meals in under 8 minutes with zero decision fatigue."
    val badgeBatchBase: String get() = if (language == AppLanguage.SPANISH) "⚡ Base Batch" else "⚡ Batch Base"
    val prepMinutesSuffix: String get() = if (language == AppLanguage.SPANISH) "prep" else "prep"
    val inStockLabel: String get() = if (language == AppLanguage.SPANISH) "En Stock" else "In Stock"
    val neededLabel: String get() = if (language == AppLanguage.SPANISH) "Reponer" else "Need Stock"
    
    val slotBreakfast: String get() = if (language == AppLanguage.SPANISH) "Desayuno" else "Breakfast"
    val slotLunch: String get() = if (language == AppLanguage.SPANISH) "Almuerzo / Comida" else "Lunch"
    val slotDinner: String get() = if (language == AppLanguage.SPANISH) "Cena" else "Dinner"
    val slotSnack: String get() = if (language == AppLanguage.SPANISH) "Snack Estratégico" else "Strategic Snack"
    
    val prepTimeLabel: String get() = if (language == AppLanguage.SPANISH) "min prep" else "min prep"
    val batchCookingBaseTag: String get() = if (language == AppLanguage.SPANISH) "Base Batch Cocinada" else "Batch Cooked Base"
    val allStockAvailable: String get() = if (language == AppLanguage.SPANISH) "Ingredientes disponibles en despensa" else "All ingredients in stock"
    val missingStockWarning: String get() = if (language == AppLanguage.SPANISH) "Faltan ingredientes por reponer" else "Ingredients need restocking"
    
    val glycemicLow: String get() = if (language == AppLanguage.SPANISH) "Bajo Índice Glucémico (Dopamina Estable)" else "Low Glycemic Focus (Stable Dopamine)"
    val glycemicModerate: String get() = if (language == AppLanguage.SPANISH) "Energía Sostenida" else "Moderate Steady Energy"
    val glycemicRecovery: String get() = if (language == AppLanguage.SPANISH) "Recuperación Parasimpática" else "Deep Parasympathetic Recovery"

    // Habits Screen
    val habitsHeader: String get() = if (language == AppLanguage.SPANISH) "ANCLAJES CIRCADIANOS" else "CIRCADIAN HABIT ANCHORS"
    val habitsSub: String get() = if (language == AppLanguage.SPANISH) "Disciplina sin culpa • Anclado a la biología, no a la fuerza de voluntad" else "Guilt-free consistency • Anchored to biology, not willpower"
    val habitsHeaderTitle: String get() = habitsHeader
    val habitsHeaderSub: String get() = habitsSub
    val anchorsCountLabel: String get() = if (language == AppLanguage.SPANISH) "Anclajes Hoy" else "Anchors Today"
    val graceDaysActiveLabel: String get() = if (language == AppLanguage.SPANISH) "Grace Days Usados" else "Grace Days Used"
    val guiltFreeMetricLabel: String get() = if (language == AppLanguage.SPANISH) "Sin Culpa" else "Guilt-Free"
    val graceDayLawTitle: String get() = if (language == AppLanguage.SPANISH) "LEY DE LOS GRACE DAYS & ELASTICIDAD" else "GRACE DAY LAW & ELASTICITY"
    val graceDayLawDesc: String get() = if (language == AppLanguage.SPANISH) "La consistencia biológica es un patrón de retorno, no una cadena perfecta. Usar un Grace Day protege tu racha e identidad sin auto-castigo." else "Biological consistency is a pattern of return, not perfection. Using a Grace Day protects your streak and identity without self-criticism."
    val streakDaysSuffix: String get() = if (language == AppLanguage.SPANISH) "d racha" else "d streak"
    val graceTagSuffix: String get() = if (language == AppLanguage.SPANISH) "Grace" else "Grace"
        
    val habitsCompletedLabel: String get() = if (language == AppLanguage.SPANISH) "Completados Hoy" else "Completed Today"
    val graceTokensUsedLabel: String get() = if (language == AppLanguage.SPANISH) "Tokens Grace Days usados" else "Grace Tokens used"
    val btnUseGraceDay: String get() = if (language == AppLanguage.SPANISH) "Activar Grace Day" else "Apply Grace Day"
    val graceDayDesc: String get() = if (language == AppLanguage.SPANISH) 
        "Protege tu racha sin culpa. La disciplina compasiva sostiene la identidad a largo plazo." 
    else 
        "Protects your streak without guilt. Compassionate discipline maintains identity."
    val streakDaysLabel: String get() = if (language == AppLanguage.SPANISH) "días de racha" else "days streak"
    val elasticTargetsLabel: String get() = if (language == AppLanguage.SPANISH) "Metas Elásticas:" else "Elastic Targets:"
    val elasticMini: String get() = if (language == AppLanguage.SPANISH) "Mini (Días difíciles)" else "Mini (Hard days)"
    val elasticPlus: String get() = if (language == AppLanguage.SPANISH) "Plus (Estándar)" else "Plus (Standard)"
    val elasticElite: String get() = if (language == AppLanguage.SPANISH) "Élite (Alta energía)" else "Elite (Peak energy)"

    // AI Screen
    val aiHeader: String get() = if (language == AppLanguage.SPANISH) "NÚCLEO IA & ORQUESTADOR" else "AETHER AI CORE & ORCHESTRATION"
    val aiSub: String get() = if (language == AppLanguage.SPANISH) "Orquestación circadiana inteligente • Esquema JSON de vida" else "Intelligent circadian orchestration • Life JSON schema"
    val aiHeaderTitle: String get() = aiHeader
    val aiHeaderSub: String get() = aiSub
    val operatingLawsTitle: String get() = if (language == AppLanguage.SPANISH) "LEYES OPERATIVAS DEL NÚCLEO AETHER" else "AETHER OPERATIONAL LAWS"
    val law1: String get() = if (language == AppLanguage.SPANISH) "1. Elasticidad Conductual: Se adapta a tu fatiga o energía sin juzgar." else "1. Behavioral Elasticity: Adapts to your fatigue or energy without judgment."
    val law2: String get() = if (language == AppLanguage.SPANISH) "2. Respeto Bioenergético: Organiza por picos hormonales, no por huecos libres." else "2. Bioenergetic Respect: Schedules by hormonal peaks, not free slots."
    val law3: String get() = if (language == AppLanguage.SPANISH) "3. Anti-Fatiga de Mantenimiento: Captura instantánea en 1 toque. Cero burocracia." else "3. Anti-Maintenance Fatigue: Instant 1-tap capture. Zero bureaucracy."
    val law4: String get() = if (language == AppLanguage.SPANISH) "4. Disciplina sin Culpa: Grace Days y reencuadres cognitivos ante fallos." else "4. Guilt-Free Discipline: Grace Days and cognitive reframing on slips."
    val law5: String get() = if (language == AppLanguage.SPANISH) "5. Techo Cognitivo Estricto: Máximo 3.5 horas de foco profundo (Tipo A) al día." else "5. Strict Cognitive Ceiling: Max 3.5 hours of Deep Work (Type A) daily."
    val reframeInputLabel: String get() = if (language == AppLanguage.SPANISH) "¿Qué bloqueo o emoción estás experimentando?" else "What block, friction or fatigue are you experiencing?"
        
    val btnReOrchestrate: String get() = if (language == AppLanguage.SPANISH) "Re-Planificar" else "Re-Plan"
    val cognitiveReframeTitle: String get() = if (language == AppLanguage.SPANISH) "Motor de Reencuadre Cognitivo" else "Cognitive Reframing Engine"
    val cognitiveReframeSub: String get() = if (language == AppLanguage.SPANISH) 
        "¿Sientes fatiga, fricción o culpa por posponer? La IA reencuadra la situación con evidencia biológica." 
    else 
        "Feeling fatigue, friction, or guilt? The AI reframes the obstacle with biological evidence."
    val reframePlaceholder: String get() = if (language == AppLanguage.SPANISH) 
        "Ej: 'No he podido hacer el frog hoy y me siento frustrado' o 'Tengo pereza de cocinar'..." 
    else 
        "E.g.: 'I could not finish my frog task today and feel frustrated' or 'I feel low energy'..."
    val btnSubmitReframe: String get() = if (language == AppLanguage.SPANISH) "Reencuadrar con IA" else "Reframe with AI"
    val schemaInspectorTitle: String get() = if (language == AppLanguage.SPANISH) "Inspección de Salida JSON (AetherDailyPlan)" else "JSON Output Inspection (AetherDailyPlan)"
    val btnCopyJson: String get() = if (language == AppLanguage.SPANISH) "Copiar JSON" else "Copy JSON"
    val jsonCopiedToast: String get() = if (language == AppLanguage.SPANISH) "¡JSON de Aether OS copiado al portapapeles!" else "Aether OS JSON copied to clipboard!"

    // Settings Screen / Dialog
    val settingsTitle: String get() = if (language == AppLanguage.SPANISH) "Ajustes de Aether OS" else "Aether OS Settings"
    val settingsSub: String get() = if (language == AppLanguage.SPANISH) "Preferencias de idioma, bio-calibración y tutorial" else "Language preferences, bio-calibration and tutorial"
    val languageSectionTitle: String get() = if (language == AppLanguage.SPANISH) "Idioma de la Aplicación" else "Application Language"
    val tutorialSectionTitle: String get() = if (language == AppLanguage.SPANISH) "Guía y Tutorial del Sistema" else "System Guide & Tutorial"
    val tutorialLaunchBtn: String get() = if (language == AppLanguage.SPANISH) "Iniciar Tutorial Completo" else "Launch Full Tutorial"
    val tutorialLaunchSub: String get() = if (language == AppLanguage.SPANISH) "Aprende cómo funciona cada menú, regla biológica y herramienta de Aether OS" else "Learn how every menu, biological law, and tool in Aether OS works"
    val bioDefaultsSection: String get() = if (language == AppLanguage.SPANISH) "Configuración Biológica" else "Biological Configuration"
    val demoDataSection: String get() = if (language == AppLanguage.SPANISH) "Datos de Demostración" else "Demonstration Data"
    val btnResetDemoData: String get() = if (language == AppLanguage.SPANISH) "Restablecer Datos de Demostración" else "Reset Demo Data"
    val demoResetConfirm: String get() = if (language == AppLanguage.SPANISH) "Datos restablecidos correctamente." else "Demo data successfully reset."
    val systemLawsTitle: String get() = if (language == AppLanguage.SPANISH) "Leyes Inquebrantables de Aether OS" else "Unbreakable Laws of Aether OS"
    val law1Text: String get() = if (language == AppLanguage.SPANISH) "1. Ley del Frog: Máximo 1 tarea de Alta Demanda (Tipo A) al día." else "1. Frog Law: Maximum 1 High-Demand (Type A) task per day."
    val law2Text: String get() = if (language == AppLanguage.SPANISH) "2. Techo Cognitivo: Máximo 3.5 horas (210 min) de Deep Work diario." else "2. Cognitive Ceiling: Maximum 3.5 hours (210 min) daily Deep Work."
    val law3Text: String get() = if (language == AppLanguage.SPANISH) "3. Protocolo de Recuperación: Con energía < 60%, se suprimen tareas Tipo A." else "3. Recovery Protocol: Below 60% energy, Type A tasks are eliminated."
    val law4Text: String get() = if (language == AppLanguage.SPANISH) "4. Nutrición Relacional: Prioriza ingredientes en stock y bases batch cooking." else "4. Relational Nutrition: Prioritizes in-stock items and batch cooking bases."

    // Focus Timer
    val focusNoTaskSelected: String get() = if (language == AppLanguage.SPANISH) 
        "Sin tarea asignada — Toca cualquier tarea para sesión de foco" 
    else 
        "No task selected — Tap any task to start deep focus"

    // TimeBlock Dialog
    val addTimeBlockTitle: String get() = if (language == AppLanguage.SPANISH) "Añadir Bloque de Tiempo" else "Add Time Block"
    val blockStartTimeLabel: String get() = if (language == AppLanguage.SPANISH) "Hora de Inicio (ej: 09:00)" else "Start Time (e.g. 09:00)"
    val blockEndTimeLabel: String get() = if (language == AppLanguage.SPANISH) "Hora de Fin (ej: 10:30)" else "End Time (e.g. 10:30)"
    val blockTypeLabel: String get() = if (language == AppLanguage.SPANISH) "Tipo de Bloque" else "Block Type"
    val blockTitleLabel: String get() = if (language == AppLanguage.SPANISH) "Título del Bloque" else "Block Title"
    val blockNotesLabel: String get() = if (language == AppLanguage.SPANISH) "Notas / Enlace opcional" else "Notes / Optional link"

    // Meal Dialog
    val addMealTitle: String get() = if (language == AppLanguage.SPANISH) "Añadir Comida Personalizada" else "Add Custom Meal"
    val mealTitleLabel: String get() = if (language == AppLanguage.SPANISH) "Nombre del Plato" else "Meal Title"
    val mealDescLabel: String get() = if (language == AppLanguage.SPANISH) "Descripción / Propósito" else "Description / Purpose"
    val mealSlotLabel: String get() = if (language == AppLanguage.SPANISH) "Momento del Día" else "Meal Slot"
    val mealPrepTimeLabel: String get() = if (language == AppLanguage.SPANISH) "Tiempo de Preparación (min)" else "Prep Time (min)"
    val mealIngredientsLabel: String get() = if (language == AppLanguage.SPANISH) "Ingredientes (separados por coma)" else "Ingredients (comma separated)"
    val mealUsesBatchBase: String get() = if (language == AppLanguage.SPANISH) "Usa Base de Batch Cooking (<8 min)" else "Uses Batch Base (<8 min)"
    val mealInStockCheck: String get() = if (language == AppLanguage.SPANISH) "Todos los ingredientes disponibles" else "All ingredients in stock"
    val mealBioImpactLabel: String get() = if (language == AppLanguage.SPANISH) "Impacto Glucémico" else "Glycemic Bio-Impact"

    // Clean Slate & Demo Mode
    val btnCleanSlate: String get() = if (language == AppLanguage.SPANISH) "🧹 Empezar de Cero (Modo Limpio)" else "🧹 Reset to Clean Slate"
    val btnLoadDemo: String get() = if (language == AppLanguage.SPANISH) "📦 Cargar Datos de Ejemplo (Demo)" else "📦 Load Demo Sample Data"
    val cleanSlateDesc: String get() = if (language == AppLanguage.SPANISH) 
        "Vacía todas las tareas, comidas y bloques de prueba para usar Aether OS con tus datos reales." 
    else 
        "Clears all demo tasks, meals and blocks to start using Aether OS with your real data."

    // Empty States
    val emptyBacklogClean: String get() = if (language == AppLanguage.SPANISH) 
        "Tu bandeja de energía está limpia. Pulsa '+' para capturar tu primera tarea sin fricción." 
    else 
        "Your energy backlog is clean. Tap '+' to capture your first friction-free task."

    val emptyMealsClean: String get() = if (language == AppLanguage.SPANISH) 
        "No hay comidas registradas hoy. Pulsa '+ Comida' o toca 'Orquestar' para sincronizar con tu despensa." 
    else 
        "No meals registered today. Tap '+ Meal' or tap 'Orchestrate' to sync with your pantry."

    val emptyPantryClean: String get() = if (language == AppLanguage.SPANISH) 
        "Tu despensa está vacía. Añade tus ingredientes y bases cocinadas con '+ Añadir'." 
    else 
        "Your pantry is empty. Add your ingredients and batch bases with '+ Add'."

    val emptyTimeBlocksClean: String get() = if (language == AppLanguage.SPANISH) 
        "No hay bloques programados hoy. Pulsa '+' para añadir un bloque o 'Orquestar' para generar el horario circadiano." 
    else 
        "No time blocks scheduled today. Tap '+' to add a block or 'Orchestrate' to generate circadian schedule."

    // Quick Task Dialog
    val quickAddTitle: String get() = if (language == AppLanguage.SPANISH) "Captura Rápida de Tarea" else "Quick Task Capture"
    val taskTitleLabel: String get() = if (language == AppLanguage.SPANISH) "Título de la Tarea" else "Task Title"
    val taskDescLabel: String get() = if (language == AppLanguage.SPANISH) "Descripción (Opcional)" else "Description (Optional)"
    val taskDurationLabel: String get() = if (language == AppLanguage.SPANISH) "Duración Estimada (minutos)" else "Estimated Duration (minutes)"
    val taskCategoryLabel: String get() = if (language == AppLanguage.SPANISH) "Categoría" else "Category"
    val taskEnergyLabel: String get() = if (language == AppLanguage.SPANISH) "Nivel de Energía Requerido" else "Required Energy Level"
    val taskPriorityLabel: String get() = if (language == AppLanguage.SPANISH) "Prioridad 1-3-5" else "1-3-5 Priority"
    val taskMakeFrogCheck: String get() = if (language == AppLanguage.SPANISH) "Asignar como Frog Principal del Día (Tipo A)" else "Set as Primary Daily Frog (Type A)"

    // Add Pantry Dialog
    val addPantryTitle: String get() = if (language == AppLanguage.SPANISH) "Añadir a Despensa" else "Add Item to Pantry"
    val itemNameLabel: String get() = if (language == AppLanguage.SPANISH) "Nombre del Ingrediente" else "Ingredient Name"
    val itemCategoryLabel: String get() = if (language == AppLanguage.SPANISH) "Categoría de Alimento" else "Food Category"
    val itemQuantityLabel: String get() = if (language == AppLanguage.SPANISH) "Cantidad / Porción" else "Quantity / Portion"
    val itemInStockCheck: String get() = if (language == AppLanguage.SPANISH) "Disponible actualmente en despensa" else "Currently available in stock"
    val itemBatchBaseCheck: String get() = if (language == AppLanguage.SPANISH) "Es una base preparada de Batch Cooking" else "Is a prepared Batch Cooking base"

    // Tutorial Steps Content (7 Steps)
    fun getTutorialSteps(): List<TutorialStep> {
        return if (language == AppLanguage.SPANISH) {
            listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Bienvenido a Aether OS",
                    subtitle = "Tu Motor Inteligente de Gestión de Vida",
                    iconName = "AutoAwesome",
                    summary = "Aether OS no es una agenda común ni una lista de tareas más. Es un sistema unificado diseñado para orquestar tu realidad, eliminar la fatiga de decisión y respetar tu biología circadiana.",
                    bulletPoints = listOf(
                        "Elasticidad Conductual: Se adapta automáticamente a tu cansancio o vitalidad.",
                        "Cero Burocracia: Captura instantánea de tareas y comidas con un solo toque.",
                        "Disciplina sin Culpa: Si surge un imprevisto, dispones de Grace Days y reencuadres cognitivos para no romper tu identidad."
                    ),
                    bioPrinciple = "Principio: La fuerza de voluntad es finita; la arquitectura de sistemas es infinita.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Nexus: Centro de Mando & Cronobiología",
                    subtitle = "Puntuación de Preparación, Cronotipos y Recuperación",
                    iconName = "Dashboard",
                    summary = "La pestaña 'Nexus' es tu panel matutino principal. Aquí calibras tu energía percibida (0 a 100) y observas tu curva de energía según tu cronotipo.",
                    bulletPoints = listOf(
                        "Puntuación de Preparación: Desliza el control para indicar cómo amaneciste hoy.",
                        "Cronotipo: Elige entre León (pico temprano), Oso (pico solar 10-14h), Lobo (pico vespertino) o Delfín.",
                        "Protocolo de Recuperación Automático: Si tu preparación baja de 60, Aether OS activa el Modo Recuperación, eliminando tareas de alta exigencia para proteger tus glándulas suprarrenales."
                    ),
                    bioPrinciple = "Ley Biológica: Programa por picos hormonales, nunca por simples huecos libres en el calendario.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Techo Cognitivo & Matriz 1-3-5",
                    subtitle = "La Ley del Frog y el límite de 3.5 horas",
                    iconName = "Speed",
                    summary = "Para evitar el agotamiento cerebral y la dispersión, Aether OS impone dos leyes matemáticas inquebrantables:",
                    bulletPoints = listOf(
                        "🐸 La Ley del Frog (Tipo A): Solo se permite 1 única tarea de Alta Demanda cognitiva al día. Es tu prioridad maestra.",
                        "⚡ 3 Tareas Medias (Tipo B): Proyectos secundarios de demanda moderada.",
                        "✨ 5 Micro-Victorias (Tipo C): Gestiones rápidas de logística o administración (<15 min).",
                        "⏱️ Techo Cognitivo de 210 min (3.5h): El medidor te alertará en rojo si programas más de 3.5 horas de Deep Work."
                    ),
                    bioPrinciple = "Neurociencia: El córtex prefrontal solo puede sostener entre 3 y 4 horas de foco intenso diario.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Bandeja de Energía & Temporizador Focus",
                    subtitle = "Captura sin fricción y sesiones protegidas",
                    iconName = "Bolt",
                    summary = "En la pestaña 'Bandeja', almacenas todas tus ideas y tareas clasificadas por nivel de energía (Alta, Media, Baja).",
                    bulletPoints = listOf(
                        "Captura Rápida (+): Añade cualquier pendiente en segundos sin campos innecesarios.",
                        "Filtro de Energía: Si estás cansado, pulsa 'Baja' para ejecutar solo micro-tareas fáciles.",
                        "Temporizador Focus: Inicia sesiones de 25 minutos conectadas a tu tarea activa con temporizador circular integrado.",
                        "Ascenso a Frog: Promueve cualquier tarea a 'Frog del Día' con un solo toque."
                    ),
                    bioPrinciple = "Estrategia: Haz coincidir la tarea con tu nivel de energía actual en lugar de obligarte a la fuerza.",
                    targetTab = 1
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Nutrición Relacional & Despensa Batch",
                    subtitle = "Cocina en <8 min y elimina el desperdicio",
                    iconName = "Restaurant",
                    summary = "La fatiga de decisión sobre qué comer destruye el enfoque. Aether OS conecta tu stock real de despensa con recetas instantáneas.",
                    bulletPoints = listOf(
                        "Bases Batch Cooking: Prepara previamente bases (como Quinoa o Batata) y ensambla platos nutritivos en menos de 8 minutos.",
                        "Inventario Inteligente: Marca qué tienes en casa con un clic (En stock / Agotado).",
                        "Impacto Glucémico: Comidas calibradas con bajo índice glucémico para mantener dopamina estable y evitar niebla mental post-comida."
                    ),
                    bioPrinciple = "Nutrición: Alimenta tu cerebro sin picos de glucosa que provoquen somnolencia reactiva.",
                    targetTab = 2
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Hábitos Circadianos & Grace Days",
                    subtitle = "Disciplina sin culpa y anclajes biológicos",
                    iconName = "WbSunny",
                    summary = "Los hábitos no se construyen con castigos, sino con anclajes a la luz y los ritmos naturales.",
                    bulletPoints = listOf(
                        "Anclajes Maestros: Fotones solares matutinos, hidratación mineral, corte de cafeína a las 14:00, caminata Zona 2 y Digital Sunset (22:00).",
                        "Metas Elásticas: En días duros ejecuta la versión 'Mini', en días normales 'Plus' y en días óptimos 'Élite'.",
                        "🛡️ Tokens de Grace Days: Si un día no puedes cumplir, pulsa 'Activar Grace Day'. Tu racha se protege y el sistema te ofrece un reencuadre compasivo."
                    ),
                    bioPrinciple = "Psicología: Fallar un día es humano; regresar al siguiente es tu línea base. Cero culpa.",
                    targetTab = 3
                ),
                TutorialStep(
                    stepNumber = 7,
                    title = "Núcleo IA & Reencuadre Cognitivo",
                    subtitle = "Orquestador Gemini y exportación de datos",
                    iconName = "Psychology",
                    summary = "En el 'Núcleo IA' cuentas con el motor inteligente de Aether OS.",
                    bulletPoints = listOf(
                        "Orquestación Automática: Genera planes diarios balanceados respetando todas las reglas bioenergéticas.",
                        "🧠 Reencuadre Cognitivo: Escribe si sientes bloqueo, postergación o agobio. La IA te responderá con argumentos biológicos para disolver la fricción.",
                        "Esquema JSON Maestro: Inspecciona y copia el objeto estructurado compatible con AetherDailyPlan."
                    ),
                    bioPrinciple = "Integración: Toda tu vida sincronizada en una arquitectura limpia y comprensible.",
                    targetTab = 4
                )
            )
        } else {
            listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Welcome to Aether OS",
                    subtitle = "Your Intelligent Life Management Engine",
                    iconName = "AutoAwesome",
                    summary = "Aether OS is not a standard task manager. It is a unified system designed to orchestrate your daily reality, eliminate decision fatigue, and honor your circadian biology.",
                    bulletPoints = listOf(
                        "Behavioral Elasticity: Automatically adapts to your fatigue or high energy.",
                        "Zero Friction: Instant single-tap capture for tasks, meals, and habits.",
                        "Guilt-Free Discipline: Includes Grace Days and cognitive reframing to preserve identity without self-criticism."
                    ),
                    bioPrinciple = "Principle: Willpower is finite; system architecture is infinite.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Nexus: Circadian Command Hub",
                    subtitle = "Readiness Score, Chronotypes & Recovery Mode",
                    iconName = "Dashboard",
                    summary = "The 'Nexus' tab is your morning dashboard. Calibrate your perceived energy (0-100) and view your circadian energy curve based on your chronotype.",
                    bulletPoints = listOf(
                        "Biometric Readiness: Slide to set your morning biological energy.",
                        "Chronotype: Select between Lion (early peak), Bear (solar peak 10-14h), Wolf (evening peak), or Dolphin.",
                        "Automatic Recovery Protocol: When readiness drops below 60, Aether OS activates Recovery Mode, pausing high-demand tasks to protect adrenal health."
                    ),
                    bioPrinciple = "Biological Law: Schedule according to hormonal peaks, never empty calendar slots.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Cognitive Ceiling & 1-3-5 Matrix",
                    subtitle = "The Frog Law & 3.5h Deep Work Limit",
                    iconName = "Speed",
                    summary = "To avoid burnout and mental fragmentation, Aether OS enforces two mathematical laws:",
                    bulletPoints = listOf(
                        "🐸 The Frog Law (Type A): Exactly 1 High-Demand task allowed per day.",
                        "⚡ 3 Medium Tasks (Type B): Secondary projects with moderate cognitive load.",
                        "✨ 5 Quick Wins (Type C): Logistics and quick administrative actions (<15 min).",
                        "⏱️ 210 min Cognitive Ceiling (3.5h): Real-time gauge alerts you if deep work exceeds biological limits."
                    ),
                    bioPrinciple = "Neuroscience: The prefrontal cortex can sustain at most 3 to 4 hours of intense focus per day.",
                    targetTab = 0
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Energy Backlog & Focus Timer",
                    subtitle = "Zero-friction capture and protected sessions",
                    iconName = "Bolt",
                    summary = "Store all your pending tasks categorized by energy requirement (High, Medium, Low).",
                    bulletPoints = listOf(
                        "Quick Capture (+): Log any task in seconds without bloated fields.",
                        "Energy Filter: Feeling low on gas? Filter by 'Low' to tackle friction-free micro wins.",
                        "Deep Work Focus Timer: Run 25-minute Pomodoro focus sprints tied to your active task.",
                        "Promote to Frog: Elevate any task to the primary Frog with a single tap."
                    ),
                    bioPrinciple = "Strategy: Match the task to your biological state instead of forcing motivation.",
                    targetTab = 1
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Relational Nutrition & Batch Bases",
                    subtitle = "Cook in <8 min with zero food waste",
                    iconName = "Restaurant",
                    summary = "Food decision fatigue drains executive function. Aether OS links your actual pantry inventory to swift, nourishing meals.",
                    bulletPoints = listOf(
                        "Batch Cooking Bases: Prep bases (like Quinoa or Sweet Potatoes) ahead of time for 8-minute assembly.",
                        "Smart Pantry: Toggle what you currently have in stock with one tap.",
                        "Glycemic Impact: Balanced meals for stable dopamine without post-meal brain fog."
                    ),
                    bioPrinciple = "Nutrition: Fuel your brain without insulin spikes that induce reactive sleepiness.",
                    targetTab = 2
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Circadian Habits & Grace Days",
                    subtitle = "Guilt-free consistency anchored in biology",
                    iconName = "WbSunny",
                    summary = "Sustainable habits are anchored to light and biological cues, not punitive willpower.",
                    bulletPoints = listOf(
                        "Master Anchors: Morning sunlight photons, mineral hydration, 14:00 caffeine cutoff, Zone 2 movement, Digital Sunset.",
                        "Elastic Targets: Execute 'Mini' on tough days, 'Plus' on normal days, and 'Elite' on peak days.",
                        "🛡️ Grace Day Tokens: Protect your streak when unexpected fatigue strikes without breaking momentum."
                    ),
                    bioPrinciple = "Psychology: Missing once is human; returning today is your baseline. Zero guilt.",
                    targetTab = 3
                ),
                TutorialStep(
                    stepNumber = 7,
                    title = "Core AI & Cognitive Reframing",
                    subtitle = "Gemini orchestrator & JSON export",
                    iconName = "Psychology",
                    summary = "Access Aether's intelligent core to orchestrate your day and overcome cognitive friction.",
                    bulletPoints = listOf(
                        "AI Orchestration: Generates balanced daily schedules respecting all bio-laws.",
                        "🧠 Cognitive Reframing: Describe any block, guilt, or procrastination to receive evidence-based biological reframing.",
                        "Master JSON Schema: View and copy the structured AetherDailyPlan payload."
                    ),
                    bioPrinciple = "Integration: Your entire life orchestrated through an elegant, respectful architecture.",
                    targetTab = 4
                )
            )
        }
    }
}

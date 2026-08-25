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
    val targetTab: Int? = null,
    val exampleScenario: String? = null,
    val actionTip: String? = null
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
    val btnAddHabit: String get() = if (language == AppLanguage.SPANISH) "+ Añadir Hábito" else "+ Add Habit"
    val btnUndo: String get() = if (language == AppLanguage.SPANISH) "Deshacer" else "Undo"
    val btnEdit: String get() = if (language == AppLanguage.SPANISH) "Editar" else "Edit"
    val editTaskTitle: String get() = if (language == AppLanguage.SPANISH) "Editar Tarea" else "Edit Task"
    val editTimeBlockTitle: String get() = if (language == AppLanguage.SPANISH) "Editar Bloque de Tiempo" else "Edit Time Block"
    val editMealTitle: String get() = if (language == AppLanguage.SPANISH) "Editar Comida" else "Edit Meal"
    val editPantryTitle: String get() = if (language == AppLanguage.SPANISH) "Editar Ingrediente" else "Edit Pantry Item"
    val addHabitTitle: String get() = if (language == AppLanguage.SPANISH) "Añadir Hábito Ancla" else "Add Anchor Habit"
    val editHabitTitle: String get() = if (language == AppLanguage.SPANISH) "Editar Hábito Ancla" else "Edit Anchor Habit"
    val habitTitleLabel: String get() = if (language == AppLanguage.SPANISH) "Nombre del Hábito" else "Habit Name"
    val habitDescLabel: String get() = if (language == AppLanguage.SPANISH) "Descripción / Micro-paso" else "Description / Micro-step"
    val habitAnchorLabel: String get() = if (language == AppLanguage.SPANISH) "Momento Circadiano (Anclaje)" else "Circadian Window (Anchor)"
    val habitTipLabel: String get() = if (language == AppLanguage.SPANISH) "Frase de Reencuadre / Mantra" else "Reframing Tip / Mantra"
    val habitStreakLabel: String get() = if (language == AppLanguage.SPANISH) "Días de Racha Actuales" else "Current Streak Days"
    val itemDeleted: String get() = if (language == AppLanguage.SPANISH) "Elemento eliminado" else "Item deleted"
    val taskDeleted: String get() = if (language == AppLanguage.SPANISH) "Tarea eliminada" else "Task deleted"
    val timeBlockDeleted: String get() = if (language == AppLanguage.SPANISH) "Bloque de tiempo eliminado" else "Time block deleted"
    val mealDeleted: String get() = if (language == AppLanguage.SPANISH) "Comida eliminada" else "Meal deleted"
    val pantryDeleted: String get() = if (language == AppLanguage.SPANISH) "Ingrediente eliminado de despensa" else "Pantry item deleted"
    val habitDeleted: String get() = if (language == AppLanguage.SPANISH) "Hábito eliminado" else "Habit deleted"
    val longPressToEditHint: String get() = if (language == AppLanguage.SPANISH) "Mantén pulsado para editar" else "Long press to edit"
    val dragToReorderHint: String get() = if (language == AppLanguage.SPANISH) "Arrastra para reordenar" else "Drag to reorder"
    val btnMoveUp: String get() = if (language == AppLanguage.SPANISH) "Mover arriba" else "Move up"
    val btnMoveDown: String get() = if (language == AppLanguage.SPANISH) "Mover abajo" else "Move down"
    val itemRestored: String get() = if (language == AppLanguage.SPANISH) "Elemento restaurado con éxito" else "Item restored successfully"

    // Persistent History Screen (Module 2)
    val historyHeaderTitle: String get() = if (language == AppLanguage.SPANISH) "HISTORIAL PERSISTENTE" else "PERSISTENT HISTORY"
    val historyHeaderSub: String get() = if (language == AppLanguage.SPANISH) "Navegación temporal Año → Mes → Semana/Día" else "Temporal navigation Year → Month → Week/Day"
    val historyTitle: String get() = if (language == AppLanguage.SPANISH) "Historial Circadiano" else "Circadian History"
    val historySub: String get() = if (language == AppLanguage.SPANISH) "Rendimiento y consistencia sin culpa a lo largo del tiempo" else "Guilt-free consistency and performance over time"
    val historyViewYear: String get() = if (language == AppLanguage.SPANISH) "Vista Anual" else "Year View"
    val historyViewMonth: String get() = if (language == AppLanguage.SPANISH) "Vista Mensual" else "Month View"
    val historyViewDay: String get() = if (language == AppLanguage.SPANISH) "Desglose Diario" else "Daily Breakdown"
    val historySelectYear: String get() = if (language == AppLanguage.SPANISH) "Seleccionar Año" else "Select Year"
    val historyAvgCompletion: String get() = if (language == AppLanguage.SPANISH) "Promedio de Cumplimiento" else "Average Completion"
    val historyTotalCompleted: String get() = if (language == AppLanguage.SPANISH) "Total Completadas" else "Total Completed"
    val historyMaxStreak: String get() = if (language == AppLanguage.SPANISH) "Racha Máxima" else "Max Streak"
    val historyBestDay: String get() = if (language == AppLanguage.SPANISH) "Mejor Día" else "Best Day"
    val historyLegendGreen: String get() = if (language == AppLanguage.SPANISH) "≥70% Óptimo" else "≥70% Optimal"
    val historyLegendAmber: String get() = if (language == AppLanguage.SPANISH) "30–69% Moderado" else "30–69% Moderate"
    val historyLegendRed: String get() = if (language == AppLanguage.SPANISH) "<30% Mínimo" else "<30% Minimal"
    val historyLegendEmpty: String get() = if (language == AppLanguage.SPANISH) "Sin registro" else "No record"
    val historyCompletedSection: String get() = if (language == AppLanguage.SPANISH) "✅ ELEMENTOS COMPLETADOS" else "✅ COMPLETED ITEMS"
    val historyGraceSection: String get() = if (language == AppLanguage.SPANISH) "🛡️ DÍAS DE GRACIA / PARCIALES" else "🛡️ GRACE DAYS / PARTIAL"
    val historyMissedSection: String get() = if (language == AppLanguage.SPANISH) "❌ PENDIENTES / NO ALCANZADOS" else "❌ MISSED / INCOMPLETE"
    val historyNoDayLogs: String get() = if (language == AppLanguage.SPANISH) "No hay registros detallados para este día." else "No detailed execution logs for this day."
    val historyTapDayHint: String get() = if (language == AppLanguage.SPANISH) "Toca cualquier día en el calendario para ver el desglose horario." else "Tap any calendar day to inspect hourly completion breakdown."
    val btnHistoryTooltip: String get() = if (language == AppLanguage.SPANISH) "Ver Historial Persistente" else "View Persistent History"

    // Full Backup & Restore (Module 2)
    val fullBackupTitle: String get() = if (language == AppLanguage.SPANISH) "Copia de Seguridad Completa" else "Full System Backup"
    val fullBackupDesc: String get() = if (language == AppLanguage.SPANISH) "Exporta todas las tablas de Aether OS a JSON en Documents/ o restaura una copia." else "Export all Aether OS tables to JSON in Documents/ or restore a backup."
    val btnExportFullBackup: String get() = if (language == AppLanguage.SPANISH) "📤 Exportar Copia de Seguridad (JSON)" else "📤 Export Full Backup (JSON)"
    val btnRestoreFullBackup: String get() = if (language == AppLanguage.SPANISH) "📥 Restaurar Copia desde JSON" else "📥 Restore Backup from JSON"
    val wipeHistoryCheckboxLabel: String get() = if (language == AppLanguage.SPANISH) "Borrar también historial (irreversible)" else "Also wipe persistent history (irreversible)"
    val wipeHistoryNote: String get() = if (language == AppLanguage.SPANISH) "El historial persistente NO se borra por defecto al iniciar en Modo Limpio." else "Persistent history is PRESERVED by default when resetting to Clean Slate."
    val restoreDialogTitle: String get() = if (language == AppLanguage.SPANISH) "Restaurar Copia de Seguridad" else "Restore Full Backup"
    val restoreDialogDesc: String get() = if (language == AppLanguage.SPANISH) "Pega el contenido JSON de la copia de seguridad completa para restaurar todas las tablas:" else "Paste the complete JSON backup payload below to restore all tables:"
    val restorePastePlaceholder: String get() = if (language == AppLanguage.SPANISH) "Pega aquí el contenido del archivo JSON..." else "Paste JSON content here..."
    val btnConfirmRestore: String get() = if (language == AppLanguage.SPANISH) "Restaurar Base de Datos" else "Restore Database"
    val backupExportedMsg: String get() = if (language == AppLanguage.SPANISH) "Copia exportada a: " else "Backup exported to: "
    val backupRestoredMsg: String get() = if (language == AppLanguage.SPANISH) "¡Base de datos restaurada correctamente!" else "Database restored successfully!"

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

    // Smart Chrono-Check & Objective Biometrics
    val btnSmartCheckIn: String get() = if (language == AppLanguage.SPANISH) "🧠 Chequeo Inteligente" else "🧠 Smart Chrono-Check"
    val smartCheckInTitle: String get() = if (language == AppLanguage.SPANISH) "Chequeo Circadiano Inteligente" else "Smart Circadian Check-in"
    val smartCheckInSub: String get() = if (language == AppLanguage.SPANISH) "Evaluación guiada de sueño, estrés y energía para calibrar el día con base fisiológica." else "Guided assessment of sleep, stress & energy to calibrate your day objectively."
    val sleepSectionTitle: String get() = if (language == AppLanguage.SPANISH) "🛌 ARQUITECTURA DE SUEÑO" else "🛌 SLEEP ARCHITECTURE"
    val sleepStartLabel: String get() = if (language == AppLanguage.SPANISH) "Hora de Dormir" else "Bedtime"
    val sleepEndLabel: String get() = if (language == AppLanguage.SPANISH) "Hora de Despertar" else "Wake Time"
    val sleepHoursLabel: String get() = if (language == AppLanguage.SPANISH) "Horas de Sueño" else "Sleep Duration"
    val sleepQualityLabel: String get() = if (language == AppLanguage.SPANISH) "Calidad Subjetiva del Sueño" else "Subjective Sleep Quality"
    val sleepInterruptionsLabel: String get() = if (language == AppLanguage.SPANISH) "Interrupciones Nocturnas" else "Night Interruptions"
    val wakeFeelingLabel: String get() = if (language == AppLanguage.SPANISH) "Sensación al Despertar" else "Wake-up Feeling"
    val internalStateTitle: String get() = if (language == AppLanguage.SPANISH) "⚡ ESTADO INTERNO & NEUROLOGÍA" else "⚡ INTERNAL STATE & NEUROLOGY"
    val energyLevelLabel: String get() = if (language == AppLanguage.SPANISH) "Nivel de Energía Actual (1-10)" else "Current Energy Level (1-10)"
    val stressLevelLabel: String get() = if (language == AppLanguage.SPANISH) "Nivel de Estrés / Tensión (1-10)" else "Stress / Tension Level (1-10)"
    val motivationLevelLabel: String get() = if (language == AppLanguage.SPANISH) "Motivación Ejecutiva (1-10)" else "Executive Motivation (1-10)"
    val mentalOverloadLabel: String get() = if (language == AppLanguage.SPANISH) "Sobrecarga o Niebla Mental" else "Mental Overload or Brain Fog"
    val modulatorsTitle: String get() = if (language == AppLanguage.SPANISH) "☕ MODULADORES & ESTILO DE VIDA" else "☕ MODULATORS & LIFESTYLE"
    val caffeineLabel: String get() = if (language == AppLanguage.SPANISH) "Ingesta de Cafeína" else "Caffeine Intake"
    val exerciseLabel: String get() = if (language == AppLanguage.SPANISH) "Actividad Física Realizada / Planificada" else "Physical Exercise Done / Planned"
    val mealRegularityLabel: String get() = if (language == AppLanguage.SPANISH) "Regularidad Nutricional" else "Meal Regularity"
    val emotionalConcernLabel: String get() = if (language == AppLanguage.SPANISH) "¿Algo te preocupa hoy? (Opcional)" else "Any emotional concern today? (Optional)"
    val calculatedScoreLabel: String get() = if (language == AppLanguage.SPANISH) "READINESS CALCULADO" else "CALCULATED READINESS"
    val dynamicCeilingLabel: String get() = if (language == AppLanguage.SPANISH) "PRESUPUESTO COGNITIVO ADAPTADO" else "ADAPTED COGNITIVE BUDGET"
    val btnApplyCheckIn: String get() = if (language == AppLanguage.SPANISH) "Guardar y Calibrar Sistema" else "Save & Calibrate System"
    val btnApplyTaskBreakdown: String get() = if (language == AppLanguage.SPANISH) "➕ Añadir al Plan de Tareas" else "➕ Add to Task Plan"

    // AI Quick Actions
    val quickActionBreakDown: String get() = if (language == AppLanguage.SPANISH) "🚀 Desglosar Frog" else "🚀 Break Down Frog"
    val quickActionNoMotivation: String get() = if (language == AppLanguage.SPANISH) "🧠 No tengo ganas" else "🧠 No Motivation"
    val quickActionOverwhelmed: String get() = if (language == AppLanguage.SPANISH) "🌊 Estoy saturado" else "🌊 I'm Overwhelmed"
    val quickActionMicroStep: String get() = if (language == AppLanguage.SPANISH) "🎯 Mínimo micro-paso" else "🎯 Smallest Micro-Step"
    val quickActionEmotionalSupport: String get() = if (language == AppLanguage.SPANISH) "💖 Apoyo Personal" else "💖 Personal Support"
    val quickActionGentlePlan: String get() = if (language == AppLanguage.SPANISH) "🗓️ Plan Suave" else "🗓️ Gentle Plan"
    
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
    val slotCustom: String get() = if (language == AppLanguage.SPANISH) "Personalizado" else "Custom"
    
    val customSlotNameLabel: String get() = if (language == AppLanguage.SPANISH) "Nombre del Momento (ej: Pre-Entreno, Merienda)" else "Custom Timing Name (e.g. Pre-Workout)"
    val customSlotSuggestions: String get() = if (language == AppLanguage.SPANISH) "Sugerencias:" else "Suggestions:"
    
    val prepTimeLabel: String get() = if (language == AppLanguage.SPANISH) "min prep" else "min prep"
    val batchCookingBaseTag: String get() = if (language == AppLanguage.SPANISH) "Base Batch Cocinada" else "Batch Cooked Base"
    val allStockAvailable: String get() = if (language == AppLanguage.SPANISH) "Ingredientes disponibles en despensa" else "All ingredients in stock"
    val missingStockWarning: String get() = if (language == AppLanguage.SPANISH) "Faltan ingredientes por reponer" else "Ingredients need restocking"
    
    val glycemicLow: String get() = if (language == AppLanguage.SPANISH) "Bajo Índice Glucémico (Dopamina Estable)" else "Low Glycemic Focus (Stable Dopamine)"
    val glycemicModerate: String get() = if (language == AppLanguage.SPANISH) "Energía Sostenida" else "Moderate Steady Energy"
    val glycemicRecovery: String get() = if (language == AppLanguage.SPANISH) "Recuperación Parasimpática" else "Deep Parasympathetic Recovery"

    // Macro Balance & Duplication
    val macroSummaryTitle: String get() = if (language == AppLanguage.SPANISH) "BALANCE BIOENERGÉTICO DE MACROS" else "BIOENERGETIC MACRO BALANCE"
    val macroSummarySub: String get() = if (language == AppLanguage.SPANISH) "Combustible celular óptimo sin picos de insulina" else "Optimal cellular fuel without insulin crashes"
    val macroProteinLabel: String get() = if (language == AppLanguage.SPANISH) "Proteína" else "Protein"
    val macroCarbsLabel: String get() = if (language == AppLanguage.SPANISH) "Carbohidratos" else "Carbs"
    val macroFatLabel: String get() = if (language == AppLanguage.SPANISH) "Grasas" else "Fats"
    val macroCaloriesLabel: String get() = if (language == AppLanguage.SPANISH) "Calorías" else "Calories"
    val macroCalculatedKcal: String get() = if (language == AppLanguage.SPANISH) "Kcal estimadas:" else "Estimated Kcal:"
    val macroGramsSuffix: String get() = "g"
    val macroCaloriesSuffix: String get() = "kcal"
    
    val btnDuplicateMeal: String get() = if (language == AppLanguage.SPANISH) "Duplicar a otro día" else "Duplicate to day"
    val duplicateMealTitle: String get() = if (language == AppLanguage.SPANISH) "Duplicar Comida a Otro Día" else "Duplicate Meal to Another Day"
    val duplicateMealSubtitle: String get() = if (language == AppLanguage.SPANISH) "Selecciona el día destino para copiar esta comida y sus ingredientes:" else "Select target day to duplicate this meal and its ingredients:"
    val duplicateTomorrow: String get() = if (language == AppLanguage.SPANISH) "☀️ Mañana (+1 día)" else "☀️ Tomorrow (+1 day)"
    val duplicateIn2Days: String get() = if (language == AppLanguage.SPANISH) "📅 En 2 días (+2 días)" else "📅 In 2 days (+2 days)"
    val duplicateIn3Days: String get() = if (language == AppLanguage.SPANISH) "🗓️ En 3 días (+3 días)" else "🗓️ In 3 days (+3 days)"
    val duplicateToday: String get() = if (language == AppLanguage.SPANISH) "⚡ Hoy (Duplicar ración)" else "⚡ Today (Duplicate portion)"
    val duplicateSuccessMessage: String get() = if (language == AppLanguage.SPANISH) "Comida duplicada con éxito." else "Meal duplicated successfully."
    val mealLongPressHint: String get() = if (language == AppLanguage.SPANISH) "Mantén pulsada una comida para editar o duplicar" else "Long-press meal card to edit or duplicate"

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
    val streakRebirthLabel: String get() = if (language == AppLanguage.SPANISH) "🌱 Renacer" else "🌱 Fresh Start"
    val habitConsistencyTitle: String get() = if (language == AppLanguage.SPANISH) "Consistencia 28 días:" else "28-Day Consistency:"
    val habitCompletedBadge: String get() = if (language == AppLanguage.SPANISH) "Cumplido" else "Done"
    val levelLabel: String get() = if (language == AppLanguage.SPANISH) "Nivel" else "Level"
    val totalXpLabel: String get() = if (language == AppLanguage.SPANISH) "XP Total" else "Total XP"
    val levelUpCelebrationTitle: String get() = if (language == AppLanguage.SPANISH) "⚡ ¡SUBISTE DE NIVEL! ⚡" else "⚡ LEVEL UP! ⚡"
    val achievementUnlockedTitle: String get() = if (language == AppLanguage.SPANISH) "🏆 ¡NUEVO LOGRO DESBLOQUEADO! 🏆" else "🏆 NEW ACHIEVEMENT UNLOCKED! 🏆"
        
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
    val aiHeader: String get() = if (language == AppLanguage.SPANISH) "NÚCLEO IA & ASISTENTE" else "AETHER AI CORE & ASSISTANT"
    val aiSub: String get() = if (language == AppLanguage.SPANISH) "Conversación circadiana • Contexto biológico real • Notas favoritas" else "Circadian conversation • Real bio-context • Favorite notes"
    val aiHeaderTitle: String get() = aiHeader
    val aiHeaderSub: String get() = aiSub
    
    // AI Tabs
    val aiTabChat: String get() = if (language == AppLanguage.SPANISH) "💬 Asistente IA" else "💬 AI Assistant"
    val aiTabFavorites: String get() = if (language == AppLanguage.SPANISH) "⭐ Notas Guardadas" else "⭐ Saved Notes"
    val aiTabInspector: String get() = if (language == AppLanguage.SPANISH) "📜 Esquema JSON" else "📜 JSON Schema"
    
    // Quick Context Prompts (5.2)
    val quickActionPlanDay: String get() = if (language == AppLanguage.SPANISH) "🚀 Planifica mi día" else "🚀 Plan my day"
    val quickActionLowEnergy: String get() = if (language == AppLanguage.SPANISH) "⚡ Tengo poca energía" else "⚡ I have low energy"
    val quickActionWeeklyReview: String get() = if (language == AppLanguage.SPANISH) "📊 Revisión semanal" else "📊 Weekly review"
    val quickAction30Min: String get() = if (language == AppLanguage.SPANISH) "⏱️ ¿Qué hago con 30 min?" else "⏱️ What to do in 30 min?"
    
    // Chat UI (5.1, 5.3, 5.4, 5.5)
    val aiChatInputPlaceholder: String get() = if (language == AppLanguage.SPANISH) "Pregunta a Aether OS o pide un ajuste biológico..." else "Ask Aether OS or request a biological adjustment..."
    val aiChatSend: String get() = if (language == AppLanguage.SPANISH) "Enviar" else "Send"
    val aiThinking: String get() = if (language == AppLanguage.SPANISH) "Sincronizando contexto biológico..." else "Syncing biological context..."
    val aiStreaming: String get() = if (language == AppLanguage.SPANISH) "Aether IA respondiendo..." else "Aether AI responding..."
    val aiSaveFavorite: String get() = if (language == AppLanguage.SPANISH) "Guardar como nota favorita" else "Save as favorite note"
    val aiRemoveFavorite: String get() = if (language == AppLanguage.SPANISH) "Quitar de favoritas" else "Remove from favorites"
    val aiFavoriteSavedToast: String get() = if (language == AppLanguage.SPANISH) "⭐ Respuesta guardada en Notas Favoritas" else "⭐ Response saved to Favorite Notes"
    val aiFavoriteRemovedToast: String get() = if (language == AppLanguage.SPANISH) "Nota eliminada de favoritas" else "Note removed from favorites"
    val aiCopyResponse: String get() = if (language == AppLanguage.SPANISH) "Copiar respuesta" else "Copy response"
    val aiResponseCopiedToast: String get() = if (language == AppLanguage.SPANISH) "¡Respuesta copiada al portapapeles!" else "Response copied to clipboard!"
    val aiClearHistory: String get() = if (language == AppLanguage.SPANISH) "Limpiar historial" else "Clear history"
    val aiClearHistoryConfirm: String get() = if (language == AppLanguage.SPANISH) "¿Deseas borrar todo el historial de conversación?" else "Do you want to clear all conversation history?"
    val aiEmptyChatTitle: String get() = if (language == AppLanguage.SPANISH) "Asistente Neuro-Circadiano Aether" else "Aether Neuro-Circadian Assistant"
    val aiEmptyChatDesc: String get() = if (language == AppLanguage.SPANISH) 
        "Haz consultas libres o toca un atajo rápido. Gemini analizará automáticamente tus tareas, hábitos, biometría y despensa en tiempo real." 
    else 
        "Ask anything or tap a quick prompt. Gemini automatically evaluates your real-time tasks, habits, biometrics, and pantry."
    val aiNoFavoritesTitle: String get() = if (language == AppLanguage.SPANISH) "Sin Notas Favoritas" else "No Favorite Notes"
    val aiNoFavoritesDesc: String get() = if (language == AppLanguage.SPANISH) 
        "Toca la estrella ⭐ en cualquier respuesta del Asistente para guardarla aquí como nota permanente." 
    else 
        "Tap the star ⭐ on any Assistant response to pin it here as a permanent note."
    val aiRealContextBadge: String get() = if (language == AppLanguage.SPANISH) "Contexto Biológico Activo" else "Active Bio Context"
    
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

    // Tutorial Steps Content (8 Comprehensive Chapters)
    fun getTutorialSteps(): List<TutorialStep> {
        return if (language == AppLanguage.SPANISH) {
            listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Bienvenido a Aether OS",
                    subtitle = "Tu Motor Inteligente de Gestión de Vida & Filosofía Bioenergética",
                    iconName = "AutoAwesome",
                    summary = "Aether OS no es una agenda ordinaria ni una lista de tareas más. Es un sistema unificado diseñado para orquestar tu realidad cotidiana, eliminar la fatiga por toma de decisiones y respetar los ritmos circadianos de tu biología.",
                    bulletPoints = listOf(
                        "Elasticidad Conductual: Se adapta automáticamente a tu cansancio o vitalidad sin juzgarte ni penalizarte.",
                        "Anti-Burocracia: Captura instantánea de tareas, comidas e ingredientes con un solo toque y micro-campos inteligentes.",
                        "Disciplina sin Culpa: Mecanismos biológicos de Días de Gracia (Grace Days) y Reencuadre Cognitivo con IA para no romper jamás tu identidad.",
                        "Consistencia de Retorno: Tu progreso no se mide por una cadena ininterrumpida imposible, sino por la capacidad de regresar al día siguiente con cero culpa."
                    ),
                    bioPrinciple = "Ley Fundamental: La fuerza de voluntad es un recurso químico finito; la arquitectura de sistemas es infinita.",
                    targetTab = 0,
                    exampleScenario = "Caso Real: Si un día te despiertas con agotamiento o una mala noche, no te fuerces a hacer tareas de 3 horas. Reduces la carga, activas un Grace Day y el sistema re-calibra tu día protegiendo tu paz mental.",
                    actionTip = "Consejo Pro: Puedes alternar entre el Modo Demostración y una Pizarra Limpia desde los Ajustes ⚙️ cuando quieras empezar de cero."
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Nexus: Centro de Mando Matutino",
                    subtitle = "Puntuación de Preparación, Cronotipos y Modo Recuperación",
                    iconName = "Dashboard",
                    summary = "La pestaña 'Nexus' es tu panel de control matutino. Cada día al despertar, calibras tu nivel de preparación biológica (0 a 100) y observas tu curva de energía según tu cronotipo biológico.",
                    bulletPoints = listOf(
                        "Puntuación de Preparación (Readiness Score): Desliza el control para cuantificar tu energía percibida, calidad de sueño y carga cognitiva.",
                        "4 Cronotipos Circadianos:\n  • 🦁 León: Madrugador con pico máximo de 07:00 a 11:00.\n  • 🐻 Oso: Sincronizado con el ciclo solar, pico de 10:00 a 14:00.\n  • 🐺 Lobo: Vespertino/nocturno, pico creativo de 16:00 a 21:00.\n  • 🐬 Delfín: Sueño ligero y sensible, picos cortos de 10:00 a 12:00.",
                        "Protocolo de Recuperación Automático (<60): Si tu preparación cae por debajo de 60, Aether OS activa el Modo Recuperación, cancelando tareas de alta exigencia para prevenir agotamiento suprarrenal."
                    ),
                    bioPrinciple = "Ley Biológica: Programa tus tareas por picos hormonales de cortisol y dopamina, nunca por huecos libres en el calendario.",
                    targetTab = 0,
                    exampleScenario = "Caso Real: Como Oso, si marcas 85/100 de preparación, tu tarea Frog de trabajo profundo se agendará automáticamente en la ventana de oro de 10:00 a 12:30.",
                    actionTip = "Consejo Pro: Selecciona tu cronotipo biológico real para que la curva de energía se ajuste a tus ritmos metabólicos naturales."
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Techo Cognitivo & Matriz 1-3-5",
                    subtitle = "La Ley del Frog y el límite biológico de 3.5 horas de foco",
                    iconName = "Speed",
                    summary = "El córtex prefrontal solo puede sostener entre 3 y 4 horas de atención ejecutiva de alta demanda al día. Aether OS impone reglas matemáticas inquebrantables para blindar tu energía mental.",
                    bulletPoints = listOf(
                        "🐸 1 Única Tarea FROG (Tipo A): La prioridad maestra del día con la mayor demanda cognitiva o impacto. Si conquistas tu Frog, tu día es un éxito rotundo.",
                        "⚡ 3 Tareas Medias (Tipo B): Proyectos secundarios de demanda moderada (redacción, análisis, reuniones).",
                        "✨ 5 Micro-Victorias (Tipo C): Gestiones rápidas de logística, pagos o administración (<15 min).",
                        "⏱️ Techo Cognitivo de 210 min (3.5h): El medidor circular te alertará en rojo si intentas programar más bloques de foco de los que tu cerebro puede procesar."
                    ),
                    bioPrinciple = "Neurociencia: Forzar más de 3.5 horas de trabajo profundo al día genera deuda cognitiva y fatiga crónica.",
                    targetTab = 0,
                    exampleScenario = "Caso Real: Si marcas tu propuesta de proyecto como Frog, dedícale tu mejor bloque de energía matutina. Al completarla, verás la celebración de Frog conquistado 🔥.",
                    actionTip = "Consejo Pro: En la Bandeja puedes convertir cualquier tarea en Frog pulsando el botón de la rana 🐸."
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Bandeja de Energía & Focus Pomodoro",
                    subtitle = "Captura sin fricción, filtrado bioenergético y alarmas en segundo plano",
                    iconName = "Bolt",
                    summary = "La pestaña 'Bandeja' almacena todas tus tareas clasificadas no por listas interminables, sino por su coste energético real (Alta, Media, Baja) para adaptarse a tu estado biológico actual.",
                    bulletPoints = listOf(
                        "Captura Rápida (+): Añade cualquier pendiente en segundos especificando únicamente título, energía requerida y duración estimada.",
                        "Filtrado por Nivel de Energía: ¿Tarde con poca energía? Pulsa el filtro 'Baja' para ejecutar micro-tareas sin desgaste mental.",
                        "Temporizador Focus Integrado: Sesiones de concentración de 25 minutos vinculadas a tu tarea activa.",
                        "Notificación en Segundo Plano (WorkManager): Programa la alarma de enfoque que te avisará al finalizar la sesión incluso con la app cerrada o pantalla bloqueada.",
                        "Búsqueda Inteligente & Filtro de Pendientes: Localiza cualquier tarea al instante por título o categoría."
                    ),
                    bioPrinciple = "Estrategia: Haz coincidir la tarea con tu nivel de energía biológica actual en lugar de forzarte a la fuerza.",
                    targetTab = 1,
                    exampleScenario = "Caso Real: Tienes 30 minutos antes de almorzar y baja energía. Filtra por 'Baja', selecciona responder 2 correos y activa el temporizador.",
                    actionTip = "Consejo Pro: Concede el permiso de notificaciones para que el temporizador te alerte al terminar sin tener que mirar el móvil."
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Nutrición Relacional & Batch Cooking",
                    subtitle = "Comidas en <8 minutos y energía cerebral sin bajones postprandiales",
                    iconName = "Restaurant",
                    summary = "La fatiga por decidir qué cocinar destruye la fuerza de voluntad. Aether OS conecta tu stock real de despensa con recetas rápidas basadas en preparaciones de Batch Cooking.",
                    bulletPoints = listOf(
                        "Bases de Batch Cooking: Cocina 1 o 2 veces por semana bases universales (Quinoa, Batata asada, Pollo marinado, Huevos) y monta comidas completas en menos de 8 minutos.",
                        "Inventario de Despensa & Stock: Alterna qué ingredientes tienes en casa con un simple toque (En stock / Agotado) y genera tu lista de la compra automática.",
                        "Diseño Anti-Pico Glucémico: Comidas calibradas con bajo índice glucémico y grasas saludables para mantener dopamina estable y evitar niebla mental post-comida."
                    ),
                    bioPrinciple = "Nutrición Celular: Alimenta tu cerebro sin picos de insulina que provoquen somnolencia reactiva y caída de concentración.",
                    targetTab = 2,
                    exampleScenario = "Caso Real: Llegaste cansado a casa. Abres Nutrición, verificas tu base de Quinoa y verduras cocinadas, calientas 4 minutos y cenas sin pedir comida ultraprocesada.",
                    actionTip = "Consejo Pro: Usa la pestaña 'Despensa & Stock' para marcar qué ingredientes necesitas reponer antes de ir al supermercado."
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Anclajes Circadianos & Grace Days",
                    subtitle = "Hábitos anclados a la luz solar y constancia sin remordimientos",
                    iconName = "WbSunny",
                    summary = "La constancia duradera no depende del castigo ni de la fuerza bruta, sino de anclar tus rutinas a desencadenantes biológicos naturales (fotones solares, temperatura, hidratación).",
                    bulletPoints = listOf(
                        "5 Anclajes Circadianos Maestros:\n  • ☀️ Fotones Solares Matutinos (10-15 min) para sincronizar tu reloj central (SCN).\n  • 💧 Hidratación con Electrolitos en ayunas.\n  • ☕ Límite de Cafeína a las 14:00 para proteger la adenosina y el sueño profundo.\n  • 🚶 Movimiento Zona 2 (30-45 min de caminata aeróbica suave).\n  • 🌙 Digital Sunset a las 22:00 para estimular la secreción natural de melatonina.",
                        "Metas Elásticas: Versión 'Mini' (para días de crisis), 'Plus' (estándar diario) y 'Élite' (para días de máxima vitalidad).",
                        "🛡️ Tokens de Días de Gracia (Grace Days): Si un día imprevisto no puedes realizar un hábito, pulsa 'Activar Grace Day'. Tu racha permanece protegida sin culpa."
                    ),
                    bioPrinciple = "Psicología del Comportamiento: Fallar un día es humano; regresar hoy es tu verdadera línea base. Cero culpa.",
                    targetTab = 3,
                    exampleScenario = "Caso Real: Tuviste un viaje inesperado y no pudiste salir a caminar. Activas un Grace Day: tu racha de 47 días sigue intacta y tu identidad se mantiene fuerte.",
                    actionTip = "Consejo Pro: Los Grace Days son una herramienta biológica, no una trampa. Úsalos con compasión estratégica."
                ),
                TutorialStep(
                    stepNumber = 7,
                    title = "Núcleo IA & Reencuadre Cognitivo",
                    subtitle = "Orquestación Gemini, disolución del autosabotaje y portabilidad de datos",
                    iconName = "Psychology",
                    summary = "En el 'Núcleo IA' cuentas con el motor inteligente de Aether OS. Sintetiza planes diarios equilibrados y te ofrece soporte cognitivo en momentos de fricción mental.",
                    bulletPoints = listOf(
                        "Orquestación Automática Gemini: Analiza tu preparación matutina, tareas pendientes y cronotipo para generar un plan horario circadiano perfecto.",
                        "Motor de Respaldo Determinista: Si no tienes conexión o API Key, el motor determinista local genera el plan de inmediato sin fallos.",
                        "🧠 Reencuadre Cognitivo: ¿Sientes pereza, frustración o culpa por postergar? Escribe tu bloqueo. La IA te responderá con argumentos biológicos para disolver la resistencia.",
                        "Esquema JSON Maestro: Inspecciona y exporta tu plan diario como JSON estándar para compartirlo o archivarlo."
                    ),
                    bioPrinciple = "Integración Holística: Toda tu vida cotidiana sincronizada en una arquitectura limpia, comprensible y exportable.",
                    targetTab = 4,
                    exampleScenario = "Caso Real: Te sientes culpable por no haber avanzado en un informe. Escribes 'Tengo culpa por no avanzar hoy' y la IA te recuerda que tras 7 horas de trabajo tu córtex está en reposo necesario.",
                    actionTip = "Consejo Pro: Puedes pulsar 'Copiar JSON' o 'Compartir' en la pestaña IA para guardar tu plan diario en cualquier otra aplicación."
                ),
                TutorialStep(
                    stepNumber = 8,
                    title = "Sistema de Logros & Maestría Diaria",
                    subtitle = "10 Medallas bioenergéticas, personalización y soporte bilingüe",
                    iconName = "EmojiEvents",
                    summary = "Aether OS premia tu constancia y respeto por tu biología con un sistema de 10 logros desbloqueables que refuerzan tu identidad positiva.",
                    bulletPoints = listOf(
                        "🏆 10 Medallas Desbloqueables: Conquista tu primera Frog, alcanza rachas de 7 y 30 días, completa 10 y 100 tareas, usa tu primer Grace Day y sintetiza con IA.",
                        "🔔 Banner de Desbloqueo: Cada vez que alcanzas un hito, aparece una notificación animada en la parte superior.",
                        "🌐 Soporte Bilingüe Instantáneo: Alterna entre Español e Inglés en cualquier momento con el botón [🇪🇸/🇬🇧] de la barra superior.",
                        "⚙️ Pizarra Limpia vs Datos Demo: Restaura el estado limpio o carga datos de ejemplo en cualquier momento desde los Ajustes."
                    ),
                    bioPrinciple = "Dopamina Intrínseca: Recompensar los micro-avances fortalece las rutas neuronales de la constancia.",
                    targetTab = 0,
                    exampleScenario = "Caso Real: Al completar tu 7mo día consecutivo de hábitos circadianos, desbloquearás la medalla 'Constancia Circadiana' en tu panel de trofeos.",
                    actionTip = "Consejo Pro: Abre los Ajustes ⚙️ en la esquina superior derecha y toca 'Ver Vitrina de Logros' para consultar tu progreso."
                )
            )
        } else {
            listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Welcome to Aether OS",
                    subtitle = "Your Intelligent Life Management Engine & Bioenergetic Philosophy",
                    iconName = "AutoAwesome",
                    summary = "Aether OS is not a conventional to-do list or planner. It is a unified life orchestration system designed to organize your daily reality, eliminate decision fatigue, and honor your circadian biology.",
                    bulletPoints = listOf(
                        "Behavioral Elasticity: Automatically adapts to your energy level and fatigue without judgment.",
                        "Zero Friction: Instant 1-tap capture for tasks, meals, and pantry items with smart micro-fields.",
                        "Guilt-Free Discipline: Biological Grace Days and AI Cognitive Reframing to preserve identity without self-criticism.",
                        "Pattern of Return: Consistency is measured by returning the next day, not by rigid perfection."
                    ),
                    bioPrinciple = "Core Law: Willpower is a finite chemical resource; system architecture is infinite.",
                    targetTab = 0,
                    exampleScenario = "Real Scenario: If you wake up with poor sleep, you don't force high-demand tasks. You lower the intensity, use a Grace Day, and the system re-calibrates without guilt.",
                    actionTip = "Pro Tip: You can toggle between Demo Mode and a Clean Slate anytime from the Settings menu ⚙️."
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Nexus: Morning Command Hub",
                    subtitle = "Readiness Score, Chronotypes & Automatic Recovery",
                    iconName = "Dashboard",
                    summary = "The 'Nexus' tab is your morning command center. Each morning, calibrate your biological readiness score (0-100) and observe your circadian curve mapped to your chronotype.",
                    bulletPoints = listOf(
                        "Biometric Readiness Score (0-100): Slide to quantify your perceived energy, sleep recovery, and mental clarity.",
                        "4 Biological Chronotypes:\n  • 🦁 Lion: Early riser with peak alert from 07:00 to 11:00.\n  • 🐻 Bear: Solar-synchronized, peak focus from 10:00 to 14:00.\n  • 🐺 Wolf: Evening/night peak from 16:00 to 21:00.\n  • 🐬 Dolphin: Light/fragmented sleeper, short focus sprints 10:00 to 12:00.",
                        "Automatic Recovery Protocol (<60): If readiness drops below 60, Aether OS activates Recovery Mode, pausing high-demand tasks to protect adrenal health."
                    ),
                    bioPrinciple = "Biological Law: Schedule tasks by hormonal peaks of cortisol and dopamine, never empty calendar slots.",
                    targetTab = 0,
                    exampleScenario = "Real Scenario: As a Bear with 85/100 readiness, your deep work Frog task is scheduled into your golden window from 10:00 to 12:30.",
                    actionTip = "Pro Tip: Set your true chronotype in Nexus so your circadian energy curve reflects your biological rhythms."
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Cognitive Ceiling & 1-3-5 Matrix",
                    subtitle = "The Frog Law & 3.5h Deep Work Biological Limit",
                    iconName = "Speed",
                    summary = "The prefrontal cortex can only sustain 3 to 4 hours of high-demand executive focus per day. Aether OS applies mathematical limits to safeguard your mental bandwidth.",
                    bulletPoints = listOf(
                        "🐸 1 Primary FROG Task (Type A): The single most impactful high-demand task of the day. Completing it guarantees a successful day.",
                        "⚡ 3 Medium Tasks (Type B): Secondary projects with moderate cognitive load (writing, analysis, meetings).",
                        "✨ 5 Quick Wins (Type C): Frictionless logistics, admin, or payments (<15 min).",
                        "⏱️ 210 min Cognitive Ceiling (3.5h): Real-time gauge alerts you in red if you schedule more deep work than your biology can handle."
                    ),
                    bioPrinciple = "Neuroscience: Forcing more than 3.5 hours of deep work daily creates cognitive debt and burnout.",
                    targetTab = 0,
                    exampleScenario = "Real Scenario: When you complete your Frog task, Aether OS displays a special celebration overlay 🔥 and unlocks achievement badges.",
                    actionTip = "Pro Tip: In the Backlog, tap the frog icon 🐸 to promote any task to today's Frog."
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Energy Backlog & Focus Pomodoro",
                    subtitle = "Zero-friction capture, energy filtering and background alarms",
                    iconName = "Bolt",
                    summary = "The 'Backlog' tab stores all pending tasks organized by biological energy cost (High, Medium, Low) rather than endless unmanageable lists.",
                    bulletPoints = listOf(
                        "Quick Capture (+): Log any task in seconds with title, energy level, and estimated duration.",
                        "Dynamic Energy Filter: Feeling tired in the afternoon? Filter by 'Low' to execute quick micro-wins without friction.",
                        "Integrated Focus Timer: 25-minute single-tasking sprints tied to your active task.",
                        "Background WorkManager Alarms: Schedules a notification that triggers when your timer ends even if the screen is locked.",
                        "Smart Search & Pending Filter: Instantly find any task by keywords or category."
                    ),
                    bioPrinciple = "Strategy: Match the task to your current biological energy instead of forcing motivation.",
                    targetTab = 1,
                    exampleScenario = "Real Scenario: You have 30 minutes before lunch. Filter by 'Low', select answering 2 emails, and start the timer.",
                    actionTip = "Pro Tip: Grant notification permissions so the timer alerts you upon completion without keeping the app open."
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Relational Nutrition & Batch Cooking",
                    subtitle = "Meals in <8 minutes and sustained dopamine without post-meal crashes",
                    iconName = "Restaurant",
                    summary = "Food decision fatigue drains executive function. Aether OS links your real pantry inventory to swift, nourishing meals built from pre-cooked batch bases.",
                    bulletPoints = listOf(
                        "Batch Cooking Bases: Cook universal bases 1-2x weekly (Quinoa, Sweet Potato, Roasted Chicken, Eggs) and assemble meals in under 8 minutes.",
                        "Smart Pantry & Stock: Toggle what you have in stock with a single tap (In Stock / Out of Stock) to generate an automated shopping list.",
                        "Anti-Spike Glycemic Design: Meals calibrated with low glycemic impact and healthy fats for steady dopamine without brain fog."
                    ),
                    bioPrinciple = "Cellular Nutrition: Fuel your brain without insulin spikes that cause reactive fatigue and lethargy.",
                    targetTab = 2,
                    exampleScenario = "Real Scenario: Arriving home tired, check your available bases in Nutrition, reheat for 4 minutes, and eat healthy without ordering delivery.",
                    actionTip = "Pro Tip: Use the 'Pantry & Stock' tab to mark items that need replenishment before going to the supermarket."
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Circadian Habits & Grace Days",
                    subtitle = "Sunlight-anchored habits and guilt-free consistency",
                    iconName = "WbSunny",
                    summary = "Sustainable habits rely on natural biological cues (photons, temperature, hydration) rather than rigid willpower.",
                    bulletPoints = listOf(
                        "5 Master Circadian Anchors:\n  • ☀️ Morning Sunlight Photons (10-15 min) to calibrate your central pacemaker (SCN).\n  • 💧 Mineral Hydration upon waking.\n  • ☕ Caffeine Cutoff at 14:00 to protect adenosine and deep sleep.\n  • 🚶 Zone 2 Movement (30-45 min aerobic walk).\n  • 🌙 Digital Sunset at 22:00 to facilitate natural melatonin secretion.",
                        "Elastic Targets: 'Mini' version (crisis days), 'Plus' (daily standard), and 'Elite' (peak energy days).",
                        "🛡️ Grace Day Tokens: When unexpected fatigue or events happen, tap 'Apply Grace Day'. Your streak stays protected without guilt."
                    ),
                    bioPrinciple = "Behavioral Psychology: Slipping once is human; returning today is your true baseline. Zero guilt.",
                    targetTab = 3,
                    exampleScenario = "Real Scenario: An unexpected travel day prevented your workout. Activate a Grace Day: your 47-day streak stays intact and identity preserved.",
                    actionTip = "Pro Tip: Grace Days are a biological tool, not a cheat. Use them with strategic self-compassion."
                ),
                TutorialStep(
                    stepNumber = 7,
                    title = "Core AI & Cognitive Reframing",
                    subtitle = "Gemini orchestration, overcoming resistance and portable data",
                    iconName = "Psychology",
                    summary = "Access Aether's intelligent core to orchestrate your day and overcome cognitive friction.",
                    bulletPoints = listOf(
                        "Automatic Gemini Orchestration: Analyzes your readiness score, tasks, and chronotype to generate an optimal circadian schedule.",
                        "Deterministic Fallback Engine: If offline or without API key, the offline engine generates a balanced schedule instantly.",
                        "🧠 Cognitive Reframing Engine: Feeling stuck, guilty, or procrastinating? Submit your thought. AI responds with biological reframing to dissolve resistance.",
                        "Master JSON Schema: Inspect and export your complete daily plan as standard JSON to share or backup."
                    ),
                    bioPrinciple = "Holistic Integration: Your entire life orchestrated through an elegant, respectful, and exportable architecture.",
                    targetTab = 4,
                    exampleScenario = "Real Scenario: Feeling guilty for not finishing a project? Submit 'I feel guilty' and AI reminds you that after 7 hours of focus your prefrontal cortex needs recovery.",
                    actionTip = "Pro Tip: Tap 'Copy JSON' or 'Share' in the AI tab to export your daily plan to any other app."
                ),
                TutorialStep(
                    stepNumber = 8,
                    title = "Achievement System & Daily Mastery",
                    subtitle = "10 Bioenergetic Badges, Customization & Bilingual Support",
                    iconName = "EmojiEvents",
                    summary = "Aether OS rewards your continuous consistency and biological respect with 10 unlockable achievement badges that reinforce positive habits.",
                    bulletPoints = listOf(
                        "🏆 10 Unlockable Badges: Conquer your first Frog, reach 7-day and 30-day streaks, complete 10 and 100 tasks, use a Grace Day, and orchestrate with AI.",
                        "🔔 Unlock Banner: When reaching a milestone, an animated celebratory banner appears at the top.",
                        "🌐 Instant Bilingual Support: Toggle seamlessly between Spanish and English using the top bar chip [🇪🇸/🇬🇧].",
                        "⚙️ Clean Slate vs Demo Data: Reset to a clean baseline or load demo data anytime in Settings."
                    ),
                    bioPrinciple = "Intrinsic Dopamine: Rewarding small wins strengthens neural pathways of consistency.",
                    targetTab = 0,
                    exampleScenario = "Real Scenario: Completing 7 consecutive days of circadian habits unlocks the 'Circadian Consistency' trophy in your achievements dialog.",
                    actionTip = "Pro Tip: Open Settings ⚙️ in the top right corner and tap 'View Achievement Showcase' to see your badges."
                )
            )
        }
    }
}

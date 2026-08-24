package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        TaskEntity::class,
        TimeBlockEntity::class,
        PantryEntity::class,
        MealEntity::class,
        HabitEntity::class,
        BiometricEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun pantryDao(): PantryDao
    abstract fun mealDao(): MealDao
    abstract fun habitDao(): HabitDao
    abstract fun biometricDao(): BiometricDao

    companion object {
        @Volatile
        private var INSTANCE: AetherDatabase? = null

        fun getDatabase(context: Context): AetherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_os_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialAetherData(database, AppLanguage.SPANISH)
                    }
                }
            }
        }

        suspend fun clearAllAetherData(database: AetherDatabase) {
            val taskDao = database.taskDao()
            val timeBlockDao = database.timeBlockDao()
            val pantryDao = database.pantryDao()
            val mealDao = database.mealDao()
            val habitDao = database.habitDao()

            taskDao.clearAllTasks()
            timeBlockDao.clearAllTimeBlocks()
            pantryDao.clearAllPantry()
            mealDao.clearAllMeals()
            habitDao.clearAllHabits()
        }

        suspend fun populateInitialAetherData(database: AetherDatabase, language: AppLanguage = AppLanguage.SPANISH) {
            val taskDao = database.taskDao()
            val timeBlockDao = database.timeBlockDao()
            val pantryDao = database.pantryDao()
            val mealDao = database.mealDao()
            val habitDao = database.habitDao()
            val biometricDao = database.biometricDao()

            // 1. Initial Biometrics
            biometricDao.insertBiometric(
                BiometricEntity(
                    date = "2026-08-22",
                    readinessScore = 78,
                    perceivedEnergy = 80,
                    sleepHours = 7.8,
                    sleepQuality = 4,
                    chronotype = Chronotype.BEAR,
                    recoveryModeTriggered = false,
                    graceDayActive = false
                )
            )

            if (language == AppLanguage.SPANISH) {
                // SPANISH SEED DATA
                val spanishTasks = listOf(
                    // 1 Frog (Type A - Máximo 1)
                    TaskEntity(
                        id = "task-frog-1",
                        title = "Arquitectura del Motor Central y Despacho Aether",
                        description = "Diseñar máquina de estados y pruebas de límites para el módulo central bioenergético.",
                        energyLevel = EnergyLevel.HIGH,
                        priorityType = PriorityType.FROG,
                        estimatedMinutes = 90,
                        isCompleted = false,
                        isFrog = true,
                        scheduledTime = "09:30",
                        category = "Trabajo Profundo"
                    ),
                    // 3 Medium Tasks (Type B)
                    TaskEntity(
                        id = "task-med-1",
                        title = "Revisar Bioanalíticas Semanales y Tendencias de Sueño",
                        description = "Analizar curvas de recuperación de pulso cardíaco y ajustar ventana de cafeína.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 30,
                        isCompleted = false,
                        isFrog = false,
                        scheduledTime = "14:30",
                        category = "Biometría"
                    ),
                    TaskEntity(
                        id = "task-med-2",
                        title = "Batch Cooking: Base de Quinoa y Boniato para 3 Días",
                        description = "Ejecutar protocolo de cocción en lote para anular la fatiga de decisión en el almuerzo.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 45,
                        isCompleted = true,
                        isFrog = false,
                        scheduledTime = "17:30",
                        category = "Nutrición"
                    ),
                    TaskEntity(
                        id = "task-med-3",
                        title = "Sintetizar Estudio de Cambios de Fase Circadiana",
                        description = "Redactar memorando conciso sobre tiempos de exposición a luxes para cronotipo lobo.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 40,
                        isCompleted = false,
                        isFrog = false,
                        scheduledTime = "11:30",
                        category = "Trabajo Profundo"
                    ),
                    // 5 Quick Tasks (Type C)
                    TaskEntity(
                        id = "task-quick-1",
                        title = "Limpiar bandeja de entrada a Cero (Inbox Zero)",
                        description = "Procesamiento por lotes en 10 minutos.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 15,
                        isCompleted = false,
                        isFrog = false,
                        category = "Administración"
                    ),
                    TaskEntity(
                        id = "task-quick-2",
                        title = "Reponer Aceite de Oliva Virgen Extra Ecológico",
                        description = "Añadir a la lista de despensa inteligente.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = true,
                        isFrog = false,
                        category = "Logística"
                    ),
                    TaskEntity(
                        id = "task-quick-3",
                        title = "Regar el jardín de hierbas aromáticas",
                        description = "Micro-pausa sensorial y desconexión visual.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 10,
                        isCompleted = false,
                        isFrog = false,
                        category = "Hábito"
                    ),
                    TaskEntity(
                        id = "task-quick-4",
                        title = "Confirmar horario de retrospectiva del equipo",
                        description = "Mensaje asíncrono rápido.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = false,
                        isFrog = false,
                        category = "Administración"
                    ),
                    TaskEntity(
                        id = "task-quick-5",
                        title = "Verificar nivel de batería de equipos de respaldo",
                        description = "Chequeo de mantenimiento preventivo.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = false,
                        isFrog = false,
                        category = "Logística"
                    )
                )
                taskDao.insertTasks(spanishTasks)

                // Spanish Time Blocks (Cognitive Ceiling Law <= 210 mins deep work)
                val spanishBlocks = listOf(
                    TimeBlockEntity(
                        id = "block-1",
                        startTime = "07:00",
                        endTime = "08:00",
                        blockType = BlockType.HABIT_ANCHOR,
                        title = "Ancla Fotónica & Recarga de Hidratación con Electrolitos",
                        isCompleted = true,
                        sortOrder = 1
                    ),
                    TimeBlockEntity(
                        id = "block-2",
                        startTime = "08:00",
                        endTime = "08:45",
                        blockType = BlockType.MEAL,
                        title = "Desayuno de Bajo Impacto Glucémico (Combustible Estable)",
                        isCompleted = true,
                        sortOrder = 2
                    ),
                    TimeBlockEntity(
                        id = "block-3",
                        startTime = "09:00",
                        endTime = "10:45", // 105 min Deep Work
                        blockType = BlockType.DEEP_WORK,
                        title = "🔥 ENFOQUE FROG: Arquitectura del Motor Central",
                        isCompleted = false,
                        linkedTaskId = "task-frog-1",
                        sortOrder = 3
                    ),
                    TimeBlockEntity(
                        id = "block-4",
                        startTime = "10:45",
                        endTime = "11:15",
                        blockType = BlockType.COGNITIVE_RECOVERY_BUFFER,
                        title = "Descanso Ocular Activo & Caminata Zona 1",
                        isCompleted = false,
                        sortOrder = 4
                    ),
                    TimeBlockEntity(
                        id = "block-5",
                        startTime = "11:15",
                        endTime = "12:15", // 60 min Deep Work
                        blockType = BlockType.DEEP_WORK,
                        title = "Síntesis de Cambios de Fase & Notas Técnicas",
                        isCompleted = false,
                        linkedTaskId = "task-med-3",
                        sortOrder = 5
                    ),
                    TimeBlockEntity(
                        id = "block-6",
                        startTime = "12:30",
                        endTime = "13:30",
                        blockType = BlockType.MEAL,
                        title = "Bowl Base de Quinoa + Paseo Postprandial",
                        isCompleted = false,
                        sortOrder = 6
                    ),
                    TimeBlockEntity(
                        id = "block-7",
                        startTime = "14:00",
                        endTime = "15:00",
                        blockType = BlockType.MEETING,
                        title = "Alineación Asíncrona / Coordinación de Equipo",
                        isCompleted = false,
                        sortOrder = 7
                    ),
                    TimeBlockEntity(
                        id = "block-8",
                        startTime = "15:30",
                        endTime = "16:30",
                        blockType = BlockType.ADMIN_SLOT,
                        title = "Triaje Administrativo y Tareas Rápidas en Bloque",
                        isCompleted = false,
                        sortOrder = 8
                    ),
                    TimeBlockEntity(
                        id = "block-9",
                        startTime = "22:00",
                        endTime = "23:00",
                        blockType = BlockType.HABIT_ANCHOR,
                        title = "Atardecer Digital & Disociación de Pantallas",
                        isCompleted = false,
                        sortOrder = 9
                    )
                )
                timeBlockDao.insertTimeBlocks(spanishBlocks)

                // Spanish Pantry
                val spanishPantry = listOf(
                    PantryEntity("p-1", "Huevos Camperos Ecológicos", PantryCategory.PROTEIN, inStock = true, isBatchBase = false, "6 huevos"),
                    PantryEntity("p-2", "Quinoa Tricolor Cocida", PantryCategory.CARB_BASE, inStock = true, isBatchBase = true, "Lote 500g"),
                    PantryEntity("p-3", "Lomo de Salmón Salvaje", PantryCategory.PROTEIN, inStock = true, isBatchBase = false, "2 raciones"),
                    PantryEntity("p-4", "Boniatos Asados al Horno", PantryCategory.CARB_BASE, inStock = true, isBatchBase = true, "3 recipientes"),
                    PantryEntity("p-5", "Espinacas Baby y Rúcula", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "Bolsa fresca"),
                    PantryEntity("p-6", "Aguacates Maduros", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "3 unidades"),
                    PantryEntity("p-7", "Aceite de Oliva Virgen Extra", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "Botella llena"),
                    PantryEntity("p-8", "Semillas de Chía y Cáñamo", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "1 tarro"),
                    PantryEntity("p-9", "Arándanos Frescos", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "1 tarrina"),
                    PantryEntity("p-10", "Yogur Griego de Pasto", PantryCategory.PROTEIN, inStock = false, isBatchBase = false, "Por reponer"),
                    PantryEntity("p-11", "Chucrut / Kimchi Fermentado", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "Medio tarro")
                )
                pantryDao.insertItems(spanishPantry)

                // Spanish Meals
                val spanishMeals = listOf(
                    MealEntity(
                        id = "meal-1",
                        slot = MealSlot.BREAKFAST,
                        title = "Revuelto de Huevos Camperos con Aguacate y Arándanos",
                        description = "Desayuno de bajo impacto glucémico que aporta dopamina estable y previene picos de glucosa.",
                        prepTimeMinutes = 10,
                        ingredients = listOf("Huevos Camperos Ecológicos", "Espinacas Baby y Rúcula", "Aguacates Maduros", "Arándanos Frescos"),
                        usesBatchCookedBase = false,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                        isCompleted = true
                    ),
                    MealEntity(
                        id = "meal-2",
                        slot = MealSlot.LUNCH,
                        title = "Bowl Energético de Base de Quinoa con Salmón",
                        description = "Aprovecha la base de quinoa precocinada para cocinar en menos de 8 minutos con cero fricción.",
                        prepTimeMinutes = 8,
                        ingredients = listOf("Quinoa Tricolor Cocida", "Lomo de Salmón Salvaje", "Espinacas Baby y Rúcula", "Aceite de Oliva Virgen Extra", "Chucrut / Kimchi Fermentado"),
                        usesBatchCookedBase = true,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.MODERATE_STEADY,
                        isCompleted = false
                    ),
                    MealEntity(
                        id = "meal-3",
                        slot = MealSlot.DINNER,
                        title = "Base de Boniato Asado con Hojas Verdes y Grasas Saludables",
                        description = "Recarga de carbohidratos complejos para estimular el sistema nervioso parasimpático y preparar el descanso.",
                        prepTimeMinutes = 12,
                        ingredients = listOf("Boniatos Asados al Horno", "Espinacas Baby y Rúcula", "Aceite de Oliva Virgen Extra", "Semillas de Chía y Cáñamo"),
                        usesBatchCookedBase = true,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.DEEP_RECOVERY,
                        isCompleted = false
                    ),
                    MealEntity(
                        id = "meal-4",
                        slot = MealSlot.SNACK,
                        title = "Nueces Crudas e Infusión de Té Matcha",
                        description = "Polifenoles limpios y energía cerebral de liberación progresiva.",
                        prepTimeMinutes = 3,
                        ingredients = listOf("Semillas de Chía y Cáñamo"),
                        usesBatchCookedBase = false,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                        isCompleted = false
                    )
                )
                mealDao.insertMeals(spanishMeals)

                // Spanish Habits
                val spanishHabits = listOf(
                    HabitAnchor(
                        id = "habit-1",
                        title = "Ancla Fotónica de Luz Solar Matutina",
                        description = "10-15 minutos de luz natural directa para calibrar el núcleo supraquiasmático (SCN).",
                        anchor = CircadianAnchor.MORNING_LIGHT,
                        isCompleted = true,
                        streakDays = 12,
                        graceDaysUsed = 1,
                        reframingTip = "Fallar un día es humano; retomar hoy consolida tu línea base."
                    ),
                    HabitAnchor(
                        id = "habit-2",
                        title = "500ml de Agua con Electrolitos Minerales",
                        description = "Rehidratación celular inmediata al despertar para encender la función ejecutiva.",
                        anchor = CircadianAnchor.HYDRATION_ELECTROLYTES,
                        isCompleted = true,
                        streakDays = 18,
                        graceDaysUsed = 0,
                        reframingTip = "La hidratación celular potencia la claridad mental."
                    ),
                    HabitAnchor(
                        id = "habit-3",
                        title = "Límite de Cafeína a las 14:00 (Depuración de Adenosina)",
                        description = "Permite la semivida de 8 horas para proteger la fase profunda del sueño.",
                        anchor = CircadianAnchor.CAFFEINE_CUTOFF,
                        isCompleted = false,
                        streakDays = 7,
                        graceDaysUsed = 2,
                        reframingTip = "Protege tu arquitectura de sueño sin culpa."
                    ),
                    HabitAnchor(
                        id = "habit-4",
                        title = "Caminata de Descompresión en Zona 2",
                        description = "20 minutos de movimiento suave para depurar lactato y reiniciar el foco.",
                        anchor = CircadianAnchor.ZONE_2_MOVEMENT,
                        isCompleted = false,
                        streakDays = 4,
                        graceDaysUsed = 1,
                        reframingTip = "El movimiento rítmico calma el sistema nervioso."
                    ),
                    HabitAnchor(
                        id = "habit-5",
                        title = "Atardecer Digital (Apagar Pantallas a las 22:00)",
                        description = "Bloqueo de fotones azules para inducir la secreción endógena de melatonina.",
                        anchor = CircadianAnchor.DIGITAL_SUNSET,
                        isCompleted = false,
                        streakDays = 6,
                        graceDaysUsed = 1,
                        reframingTip = "La noche está reservada para la regeneración biológica."
                    )
                )
                habitDao.insertHabits(spanishHabits.map {
                    HabitEntity(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        anchor = it.anchor,
                        isCompleted = it.isCompleted,
                        streakDays = it.streakDays,
                        graceDaysUsed = it.graceDaysUsed,
                        reframingTip = it.reframingTip
                    )
                })

            } else {
                // ENGLISH SEED DATA
                val englishTasks = listOf(
                    TaskEntity(
                        id = "task-frog-1",
                        title = "Architect Core Engine Logic Specification",
                        description = "Write complete state machine & boundary tests for Aether OS core dispatch module.",
                        energyLevel = EnergyLevel.HIGH,
                        priorityType = PriorityType.FROG,
                        estimatedMinutes = 90,
                        isCompleted = false,
                        isFrog = true,
                        scheduledTime = "09:30 AM",
                        category = "Deep Work"
                    ),
                    TaskEntity(
                        id = "task-med-1",
                        title = "Review Weekly Bio-Analytics & Sleep Trends",
                        description = "Examine heart rate recovery curves and adjust afternoon caffeine window.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 30,
                        isCompleted = false,
                        isFrog = false,
                        scheduledTime = "02:30 PM",
                        category = "Biometrics"
                    ),
                    TaskEntity(
                        id = "task-med-2",
                        title = "Meal Prep: 3-Day Quinoa & Sweet Potato Base",
                        description = "Execute batch cooking protocol to eliminate lunch decision fatigue.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 45,
                        isCompleted = true,
                        isFrog = false,
                        scheduledTime = "05:30 PM",
                        category = "Nutrition"
                    ),
                    TaskEntity(
                        id = "task-med-3",
                        title = "Synthesize Research on Circadian Phase Shifts",
                        description = "Draft concise bullet memo on lux exposure timing for wolf chronotype.",
                        energyLevel = EnergyLevel.MEDIUM,
                        priorityType = PriorityType.MEDIUM,
                        estimatedMinutes = 40,
                        isCompleted = false,
                        isFrog = false,
                        scheduledTime = "11:30 AM",
                        category = "Deep Work"
                    ),
                    TaskEntity(
                        id = "task-quick-1",
                        title = "Clear inbox to Zero / Archive triage",
                        description = "Standard 10m batch processing.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 15,
                        isCompleted = false,
                        isFrog = false,
                        category = "Admin"
                    ),
                    TaskEntity(
                        id = "task-quick-2",
                        title = "Restock Organic Cold-Pressed Olive Oil",
                        description = "Add to smart pantry list.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = true,
                        isFrog = false,
                        category = "Logistics"
                    ),
                    TaskEntity(
                        id = "task-quick-3",
                        title = "Water balcony herb garden",
                        description = "Sensory micro-break.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 10,
                        isCompleted = false,
                        isFrog = false,
                        category = "Habit"
                    ),
                    TaskEntity(
                        id = "task-quick-4",
                        title = "Confirm Friday team sprint retro slot",
                        description = "Fast async ping.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = false,
                        isFrog = false,
                        category = "Admin"
                    ),
                    TaskEntity(
                        id = "task-quick-5",
                        title = "Check backup battery levels",
                        description = "Gear maintenance check.",
                        energyLevel = EnergyLevel.LOW,
                        priorityType = PriorityType.QUICK,
                        estimatedMinutes = 5,
                        isCompleted = false,
                        isFrog = false,
                        category = "Logistics"
                    )
                )
                taskDao.insertTasks(englishTasks)

                val englishBlocks = listOf(
                    TimeBlockEntity(
                        id = "block-1",
                        startTime = "07:00",
                        endTime = "08:00",
                        blockType = BlockType.HABIT_ANCHOR,
                        title = "Photonic Anchor & Hydration Charge",
                        isCompleted = true,
                        sortOrder = 1
                    ),
                    TimeBlockEntity(
                        id = "block-2",
                        startTime = "08:00",
                        endTime = "08:45",
                        blockType = BlockType.MEAL,
                        title = "Low-Glycemic Sustained Fuel Breakfast",
                        isCompleted = true,
                        sortOrder = 2
                    ),
                    TimeBlockEntity(
                        id = "block-3",
                        startTime = "09:00",
                        endTime = "10:45",
                        blockType = BlockType.DEEP_WORK,
                        title = "🔥 FROG FOCUS: Core Engine Specification",
                        isCompleted = false,
                        linkedTaskId = "task-frog-1",
                        sortOrder = 3
                    ),
                    TimeBlockEntity(
                        id = "block-4",
                        startTime = "10:45",
                        endTime = "11:15",
                        blockType = BlockType.COGNITIVE_RECOVERY_BUFFER,
                        title = "Active Eye-Rest & Zone 1 Walk",
                        isCompleted = false,
                        sortOrder = 4
                    ),
                    TimeBlockEntity(
                        id = "block-5",
                        startTime = "11:15",
                        endTime = "12:15",
                        blockType = BlockType.DEEP_WORK,
                        title = "Phase Shift Synthesis & Technical Notes",
                        isCompleted = false,
                        linkedTaskId = "task-med-3",
                        sortOrder = 5
                    ),
                    TimeBlockEntity(
                        id = "block-6",
                        startTime = "12:30",
                        endTime = "13:30",
                        blockType = BlockType.MEAL,
                        title = "Batch Base Quinoa Bowl + Digestion Stroll",
                        isCompleted = false,
                        sortOrder = 6
                    ),
                    TimeBlockEntity(
                        id = "block-7",
                        startTime = "14:00",
                        endTime = "15:00",
                        blockType = BlockType.MEETING,
                        title = "Async Sync / Cross-Team Alignment",
                        isCompleted = false,
                        sortOrder = 7
                    ),
                    TimeBlockEntity(
                        id = "block-8",
                        startTime = "15:30",
                        endTime = "16:30",
                        blockType = BlockType.ADMIN_SLOT,
                        title = "Admin Triage & Logistics Batching",
                        isCompleted = false,
                        sortOrder = 8
                    ),
                    TimeBlockEntity(
                        id = "block-9",
                        startTime = "22:00",
                        endTime = "23:00",
                        blockType = BlockType.HABIT_ANCHOR,
                        title = "Digital Sunset & Melatonin Buffer",
                        isCompleted = false,
                        sortOrder = 9
                    )
                )
                timeBlockDao.insertTimeBlocks(englishBlocks)

                val englishPantry = listOf(
                    PantryEntity("p-1", "Organic Eggs", PantryCategory.PROTEIN, inStock = true, isBatchBase = false, "6 eggs"),
                    PantryEntity("p-2", "Cooked Tricolor Quinoa", PantryCategory.CARB_BASE, inStock = true, isBatchBase = true, "500g batch"),
                    PantryEntity("p-3", "Wild Salmon Fillet", PantryCategory.PROTEIN, inStock = true, isBatchBase = false, "2 portions"),
                    PantryEntity("p-4", "Roasted Sweet Potatoes", PantryCategory.CARB_BASE, inStock = true, isBatchBase = true, "3 containers"),
                    PantryEntity("p-5", "Baby Spinach & Arugula", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "Fresh bag"),
                    PantryEntity("p-6", "Avocados", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "3 whole"),
                    PantryEntity("p-7", "Extra Virgin Olive Oil", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "Full bottle"),
                    PantryEntity("p-8", "Chia Seeds & Hemp Hearts", PantryCategory.HEALTHY_FAT, inStock = true, isBatchBase = false, "1 jar"),
                    PantryEntity("p-9", "Organic Blueberries", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "1 box"),
                    PantryEntity("p-10", "Grass-Fed Greek Yogurt", PantryCategory.PROTEIN, inStock = false, isBatchBase = false, "Need restock"),
                    PantryEntity("p-11", "Kimchi / Fermented Slaw", PantryCategory.PRODUCE, inStock = true, isBatchBase = false, "Half jar")
                )
                pantryDao.insertItems(englishPantry)

                val englishMeals = listOf(
                    MealEntity(
                        id = "meal-1",
                        slot = MealSlot.BREAKFAST,
                        title = "Avocado & Pastured Egg Scramble with Blueberries",
                        description = "Low-glycemic breakfast delivering stable dopamine and avoiding glucose spikes.",
                        prepTimeMinutes = 10,
                        ingredients = listOf("Organic Eggs", "Baby Spinach & Arugula", "Avocados", "Organic Blueberries"),
                        usesBatchCookedBase = false,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                        isCompleted = true
                    ),
                    MealEntity(
                        id = "meal-2",
                        slot = MealSlot.LUNCH,
                        title = "Batch Base Quinoa Power Bowl with Salmon",
                        description = "Utilizes pre-cooked Tricolor Quinoa base for zero cooking friction.",
                        prepTimeMinutes = 8,
                        ingredients = listOf("Cooked Tricolor Quinoa", "Wild Salmon Fillet", "Baby Spinach & Arugula", "Extra Virgin Olive Oil", "Kimchi / Fermented Slaw"),
                        usesBatchCookedBase = true,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.MODERATE_STEADY,
                        isCompleted = false
                    ),
                    MealEntity(
                        id = "meal-3",
                        slot = MealSlot.DINNER,
                        title = "Roasted Sweet Potato Base with Greens & Healthy Fats",
                        description = "Carbohydrate replenishment for parasympathetic nervous system down-regulation.",
                        prepTimeMinutes = 12,
                        ingredients = listOf("Roasted Sweet Potatoes", "Baby Spinach & Arugula", "Extra Virgin Olive Oil", "Chia Seeds & Hemp Hearts"),
                        usesBatchCookedBase = true,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.DEEP_RECOVERY,
                        isCompleted = false
                    ),
                    MealEntity(
                        id = "meal-4",
                        slot = MealSlot.SNACK,
                        title = "Handful Walnuts & Green Matcha Infusion",
                        description = "Clean polyphenols and slow release brain fuel.",
                        prepTimeMinutes = 3,
                        ingredients = listOf("Chia Seeds & Hemp Hearts"),
                        usesBatchCookedBase = false,
                        allIngredientsInStock = true,
                        bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                        isCompleted = false
                    )
                )
                mealDao.insertMeals(englishMeals)

                val englishHabits = listOf(
                    HabitAnchor("habit-1", "Morning Sunlight Photonic Anchor", "10-15 minutes direct outdoor light to reset master circadian clock (SCN).", CircadianAnchor.MORNING_LIGHT, true, 12, 1, "Missing once is human; returning today is your baseline."),
                    HabitAnchor("habit-2", "500ml Water + Mineral Electrolytes", "Rehydrate neural tissue immediately upon waking.", CircadianAnchor.HYDRATION_ELECTROLYTES, true, 18, 0, "Cellular hydration primes executive function."),
                    HabitAnchor("habit-3", "Caffeine Stop at 14:00 (Adenosine Clearance)", "Allows 8h half-life decay to safeguard Stage 3 Deep Sleep.", CircadianAnchor.CAFFEINE_CUTOFF, false, 7, 2, "Protect your sleep architecture without guilt."),
                    HabitAnchor("habit-4", "Zone 2 Decompression Stroll", "20 mins low intensity movement for lactate clearance and cognitive reset.", CircadianAnchor.ZONE_2_MOVEMENT, false, 4, 1, "Movement calms the nervous system."),
                    HabitAnchor("habit-5", "Digital Sunset (Screens Dark at 22:00)", "Suppress blue photons to stimulate natural melatonin synthesis.", CircadianAnchor.DIGITAL_SUNSET, false, 6, 1, "The night belongs to biological rejuvenation.")
                )
                habitDao.insertHabits(englishHabits.map {
                    HabitEntity(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        anchor = it.anchor,
                        isCompleted = it.isCompleted,
                        streakDays = it.streakDays,
                        graceDaysUsed = it.graceDaysUsed,
                        reframingTip = it.reframingTip
                    )
                })
            }
        }
    }
}

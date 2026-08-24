package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return stringListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromEnergyLevel(value: EnergyLevel?): String = value?.name ?: EnergyLevel.MEDIUM.name

    @TypeConverter
    fun toEnergyLevel(value: String?): EnergyLevel = try {
        EnergyLevel.valueOf(value ?: EnergyLevel.MEDIUM.name)
    } catch (e: Exception) {
        EnergyLevel.MEDIUM
    }

    @TypeConverter
    fun fromPriorityType(value: PriorityType?): String = value?.name ?: PriorityType.QUICK.name

    @TypeConverter
    fun toPriorityType(value: String?): PriorityType = try {
        PriorityType.valueOf(value ?: PriorityType.QUICK.name)
    } catch (e: Exception) {
        PriorityType.QUICK
    }

    @TypeConverter
    fun fromBlockType(value: BlockType?): String = value?.name ?: BlockType.ADMIN_SLOT.name

    @TypeConverter
    fun toBlockType(value: String?): BlockType = try {
        BlockType.valueOf(value ?: BlockType.ADMIN_SLOT.name)
    } catch (e: Exception) {
        BlockType.ADMIN_SLOT
    }

    @TypeConverter
    fun fromChronotype(value: Chronotype?): String = value?.name ?: Chronotype.BEAR.name

    @TypeConverter
    fun toChronotype(value: String?): Chronotype = try {
        Chronotype.valueOf(value ?: Chronotype.BEAR.name)
    } catch (e: Exception) {
        Chronotype.BEAR
    }

    @TypeConverter
    fun fromPantryCategory(value: PantryCategory?): String = value?.name ?: PantryCategory.PROTEIN.name

    @TypeConverter
    fun toPantryCategory(value: String?): PantryCategory = try {
        PantryCategory.valueOf(value ?: PantryCategory.PROTEIN.name)
    } catch (e: Exception) {
        PantryCategory.PROTEIN
    }

    @TypeConverter
    fun fromMealSlot(value: MealSlot?): String = value?.name ?: MealSlot.BREAKFAST.name

    @TypeConverter
    fun toMealSlot(value: String?): MealSlot = try {
        MealSlot.valueOf(value ?: MealSlot.BREAKFAST.name)
    } catch (e: Exception) {
        MealSlot.BREAKFAST
    }

    @TypeConverter
    fun fromBioImpact(value: BioGlycemicImpact?): String = value?.name ?: BioGlycemicImpact.LOW_GLYCEMIC_FOCUS.name

    @TypeConverter
    fun toBioImpact(value: String?): BioGlycemicImpact = try {
        BioGlycemicImpact.valueOf(value ?: BioGlycemicImpact.LOW_GLYCEMIC_FOCUS.name)
    } catch (e: Exception) {
        BioGlycemicImpact.LOW_GLYCEMIC_FOCUS
    }

    @TypeConverter
    fun fromCircadianAnchor(value: CircadianAnchor?): String = value?.name ?: CircadianAnchor.MORNING_LIGHT.name

    @TypeConverter
    fun toCircadianAnchor(value: String?): CircadianAnchor = try {
        CircadianAnchor.valueOf(value ?: CircadianAnchor.MORNING_LIGHT.name)
    } catch (e: Exception) {
        CircadianAnchor.MORNING_LIGHT
    }
}

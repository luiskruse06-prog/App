package de.luis.bulktracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.luis.bulktracker.domain.model.MealSlot
import java.time.LocalDate
import java.time.LocalTime

/*
 * Portionen nach Handmethode als Double, damit auch halbe Portionen gehen.
 * kcal und proteinG werden beim Eintragen BERECHNET UND FESTGESCHRIEBEN: Ändern sich später
 * die Umrechnungsfaktoren, bleiben vergangene Tage unverändert (Entscheidung aus der Planung).
 */

@Entity(
    tableName = "meal_entry",
    foreignKeys = [
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = MealTemplateEntity::class, parentColumns = ["id"], childColumns = ["templateId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("date"), Index("recipeId"), Index("templateId")],
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val slot: MealSlot,
    val name: String,
    val proteinPortions: Double,
    val carbPortions: Double,
    val vegPortions: Double,
    val fatPortions: Double,
    val extraKcal: Int,
    val kcal: Int,
    val proteinG: Double,
    val recipeId: Long? = null,
    val templateId: Long? = null,
)

@Entity(tableName = "meal_template")
data class MealTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultSlot: MealSlot,
    val proteinPortions: Double,
    val carbPortions: Double,
    val vegPortions: Double,
    val fatPortions: Double,
    val extraKcal: Int,
    val sortOrder: Int,
)

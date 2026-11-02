package de.luis.bulktracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.luis.bulktracker.domain.model.Appliance
import de.luis.bulktracker.domain.model.RecipeCategory
import java.time.Instant

/** Portionen-Felder gelten PRO PERSON; kcal/proteinG sind die grobe Schätzung pro Person. */
@Entity(tableName = "recipe", indices = [Index("category"), Index("favorite")])
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: RecipeCategory,
    val prepMinutes: Int?,
    val servings: Int,
    val proteinPortions: Double,
    val carbPortions: Double,
    val vegPortions: Double,
    val fatPortions: Double,
    val kcal: Int?,
    val proteinG: Double?,
    val favorite: Boolean = false,
    val sourceUrl: String? = null,
    val imageUrl: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "recipe_ingredient",
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("recipeId")],
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val position: Int,
    val name: String,
    val amount: Double?,
    val unit: String?,
)

@Entity(
    tableName = "recipe_step",
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("recipeId")],
)
data class RecipeStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val position: Int,
    val text: String,
)

@Entity(
    tableName = "recipe_appliance",
    primaryKeys = ["recipeId", "appliance"],
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
)
data class RecipeApplianceEntity(
    val recipeId: Long,
    val appliance: Appliance,
)

@Entity(
    tableName = "recipe_tag",
    primaryKeys = ["recipeId", "tag"],
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tag")],
)
data class RecipeTagEntity(
    val recipeId: Long,
    val tag: String,
)

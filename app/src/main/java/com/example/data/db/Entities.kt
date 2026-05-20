package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,      // Color of the planet, e.g. #FF3366
    val relativeSize: Float,   // Scale size of the planet (e.g. 1.0f is default)
    val orbitRadius: Float,    // Orbit distance from central sun in dp
    val orbitSpeed: Float,     // Orbital speed modifier (e.g. 1.0f is default, negative is retrograde)
    val description: String
) {
    companion object {
        fun createDefaults(): List<CategoryEntity> {
            return listOf(
                CategoryEntity(
                    id = 1,
                    name = "Zenith",
                    colorHex = "#BB86FC", // Radiant Amethyst
                    relativeSize = 1.2f,
                    orbitRadius = 85f,
                    orbitSpeed = 0.6f,
                    description = "Core priorities, goals, and critical tasks"
                ),
                CategoryEntity(
                    id = 2,
                    name = "Ignis",
                    colorHex = "#CF6679", // Supernova Crimson retrograde
                    relativeSize = 0.9f,
                    orbitRadius = 145f,
                    orbitSpeed = -0.4f, // Retrograde orbit
                    description = "Creative ambitions, passions, and wild dreams"
                ),
                CategoryEntity(
                    id = 3,
                    name = "Musa",
                    colorHex = "#03DAC6", // Bioluminescent Aurora Teal
                    relativeSize = 1.0f,
                    orbitRadius = 205f,
                    orbitSpeed = 0.25f,
                    description = "Transient musings, fast captures, and creative writing"
                ),
                CategoryEntity(
                    id = 4,
                    name = "Terra",
                    colorHex = "#00E5FF", // Light speed tracer cyan
                    relativeSize = 1.1f,
                    orbitRadius = 265f,
                    orbitSpeed = -0.15f,
                    description = "Routines, personal learning, and life systems"
                )
            )
        }
    }
}

@Entity(tableName = "thoughts")
data class ThoughtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,       // Links to CategoryEntity
    val title: String,
    val content: String,
    val status: String = "Active", // Active, Completed, Archived
    val importance: Float = 1.0f,  // Controls satellite size
    val createdAt: Long = System.currentTimeMillis()
)

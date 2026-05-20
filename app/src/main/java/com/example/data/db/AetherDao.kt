package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AetherDao {
    @Query("SELECT * FROM categories ORDER BY orbitRadius ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM thoughts ORDER BY createdAt DESC")
    fun getAllThoughts(): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getThoughtsByCategory(categoryId: Int): Flow<List<ThoughtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: ThoughtEntity): Long

    @Update
    suspend fun updateThought(thought: ThoughtEntity)

    @Delete
    suspend fun deleteThought(thought: ThoughtEntity)

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun deleteThoughtById(id: Int)
}

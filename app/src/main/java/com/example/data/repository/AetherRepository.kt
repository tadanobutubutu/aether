package com.example.data.repository

import com.example.data.db.AetherDao
import com.example.data.db.CategoryEntity
import com.example.data.db.ThoughtEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AetherRepository(private val aetherDao: AetherDao) {
    val allCategories: Flow<List<CategoryEntity>> = aetherDao.getAllCategories()
    val allThoughts: Flow<List<ThoughtEntity>> = aetherDao.getAllThoughts()

    suspend fun checkAndPrepopulate() {
        val currentCategories = allCategories.first()
        if (currentCategories.isEmpty()) {
            aetherDao.insertCategories(CategoryEntity.createDefaults())
        }
    }

    suspend fun insertCategory(category: CategoryEntity) = aetherDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = aetherDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = aetherDao.deleteCategory(category)

    suspend fun insertThought(thought: ThoughtEntity): Long = aetherDao.insertThought(thought)
    suspend fun updateThought(thought: ThoughtEntity) = aetherDao.updateThought(thought)
    suspend fun deleteThought(thought: ThoughtEntity) = aetherDao.deleteThought(thought)
    suspend fun deleteThoughtById(id: Int) = aetherDao.deleteThoughtById(id)
}

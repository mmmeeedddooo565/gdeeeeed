package com.mesm.clinic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicDao {
    @Query("SELECT * FROM cases ORDER BY updatedAt DESC")
    fun observeCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :id LIMIT 1")
    suspend fun getCase(id: Long): CaseEntity?

    @Query("SELECT * FROM case_images WHERE caseId = :caseId ORDER BY createdAt ASC")
    suspend fun getImages(caseId: Long): List<CaseImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(entity: CaseEntity): Long

    @Update
    suspend fun updateCase(entity: CaseEntity)

    @Delete
    suspend fun deleteCase(entity: CaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(entity: CaseImageEntity): Long

    @Delete
    suspend fun deleteImage(entity: CaseImageEntity)

    @Query("DELETE FROM case_images WHERE id = :imageId")
    suspend fun deleteImageById(imageId: Long)

    @Transaction
    suspend fun getCaseWithImages(id: Long): CaseWithImages? {
        val c = getCase(id) ?: return null
        return CaseWithImages(c, getImages(id))
    }
}

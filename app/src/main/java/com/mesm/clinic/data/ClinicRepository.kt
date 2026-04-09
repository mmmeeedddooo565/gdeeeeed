package com.mesm.clinic.data

import kotlinx.coroutines.flow.Flow

class ClinicRepository(private val dao: ClinicDao) {
    fun observeCases(): Flow<List<CaseEntity>> = dao.observeCases()
    suspend fun getCaseWithImages(id: Long): CaseWithImages? = dao.getCaseWithImages(id)
    suspend fun saveCase(entity: CaseEntity): Long = if (entity.id == 0L) dao.insertCase(entity) else { dao.updateCase(entity); entity.id }
    suspend fun deleteCase(entity: CaseEntity) = dao.deleteCase(entity)
    suspend fun addImage(entity: CaseImageEntity) = dao.insertImage(entity)
    suspend fun deleteImage(image: CaseImageEntity) = dao.deleteImage(image)
}

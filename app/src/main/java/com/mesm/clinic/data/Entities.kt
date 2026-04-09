package com.mesm.clinic.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String,
    val patientName: String,
    val age: String,
    val diagnosis: String,
    val noteHint: String = "",
    val visitDate: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "case_images",
    foreignKeys = [ForeignKey(entity = CaseEntity::class, parentColumns = ["id"], childColumns = ["caseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("caseId")]
)
data class CaseImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val uri: String,
    val kind: String, // case | rx
    val createdAt: Long = System.currentTimeMillis(),
)

data class CaseWithImages(
    val case: CaseEntity,
    val images: List<CaseImageEntity>,
)

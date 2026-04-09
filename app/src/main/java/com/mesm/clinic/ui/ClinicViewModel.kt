package com.mesm.clinic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesm.clinic.data.CaseEntity
import com.mesm.clinic.data.CaseImageEntity
import com.mesm.clinic.data.CaseWithImages
import com.mesm.clinic.data.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

data class CaseForm(
    val id: Long = 0,
    val caseNumber: String = "",
    val patientName: String = "",
    val age: String = "",
    val diagnosis: String = "",
    val noteHint: String = "",
    val visitDate: String = "",
    val notes: String = "",
    val caseImages: List<CaseImageEntity> = emptyList(),
    val rxImages: List<CaseImageEntity> = emptyList(),
)

class ClinicViewModel(private val repo: ClinicRepository) : ViewModel() {
    val cases = repo.observeCases().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _form = MutableStateFlow(CaseForm())
    val form: StateFlow<CaseForm> = _form.asStateFlow()
    private val _viewCase = MutableStateFlow<CaseWithImages?>(null)
    val viewCase = _viewCase.asStateFlow()

    fun newCase() { _form.value = CaseForm(); _viewCase.value = null }
    fun update(transform: (CaseForm) -> CaseForm) { _form.value = transform(_form.value) }

    fun loadCase(id: Long) = viewModelScope.launch {
        val result = repo.getCaseWithImages(id) ?: return@launch
        _viewCase.value = result
        _form.value = CaseForm(
            id = result.case.id,
            caseNumber = result.case.caseNumber,
            patientName = result.case.patientName,
            age = result.case.age,
            diagnosis = result.case.diagnosis,
            noteHint = result.case.noteHint,
            visitDate = result.case.visitDate,
            notes = result.case.notes,
            caseImages = result.images.filter { it.kind == "case" },
            rxImages = result.images.filter { it.kind == "rx" },
        )
    }

    fun save(onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        val f = _form.value
        val id = repo.saveCase(CaseEntity(id = f.id, caseNumber = f.caseNumber, patientName = f.patientName, age = f.age, diagnosis = f.diagnosis, noteHint = f.noteHint, visitDate = f.visitDate, notes = f.notes, updatedAt = System.currentTimeMillis()))
        onDone(id)
        loadCase(id)
    }

    fun addImage(uri: String, kind: String) = viewModelScope.launch {
        val id = _form.value.id
        if (id == 0L) return@launch
        repo.addImage(CaseImageEntity(caseId = id, uri = uri, kind = kind))
        loadCase(id)
    }

    fun deleteImage(img: CaseImageEntity) = viewModelScope.launch {
        repo.deleteImage(img)
        loadCase(img.caseId)
    }

    fun deleteCurrent(onDone: () -> Unit) = viewModelScope.launch {
        val id = _form.value.id
        val current = repo.getCaseWithImages(id)?.case ?: return@launch
        repo.deleteCase(current)
        newCase()
        onDone()
    }

    class Factory(private val repo: ClinicRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ClinicViewModel(repo) as T
    }
}

package com.mesm.clinic.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("auth_store")

class AuthStore(private val context: Context) {
    private val usernameKey = stringPreferencesKey("username")
    private val passwordKey = stringPreferencesKey("password")
    private val changedKey = booleanPreferencesKey("changed")
    private val biometricKey = booleanPreferencesKey("biometric")

    fun username(): String = runBlocking { context.dataStore.data.first()[usernameKey] ?: "admin" }
    fun password(): String = runBlocking { context.dataStore.data.first()[passwordKey] ?: "1234" }
    fun changed(): Boolean = runBlocking { context.dataStore.data.first()[changedKey] ?: false }
    fun biometricEnabled(): Boolean = runBlocking { context.dataStore.data.first()[biometricKey] ?: false }

    suspend fun saveCredentials(username: String, password: String) {
        context.dataStore.edit {
            it[usernameKey] = username
            it[passwordKey] = password
            it[changedKey] = true
            it[biometricKey] = true
        }
    }
}

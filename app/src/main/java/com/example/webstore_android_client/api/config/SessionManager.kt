package com.example.webstore_android_client.api.config

import android.content.Context
import androidx.datastore.core.DataStore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings"

val Context.dataStore: DataStore<Preferences>
by preferencesDataStore(name = DATASTORE_NAME)

class SessionManager(
    private val context: Context
) {

    companion object {
        private val SESSION_ID = stringPreferencesKey("session_id")
    }

    fun getSessionId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[SESSION_ID]
        }
    }

    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[SESSION_ID] = sessionId
        }
    }

    suspend fun deleteSessionId() {
        context.dataStore.edit { preferences ->
            preferences.remove(SESSION_ID)
        }
    }
}

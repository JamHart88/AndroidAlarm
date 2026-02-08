package com.example.androidalarm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        val ALARM_HOUR_KEY = intPreferencesKey("alarm_hour")
        val ALARM_MINUTE_KEY = intPreferencesKey("alarm_minute")
        val ALARM_SOUND_KEY = stringPreferencesKey("alarm_sound")
    }

    val alarmHour: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[ALARM_HOUR_KEY] ?: 7
        }

    val alarmMinute: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[ALARM_MINUTE_KEY] ?: 0
        }

    val alarmSound: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[ALARM_SOUND_KEY] ?: "alarm_digital"
        }

    suspend fun saveAlarmTime(hour: Int, minute: Int) {
        appContext.dataStore.edit { settings ->
            settings[ALARM_HOUR_KEY] = hour
            settings[ALARM_MINUTE_KEY] = minute
        }
    }

    suspend fun saveAlarmSound(sound: String) {
        appContext.dataStore.edit { settings ->
            settings[ALARM_SOUND_KEY] = sound
        }
    }
}

package com.example.clicker35

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("game_prefs")

class GameStorage(private val context: Context) {
    private val SCORE_KEY = intPreferencesKey("score")
    private val AUTOCLICK_KEY = intPreferencesKey("autoclick")

    suspend fun saveScore(score: Int){
        context.dataStore.edit { prfs ->
            prfs[SCORE_KEY] = score
        }
    }

    suspend fun getScore() : Int{
        return context.dataStore.data.map { prfs->
            prfs[SCORE_KEY] ?: 0
        }.first()
    }


    suspend fun saveAutoclick(autoclicks: Int){
        context.dataStore.edit { prfs ->
            prfs[AUTOCLICK_KEY] = autoclicks
        }
    }

    suspend fun getAutoclick() : Int{
        return context.dataStore.data.map { prfs->
            prfs[AUTOCLICK_KEY] ?: 0
        }.first()
    }
}
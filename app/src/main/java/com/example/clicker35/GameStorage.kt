package com.example.clicker35

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.math.BigDecimal

private val Context.dataStore by preferencesDataStore("game_prefs")

class GameStorage(private val context: Context) {
    private val SCORE_KEY = stringPreferencesKey("score")
    private val UPGRADES_KEY = stringPreferencesKey("upgrades")
    private val EXIT_TIME_KEY = stringPreferencesKey("exit_time")

    suspend fun saveScore(score: BigDecimal){
        context.dataStore.edit { prfs ->
            prfs[SCORE_KEY] = score.toString()
        }
    }

    suspend fun getScore() : BigDecimal{
        return context.dataStore.data.map { prfs->
            prfs[SCORE_KEY]?.toBigDecimalOrNull() ?: BigDecimal(0)
        }.first()
    }

    private val upgradeModule = SerializersModule {
        polymorphic(Upgrade::class){
            subclass(ClickMultiplierUpgrade::class, ClickMultiplierUpgrade.serializer())
            subclass(AutoClickUpgrade::class, AutoClickUpgrade.serializer())
            subclass(OfflineEarningsUpgrade::class, OfflineEarningsUpgrade.serializer())
        }
        contextual(BigDecimal::class, BigDecimalSerializer)
    }

    private val json = Json {
        serializersModule = upgradeModule
        classDiscriminator = "type"
    }

    suspend fun saveUpgrades(upgrades: List<Upgrade>){
        val data = json.encodeToString(upgrades)
        context.dataStore.edit { prfs ->
            prfs[UPGRADES_KEY] = data
        }
    }

    suspend fun getUpgrades() : List<Upgrade> {
        return context.dataStore.data.map<Preferences, List<Upgrade>> { prfs->
            val data = prfs[UPGRADES_KEY] ?: "[]"
            json.decodeFromString(data)
        }.first()
    }

    suspend fun saveExitTime(){
        val time = System.currentTimeMillis()
        context.dataStore.edit { prfs ->
            prfs[EXIT_TIME_KEY] = time.toString()
        }
    }

    suspend fun getExitTime() : Long{
        return context.dataStore.data.map { prfs->
            prfs[EXIT_TIME_KEY]?.toLongOrNull() ?: 0L
        }.first()
    }
}
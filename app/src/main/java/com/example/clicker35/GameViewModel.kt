package com.example.clicker35

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.math.BigDecimal
import kotlin.math.pow

class GameViewModel(app: Application): AndroidViewModel(app) {
    private val storage : GameStorage = GameStorage(app)

    init {
        viewModelScope.launch {
            count = storage.getScore()
        }
        viewModelScope.launch {
            val data = storage.getUpgrades()
            if(data.any()){
                upgrades.addAll(data)
            }
            else{
                upgrades.add(ClickMultiplierUpgrade(
                    0,
                    100,
                    1.5,
                    "Сила безумия",
                    1.0)
                )
                upgrades.add(AutoClickUpgrade(
                    0,
                    100,
                    1.7,
                    "Последователи культа",
                    0)
                )
                upgrades.add(OfflineEarningsUpgrade(
                    0,
                    100,
                    1.2,
                    "Храм Древних",
                    0)
                )
            }

            upgrades.map {
                when(it){
                    is AutoClickUpgrade -> clicksPerSecond = it.clicksPerSecond
                    is ClickMultiplierUpgrade -> multiplier = it.multiplier
                    is OfflineEarningsUpgrade -> offlineCap = it.offlineCap
                }
            }

            val currentTime = System.currentTimeMillis()
            val exitTime = storage.getExitTime()

            val deltaSec = (currentTime - exitTime)/1000

            if (offlineCap > 0){
                count += ((deltaSec * clicksPerSecond) % offlineCap).toInt()
            }
        }
    }

    var count by mutableStateOf(0)

    var clicksPerSecond by mutableStateOf(0)

    var multiplier by mutableStateOf(0.0)

    var offlineCap by mutableStateOf(0)

    val upgrades = mutableStateListOf<Upgrade>()

    fun buyUpgrade(upgrade: Upgrade){
        upgrade.upgrade()

        when(upgrade){
            is AutoClickUpgrade -> clicksPerSecond = upgrade.clicksPerSecond
            is ClickMultiplierUpgrade -> multiplier = upgrade.multiplier
            is OfflineEarningsUpgrade -> offlineCap = upgrade.offlineCap
        }
    }

    fun saveData(){
        viewModelScope.launch {
            storage.saveScore(count)
            storage.saveUpgrades(upgrades)
            storage.saveExitTime()
        }
    }
}


@Serializable
@Polymorphic
sealed class Upgrade{
    abstract var level: Int
    abstract var cost: Int
    abstract val growthFactor: Double
    abstract val title: String
    abstract val description: String

    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    open fun calculateNextCost(): Int{
        return (cost * (growthFactor.pow(level)).toInt())
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var multiplier : Double
):Upgrade() {
    override val description: String
        get() = "Множитель кликов ур.$level - x%.2f".format(multiplier)


    override fun upgrade(){
        super.upgrade()
        multiplier *= 1.2
    }
}
@Serializable
@SerialName("AutoClickUpgrade")
class AutoClickUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var clicksPerSecond : Int
):Upgrade() {
    override val description: String
        get() = "Автоклик ур.$level - $clicksPerSecond к/с"


    override fun upgrade(){
        super.upgrade()
        clicksPerSecond = (clicksPerSecond * 1.05).toInt() + 1
    }
}
@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var offlineCap : Int
):Upgrade() {
    override val description: String
        get() = "Лимит оффлайн дохода ур.$level - $offlineCap"


    override fun upgrade(){
        super.upgrade()
        offlineCap = (offlineCap * 1.2).toInt() + 10
    }
}
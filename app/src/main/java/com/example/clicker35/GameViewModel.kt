package com.example.clicker35

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
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
                    BigDecimal(100),
                    1.5,
                    "Сила безумия",
                    BigDecimal(1.0))
                    {
                        u->"Множитель кликов ур.${u.level} - x%.2f".format(u.multiplier)
                    }
                )
                upgrades.add(AutoClickUpgrade(
                    0,
                    BigDecimal(100),
                    1.7,
                    "Последователи культа",
                    BigDecimal(0))
                    {
                        u->"Автоклики ур.${u.level} - x%.2f".format(u.clicksPerSecond)
                    }
                )

                upgrades.add(OfflineEarningsUpgrade(
                    0,
                    BigDecimal(100),
                    1.2,
                    "Храм Древних",
                    BigDecimal(0))
                    {u->"Лимит оффлайн дохода ур.${u.level} - ${u.offlineCap}"}
                )
            }

            upgrades.map {
                when(it){
                    is AutoClickUpgrade -> clicksPerSecond = it.clicksPerSecond
                    is ClickMultiplierUpgrade -> multiplier = it.multiplier
                    is OfflineEarningsUpgrade -> offlineCap = it.offlineCap
                }
            }


        }
    }

    var count by mutableStateOf(BigDecimal(0))

    var clicksPerSecond by mutableStateOf(BigDecimal(0))

    var multiplier by mutableStateOf(BigDecimal(0))

    var offlineCap by mutableStateOf(BigDecimal(0))

    val upgrades = mutableStateListOf<Upgrade>()

    fun buyUpgrade(upgrade: Upgrade){
        upgrade.upgrade()

        when(upgrade){
            is AutoClickUpgrade -> clicksPerSecond = upgrade.clicksPerSecond
            is ClickMultiplierUpgrade -> multiplier = upgrade.multiplier
            is OfflineEarningsUpgrade -> offlineCap = upgrade.offlineCap
        }
    }

    suspend fun calculateOfflineEarnings(): Deferred<BigDecimal>{
        return viewModelScope.async {
            val currentTime = System.currentTimeMillis()
            val exitTime = storage.getExitTime()

            val deltaSec = (currentTime - exitTime)/1000
            var earnings = BigDecimal(0)
            if (offlineCap > BigDecimal(0)){
                earnings = (BigDecimal(deltaSec) * clicksPerSecond) % offlineCap
                count += earnings
            }
            earnings
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
    abstract var cost: BigDecimal
    abstract val growthFactor: Double
    abstract val title: String
    abstract val description: String

    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    open fun calculateNextCost(): BigDecimal{
        return cost * BigDecimal(growthFactor.pow(level))
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level: Int,
    @Contextual
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Contextual
    var multiplier: BigDecimal,
    @SerialName("-")
    val descriptionString: (ClickMultiplierUpgrade) -> String
):Upgrade() {
    override val description: String
        get() = descriptionString(this)


    override fun upgrade(){
        super.upgrade()
        multiplier *= BigDecimal(1.2)
    }
}
@Serializable
@SerialName("AutoClickUpgrade")
class AutoClickUpgrade(
    override var level: Int,
    @Contextual
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Contextual
    var clicksPerSecond : BigDecimal,
    val descriptionString: (AutoClickUpgrade) -> String
):Upgrade() {
    override val description: String
        get() = descriptionString(this)


    override fun upgrade(){
        super.upgrade()
        clicksPerSecond = clicksPerSecond * BigDecimal(1.05) + BigDecimal(1)
    }
}
@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level: Int,
    @Contextual
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Contextual
    var offlineCap : BigDecimal,
    val descriptionString: (OfflineEarningsUpgrade) -> String
):Upgrade() {
    override val description: String
        get() = descriptionString(this)


    override fun upgrade(){
        super.upgrade()
        offlineCap = offlineCap * BigDecimal(1.2) + BigDecimal(10)
    }
}
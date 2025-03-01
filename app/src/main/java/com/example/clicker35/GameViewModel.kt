package com.example.clicker35

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GameViewModel(app: Application): AndroidViewModel(app) {
    private val storage : GameStorage = GameStorage(app)

    init {
        viewModelScope.launch {
            count = storage.getScore()
            clicksPerSecond = storage.getAutoclick()
        }
    }


    var count by mutableStateOf(0)

    var clicksPerSecond by mutableStateOf(0)



    fun saveData(){
        viewModelScope.launch {
            storage.saveScore(count)
            storage.saveAutoclick(clicksPerSecond)
        }
    }
}
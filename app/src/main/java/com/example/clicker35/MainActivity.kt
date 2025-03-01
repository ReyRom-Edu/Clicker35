package com.example.clicker35

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.clicker35.ui.theme.Clicker35Theme
import kotlinx.coroutines.coroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClickerGame()
        }
    }
}

@Composable
fun ClickerGame(vm: GameViewModel = viewModel()) {
    Clicker35Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
            {
                Text("Натапано: ${vm.count}",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                val particles = remember { mutableStateListOf<Particle>() }

                var position by remember { mutableStateOf(Offset.Zero) }
                var buttonPosition by remember { mutableStateOf(Offset.Zero) }
                var buttonSize by remember { mutableStateOf(IntSize.Zero) }
                var isPressed by remember { mutableStateOf(false) }
                val tapScaling by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = tween(10)
                )

                LaunchedEffect(Unit) {
                    while (true){
                        delay(1000L)
                        vm.count += vm.clicksPerSecond
                        if (vm.clicksPerSecond > 0){
                            val particle = getRandomParticleInCircle(
                                buttonPosition.x + buttonSize.width/2,
                                buttonPosition.y + buttonSize.height/2,
                                buttonSize.height/2f)
                            particles.add(particle)
                        }
                    }
                }

                Box(modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center)
                    .onGloballyPositioned {
                        buttonPosition = Offset(it.positionInParent().x,it.positionInParent().y)
                        buttonSize = it.size
                    }
                    .pointerInput(Unit){
                        coroutineScope {
                            while (true){
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    position = down.position
                                    vm.count++
                                    isPressed = true
                                    repeat(5){
                                        particles.add(Particle(
                                            buttonPosition.x + position.x,
                                            buttonPosition.y + position.y,
                                        ))
                                    }
                                    down.consume()

                                    val up = waitForUpOrCancellation()
                                    if (up != null){
                                        isPressed = false
                                    }
                                }
                            }
                        }
                    }
                ){
                    Image(
                        painter = painterResource(id = R.drawable.cthulhu_star),
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.cthulhu),
                        modifier = Modifier.fillMaxSize(0.7f)
                            .align(Alignment.Center)
                            .graphicsLayer(scaleX = tapScaling, scaleY = tapScaling),
                        contentDescription = "Cthulhu",
                        contentScale = ContentScale.Crop
                    )
                }
                ParticleAnimation(particles)
            }
            BottomSheet(vm)
        }
    }
    GameLifetimeObserver{ vm.saveData() }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(vm: GameViewModel) {
    var isSheetOpen by remember { mutableStateOf(false) }

    if (isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            windowInsets = WindowInsets(0.dp)
        ) {
            Column {
                UpgradesView(vm)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        Button(
            onClick = {isSheetOpen=true}
        ) {
            Text("Меню")
        }
    }

}

@Composable
fun GameLifetimeObserver(onExit: ()->Unit){
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver{
            override fun onStop(owner: LifecycleOwner) {
                onExit()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                onExit()
            }
            override fun onPause(owner: LifecycleOwner) {
                onExit()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


@Composable
fun UpgradesView(vm: GameViewModel) {
    Column {
        Text("Улучшения")
        Button(onClick = {vm.clicksPerSecond++}) {
            Text("Автоклик: ${vm.clicksPerSecond} кликов в секунду")
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    ClickerGame()
}
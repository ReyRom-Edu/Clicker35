package com.example.clicker35

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clicker35.ui.theme.Clicker35Theme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
fun ClickerGame() {
    var count by remember { mutableStateOf(0) }


    Clicker35Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
            {
                Text("Натапано: $count",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                val particles = remember { mutableStateListOf<Particle>() }

                var position by remember { mutableStateOf(Offset.Zero) }
                var buttonPosition by remember { mutableStateOf(Offset.Zero) }
                var scaled by remember { mutableStateOf(false) }
                val tapScaling by animateFloatAsState(
                    targetValue = if (scaled) 1.1f else 1f,
                    animationSpec = keyframes {
                        durationMillis = 500
                        1f at 0
                        1.2f at 250
                        1f at 500
                    }
                )
                Box(modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(shadowElevation = tapScaling)
                    .background(Color.Blue)
                    .align(Alignment.Center)
                    .onGloballyPositioned {
                        buttonPosition = Offset(it.positionInParent().x,it.positionInParent().y)
                    }
                    .pointerInput(Unit){
                        coroutineScope {
                            while (true){
                                awaitPointerEventScope {
                                    position = awaitFirstDown().position
                                    count++
                                    //scaled = !scaled
                                    repeat(10){
                                        particles.add(Particle(buttonPosition.x + position.x, buttonPosition.y + position.y))
                                    }
                                }
                            }
                        }
                    }
                )
                ParticleAnimation(particles)
            }
        }
    }
}

@Composable
fun ParticleAnimation(particles : MutableList<Particle>){
    var invalidation by remember{mutableStateOf(false)}
    LaunchedEffect(Unit) {
        while (true){
            delay(16L)
            particles.removeAll { !it.update() }
            invalidation = !invalidation
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        invalidation.let {
            for (part in particles){
                drawIntoCanvas {
                    drawCircle(
                        Color.Red.copy(alpha = part.alpha),
                        radius = 8f,
                        center = Offset(part.x,part.y))
                }
            }
        }
    }

}


data class Particle(var x: Float, var y:Float,
                    var alpha: Float = 1f,
                    var rotation: Float = Random.nextFloat() * 360) {
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 5 + 2
    private val speedX = cos(angle) * speed
    private val speedY = sin(angle) * speed

    private var lifetime = 1f

    fun update():Boolean{
        x += speedX
        y += speedY
        alpha -= 0.05f
        rotation += 5
        lifetime -= 0.05f
        return lifetime > 0
    }
}


@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    ClickerGame()
}
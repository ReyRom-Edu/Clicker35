package com.example.clicker35

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


/**
 * Элемент отрисовки анимации частиц
 * @param particles список отрисовываемых частиц
 */
@Composable
fun ParticleAnimation(particles : MutableList<Particle>){
    var invalidation by remember{ mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true){
            delay(16L)
            particles.removeAll { !it.update() }
            invalidation = !invalidation
        }
    }

    val context = LocalContext.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        invalidation.let {
            for (part in particles){
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(part.alpha, 0.38f,0.96f,0.86f)
                        textSize = 80f
                        typeface = ResourcesCompat.getFont(context, R.font.daedra)
                        setShadowLayer(10f,5f,5f,
                            android.graphics.Color.argb(part.alpha, 0.0f,0.0f,0.0f))
                    }

                    canvas.nativeCanvas.drawText(
                        part.letter,
                        part.x, part.y,
                        paint
                    )
                }
            }
        }
    }

}

/**
 * Частица
 *
 * @property x координата x
 * @property y координата y
 * @property alpha прозрачность частицы - по умолчанию `1`
 * @property rotation угол вращения
 * @property letter символ частицы - по умолчанию случайный от `A` до `Z`
 */
data class Particle(
    var x: Float, var y: Float,
    var alpha: Float = 1f,
    var rotation: Float = Random.nextFloat() * 360,
    val angle : Float = Random.nextFloat() * 2 * PI.toFloat(),
    val letter: String = ('A'..'Z').random().toString()
) {
    private val speed = Random.nextFloat() * 5 + 2
    private val speedX = cos(angle) * speed
    private val speedY = sin(angle) * speed

    private var lifetime = 1f

    /**
     * Обновление состояния частицы
     * @return `false` если время жизни частицы истекло
     */
    fun update():Boolean{
        x += speedX
        y += speedY
        alpha -= 0.02f
        rotation += 5
        lifetime -= 0.02f
        return lifetime > 0
    }
}

fun getRandomParticleInCircle(centerX:Float, centerY: Float, radius: Float) : Particle{
    val theta = Random.nextFloat() * 2 * PI.toFloat()
    val r = sqrt(Random.nextFloat()) * radius

    val x = centerX + r * cos(theta)
    val y = centerY + r * sin(theta)

    return Particle(x, y, angle = theta)
}
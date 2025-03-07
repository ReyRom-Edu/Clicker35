package com.example.clicker35

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
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
import java.math.BigDecimal

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
    Clicker35Theme(darkTheme = vm.isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .height(100.dp)
                ) {
                    Text(
                        stringResource(R.string.score),
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(vm.count.formatNumber(),
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }


                val particles = remember { mutableStateListOf<Particle>() }

                var position by remember { mutableStateOf(Offset.Zero) }
                var buttonPosition by remember { mutableStateOf(Offset.Zero) }
                var buttonSize by remember { mutableStateOf(IntSize.Zero) }
                var isPressed by remember { mutableStateOf(false) }
                val tapScaling by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = tween(10)
                )
                var showDialog by remember { mutableStateOf(false) }
                var offlineEarnings by remember { mutableStateOf(BigDecimal(0)) }

                LaunchedEffect(Unit) {
                    offlineEarnings = vm.calculateOfflineEarnings().await()
                    if (offlineEarnings > BigDecimal(0)){
                        showDialog = true
                    }
                }

                if(showDialog){
                    AlertDialog(
                        onDismissRequest = { showDialog=false },
                        title = { Text("C возвращением!") },
                        text = { Text("Последователи заработали ${offlineEarnings.formatNumber()} безумия пока отсутствовали") },
                        confirmButton = {
                            Button(onClick = {showDialog = false}) {
                                Text("Ok")
                            }
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    while (true){
                        delay(1000L)
                        vm.count += vm.clicksPerSecond
                        if (vm.clicksPerSecond > BigDecimal(0)){
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
                        buttonPosition = Offset(it.positionInParent().x, it.positionInParent().y)
                        buttonSize = it.size
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            while (true) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    position = down.position
                                    vm.count += vm.multiplier
                                    isPressed = true
                                    repeat(5) {
                                        particles.add(
                                            Particle(
                                                buttonPosition.x + position.x,
                                                buttonPosition.y + position.y,
                                            )
                                        )
                                    }
                                    down.consume()

                                    val up = waitForUpOrCancellation()
                                    if (up != null) {
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
                        modifier = Modifier
                            .fillMaxSize(0.7f)
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

    val tabs = listOf("Улучшения", "Магазин", "Настройки")
    var selectedTabIndex by remember { mutableStateOf(0) }

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
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {selectedTabIndex = index}
                        ) {
                            Text(title)
                        }
                    }
                }
                when(selectedTabIndex){
                    0 -> UpgradesView(vm)
                    1 -> ShopView()
                    2 -> SettingsView(vm)
                }
                
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        Button(
            onClick = {isSheetOpen=true},
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text("Меню", fontSize = 28.sp)
        }
    }

}

@Composable
fun SettingsView(viewModel: GameViewModel) {
    var volume by remember { mutableStateOf(0f) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp) ) {
            Text("Громкость")
            Slider(value = volume,
                onValueChange = {volume = it},
                steps = 3,
                modifier = Modifier.fillMaxWidth().padding(20.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically ,
            modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Темная тема")
            Spacer(Modifier.width(10.dp))
            Switch(checked = viewModel.isDarkTheme,
                onCheckedChange = {viewModel.isDarkTheme = !viewModel.isDarkTheme })
        }
    }
}

@Composable
fun ShopView() {
    
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
    var invalidate by remember { mutableStateOf(false) }
    Column {
        //Text("Улучшения", fontSize = 25.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
        invalidate.let {
            vm.upgrades.forEach{ u ->
                UpgradeButton(u.title, u.description, u.cost.formatNumber()) {
                    vm.buyUpgrade(u)
                    invalidate = !invalidate
                }
            }
        }
    }
}

@Composable
fun UpgradeButton(title:String, description: String, cost:String, onClick: ()-> Unit){
    Button(onClick = onClick, shape = RectangleShape, modifier = Modifier
        .fillMaxWidth()
        .padding(3.dp)) {
        Box {
            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)
            ) {
                Text(title)
                Text(description)
            }
            Text(cost, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    ClickerGame()
 //Image(bitmap = ImageBitmap.imageResource(R.drawable.cthulhu_back), null)
}
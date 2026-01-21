package np.ict.mad.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import np.ict.mad.basic.ui.theme.BasicTheme
import kotlin.random.Random
import kotlin.random.nextLong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicTheme {
                WackAMoleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) {

    var currentScore by remember { mutableStateOf(0) } // current score of user as the game is running
    var timeLeft by remember { mutableStateOf(30) } // time left of the game
    var moleIndex by remember { mutableIntStateOf(4) } // index of the hole that shows the mole currently

    var isRunning by rememberSaveable { mutableStateOf(false) } // to check if the game is still running
    var isGameOver by rememberSaveable { mutableStateOf(false) } // to check if the game is over or not

    // For Persistent Storage
    val context = LocalContext.current
    val prefs = remember {context.getSharedPreferences("wack-a-mole_prefs", android.content.Context.MODE_PRIVATE)} // saving the preference
    var highScore by rememberSaveable { mutableIntStateOf(0) } // highscore of user

    // Timer logic (countdown)
    LaunchedEffect(isRunning){
        if (!isRunning) {
            return@LaunchedEffect // if its not running, return
        }
        while (isRunning && timeLeft > 0){ // if game running and timer is not 0
            delay(1000)
            timeLeft -= 1 // decrement it (countdown) until 0
        }
        if (timeLeft == 0){ // if timer reach 0
            isRunning = false // game is not running anymore
            isGameOver = true // game ends as no more timer
            if (currentScore > highScore){
                highScore = currentScore
                prefs.edit().putInt("HIGH_SCORE", highScore).apply()
            }
        }
    }

    // for mole movement
    LaunchedEffect(isRunning) {
        if (!isRunning){
            return@LaunchedEffect // return if game is not running
        }
        while (isRunning && timeLeft > 0){ // while game is running and time is remaining
            delay(Random.nextLong(700, 1001)) // movement speed for mole
            moleIndex = Random.nextInt(0,9) // select another number for the mole to be in next
        }
    }

    // for highscore, reads highscore before game starts, displays 0 if none
    LaunchedEffect(Unit) {
        highScore = prefs.getInt("HIGH_SCORE", 0)
    }

    Scaffold(
        // TopAppBar contains the game name Wack-a-Mole and the settings icon to lead to another page
        topBar = {
            TopAppBar(
                title = { Text("Wack-a-Mole") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("settings") // leads to settings page
                    }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Column will fill the entire screen containing the game
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score + Timer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: $currentScore", style = MaterialTheme.typography.titleMedium)
                Text("Timer: $timeLeft", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("High Score: $highScore", style = MaterialTheme.typography.bodyMedium) // display highscore at the top for users to see

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Restart button
            Button(onClick = {
                currentScore = 0
                timeLeft = 30
                moleIndex = Random.nextInt(0,9) // stores which hole the mole is in
                isRunning = true
                isGameOver = false
            }) {
                Text(if (isRunning) "Restart" else "Start")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // the 3x3 grid for the mole layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items((0..8).toList()) { index ->
                    HoleButton(
                        isMole = (index == moleIndex),
                        onClick = {
                            // Once user clicks mole, increment score
                            if (isRunning && index == moleIndex) {
                                currentScore += 1

                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // if game ends, display the final score user got in this round
            if (isGameOver){
                Spacer(Modifier.height(12.dp))
                Text("Game Over! Final Score: $currentScore")

            }

        }
    }
}
// The settings page (with a back arrow to the game page)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Settings screen")
        }
    }
}

@Composable
fun HoleButton(
    isMole: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f) // makes it square within the grid cell
            .fillMaxWidth()
    ) {
        if (isMole) {
            Image(
                painter = painterResource(id = R.drawable.mole), //image for mole
                contentDescription = "Mole",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
// for the navigation between the game page and the settings page (using nav controller)
@Composable
fun WackAMoleApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "game") {
        composable("game") { GameScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    BasicTheme {
        WackAMoleApp()
    }
}
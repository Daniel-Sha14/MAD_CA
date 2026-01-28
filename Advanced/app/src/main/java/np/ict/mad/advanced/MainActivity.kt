package np.ict.mad.advanced

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.ict.mad.advanced.data.AppDatabase
import np.ict.mad.advanced.data.LeaderboardRow
import np.ict.mad.advanced.data.ScoreEntity
import np.ict.mad.advanced.data.UserEntity
import np.ict.mad.advanced.ui.theme.AdvancedTheme
import kotlin.random.Random
import kotlin.random.nextLong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvancedTheme {
                WackAMoleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, currentUserId: Long, currentUsername: String) {

    var currentScore by remember { mutableStateOf(0) } // current score of user as the game is running
    var timeLeft by remember { mutableStateOf(30) } // time left of the game
    var moleIndex by remember { mutableIntStateOf(4) } // index of the hole that shows the mole currently

    var isRunning by rememberSaveable { mutableStateOf(false) } // to check if the game is still running
    var isGameOver by rememberSaveable { mutableStateOf(false) } // to check if the game is over or not

    // For Room
    val context = LocalContext.current
    val db = remember(context){ AppDatabase.getInstance(context) }
    val scoreDao = remember(db){db.scoreDao()} // for user's best score (highscore)
    val scope = rememberCoroutineScope()

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
            // for Room database to insert score into room for this user
            scope.launch {
                scoreDao.insertScore(
                    ScoreEntity(
                        userId = currentUserId,
                        score = currentScore,
                        timestamp = System.currentTimeMillis()
                    )
                )
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

            Spacer(modifier = Modifier.height(16.dp))

            // to allow users to view their score and go to leaderboard
            Button(onClick = { navController.navigate("scores") }) {
                Text("View Scores")
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

// The login screen where users would be brought to login to play the game
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (Long, String) -> Unit
) {
    // For Room database access
    val context = LocalContext.current
    val db = remember(context){ AppDatabase.getInstance(context) } // retrieves the singleton db instance
    val userDao = remember(db){db.userDao()} // gets UserDao from the database
    val scope = rememberCoroutineScope()

    // input fields
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() } // to show login errors

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sign In", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // for username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // for password input (hidden)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // sign in button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val u = username.trim()
                    val p = password

                    // validation
                    if (u.isEmpty() || p.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter username and password.") }
                        return@Button
                    }

                    // authenticate using Room
                    scope.launch {
                        val user = userDao.findByUsername(u)
                        when {
                            user == null -> snackbarHostState.showSnackbar("User not found. Please sign up.")
                            user.password != p -> snackbarHostState.showSnackbar("Wrong password.")
                            else -> onLoginSuccess(user.userId, user.username)
                        }
                    }
                }
            ) {
                Text("Sign In")
            }

            Spacer(Modifier.height(12.dp))

            // to go to sign up screen
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("signup") }
            ) {
                Text("Go to Sign Up")
            }
        }
    }
}

// The signup screen where users would be brought to to create a account
@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember(context){ AppDatabase.getInstance(context) } // retrieves the singleton db instance
    val userDao = remember(db){db.userDao()} // gets UserDao from the database
    val scope = rememberCoroutineScope()

    // the input fields
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val snackbarHostState = remember{SnackbarHostState()} // to show login errors

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sign Up", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // to enter username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // to enter password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val u = username.trim()
                    val p = password

                    // validation
                    if (u.isEmpty() || p.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter username and password.") }
                        return@Button
                    }

                    scope.launch {
                        try {
                            userDao.insertUser(UserEntity(username = u, password = p))
                            snackbarHostState.showSnackbar("Account created! Please sign in.")
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            } // back to login
                        } catch (e: Exception) {

                            snackbarHostState.showSnackbar("Username already exists. Try another.")
                        }
                    }
                }
            ) {
                Text("Create Account")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                } }
            ) {
                Text("Back to Login")
            }
        }
    }
}

// The screen where users can see their personal best score with other users
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    navController: NavController,
    currentUserId: Long,
    currentUsername: String
) {
    // for Room
    val context = LocalContext.current
    val db = remember(context){ AppDatabase.getInstance(context) } // retrieves the singleton db instance
    val scoreDao = remember(db){db.scoreDao()} // gets ScoreDao from the database

    // personal best score for user and the leaderboard to see for other users
    var personalBest by rememberSaveable { mutableIntStateOf(0) }
    var leaderboard by remember{mutableStateOf<List<LeaderboardRow>>(emptyList())}

    LaunchedEffect(currentUserId) {
        personalBest = scoreDao.getPersonalBest(currentUserId) ?: 0
        leaderboard = scoreDao.getLeaderboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // a header card to show user and their personal best score
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "User: $currentUsername",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Personal Best: $personalBest",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Leaderboard (best per user)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            // the leaderboard list to view other users' scores
            LazyColumn {
                itemsIndexed(leaderboard) { index, row ->
                    val best = row.bestScore ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // the rank for the users
                        Text(
                            text = "#${index + 1}",
                            modifier = Modifier.width(44.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = row.username,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = best.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // divider between rows
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
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

    // to hold the currently logged-in user info, null means haven login
    var currentUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var currentUsername by rememberSaveable { mutableStateOf<String?>(null) }

    NavHost(navController = navController, startDestination = "login") {
        // user starts at login instead of the gamescreen
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {userId, username ->
                    currentUserId = userId
                    currentUsername = username
                    navController.navigate("game"){
                        popUpTo("login"){inclusive = true}
                    }
                })
        }
        // signup screen
        composable("signup") { SignupScreen(navController = navController) }
        // the main gamescreen that has the wack a mole game
        composable("game"){
            val uid = currentUserId
            val username = currentUsername

            // cant access game if user haven login yet
            if (uid == null || username == null){
                LaunchedEffect(Unit) {
                    navController.navigate("login"){
                        popUpTo("game"){inclusive = true}
                    }
                }
            }else{
                GameScreen(
                    navController = navController,
                    currentUserId = uid,
                    currentUsername = username
                )
            }
        }
        // for the scores screen which also requires logging in
        composable("scores"){
            val uid = currentUserId
            val username = currentUsername
            if (uid == null || username == null){
                LaunchedEffect(Unit) {navController.navigate("login") }
            }else{
                ScoresScreen(
                    navController = navController,
                    currentUserId = uid,
                    currentUsername = username
                )
            }
        }
        // settings screen
        composable("settings") { SettingsScreen(navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AdvancedTheme {
        WackAMoleApp()
    }
}
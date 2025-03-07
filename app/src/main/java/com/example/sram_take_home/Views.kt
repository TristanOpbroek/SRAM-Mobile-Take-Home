package com.example.sram_take_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AppScreen(viewModel: AppViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStats()
    }

    when (uiState) {
        is StravaUiState.Loading -> LoadingScreen()
        is StravaUiState.Success -> StatsScreen((uiState as StravaUiState.Success).stats)
        is StravaUiState.Error -> ErrorScreen((uiState as StravaUiState.Error).message)
    }
}

@Composable
fun StatsScreen(athleteStats: AthleteStats) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Top Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("${athleteStats.athlete.firstName} ${athleteStats.athlete.lastName}")
                    Text(athleteStats.athlete.city)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Bottom Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StatsGrid(athleteStats.allRideTotal)
        }
    }
}

@Composable
fun StatsGrid(rideStats: RideTotal) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatsCard("Total ride number\n${rideStats.count}")
            Spacer(modifier = Modifier.weight(1f))
            StatsCard("Total distance\n${rideStats.distance}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatsCard("Total ride time\n${rideStats.elapsedTime}")
            Spacer(modifier = Modifier.weight(1f))
            StatsCard("Total time moving\n ${rideStats.movingTime}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatsCard("Total elevation gain\n${rideStats.elevationGain}")
        }
    }
}

@Composable
fun StatsCard(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .width(150.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .padding(8.dp)
    )
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message")
    }
}

@Composable
fun AuthScreen(viewModel: AppViewModel, navController: NavController) {
    val authButtonClicked = remember { mutableStateOf(false) }

    if (authButtonClicked.value) {
        viewModel.authFlow()
        navController.navigate("home") {
            popUpTo("auth") { inclusive = true }
        }

        authButtonClicked.value = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { authButtonClicked.value = true }) {
            Text("Connect to Strava")
        }
        Text(
            text = "clicking this button will likely crash the app",
            fontSize = 9.sp
            )
    }
}
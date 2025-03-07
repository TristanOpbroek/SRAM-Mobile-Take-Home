package com.example.sram_take_home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initAuthRedirect(this)
        enableEdgeToEdge()
        setContent {
            SramTakeHome(viewModel)
        }
    }

    @Composable
    fun SramTakeHome(viewModel: AppViewModel) {
        val navController = rememberNavController()

        var isReady by remember { mutableStateOf(false) }
        var startDest by remember { mutableStateOf("auth") }


        LaunchedEffect(Unit) {
            val hasTokens = viewModel.hasTokens()
            startDest = if (hasTokens) "app" else "auth"
            isReady = true
        }

        if (isReady) {
            NavHost(navController, startDestination = startDest) {
                composable("auth") { AuthScreen(viewModel, navController) }
                composable("app") { AppScreen(viewModel, navController) }
            }
        } else {
            LoadingScreen()
        }
    }
}

object Constants {
    const val authorizeUrl = "https://www.strava.com/oauth/mobile/authorize"
    const val redirectUrl = "https://localhost"
    const val tokenUrl = "https://www.strava.com/oauth/token"
    const val clientID = "129607"

    const val getAthleteUrl = "https://www.strava.com/api/v3/athlete"
    const val getStatsUrl = "https://www.strava.com/api/v3/athletes/%d/stats"
}
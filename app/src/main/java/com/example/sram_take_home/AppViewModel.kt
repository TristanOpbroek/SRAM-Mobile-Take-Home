package com.example.sram_take_home

import android.content.Context
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private var authRedirect: ActivityResultLauncher<Intent>? = null
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private val _uiState = MutableStateFlow<StravaUiState>(StravaUiState.Loading)
    val uiState: StateFlow<StravaUiState> = _uiState


    init {
        checkLoadKeys()
    }

    fun initAuthRedirect(activity: ComponentActivity) {
        authRedirect = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                this.accessToken = intent?.getStringExtra("ACCESS_TOKEN")
                this.refreshToken = intent?.getStringExtra("REFRESH_TOKEN")
            } else {

            }
        }
    }

    fun fetchStats() {
        viewModelScope.launch {
            _uiState.value = StravaUiState.Loading
            try {
                val stats = getStats()
                _uiState.value = StravaUiState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = StravaUiState.Error(e.message ?: "Error!")
            }
        }
    }

    private suspend fun getStats(): AthleteStats {
        val athlete = getAthlete()
        val rideTotal = getAllRides(athlete.id)

        return AthleteStats(
            athlete = athlete, allRideTotal = rideTotal
        )
    }

    private suspend fun getAthlete(): Athlete {
        return withContext(Dispatchers.IO) {
            val httpClient = OkHttpClient()
            val request = Request.Builder().url(Constants.getAthleteUrl)
                .addHeader("Authorization", "Bearer $accessToken").get().build()

            val response: Response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to fetch Athlete")
            val json =
                JSONObject(response.body?.string() ?: throw Exception("Athlete response is empty"))

            Athlete(
                id = json.getInt("id"),
                firstName = json.getString("firstname"),
                lastName = json.getString("lastname"),
                city = json.getString("city")
            )
        }
    }

    private suspend fun getAllRides(id: Int): RideTotal {
        return withContext(Dispatchers.IO) {
            val url = Constants.getStatsUrl.format(id)
            val httpClient = OkHttpClient()
            val request =
                Request.Builder().url(url).addHeader("Authorization", "Bearer $accessToken").get()
                    .build()
            val response: Response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to fetch stats")
            val json =
                JSONObject(response.body?.string() ?: throw Exception("Stats response is empty"))
            val allRideJson = json.getJSONObject("all_ride_totals")
            RideTotal(
                count = allRideJson.getInt("count"),
                distance = allRideJson.getDouble("distance").toFloat(),
                movingTime = allRideJson.getInt("moving_time"),
                elapsedTime = allRideJson.getInt("elapsed_time"),
                elevationGain = allRideJson.getDouble("elevation_gain").toFloat()
            )
        }
    }

    private fun checkLoadKeys() {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("storage", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("ACCESS_TOKEN") && sharedPreferences.contains("REFRESH_TOKEN")) {
            accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)
            refreshToken = sharedPreferences.getString("REFRESH_TOKEN", null)
        }
    }

    fun hasTokens(): Boolean {
        return accessToken != null && refreshToken != null
    }

    fun authFlow() {
        val scope = "profile:read_all,activity:read_all"
        val authUrl = Constants.authorizeUrl.toUri().buildUpon()
            .appendQueryParameter("client_id", Constants.clientID)
            .appendQueryParameter("redirect_uri", Constants.redirectUrl)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto").appendQueryParameter("scope", scope)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, authUrl)
        authRedirect?.launch(intent)
    }
}

// Track UI state for view
sealed class StravaUiState {
    data object Loading : StravaUiState()
    data class Success(val stats: AthleteStats) : StravaUiState()
    data class Error(val message: String) : StravaUiState()
}

data class Athlete(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val city: String
    //val profilePicture: String //(??) implement if have enough time
)

data class RideTotal(
    val count: Int,
    val distance: Float,
    val movingTime: Int,
    val elapsedTime: Int,
    val elevationGain: Float
)

data class AthleteStats(
    val athlete: Athlete, val allRideTotal: RideTotal
)
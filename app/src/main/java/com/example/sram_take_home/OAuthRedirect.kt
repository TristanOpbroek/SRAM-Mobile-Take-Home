package com.example.sram_take_home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import androidx.core.content.edit


class OAuthRedirect : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            Yee()
        }
    }

    private fun handleIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                codeExchange(code, object : TokenCallback {
                    override fun onSuccess(accessToken: String, refreshToken: String) {
                        saveTokens(this@OAuthRedirect, accessToken, refreshToken)
                        val returnIntent = Intent()
                        returnIntent.putExtra("ACCESS_TOKEN", accessToken)
                        returnIntent.putExtra("REFRESH_TOKEN", refreshToken)

                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }

                    override fun onFailure(error: String) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                })
            }
        }
    }

    private fun codeExchange(code: String, callback: TokenCallback) {
        val httpClient = OkHttpClient()
        val body = FormBody.Builder()
            .add("client_id", Constants.clientID)
            .add("client_secret", BuildConfig.CLIENT_SECRET)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url(Constants.tokenUrl)
            .post(body)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Request failed!")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody =
                        response.body?.string() // ToString() rather than .string delayed me for longer than I'd like to admit
                    val json = JSONObject(responseBody!!)
                    val accessToken = json.getString("access_token")
                    val refreshToken = json.getString("refresh_token")
                    callback.onSuccess(accessToken, refreshToken)
                } else {
                    callback.onFailure("Response failed!")
                }
            }
        })
    }

    private fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val sharedPreferences = context.getSharedPreferences("storage", Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            putString("ACCESS_TOKEN", accessToken)
            putString("REFRESH_TOKEN", refreshToken)
        }
    }

    @Composable
    fun Yee() { }
}

interface TokenCallback {
    fun onSuccess(accessToken: String, refreshToken: String)
    fun onFailure(error: String)
}
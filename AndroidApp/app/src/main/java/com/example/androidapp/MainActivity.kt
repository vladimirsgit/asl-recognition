package com.example.androidapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.objects.RefreshToken
import com.example.androidapp.requests.ApiInterface
import com.example.androidapp.requests.Authentication
import com.example.androidapp.requests.RetrofitInstance
import com.example.androidapp.ui.screens.ChangePasswordScreen
import com.example.androidapp.ui.screens.FFACameraScreen
import com.example.androidapp.ui.screens.LearnCameraScreen
import com.example.androidapp.ui.screens.MenuScreen
import com.example.androidapp.ui.screens.SignInScreen
import com.example.androidapp.ui.screens.SignUpScreen
import com.example.androidapp.ui.screens.ValidationCodeScreen
import com.example.androidapp.ui.theme.AndroidAppTheme
import com.example.androidapp.ui.screens.WelcomeScreen
import com.example.androidapp.utils.Constants
import com.example.androidapp.utils.DataPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    var backCamera: Boolean = false
    var apiInterface: ApiInterface = RetrofitInstance.getInstance().create(ApiInterface::class.java)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var stateLoading by remember {mutableStateOf(true)}
            var startDest by remember {mutableStateOf(Constants.WELCOME_SCREEN)}
            val navController = rememberNavController()
            AndroidAppTheme {
                Scaffold( modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current
                    val accessToken = DataPersistence.loadAccessToken(context)
                    val refreshToken = DataPersistence.loadRefreshToken(context)
                    var hasInternetConn = false
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (connectivityManager != null) {
                        val capabilities =
                            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                        if (capabilities != null) {
                            if (
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(
                                    NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                                    NetworkCapabilities.TRANSPORT_ETHERNET)
                            ) {
                                hasInternetConn = true
                            }

                        }
                    }
                    if(!hasInternetConn) {
                        Log.d("InternetCheck", "Device has no internet connection")
                    } else {
                        Log.d("InternetCheck", "Device connected to the internet.")
                    }
                    if (hasInternetConn) {
                        LaunchedEffect(Unit) {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        Authentication(apiInterface).validateToken("Bearer $accessToken")
                                    }
                                    if (result.isSuccessful) {
                                        startDest = Constants.MENU_SCREEN
                                        stateLoading = false
                                        Log.d("TAG", "ACCESS TOKEN IS VALID")
                                    } else {
                                        Log.d("TAG", "ACCESS TOKEN $accessToken INVALID, TRYING TO REFRESH ACCESS")
                                        val refreshTokenRes = withContext(Dispatchers.IO) {
                                            Authentication(apiInterface).refreshAuth(RefreshToken(token=refreshToken))
                                        }
                                        if (refreshTokenRes.isSuccessful){
                                            startDest = Constants.MENU_SCREEN
                                            stateLoading = false
                                            Log.d("TAG", "REFRESH TOKEN IS VALID")
                                            val respBody = refreshTokenRes.body()
                                            val accessToken = respBody?.access_token?.token
                                            val refreshToken = respBody?.refresh_token?.token
                                            DataPersistence.saveAccessToken(context, accessToken.toString())
                                            DataPersistence.saveRefreshToken(context, refreshToken.toString())
                                        }
                                        else {
                                            stateLoading = false
                                            Log.d("TAG", "REFRESH TOKEN $refreshToken INVALID")
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                    } else {
                        stateLoading = false
                    }
                    if (!stateLoading) {
                        NavHost(
                            navController = navController,
                            startDestination = startDest,
                            modifier = Modifier
                        ) {
                            composable(Constants.WELCOME_SCREEN) {
                                WelcomeScreen(
                                    navController = navController,
                                    modifier = Modifier
                                )
                            }
                            composable(Constants.FFA_CAMERA_SCREEN) {
                                FFACameraScreen(navController = navController, backCamera = backCamera)
                            }
                            composable(Constants.SIGNUP_SCREEN){
                                SignUpScreen(navController = navController, apiInterface=apiInterface)
                            }
                            composable(Constants.VALIDATION_CODE_SCREEN) {
                                ValidationCodeScreen(navController=navController, apiInterface=apiInterface)
                            }
                            composable(Constants.SIGNIN_SCREEN) {
                                SignInScreen(navController = navController, apiInterface = apiInterface)
                            }
                            composable(Constants.MENU_SCREEN) {
                                MenuScreen(navController = navController, apiInterface = apiInterface)
                            }
                            composable (Constants.LEARN_CAMERA_SCREEN) {
                                LearnCameraScreen(navController)
                            }
                            composable (Constants.CHANGE_PASS_SCREEN) {
                                ChangePasswordScreen(navController = navController, apiInterface = apiInterface)
                            }

                        }
                    }
                }
            }

        }
    }
}


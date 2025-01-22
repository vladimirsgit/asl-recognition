package com.example.androidapp.ui.screens

import android.util.Log
import com.example.androidapp.requests.ApiInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidapp.R
import com.example.androidapp.objects.RefreshToken
import com.example.androidapp.requests.Authentication
import com.example.androidapp.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.androidapp.utils.DataPersistence


@Composable
fun MenuScreen(modifier: Modifier = Modifier, navController: NavController, apiInterface: ApiInterface) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    Column (
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main Menu",
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold)
        MainMenuButtons(apiInterface, navController)
    }
}


@Composable
fun MainMenuButtons(apiInterface: ApiInterface, navController: NavController) {
    val activContext = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        //modifier = Modifier.padding(bottom = 300.dp)
    ) {
        Button(
            onClick = {
                navController.navigate(Constants.LEARN_CAMERA_SCREEN)
            },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0049e6),
                contentColor =  colorResource(R.color.white),
            )
        ) {
            Text("\uD83D\uDCA1 Start learning")
        }
        Button(
            onClick = {
                val accessToken = DataPersistence.loadAccessToken(activContext)
                val refreshToken = DataPersistence.loadRefreshToken(activContext)
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            Authentication(apiInterface).logOut("Bearer $accessToken", RefreshToken(token=refreshToken))
                        }
                        if (result.isSuccessful) {
                            DataPersistence.deleteTokens(activContext)
                            navController.navigate(Constants.WELCOME_SCREEN) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                            Log.d("TAG", "ACCESS TOKEN IS VALID, LOGGING OUT")
                        } else {
                            Log.d("TAG", "ACCESS TOKEN $accessToken INVALID, TRYING TO REFRESH ACCESS")
                            val refreshTokenRes = withContext(Dispatchers.IO) {
                                Authentication(apiInterface).refreshAuth(RefreshToken(token=refreshToken))
                            }
                            if (refreshTokenRes.isSuccessful){
                                DataPersistence.deleteTokens(activContext)
                                navController.navigate(Constants.WELCOME_SCREEN) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                                Log.d("TAG", "REFRESHED ACCESS, LOGGING OUT")
                            }
                            else {
                                Log.d("TAG", "REFRESH TOKEN $refreshToken INVALID")
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            },
            modifier = Modifier.width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white),
                contentColor =  Color(0xFF0049e6),
            )
        ) {
            Text("↪\uFE0F Log Out")
        }
    }
}
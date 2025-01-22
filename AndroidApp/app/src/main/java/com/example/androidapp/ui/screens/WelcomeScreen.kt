package com.example.androidapp.ui.screens

import android.content.res.Configuration
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.R
import com.example.androidapp.ui.theme.AndroidAppTheme
import com.example.androidapp.utils.Constants

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignInSignUpButtons(navController=navController, onClick = {})
    }
}

@Composable
fun SignInSignUpButtons(navController: NavController, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { navController.navigate(Constants.SIGNIN_SCREEN) },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white),
                contentColor =  Color(0xFF0049e6),
            )
        ) {
            Text(text = "Sign In", color = Color(0xFF0049e6))

        }
        Button(
            onClick = { navController.navigate(Constants.SIGNUP_SCREEN) },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0049e6),
                contentColor =  colorResource(R.color.white),
            )
        ) {
            Text("Sign Up")
        }
        Button(
            onClick = {navController.navigate(Constants.FFA_CAMERA_SCREEN)},
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF01042d),
                contentColor = colorResource(R.color.white)
            )
        ) {
            Text(
                text = "Skip and use offline",
                fontWeight = FontWeight.ExtraBold,
                textDecoration = TextDecoration.Underline
            )
        }

    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    val navController = rememberNavController()
    AndroidAppTheme {
        WelcomeScreen(navController = navController)
    }
}
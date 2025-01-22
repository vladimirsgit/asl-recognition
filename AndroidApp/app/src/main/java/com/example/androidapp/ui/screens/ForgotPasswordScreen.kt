package com.example.androidapp.ui.screens

import com.example.androidapp.requests.ApiInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.androidapp.R
import com.example.androidapp.objects.ForgotPasswordRequest
import com.example.androidapp.requests.Authentication
import com.example.androidapp.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


@Composable
fun ChangePasswordScreen(modifier: Modifier = Modifier, navController: NavController, apiInterface: ApiInterface) {
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
        ChangePasswordFields(apiInterface=apiInterface, navController=navController)
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordFields(apiInterface: ApiInterface, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 300.dp)
    ) {
        var email by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        TextField(
            value = email,
            shape = RoundedCornerShape(12.dp),
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            colors = OutlinedTextFieldDefaults.colors (unfocusedContainerColor = Color.White, unfocusedTextColor = Color(0xFF01042d),
                focusedTextColor = Color.White)
        )
        var invalidData by remember {mutableStateOf(false)}
        var errorMessage by remember {mutableStateOf("")}
        var respOk by remember {mutableStateOf(false)}
        if (invalidData) {
            BasicAlertDialog(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                onDismissRequest = {
                    invalidData = false
                    errorMessage = "" },
            ) {
                Box(
                    Modifier
                        .width(250.dp)
                        .height(100.dp)
                        .background(Color(0xFFFF4545))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        errorMessage
                    )
                }
            }
        }
        if (respOk) {
            BasicAlertDialog(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                onDismissRequest = {
                    navController.navigate(Constants.WELCOME_SCREEN)
                },
            ) {
                Box(
                    Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .background(Color(0xFF00FFFF))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please check your email for further instructions.",
                        color = Color(0xFF0049e6),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            Authentication(apiInterface).forgotPassword(ForgotPasswordRequest(email=email))
                        }
                        if (result.isSuccessful) {
                            respOk = true
                        } else {
                            invalidData = true
                            val errBody = result.errorBody()?.string() ?: ""
                            val errJson = JSONObject(errBody)
                            errorMessage = errJson.get("detail").toString()
                        }
                    } catch (e: Exception) {
                    }
                }
            },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white),
                contentColor = Color(0xFF0049e6),
            )
        ) {
            Text("Send request")
        }
    }
}
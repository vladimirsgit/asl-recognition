package com.example.androidapp.ui.screens

import android.content.res.Configuration
import android.graphics.Paint
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.R
import com.example.androidapp.objects.SignUpRequest
import com.example.androidapp.requests.ApiInterface
import com.example.androidapp.requests.Authentication
import com.example.androidapp.ui.theme.AndroidAppTheme
import com.example.androidapp.utils.Constants
import com.example.androidapp.utils.DataPersistence
import com.example.androidapp.utils.validateSignUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Composable
fun ValidationCodeScreen(modifier: Modifier = Modifier, navController: NavController, apiInterface: ApiInterface){
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
        CodeFields(apiInterface=apiInterface, navController=navController)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeFields(navController: NavController, apiInterface: ApiInterface){
    var savedTime = DataPersistence.loadSignupTimestamp(LocalContext.current)
    var maxAllowedTime = savedTime + 300_000L
    var elapsedTime by remember {mutableLongStateOf(maxAllowedTime - System.currentTimeMillis())}
    var isRunning by remember {mutableStateOf(true)}

    Column (modifier = Modifier.fillMaxSize().padding(top = 50.dp).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(text=formatTime(elapsedTime),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.White)

        Row (
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 200.dp),
        ) {
            var invalidData by remember {mutableStateOf(false)}
            var errorMessage by remember {mutableStateOf("")}
            var codeIsGood by remember {mutableStateOf(false)}
            if (codeIsGood) {
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
                            "Great! You have verified your email account. Please proceed to logging in.",
                            color = Color(0xFF0049e6),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (invalidData) {
                BasicAlertDialog(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    onDismissRequest = {
                        invalidData = false
                        errorMessage = ""
                    },
                ) {
                    Box(
                        Modifier
                            .width(300.dp)
                            .height(150.dp)
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
            var codes = remember { mutableStateListOf("", "", "", "", "", "") }
            val keyBoardController = LocalSoftwareKeyboardController.current
            codes.forEachIndexed { index, code ->
                val focusManager = LocalFocusManager.current
                var imeAction = ImeAction.Next
                if (index == 5){
                    imeAction = ImeAction.Done
                }

                TextField(
                    modifier = Modifier.weight(1f).height(60.dp),
                    value = codes[index],
                    shape = RoundedCornerShape(12.dp),
                    onValueChange = { it->
                        if (it != codes[index]) {
                            codes[index] = it.takeLast(1)

                            if(it.isNotEmpty() && index < 5){
                                focusManager.moveFocus(FocusDirection.Next)
                            }
                        }

                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = imeAction,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyBoardController?.hide()
                            val code: String = codes.joinToString("")
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        Authentication(apiInterface).confirmEmail(code)
                                    }
                                    if (result.isSuccessful) {
                                        codeIsGood = true
                                    } else {
                                        val errBody = result.errorBody()?.string() ?: ""
                                        val errJson = JSONObject(errBody)
                                        errorMessage = errJson.get("detail").toString()
                                        invalidData = true
                                    }

                                } catch (e: Exception) {

                                }
                            }


                        }
                        ,onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors (unfocusedContainerColor = Color.White, unfocusedTextColor = Color(0xFF01042d),
                        focusedTextColor = Color.White)
                )

            }
        }
    }
    LaunchedEffect(isRunning) {
        while (isRunning && elapsedTime > 0) {
            delay(1000)
            elapsedTime = maxAllowedTime - System.currentTimeMillis()
        }
    }
}



@Composable
fun formatTime(timeMi: Long): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(timeMi) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMi) % 60

    val instrStr = "Please verify your email address and enter the code here."
    val eOfstring: String = if (min < 1) {
        "seconds"
    } else  {
        "minutes"
    }
    val timeLeft = String.format("%02d:%02d $eOfstring left.", min, seconds)
    return if (timeMi <= 0) {
        "Time expired. Please go back and create a new Sign Up request."
    } else {
        "$instrStr\nTime remaining: $timeLeft"
    }
}







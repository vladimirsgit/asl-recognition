package com.example.androidapp.requests

import com.example.androidapp.objects.ForgotPasswordRequest
import com.example.androidapp.objects.LogInRequest
import com.example.androidapp.objects.LogInResponse
import com.example.androidapp.objects.RefreshToken
import com.example.androidapp.objects.SignUpRequest
import retrofit2.Response

class Authentication (val apiInterface: ApiInterface){
    fun signUp(signUpRequest: SignUpRequest): Response<String?> {
        val call = apiInterface.signup(signUpRequest)
        return call.execute()
    }

    fun logIn(logInRequest: LogInRequest): Response<LogInResponse>{
        val call = apiInterface.login(logInRequest)
        return call.execute()
    }

    fun confirmEmail(code: String): Response<Any> {
        val call = apiInterface.confirmEmail(code)
        return call.execute()
    }

    fun validateToken(token: String): Response<Any> {
        val call = apiInterface.validateToken(token)
        return call.execute()
    }
    fun refreshAuth(token: RefreshToken): Response<LogInResponse> {
        val call = apiInterface.refreshAuth(token)
        return call.execute()
    }

    fun logOut(accessToken: String, refreshToken: RefreshToken): Response<Any> {
        val call = apiInterface.logOut(accessToken, refreshToken)
        return call.execute()
    }

    fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest): Response<Any> {
        val call = apiInterface.forgotPassword(forgotPasswordRequest)
        return call.execute()
    }
}
package com.example.androidapp.requests

import com.example.androidapp.objects.ForgotPasswordRequest
import com.example.androidapp.objects.LogInRequest
import com.example.androidapp.objects.LogInResponse
import com.example.androidapp.objects.RefreshToken
import com.example.androidapp.objects.SignUpRequest
import retrofit2.http.Body
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {
    @POST("auth/signup")
    fun signup(@Body signUpRequest: SignUpRequest): Call<String>

    @POST("auth/login")
    fun login(@Body logInRequest: LogInRequest): Call<LogInResponse>

    @GET("auth/confirm_email/{code}")
    fun confirmEmail(@Path("code") code: String): Call<Any>

    @GET("auth/validate_token")
    fun validateToken(@Header("Authorization") token: String): Call<Any>

    @POST("auth/refresh_session")
    fun refreshAuth(@Body refreshToken: RefreshToken): Call<LogInResponse>

    @POST("auth/logout")
    fun logOut(@Header("Authorization") token: String,
        @Body refreshToken: RefreshToken): Call<Any>

    @POST("auth/forgot_password")
    fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Call<Any>
}
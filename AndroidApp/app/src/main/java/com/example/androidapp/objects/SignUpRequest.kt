package com.example.androidapp.objects

data class SignUpRequest(
    val email: String,
    val initial_password: String,
    val confirmed_password: String
)

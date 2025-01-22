package com.example.androidapp.utils

import android.util.Patterns

fun validateSignUp(email: String, initialPassword: String, confirmedPassword: String): Boolean {
    return validateEmail(email) && validatePassword(initialPassword, confirmedPassword)
}

fun validateEmail(email: String): Boolean{
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun validatePassword(initialPassword: String, confirmedPassword: String): Boolean {
    return initialPassword.isNotEmpty() && initialPassword == confirmedPassword
}
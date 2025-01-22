package com.example.androidapp.utils

import android.content.SharedPreferences
import com.google.protobuf.Timestamp
import android.content.Context
import androidx.core.content.edit

object DataPersistence {
    private const val PREFS = "PREFS"
    private const val KEY_SIGNUP_TIMESTAMP = "signup_timestamp"
    private const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"
    private const val KEY_REFRESH_TOKEN = "REFRESH_TOKEN"


    fun saveSignupTimestamp(context: Context, timestamp: Long) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(KEY_SIGNUP_TIMESTAMP, timestamp)

        editor.apply()

    }

    fun loadSignupTimestamp(context: Context): Long {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        return sharedPreferences.getLong(KEY_SIGNUP_TIMESTAMP, -1)
    }

    fun saveAccessToken(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_ACCESS_TOKEN, token)

        editor.apply()
    }
    fun saveRefreshToken(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
    }

    fun loadAccessToken(context: Context) : String {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "").toString()
    }

    fun loadRefreshToken(context: Context) : String {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, "").toString()
    }

    fun deleteTokens(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
    }

 }
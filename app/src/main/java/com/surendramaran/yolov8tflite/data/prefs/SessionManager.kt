package com.surendramaran.yolov8tflite.data.prefs

import com.surendramaran.yolov8tflite.data.model.*

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(context: Context) {
    private var prefs: SharedPreferences
    private val editor: SharedPreferences.Editor

    companion object {
        const val PREFS_FILENAME = "app_prefs"
        const val USER_TOKEN = "user_token"
        const val USER_JSON = "user_json"
    }

    init {
        prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        editor = prefs.edit()
    }

    fun saveAuthToken(token: String) {
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        editor.putString(USER_JSON, userJson)
        editor.apply()
    }

    fun fetchUser(): User? {
        val userJson = prefs.getString(USER_JSON, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}
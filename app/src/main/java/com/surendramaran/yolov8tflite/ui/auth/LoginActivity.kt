package com.surendramaran.yolov8tflite.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.surendramaran.yolov8tflite.ui.startup.WelcomeActivity
import com.surendramaran.yolov8tflite.data.model.ErrorResponse
import com.surendramaran.yolov8tflite.data.model.LoginRequest
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.data.prefs.SessionManager
import com.surendramaran.yolov8tflite.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.net.ConnectException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.fetchAuthToken() != null) {
            navigateToMain()
            return
        }

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textViewGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        binding.textViewStatusMessage.visibility = View.GONE

        if (email.isEmpty() || password.isEmpty()) {
            showStatusMessage("Email dan Password tidak boleh kosong")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = RetrofitClient.getInstance(this@LoginActivity).login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.success) {
                        sessionManager.saveAuthToken(authResponse.accessToken!!)
                        sessionManager.saveUser(authResponse.user!!)
                        Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        showStatusMessage(authResponse.message)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Login Gagal. Terjadi kesalahan tidak diketahui."

                    if (errorBody != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            errorMessage = errorResponse.message
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Gagal parsing error body: $errorBody", e)
                        }
                    }
                    showStatusMessage(errorMessage)
                }

            } catch (e: ConnectException) {
                Log.e("LoginActivity", "Connection Error: ${e.message}", e)
                showStatusMessage("Tidak dapat terhubung ke server. Periksa koneksi Anda.")
            } catch (e: Exception) {
                Log.e("LoginActivity", "Exception: ${e.message}", e)
                showStatusMessage("Terjadi kesalahan: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.buttonLogin.isEnabled = true
            }
        }
    }

    private fun showStatusMessage(message: String) {
        binding.textViewStatusMessage.text = message
        binding.textViewStatusMessage.visibility = View.VISIBLE
    }

    private fun navigateToMain() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
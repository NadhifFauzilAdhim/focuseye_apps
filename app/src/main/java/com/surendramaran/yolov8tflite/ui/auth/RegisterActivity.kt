package com.surendramaran.yolov8tflite.ui.auth

import com.surendramaran.yolov8tflite.R

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.surendramaran.yolov8tflite.data.model.AuthResponse
import com.surendramaran.yolov8tflite.data.model.RegisterRequest
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import java.net.ConnectException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleDropdown()

        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.textViewGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupRoleDropdown() {
        val roles = resources.getStringArray(R.array.role_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.autoCompleteRole.setAdapter(adapter)
    }

    private fun registerUser() {
        val name = binding.editTextName.text.toString().trim()
        val username = binding.editTextUsername.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val role = binding.autoCompleteRole.text.toString().trim()

        binding.textViewStatusMessage.visibility = View.GONE

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            showStatusMessage("Semua field harus diisi.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatusMessage("Format email tidak valid.")
            return
        }
        if (password.length < 5) {
            showStatusMessage("Password minimal harus 5 karakter.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(name, username, email, password, role)
                val response = RetrofitClient.getInstance(this@RegisterActivity).register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    Toast.makeText(this@RegisterActivity, authResponse.message, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Registrasi Gagal. Terjadi kesalahan."

                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val errorResponse = Gson().fromJson(errorBody, AuthResponse::class.java)
                            if (errorResponse.errors != null) {
                                val specificErrors = errorResponse.errors.values.joinToString("\n") {
                                    it.firstOrNull() ?: ""
                                }
                                errorMessage = specificErrors
                            } else if (errorResponse.message.isNotEmpty()) {
                                errorMessage = errorResponse.message
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterActivity", "Gagal parsing error body: $errorBody", e)
                        }
                    }
                    showStatusMessage(errorMessage)
                }

            } catch (e: ConnectException) {
                Log.e("RegisterActivity", "Connection Error: ${e.message}", e)
                showStatusMessage("Tidak dapat terhubung ke server. Periksa koneksi Anda.")
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Exception: ${e.message}", e)
                showStatusMessage("Terjadi kesalahan: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.buttonRegister.isEnabled = true
            }
        }
    }

    private fun showStatusMessage(message: String) {
        binding.textViewStatusMessage.text = message
        binding.textViewStatusMessage.visibility = View.VISIBLE
    }
}

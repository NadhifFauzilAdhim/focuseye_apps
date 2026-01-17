package com.surendramaran.yolov8tflite.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.BuildConfig
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.data.prefs.SessionManager
import com.surendramaran.yolov8tflite.ui.news.NewsActivity
import com.surendramaran.yolov8tflite.ui.history.HistoryActivity
import com.surendramaran.yolov8tflite.ui.schedule.ScheduleListActivity
import com.surendramaran.yolov8tflite.ui.auth.LoginActivity
import com.surendramaran.yolov8tflite.databinding.ActivityMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import android.view.animation.AnimationUtils

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private val serverBaseUrl = BuildConfig.BASE_IMG_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = RetrofitClient.getInstance(this)

        if (sessionManager.fetchAuthToken() == null) {
            navigateToLogin()
            return
        }

        displayUserInfo()
        setupClickListeners()
        setupAnimations()
        checkServerStatus()
    }

    private fun setupClickListeners() {
        binding.cardStartAnalysis.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.cardViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.cardNews.setOnClickListener {
            startActivity(Intent(this, NewsActivity::class.java))
        }

        binding.cardSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleListActivity::class.java))
        }

        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun checkServerStatus() {
        binding.textViewServerStatus.text = "Mengecek status server..."
        lifecycleScope.launch {
            val isOnline = try {
                val response = apiService.checkServerStatus()
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                Log.e("MenuActivity", "Server check failed: ${e.message}")
                false
            }
            updateServerStatusUI(isOnline)
        }
    }

    private fun updateServerStatusUI(isOnline: Boolean) {
        if (isOnline) {
            binding.textViewServerStatus.text = "Online"
            binding.textViewServerStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.status_dot_online, 0, 0, 0
            )
        } else {
            binding.textViewServerStatus.text = "Upps, Can't Connect, Please Try Again"
            binding.textViewServerStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.status_dot_offline, 0, 0, 0
            )
        }
    }

    private fun setupAnimations() {
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.cardStartAnalysis.startAnimation(pulseAnimation)
    }

    private fun displayUserInfo() {
        val user = sessionManager.fetchUser()
        user?.let {
            val calendar = Calendar.getInstance()
            val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Selamat Pagi,"
                in 12..17 -> "Selamat Siang,"
                else -> "Selamat Malam,"
            }
            binding.textViewGreeting.text = greeting
            binding.textViewUserName.text = it.name

            if (!it.avatar.isNullOrEmpty()) {
                val fullAvatarUrl = serverBaseUrl + it.avatar
                Glide.with(this).load(fullAvatarUrl).circleCrop().placeholder(R.drawable.default_no_profile).error(R.drawable.default_no_profile).into(binding.imageViewAvatar)
            } else {
                Glide.with(this).load(R.drawable.default_no_profile).circleCrop().into(binding.imageViewAvatar)
            }
        }
    }

    // FUNGSI BARU UNTUK LOGOUT
    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                val response = apiService.logout()
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.i("MenuActivity", "Server logout successful.")
                } else {
                    Log.w("MenuActivity", "Server logout failed.")
                }
            } catch (e: Exception) {
                Log.e("MenuActivity", "Logout API call failed: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    sessionManager.clearSession()
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.fetchAuthToken() == null) {
            navigateToLogin()
        } else {
            displayUserInfo()
        }
    }
}
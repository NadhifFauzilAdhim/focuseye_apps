package com.surendramaran.yolov8tflite.ui.startup

import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.ui.home.MenuActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.surendramaran.yolov8tflite.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var isRedirecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWelcomeScreen()
    }

    private fun setupWelcomeScreen() {
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAgreementLogic()

        binding.buttonContinue.setOnClickListener {
            showLoader()
            val mainHandler = Handler(Looper.getMainLooper())

            mainHandler.postDelayed({
                if (binding.loaderGroup.visibility == View.VISIBLE) {
                    binding.gpuCheckText.visibility = View.VISIBLE
                }
            }, 800)

            mainHandler.postDelayed({
                redirectToMain()
            }, 2000)
        }
    }


    private fun setupAgreementLogic() {
        binding.checkboxAgreement.setOnCheckedChangeListener { _, isChecked ->
            binding.buttonContinue.isEnabled = isChecked
        }

        val fullText = binding.textAgreement.text.toString()
        val privacyPolicyText = "Kebijakan Privasi"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = "https://ndfproject.my.id/focuseye/privacy-policy"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        val startIndex = fullText.indexOf(privacyPolicyText)
        if (startIndex != -1) {
            spannableString.setSpan(
                clickableSpan,
                startIndex,
                startIndex + privacyPolicyText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.textAgreement.text = spannableString
            binding.textAgreement.movementMethod = LinkMovementMethod.getInstance()
        }
    }


    private fun showLoader() {
        binding.buttonContinue.visibility = View.INVISIBLE
        binding.welcomeIllustration.visibility = View.INVISIBLE
        binding.welcomeTitle.visibility = View.INVISIBLE
        binding.welcomeDescription.visibility = View.INVISIBLE
        binding.footerText.visibility = View.INVISIBLE
        binding.agreementLayout.visibility = View.INVISIBLE

        binding.buttonContinue.isEnabled = false

        binding.loaderGroup.visibility = View.VISIBLE
        binding.gpuCheckText.visibility = View.GONE
    }

    private fun hideLoader() {
        binding.buttonContinue.visibility = View.VISIBLE
        binding.welcomeIllustration.visibility = View.VISIBLE
        binding.welcomeTitle.visibility = View.VISIBLE
        binding.welcomeDescription.visibility = View.VISIBLE
        binding.footerText.visibility = View.VISIBLE
        binding.agreementLayout.visibility = View.VISIBLE
        binding.buttonContinue.isEnabled = binding.checkboxAgreement.isChecked
        binding.loaderGroup.visibility = View.GONE
    }

    private fun redirectToMain() {
        if (isRedirecting) return
        isRedirecting = true

        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (binding.loaderGroup.visibility == View.VISIBLE && !isRedirecting) {
            Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
            hideLoader()
        }
    }

    override fun onResume() {
        super.onResume()
        isRedirecting = false
    }
}
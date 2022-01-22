package com.raywenderlich.android.menagerie.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.view.doOnLayout
import com.raywenderlich.android.menagerie.R
import com.raywenderlich.android.menagerie.databinding.ActivitySettingsBinding
import com.raywenderlich.android.menagerie.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), SettingsView {

    private val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }
    private val settingsViewModel by viewModels<SettingsViewModel>()

    companion object {
        fun getIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel.setView(this)
        setContentView(binding.root)
        overridePendingTransition(0, 0)
        setupUi()
        setupCircularReveal(savedInstanceState)
    }

    private fun setupCircularReveal(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            binding.settingsRoot.visibility = View.INVISIBLE

            binding.settingsRoot.doOnLayout {
                revealCircular()
            }
        }
    }

    private fun revealCircular() {
        val rootHeight = binding.settingsRoot.height

        val centerX = binding.settingsButton.x
        val centerY = binding.settingsButton.y / 2

        val circularReveal = ViewAnimationUtils.createCircularReveal(
            binding.settingsRoot,
            centerX.toInt(),
            centerY.toInt(),
            0f,
            rootHeight.toFloat() * 2f
        )

        circularReveal.duration = 1000

        binding.settingsRoot.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun setupUi() {
        binding.settingsButton.setOnClickListener { exitCircular() }
        binding.petSleep.setOnClickListener { settingsViewModel.onPetSleepTap() }
        binding.logOut.setOnClickListener { settingsViewModel.logOut() }
        binding.petSleep.setMaxFrame(140)

        settingsViewModel.pets.observe(this, { pets ->
            val isAnimating = binding.petSleep.isAnimating

            if (isAnimating) return@observe

            val arePetsAsleep = pets.all { it.isSleeping }
            val lottieProgress = binding.petSleep.progress

            if (arePetsAsleep && lottieProgress != 1f) {
                binding.petSleep.speed = 1f
                binding.petSleep.playAnimation()
            } else if (!arePetsAsleep && lottieProgress != 0f) {
                binding.petSleep.speed = -1f
                binding.petSleep.playAnimation()
            }
        })
    }

    override fun onUserLoggedOut() {
        startActivity(LoginActivity.getIntent(this))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() = exitCircular()

    private fun exitCircular() {
        val rootHeight = binding.settingsRoot.height
        val centerX = binding.settingsButton.x
        val centerY = binding.settingsButton.y / 2

        val circularReveal = ViewAnimationUtils.createCircularReveal(
            binding.settingsRoot,
            centerX.toInt(),
            centerY.toInt(),
            rootHeight * 1.2f,
            0f
        )

        circularReveal.duration = 1000
        circularReveal.addListener(onEnd = {
            finish()
            overridePendingTransition(0, 0)
        })

        circularReveal.start()
    }
}
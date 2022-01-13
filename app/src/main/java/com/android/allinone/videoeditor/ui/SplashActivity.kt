package com.android.allinone.videoeditor.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.ActivitySplashBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    lateinit var mAppUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 11
    var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                Log.e("TAdsvgsdG", "InstallStateUpdatedListener: state: " + state.installStatus())
                when {
                    state.installStatus() == InstallStatus.DOWNLOADED -> {
                        popupSnackBarForCompleteUpdate()
                    }
                    state.installStatus() == InstallStatus.INSTALLED -> {
                        mAppUpdateManager.unregisterListener(this)
                    }
                    else -> {
                        Log.e(
                            "TAdsvgsdG",
                            "InstallStateUpdatedListener: state: " + state.installStatus()
                        )
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("LogD", "onActivityResult: app download failed")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        Log.e("SplashActivity", "checkForAppUpdateAvailability: onResume")

        mAppUpdateManager.registerListener(installStateUpdatedListener)

        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Log.e("SplashActivity", "checkForAppUpdateAvailability: onSuccess")
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this@SplashActivity,
                        RC_APP_UPDATE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            } else {
                Log.e("SplashActivity", "checkForAppUpdateAvailability: something else")
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            binding.container,
            "New app is ready!",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(
                "Install"
            ) { mAppUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.colorPrimary))
            show()
        }
    }

    override fun onStop() {
        super.onStop()
        mAppUpdateManager.unregisterListener(installStateUpdatedListener)
    }


}
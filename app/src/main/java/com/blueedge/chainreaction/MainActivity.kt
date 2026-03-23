package com.blueedge.chainreaction

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.blueedge.chainreaction.ads.InterstitialAdManager
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.platform.ServiceLocator
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "InAppUpdate"
    }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val snackbarHostState = SnackbarHostState()

    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                Log.d(TAG, "Update flow cancelled or failed, result code: ${result.resultCode}")
                // If an immediate update was cancelled, you could re-prompt or close the app.
            }
        }

    /** Listener for flexible update download progress / completion. */
    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateSnackbar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameConfig.initStorage(com.blueedge.chainreaction.bridge.AndroidPlatformStorage(this))
        GameConfig.load()
        SoundManager.init(this)
        InterstitialAdManager.initializeSdk(this)
        enableEdgeToEdge()
        hideSystemBars()

        // Wire platform services into shared module
        val androidSoundPlayer = com.blueedge.chainreaction.bridge.AndroidSoundPlayer
        val androidAdManager = com.blueedge.chainreaction.bridge.AndroidAdManager(this)
        ServiceLocator.soundPlayer = androidSoundPlayer
        ServiceLocator.adManager = androidAdManager

        // Check for app updates
        checkForUpdate()

        setContent {
            Box(Modifier.fillMaxSize()) {
                App(
                    soundPlayer = androidSoundPlayer,
                    adManager = androidAdManager
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.resumeMusic()

        // If an immediate update was in progress, resume it.
        // If a flexible update was downloaded while the app was in the background, prompt install.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume immediate update that was interrupted
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }

            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateSnackbar()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
        GameConfig.save()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
        appUpdateManager.unregisterListener(installStateListener)
    }

    // ──────────────────────────────────────────────────────────────
    //  In-App Update helpers
    // ──────────────────────────────────────────────────────────────

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when {
                    // Prefer immediate update if allowed (use for critical fixes)
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        )
                    }
                    // Fall back to flexible update
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        appUpdateManager.registerListener(installStateListener)
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Update check failed", e)
        }
    }

    /**
     * Shows a Snackbar prompting the user to restart the app
     * after a flexible update has been downloaded.
     */
    private fun showUpdateSnackbar() {
        lifecycleScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "An update has been downloaded.",
                actionLabel = "RESTART",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
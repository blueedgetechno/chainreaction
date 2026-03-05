package com.blueedge.chainreaction

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.blueedge.chainreaction.ads.InterstitialAdManager
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.ui.navigation.ChainReactionNavGraph
import com.blueedge.chainreaction.ui.theme.ChainReactionTheme
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "InAppUpdate"
    }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

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
        GameConfig.load(this)
        SoundManager.init(this)
        InterstitialAdManager.initializeSdk(this)
        enableEdgeToEdge()
        hideSystemBars()

        // Check for app updates
        checkForUpdate()

        setContent {
            ChainReactionTheme {
                val navController = rememberNavController()
                ChainReactionNavGraph(navController = navController)
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
        GameConfig.save(this)
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
        Snackbar.make(
            findViewById(android.R.id.content),
            "An update has been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            show()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
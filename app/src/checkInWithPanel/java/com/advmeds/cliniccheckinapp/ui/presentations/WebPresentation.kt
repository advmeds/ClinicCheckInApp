package com.advmeds.cliniccheckinapp.ui.presentations

import android.app.Presentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaRouter
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import timber.log.Timber

/** 副螢幕網頁輸出 */
class WebPresentation(
    outerContext: Context,
    display: Display
) : Presentation(outerContext, display) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(context)

    private val geckoView: GeckoView by lazy {
        findViewById(R.id.geckoView)
    }

    private val progressBar: ProgressBar by lazy {
        findViewById(R.id.progressBar)
    }
    private val reconnectingLayout: RelativeLayout by lazy {
        findViewById(R.id.layout_reconnecting)
    }

    private val session = GeckoSession()

    private val runtime: GeckoRuntime by lazy {
        GeckoRuntime.getDefault(context)
    }

    private var autoReloadJob: Job? = null

    private val reloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            reload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_presentation)

        LocalBroadcastManager.getInstance(context).registerReceiver(
            reloadReceiver,
            IntentFilter(SharedPreferencesRepo.CLINIC_PANEL_MODE).apply {
                addAction(SharedPreferencesRepo.ORG_ID)
                addAction(SharedPreferencesRepo.ROOMS)
            }
        )

        runtime.settings.autoplayDefault = GeckoRuntimeSettings.AUTOPLAY_DEFAULT_ALLOWED
        runtime.settings.javaScriptEnabled = true

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(p0: GeckoSession, p1: String) {
                Timber.d("onPageStart: $p0, $p1")

                progressBar.visibility = View.VISIBLE
                autoReloadJob?.takeIf { it.isActive }?.cancel()
                reconnectingLayout.visibility = View.GONE
            }

            override fun onPageStop(p0: GeckoSession, p1: Boolean) {
                Timber.d("onPageStop: $p0, $p1")

                progressBar.visibility = View.GONE
                autoReloadJob?.takeIf { it.isActive }?.cancel()
                reconnectingLayout.visibility = if (!p1) View.VISIBLE else View.GONE

                if (!p1) {
                    autoReloadJob = MainScope().launch {
                        delay(15000)
                        reload()
                    }
                }
            }

            override fun onProgressChange(p0: GeckoSession, p1: Int) {
                Timber.d("onProgressChange: $p0, $p1")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(p1, true)
                } else {
                    progressBar.progress = p1
                }
            }
        }

        if (!session.isOpen) {
            session.open(runtime)
            geckoView.setSession(session)
        }

        reload()
    }

    private fun reload() {
        if (session.isOpen) {
            session.loadUri(
                getUrl(),
                GeckoSession.LOAD_FLAGS_BYPASS_CACHE
            )
        }
    }

    private fun getUrl(): String {
        val url = sharedPreferencesRepo.clinicPanelUrl ?: ""
        return String.format(url, sharedPreferencesRepo.orgId, sharedPreferencesRepo.rooms.firstOrNull())
    }

    override fun onStop() {
        super.onStop()

        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(reloadReceiver)
        } catch (ignored: Exception) {

        }

        session.progressDelegate = null
        autoReloadJob?.takeIf { it.isActive }?.cancel()

        geckoView.releaseSession()?.also {
            it.close()
        }

        runtime.shutdown()
    }

    companion object {
        fun newInstance(context: Context): WebPresentation? {
            val mediaRouter =
                context.getSystemService(ComponentActivity.MEDIA_ROUTER_SERVICE) as MediaRouter
            val presentationDisplay =
                mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO).presentationDisplay
            return if (presentationDisplay != null) {
                WebPresentation(
                    context,
                    presentationDisplay
                )
            } else {
                null
            }
        }
    }
}
package com.vicationfly.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var root: FrameLayout
    private var web: WebView? = null
    private var splash: TextView? = null
    private val url = "https://vicationfly-8n1zpr.v2.appdeploy.ai/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(255,247,239)
        window.navigationBarColor = Color.rgb(255,247,239)
        buildUi()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildUi() {
        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(255,247,239))
        val browser = WebView(this)
        web = browser
        browser.setBackgroundColor(Color.rgb(255,247,239))
        browser.overScrollMode = View.OVER_SCROLL_NEVER
        browser.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
        }
        browser.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { hideSplash() }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) showRetry()
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                root.removeView(view)
                view.destroy()
                web = null
                root.postDelayed({ if (!isFinishing && !isDestroyed) buildUi() }, 250)
                return true
            }
        }
        root.addView(browser, FrameLayout.LayoutParams(-1, -1))
        showSplash()
        setContentView(root)
        if (hasNetwork()) browser.loadUrl(url) else showRetry()
    }

    private fun showSplash() {
        splash = TextView(this).apply {
            text = "✈\nVicationfly\na whole vacation in one place"
            textSize = 26f
            gravity = 17
            setTextColor(Color.rgb(255,122,0))
            setBackgroundColor(Color.rgb(255,247,239))
        }
        root.addView(splash, FrameLayout.LayoutParams(-1, -1))
    }

    private fun hideSplash() {
        splash?.animate()?.alpha(0f)?.setDuration(350)?.withEndAction {
            splash?.let { root.removeView(it) }
            splash = null
        }?.start()
    }

    private fun showRetry() {
        hideSplash()
        val retry = TextView(this).apply {
            text = "Vicationfly\n\nUnable to load right now.\nCheck your internet connection and tap here to retry."
            textSize = 17f
            gravity = 17
            setTextColor(Color.rgb(125,78,48))
            setBackgroundColor(Color.rgb(255,247,239))
            setOnClickListener {
                root.removeView(this)
                web?.loadUrl(url) ?: buildUi()
            }
        }
        root.addView(retry, FrameLayout.LayoutParams(-1, -1))
    }

    private fun hasNetwork(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        web?.apply {
            stopLoading()
            webViewClient = WebViewClient()
            destroy()
        }
        web = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Android 13; retained for older devices")
    override fun onBackPressed() {
        val browser = web
        if (browser != null && browser.canGoBack()) browser.goBack() else super.onBackPressed()
    }
}

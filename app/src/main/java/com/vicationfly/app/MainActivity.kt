package com.vicationfly.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Gravity
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var root: FrameLayout
    private var browser: WebView? = null
    private val appUrl = "https://vicationfly-8n1zpr.v2.appdeploy.ai/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(255,247,237)
        window.navigationBarColor = Color.rgb(255,247,237)
        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(255,247,237))
        setContentView(root)
        showLaunchScreen()
        root.postDelayed({ openApp() }, 850)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openApp() {
        val web = WebView(this)
        browser = web
        web.setBackgroundColor(Color.rgb(255,247,237))
        web.overScrollMode = WebView.OVER_SCROLL_NEVER
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                root.post { removeLaunchScreen() }
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) showError()
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                root.removeView(view)
                view.destroy()
                browser = null
                root.postDelayed({ if (!isFinishing && !isDestroyed) openApp() }, 300)
                return true
            }
        }
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        if (hasInternet()) web.loadUrl(appUrl) else showError()
    }

    private fun showLaunchScreen() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(255,247,237))
            val icon = TextView(context).apply {
                text = "✈"
                textSize = 58f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(255,107,0))
            }
            addView(icon, LinearLayout.LayoutParams(-1, 90))
            val title = TextView(context).apply {
                text = "Vicationfly"
                textSize = 36f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(55,32,20))
                setTypeface(typeface, 1)
            }
            addView(title, LinearLayout.LayoutParams(-1, 55))
            val sub = TextView(context).apply {
                text = "FLY • EXPLORE • ESCAPE"
                textSize = 11f
                letterSpacing = .18f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(174,113,72))
            }
            addView(sub, LinearLayout.LayoutParams(-1, 40))
        }
        root.addView(box, FrameLayout.LayoutParams(-1, -1))
    }

    private fun removeLaunchScreen() {
        if (root.childCount > 1) root.removeViewAt(0)
    }

    private fun showError() {
        while (root.childCount > 0) root.removeViewAt(0)
        val message = TextView(this).apply {
            text = "Vicationfly\n\nNo internet connection.\nTap to try again."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100,62,39))
            setBackgroundColor(Color.rgb(255,247,237))
            setOnClickListener { openApp() }
        }
        root.addView(message, FrameLayout.LayoutParams(-1, -1))
    }

    private fun hasInternet(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return true
        val n = cm.activeNetwork ?: return false
        val c = cm.getNetworkCapabilities(n) ?: return false
        return c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        val web = browser
        if (web?.canGoBack() == true) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        browser?.apply { stopLoading(); webViewClient = WebViewClient(); destroy() }
        browser = null
        super.onDestroy()
    }
}

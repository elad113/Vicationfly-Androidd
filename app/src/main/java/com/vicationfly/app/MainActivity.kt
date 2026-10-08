package com.vicationfly.app

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val frame = FrameLayout(this)
        frame.setBackgroundColor(Color.rgb(255,247,239))
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        frame.addView(web, FrameLayout.LayoutParams(-1,-1))
        val splash = TextView(this)
        splash.text = "✈\nVicationfly\na whole vacation in one place"
        splash.textSize = 26f
        splash.gravity = 17
        splash.setTextColor(Color.rgb(255,122,0))
        frame.addView(splash, FrameLayout.LayoutParams(-1,-1))
        setContentView(frame)
        web.loadUrl("https://vicationfly-8n1zpr.v2.appdeploy.ai/")
        web.postDelayed({ splash.animate().alpha(0f).setDuration(400).withEndAction { frame.removeView(splash) }.start() }, 2200)
    }
}

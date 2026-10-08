package com.vicationfly.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String URL = "https://vicationfly-8n1zpr.v2.appdeploy.ai/";
    private FrameLayout root;
    private WebView web;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(255,247,237));
        getWindow().setNavigationBarColor(Color.rgb(255,247,237));
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(255,247,237));
        setContentView(root);
        showSplash();
        root.postDelayed(new Runnable() { public void run() { createWebView(); } }, 350);
    }

    private void showSplash() {
        TextView t = new TextView(this);
        t.setText("✈\nVicationfly");
        t.setTextSize(34);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.rgb(255,107,0));
        t.setBackgroundColor(Color.rgb(255,247,237));
        t.setTypeface(null, 1);
        t.setId(1001);
        root.addView(t, new FrameLayout.LayoutParams(-1,-1));
    }

    private void createWebView() {
        if (isFinishing()) return;
        final WebView w = new WebView(this);
        web = w;
        w.setBackgroundColor(Color.rgb(255,247,237));
        w.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        w.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                View splash = root.findViewById(1001);
                if (splash != null) root.removeView(splash);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) showRetry();
            }
            @Override public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                root.removeView(view);
                view.destroy();
                web = null;
                showRetry();
                return true;
            }
        });
        root.addView(w, new FrameLayout.LayoutParams(-1,-1));
        w.loadUrl(URL);
    }

    private void showRetry() {
        if (isFinishing()) return;
        while (root.getChildCount() > 0) root.removeViewAt(0);
        TextView t = new TextView(this);
        t.setText("Vicationfly\n\nלא ניתן לטעון כרגע.\nלחץ כדי לנסות שוב");
        t.setTextSize(18);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.rgb(70,45,30));
        t.setBackgroundColor(Color.rgb(255,247,237));
        t.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { createWebView(); }});
        root.addView(t, new FrameLayout.LayoutParams(-1,-1));
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.setWebViewClient(null);
            root.removeView(web);
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}

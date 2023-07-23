package com.example.beachfinder1

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView)
        val url = "https://survey123.arcgis.com/share/918c6616c2694c48b031dfb5fe8a39ca"
        webView.loadUrl(url)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

    }
}

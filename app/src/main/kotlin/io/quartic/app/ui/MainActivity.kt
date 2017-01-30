package io.quartic.app.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.webkit.WebView
import io.quartic.app.R
import io.quartic.app.sensors.SensorService
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG  by tag()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        SensorService.startService(applicationContext)
        render()
    }

    override fun onResume() {
        super.onResume()

        // We immediately transition to login if we don't have a userId
        val state = ApplicationState.get(applicationContext)
        if (state.userId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }

    fun render() {
        Log.i(TAG, "rendering")
        val applicationState = ApplicationState.get(applicationContext)
        val webView = findViewById(R.id.webview) as WebView
        webView.loadData("""
            <html>
            <body>
            <b>User Id: </b> ${applicationState.userId} <br>
            <b>Backlog Size:</b> ${applicationState.database.backlogSize}
            </body>
            </html>
        """, "text/html", null)
    }
}

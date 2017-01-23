package io.quartic.app.ui

import android.Manifest
import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.editorActions
import com.jakewharton.rxbinding.widget.textChanges
import io.quartic.app.sensors.SensorService
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.generateKeyPair
import io.quartic.app.publicKey
import io.quartic.app.state.ApplicationState
import io.quartic.tracker.api.RegistrationRequest
import rx.Observable.empty
import rx.lang.kotlin.merge

class LoginActivity : Activity() {

    private var loginTask: UserLoginTask? = null
    private lateinit var signInButton: Button
    private lateinit var codeText: EditText
    private lateinit var progressView: View
    private lateinit var loginFormView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, 0)
        Log.i("LoginActivity", "request perms")
        loadPermissions("com.google.android.gms.permission.ACTIVITY_RECOGNITION", 0)
        configureWidgets()
        generateKeyPair()
        SensorService.startService(applicationContext)
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(applicationContext, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
            }
        }
    }

    private fun configureWidgets() {
        setContentView(io.quartic.app.R.layout.activity_login)

        signInButton = findViewById(io.quartic.app.R.id.sign_in_button) as Button
        codeText = findViewById(io.quartic.app.R.id.code) as EditText
        loginFormView = findViewById(io.quartic.app.R.id.login_form)
        progressView = findViewById(io.quartic.app.R.id.login_progress)

        // TODO: lifecycle stuff

        val editorTriggerEvents = codeText.editorActions().filter { it == io.quartic.app.R.id.login || it == EditorInfo.IME_NULL }
        val signInTriggerEvents = signInButton.clicks()
        val loginTriggerEvents = listOf(editorTriggerEvents, signInTriggerEvents).merge()
        codeText.textChanges()
                .map { it.length >= MINIMUM_CODE_LENGTH }
                .doOnNext { signInButton.isEnabled = it }
                .switchMap { if (it) loginTriggerEvents else empty() }
                .subscribe { attemptLogin() }
    }

    private fun attemptLogin() {
        if (loginTask != null) {
            return
        }

        showProgress(true)
        loginTask = UserLoginTask(codeText.text.toString())
        loginTask!!.execute(null)
    }

    private fun showProgress(show: Boolean) {
        animate(loginFormView, if (show) View.GONE else View.VISIBLE, if (show) 0.0f else 1.0f)
        animate(progressView, if (show) View.VISIBLE else View.GONE, if (show) 1.0f else 0.0f)
    }

    private fun animate(view: View, visibility: Int, alpha: Float) {
        view.visibility = visibility
        view.animate()
                .setDuration(resources.getInteger(R.integer.config_shortAnimTime).toLong())
                .alpha(alpha)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = visibility
                    }
                })
    }

    private inner class UserLoginTask constructor(private val code: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            val request = RegistrationRequest(code, Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP))

            val state = ApplicationState.get(applicationContext)
            // TODO: we should inject this
            val registration = state.client

            val observable = registration.register(request)

            var success = false
            observable.subscribe(
                    { resp ->
                        state.userId = resp.userId
                        success = true
                    },
                    { Log.e("LoginActivity", "Error registering with server", it) }
            )

            // TODO: if 2xx then cool - finish() activity (who's responsible for updating state in local storage?)
            // TODO: if 4xx then say "code incorrect"
            // TODO: if 5xx then say "server error - please try again later"

            return success
        }

        override fun onPostExecute(success: Boolean?) {
            loginTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                codeText.error = getString(io.quartic.app.R.string.error_unrecognised_code)
                codeText.requestFocus()
            }
        }

        override fun onCancelled() {
            loginTask = null
            showProgress(false)
        }
    }

    companion object {
        private val MINIMUM_CODE_LENGTH = 4
    }
}


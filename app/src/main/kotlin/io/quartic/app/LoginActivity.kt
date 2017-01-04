package io.quartic.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.editorActions
import com.jakewharton.rxbinding.widget.textChanges
import io.quartic.app.api.BackendApi
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
        configureWidgets()
        generateKeyPair()
    }

    private fun configureWidgets() {
        setContentView(R.layout.activity_login)

        signInButton = findViewById(R.id.sign_in_button) as Button
        codeText = findViewById(R.id.code) as EditText
        loginFormView = findViewById(R.id.login_form)
        progressView = findViewById(R.id.login_progress)

        // TODO: lifecycle stuff

        val editorTriggerEvents = codeText.editorActions().filter { it == R.id.login || it == EditorInfo.IME_NULL }
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
                .setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
                .alpha(alpha)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = visibility
                    }
                })
    }

    private inner class UserLoginTask constructor(private val code: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            val request = RegistrationRequest(code, Base64.encodeToString(publicKey.encoded, Base64.DEFAULT))

            // TODO: we should inject this
            val registration = clientOf<BackendApi>("http://localhost:5555")

            val observable = registration.register(request)

            observable.subscribe(
                    { Log.d("LoginActivity", it.toString()) },
                    { Log.e("LoginActivity", "Error registering with server", it) }
            )

            // TODO: send public key + code in request
            // TODO: if 2xx then cool - finish() activity (who's responsible for updating state in local storage?)
            // TODO: if 4xx then say "code incorrect"
            // TODO: if 5xx then say "server error - please try again later"

            return false
        }

        override fun onPostExecute(success: Boolean?) {
            loginTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                codeText.error = getString(R.string.error_unrecognised_code)
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


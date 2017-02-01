package io.quartic.app.ui

import android.Manifest
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
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.editorActions
import com.jakewharton.rxbinding.widget.textChanges
import io.quartic.app.KeyManager
import io.quartic.app.R
import io.quartic.app.api.unauthedBackendClient
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag
import io.quartic.app.ui.LoginActivity.Result.*
import io.quartic.tracker.api.RegistrationRequest
import retrofit2.adapter.rxjava.HttpException
import rx.Observable.empty
import rx.lang.kotlin.merge

class LoginActivity : Activity() {
    private val TAG by tag()

    private var loginTask: UserLoginTask? = null
    private lateinit var state: ApplicationState
    private lateinit var keyManager: KeyManager
    private lateinit var signInButton: Button
    private lateinit var codeText: EditText
    private lateinit var progressView: View
    private lateinit var loginFormView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, 0)
        Log.i(TAG, "request perms")
        loadPermissions("com.google.android.gms.permission.ACTIVITY_RECOGNITION", 0)
        configureWidgets()

        state = ApplicationState.get(applicationContext)
        keyManager = KeyManager(state)

        keyManager.generateKeyPairIfMissing()
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

        codeText.error = null
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

    enum class Result {
        SUCCESS,
        INCORRECT_CODE,
        OTHER_ERROR
    }

    private inner class UserLoginTask constructor(private val code: String) : AsyncTask<Void, Void, Result>() {
        override fun doInBackground(vararg params: Void): Result {
            val request = RegistrationRequest(code, Base64.encodeToString(keyManager.publicKey.encoded, Base64.NO_WRAP))
            var result = OTHER_ERROR

            unauthedBackendClient(state).register(request).subscribe(
                    { resp ->
                        state.userId = resp.userId
                        result = SUCCESS
                    },
                    {
                        Log.e(TAG, "Error registering with server", it)
                        if (it is HttpException && it.code() == 401) {
                            result = INCORRECT_CODE
                        }
                    }
            )
            return result
        }

        override fun onPostExecute(result: Result) {
            loginTask = null
            showProgress(false)

            when (result) {
                SUCCESS -> finish()
                INCORRECT_CODE -> {
                    codeText.error = getString(R.string.error_unrecognised_code)
                    codeText.requestFocus()
                }
                OTHER_ERROR -> {
                    Toast.makeText(applicationContext, R.string.error_server, LENGTH_LONG).show()
                    codeText.requestFocus()
                }
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


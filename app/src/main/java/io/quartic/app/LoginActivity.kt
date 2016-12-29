package io.quartic.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import io.quartic.app.api.RegistrationRequest
import io.quartic.app.api.RegistrationService
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class LoginActivity : Activity() {

    private var loginTask: UserLoginTask? = null
    private lateinit var codeView: EditText
    private lateinit var progressView: View
    private lateinit var loginFormView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureWidgets()
        generateKeyPair()
    }

    private fun configureWidgets() {
        setContentView(R.layout.activity_login)

        val signInButton = findViewById(R.id.sign_in_button) as Button

        codeView = findViewById(R.id.code) as EditText
        codeView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable) {
                signInButton.isEnabled = isCodeValid(s.toString())
            }
        })
        codeView.setOnEditorActionListener { textView, id, keyEvent ->
            if ((id == R.id.login || id == EditorInfo.IME_NULL) && signInButton.isEnabled) {
                attemptLogin()
                true
            } else {
                false
            }
        }

        signInButton.setOnClickListener { view -> attemptLogin() }

        loginFormView = findViewById(R.id.login_form)
        progressView = findViewById(R.id.login_progress)
    }

    private fun attemptLogin() {
        if (loginTask != null) {
            return
        }

        codeView.error = null
        val code = codeView.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid code
        if (isEmpty(code)) {
            codeView.error = getString(R.string.error_field_required)
            focusView = codeView
            cancel = true
        } else if (!isCodeValid(code)) {
            codeView.error = getString(R.string.error_invalid_code)
            focusView = codeView
            cancel = true
        }

        if (cancel) {
            focusView!!.requestFocus()
        } else {
            showProgress(true)
            loginTask = UserLoginTask(code)
            loginTask!!.execute(null)
        }
    }

    private fun isCodeValid(code: String) = code.length >= MINIMUM_CODE_LENGTH

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


            val retrofit = Retrofit.Builder()
                    .baseUrl("http://localhost:5555")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build()

            val registration = retrofit.create(RegistrationService::class.java)

            registration.register(request)



            // TODO: send request and receive response

            // TODO: send public key + code in request
            // TODO: if 2xx then cool - finish() activity (who's responsible for updating state in local storage?)
            // TODO: if 4xx then say "code incorrect"
            // TODO: if 5xx then say "server error - please try again later"


            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return false
            }

            for (trialCode in DUMMY_CODES) {
                if (code == trialCode) {
                    return true
                }
            }

            return false
        }

        override fun onPostExecute(success: Boolean?) {
            loginTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                codeView.error = getString(R.string.error_unrecognised_code)
                codeView.requestFocus()
            }
        }

        override fun onCancelled() {
            loginTask = null
            showProgress(false)
        }
    }

    companion object {
        private val MINIMUM_CODE_LENGTH = 4

        // TODO: remove these
        private val DUMMY_CODES = arrayOf("1234", "5678")
    }
}


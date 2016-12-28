package io.quartic.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends Activity {

    private static final int EXPECTED_CODE_LENGTH = 4;

    // TODO: remove these
    private static final String[] DUMMY_CODES = { "1234", "5678" };

    private UserLoginTask loginTask = null;
    private EditText codeView;
    private View progressView;
    private View loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final Button signInButton = (Button) findViewById(R.id.sign_in_button);

        codeView = (EditText) findViewById(R.id.code);
        codeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                signInButton.setEnabled(isCodeValid(s.toString()));
            }
        });
        codeView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if ((id == R.id.login || id == EditorInfo.IME_NULL) && signInButton.isEnabled()) {
                attemptLogin();
                return true;
            }
            return false;
        });

        signInButton.setOnClickListener(view -> attemptLogin());

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (loginTask != null) {
            return;
        }

        codeView.setError(null);
        String code = codeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid code
        if (isEmpty(code)) {
            codeView.setError(getString(R.string.error_field_required));
            focusView = codeView;
            cancel = true;
        } else if (!isCodeValid(code)) {
            codeView.setError(getString(R.string.error_invalid_code));
            focusView = codeView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            loginTask = new UserLoginTask(code);
            loginTask.execute((Void) null);
        }
    }

    private boolean isCodeValid(String code) {
        return code.length() == EXPECTED_CODE_LENGTH;
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String code;

        UserLoginTask(String code) {
            this.code = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String trialCode : DUMMY_CODES) {
                if (code.equals(trialCode)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            loginTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                codeView.setError(getString(R.string.error_unrecognised_code));
                codeView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            loginTask = null;
            showProgress(false);
        }
    }
}


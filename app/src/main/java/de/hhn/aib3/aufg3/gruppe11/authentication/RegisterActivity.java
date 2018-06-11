package de.hhn.aib3.aufg3.gruppe11.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;


/**
 * A login screen that offers login via register/password.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private static final String DEBUGLOG_TAG = "DEBUGLOG_RA";
    private AutoCompleteTextView registerView;
    private EditText passwordView;
    private EditText passwordConfirmationView;
    private View progressView;
    private View loginFormView;
    private View focusView = null;

    private RestService restService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        // Set up the login form.
        registerView = (AutoCompleteTextView) findViewById(R.id.registration_register);
        passwordConfirmationView = (EditText) findViewById(R.id.edit_text_registration_password_confirmation);
        passwordView = (EditText) findViewById(R.id.edit_text_registration_password);

        Button registerSignInButton = (Button) findViewById(R.id.registration_register_sign_in_button);
        registerSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Utility.refreshEventBus(this);

        loginFormView = findViewById(R.id.registration_form);
        progressView = findViewById(R.id.registration_progress);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));
    }

    @Subscribe
    public void onEvent(RestEvent restEvent) {
        Event event = restEvent.getEvent();
        int responseCode;

        if (event == Event.REGISTER) {
            if (restEvent.isWsConnected()) {
                Log.d(DEBUGLOG_TAG, "RegisterActivity RegisterEvent received");
                responseCode = restEvent.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    finish();
                    Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_SHORT).show();
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    showProgress(false);
                    registerView.setError(getString(R.string.error_username_already_exists));
                    focusView = registerView;
                } else {
                    Toast.makeText(this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            } else {
                showProgress(false);
                Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
            }
        }

        Utility.refreshEventBus(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid register, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        registerView.setError(null);
        passwordView.setError(null);
        passwordConfirmationView.setError(null);

        // Store values at the time of the login attempt.
        String username = registerView.getText().toString();
        String password = passwordView.getText().toString();
        String passwordConfirmation = passwordConfirmationView.getText().toString();

        boolean cancel = false;

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password, passwordConfirmation)) {
            cancel = true;
        }

        // Validate Username
        if (TextUtils.isEmpty(username)) {
            registerView.setError(getString(R.string.error_field_required));
            focusView = registerView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Log.d(DEBUGLOG_TAG, "Registration credentials locally validated.");
            showProgress(true);
            if (restService != null) {
//                Client client = new Client(username, Utility.hash(password));
                restService.registerClient(username, password);
            }
        }
    }

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String REGEXP_NUMBERS = ".*\\d+.*";
    private static final String REGEXP_ALPHANUMERICS = ".*\\W+.*";

    /**
     * Validates password strength LOCALLY - sets Error messages accordingly
     * Criteria: (1) length >= 8 (2) contains number/s (3) contains symbol/s (4) pws match
     *
     * @param password plain text password
     * @return true: Criteria met
     * false: Criteria not met
     */
    private boolean isPasswordValid(String password, String passwordConfirmation) {

        if (password.length() >= MIN_PASSWORD_LENGTH) {
            if (password.matches(REGEXP_NUMBERS)) {
                if (password.matches(REGEXP_ALPHANUMERICS)) {
                    if (password.equals(passwordConfirmation)) {
                        Log.d(DEBUGLOG_TAG, "PASSWORD VALID");
                        return true;
                    } else {
                        Log.d(DEBUGLOG_TAG, "PASSWORDS DIFFER");
                        passwordConfirmationView.setError(getString(R.string.error_invalid_password_match));
                        focusView = passwordConfirmationView;
                        return false;
                    }
                } else {
                    Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN SYMBOLS");
                    passwordView.setError(getString(R.string.error_invalid_password_symbol));
                    focusView = passwordView;
                    return false;
                }
            } else {
                Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN NUMBERS");
                passwordView.setError(getString(R.string.error_invalid_password_number));
                focusView = passwordView;
                return false;
            }
        } else {
            Log.d(DEBUGLOG_TAG, "PASSWORD LENGTH TOO SHORT");
            passwordView.setError(getString(R.string.error_invalid_password_length));
            focusView = passwordView;
            return false;
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}
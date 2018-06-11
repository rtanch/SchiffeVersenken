package de.hhn.aib3.aufg3.gruppe11.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.gui.activities.PlacementActivity;
import de.hhn.aib3.aufg3.gruppe11.lobby.LobbyActivity;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private static final String DEBUGLOG_TAG = "DEBUGLOG-LA";
    private AutoCompleteTextView usernameView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    private RestService restService = null;
    private Client client = null;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(DEBUGLOG_TAG, "Service connected");
            RestService.LocalService localService = (RestService.LocalService) service;
            restService = localService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            restService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.login_username);
        passwordView = (EditText) findViewById(R.id.edit_text_login_password);

        TextView signUpView = (TextView) findViewById(R.id.login_signup);
        signUpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RegisterActivity.class);
                intent.putExtra(getString(R.string.extra_restService), restService);
                startActivity(intent);
            }
        });

        Button userSignInButton = (Button) findViewById(R.id.login_username_sign_in_button);
        userSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button playOfflineButton = (Button) findViewById(R.id.button_play_offline);
        playOfflineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, R.string.join_game, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, PlacementActivity.class);
                intent.putExtra(getString(R.string.extra_offline), true);
                intent.putExtra(getString(R.string.extra_initiator), true);
                startActivity(intent);
            }
        });

        Utility.refreshEventBus(this);

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        if (restService == null) {
            Intent intent2 = new Intent(this, RestService.class);
            bindService(intent2, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    @Subscribe
    public void onEvent(RestEvent restEvent) {

        Event event = restEvent.getEvent();
        int responseCode;

        switch (event) {
            case LOGIN:
                if (restEvent.isWsConnected()) {
                    responseCode = restEvent.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {//200
                        showProgress(false);
                        Intent intent = new Intent(this, LobbyActivity.class);
                        intent.putExtra(getString(R.string.extra_restService), restService);
                        intent.putExtra(getString(R.string.extra_client), restEvent.getClient());
                        startActivity(intent);

                    } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {//403
                        Log.d(DEBUGLOG_TAG, "Authentication with Rest Service failed.");
                        showProgress(false);
                        Toast.makeText(this, R.string.error_authentification_with_rest_service, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(DEBUGLOG_TAG, "Authentication with Rest Service failed");
                        showProgress(false);
                        Toast.makeText(this, R.string.error_authentification_with_rest_service, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showProgress(false);
                    Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
                }
                break;
        }

        Utility.refreshEventBus(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        showProgress(true);

        if (restService != null) {
//            client = new Client(username, Utility.hash(password));
//            restService.loginClient(client);
            restService.loginClient(username, password);
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
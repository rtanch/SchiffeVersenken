package de.hhn.aib3.aufg3.gruppe11.game.gui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GameState;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateEvent;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * User interface for game creation.
 * Holds text input views to set game name and description
 * as well as a button to confirm and save entries.
 */
public class ConfigGameActivity extends AppCompatActivity {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-CGA";

    private RestService restService = null;
    private Game game = null;

    private View configView;
    private View progressView;
    private EditText gameNameEditText;
    private EditText gameDescriptionEditText;

    private UpdateService myService = null;
    private boolean isBound = false;

    private final ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            UpdateService.MyLocalBinder binder = (UpdateService.MyLocalBinder) service;
            myService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Subscribe
    public void onEvent(RestEvent restEvent) {
        switch (restEvent.getEvent()) {
            case NEW:
                if (restEvent.isWsConnected()) {
                    if (restEvent.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        game = restEvent.getGame();
                        showProgress(true);
                        if (myService != null) {
                            myService.start(this);
                        }
                    } else if (restEvent.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_SHORT).show();
                        Utility.backToLogin(this);
                    } else {
                        Toast.makeText(this, R.string.error_create_new_game, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    Utility.backToLogin(this);
                }
                break;

            case STATE:
                if (restEvent.isWsConnected()) {
                    if (restEvent.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        if (restEvent.getGame().getGameState().equals(GameState.PLACEMENT.toString())) {
                            Log.d(DEBUGLOG_TAG, "All players have joined the game. Start Placement Activity.");
                            showProgress(false);
                            Intent intent = new Intent(ConfigGameActivity.this, PlacementActivity.class);
                            intent.putExtra(getString(R.string.extra_restService), restService);
                            intent.putExtra(getString(R.string.extra_game), restEvent.getGame());
                            intent.putExtra(getString(R.string.extra_initiator), true);
                            startActivity(intent);
                            finish();

                        } else if (restEvent.getGame().getGameState().equals(GameState.STARTED.toString())) {
                            Log.d(DEBUGLOG_TAG, "Waiting for other player to join the game");
                        } else {
                            Log.d(DEBUGLOG_TAG, "unexpected game state");
                        }
                    } else if (restEvent.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_SHORT).show();
                        Utility.backToLogin(this);
                    } else {
                        Toast.makeText(this, R.string.error_create_new_game, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    Utility.backToLogin(this);
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if (updateEvent.getContext() instanceof ConfigGameActivity) {
            if (game != null) {
                restService.getGameState(game.getId());
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_game);

        Utility.refreshEventBus(this);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));

        configView = findViewById(R.id.layout_config);
        progressView = findViewById(R.id.config_game_progress);
        gameNameEditText = (EditText) findViewById(R.id.edit_text_game_name);
        gameDescriptionEditText = (EditText) findViewById(R.id.edit_text_game_description);

        Button createGameButton = (Button) findViewById(R.id.button_create_game);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!gameNameEditText.getText().toString().isEmpty()) {
                    String gameName = gameNameEditText.getText().toString();
                    String gameDescription = gameDescriptionEditText.getText().toString();
                    if (restService != null) {
//                        Game game = new Game(editTextName.getText().toString(), editTextDescription.getText().toString());
//                        restService.createGame(game);
                        restService.createGame(gameName, gameDescription);
                    } else {
                        Log.d(DEBUGLOG_TAG, "RestService null");
                    }
                } else {
                    Toast.makeText(ConfigGameActivity.this, R.string.input_game_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.config_game_progress_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound) {
                    unbindService(myConnection);
                }
                finish();
            }
        });

        if (!isBound) {
            Intent serviceIntent = new Intent(this, UpdateService.class);
            bindService(serviceIntent, myConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isBound) {
            unbindService(myConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Shows the progress UI and hides the Game Configuration form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        configView.setVisibility(show ? View.GONE : View.VISIBLE);
        configView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                configView.setVisibility(show ? View.GONE : View.VISIBLE);
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

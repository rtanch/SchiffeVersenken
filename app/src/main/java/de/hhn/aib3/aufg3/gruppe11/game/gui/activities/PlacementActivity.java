package de.hhn.aib3.aufg3.gruppe11.game.gui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GameState;
import de.hhn.aib3.aufg3.gruppe11.game.gui.fragments.GameBoardFragment;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateEvent;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * User Interface for Ship Placement.
 * The Placement consists of one preset game board and a button to confirm the final
 * position of the ships.
 */
public class PlacementActivity extends AppCompatActivity{

    private static final String DEBUGLOG_TAG = "DEBUGLOG-PA";

    private RestService restService = null;
    private Game game = null;
    private boolean initiator = false;
    private boolean offlineMode = false;

    private View progressView;
    private View placementView;

    private UpdateService myService = null;
    private boolean isBound = false;

    private final ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            UpdateService.MyLocalBinder binder = (UpdateService.MyLocalBinder) service;
            myService = binder.getService();
            if(myService == null){
                Log.d(DEBUGLOG_TAG, "myService null myConnection");
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        placementView = findViewById(R.id.layout_placement);
        progressView = findViewById(R.id.placement_progress);

        final Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));
        game = (Game) intent.getSerializableExtra(getString(R.string.extra_game));
        initiator = intent.getBooleanExtra(getString(R.string.extra_initiator), false);

        //TODO: offline impl
        offlineMode = intent.getBooleanExtra("offlineMode", false);

        if(offlineMode){
            TextView playerTag = (TextView) findViewById(R.id.textview_offline_player_tag);
            playerTag.setVisibility(View.VISIBLE);
            if(initiator){
                playerTag.setText("Player 1");
            }else{
                playerTag.setText("Player 2");
            }
        }

        final GameBoardFragment gameBoardFragment = new GameBoardFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.extra_restService), restService);
        bundle.putSerializable(getString(R.string.extra_game), game);
        bundle.putBoolean(getString(R.string.extra_shipPreset), true);
        gameBoardFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.activity_placement_board_table_wrapper, gameBoardFragment);
        fragmentTransaction.commit();


        Button confirmButton = (Button) findViewById(R.id.activity_placement_confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlacementActivity.this);
                alertDialog.setTitle(R.string.confirm_ship_placement);
                alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //TODO: offline impl
                        if(offlineMode){
                            if(initiator){
                                game = new Game("Offline Game", "");
                                game.setInitiator(getString(R.string.offline_player1_tag));
                                game.setBoardP1(gameBoardFragment.getGameGrid());
                                finish();
                                Intent intent = new Intent(getIntent());
                                intent.putExtra(getString(R.string.extra_offline), true);
                                intent.putExtra(getString(R.string.extra_initiator), false);
                                intent.putExtra(getString(R.string.extra_game), game);
                                startActivity(intent);
                            }else{
                                game.setPlayer2(getString(R.string.offline_player2_tag));
                                game.setBoardP2(gameBoardFragment.getGameGrid());
                                game.setGameState(GameState.PLAYING.toString());
                                finish();
                                Intent intent = new Intent(PlacementActivity.this, GameActivity.class);
                                intent.putExtra(getString(R.string.extra_offline), true);
                                intent.putExtra(getString(R.string.extra_initiator), true);
                                intent.putExtra(getString(R.string.extra_game), game);
                                startActivity(intent);
                            }
                        } else {
                            showProgress(true);
                            if (game != null && restService != null) {
                                if (initiator) {
                                    game.setBoardP1(gameBoardFragment.getGameGrid());
                                } else {
                                    game.setBoardP2(gameBoardFragment.getGameGrid());
                                }
                                restService.setShipPlacement(game);
                            } else {
                                Log.d(DEBUGLOG_TAG, "Game/Rest-Service null");
                            }
                        }
                    }
                });
                alertDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        FloatingActionButton helpButton = (FloatingActionButton) findViewById(R.id.placement_help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlacementActivity.this);
                alertDialog.setTitle(R.string.help);
                alertDialog.setMessage(R.string.help_message);
                alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        Utility.refreshEventBus(this);

        Button cancelButton = (Button)findViewById(R.id.placement_progress_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBound){
                    unbindService(myConnection);
                }
                finish();
            }
        });

        if(!offlineMode){
            Log.d(DEBUGLOG_TAG, "Activity created - bind service");
            Intent serviceIntent = new Intent(this, UpdateService.class);
            bindService(serviceIntent, myConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(RestEvent restEvent) {
        Event event = restEvent.getEvent();
        int responseCode = restEvent.getResponseCode();

        switch(event){
            case PLACEMENT:
                if(restEvent.isWsConnected()){
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        Log.d(DEBUGLOG_TAG, "PLACEMENT received HTTP OK");
                        myService.start(this);
                    }else if(responseCode == HttpURLConnection.HTTP_FORBIDDEN){
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    }else{
                        Toast.makeText(this, R.string.failed_ship_placement, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;

            case STATE:
                if(restEvent.isWsConnected()){
                    if(restEvent.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        if (restEvent.getGame().getGameState().equals(GameState.PLAYING.toString())) {
                            if(isBound){
                                unbindService(myConnection);
                            }
                            Log.d(DEBUGLOG_TAG, "All players have set their ships. Start Game Activity.");
                            Intent intent1 = new Intent(PlacementActivity.this, GameActivity.class);
                            intent1.putExtra(getString(R.string.extra_restService), restService);
                            intent1.putExtra(getString(R.string.extra_game), restEvent.getGame());
                            intent1.putExtra(getString(R.string.extra_initiator), initiator);
                            showProgress(false);
                            startActivity(intent1);
                            finish();
                        } else if (restEvent.getGame().getGameState().equals(GameState.PLACEMENT.toString())) {
                            Log.d(DEBUGLOG_TAG, "Waiting for other player to place ships");
                        } else {
                            Log.d(DEBUGLOG_TAG, "unexpected game state");
                        }
                    }else if(responseCode == HttpURLConnection.HTTP_FORBIDDEN){
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    }else{
                        Toast.makeText(this, R.string.failed_ship_placement, Toast.LENGTH_SHORT).show();
                        Utility.backToLogin(this);
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if(updateEvent.getContext() instanceof PlacementActivity){
            if(game != null){
                restService.getGameState(game.getId());
            }
        }
    }

    /**
     * Shows the progress UI and hides the placement form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        placementView.setVisibility(show ? View.GONE : View.VISIBLE);
        placementView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                placementView.setVisibility(show ? View.GONE : View.VISIBLE);
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
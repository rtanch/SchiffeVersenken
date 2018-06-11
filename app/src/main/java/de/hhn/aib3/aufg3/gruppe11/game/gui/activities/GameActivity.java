package de.hhn.aib3.aufg3.gruppe11.game.gui.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hhn.aib3.aufg3.gruppe11.Exceptions.InvalidFiringPosition;
import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.authentication.LoginActivity;
import de.hhn.aib3.aufg3.gruppe11.game.OfflineGameHandler;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Cell;
import de.hhn.aib3.aufg3.gruppe11.game.elements.placement.EmptyPreset;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GEvent;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GameState;
import de.hhn.aib3.aufg3.gruppe11.game.enums.NextMove;
import de.hhn.aib3.aufg3.gruppe11.game.gui.fragments.GameBoardFragment;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.GameEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.GeneralEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.OfflineEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateEvent;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * User Interface for a Game.
 * Each Game has two game boards.
 * The client may switch between boards via tabs.
 * The interface provides controls for firing or quitting a game
 * as well as active player indication
 */
public class GameActivity extends AppCompatActivity {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-GA";

    private RestService restService = null;
    private Game game = null;
    private boolean initiator;

    private UpdateService myService = null;
    private boolean isBound = false;

//    private static final String Utility.INITIATOR_TAB_TAG = "INITIATOR";
//    private static final String Utility.PLAYER2_TAB_TAG = "PLAYER2";

    private TextView namePlayer;
    private TextView nameOpponent;

    //TODO: Offline Mode
    private boolean offlineMode = false;
    private OfflineGameHandler offlineGameHandler = null;

    private final ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            UpdateService.MyLocalBinder binder = (UpdateService.MyLocalBinder) service;
            myService = binder.getService();
            if (myService == null) {
                Log.d(DEBUGLOG_TAG, "myService null myConnection");
            }
            isBound = true;
            myService.start(1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));
        game = (Game) intent.getSerializableExtra(getString(R.string.extra_game));
        initiator = intent.getBooleanExtra(getString(R.string.extra_initiator), false);

        //TODO: Offline Mode
        offlineMode = intent.getBooleanExtra("offlineMode", false);

        if(offlineMode){

//            try{
//                Thread.sleep(5000);
//            }catch(InterruptedException e){
//                Log.d(DEBUGLOG_TAG, e.getMessage());
//            }

            LinearLayout tabBar = (LinearLayout) findViewById(R.id.tab_bar_game);

            final FragmentManager fragmentManager = getSupportFragmentManager();

            final FragmentTabHost tabHost = new FragmentTabHost(this);

            tabHost.setup(this, fragmentManager, R.id.fragment_game_board);

            final Game offlineMatch = new Game("", "");
            offlineMatch.setBoardP1(new EmptyPreset().toByteArray());
            offlineMatch.setBoardP2(new EmptyPreset().toByteArray());

            final Bundle bundleInitiator = new Bundle();
            bundleInitiator.putSerializable(getString(R.string.extra_game), offlineMatch);
//            bundleInitiator.putSerializable(getString(R.string.extra_offline_game_result), game);
            bundleInitiator.putBoolean(getString(R.string.extra_initiator), true);
            bundleInitiator.putBoolean(getString(R.string.extra_initiatorField), true);
            bundleInitiator.putBoolean(getString(R.string.extra_offline), true);
            bundleInitiator.putBoolean("test", true);

            Bundle bundlePlayer2 = new Bundle();
            bundlePlayer2.putSerializable(getString(R.string.extra_game), offlineMatch);
//            bundlePlayer2.putSerializable(getString(R.string.extra_offline_game_result), game);
            bundlePlayer2.putBoolean(getString(R.string.extra_initiator), false);
            bundlePlayer2.putBoolean(getString(R.string.extra_initiatorField), false);
            bundlePlayer2.putBoolean(getString(R.string.extra_offline), true);
            bundlePlayer2.putBoolean("test", false);

            namePlayer = (TextView) findViewById(R.id.fragment_game_client_text_view);
            nameOpponent = (TextView) findViewById(R.id.fragment_game_opponent_text_view);

            if(tabHost.getCurrentTabTag() != Utility.INITIATOR_TAB_TAG){
                namePlayer.setTextColor(Color.BLACK);
                nameOpponent.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            }else {
                namePlayer.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                nameOpponent.setTextColor(Color.BLACK);
            }

            tabHost.addTab(tabHost.newTabSpec(Utility.INITIATOR_TAB_TAG).setIndicator(game.getInitiator()),
                    GameBoardFragment.class, bundleInitiator);
            tabHost.addTab(tabHost.newTabSpec(Utility.PLAYER2_TAB_TAG).setIndicator(game.getPlayer2()),
                    GameBoardFragment.class, bundlePlayer2);

            tabHost.getTabWidget().setEnabled(false);

            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String s) {

                    if(tabHost.getCurrentTabTag() == Utility.INITIATOR_TAB_TAG){
                        namePlayer.setTextColor(Color.BLACK);
                        nameOpponent.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    }else {
                        namePlayer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        nameOpponent.setTextColor(Color.BLACK);
                    }
                }
            });

            tabBar.addView(tabHost);

            namePlayer.setText(game.getInitiator());
            nameOpponent.setText(game.getPlayer2());

            offlineGameHandler = new OfflineGameHandler();

            FloatingActionButton firingButton = (FloatingActionButton) findViewById(R.id.fragment_game_save_button);
            firingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //TODO: Here lies the problem
                    GameBoardFragment gameBoardFragment = (GameBoardFragment) fragmentManager.findFragmentByTag(tabHost.getCurrentTabTag());

                    if(gameBoardFragment.getSelectedField() != null){
                            try{
                                Game testGame = new Game("", "");
                                byte[] currentBoardP1 = offlineMatch.getBoardP1();
                                if(currentBoardP1 == null){
                                    currentBoardP1 = new EmptyPreset().toByteArray();
                                }
                                byte[] currentBoardP2 = offlineMatch.getBoardP2();
                                if(currentBoardP2 == null){
                                    currentBoardP2 = new EmptyPreset().toByteArray();
                                }
                                testGame.setBoardP1(currentBoardP1);
                                testGame.setBoardP2(currentBoardP2);

                                String nextMove = offlineGameHandler.getNextMove(game, gameBoardFragment.getGameGrid(), gameBoardFragment.getSelectedField(), offlineMatch.getNextMove());

                                byte[] finishRes = new EmptyPreset().toByteArray();
                                byte[] finishOfflineMatch = new EmptyPreset().toByteArray();
                                String winner = null;

                                if(tabHost.getCurrentTabTag().equals(Utility.INITIATOR_TAB_TAG)){
                                    offlineMatch.setBoardP1(offlineGameHandler.fireShot(game.getBoardP1(), testGame.getBoardP1(), gameBoardFragment.getSelectedField(), getApplicationContext()));
                                    EventBus.getDefault().post(new OfflineEvent(Event.STATE, offlineMatch, offlineMatch.getBoardP1()));

                                    finishRes = game.getBoardP1();
                                    finishOfflineMatch = offlineMatch.getBoardP1();
                                    winner = "Player 2";
                                }else if(tabHost.getCurrentTabTag().equals(Utility.PLAYER2_TAB_TAG)){
                                    offlineMatch.setBoardP2(offlineGameHandler.fireShot(game.getBoardP2(), testGame.getBoardP2(), gameBoardFragment.getSelectedField(), getApplicationContext()));
                                    EventBus.getDefault().post(new OfflineEvent(Event.STATE, offlineMatch, offlineMatch.getBoardP2()));

                                    finishRes = game.getBoardP2();
                                    finishOfflineMatch = offlineMatch.getBoardP2();
                                    winner = "Player 1";
                                }

                                Utility.detectHit(offlineMatch, testGame, getApplicationContext());

                                //TODO: Game finished? if so to what has to be done right here
                                if(offlineGameHandler.isGameFinished(finishRes, finishOfflineMatch)){
                                    String msg = "You Win";
                                    Utility.play(R.raw.win, GameActivity.this);
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameActivity.this);
                                    alertDialog.setTitle(msg + " " + winner);
                                    alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            EventBus.getDefault().unregister(this);
                                            GameActivity.this.finish();
                                        }
                                    });
                                    alertDialog.show();
                                }

                                if(nextMove != offlineMatch.getNextMove()){
                                    if(nextMove.equals(NextMove.INITIATOR.toString())){
                                        tabHost.setCurrentTabByTag(Utility.INITIATOR_TAB_TAG);
                                    }else if(nextMove.equals(NextMove.P2.toString())){
                                        tabHost.setCurrentTabByTag(Utility.PLAYER2_TAB_TAG);
                                    }
                                }
                                offlineMatch.setNextmove(nextMove);


                            }catch(InvalidFiringPosition e){
                                Toast.makeText(GameActivity.this, "Cannot shoot here", Toast.LENGTH_LONG).show();
                            }
                    }else{
                        Toast.makeText(GameActivity.this, "Cannot shoot here", Toast.LENGTH_LONG).show();
                    }
                }
            });


            //                                String currentMove = null;
//                                if(offlineMatch.getNextMove() == null){
//                                    currentMove = NextMove.INITIATOR.toString();
//                                }else if(offlineMatch.getNextMove().equals(NextMove.INITIATOR)){
//                                    currentMove = NextMove.P2.toString();
//                                }else if(offlineMatch.getNextMove().equals(NextMove.P2)){
//                                    currentMove = NextMove.INITIATOR.toString();
//                                }

            //TODO: you might want to do this in fragment board and update the board after offlineGameHandler fireShot

//            FloatingActionButton firingButton = (FloatingActionButton) findViewById(R.id.fragment_game_save_button);
////            firingButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
////            firingButton.setClickable(false);
//            firingButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.d(DEBUGLOG_TAG, "0");
//                    GameBoardFragment gfrag = (GameBoardFragment) fragmentManager.findFragmentByTag(Utility.INITIATOR_TAB_TAG);
//                    if(gfrag.getSelectedField() != null){
//                        Log.d(DEBUGLOG_TAG, "1");
//                        OfflineGameHandler offlineGameHandler = new OfflineGameHandler();
//                        if(tabHost.getCurrentTabTag() == Utility.INITIATOR_TAB_TAG){
//
//                            Log.d(DEBUGLOG_TAG, "2");
//                            try{
//                                offlineMatch.setBoardP1(offlineGameHandler.fireShot(game.getBoardP1(), gfrag.getGameGrid(), gfrag.getSelectedField()));;
//                                gfrag.updateCurrentGameBoard(offlineMatch);
//                            }catch(InvalidFiringPosition e){
//                                Toast.makeText(GameActivity.this, "Cannot shoot here", Toast.LENGTH_LONG).show();
//                            }
//                        }else if(tabHost.getCurrentTabTag() == Utility.PLAYER2_TAB_TAG){
//                            Log.d(DEBUGLOG_TAG, "3");
//                            try{
//                                offlineMatch.setBoardP2(offlineGameHandler.fireShot(game.getBoardP2(), gfrag.getGameGrid(), gfrag.getSelectedField()));
//                                gfrag.updateCurrentGameBoard(offlineMatch);
//                            }catch(InvalidFiringPosition e){
//                                Toast.makeText(GameActivity.this, "Cannot shoot here", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    }else{
//                        Toast.makeText(GameActivity.this, "Cannot shoot here", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });




//            final FloatingActionButton firingButton = (FloatingActionButton) findViewById(R.id.fragment_game_save_button);
//            firingButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    if(tabHost.getCurrentTabTag() == Utility.INITIATOR_TAB_TAG){
//                        tabHost.setCurrentTabByTag(Utility.PLAYER2_TAB_TAG);
//                        namePlayer.setTextColor(Color.BLACK);
//                        nameOpponent.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.colorAccent));
//
//                        offlineMatch.setNextmove(NextMove.P2.toString());
//                        EventBus.getDefault().post(new OfflineEvent(Event.STATE, offlineMatch));
//
//                        GameBoardFragment gfrag = (GameBoardFragment) fragmentManager.findFragmentByTag(Utility.INITIATOR_TAB_TAG);
//                        Cell cell  = gfrag.getSelectedField();
//                        Log.d(DEBUGLOG_TAG, "Selected Cell from GameActivity: \nCollumn: " + cell.getColumn() + "\nRow: " + cell.getRow());
//                    }else{
//                        tabHost.setCurrentTabByTag(Utility.INITIATOR_TAB_TAG);
//                        namePlayer.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.colorAccent));
//                        nameOpponent.setTextColor(Color.BLACK);
//
//                        offlineMatch.setNextmove(NextMove.INITIATOR.toString());
////                        offlineMatch.setWinner(NextMove.INITIATOR.toString());
//                        EventBus.getDefault().post(new OfflineEvent(Event.STATE, offlineMatch));
//                    }
//                }
//            });

        }else {
            LinearLayout tabBar = (LinearLayout) findViewById(R.id.tab_bar_game);

            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTabHost tabHost = new FragmentTabHost(this);
            tabHost.setup(this, fragmentManager, R.id.fragment_game_board);

            final GameBoardFragment gameBoard = new GameBoardFragment();

            Bundle bundleInitiator = new Bundle();
            bundleInitiator.putSerializable(getString(R.string.extra_restService), restService);
            bundleInitiator.putSerializable(getString(R.string.extra_game), game);
            bundleInitiator.putBoolean(getString(R.string.extra_initiator), initiator);
            bundleInitiator.putBoolean(getString(R.string.extra_initiatorField), true);

            Bundle bundlePlayer2 = new Bundle();
            bundlePlayer2.putSerializable(getString(R.string.extra_restService), restService);
            bundlePlayer2.putSerializable(getString(R.string.extra_game), game);
            bundlePlayer2.putBoolean(getString(R.string.extra_initiator), initiator);
            bundlePlayer2.putBoolean(getString(R.string.extra_initiatorField), false);

            TextView namePlayer = (TextView) findViewById(R.id.fragment_game_client_text_view);
            TextView nameOpponent = (TextView) findViewById(R.id.fragment_game_opponent_text_view);

            if (initiator) {
                tabHost.addTab(tabHost.newTabSpec(Utility.INITIATOR_TAB_TAG).setIndicator(game.getInitiator()),
                        gameBoard.getClass(), bundleInitiator);
                tabHost.addTab(tabHost.newTabSpec(Utility.PLAYER2_TAB_TAG).setIndicator(game.getPlayer2()),
                        gameBoard.getClass(), bundlePlayer2);
                namePlayer.setText(game.getInitiator());
                nameOpponent.setText(game.getPlayer2());
            } else {
                tabHost.addTab(tabHost.newTabSpec(Utility.PLAYER2_TAB_TAG).setIndicator(game.getPlayer2()),
                        gameBoard.getClass(), bundlePlayer2);
                tabHost.addTab(tabHost.newTabSpec(Utility.INITIATOR_TAB_TAG).setIndicator(game.getInitiator()),
                        gameBoard.getClass(), bundleInitiator);
                namePlayer.setText(game.getPlayer2());
                nameOpponent.setText(game.getInitiator());
            }

            tabBar.addView(tabHost);

            final FloatingActionButton firingButton = (FloatingActionButton) findViewById(R.id.fragment_game_save_button);
            firingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new GameEvent(GEvent.SHOOT));
                }
            });
            firingButton.setVisibility(View.INVISIBLE);

            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabName) {
                    if ((tabName.equals(Utility.INITIATOR_TAB_TAG) && initiator) || (tabName.equals(Utility.PLAYER2_TAB_TAG) && !initiator)) {
                        firingButton.setVisibility(View.INVISIBLE);
                    } else if ((tabName.equals(Utility.INITIATOR_TAB_TAG) && !initiator) || (tabName.equals(Utility.PLAYER2_TAB_TAG) && initiator)) {
                        firingButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            Utility.refreshEventBus(this);

            if (!isBound) {
                Intent serviceIntent = new Intent(this, UpdateService.class);
                bindService(serviceIntent, myConnection, BIND_AUTO_CREATE);
            }
        }

        FloatingActionButton surrenderButton = (FloatingActionButton) findViewById(R.id.fragment_game_surrender_button);
        surrenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameActivity.this);
                alertDialog.setTitle(R.string.rage_quit);
                alertDialog.setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isBound) {
                            unbindService(myConnection);
                        }
                        finish();
                    }
                });

                alertDialog.setNegativeButton(R.string.go_on, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isBound) {
            unbindService(myConnection);
        }
    }

    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if (game != null) {
            restService.getGameState(game.getId());
        }
        Utility.refreshEventBus(this);
    }

}



//                                if(offlineMatch.getNextMove().equals(NextMove.INITIATOR.toString())){
//                                    tabHost.setCurrentTabByTag(Utility.PLAYER2_TAB_TAG);
//                                    offlineMatch.setBoardP2(offlineGameHandler.fireShot(game.getBoardP2(), gameBoardFragment.getGameGrid(), gameBoardFragment.getSelectedField()));
//                                }else if(offlineMatch.getNextMove().equals(NextMove.P2.toString())){
//                                    tabHost.setCurrentTabByTag(Utility.INITIATOR_TAB_TAG);
//                                    offlineMatch.setBoardP1(offlineGameHandler.fireShot(game.getBoardP1(), gameBoardFragment.getGameGrid(), gameBoardFragment.getSelectedField()));
//                                }


//                                String nextMove = offlineGameHandler.getNextMove(game, gameBoardFragment.getGameGrid(), gameBoardFragment.getSelectedField(), offlineMatch.getNextMove());
//                                if(nextMove != offlineMatch.getNextMove()){
//                                    if(nextMove.equals(NextMove.INITIATOR.toString())){
//                                        tabHost.setCurrentTabByTag(Utility.INITIATOR_TAB_TAG);
//                                    }else if(nextMove.equals(NextMove.P2.toString())){
//                                        tabHost.setCurrentTabByTag(Utility.PLAYER2_TAB_TAG);
//                                    }
//                                }
//
//                                offlineMatch.setNextmove(nextMove);
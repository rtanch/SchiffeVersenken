package de.hhn.aib3.aufg3.gruppe11.lobby;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.authentication.ConfigAccountActivity;
import de.hhn.aib3.aufg3.gruppe11.game.gui.activities.ConfigGameActivity;
import de.hhn.aib3.aufg3.gruppe11.game.gui.activities.PlacementActivity;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.authentication.Client;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateEvent;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateService;
import de.hhn.aib3.aufg3.gruppe11.utility.UpdateService.MyLocalBinder;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * GUI for creating new or selecting open game sessions
 */
public class LobbyActivity extends AppCompatActivity {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-LobbyA";

    private RestService restService = null;
    private Client client = null;
    private ListView listView;

    private UpdateService myService = null;
    private boolean isBound = false;


    private final ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyLocalBinder binder = (MyLocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.start(LobbyActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));
        client = (Client) intent.getSerializableExtra(getString(R.string.extra_client));

        Log.d(DEBUGLOG_TAG, "Activity created - bind service");
        Intent serviceIntent = new Intent(this, UpdateService.class);
        bindService(serviceIntent, myConnection, BIND_AUTO_CREATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_lobby_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, ConfigGameActivity.class);
                intent.putExtra(getString(R.string.extra_restService), restService);
                startActivity(intent);
            }
        });

        restService.getGames();

        listView = (ListView) findViewById(R.id.list_view_lobby);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(DEBUGLOG_TAG, "List View item selected");
//                restService.joinGame((Game)adapterView.getItemAtPosition(position));
                Game game = (Game) adapterView.getItemAtPosition(position);
                restService.joinGame(game.getId());

            }
        });

        Utility.refreshEventBus(this);

    }

    @Override
    protected void onDestroy() {
        Log.d(DEBUGLOG_TAG, "Activity destroyed - unbind Service");
        super.onDestroy();
        if (isBound) {
            unbindService(myConnection);
            myService = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUGLOG_TAG, "Lobby activity - resume");
        if (myService != null) {
            myService.start(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUGLOG_TAG, "Lobby activity - pause");
        myService.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {

            case R.id.menu_logout:
                Log.d(DEBUGLOG_TAG, "selected menu item: Logout");
                client = null;
                finish();
                return true;

            case R.id.menu_config_account:
                Log.d(DEBUGLOG_TAG, "selected menu item: Config Account");
                intent = new Intent(this, ConfigAccountActivity.class);
                intent.putExtra(getString(R.string.extra_restService), restService);
                intent.putExtra(getString(R.string.extra_client), client);
                startActivity(intent);
                return true;

            case R.id.menu_refresh:
                Log.d(DEBUGLOG_TAG, "selected menu item: Setting");
                if (restService != null) {
                    restService.getGames();
                }
                return true;

            default:
                Log.d(DEBUGLOG_TAG, "(selected menu item) Unexpected Condition");
                return super.onOptionsItemSelected(item);

        }
    }

    @Subscribe
    public void onEvent(RestEvent restEvent) {
        Event event = restEvent.getEvent();
        int responseCode = restEvent.getResponseCode();

        switch (event) {
            case GAMES:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(DEBUGLOG_TAG, "GAMES event received");
                        LobbyAdapter adapter = new LobbyAdapter(this, restEvent.getGames());
                        listView.setAdapter(adapter);
                    } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;

            case JOIN:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, R.string.join_game, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, PlacementActivity.class);
                        intent.putExtra(getString(R.string.extra_restService), restService);
                        intent.putExtra(getString(R.string.extra_game), restEvent.getGame());
                        startActivity(intent);
                    } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    } else {
                        Toast.makeText(this, R.string.error_join_game, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;

            case UPDATE:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        client = restEvent.getClient();
                    }
                }
                break;
        }
        Utility.refreshEventBus(this);
    }

    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if (updateEvent.getContext() instanceof LobbyActivity) {
            restService.getGames();
        }
    }

}
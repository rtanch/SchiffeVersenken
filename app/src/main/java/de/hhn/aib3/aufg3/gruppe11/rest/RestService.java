package de.hhn.aib3.aufg3.gruppe11.rest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.authentication.Client;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handles all connections between Client and Rest-Webservice
 */
public class RestService extends Service implements Serializable {

    private final IBinder iBinder = new LocalService();

    private final static String BASE_URL = "https://swlab.iap.hs-heilbronn.de/ex3/api/v0.1/";
    private static SWLabWSClient SERVICE = null;
    private static String JWT_TOKEN = null;

    private static final String DEBUGLOG_TAG = "DEBUGLOG-RS";

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public class LocalService extends Binder implements Serializable {

        public RestService getService() {
            return RestService.this;
        }
    }


    /**
     * Initializes Retrofit using BASE_URL. Automatically converts transmitted Object to JSON
     * using GsonConverterFactory
     */
    private void initRestService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SERVICE = retrofit.create(SWLabWSClient.class);
    }


    /**
     * Registers new Client/Client with Rest-Webservice
     *
     * @param username name which will later identify the client to other users
     * @param password plain text password
     */
    public void registerClient(@NonNull String username, @NonNull String password) {
        Log.d(DEBUGLOG_TAG, "registerClient() called");
        if (SERVICE == null) {
            Log.d(DEBUGLOG_TAG, "No custom URL set. Using BASE_URL");
            initRestService();
        }

        Client client = new Client(username, Utility.hash(password));

        Call<ResponseBody> call = SERVICE.createUser(client);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                printRestResponse(Event.REGISTER, response);
                EventBus.getDefault().post(new RestEvent(Event.REGISTER, response.code()));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in RegisterClient: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.REGISTER, false));
            }
        });

    }


    /**
     * Login user/client
     * Receives ID and JWT-Token form Rest-Webservice which will be used to authenticate
     * the client in future transactions with the server
     *
     * @param username name which will later identify the client to other users
     * @param password plain text password
     */
    public void loginClient(@NonNull String username, @NonNull String password) {
        Log.d(DEBUGLOG_TAG, "loginClient() called");
        if (SERVICE == null) {
            Log.d(DEBUGLOG_TAG, "No custom URL set. Using BASE_URL");
            initRestService();
        }

        final Client client = new Client(username, Utility.hash(password));
        Call<Client> call = SERVICE.loginUser(client);
        call.enqueue(new Callback<Client>() {
            @Override
            public void onResponse(@NonNull Call<Client> call, @NonNull Response<Client> response) {
                printRestResponse(Event.LOGIN, response);
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Headers headers = response.headers();
                    JWT_TOKEN = headers.get(getString(R.string.rest_authorization));
                    client.setId(response.body().getId());
                }
                EventBus.getDefault().post(new RestEvent(Event.LOGIN, response.code(), client));
            }

            @Override
            public void onFailure(@NonNull Call<Client> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in LoginClient: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.LOGIN, false));
            }
        });
    }


    /**
     * Sends current username and password to Rest-Webservice
     */
    public void updateClientData(@NonNull final Client client) {
        Log.d(DEBUGLOG_TAG, "updateClientData() called");
        Call<ResponseBody> call = SERVICE.updateUser(client, client.getId(), JWT_TOKEN);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                printRestResponse(Event.UPDATE, response);
                EventBus.getDefault().post(new RestEvent(Event.UPDATE, response.code(), client));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                EventBus.getDefault().post(new RestEvent(Event.UPDATE, false));
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
            }
        });
    }

    /**
     * Deletes client on Server with corresponding identifier
     *
     * @param clintId String provided by Rest Webservice used to identify the client
     */
    public void deleteClientAccount(@NonNull String clintId) {
        Call<ResponseBody> call = SERVICE.deleteUser(clintId, JWT_TOKEN);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                printRestResponse(Event.DELETE, response);
                EventBus.getDefault().post(new RestEvent(Event.DELETE, response.code()));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.DELETE, false));
            }
        });
    }

    /**
     * Creates a new game on the Server
     *
     * @param gameName        name which will identify the game to other users
     * @param gameDescription optional description for more information about the game
     */
    public void createGame(@NonNull String gameName, @NonNull String gameDescription) {

        Game game = new Game(gameName, gameDescription);

        Call<Game> call = SERVICE.createGame(game, JWT_TOKEN);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(@NonNull Call<Game> call, @NonNull Response<Game> response) {
                printRestResponse(Event.NEW, response);
                EventBus.getDefault().post(new RestEvent(Event.NEW, response.code(), response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<Game> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.NEW, false));
            }
        });
    }


    /**
     * List of all STARTED games on Server
     */
    public void getGames() {
        Call<List<Game>> call = SERVICE.getAllGames(JWT_TOKEN);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(@NonNull Call<List<Game>> call, @NonNull Response<List<Game>> response) {
                printRestResponse(Event.GAMES, response);
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    ArrayList<Game> gameList = (ArrayList<Game>) response.body();
                    for (int i = 0; i < gameList.size(); i++) {
                        Log.d(DEBUGLOG_TAG, "Game\nName: "
                                + gameList.get(i).getName() + "\nDescription: "
                                + gameList.get(i).getDescription() + "\nInitiator: "
                                + gameList.get(i).getInitiator());
                    }
                    EventBus.getDefault().post(new RestEvent(Event.GAMES, response.code(), gameList));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Game>> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in getCommunityData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.GAMES, false));
            }
        });
    }

    /**
     * Client joins game with corresponding identifier
     *
     * @param gameId String provided by Rest Webservice used to identify the game
     */
    public void joinGame(@NonNull String gameId) {

        Call<Game> call = SERVICE.joinGame(gameId, JWT_TOKEN);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(@NonNull Call<Game> call, @NonNull Response<Game> response) {
                printRestResponse(Event.JOIN, response);
                EventBus.getDefault().post(new RestEvent(Event.JOIN, response.code(), response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<Game> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.JOIN, false));
            }
        });
    }


    /**
     * State of game with corresponding identifier
     *
     * @param gameId String provided by Rest Webservice used to identify the game
     */
    public void getGameState(@NonNull String gameId) {
        Call<Game> call = SERVICE.getGameState(gameId, JWT_TOKEN);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(@NonNull Call<Game> call, @NonNull Response<Game> response) {
                printRestResponse(Event.STATE, response);
                EventBus.getDefault().post(new RestEvent(Event.STATE, response.code(), response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<Game> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.STATE, false));
            }
        });
    }


    /**
     * Sends client's ship placement positions to Rest Webservice
     *
     * @param game game in which client is currently enrolled
     */
    public void setShipPlacement(@NonNull Game game) {
        Call<Game> call = SERVICE.setShipPlacement(game.getId(), JWT_TOKEN, game);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(@NonNull Call<Game> call, @NonNull Response<Game> response) {
                printRestResponse(Event.PLACEMENT, response);
                EventBus.getDefault().post(new RestEvent(Event.PLACEMENT, response.code()));
            }

            @Override
            public void onFailure(@NonNull Call<Game> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.PLACEMENT, false));
            }
        });
    }


    /**
     * Sends the position (row / column) of where the client wants to shoot to Rest Webservice
     *
     * @param gameId String provided by Rest Webservice used to identify the game
     * @param row    valid interval 1-10
     * @param column valid interval 1-10
     */
    public void fireShot(@NonNull String gameId, int row, int column) {
        Call<Game> call = SERVICE.fireShot(gameId, String.valueOf(row), String.valueOf(column), JWT_TOKEN);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(@NonNull Call<Game> call, @NonNull Response<Game> response) {
                printRestResponse(Event.SHOOT, response);
                EventBus.getDefault().post(new RestEvent(Event.SHOOT, response.code(), response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<Game> call, @NonNull Throwable throwable) {
                Log.d(DEBUGLOG_TAG, "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.SHOOT, false));
            }
        });
    }


    /**
     * Prints human readable error message for failed server requests
     *
     * @param response Server response with response code
     *                 valid response codes:
     *                 - HTTP_OK
     *                 - HTTP_FORBIDDEN
     *                 - HTTP_NOT_MODIFIED
     *                 - HTTP_CONFLICT
     *                 - HTTP_BAD_REQUEST
     */
    private void printRestResponse(Event event, Response response) {

        switch (response.code()) {

            case HttpURLConnection.HTTP_OK:
                Log.d(DEBUGLOG_TAG, "HTTP_OK FROM EVENT " + event.toString() + "\n" + response);
                break;

            case HttpURLConnection.HTTP_FORBIDDEN:
                Log.d(DEBUGLOG_TAG, "HTTP_FORBIDDEN FROM EVENT " + event.toString() + "\n" + response);
                break;

            case HttpURLConnection.HTTP_NOT_MODIFIED:
                Log.d(DEBUGLOG_TAG, "HTTP_NOT_MODIFIED FROM EVENT " + event.toString() + "\n" + response);
                break;

            case HttpURLConnection.HTTP_CONFLICT:
                Log.d(DEBUGLOG_TAG, "HTTP_CONFLICT FROM EVENT " + event.toString() + "\n" + response);
                break;

            case HttpURLConnection.HTTP_BAD_REQUEST:
                Log.d(DEBUGLOG_TAG, "HTTP_BAD_REQUEST FROM EVENT " + event.toString() + "\n" + response);
                break;

            default:
                Log.d(DEBUGLOG_TAG, "UNEXPECTED RESPONSE FROM EVENT " + event.toString() + "\n" + response);
                break;
        }
    }

}
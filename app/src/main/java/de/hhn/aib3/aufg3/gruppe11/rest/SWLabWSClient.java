package de.hhn.aib3.aufg3.gruppe11.rest;


import java.util.List;

import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.authentication.Client;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * SWLabWSClient for Hochschule Heilbronn REST-Webservice (SLAB A3)
 */
public interface SWLabWSClient {

    @POST("user/new")
    Call<ResponseBody> createUser(@Body Client user);

    @POST("user/login")
    Call<Client> loginUser(@Body Client user);

    @POST("user/update/{bid}")
    Call<ResponseBody> updateUser(@Body Client user, @Path("bid") String bid, @Header("Authorization") String jwt);

    @DELETE("user/delete/{bid}")
    Call<ResponseBody> deleteUser(@Path("bid") String bid, @Header("Authorization") String jwt);

    @POST("game/new")
    Call<Game> createGame(@Body Game game, @Header("Authorization") String jwt);

    @GET("game/all")
    Call<List<Game>> getAllGames(@Header("Authorization") String jwt);

    @POST("game/join/{gameID}")
    Call<Game> joinGame(@Path("gameID") String gameID, @Header("Authorization") String jwt);

    @GET("game/state/{gameID}")
    Call<Game> getGameState(@Path("gameID") String gameID, @Header("Authorization") String jwt);

    @POST("game/placement/{gameID}")
    Call<Game> setShipPlacement(@Path("gameID") String gameID, @Header("Authorization") String jwt, @Body Game game);

    @POST("game/shot/{gameID}/{row}/{column}")
    Call<Game> fireShot(@Path("gameID") String gameID, @Path("row") String row, @Path("column") String column, @Header("Authorization") String jwt);

}
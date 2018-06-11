package de.hhn.aib3.aufg3.gruppe11.rest;


import java.util.ArrayList;

import de.hhn.aib3.aufg3.gruppe11.authentication.Client;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;

/**
 * POJO class for EventBus
 * - returns REST response codes and event enum as an identifier
 */
public class RestEvent implements GeneralEvent {

    private Event event;
    private int responseCode;
    private ArrayList<Game> games = null;
    private Game game = null;
    private Client client = null;
    private boolean wsConnected = true;

    public RestEvent(Event event, int responseCode, ArrayList<Game> games) {
        this.event = event;
        this.responseCode = responseCode;
        this.games = games;
    }

    public RestEvent(Event event, int responseCode, Game game) {
        this.event = event;
        this.responseCode = responseCode;
        this.game = game;
    }

    public RestEvent(Event event, int responseCode, Client client) {
        this.event = event;
        this.responseCode = responseCode;
        this.client = client;
    }

    public RestEvent(Event event, int responseCode) {
        this.event = event;
        this.responseCode = responseCode;
    }

    public RestEvent(Event event, boolean wsConnected) {
        this.event = event;
        this.wsConnected = wsConnected;
    }

    public RestEvent(Event event) {
        this.event = event;
    }


    public Event getEvent() {
        return event;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ArrayList<Game> getGames() {
        return games;
    }

    public boolean isWsConnected() {
        return wsConnected;
    }

    public Game getGame() {
        return game;
    }

    public Client getClient() {
        return client;
    }
}

package de.hhn.aib3.aufg3.gruppe11.rest;

import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;

public class OfflineEvent implements GeneralEvent {


    private Event event;
    private Game game;
    private String move;
    private byte[] board;

    public OfflineEvent(Event event, Game game) {
        this.event = event;
        this.game = game;
    }

    public OfflineEvent(Event event, Game game, String move) {
        this.event = event;
        this.game = game;
        this.move = move;
    }

    public OfflineEvent(Event event, Game game, byte[] board) {
        this.event = event;
        this.game = game;
        this.board = board;
    }

    public Event getEvent() {
        return event;
    }

    public Game getGame() {
        return game;
    }

    public String getMove(){
        return move;
    }

    public byte[] getBoard(){
        return board;
    }
}

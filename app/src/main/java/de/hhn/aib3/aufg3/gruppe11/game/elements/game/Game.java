package de.hhn.aib3.aufg3.gruppe11.game.elements.game;

import java.io.Serializable;

import de.hhn.aib3.aufg3.gruppe11.game.enums.GameState;


/**
 * Represents a Battleships Game
 */
public class Game implements Serializable{

    private String id;
    private String name;
    private String description;
    private String initiator;
    private String player2;
    private String gameState;
    private String nextMove;
    private byte[] boardP1;
    private byte[] boardP2;
    private String winner;

    public Game(String name, String description){
        this.name = name;
        this.description = description;
    }

    public Game(String id, String name, String description, String initiator){
        this.id = id;
        this.name = name;
        this.description = description;
        this.initiator = initiator;
    }

    public Game(String id, String name, String description, String initiator, String player2,
                String gameState, String nextMove, byte[] boardP1, byte[] boardP2, String winner){
        this.id = id;
        this.name = name;
        this.description = description;
        this.initiator = initiator;
        this.player2 = player2;
        this.gameState = gameState;
        this.nextMove = nextMove;
        this.boardP1 = boardP1;
        this.boardP2 = boardP2;
        this.winner = winner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator){
        this.initiator = initiator;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2){
        this.player2 = player2;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState){
        this.gameState = gameState;
    }

    public String getNextMove() {
        return nextMove;
    }

    public void setNextmove(String nextMove){
        this.nextMove = nextMove;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner){
        this.winner = winner;
    }

    public String getId() {
        return id;
    }

    public byte[] getBoardP1() {
        return boardP1;
    }

    public void setBoardP1(byte[] boardP1) {
        this.boardP1 = boardP1;
    }

    public byte[] getBoardP2() {
        return boardP2;
    }

    public void setBoardP2(byte[] boardP2) {
        this.boardP2 = boardP2;
    }
}

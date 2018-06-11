package de.hhn.aib3.aufg3.gruppe11.game.elements.placement;

import java.util.ArrayList;

import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;
import de.hhn.aib3.aufg3.gruppe11.game.enums.ShipType;

/**
 *  Represents initial placement of ships on a game board
 *  One preset per game board
 *  A preset holds 4 ships of different types and lengths
 */
public class BattlePreset implements Preset{

    private final ArrayList<Ship> preset;

    public BattlePreset(){
        preset = new ArrayList<>();
        preset.add(new Ship(1, 1, Orientation.VERTICALLY, ShipType.BOAT5));
        preset.add(new Ship(3, 7, Orientation.HORIZONTALLY, ShipType.BOAT4));
        preset.add(new Ship(8, 3, Orientation.VERTICALLY, ShipType.BOAT3));
        preset.add(new Ship(5, 2, Orientation.HORIZONTALLY, ShipType.BOAT2));
    }

    @Override
    public byte[] toByteArray(){
        byte[][] gameBoard = new byte[GAME_BOARD_SIZE][GAME_BOARD_SIZE];
        for(int i=0; i<gameBoard.length; i++){

            for(int j=0; j<gameBoard.length; j++){

                gameBoard[i][j] = 10;
                for(int k = 0; k< preset.size(); k++){
                    Ship ship = preset.get(k);
                    if((i>= ship.getStartY() && i<= ship.getStopY()) && (j>= ship.getStartX() && j<= ship.getStopX())){
                        switch (ship.getShipType()){
                            case BOAT2:
                                gameBoard[i][j] = 20;
                                break;

                            case BOAT3:
                                gameBoard[i][j] = 30;
                                break;

                            case BOAT4:
                                gameBoard[i][j] = 40;
                                break;

                            case BOAT5:
                                gameBoard[i][j] = 50;
                                break;
                        }
                    }
                }
            }
        }

        byte[] byteList = new byte[GAME_BOARD_SIZE * GAME_BOARD_SIZE];
        for(int i = 0; i< GAME_BOARD_SIZE; i++){
            System.arraycopy(gameBoard[i], 0, byteList, i * GAME_BOARD_SIZE, GAME_BOARD_SIZE);
        }
        return byteList;
    }
}
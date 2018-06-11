package de.hhn.aib3.aufg3.gruppe11.game.elements.placement;

public interface Preset {

    int GAME_BOARD_SIZE = 10;

    /**
     * Conversion of ship objects to byte patterns
     * @return game board in byte
     */
    byte[] toByteArray();

}

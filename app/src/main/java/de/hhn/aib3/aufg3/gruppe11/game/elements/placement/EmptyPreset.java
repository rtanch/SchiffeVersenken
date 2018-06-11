package de.hhn.aib3.aufg3.gruppe11.game.elements.placement;

public class EmptyPreset implements Preset{

    public EmptyPreset(){

    }

    @Override
    public byte[] toByteArray(){
        byte[] byteList = new byte[GAME_BOARD_SIZE * GAME_BOARD_SIZE];
        for(int i=0; i<byteList.length; i++){
            byteList[i] = 10;
        }
        return byteList;
    }
}

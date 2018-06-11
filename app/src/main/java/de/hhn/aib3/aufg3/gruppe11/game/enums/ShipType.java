package de.hhn.aib3.aufg3.gruppe11.game.enums;

public enum ShipType {
    BOAT2(2), BOAT3(3), BOAT4(4), BOAT5(5);

    private final int length;

    ShipType(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}

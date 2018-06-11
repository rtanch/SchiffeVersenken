package de.hhn.aib3.aufg3.gruppe11.game.elements.placement;

import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;
import de.hhn.aib3.aufg3.gruppe11.game.enums.ShipType;

/**
 * Represents a battleship
 */
public class Ship {

    private final int startX;
    private final int startY;
    private final int stopX;
    private final int stopY;
    private final Orientation orientation;
    private final ShipType shipType;

    public Ship(int startX, int startY, Orientation orientation, ShipType shipType) {
        this.startX = startX;
        this.startY = startY;
        this.orientation = orientation;
        this.shipType = shipType;

        if (orientation == Orientation.HORIZONTALLY) {
            stopX = startX + this.shipType.getLength() - 1;
            stopY = startY;
        } else {
            stopX = startX;
            stopY = startY + this.shipType.getLength() - 1;
        }
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getStopX() {
        return stopX;
    }

    public int getStopY() {
        return stopY;
    }

    public ShipType getShipType() {
        return shipType;
    }
}

package de.hhn.aib3.aufg3.gruppe11.game.elements.game;

import de.hhn.aib3.aufg3.gruppe11.game.enums.ShipType;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GEvent;

/**
 * Created by snowflake on 12/6/17.
 */

public class GameEvent {

    private int viewId;
    private GEvent gEvent;
    private ShipType shipType;

    private int row;
    private int column;

    public GameEvent(int viewId, GEvent gEvent) {
        this.viewId = viewId;
        this.gEvent = gEvent;
    }

    public GameEvent(ShipType shipType, GEvent gEvent) {
        this.shipType = shipType;
        this.gEvent = gEvent;
    }

    public GameEvent(int viewId, GEvent gEvent, int row, int column) {
        this.viewId = viewId;
        this.gEvent = gEvent;
        this.row = row;
        this.column = column;
    }

    public GameEvent(GEvent gEvent) {
        this.gEvent = gEvent;
    }

    public int getViewId() {
        return viewId;
    }

    public GEvent getgEvent() {
        return gEvent;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public ShipType getShipType() {
        return shipType;
    }
}

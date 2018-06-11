package de.hhn.aib3.aufg3.gruppe11.game.gui;

import android.graphics.Point;
import android.view.View;

import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;
import de.hhn.aib3.aufg3.gruppe11.game.gui.elements.ShipField;

/**
 * Shadow Builder with size and orientation of selected ship
 */
public class CustomShadowBuilder extends View.DragShadowBuilder {

    private final int length;
    private final Orientation orientation;

    public CustomShadowBuilder(ShipField shipField) {
        super(shipField);

        this.length = shipField.getShipType().getLength();
        this.orientation = shipField.getOrientation();
    }

    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {

        switch (orientation) {
            case HORIZONTALLY:
                size.set(getView().getWidth() * length, getView().getHeight());
                touch.set(getView().getWidth() / 2, getView().getHeight() / 2);
                break;

            case VERTICALLY:
                size.set(getView().getWidth(), getView().getHeight() * length);
                touch.set(getView().getWidth() / 2, getView().getHeight() / 2);
                break;

            default:
                size.set(getView().getWidth(), getView().getHeight());
                touch.set(getView().getWidth() / 2, getView().getHeight() / 2);
                break;
        }
    }
}

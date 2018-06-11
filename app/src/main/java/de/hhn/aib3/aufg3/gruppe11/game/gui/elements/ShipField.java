package de.hhn.aib3.aufg3.gruppe11.game.gui.elements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Cell;
import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;
import de.hhn.aib3.aufg3.gruppe11.game.enums.ShipType;

/**
 * Overlay indicating that filed contains a ship
 */
public class ShipField extends AppCompatImageView {

    private ShipType shipType;
    private Orientation orientation;
    private Cell position;

    public ShipField(Context context, AttributeSet attributeSet, ShipType shipType) {
        super(context, attributeSet);
        this.shipType = shipType;
    }

    public ShipField(Context context, AttributeSet attributeSet, ShipType shipType, Orientation orientation, Cell position) {
        super(context, attributeSet);
        this.shipType = shipType;
        this.orientation = orientation;
        this.position = position;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        Paint paint = new Paint();

        switch (shipType) {
            case BOAT2:
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorShip2));
                break;

            case BOAT3:
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorShip3));
                break;

            case BOAT4:
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorShip4));
                break;

            case BOAT5:
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorShip5));
                break;
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rectF, paint);
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }

    public Cell getPosition() {
        return position;
    }

    public void setPosition(Cell position) {
        this.position = position;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}

package de.hhn.aib3.aufg3.gruppe11.game.gui.elements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Marker indicating whether the field was hit
 */
public class Marker extends View {

    private final Paint paint;

    public Marker(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.YELLOW);
    }

    public Marker(Context context, int color) {
        super(context);
        paint = new Paint();
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getWidth() / 2, canvas.getWidth() / 4, paint);
    }
}

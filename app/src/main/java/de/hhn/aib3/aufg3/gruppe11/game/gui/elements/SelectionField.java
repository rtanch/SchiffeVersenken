package de.hhn.aib3.aufg3.gruppe11.game.gui.elements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import de.hhn.aib3.aufg3.gruppe11.R;

/**
 * Translucent overlay indication that a field is selected
 */
public class SelectionField extends AppCompatImageView {

    private static RectF RECT = new RectF(0, 0, 200, 200);
    private static Paint PAINT = new Paint();

    public SelectionField(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (RECT.width() < this.getWidth() || RECT.height() < this.getHeight()) {
            RECT = new RectF(0, 0, this.getWidth(), this.getHeight());
        }
        PAINT.setColor(ContextCompat.getColor(getContext(), R.color.colorSelection));
        PAINT.setStyle(Paint.Style.FILL);
        canvas.drawRect(RECT, PAINT);
    }

}

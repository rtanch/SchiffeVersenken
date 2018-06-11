package de.hhn.aib3.aufg3.gruppe11.utility;

import android.content.Context;

/**
 * POJO class for EventBus
 */

public class UpdateEvent {

    private Context context = null;

    public UpdateEvent() {
    }

    public UpdateEvent(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

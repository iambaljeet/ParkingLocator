package com.app.parkinglocator.callbacks;

import android.os.Bundle;

public interface AlertSaveLocationCallback {
    void onCancel();

    void onSave(Bundle bundle);
}

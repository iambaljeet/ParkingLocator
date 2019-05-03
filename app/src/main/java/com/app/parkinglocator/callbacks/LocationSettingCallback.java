package com.app.parkinglocator.callbacks;

public interface LocationSettingCallback {
    void onSuccess();

    void onFailed();

    void onNotAvailable();
}

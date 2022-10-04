package com.karikari.goodlocation.interfaces;

import android.location.Location;

public interface GoodLocationDurationListener {
    void onCurrentLocation(Location location);

    void onDurationLeft(Long timeLeft);

    void onDurationFinished();

    void onError(String error);
}

package com.karikari.goodlocation.interfaces;

import android.location.Location;

public interface GoodLocationListener {
    void onCurrentLocation(Location location);
    void onError(String error);
}

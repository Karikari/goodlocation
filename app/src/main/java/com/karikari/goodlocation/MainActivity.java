package com.karikari.goodlocation;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    GoodLocation goodLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goodLocation = new GoodLocation(this);

        /*goodLocation.autoStartLocation(new GoodLocation.GoodLocationListener() {
            @Override
            public void onCurrenLocation(Location location) {
                Log.d(TAG, "Lat : "+ location.getLatitude() + " Lng : "+ location.getLongitude());
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Error : "+ error);
            }
        });*/

        Location location= goodLocation.getLastKnownLocation();

        if(location!=null){
            Log.d(TAG, "Lat : "+ location.getLatitude() + " Lng : "+ location.getLongitude());
        }else {
            Log.d(TAG, "Location is Null");
        }



    }
}

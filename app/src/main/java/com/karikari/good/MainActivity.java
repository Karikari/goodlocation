package com.karikari.good;

import static com.karikari.goodlocation.GoodLocation.APPROXIMATE;
import static com.karikari.goodlocation.GoodLocation.PRECISE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.karikari.goodlocation.GoodLocation;
import com.karikari.goodlocation.MessageBox;
import com.karikari.goodlocation.interfaces.GoodLocationDurationListener;
import com.karikari.goodlocation.interfaces.GoodLocationListener;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private GoodLocation goodLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goodLocation = new GoodLocation(this, "");

        if (!goodLocation.isLocationEnabled()){
            MessageBox.showMessageOK(
                    this,
                    "Enable your GPS Location",
                    (dialogInterface, i) -> {
                        goodLocation.openLocationSettings();
                    }
            );
            return;
        }

        if (!goodLocation.checkIfLocationPermissionGranted()){
            goodLocation.checkForPermissions();

            return;
        }

        goodLocation.startReadingLocation(new GoodLocationListener() {
            @Override
            public void onCurrentLocation(Location location) {
                Log.d(TAG, "Location Reading is "+location.getLatitude()+", "+location.getLongitude() +" Accuracy "+location.getAccuracy());
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Location Reading Error..."+error);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       //  Log.d(TAG, "Request Code " + requestCode + "Granted result is "+ grantResults[0]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goodLocation.getLastKnownLocation() != null){
            Log.d(TAG, "Get Last Known Location "+ goodLocation.getLastKnownLocation().getLatitude());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        goodLocation.stopLocationUpdates();
    }
}
package com.karikari.goodlocation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    GoodLocation goodLocation;
    GoodLocationB goodLocationB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goodLocation = new GoodLocation(this);
        final TextView textView = findViewById(R.id.text);

        final List<LatLng> latLngs = new ArrayList<>();

        String cordiinates = loadJSONFromAsset("cord.json");
        Log.d(TAG, "Cordinates : "+ cordiinates);

        List<Cordinate> cordinateList = new Gson().fromJson(cordiinates, new TypeToken<List<Cordinate>>() {
        }.getType());

        Log.d(TAG, "Cordinates Size: "+ cordinateList.size());

        for(Cordinate cordinate : cordinateList){
            latLngs.add(new LatLng(cordinate.lat, cordinate.lng));
        }

        /*goodLocationB = new GoodLocationB.Builder(this)
                .startLocation(new GoodLocationB.Builder.GoodLocationListener() {
                    @Override
                    public void onCurrenLocation(Location location) {
                        Log.d(TAG, "Location : "+ location.getLatitude()+", "+location.getLongitude());
                    }

                    @Override
                    public void onError(String error) {

                    }
                }).build();*/

        Log.d(TAG, "IS POLY CLOSED : "+ PolyUtil.isClosedPolygon(latLngs));

        goodLocation.autoStartLocation(new GoodLocation.GoodLocationListener() {
            @Override
            public void onCurrenLocation(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "Lat : "+ location.getLatitude() + " Lng : "+ location.getLongitude());
                String s = "Lat : "+ location.getLatitude() + " Lng : "+ location.getLongitude() + " Accuracy : "+location.getAccuracy() + " Location Inside :"+ PolyUtil.containsLocation(latLng,latLngs, false);
                textView.setText(" Cordinates ==>"+ s);

                Log.d(TAG, "Is Location Inside : "+ PolyUtil.containsLocation(latLng,latLngs, true));

            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Error : "+ error);
            }
        });

        Location location= goodLocation.getLastKnownLocation();

        if(location!=null){
            Log.d(TAG, "Lat : "+ location.getLatitude() + " Lng : "+ location.getLongitude());
        }else {
            Log.d(TAG, "Location is Null");
        }




    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}

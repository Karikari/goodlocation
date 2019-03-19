package com.karikari.goodlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GoodLocation implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = GoodLocation.class.getSimpleName();

    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int REQUEST_ALL_PERMISSIONS = 1;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Long LOCATION_TIMER = 0L;
    public static final Long TIMER_INTERVAL = 1000L;


    private LocationManager mLocationManager;

    private CountDownTimer timer;


    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    private Location mLastKnwnLocation;


    /**
     * Tracks the status of the location updates request. Value changes when the boy presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates = false;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private GoodLocationListener mLocationListener;
    private GoodLocationListenerTime mLocatoinTimeListener;
    private Context ctx;


    public GoodLocation(Context context) {
        this.ctx = context;
        //step 1
        buildGoogleApiClient(context);

        //step 2
        createLocationRequest();

        // step 3
        buildLocationSettingsRequest();

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
            Log.i(TAG, "Building GoogleApiClient Connected");

        } else {
            googleAPI.getErrorDialog((Activity) context, resultCode, 0);
        }

        checkforpermissions();


        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        setSingleLocation();
    }


    private void startAfter3sec() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Start Location after" + LOCATION_TIMER);
                startLocationUpdates();
                if (LOCATION_TIMER != 0L) {
                    startLocationTimer();
                }

            }
        }, 3000);
    }

    /*
    * 
    *
    * */
    public void autoStartGoodLocation(GoodLocationListener listener) {
        this.mLocationListener =  listener;
        startAfter3sec();
    }

    public void startGoodLocation(GoodLocationListener listener) {
        this.mLocationListener = listener;
        startLocationUpdates();
    }

    public void startGoodLocationTimer(GoodLocationListenerTime listenerTime){
        this.mLocatoinTimeListener = listenerTime;
        startLocationUpdates();
        startLocationTimer();
    }

    public void cancelTimer(){
            if(timer!=null)
                timer.cancel();
    }


    public boolean isLocationUpdateRunning() {
        return this.mRequestingLocationUpdates;
    }

    public Location getLastUpdateLocation() {
        return this.mCurrentLocation;
    }

    public Location getmLastKnwnLocation() {
        return this.mLastKnwnLocation;
    }

    public Long getmLastUpdateTime() {
        return this.mCurrentLocation.getTime();
    }

    public void setLocationDuration(Long time_in_millis) {
        this.LOCATION_TIMER = time_in_millis;
    }

    //step 1
    private synchronized void buildGoogleApiClient(Context context) {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //step 2
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    //step 3
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    //step 4
    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        );
        result.setResultCallback(this);
    }

    private void startLocationUpdates() {
        goAndDetectLocation();
    }

    @SuppressLint("MissingPermission")
    private void goAndDetectLocation() {
        Log.d(TAG, "Go and Detection Location Started :");

        if(mGoogleApiClient.isConnected()){
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                mRequestingLocationUpdates = true;
                                //     setButtonsEnabledState();
                                Log.d(TAG, "Go and Detection Location :" + status.isSuccess());

                            }
                        });
            } catch (Exception e) {
                Toast.makeText(ctx,  "", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Detect Location error  :"+e.getMessage());

            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {

        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mRequestingLocationUpdates = false;
                    //   setButtonsEnabledState();
                    checkLocationSettings();
                    if (LOCATION_TIMER != 0L) {
                        timer.cancel();
                    }
                }
            });
            if(timer!=null){
                timer.cancel();
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastKnwnLocation == null) {
            mLastKnwnLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        //Log.d(TAG, location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
        if (mLocationListener != null) {
            mLocationListener.currenLocation(location);
        } else {
            //mLocationListener = (LocationListerner) this;
            Log.d(TAG, "Location Listener is null");
        }

        if(mLocatoinTimeListener!=null){
            Log.d(TAG, location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
            mLocatoinTimeListener.currentLocation(location);
        }else{
            Log.d(TAG, "Location Timer Listener is null");
        }
    }


    private void setSingleLocation() {

        List<String> providers = mLocationManager.getProviders(true);
        Location location = null;

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(((AppCompatActivity) ctx), new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }


        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            for (String s : providers) {
                location = mLocationManager.getLastKnownLocation(s);
                if (location != null) {
                    mLastKnwnLocation = location;
                    break;
                }
            }

        }

        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            for (String s : providers) {
                location = mLocationManager.getLastKnownLocation(s);
                if (location != null) {
                    mLastKnwnLocation = location;
                    break;
                }

            }
        }
    }

    public float distance(double lat, double lng){
        float[] results = new float[1];

        if(mLastKnwnLocation!=null){
            Location.distanceBetween(lat, lng, mLastKnwnLocation.getLatitude(), mLastKnwnLocation.getLongitude(), results);
        }

        return results[0];
    }

    public double distance2(double lat, double lng){
        /*
        * Unit of Measurement is in Meters
        * */
        LatLng fromlatlng = null;
        LatLng tolatlng = null;
        if(mLastKnwnLocation!=null){
            fromlatlng = new LatLng(mLastKnwnLocation.getLatitude(), mLastKnwnLocation.getLongitude());
            tolatlng = new LatLng(lat, lng);
            return SphericalUtil.computeDistanceBetween(fromlatlng, tolatlng);
        }

        return 0;
    }


    private void startLocationTimer() {
        //Log.d(TAG, "Time also started : ");

        timer = new CountDownTimer(LOCATION_TIMER, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {

                if(mLocatoinTimeListener!=null){
                    Log.d(TAG, "Timer Left : " + millisUntilFinished);
                    mLocatoinTimeListener.currentLocationTimer(millisUntilFinished);
                }else{
                    Log.d(TAG, "Time Left : " + millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                stopLocationUpdates();
                if(mLocatoinTimeListener!=null){
                    cancelTimer();
                    mLocatoinTimeListener.onLocationTimerFinish();
                }
            }
        }.start();
    }

    private void checkforpermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String PERMISSION : PERMISSIONS) {
                Log.i(TAG, "Permission :" + PERMISSION);

                int permission = ContextCompat.checkSelfPermission(ctx, PERMISSION);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission " + PERMISSION + " is denied");
                    makeRequest();
                }
            }
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(((AppCompatActivity) ctx), PERMISSIONS, REQUEST_ALL_PERMISSIONS);
    }


    public interface GoodLocationListener {
        void currenLocation(Location location);

    }

    public interface GoodLocationListenerTime{
        void currentLocation(Location location);
        void currentLocationTimer(Long timer);
        void onLocationTimerFinish();
    }

}

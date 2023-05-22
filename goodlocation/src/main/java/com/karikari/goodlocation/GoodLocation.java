package com.karikari.goodlocation;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.karikari.goodlocation.interfaces.GoodLocationDurationListener;
import com.karikari.goodlocation.interfaces.GoodLocationListener;

import java.util.concurrent.TimeUnit;

public class GoodLocation {

    private static final String TAG = GoodLocation.class.getSimpleName();

    String[] PERMISSIONS = {
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION
    };

    String[] COARSE_PERMISSION = {ACCESS_COARSE_LOCATION};

    String[] FINE_PERMISSION = {ACCESS_FINE_LOCATION};

    public static final String APPROXIMATE = "Approximate";
    public static final String PRECISE = "Precise";
    public static final String ALL = "All";

    public static final String NONE = "None";


    private static final int REQUEST_ALL_PERMISSIONS = 1;

    private LocationRequest mLocationRequest;
    private Long DURATION = 0L;
    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;

    private LocationManager mLocationManager;

    private CountDownTimer countDownTimer;

    private Long LOCATION_INTERVAL = 5000L;

    private Long FAST_LOCATION_INTERVAL = LOCATION_INTERVAL / 2;

    private Location mLastKnownLocation;

    private GoodLocationListener mLocationListener;

    private GoodLocationDurationListener mLocationDurationListener;

    private Context ctx;

    private boolean permissionsGranted = true;

    private boolean locationUpdateRunning = false;

    public GoodLocation(Context context, Long locationInterval) {
        LOCATION_INTERVAL = TimeUnit.SECONDS.toMillis(locationInterval);
        FAST_LOCATION_INTERVAL = LOCATION_INTERVAL / 2;
        initialize(context, ALL);
    }

    public GoodLocation(Context context) {
        initialize(context, ALL);
    }

    public GoodLocation(Context context, String locationType) {
        initialize(context, locationType);
    }

    private void initialize(Context context, String locationType) {
        this.ctx = context;

        if (TextUtils.equals(locationType, PRECISE)) {
            checkForLocationFinePermissions();
        } else if (TextUtils.equals(locationType, APPROXIMATE)) {
            checkForLocationCoursePermissions();
        } else if (TextUtils.equals(locationType, ALL)) {
            checkForPermissions();
        } else {
            Log.d(TAG, "No Permission");
        }

        //step 2
        createLocationRequest();

        // step 3
        buildLocationSettingsRequest();

        mLocationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.ctx);
        // setSingleLocation();
        setGetLastKnownLocation();
    }

    @SuppressLint("MissingPermission")
    private void setGetLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mLastKnownLocation = location;
                }
            }
        });

        fusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "LastKnown Location failed :" + e.getMessage());
            }
        });
    }

    private void startReadingLocation() {
        startLocationUpdates();
    }


    private void startReadingLocationWithDelay(Long duration) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }, duration);
    }


    public void autoStartLocation(GoodLocationListener listener) {
        this.mLocationListener = listener;
        startReadingLocation();
    }


    public void startReadingLocation(GoodLocationListener listener) {
        this.mLocationListener = listener;
        startReadingLocation();
    }

    public void startReadingDurationLocation(Long minutes, GoodLocationDurationListener listenerTime) {
        this.DURATION = TimeUnit.MINUTES.toMillis(minutes);
        this.mLocationDurationListener = listenerTime;
        startLocationUpdates();
        startLocationTimer();
    }

    public void stopDurationLocation() {
        stopLocationUpdates();
        cancelTimer();
    }

    private void cancelTimer() {
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    // Prevents Memory Leaks
    public void removeContext() {
        if (this.ctx != null) {
            this.ctx = null;
        }
    }

    public Location getLastKnownLocation() {
        return this.mLastKnownLocation;
    }

    public Long getLastUpdateTime() {
        return this.mLastKnownLocation.getTime();
    }

    public boolean isPermissionsGranted() {
        return this.permissionsGranted;
    }

    public void setLocationDuration(Long minutes) {
        this.DURATION = TimeUnit.MINUTES.toMillis(minutes);
    }

    //step 2
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FAST_LOCATION_INTERVAL);
        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
    }

    //step 3
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this.ctx);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "LocationSettings Response is Location Present? " + locationSettingsResponse.getLocationSettingsStates().isLocationPresent());
                Log.d(TAG, "LocationSettings is GPS Available is " + locationSettingsResponse.getLocationSettingsStates().isGpsPresent());
                Log.d(TAG, "LocationSettings is Location Usable is " + locationSettingsResponse.getLocationSettingsStates().isLocationUsable());
            }
        });

    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationUpdateRunning = true;
                for (Location location : locationResult.getLocations()) {
                    if (mLocationListener != null) {
                        mLocationListener.onCurrentLocation(location);
                    }

                    if (mLocationDurationListener != null) {
                        Log.d(TAG, location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
                        mLocationDurationListener.onCurrentLocation(location);
                    }
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void stopLocationUpdates() {
        locationUpdateRunning = false;
        removeLocationUpdates();
    }

    public float distance(double startLatitude, double startLongitude) {
        float[] results = new float[1];
        if (mLastKnownLocation != null) {
            Location.distanceBetween(startLatitude, startLongitude, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), results);
        }
        return results[0];
    }

    public double distance2(double lat, double lng) {
        /*
         * Unit of Measurement is in Meters
         * */
        LatLng fromlatlng = null;
        LatLng tolatlng = null;
        if (mLastKnownLocation != null) {
            fromlatlng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            tolatlng = new LatLng(lat, lng);
            return SphericalUtil.computeDistanceBetween(fromlatlng, tolatlng);
        }

        return 0;
    }

    public boolean isLocationEnabledGPS() {
        LocationManager locationManager = null;
        boolean gpsEnabled = false;
        try {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gpsEnabled;
    }

    public boolean isLocationEnabledNetWork() {
        LocationManager locationManager;
        boolean gpsEnabled = false;
        try {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gpsEnabled;
    }

    public boolean isLocationEnabled() {
        return isLocationEnabledGPS() || isLocationEnabledNetWork();
    }

    public void openLocationSettings() {
        ctx.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void startLocationTimer() {
        if (DURATION != 0) {
            countDownTimer = new CountDownTimer(DURATION, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (mLocationDurationListener != null) {
                        mLocationDurationListener.onDurationLeft(millisUntilFinished);
                    }
                }

                @Override
                public void onFinish() {
                    stopLocationUpdates();
                    cancelTimer();
                    if (mLocationDurationListener != null) {
                        mLocationDurationListener.onDurationFinished();
                    }
                }
            }.start();
        } else {
            mLocationDurationListener.onError("Duration not set");
        }
    }

    public void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String PERMISSION : PERMISSIONS) {
                int permission = ContextCompat.checkSelfPermission(ctx, PERMISSION);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    makeRequest();
                }
            }
        }
    }

    private void checkForLocationFinePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ContextCompat.checkSelfPermission(ctx, ACCESS_FINE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((AppCompatActivity) ctx), FINE_PERMISSION, REQUEST_ALL_PERMISSIONS);

            }
        }
    }

    private void checkForLocationCoursePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ContextCompat.checkSelfPermission(ctx, ACCESS_COARSE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((AppCompatActivity) ctx), COARSE_PERMISSION, REQUEST_ALL_PERMISSIONS);
            }
        }
    }

    public boolean checkIfCoarsePermissionGranted() {
        return ContextCompat.checkSelfPermission(ctx, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkIfFinePermissionGranted() {
        return ContextCompat.checkSelfPermission(ctx, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkIfLocationPermissionGranted() {
        boolean isPermissionGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String PERM : PERMISSIONS) {
                int permission = ContextCompat.checkSelfPermission(ctx, PERM);
                if (permission != PackageManager.PERMISSION_GRANTED){
                    isPermissionGranted = false;
                }

            }
        }
        return isPermissionGranted;
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(((AppCompatActivity) ctx), PERMISSIONS, REQUEST_ALL_PERMISSIONS);
    }

    public boolean isLocationUpdateRunning(){
        return locationUpdateRunning;
    }

}

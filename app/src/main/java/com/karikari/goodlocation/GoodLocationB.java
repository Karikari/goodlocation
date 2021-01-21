package com.karikari.goodlocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GoodLocationB {

    private static final String TAG = GoodLocation.class.getSimpleName();

    public static class Builder implements LocationListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<LocationSettingsResult> {


        private LocationRequest mLocationRequest;
        private GoogleApiClient mGoogleApiClient;
        private LocationSettingsRequest mLocationSettingsRequest;
        private Long DURATION = 0L;
        private FusedLocationProviderClient fusedLocationClient;

        private Context ctx;

        private LocationManager mLocationManager;

        private CountDownTimer countDownTimer;

        private Location mCurrentLocation;

        private Location mLastKnownLocation;

        private Boolean mRequestingLocationUpdates = false;

        protected String mLastUpdateTime;
        private GoodLocationDurationListener mLocationDurationListener;


        private GoodLocationListener mLocationListener;


        public Builder(Context context) {
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


            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }


        public Builder withDuration(Long time) {
            this.DURATION = time;
            return this;
        }


        public GoodLocationB build() {
            return new GoodLocationB();
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
        }

        private void startAfter3secLastKnown() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setGetLastKnownLocation();
                }
            }, 3000);
        }

        private void startAfter3sec() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startLocationUpdates();
                }
            }, 3000);
        }


        public Builder startLocation(GoodLocationListener listener) {
            this.mLocationListener = listener;
            startAfter3sec();
            return this;
        }

        public Builder startDurationLocation(Long minutes, GoodLocationDurationListener listenerTime) {
            this.DURATION = TimeUnit.MINUTES.toMillis(minutes);
            this.mLocationDurationListener = listenerTime;
            startLocationUpdates();
            startLocationTimer();
            return this;

        }

        public void stopDurationLocation() {
            stopLocationUpdates();
            cancelTimer();
        }

        public void stopLocation() {
            stopLocationUpdates();
        }

        public void stopTimer() {
            cancelTimer();
        }

        private void cancelTimer() {
            if (countDownTimer != null)
                countDownTimer.cancel();
        }


        @SuppressLint("MissingPermission")
        private void startLocationUpdates() {
            Log.d(TAG, "Go and Detection Location Started :");

            if (mGoogleApiClient.isConnected()) {
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
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Detect Location error  :" + e.getMessage());

                    if (mLocationDurationListener != null) {
                        mLocationDurationListener.onError(e.getMessage());
                    }

                    if (mLocationListener != null) {
                        mLocationListener.onError(e.getMessage());
                    }

                }
            } else {
                Log.d(TAG, "google Client not Connected");
            }
        }

        /**
         * Removes location updates from the FusedLocationApi.
         */
        private void stopLocationUpdates() {

            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this
                ).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mRequestingLocationUpdates = false;
                        //   setButtonsEnabledState();
                        checkLocationSettings();
                    }
                });
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mLastKnownLocation == null) {
                mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

        }

        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            if (mLocationListener != null) {
                mLocationListener.onCurrenLocation(location);
            }

            if (mLocationDurationListener != null) {
                Log.d(TAG, location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
                mLocationDurationListener.onCurrentLocation(location);
            }
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
                        //cancelTimer();
                        if (mLocationDurationListener != null) {
                            mLocationDurationListener.onDurationFinished();
                        }
                    }
                }.start();
            } else {
                mLocationDurationListener.onError("Duration not set");
            }
        }


        public static boolean isLocationEnabled(Context context) {

            LocationManager locationManager = null;
            boolean gps_enabled = false;
            try {
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                //do nothing...
            }

            boolean network_enabled = false;
            try {
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                //do nothing...
            }

            return gps_enabled || network_enabled;
        }

        public interface GoodLocationListener {
            void onCurrenLocation(Location location);

            void onError(String error);
        }

        public interface GoodLocationDurationListener {
            void onCurrentLocation(Location location);

            void onDurationLeft(Long time_left);

            void onDurationFinished();

            void onError(String error);
        }
    }

}

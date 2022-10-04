# GoodLocation 
[![](https://jitpack.io/v/Karikari/goodlocation.svg)](https://jitpack.io/#Karikari/goodlocation)
### GoodLocation is an Android location library that get location updates for a period of time.
### Get Started
**Add it to your gradle files**
```gradle
   (Project build.gradle)
        allprojects {
                repositories {
                    ...
                    maven { url 'https://jitpack.io' }
                }
            }
        and 
        (app build.gradle)
        dependencies {
            def current_version = "0.0.6"
        	implementation 'com.github.Karikari:goodlocation:$current_version'
        }
```
**Implementation**
```java
        Instantiate the object
        GoodLocation mGoodLocation = new GoodLocation(this)
```     
**This code will update the location and stops after 2 minutes**
```java
       mGoodLocation.startReadingDurationLocation(2L, new GoodLocation.GoodLocationDurationListener() {
                @Override
                public void onCurrentLocation(Location location) {
                    Log.d(TAG, "Location is Duration: "+ location.getLatitude() + ", "+ location.getLongitude());
                }
        
                @Override
                public void onDurationLeft(Long time_left) {
                    Log.d(TAG, "Time Left "+ time_left);
                }
        
                @Override
                public void onDurationFinished() {
                        Log.d(TAG, "Finished");
                }
        
                @Override
                public void onError(String error) {
                    Log.d(TAG, "Duration Error : "+ error);
                }
            });
```
**This location update can be stoped by calling**
```java
        mGoodlocation.stopDurationLacation()
```
**This is for location update with no duration**
```java
        mGoodlocation.startReadingLocation(new GoodLocation.GoodLocationListener() {
            @Override
            public void onCurrenLocation(Location location) {
                Log.d(TAG, "Location is Auto: "+ location.getLatitude() + ", "+ location.getLongitude());
            }
        
            @Override
            public void onError(String error) {
                Log.d(TAG, "AUTO error : "+ error);
            }
        });
```
**This can also be stopped by calling**
```java
        mGoodLocation.stopLocation()
```
**Check if location if enabled**
```java
        boolean enabled = mGoodlocation.isLocationEnabled()
```
**Open Location Settings**
```java
        mGoodlocation.openLocationSettings()
```
**Get Last KnownLocation**
```java
        Location location = mGoodlocation.getLastKnownLocation()
```
**HAPPY CODING !!!**

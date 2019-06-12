# GoodLocation 
[![](https://jitpack.io/v/Karikari/goodlocation.svg)](https://jitpack.io/#Karikari/goodlocation)
### GoodLocation is a Location Library get a location updates for a period of time.
### Get Started
**Add it to your gradle files**

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

**Implementation**

        Instantiate the object
        GoodLocation mGoodLocation = GoodLocation(this)
        
**This code will update the location and stops after 2 minutes**
        
        mGoodLocation.startDurationLocation(2L, new GoodLocation.GoodLocationDurationListener() {
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
**This location update can stop by calling**

        mGoodlocation.stopDurationLacation()

**This is for location update with no duration**

        mGoodlocation.startLocation(new GoodLocation.GoodLocationListener() {
            @Override
            public void onCurrenLocation(Location location) {
                Log.d(TAG, "Location is Auto: "+ location.getLatitude() + ", "+ location.getLongitude());
            }
        
            @Override
            public void onError(String error) {
                Log.d(TAG, "AUTO error : "+ error);
            }
        });

**This can also be stoped by calling**

        mGoodLocation.stopLocation()

**Check if location if enabled**

        boolean enabled = mGoodlocation.isLocationEnabled()

**Get Last KnownLocation**

        Location location = mGoodlocation.getLastKnownLocation()

**HAPPY CODING**

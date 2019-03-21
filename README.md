[![](https://jitpack.io/v/Karikari/goodlocation.svg)](https://jitpack.io/#Karikari/goodlocation)

GoodLocation is a Location Library that gets users location given a duration in minutes.

        Add it in your root build.gradle at the end of repositories:

        allprojects {
                repositories {
                    ...
                    maven { url 'https://jitpack.io' }
                }
            }
            
        
        dependencies {
            def current_version = "0.0.3"
        	implementation 'com.github.Karikari:goodlocation:$current_version'
        }

   
        
        /*
        * Always instantiate this in your onCreate method
        *
        GoodLocation goodlocation = new GoodLocation(this);
        
        
        /*
        * Location start reading after 3 seconds for the automatic reading of locaion.
        */
        goodlocation.autoStartLocation(new GoodLocation.GoodLocationListener() {
                    @Override
                    public void onCurrenLocation(Location location) {
                        Log.d(TAG, "Location is Auto: "+ location.getLatitude() + ", "+ location.getLongitude());
                    }
        
                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "AUTO error : "+ error);
                    }
          });
          
          
          /*
          * You can stop location by calling 
          */
           goodlocation.stopLocation();
        
        
        /*
        *  Duration is set in Minutes  2L
        *
        */
        goodlocation.startDurationLocation(2L, new GoodLocation.GoodLocationDurationListener() {
        
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
                
          /*
          * You can stop duration location by calling
          */
          goodlocation.stopDurationLocation()
          
          /*
          * Get Last Known Location by calling
          */
          goodlocation.getLastKnownLocation()
          
          


GoodLocation is a Location Library that gets users location given a Duration in milliseconds.

        Add it in your root build.gradle at the end of repositories:

        allprojects {
                repositories {
                    ...
                    maven { url 'https://jitpack.io' }
                }
            }
            
        
        dependencies {
        	        implementation 'com.github.Karikari:goodlocation:0.02'
        }

How to

        GoodLocation goodLocation = new GoodLocation(this);
        
        /*
        * Set the duration you want location to update in millis
        * Example 60000 mills = 1 minute
        *
        */
        goodLocation.setLocationDuration(6000);
        
        goodLocation.startGoodLocationTimer(new GoodLocation.GoodLocationListenerTime() {
            @Override
            public void currentLocation(Location location) {
                
            }

            @Override
            public void currentLocationTimer(Long timer) {

            }

            @Override
            public void onLocationTimerFinish() {

            }
        });
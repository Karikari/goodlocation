
GoodLocation is a Location Library that gets users location given a Duration in milliseconds.

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
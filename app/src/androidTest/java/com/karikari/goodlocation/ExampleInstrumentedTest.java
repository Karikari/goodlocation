package com.karikari.goodlocation;


import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = getTargetContext();
        assertEquals("com.karikari.goodlocation", appContext.getPackageName());
    }


    @Test
    public void TestGoodlocationObjectIsNull(){
        GoodLocation goodLocation = new GoodLocation(getTargetContext());
        assertNotNull(goodLocation);

    }

}

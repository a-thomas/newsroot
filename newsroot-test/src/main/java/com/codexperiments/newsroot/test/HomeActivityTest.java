package com.codexperiments.newsroot.test;

import android.test.ActivityInstrumentationTestCase2;

import com.codexperiments.newsroot.ui.activity.*;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super(HomeActivity.class); 
    }

    public void testActivity() {
        HomeActivity activity = getActivity();
        assertNotNull(activity);
    }
}


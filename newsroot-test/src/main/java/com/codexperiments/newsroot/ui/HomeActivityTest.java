package com.codexperiments.newsroot.ui;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.ui.activity.HomeActivity;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    public void testSpinnerValuePersistedBetweenLaunches() {
        Activity activity = getActivity();

        final Button button1 = (Button) activity.findViewById(R.id.button1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Attempts to manipulate the UI must be performed on a UI thread.
                // Calling this outside runOnUiThread() will cause an exception.
                //
                // You could also use @UiThreadTest, but activity lifecycle methods
                // cannot be called if this annotation is used.
                button1.requestFocus();
            }
        });

        // Close the activity
        activity.finish();
        setActivity(null); // Required to force creation of a new activity

        // Relaunch the activity
        activity = this.getActivity();

        // Verify that the spinner was saved at position 1
        final Button button2 = (Button) activity.findViewById(R.id.button1);

        // Since this is a stateful test, we need to make sure that the activity isn't simply
        // echoing a previously-stored value that (coincidently) matches position 1
        // Set spinner to test position 2
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button2.requestFocus();
                button2.hasFocus();
            }
        });

        // Close the activity
        activity.finish();
        setActivity(null);
    }
}

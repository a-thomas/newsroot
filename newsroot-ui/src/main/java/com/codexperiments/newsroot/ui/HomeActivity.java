package com.codexperiments.newsroot.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.api.AuthorizationDeniedException;
import com.codexperiments.newsroot.api.TwitterAuthorizer;
import com.codexperiments.newsroot.ui.timeline.TimecardFragment;
import com.codexperiments.newsroot.ui.timeline.TimelineFragment;
import com.codexperiments.newsroot.ui.authentication.AuthorizationFragment;
import com.codexperiments.newsroot.ui.authentication.AuthorizedEvent;
import com.codexperiments.newsroot.ui.authentication.NotAuthorizedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import static com.codexperiments.newsroot.NewsRootApplication.from;

public class HomeActivity extends BaseActivity {
    @Inject Bus eventBus;
    @Inject TwitterAuthorizer twitterAuthorizer;

    @InjectView(R.id.button1) Button button1;
    @InjectView(R.id.button2) Button button2;
    @InjectView(R.id.button3) Button button3;
    @InjectView(R.id.button4) Button button4;

    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        from(this).inject(this);
        Views.inject(this);

        if (savedInstanceState == null) {
            if (!authorize()) {
                showHomeTimecard();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }
    //endregion


    //region Events
    @OnClick(R.id.button1)
    public void onButton1Action() {
        authorize();
    }

    @OnClick(R.id.button2)
    public void onButton2Action() {
        showHomeTimeline();
    }

    @OnClick(R.id.button3)
    public void onButton3Action() {
        showHomeTimecard();
    }

    @Subscribe
    public void onAuthorized(AuthorizedEvent authorizedEvent) {
        Toast.makeText(this, "Authorized!!!", Toast.LENGTH_LONG).show();
        showHomeTimeline();
    }

    @Subscribe
    public void onNotAuthorized(NotAuthorizedEvent notAuthorizedEvent) {
        if (notAuthorizedEvent.error instanceof AuthorizationDeniedException) {
            Toast.makeText(this, "Authorization denied!?", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Authorization failed...", Toast.LENGTH_LONG).show();
        }
    }
    //endregion


    //region Actions
    protected boolean authorize() {
        AuthorizationFragment authorizationFragment = AuthorizationFragment.authorize(this);
        if (authorizationFragment == null) return false;

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.activity_content, authorizationFragment)
                                   .commit();
        return true;
    }

    protected void showHomeTimeline() {
        TimelineFragment timelineFragment = TimelineFragment.forUser();
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.activity_content, timelineFragment)
                                   .commit();
    }

    protected void showHomeTimecard() {
        TimecardFragment timecardFragment = TimecardFragment.forUser();
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.activity_content, timecardFragment)
                                   .commit();
    }
    //endregion
}

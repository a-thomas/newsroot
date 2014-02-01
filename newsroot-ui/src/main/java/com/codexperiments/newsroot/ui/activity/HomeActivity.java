package com.codexperiments.newsroot.ui.activity;

import javax.inject.Inject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import butterknife.InjectView;
import butterknife.Views;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseActivity;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.ui.fragment.authorization.AuthorizationFragment;
import com.codexperiments.newsroot.ui.fragment.authorization.AuthorizedEvent;
import com.codexperiments.newsroot.ui.fragment.authorization.UnauthorizedEvent;
import com.codexperiments.newsroot.ui.fragment.newslist.NewsListFragment;

public class HomeActivity extends BaseActivity implements AuthorizedEvent.Listener, UnauthorizedEvent.Listener {
    private EventBus mEventBus;
    @Inject TweetManager mTweetManager;

    @InjectView(R.id.button1) Button mButton1;
    @InjectView(R.id.button2) Button mButton2;
    @InjectView(R.id.button3) Button mButton3;

    @Override
    protected void onCreate(Bundle pBundle) {
        super.onCreate(pBundle);
        setContentView(R.layout.activity_home);
        BaseApplication.from(this).dependencies().inject(this);
        Views.inject(this);

        mEventBus = BaseApplication.getServiceFrom(this, EventBus.class);

        mButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                // NewsListFragment lNewsFragment = (NewsListFragment)
                // getSupportFragmentManager().findFragmentById(R.id.activity_content);
                // lNewsFragment.refreshTweets();
            }
        });

        mButton2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                // NewsListFragment lNewsFragment = (NewsListFragment)
                // getSupportFragmentManager().findFragmentById(R.id.activity_content);
                // lNewsFragment.moreTweets();
            }
        });

        mButton3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
            }
        });

        onInitializeInstanceState(pBundle);
        // doSome();
    }

    protected void onInitializeInstanceState(Bundle pBundle) {
        if (pBundle != null) {
        } else {
            // getSupportFragmentManager().beginTransaction().replace(R.id.activity_content,
            // TestFragment.authenticate()).commit();
            if (!mTweetManager.isAuthorized()) {
                authorize();
            } else {
                showHome();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle pBundle) {
        super.onSaveInstanceState(pBundle);
    }

    @Override
    protected void onStart() {
        mEventBus.registerListener(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregisterListener(this);
    }

    private void authorize() {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.activity_content, AuthorizationFragment.authenticate())
                                   .commit();
    }

    @Override
    public void onAuthorized() {
        Toast.makeText(this, "Authorized!!!", Toast.LENGTH_LONG).show();
        showHome();
    }

    @Override
    public void onAuthorizationFailed() {
        Toast.makeText(this, "Authorization failed...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthorizationDenied() {
        Toast.makeText(this, "Authorization denied!?", Toast.LENGTH_LONG).show();
    }

    private void showHome() {
        NewsListFragment lNewsFragment = NewsListFragment.forUser(mTweetManager.getScreenName());
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_content, lNewsFragment).commit();
    }
}

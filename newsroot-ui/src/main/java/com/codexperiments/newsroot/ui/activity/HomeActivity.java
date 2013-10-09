package com.codexperiments.newsroot.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.ui.fragment.AuthorizationFragment;
import com.codexperiments.newsroot.ui.fragment.AuthorizedEvent;
import com.codexperiments.newsroot.ui.fragment.NewsListFragment;
import com.codexperiments.newsroot.ui.fragment.UnauthorizedEvent;

public class HomeActivity extends FragmentActivity implements AuthorizedEvent.Listener, UnauthorizedEvent.Listener {
    private EventBus mEventBus;
    private TwitterManager mTweetManager;

    private Button mButton1;
    private Button mButton2;
    private Button mButton3;

    @Override
    protected void onCreate(Bundle pBundle) {
        super.onCreate(pBundle);
        setContentView(R.layout.activity_home);

        mEventBus = BaseApplication.getServiceFrom(this, EventBus.class);
        mTweetManager = BaseApplication.getServiceFrom(this, TwitterManager.class);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                NewsListFragment lNewsFragment = (NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.activity_content);
                lNewsFragment.refreshTweets();
            }
        });

        mButton2 = (Button) findViewById(R.id.button2);
        mButton2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                NewsListFragment lNewsFragment = (NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.activity_content);
                lNewsFragment.moreTweets();
            }
        });

        mButton3 = (Button) findViewById(R.id.button3);
        mButton3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                doSome();
            }
        });

        onInitializeInstanceState(pBundle);
        doSome();
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

    public void doSome() {
        Log.e("launch", "launch");
        // Observable.from("toto", "titi", "tutu", "tete", "tata", "tyty")
        // .subscribeOn(Schedulers.newThread())
        // .observeOn(AndroidScheduler.getInstance())
        // .subscribe(new Action1<String>() {
        // public void call(String pT1) {
        // Log.e("wtf", "==" + pT1);
        // }
        // }, new Action1<Exception>() {
        // public void call(Exception pT1) {
        // Log.e("wtf", "error");
        // }
        // }, new Action0() {
        // public void call() {
        // Log.e("wtf", "completed");
        // }
        // });
        Log.e("continue", "continue");
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

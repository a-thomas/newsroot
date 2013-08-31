package com.codexperiments.newsroot.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.twitter.TwitterAuthorizationCallback;
import com.codexperiments.robolabor.task.TaskManager;

public class TestFragment extends Fragment {
    private static final String BUNDLE_REDIRECTION = "redirection";

    private EventBus mEventBus;
    private TaskManager mTaskManager;

    private TwitterAuthorizationCallback mRedirection;

    public static final TestFragment authenticate() {
        TestFragment lFragment = new TestFragment();
        Bundle lArguments = new Bundle();
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);

        View lUIFragment = pInflater.inflate(R.layout.fragment_authorization, pContainer, false);

        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
        mRedirection = pBundle.getParcelable(BUNDLE_REDIRECTION);
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
        pBundle.putParcelable(BUNDLE_REDIRECTION, mRedirection);
    }

    @Override
    public void onStart() {
        mTaskManager.manage(this);
        if (mRedirection == null) {
            mEventBus.registerListener(this);
        } else {
        }
        super.onStart();

        // Observable.from("toto", "titi", "tutu", "tete", "tata", "tyty").subscribe(new Action1<String>() {
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
        // }, Schedulers.newThread());
        // Log.e("continue", "continue");
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregisterListener(this);
        mTaskManager.unmanage(this);
    }
}

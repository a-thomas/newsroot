package com.codexperiments.newsroot.ui.timeline;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.ui.BaseFragment;

import static com.codexperiments.newsroot.NewsRootApplication.from;

public class TimelineFragment extends BaseFragment {
    public static final TimelineFragment forUser() {
        TimelineFragment fragment = new TimelineFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        super.onCreateView(layoutInflater, container, bundle);
        View fragment = layoutInflater.inflate(R.layout.fragment_timeline, container, false);
        //Views.inject(this, fragment);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        from(activity).inject(this);
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }
}

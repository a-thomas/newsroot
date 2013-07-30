package com.codexperiments.newsroot.ui.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.twitter.TwitterAuthorizationCallback;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.platform.Platform;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.handler.TaskNotifier;
import com.codexperiments.robolabor.task.util.TaskAdapter;

public class AuthorizationFragment extends Fragment {
    private static final String BUNDLE_REDIRECTION = "redirection";

    private Platform mPlatform;
    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;

    private TwitterAuthorizationCallback mRedirection;

    private WebView mUIWebView;
    private ProgressDialog mUIDialog;

    public static final AuthorizationFragment authenticate() {
        AuthorizationFragment lFragment = new AuthorizationFragment();
        Bundle lArguments = new Bundle();
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pInflater, pContainer, pBundle);
        mPlatform = BaseApplication.getServiceFrom(getActivity(), Platform.class);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterManager = BaseApplication.getServiceFrom(getActivity(), TwitterManager.class);

        View lUIFragment = pInflater.inflate(R.layout.fragment_authorization, pContainer, false);

        mUIWebView = (WebView) lUIFragment.findViewById(R.id.authorization_webview);
        mPlatform.setupWebViewWithJavascript(mUIWebView, new WebChromeClient());
        mUIWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView pWebView, String pUrl) {
                super.onPageFinished(pWebView, pUrl);
                pWebView.requestFocus(View.FOCUS_DOWN);
                mUIDialog.dismiss();

                if (mRedirection == null) {
                    requestAuthorizationTwitter();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView pUIWebView, String pUrl) {
                if (mRedirection.isCallbackUrl(pUrl)) {
                    confirmAuthorization(Uri.parse(pUrl));
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(pUIWebView, pUrl);
                }
            }

            @Override
            public void onReceivedError(WebView pWebViewiew, int pErrorCode, String pDescription, String pFailingUrl) {
                onConnectionError();
            }
        });
        mUIWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
                }
                return false;
            }
        });
        mUIWebView.getSettings().setUserAgentString(mUIWebView.getSettings().getUserAgentString() + " agent");
        mUIDialog = new ProgressDialog(getActivity());

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
            mUIWebView.loadUrl("file:///android_asset/twitter_authorize.html");
        } else {
            mUIWebView.loadUrl(mRedirection.getAuthorizationUrl());
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUIDialog.dismiss();
        mEventBus.unregisterListener(this);
        mTaskManager.unmanage(this);
        mUIWebView.setWebViewClient(new WebViewClient());
        mPlatform.stopWebView(mUIWebView);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlatform.pauseWebView(mUIWebView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlatform.resumeWebView(mUIWebView);
    }

    protected void onConnectionError() {
        mEventBus.dispatch(new UnauthorizedEvent());
    }

    private void requestAuthorizationTwitter() {
        mTaskManager.execute(new TaskAdapter<TwitterAuthorizationCallback>() {
            TwitterManager lTwitterManager = mTwitterManager;

            @Override
            public void onStart(boolean pIsRestored) {
                mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving data ...", true);
            }

            @Override
            public TwitterAuthorizationCallback onProcess(TaskNotifier pNotifier) throws Exception {
                return lTwitterManager.requestAuthorization();
            }

            @Override
            public void onFinish(TwitterAuthorizationCallback pRedirection) {
                mRedirection = pRedirection;
                mUIWebView.loadUrl(mRedirection.getAuthorizationUrl());
                // Dialog will get dismissed when page is loaded.
            }

            @Override
            public void onFail(Throwable pException) {
                mUIDialog.dismiss();
                mEventBus.dispatch(new UnauthorizedEvent(pException));
            }
        });
    }

    public void confirmAuthorization(final Uri pUri) {
        mTaskManager.execute(new TaskAdapter<Void>() {
            TwitterManager lTweetManager = mTwitterManager;

            @Override
            public void onStart(boolean pIsRestored) {
                mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving data ...", true);
            }

            @Override
            public Void onProcess(TaskNotifier pNotifier) throws Exception {
                lTweetManager.confirmAuthorization(pUri);
                return null;
            }

            @Override
            public void onFinish(Void pEmpty) {
                mUIDialog.dismiss();
                mEventBus.dispatch(new AuthorizedEvent());
            }

            @Override
            public void onFail(Throwable pException) {
                mUIDialog.dismiss();
                mEventBus.dispatch(new UnauthorizedEvent(pException));
            }
        });
    }
}

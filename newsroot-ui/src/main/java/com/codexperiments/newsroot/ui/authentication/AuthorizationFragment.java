package com.codexperiments.newsroot.ui.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.api.TwitterAuthorizer;
import com.codexperiments.newsroot.api.TwitterCredentials;
import com.codexperiments.newsroot.ui.BaseFragment;
import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.codexperiments.newsroot.NewsRootApplication.from;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class AuthorizationFragment extends BaseFragment {
    private static final String LOADING_URL = "file:///android_asset/html/twitter_authorize.html";
    private static final String CALLBACK_URL = "oauth://newsroot-callback";

    // Dependencies
    @Inject Bus eventBus;
    @Inject TwitterAuthorizer twitterAuthorizer;
    protected SharedPreferences preferences;
    // State
    protected String redirection;
    protected CompositeSubscription subscriptions = new CompositeSubscription();
    // UI
    protected WebView webView;
    protected ProgressDialog progressDialog;


    public static final AuthorizationFragment authorize(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("newsroot_settings", 0);
        String userToken = preferences.getString("userToken", null);
        String userSecret = preferences.getString("userSecret", null);
        if (!Strings.isNullOrEmpty(userToken) && !Strings.isNullOrEmpty(userSecret)) return null;

        AuthorizationFragment fragment = new AuthorizationFragment();
        Bundle lArguments = new Bundle();
        fragment.setArguments(lArguments);
        return fragment;
    }

    //region Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragment = inflater.inflate(R.layout.fragment_authorization, container, false);
        Views.inject(this, fragment);

        webView = (WebView) fragment.findViewById(R.id.authorization_webview);
        // Main WebView settings.
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setJavaScriptEnabled(true);

        // Cache settings.
        String cachePath = getActivity().getCacheDir().getAbsolutePath();
//        int appCacheMaxSize = 1024 * 1024 * getWebViewCacheSizeInByte();
//        webView.getSettings().setAllowFileAccess(true);
//        webView.getSettings().setAppCacheEnabled(true);
//        webView.getSettings().setAppCacheMaxSize(appCacheMaxSize);
//        webView.getSettings().setAppCachePath(cachePath);
//        webView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webview, String url) {
                super.onPageFinished(webview, url);
                webview.requestFocus(View.FOCUS_DOWN);

                onPageLoaded(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                if ((url != null) && onLoadPage(url)) return true;
                else return super.shouldOverrideUrlLoading(webview, url);
            }

            @Override
            public void onReceivedError(WebView webview, int errorCode, String description, String failingUrl) {
                onPageError();
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!view.hasFocus()) view.requestFocus();
                        break;
                }
                return false;
            }
        });
        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString() + " agent");
        progressDialog = new ProgressDialog(getActivity());

        // State restoration.
        Bundle bundle = (savedInstanceState != null) ? savedInstanceState : getArguments();
        redirection = bundle.getString("redirection");
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        from(getActivity()).inject(this);
        preferences = getActivity().getSharedPreferences("newsroot_settings", 0);

        requestAuthorization();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
        pBundle.putString("redirection", redirection);
    }

    @Override
    public void onStart() {
        super.onStart();
        // mTaskManager.manage(this);
        if (redirection == null) {
            webView.loadUrl(LOADING_URL);
        } else {
            webView.loadUrl(redirection);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        progressDialog.dismiss();
        // mTaskManager.unmanage(this);
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.register(this);

        try {
            WebView.class.getMethod("onPause").invoke(webView);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.unregister(this);

        try {
            WebView.class.getMethod("onResume").invoke(webView);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
        }
    }
    //endregion


    //region Events
    protected boolean onLoadPage(String url) {
        if (url.startsWith(CALLBACK_URL)) {
            confirmAuthorization(Uri.parse(url));
            return true;
        } else return false;
    }

    protected void onPageLoaded(String url) {
        if (!url.equals(LOADING_URL)) {
            progressDialog.dismiss();
        }
    }

    protected void onPageError() {
        eventBus.post(new NotAuthorizedEvent());
    }
    //endregion


    //region Actions
    protected void requestAuthorization() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Connecting to Twitter...", true);
        sub(twitterAuthorizer.requestAuthorization(CALLBACK_URL)
                             .observeOn(mainThread())
                             .subscribe(new Observer<String>() {
                                 public void onNext(String redirection) {
                                     AuthorizationFragment.this.redirection = redirection;
                                     webView.loadUrl(redirection);
                                 }

                                 public void onCompleted() {
                                 }

                                 public void onError(Throwable error) {
                                     progressDialog.dismiss();
                                     eventBus.post(new NotAuthorizedEvent(error));
                                 }
                             }));
    }

    protected void confirmAuthorization(final Uri uri) {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Confirming authorization...", true);
        sub(twitterAuthorizer.confirmAuthorization(uri.toString())
                             .observeOn(mainThread())
                             .subscribe(new Observer<TwitterCredentials>() {
                                 public void onNext(TwitterCredentials credentials) {
                                     progressDialog.dismiss();
                                     preferences.edit()
                                                .putString("userToken", credentials.consumerKey)
                                                .putString("userSecret", credentials.consumerSecret)
                                                .apply();
                                     eventBus.post(new AuthorizedEvent());
                                 }

                                 public void onCompleted() {
                                 }

                                 public void onError(Throwable error) {
                                     progressDialog.dismiss();
                                     eventBus.post(new NotAuthorizedEvent(error));
                                 }
                             }));
    }
    //endregion


    //region Utilities
    protected void sub(Subscription subscription) {
        subscriptions.add(subscription);
    }
    //endregion
}

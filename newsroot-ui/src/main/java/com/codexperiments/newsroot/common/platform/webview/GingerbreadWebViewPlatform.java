package com.codexperiments.newsroot.common.platform.webview;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

@TargetApi(9)
public class GingerbreadWebViewPlatform implements WebViewPlatform {
    protected Context mContext;

    public GingerbreadWebViewPlatform(Context pContext) {
        mContext = pContext;
    }

    protected int getWebViewCacheSizeInByte() {
        return 8 * 1024 * 1024;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setupWebView(WebView pWebView, WebChromeClient pWebChromeClient) {
        // Main WebView settings.
        pWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
        pWebView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
        pWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Cache settings.
        String cachePath = mContext.getCacheDir().getAbsolutePath();
        int appCacheMaxSize = 1024 * 1024 * getWebViewCacheSizeInByte();
        pWebView.getSettings().setAllowFileAccess(true);
        pWebView.getSettings().setAppCacheEnabled(true);
        pWebView.getSettings().setAppCacheMaxSize(appCacheMaxSize);
        pWebView.getSettings().setAppCachePath(cachePath);
        pWebView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);

        pWebView.setWebChromeClient(pWebChromeClient);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void setupWebViewWithJavascript(WebView pWebView, WebChromeClient pWebChromeClient) {
        setupWebView(pWebView, pWebChromeClient);

        pWebView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void setupWebViewWithDataStore(WebView pWebView, WebChromeClient pWebChromeClient, String pDatastoreName) {
        setupWebViewWithJavascript(pWebView, pWebChromeClient);

        // To enable HTML5 SQL Datastore, uncomment the following piece of code.
        // Don't forget to change also WebChromeClientWrapper.onExceededDatabaseQuota().
        File databasePath = new File(mContext.getFilesDir(), pDatastoreName);
        try {
            pWebView.getSettings().setDatabasePath(databasePath.getCanonicalPath());
            pWebView.getSettings().setDomStorageEnabled(true);
            pWebView.getSettings().setDatabaseEnabled(true);
        } catch (IOException e) {
            e.printStackTrace(); // TODO Handle logs.
            pWebView.getSettings().setDomStorageEnabled(false);
            pWebView.getSettings().setDatabaseEnabled(false);
        }
    }

    @Override
    public void resumeWebView(WebView pWebView) {
        try {
            Method method = WebView.class.getMethod("onResume");
            method.invoke(pWebView);
        }
        // Ignored.
        catch (NoSuchMethodException eNoSuchMethodException) {
        } catch (IllegalAccessException eIllegalAccessException) {
        } catch (InvocationTargetException eInvocationTargetException) {
        }
    }

    @Override
    public void pauseWebView(WebView pWebView) {
        try {
            Method method = WebView.class.getMethod("onPause");
            method.invoke(pWebView);
        }
        // Ignored.
        catch (NoSuchMethodException eNoSuchMethodException) {
        } catch (IllegalAccessException eIllegalAccessException) {
        } catch (InvocationTargetException eInvocationTargetException) {
        }
    }

    @Override
    public void stopWebView(WebView pWebView) {
        pWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
    }
}

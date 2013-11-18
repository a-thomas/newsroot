package com.codexperiments.newsroot.common.platform.webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Abstracts WebView specific set-up.
 */
public interface WebViewPlatform {
    /**
     * Setup a WebView in a platform specific way (hardware rendering, etc.)
     */
    void setupWebView(WebView pWebView, WebChromeClient pWebChromeClient);

    /**
     * Setup a WebView with Javascript enabled.
     */
    void setupWebViewWithJavascript(WebView pWebView, WebChromeClient pWebChromeClient);

    /**
     * Same as setupWebviewWithJavascript() but activates the HTML5 SQL Datastore.
     */
    void setupWebViewWithDataStore(WebView pWebView, WebChromeClient pWebChromeClient, String pDatastoreName);

    /**
     * Makes sure the webview is properly paused (because Javascript thread or the Flash plugin may still run, etc.).
     */
    void resumeWebView(WebView pWebView);

    /**
     * Makes sure the webview is properly resumed (because Javascript thread or the Flash plugin may be paussed, etc.).
     */
    void pauseWebView(WebView pWebView);

    /**
     * Makes sure the webview is properly stopped (because Javascript thread or the Flash plugin may still run, etc.).
     */
    void stopWebView(WebView pWebView);
}

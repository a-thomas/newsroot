package com.codexperiments.newsroot.common.platform;

import android.content.Context;
import android.os.Build;

import com.codexperiments.newsroot.common.AndroidModule;
import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.common.platform.webview.GingerbreadWebViewPlatform;
import com.codexperiments.newsroot.common.platform.webview.HoneycombWebViewPlatform;
import com.codexperiments.newsroot.common.platform.webview.WebViewPlatform;

import dagger.Module;
import dagger.Provides;

/**
 * Class used to abstract platform-specific set-up, behaviour or anything else.
 */
@Module(library = true, includes = AndroidModule.class)
public class PlatformModule {
    @Provides
    public WebViewPlatform provideWebViewPlatform(@Application Context pContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new HoneycombWebViewPlatform(pContext);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return new GingerbreadWebViewPlatform(pContext);
        } else {
            throw new PlatformNotSupported(Build.VERSION.SDK_INT);
        }
    }
}

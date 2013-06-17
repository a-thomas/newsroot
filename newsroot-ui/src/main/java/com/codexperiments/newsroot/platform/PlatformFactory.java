package com.codexperiments.newsroot.platform;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.codexperiments.newsroot.platform.eclair.EclairPlatform;
import com.codexperiments.newsroot.platform.froyo.FroyoPlatform;
import com.codexperiments.newsroot.platform.gingerbread.GingerbreadPlatform;
import com.codexperiments.newsroot.platform.honeycomb.HoneycombPlatform;
import com.codexperiments.newsroot.platform.icecreamsandwich.IceCreamSandwichPlatform;

public class PlatformFactory
{
    public static Platform getCurrentPlatform(Application pApplication) {
        Context context = pApplication.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new IceCreamSandwichPlatform(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new HoneycombPlatform(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return new GingerbreadPlatform(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return new FroyoPlatform(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            return new EclairPlatform(context);
        } else {
            throw new PlatformNotSupported(Build.VERSION.SDK_INT);
        }
    }
}

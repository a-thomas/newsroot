package com.codexperiments.newsroot.platform.jellybean;

import android.annotation.TargetApi;
import android.content.Context;

import com.codexperiments.newsroot.platform.icecreamsandwich.IceCreamSandwichPlatform;

@TargetApi(16)
public class JellyBeanPlatform extends IceCreamSandwichPlatform
{
    public JellyBeanPlatform(Context pContext) {
        super(pContext);
    }
}

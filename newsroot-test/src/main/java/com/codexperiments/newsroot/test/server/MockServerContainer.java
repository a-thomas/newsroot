package com.codexperiments.newsroot.test.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class MockServerContainer implements Container {
    private Context mContext;
    private MockServerHandler mHandler;

    public MockServerContainer(Context pContext, MockServerHandler pHandler) {
        super();
        mContext = pContext;
        mHandler = pHandler;
    }

    @Override
    public void handle(Request pRequest, Response pResponse) {
        PrintStream lBody = null;
        try {
            lBody = renderHeader(pRequest, pResponse);
            if ("1".equals(pRequest.getQuery().get("ping"))) {
                lBody.println("1");
            } else {
                byte[] lResponse = readAssetToByte(mContext.getAssets(), mHandler.getResponseAsset(pRequest));
                lBody.write(lResponse);
            }
        } catch (Exception pException) {
            pException.printStackTrace();
        } finally {
            if (lBody != null) lBody.close();
        }
    }

    protected PrintStream renderHeader(Request request, Response response) throws IOException {
        PrintStream body = response.getPrintStream();
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain; charset=UTF-8");
        response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        return body;
    }

    protected static byte[] readAssetToByte(AssetManager assetManager, String pAssetPath) throws IOException {
        if (pAssetPath == null) return new byte[0];

        InputStream lInput = null;
        try {
            lInput = assetManager.open(pAssetPath);
            // File can't be more than 2 Go...
            byte[] lInputBuffer = new byte[lInput.available()];
            lInput.read(lInputBuffer);
            return lInputBuffer;
        } finally {
            try {
                if (lInput != null) lInput.close();
            } catch (IOException ioException) {
                Log.e(MockServer.class.getSimpleName(), "Error while reading assets", ioException);
            }
        }
    }
}
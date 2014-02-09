package com.codexperiments.newsroot.test.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import android.content.Context;
import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

/**
 * Mock of the backend server the application communicates with. It must be used in conjunction with Mockito to check expectations
 * and specify its behaviour.
 * 
 * <br/>
 * Usage example:
 * 
 * <pre>
 * MockServerHandler mServerHandler = mock(MockServerHandler.class);
 * MockServer mServer = new MockServer(this, mServerHandler);
 * ...
 * whenRequest(mServerHandler).thenReturn("myAssetResource.file");
 * ... perform the request ...
 * verify(mServerHandler).getResponse(argThat(allOf(hasUrl(&quot;my/expected/url&quot;),
 *                                                  hasQueryParam(&quot;myparam&quot;, 1234),
 *                                                  not(hasQueryParam(&quot;unexisting&quot;)))));
 * ...
 * mServer.stop();
 * </pre>
 */
public class MockServer {
    public static final int PORT = 8378;

    private Context mContext;
    private Server mServer;
    private Connection mConnection;
    private MockServerHandler mHandler;

    public MockServer(InstrumentationTestCase pInstrumentationTestCase, MockServerHandler pHandler) {
        this(pInstrumentationTestCase.getInstrumentation().getContext(), pHandler);
    }

    public MockServer(Context pContext, MockServerHandler pHandler) {
        try {
            mContext = pContext;
            mHandler = pHandler;
            mServer = new ContainerServer(new Container() {
                public void handle(Request pRequest, Response pResponse) {
                    MockServer.this.handle(pRequest, pResponse);
                }
            });
            mConnection = new SocketConnection(mServer);
            mConnection.connect(new InetSocketAddress(PORT));
            waitUntilReady();
        } catch (IOException eIOException) {
            throw new RuntimeException(eIOException);
        }
    }

    private void waitUntilReady() throws IOException {
        URL pingURL = new URL("http://127.0.0.1:" + PORT + "/?ping=1");
        URLConnection yc = pingURL.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String response = in.readLine();
        if (response == null || !response.equals("1")) {
            throw new IOException("Bad ping response : " + response);
        }
        in.close();
    }

    public void stop() throws IOException {
        mServer.stop();
        mConnection.close();
    }

    private void handle(Request pRequest, Response pResponse) {
        PrintStream lBody = null;
        try {
            lBody = renderHeader(pRequest, pResponse);
            if ("1".equals(pRequest.getQuery().get("ping"))) {
                lBody.println("1");
            } else {
                String responseAsset;
                synchronized (mHandler) {
                    responseAsset = mHandler.getResponse(pRequest);
                }
                byte[] lResponse = readAssetToByte(mContext.getAssets(), responseAsset);
                lBody.write(lResponse);
            }
        } catch (Exception pException) {
            pException.printStackTrace();
        } finally {
            if (lBody != null) lBody.close();
        }
    }

    private static PrintStream renderHeader(Request request, Response response) throws IOException {
        PrintStream body = response.getPrintStream();
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain; charset=UTF-8");
        response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        return body;
    }

    private static byte[] readAssetToByte(AssetManager assetManager, String pAssetPath) throws IOException {
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
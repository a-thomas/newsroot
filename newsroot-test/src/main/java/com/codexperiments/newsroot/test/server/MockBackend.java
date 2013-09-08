package com.codexperiments.newsroot.test.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.SocketConnection;

import android.content.Context;
import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class MockBackend implements Container {
    private static final int PORT = 8378;

    private Context mContext;
    private Handler mHandler;

    public MockBackend(Context pContext, Handler pHandler) {
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

    private PrintStream renderHeader(Request request, Response response) throws IOException {
        PrintStream body = response.getPrintStream();
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain; charset=UTF-8");
        response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        return body;
    }

    public static String readAssetToString(AssetManager assetManager, String pAssetPath) throws IOException {
        InputStream input = null;
        try {
            input = assetManager.open(pAssetPath);
            // File can't be more than 2 Go...
            byte[] inputBuffer = new byte[input.available()];
            input.read(inputBuffer);
            return new String(inputBuffer);
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException ioException) {
                Log.e(MockBackend.class.getSimpleName(), "Error while reading assets", ioException);
            }
        }
    }

    public static byte[] readAssetToByte(AssetManager assetManager, String pAssetPath) throws IOException {
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
                Log.e(MockBackend.class.getSimpleName(), "Error while reading assets", ioException);
            }
        }
    }

    public static class Server {
        private MockBackend container;
        private org.simpleframework.transport.Server server;
        private org.simpleframework.transport.connect.Connection connection;
        private SocketAddress address;

        public Server(InstrumentationTestCase pInstrumentationTestCase, Handler pHandler) {
            this(pInstrumentationTestCase.getInstrumentation().getContext(), pHandler);
        }

        public Server(Context pContext, Handler pHandler) {
            try {
                container = new MockBackend(pContext, pHandler);
                server = new ContainerServer(container);
                connection = new SocketConnection(server);
                address = new InetSocketAddress(PORT);

                connection.connect(address);
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
            server.stop();
            connection.close();
        }
    }

    public interface Handler {
        String getResponseAsset(Request pRequest);
    }
}
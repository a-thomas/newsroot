package com.codexperiments.newsroot.test.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * Mock of the backend server the application communicates with. It must be used in conjunction with Mockito to check expectations
 * and specify its behaviour.
 * 
 * <br/>
 * Usage example:
 * 
 * <pre>
 * MockBackendHandler mServerHandler = mock(MockBackendHandler.class);
 * MockServerHandler mServer = new MockServer(this, mServerHandler);
 * ...
 * whenRequest(getBackend()).thenReturn("myAssetResource.file");
 * ... perform the request ...
 * verify(mServerHandler).getResponseAsset(argThat(allOf(hasUrl(&quot;my/expected/url&quot;),
 *                                                       hasQueryParam(&quot;myparam&quot;, 1234),
 *                                                       not(hasQueryParam(&quot;unexisting&quot;)))));
 * ...
 * mServer.stop();
 * </pre>
 */
public class MockServer {
    private static final int PORT = 8378;

    private Server server;
    private Connection connection;

    public MockServer(InstrumentationTestCase pInstrumentationTestCase, MockServerHandler pHandler) {
        this(pInstrumentationTestCase.getInstrumentation().getContext(), pHandler);
    }

    public MockServer(Context pContext, MockServerHandler pHandler) {
        try {
            MockServerContainer container = new MockServerContainer(pContext, pHandler);
            server = new ContainerServer(container);
            connection = new SocketConnection(server);
            connection.connect(new InetSocketAddress(PORT));
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
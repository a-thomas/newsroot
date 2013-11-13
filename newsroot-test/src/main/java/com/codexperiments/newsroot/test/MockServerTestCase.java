package com.codexperiments.newsroot.test;

import static org.mockito.Mockito.mock;
import android.app.Application;
import android.test.InstrumentationTestCase;

import com.codexperiments.newsroot.test.server.MockServer;
import com.codexperiments.newsroot.test.server.MockServerHandler;

public abstract class MockServerTestCase extends InstrumentationTestCase {
    private Application mApplication;
    private MockServerHandler mServerHandler;
    private MockServer mServer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mServerHandler = mock(MockServerHandler.class);
        mServer = new MockServer(this, mServerHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        mServer.stop();
        super.tearDown();
    }

    public Application getApplication() {
        return mApplication;
    }

    public MockServerHandler getServer() {
        return mServerHandler;
    }
}

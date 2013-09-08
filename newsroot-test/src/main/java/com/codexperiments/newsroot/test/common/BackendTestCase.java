package com.codexperiments.newsroot.test.common;

import static org.mockito.Mockito.mock;
import android.app.Application;
import android.test.InstrumentationTestCase;

import com.codexperiments.newsroot.test.server.MockBackend;

public abstract class BackendTestCase extends InstrumentationTestCase {
    private Application mApplication;
    private MockBackend.Handler mServerHandler;
    private MockBackend.Server mServer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mServerHandler = mock(MockBackend.Handler.class);
        mServer = new MockBackend.Server(this, mServerHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        mServer.stop();
        super.tearDown();
    }

    public Application getApplication() {
        return mApplication;
    }

    public MockBackend.Handler getServer() {
        return mServerHandler;
    }
}

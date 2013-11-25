package com.codexperiments.newsroot.test;

import static org.mockito.Mockito.mock;
import android.app.Application;
import android.test.InstrumentationTestCase;

import com.codexperiments.newsroot.test.server.MockServer;
import com.codexperiments.newsroot.test.server.MockServerHandler;
import com.codexperiments.newsroot.ui.NewsRootApplication;

import dagger.ObjectGraph;

public abstract class MockServerTestCase extends InstrumentationTestCase {
    private NewsRootApplication mApplication;
    private MockServerHandler mServerHandler;
    private MockServer mServer;
    private ObjectGraph mDependenciesBackup;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mApplication = (NewsRootApplication) getInstrumentation().getTargetContext().getApplicationContext();
        mServerHandler = mock(MockServerHandler.class);
        mServer = new MockServer(this, mServerHandler);
        mDependenciesBackup = mApplication.dependencies();
    }

    @Override
    protected void tearDown() throws Exception {
        mServer.stop();
        mApplication.resetDependenciesForTestPurpose(mDependenciesBackup);
        mDependenciesBackup = null;
        super.tearDown();
    }

    protected void inject(ObjectGraph pDependencies) {
        mApplication.resetDependenciesForTestPurpose(pDependencies);
        mApplication.dependencies().inject(this);
    }

    public Application getApplication() {
        return mApplication;
    }

    public MockServerHandler getServer() {
        return mServerHandler;
    }
}

package com.codexperiments.newsroot.test;

import static org.mockito.Mockito.mock;
import rx.concurrency.TestScheduler;
import android.app.Application;
import android.test.InstrumentationTestCase;

import com.codexperiments.newsroot.test.server.MockServer;
import com.codexperiments.newsroot.test.server.MockServerHandler;
import com.codexperiments.newsroot.ui.NewsRootApplication;
import com.codexperiments.newsroot.ui.NewsRootModule;

import dagger.ObjectGraph;

public abstract class TestCase extends InstrumentationTestCase {
    private NewsRootApplication mApplication;
    private MockServerHandler mServerHandler;
    private MockServer mServer;
    private TestScheduler mScheduler;
    private ObjectGraph mDependenciesBackup;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mApplication = (NewsRootApplication) getInstrumentation().getTargetContext().getApplicationContext();
        mServerHandler = mock(MockServerHandler.class);
        mServer = new MockServer(this, mServerHandler);
        mScheduler = new TestScheduler();
        mDependenciesBackup = mApplication.dependencies();
    }

    @Override
    protected void tearDown() throws Exception {
        mServer.stop();
        mApplication.resetDependenciesForTestPurpose(mDependenciesBackup);
        mDependenciesBackup = null;
        super.tearDown();
    }

    protected void inject(Object... pModules) {
        Object[] lModules = new Object[pModules.length + 1];
        lModules[0] = new NewsRootModule(getApplication());
        System.arraycopy(pModules, 0, lModules, 1, pModules.length);

        ObjectGraph lDependencies = ObjectGraph.create(lModules);
        mApplication.resetDependenciesForTestPurpose(lDependencies);
        mApplication.dependencies().inject(this);
    }

    public Application getApplication() {
        return mApplication;
    }

    public MockServerHandler server() {
        return mServerHandler;
    }

    public TestScheduler scheduler() {
        return mScheduler;
    }
}

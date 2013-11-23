package com.codexperiments.newsroot.repository.twitter;

import com.codexperiments.newsroot.ui.NewsRootModule;

import dagger.Module;

@Module(includes = NewsRootModule.class, //
        injects = { TwitterRemoteRepositoryTest.class },
        overrides = true)
public class TestModule {
}

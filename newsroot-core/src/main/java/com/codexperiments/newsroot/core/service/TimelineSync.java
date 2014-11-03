package com.codexperiments.newsroot.core.service;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import rx.Observable;

public interface TimelineSync {
    Observable<java.util.List<Tweet>> synchronize();
}

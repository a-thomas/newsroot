package com.codexperiments.newsroot.test.data;

import static com.codexperiments.newsroot.test.helper.RxTest.scheduleOnComplete;
import static com.codexperiments.newsroot.test.helper.RxTest.scheduleOnNext;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.TestScheduler;
import rx.subscriptions.Subscriptions;
import android.content.Context;
import android.util.Log;

import com.codexperiments.newsroot.common.data.Database;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.manager.tweet.TweetAccessException;
import com.codexperiments.newsroot.manager.tweet.TweetParser;
import com.codexperiments.newsroot.repository.tweet.TweetPageResponse;

public class TweetPageResponseData {
    public static Observable<TweetPageResponse> observable(final TestScheduler pScheduler, final TweetPageResponse... pResponses)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(Observer<? super TweetPageResponse> pTweetPageResponseObserver) {
                int lTimeDelay = 0;
                for (int i = 0; i < pResponses.length; ++i) {
                    scheduleOnNext(pScheduler, pTweetPageResponseObserver, pResponses[i], lTimeDelay);
                    lTimeDelay += 100;
                }
                scheduleOnComplete(pScheduler, pTweetPageResponseObserver, lTimeDelay);

                pScheduler.advanceTimeBy(lTimeDelay + 1, TimeUnit.MILLISECONDS);
                return Subscriptions.empty();
            }
        });
    }

    public static TweetPageResponse fromAsset(Context pContext, TimeGap pTimeGap, String pAssetPath)
        throws IOException, TweetAccessException
    {
        InputStream lAssetStream = null;
        try {
            lAssetStream = pContext.getAssets().open(pAssetPath);
            TweetParser lParser = new TweetParser(pContext);
            TweetPage lTweetPage = lParser.parseTweetPage(pTimeGap, TweetPageData.PAGE_SIZE, lAssetStream);
            return new TweetPageResponse(lTweetPage, pTimeGap);
        } finally {
            try {
                if (lAssetStream != null) lAssetStream.close();
            } catch (IOException eIOException) {
                Log.e(Database.class.getSimpleName(), "Error while reading assets", eIOException);
            }
        }
    }
}

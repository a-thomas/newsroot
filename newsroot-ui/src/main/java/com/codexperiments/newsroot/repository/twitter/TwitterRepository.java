package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.util.functions.Func1;
import android.app.Application;
import android.database.Cursor;

import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.Query;
import com.codexperiments.newsroot.manager.twitter.ResultHandler;
import com.codexperiments.newsroot.manager.twitter.TimeGapDAO;
import com.codexperiments.newsroot.manager.twitter.TweetDAO;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_VIEW_TIMELINE;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.manager.twitter.ViewTimelineDAO;
import com.codexperiments.robolabor.task.TaskManager;

public class TwitterRepository {
    private static final int DEFAULT_PAGE_SIZE = 20;

    private Config mConfig;
    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;
    private TwitterAPI mTwitterAPI;
    private Set<TweetListener> mListeners;

    public interface TweetListener {
        void onNewsLoaded(List<News> pItems);
    }

    public void register(TweetListener pTweetListener) {
        mListeners.add(pTweetListener);
    }

    public void unregister(TweetListener pTweetListener) {
        mListeners.remove(pTweetListener);
    }

    private TwitterDatabase mDatabase;
    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;
    private ViewTimelineDAO mViewTimelineDAO;

    public TwitterRepository(Application pApplication,
                             EventBus pEventBus,
                             TaskManager pTaskManager,
                             TwitterManager pTwitterManager,
                             TwitterAPI pTwitterAPI,
                             TwitterDatabase pDatabase,
                             Config pConfig)
    {
        super();
        mConfig = pConfig;
        mEventBus = pEventBus;
        mEventBus.registerListener(this);
        mTaskManager = pTaskManager;
        mTwitterManager = pTwitterManager;
        mTwitterAPI = pTwitterAPI;

        mDatabase = pDatabase;
        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mListeners = new HashSet<TwitterRepository.TweetListener>();
    }

    public Observable<News> findLatestTweets(Timeline pTimeline) throws TwitterAccessException {
        List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        pTimeline.appendOldItems(lResult);
        lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        pTimeline.appendNewItems(lResult);
        return lResult;
    }

    public List<News> findLatestTweets(Timeline pTimeline) throws TwitterAccessException {
        List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        pTimeline.appendOldItems(lResult);
        lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        pTimeline.appendNewItems(lResult);
        return lResult;
    }

    public List<News> findOlderTweets(Timeline pTimeline) throws TwitterAccessException {
        List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        pTimeline.appendOldItems(lResult);
        return lResult;
    }

    private List<News> findTweets(TimeGap pTimeGap) throws TwitterAccessException {
        Query<DB_TWITTER> lQuery = Query.on(DB_TWITTER.values())
                                        .selectAll(DB_TWITTER.VIEW_TIMELINE)
                                        .from(DB_TWITTER.VIEW_TIMELINE)
                                        .limit(DEFAULT_PAGE_SIZE);
        if (pTimeGap.isFutureGap()) {
            lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getOldestBound());
        } else if (pTimeGap.isPastGap()) {
            lQuery.whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getEarliestBound());
        } else if (!pTimeGap.isInitialGap()) {
            lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getOldestBound())
                  .whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getEarliestBound());
        }

        final List<News> lResult = new ArrayList<News>(DEFAULT_PAGE_SIZE);
        lQuery.execute(mDatabase.getWritableDatabase(), new ResultHandler.Handle() {
            public void handleRow(ResultHandler.Row pRow, Cursor pCursor) {
                switch (mViewTimelineDAO.getKind(pRow)) {
                case TWEET:
                    lResult.add(mViewTimelineDAO.getTweet(pRow));
                    break;
                case TIMEGAP:
                    lResult.add(mViewTimelineDAO.getTimeGap(pRow));
                    break;
                }
            }
        });
        for (TweetListener lListener : mListeners) {
            lListener.onNewsLoaded(lResult);
        }
        return lResult;
    }

    public Observable<List<News>> findTweetsInGap(final TimeGap pTimeGap) throws TwitterAccessException {
        return mTwitterAPI.getHome(pTimeGap, DEFAULT_PAGE_SIZE) //
                          .map(new Func1<Tweet, Tweet>() {
                              public Tweet call(Tweet pTweet) {
                                  mTweetDAO.create(pTweet);
                                  return pTweet;
                              }
                          })
                          .toList()
                          .map(new Func1<List<Tweet>, List<Tweet>>() {
                              public List<Tweet> call(List<Tweet> pTweets) {
                                  if (pTweets.size() == 0) {
                                      if (!pTimeGap.isFutureGap()) {
                                          mTimeGapDAO.delete(pTimeGap);
                                      }
                                  } else {
                                      for (Tweet lTweet : pTweets) {
                                          mTweetDAO.create(lTweet);
                                      }

                                      if (pTimeGap.isInitialGap()) {
                                          mTimeGapDAO.create(TimeGap.futureTimeGap(pTweets));
                                          mTimeGapDAO.create(TimeGap.pastTimeGap(pTweets));
                                      } else {
                                          TimeGap lRemainingTimeGap = pTimeGap.substract(pTweets, DEFAULT_PAGE_SIZE);
                                          mTimeGapDAO.update(lRemainingTimeGap);
                                      }
                                  }
                                  return pTweets;
                              }
                          });
        // .subscribe(new Action1<List<Tweet>>() {
        // public void call(List<Tweet> pTweets) {
        // final List<Timeline.Item> lItems = new ArrayList<Timeline.Item>(pTweets);
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        // }
        // }, null, null, null);
        // try {
        // mDatabase.executeInTransaction(new Runnable() {
        // public void run() {
        // if (lTweets.size() > 0) {
        // for (Tweet lTweet : lTweets) {
        // mTweetDAO.create(lTweet);
        // }
        //
        // if (pTimeGap.isInitialGap()) {
        // mTimeGapDAO.create(TimeGap.futureTimeGap(lTweets));
        // mTimeGapDAO.create(TimeGap.pastTimeGap(lTweets));
        // } else {
        // TimeGap lRemainingTimeGap = pTimeGap.substract(lTweets, DEFAULT_PAGE_SIZE);
        // mTimeGapDAO.update(lRemainingTimeGap);
        // }
        // } else {
        // if (!pTimeGap.isFutureGap()) {
        // mTimeGapDAO.delete(pTimeGap);
        // }
        // }
        // }
        // });
        // } catch (Exception eException) {
        // throw TwitterAccessException.from(eException);
        // }
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        return null;
    }

    public interface Config {
        String getHost();

        String getCallbackURL();
    }
}

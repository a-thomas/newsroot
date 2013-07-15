package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Application;
import android.database.Cursor;

import com.codexperiments.newsroot.common.event.EventBus;
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
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.robolabor.task.TaskManager;
import com.fasterxml.jackson.core.JsonParser;

public class TwitterRepository {
    private static final int DEFAULT_PAGE_SIZE = 20;

    private Config mConfig;
    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;
    private Set<TweetListener> mListeners;

    public interface TweetListener {
        void onNewsLoaded(List<Timeline.Item> pItems);
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
                             TwitterManager pTwitterManager,
                             TwitterDatabase pDatabase,
                             Config pConfig)
    {
        super();
        mConfig = pConfig;
        mEventBus = pEventBus;
        mEventBus.registerListener(this);
        mTwitterManager = pTwitterManager;

        mDatabase = pDatabase;
        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mListeners = new HashSet<TwitterRepository.TweetListener>();
    }

    public List<Timeline.Item> findLatestTweets(Timeline pTimeline) throws TwitterAccessException {
        List<Timeline.Item> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        pTimeline.appendOldItems(lResult);
        lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        pTimeline.appendNewItems(lResult);
        return lResult;
    }

    public List<Timeline.Item> findOlderTweets(Timeline pTimeline) throws TwitterAccessException {
        List<Timeline.Item> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        pTimeline.appendOldItems(lResult);
        return lResult;
    }

    private List<Timeline.Item> findTweets(TimeGap pTimeGap) throws TwitterAccessException {
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

        final List<Timeline.Item> lResult = new ArrayList<Timeline.Item>(DEFAULT_PAGE_SIZE);
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

    public List<Timeline.Item> findTweetsInGap(final TimeGap pTimeGap) throws TwitterAccessException {
        // mTaskManager.execute(new TaskAdapter<List<Tweet>>() {
        // @Override
        // public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
        // TwitterQuery lQuery = mTwitterManager.queryHome().withParam("count", DEFAULT_PAGE_SIZE);
        // if (!pTimeGap.isFutureGap()) {
        // lQuery.withParam("max_id", pTimeGap.getEarliestBound() - 1);
        // }
        // if (!pTimeGap.isPastGap()) {
        // lQuery.withParam("since_id", pTimeGap.getOldestBound());
        // }
        //
        // return mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception {
        // return TwitterParser.parseTweetList(pParser);
        // }
        // });
        // }
        // });
        // return null;

        //
        // mTaskManager.execute(new TaskAdapter<List<Tweet>>() {
        // @Override
        // public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
        // TwitterQuery lQuery = mTwitterManager.queryHome().withParam("count", DEFAULT_PAGE_SIZE);
        // if (!pTimeGap.isFutureGap()) {
        // lQuery.withParam("max_id", pTimeGap.getEarliestBound() - 1);
        // }
        // if (!pTimeGap.isPastGap()) {
        // lQuery.withParam("since_id", pTimeGap.getOldestBound());
        // }
        //
        // return mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception {
        // return TwitterParser.parseTweetList(pParser);
        // }
        // });
        // }
        // }).pipe(new TaskAdapter<List<Tweet>>() {
        // @Override
        // public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
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
        // }
        // }).pipe(new TaskAdapter<List<Tweet>>() {
        // @Override
        // public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        // }
        // });

        final TwitterQuery lQuery = mTwitterManager.queryHome().withParam("count", DEFAULT_PAGE_SIZE);
        if (!pTimeGap.isFutureGap()) {
            lQuery.withParam("max_id", pTimeGap.getEarliestBound() - 1);
        }
        if (!pTimeGap.isPastGap()) {
            lQuery.withParam("since_id", pTimeGap.getOldestBound());
        }
        // public Void parse(JsonParser pParser) throws Exception {
        // TwitterParser.parseTweetList(pParser).map(new Func1<Tweet, Tweet>() {
        // public Tweet call(Tweet pTweet) {
        // mTweetDAO.create(pTweet);
        // return pTweet;
        // }
        // }).toList().map(new Func1<List<Tweet>, List<Tweet>>() {
        // public List<Tweet> call(List<Tweet> pTweets) {
        // if (pTweets.size() == 0) {
        // if (!pTimeGap.isFutureGap()) {
        // mTimeGapDAO.delete(pTimeGap);
        // }
        // } else if (pTimeGap.isInitialGap()) {
        // mTimeGapDAO.create(TimeGap.futureTimeGap(pTweets));
        // mTimeGapDAO.create(TimeGap.pastTimeGap(pTweets));
        // } else {
        // TimeGap lRemainingTimeGap = pTimeGap.substract(pTweets, DEFAULT_PAGE_SIZE);
        // mTimeGapDAO.update(lRemainingTimeGap);
        // }
        // return pTweets;
        // }
        // }).subscribe(new Action1<List<Tweet>>() {
        // public void call(List<Tweet> pTweets) {
        // final List<Timeline.Item> lItems = new ArrayList<Timeline.Item>(pTweets);
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        // }
        // }, null, null, null);
        // return null;
        // }

        // Observable.from(mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception {
        // return TwitterParser.parseTweetList2(pParser);
        // }
        // }));
        Observable.create(new Func1<Observer<List<Tweet>>, Subscription>() {
            public Subscription call(Observer<List<Tweet>> pT1) {
                try {
                    pT1.onNext(mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
                        public List<Tweet> parse(JsonParser pParser) throws Exception {
                            return TwitterParser.parseTweetList2(pParser);
                        }
                    }));
                } catch (TwitterAccessException e) {
                    pT1.onError(e);
                }
                return Subscriptions.empty();
            }
        })
                  .subscribeOn(Schedulers.threadPoolForIO())
                  .observeOn(AndroidScheduler.threadPoolForDatabase())
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
                          return null;
                      }
                  })
                  .observeOn(AndroidScheduler.getInstance())
                  .subscribe(new Action1<List<Tweet>>() {
                      public void call(List<Tweet> pTweets) {
                          final List<Timeline.Item> lItems = new ArrayList<Timeline.Item>(pTweets);
                          for (TweetListener lListener : mListeners) {
                              lListener.onNewsLoaded(lItems);
                          }
                      }
                  }, new Action1<Exception>() {
                      public void call(Exception pT1) {
                      }
                  }, new Action0() {
                      public void call() {
                      }
                  });
        return null;

        //
        // mTwitterManager.query(lQuery, TwitterParser.parseTweetList()) //
        // .aggregate(new ArrayList<Tweet>(), new Func2<List<Tweet>, Tweet, List<Tweet>>() {
        // public List<Tweet> call(List<Tweet> pTweets, Tweet pTweet) {
        // mTweetDAO.create(pTweet);
        // pTweets.add(pTweet);
        // return pTweets;
        // }
        // })
        // .map(new Func1<Tweet, List<Tweet>>() {
        // public List<Tweet> call(Tweet pTweet) {
        // mTweetDAO.create(pTweet);
        // return null;
        // }
        // })
        // .toList()
        // .map(new Func1<List<Tweet>, List<Tweet>>() {
        // public List<Tweet> call(List<Tweet> pTweets) {
        // if (pTweets.size() == 0) {
        // if (!pTimeGap.isFutureGap()) {
        // mTimeGapDAO.delete(pTimeGap);
        // }
        // } else {
        // for (Tweet lTweet : pTweets) {
        // mTweetDAO.create(lTweet);
        // }
        //
        // if (pTimeGap.isInitialGap()) {
        // mTimeGapDAO.create(TimeGap.futureTimeGap(pTweets));
        // mTimeGapDAO.create(TimeGap.pastTimeGap(pTweets));
        // } else {
        // TimeGap lRemainingTimeGap = pTimeGap.substract(pTweets, DEFAULT_PAGE_SIZE);
        // mTimeGapDAO.update(lRemainingTimeGap);
        // }
        // }
        // return pTweets;
        // }
        // })
        // .subscribe(new Action1<List<Tweet>>() {
        // public void call(List<Tweet> pTweets) {
        // final List<Timeline.Item> lItems = new ArrayList<Timeline.Item>(pTweets);
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        // }
        // }, null, null, null);
        // return null;

        // TwitterQuery lQuery = mTwitterManager.queryHome().withParam("count", DEFAULT_PAGE_SIZE);
        // if (!pTimeGap.isFutureGap()) {
        // lQuery.withParam("max_id", pTimeGap.getEarliestBound() - 1);
        // }
        // if (!pTimeGap.isPastGap()) {
        // lQuery.withParam("since_id", pTimeGap.getOldestBound());
        // }
        //
        // final List<Tweet> lTweets = mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception {
        // return TwitterParser.parseTweetList(pParser);
        // }
        // });
        //
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
        //
        // final List<Timeline.Item> lItems = new ArrayList<Timeline.Item>(lTweets);
        // // new Handler(Looper.getMainLooper()).post(new Runnable() {
        // // public void run() {
        // for (TweetListener lListener : mListeners) {
        // lListener.onNewsLoaded(lItems);
        // }
        // // }
        // // });
        // return lItems;
    }

    public interface Config {
        String getHost();

        String getCallbackURL();
    }
}

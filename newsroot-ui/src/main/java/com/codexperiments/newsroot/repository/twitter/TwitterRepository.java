package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.util.TaskAdapter;
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
        mTaskManager.execute(new TaskAdapter<List<Tweet>>() {
            @Override
            public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
                TwitterQuery lQuery = mTwitterManager.queryHome().withParam("count", DEFAULT_PAGE_SIZE);
                if (!pTimeGap.isFutureGap()) {
                    lQuery.withParam("max_id", pTimeGap.getEarliestBound() - 1);
                }
                if (!pTimeGap.isPastGap()) {
                    lQuery.withParam("since_id", pTimeGap.getOldestBound());
                }

                return mTwitterManager.query(lQuery, new TwitterQuery.Handler<List<Tweet>>() {
                    public List<Tweet> parse(JsonParser pParser) throws Exception {
                        return TwitterParser.parseTweetList(pParser);
                    }
                });
            }
        }).pipe(new TaskAdapter<List<Tweet>>() {
            @Override
            public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
                try {
                    mDatabase.executeInTransaction(new Runnable() {
                        public void run() {
                            if (lTweets.size() > 0) {
                                for (Tweet lTweet : lTweets) {
                                    mTweetDAO.create(lTweet);
                                }

                                if (pTimeGap.isInitialGap()) {
                                    mTimeGapDAO.create(TimeGap.futureTimeGap(lTweets));
                                    mTimeGapDAO.create(TimeGap.pastTimeGap(lTweets));
                                } else {
                                    TimeGap lRemainingTimeGap = pTimeGap.substract(lTweets, DEFAULT_PAGE_SIZE);
                                    mTimeGapDAO.update(lRemainingTimeGap);
                                }
                            } else {
                                if (!pTimeGap.isFutureGap()) {
                                    mTimeGapDAO.delete(pTimeGap);
                                }
                            }
                        }
                    });
                } catch (Exception eException) {
                    throw TwitterAccessException.from(eException);
                }
            }
        }).pipe(new TaskAdapter<List<Tweet>>() {
            @Override
            public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception {
                for (TweetListener lListener : mListeners) {
                    lListener.onNewsLoaded(lItems);
                }
            }
        });

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
package com.codexperiments.rx;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import android.support.v4.app.Fragment;

public class RxAndroid {
    public static <TResult> Observable<TResult> from(Observable<TResult> pFindTweets, Fragment pFragment) {
        return Observable.create(new OnSubcribeFragmentSupport<TResult>(pFindTweets, pFragment));
    }

    private static class OnSubcribeFragmentSupport<TResult> implements OnSubscribeFunc<TResult> {
        private final Observable<TResult> mSource;
        private Fragment mFragment;

        public OnSubcribeFragmentSupport(Observable<TResult> pSource, Fragment pFragment) {
            super();
            mSource = pSource;
            mFragment = pFragment;
        }

        public Subscription onSubscribe(final Observer<? super TResult> pObserver) {
            return mSource.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TResult>() {
                public void onNext(TResult pResult) {
                    pObserver.onNext(pResult);
                }

                public void onCompleted() {
                    pObserver.onCompleted();
                }

                public void onError(Throwable pError) {
                    pObserver.onError(pError);
                }
            });
        }
    }
}

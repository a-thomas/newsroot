package com.codexperiments.newsroot.common.rx;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;
import android.view.View;
import android.widget.ListView;

import com.codexperiments.newsroot.ui.fragment.PageAdapter.RxRecycleCallback;

public class RxListBindListener implements RxRecycleCallback {
    private final ListView mListView;
    private final Map<Class<?>, Handler<?, ?>> mRefs;
    private final Map<Class<?>, Handler2<?>> mRefs2;

    public static RxListBindListener create(ListView pListView) {
        return new RxListBindListener(pListView);
    }

    protected RxListBindListener(ListView pListView) {
        mListView = pListView;
        mRefs = new HashMap<Class<?>, Handler<?, ?>>();
        mRefs2 = new HashMap<Class<?>, Handler2<?>>();
    }

    public <TView, TItem> Observable<ListEvent<TView, TItem>> register(Class<TView> pViewClass, Class<TItem> pItemClass) {
        Handler<TView, TItem> lHandler = new Handler<TView, TItem>(pItemClass);
        mRefs.put(pViewClass, lHandler);
        return lHandler.mSubject;
    }

    public <TView> Observable<TView> register(Class<TView> pViewClass) {
        Handler2<TView> lHandler = new Handler2<TView>(pViewClass);
        mRefs2.put(pViewClass, lHandler);
        return lHandler.mSubject;
    }

    @Override
    public void onRecycle(int pPosition, View pView, Object pItem) {
        // Handler<?, ?> lHandler = mRefs.get(pView.getClass());
        // if (lHandler != null) {
        // lHandler.bind(pPosition, pView, pItem);
        // }
        Handler2<?> lHandler = mRefs2.get(pView.getClass());
        if (lHandler != null) {
            lHandler.bind(pPosition, pView);
        }
    }

    private static final class Handler<TView, TItem> {
        final Class<TItem> mItemClass;
        final PublishSubject<ListEvent<TView, TItem>> mSubject;

        Handler(Class<TItem> pItemClass) {
            mItemClass = pItemClass;
            mSubject = PublishSubject.create();
        }

        @SuppressWarnings("unchecked")
        void bind(int pPosition, View pView, Object pItem) {
            if (mItemClass.isInstance(pItem)) {
                mSubject.onNext(new ListEvent<TView, TItem>(pPosition, (TView) pView, (TItem) pItem));
            }
        }
    }

    private static final class Handler2<TView> {
        final Class<TView> mViewClass;
        final PublishSubject<TView> mSubject;

        Handler2(Class<TView> pViewClass) {
            mViewClass = pViewClass;
            mSubject = PublishSubject.create();
        }

        void bind(int pPosition, View pView) {
            if (mViewClass.isInstance(pView)) {
                mSubject.onNext(mViewClass.cast(pView));
            }
        }
    }
}

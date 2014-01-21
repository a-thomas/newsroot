// package com.codexperiments.rx;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import rx.Observable;
// import rx.subjects.PublishSubject;
// import android.view.View;
// import android.widget.ListView;
//
// import com.codexperiments.newsroot.common.rx.ListEvent;
// import com.codexperiments.newsroot.ui.fragment.PageAdapter.RxRecycleCallback;
//
// public class RxListBinder implements RxRecycleCallback {
// private final ListView mListView;
// private final Map<Class<?>, Handler<?, ?>> mRefs;
//
// public static RxListBinder create(ListView pListView) {
// return new RxListBinder(pListView);
// }
//
// protected RxListBinder(ListView pListView) {
// mListView = pListView;
// mRefs = new HashMap<Class<?>, Handler<?, ?>>();
// }
//
// public <TView, TItem> Observable<TItem> register(Class<TView> pViewClass, Class<TItem> pItemClass) {
// Handler<TView, TItem> lHandler = new Handler<TView, TItem>(pItemClass);
// mRefs.put(pViewClass, lHandler);
// return null; // lHandler.mSubject;
// }
//
// @Override
// public void onRecycle(int pPosition, View pView, Object pItem) {
// Handler<?, ?> lHandler = mRefs.get(pView.getClass());
// if (lHandler != null) {
// lHandler.bind(pPosition, pView, pItem);
// }
// }
//
// private static final class Handler<TView, TItem> {
// final Class<TItem> mItemClass;
// final PublishSubject<ListEvent<TView, TItem>> mSubject;
//
// Handler(Class<TItem> pItemClass) {
// mItemClass = pItemClass;
// mSubject = PublishSubject.create();
// }
//
// @SuppressWarnings("unchecked")
// void bind(int pPosition, View pView, Object pItem) {
// if (mItemClass.isInstance(pItem)) {
// mSubject.onNext(new ListEvent<TView, TItem>(pPosition, (TView) pView, (TItem) pItem));
// }
// }
// }
// }
//
// // package com.codexperiments.rx;
// //
// // import java.util.HashMap;
// // import java.util.Map;
// //
// // import rx.Observable;
// // import rx.subjects.PublishSubject;
// // import android.view.View;
// // import android.widget.ListView;
// //
// // import com.codexperiments.newsroot.ui.fragment.PageAdapter.RxRecycleCallback;
// //
// // public class RxListBinder implements RxRecycleCallback {
// // private final ListView mListView;
// // private final Map<Class<?>, Handler<?>> mHandlers;
// //
// // public static RxListBinder create(ListView pListView) {
// // return new RxListBinder(pListView);
// // }
// //
// // protected RxListBinder(ListView pListView) {
// // mListView = pListView;
// // mHandlers = new HashMap<Class<?>, Handler<?>>();
// // }
// //
// // public <TView> Observable<TView> register(Class<TView> pViewClass) {
// // Handler<TView> lHandler = new Handler<TView>(pViewClass);
// // mHandlers.put(pViewClass, lHandler);
// // return lHandler.mSubject;
// // }
// //
// // @Override
// // public void onRecycle(int pPosition, View pView, Object pItem) {
// // Handler<?> lHandler = mHandlers.get(pView.getClass());
// // if (lHandler != null) {
// // lHandler.bind(pPosition, pView);
// // }
// // }
// //
// // private static final class Handler<TView> {
// // final Class<TView> mViewClass;
// // final PublishSubject<TView> mSubject;
// //
// // Handler(Class<TView> pViewClass) {
// // mViewClass = pViewClass;
// // mSubject = PublishSubject.create();
// // }
// //
// // void bind(int pPosition, View pView) {
// // if (mViewClass.isInstance(pView)) {
// // mSubject.onNext(mViewClass.cast(pView));
// // }
// // }
// // }
// // }

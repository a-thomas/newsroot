// package com.codexperiments.newsroot.presentation;
//
// import com.codexperiments.newsroot.common.rx.Command;
// import com.codexperiments.newsroot.common.rx.Property2;
// import com.codexperiments.newsroot.domain.twitter.Tweet;
//
// public class TweetPresentation {
// // public static class Model implements NewsPresentation {
// // public Tweet mTweet;
// // public Boolean mSelected;
// //
// // public Model(Tweet pTweet) {
// // this(pTweet, Boolean.FALSE);
// // }
// //
// // public Model(Tweet pTweet, Boolean pSelected) {
// // super();
// // mTweet = pTweet;
// // mSelected = pSelected;
// // }
// // }
// //
// // public Model mModel;
//
// // private Property2<Boolean> mSelectedProperty;
// // private Command<Void, Boolean> mToggleSelection= Command.create(Property2.toggle(mSelectedProperty));
//
// // public TweetPresentation(Model pTweetPresentationModel) {
// // mModel = pTweetPresentationModel;
// // mSelectedProperty = Property2.create(new PropertyProxy<Boolean>() {
// // public Boolean get() {
// // return mModel.mSelected;
// // }
// //
// // public void set(Boolean pValue) {
// // mModel.mSelected = pValue;
// // }
// // });
// // mToggleSelection = Command.create(Property2.toggle(mSelectedProperty));
// // mToggleSelection.subscribe(mSelectedProperty);
// // }
//
// // public void bind(Model pModel) {
// // mModel = pModel;
// // mSelectedProperty.set(mModel.mSelected);
// // }
//
// public Tweet getTweet() {
// return mModel.mTweet;
// }
//
// public Command<Void, ?> toggleSelection() {
// return mToggleSelection;
// }
//
// public Property2<Boolean> isSelected() {
// return mSelectedProperty;
// }
// }

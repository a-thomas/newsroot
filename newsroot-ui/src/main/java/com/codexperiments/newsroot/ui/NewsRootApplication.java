package com.codexperiments.newsroot.ui;

import com.codexperiments.newsroot.common.BaseApplication;

public class NewsRootApplication extends BaseApplication
{
    // private Configuration configuration;

    // public static void start(Activity activity) {
    // if (activity != null) {
    // Application application = activity.getApplication();
    // if ((application != null) && (application instanceof NewsRootApplication)) {
    // ((NewsRootApplication) application).onStart();
    // return;
    // }
    // }
    // throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    // }
    //
    // public static void stop(Activity activity) {
    // if (activity != null) {
    // Application application = activity.getApplication();
    // if ((application != null) && (application instanceof NewsRootApplication)) {
    // ((NewsRootApplication) application).onStop();
    // return;
    // }
    // }
    // throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    // }
    //
    // public static Configuration getConfigurationFrom(Activity activity) {
    // if (activity != null) {
    // Application application = activity.getApplication();
    // if ((application != null) && (application instanceof BaseApplication)) {
    // return ((NewsRootApplication) application).configuration;
    // }
    // }
    // throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    // }

    // public NewsRootApplication() {
    // super();
    // configuration = null;
    // }

    // protected abstract Configuration onCreateConfiguration();

    @Override
    public void onCreate()
    {
        super.onCreate();
        // configuration = onCreateConfiguration();

        // registerService(Platform.Factory.findCurrentPlatform(this));
        // registerService(new URLService(this));
        // registerService(new AuthenticationService(this));
        // registerService(new FollowerService(this, getService(AuthenticationService.class)));
        // registerService(new ImageService(this,
        // getService(URLService.class),
        // getResources().getDrawable(R.drawable.default_image),
        // getResources().getDrawable(R.drawable.default_image),
        // getResources().getDrawable(R.drawable.default_image)));
        // registerService(new NotificationService(this));
        // registerService(new QuizService(configuration));
        // registerService(new RubricService(this, configuration));
        // registerService(new PushService(this));
        // registerService(ArticlesService.getInstance(this));
        // try {
        // registerService(OpenHelperManager.getHelper(this, ArticlesDatabaseHelper.class).getArticlesManager());
        // } catch (SQLException eSQLException) {
        // // TODO Handle application crash more properly.
        // // Logger.error(this, eSQLException);
        // }
        //
        // TODO Ugly. Put in ArticleService constructor!!
        // getService(ArticlesService.class).setWebServicesUrl(configuration.getWebServicesUrl());

        // onStart();
    }

    // public void onStart() {
    // // Ensures the database is opened.
    // try {
    // OpenHelperManager.getHelper(this, ArticlesDatabaseHelper.class).getArticlesManager();
    // } catch (SQLException eSQLException) {
    // // Logger.error(this, eSQLException);
    // }
    // }

    // public void onStop() {
    // getServiceFrom(this, NotificationService.class).stop();
    // getServiceFrom(this, URLService.class).purgeCache();
    // OpenHelperManager.releaseHelper();
    // }
}

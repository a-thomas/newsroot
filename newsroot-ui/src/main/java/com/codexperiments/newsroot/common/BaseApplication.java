package com.codexperiments.newsroot.common;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

import com.codexperiments.newsroot.common.event.AndroidEventBus;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.platform.Platform;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.robolabor.task.android.TaskManagerAndroid;
import com.codexperiments.robolabor.task.android.TaskManagerConfigAndroid;
import com.codexperiments.robolabor.task.handler.Task;

public abstract class BaseApplication extends Application {
    private List<Object> mServices;

    public static <TService> TService getServiceFrom(Activity activity, Class<TService> serviceType) {
        if (activity != null) {
            Application application = activity.getApplication();
            if ((application != null) && (application instanceof BaseApplication)) {
                return ((BaseApplication) application).getService(serviceType);
            }
        }
        throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    }

    protected static <TService> TService getServiceFrom(Application application, Class<TService> serviceType) {
        if ((application != null) && (application instanceof BaseApplication)) {
            return ((BaseApplication) application).getService(serviceType);
        }
        throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    }

    public BaseApplication() {
        super();
        mServices = new LinkedList<Object>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
        // StrictMode.setThreadPolicy(new ThreadPolicy.Builder().detectAll().build());
        // StrictMode.setVmPolicy(new VmPolicy.Builder().detectAll().build());
        // }

        TwitterDatabase lDatabase = new TwitterDatabase(this);

        registerService(Platform.Factory.findCurrentPlatform(this));
        registerService(new AndroidEventBus());
        registerService(new TaskManagerAndroid(this, new TaskManagerConfigAndroid(this) {
            @Override
            public boolean keepResultOnHold(Task<?> pTask) {
                // if (pTask) {
                return true;
                // }
                // return super.keepResultOnHold(pTask);
            }
        }));
        registerService(new TwitterManager(this, getService(EventBus.class), lDatabase, new TwitterManager.Config() {
            public String getHost() {
                return "https://api.twitter.com/";
            }

            public String getConsumerKey() {
                return "3Ng9QGTB7EpZCHDOIT2jg";
            }

            public String getConsumerSecret() {
                return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
            }

            public String getCallbackURL() {
                return "oauth://newsroot-callback";
            }
        }));
        registerService(new TwitterRepository(this,
                                              getService(EventBus.class),
                                              getService(TwitterManager.class),
                                              lDatabase,
                                              new TwitterRepository.Config() {
                                                  public String getHost() {
                                                      return "https://api.twitter.com/";
                                                  }

                                                  public String getCallbackURL() {
                                                      return "oauth://newsroot-callback";
                                                  }
                                              }));
    }

    public void registerService(Object service) {
        mServices.add(service);
    }

    /**
     * TODO Update comment Supprime un service en fonction de son type (ou type dont il h�rite ou qu'il impl�mente). Il est
     * vivement conseill� d'utiliser une interface pour �viter une d�pendance vers une impl�mentation sp�cifique.
     * 
     * Attention: si plusieurs classes instance de serviceType existe, tous sont supprim�s.
     * 
     * @param serviceType Type du service � supprimer.
     */
    public void unregisterService(Class<?> serviceType) {
        for (Object iservice : mServices) {
            if (serviceType.isInstance(iservice)) {
                mServices.remove(iservice);
            }
        }
    }

    /**
     * R�cup�re un service selon son type. Il est vivement conseill� d'utiliser une interface pour �viter une d�pendance vers une
     * impl�mentation sp�cifique.
     * 
     * Attention: si plusieurs classes instance de serviceType existe, le premier enregistr� est retourn�.
     * 
     * @param <TService>
     * @param serviceType Type de service � r�cup�rer.
     * @return Service du type demand�.
     * @throws UnknownServiceException Si le service n'est pas enregistr�.
     */
    @SuppressWarnings("unchecked")
    public <TService> TService getService(Class<TService> serviceType) {
        for (Object service : mServices) {
            if (serviceType.isInstance(service)) {
                return (TService) service;
            }
        }
        throw new UnknownServiceException("%1$s is not a registered service.", serviceType.getName());
    }
}

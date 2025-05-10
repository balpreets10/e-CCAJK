package com.mycca.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {
    public static final String ENGLISH = "en";
    public static final String HINDI = "hi";

//    private static LocaleHelper _instance;
//
//    private LocaleHelper() {
//        _instance = this;
//    }
//
//    public static LocaleHelper getInstance() {
//        if (_instance == null) {
//            return new LocaleHelper();
//        } else {
//            return _instance;
//        }
//    }

    public static Context setLocale(Context context) {
        String lang = Preferences.getInstance().getStringPref(context, Preferences.PREF_LANGUAGE);
        return updateResources(context, lang);
    }

    private static Context updateResources(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        CustomLogger.getInstance().logVerbose("Config initial = " + config, CustomLogger.Mask.LOCALE_HELPER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
            context = context.createConfigurationContext(config);
        } else {
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        CustomLogger.getInstance().logVerbose("Config new = " + config, CustomLogger.Mask.LOCALE_HELPER);
        return context;
    }
}

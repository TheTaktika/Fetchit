package org.example.ui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageManager {

    private static final LanguageManager instance = new LanguageManager();
    private ResourceBundle bundle;
    private Locale currentLocale;

    private LanguageManager() {
        setLocale(Locale.of("ru"));
    }

    public static LanguageManager getInstance() {
        return instance;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
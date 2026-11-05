package es.unican.bicis;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;
import es.unican.bicis.common.TlsSupport;

/**
 * The Application class must be explicitly annotated for the dependency injection to work
 */
@HiltAndroidApp
public class BicisApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TlsSupport.install(this);
    }

}
package es.unican.bicis.utils;

import android.app.UiAutomation;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;

public class FeatureManager {

    /**
     * Auxiliary method to allow changing the GPS setting in the tested device
     * @param option true to enable the setting, false to disable it
     */
    public static void setGPS(boolean option) {
        LocationManager locationManager = (LocationManager) InstrumentationRegistry
                .getInstrumentation().getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if ((gpsEnabled && !option) || (!gpsEnabled && option)) {
            String command;

            if (option) {
                command = "settings put secure location_mode 3";
            } else {
                command = "settings put secure location_mode 0";
            }

            UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            try (ParcelFileDescriptor pfd = automation.executeShellCommand(command)) {
                pfd.checkError();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // wait for the GPS service to be effectively enabled/disabled
            boolean set = false;
            while (!set) {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (gpsEnabled == option) {
                    set = true;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Auxiliary method to allow changing the networking setting in the tested device
     * @param option true to enable the setting, false to disable it
     */
    public static void setNetworking(boolean option) {
        // airplane mode has no manager class for the sake of developers frustration
        boolean airplaneEnabled = Settings.Global.getInt(InstrumentationRegistry
                        .getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

        // might as well run in airplane mode than deal with the non-standardized
        // and frankly overcomplicated way of checking carrier data mode on android
        if (!airplaneEnabled) {
            String command = "cmd connectivity airplane-mode enable";

            UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            try (ParcelFileDescriptor pfd = automation.executeShellCommand(command)) {
                pfd.checkError();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // then we set up the appropriate Wi-Fi setting
        WifiManager wifiManager = (WifiManager) InstrumentationRegistry
                .getInstrumentation().getTargetContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();

        if ((wifiEnabled && !option) || (!wifiEnabled && option)) {
            String command;

            if (option) {
                command = "svc wifi enable";
            } else {
                command = "svc wifi disable";
            }

            UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            try (ParcelFileDescriptor pfd = automation.executeShellCommand(command)) {
                pfd.checkError();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // wait for the Wi-Fi service to be effectively enabled/disabled
            boolean set = false;
            while (!set) {
                wifiEnabled = wifiManager.isWifiEnabled();

                if (wifiEnabled == option) {
                    set = true;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // wait for the Wi-Fi service to be effectively connected/disconnected from a network
            boolean ready = false;
            while (!ready) {
                ConnectivityManager connectivityManager = (ConnectivityManager) InstrumentationRegistry
                        .getInstrumentation().getTargetContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                Network activeNetwork = connectivityManager.getActiveNetwork();

                if (!option) {
                    if (activeNetwork == null) {
                        ready = true;
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

                    if (capabilities != null) {
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                            ready = true;
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}

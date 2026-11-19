package es.unican.bicis.utils;

import android.app.UiAutomation;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;
import java.util.function.BooleanSupplier;

public class FeatureManager {

    private static final long TIMEOUT_MS = 10_000L;
    private static final long POLL_INTERVAL_MS = 100L;

    private FeatureManager() {
        // Utility class
    }

    /**
     * Auxiliary method to allow changing the GPS setting in the tested device
     *
     * @param option true to enable the setting, false to disable it
     */
    public static void setGPS(boolean option) {
        LocationManager locationManager = (LocationManager) InstrumentationRegistry
                .getInstrumentation().getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (gpsEnabled != option) {
            String command = option
                    ? "settings put secure location_mode 3"
                    : "settings put secure location_mode 0";

            executeShellCommand(command);

            // wait for the GPS service to be effectively enabled/disabled
            waitUntil(
                    () -> locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == option,
                    "Timeout waiting for GPS provider to become " + option
            );
        }
    }

    /**
     * Auxiliary method to allow changing the networking setting in the tested device
     *
     * @param option true to enable the setting, false to disable it
     */
    public static void setNetworking(boolean option) {
        WifiManager wifiManager = (WifiManager) InstrumentationRegistry
                .getInstrumentation().getTargetContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();

        if (wifiEnabled != option) {
            String command = option ? "svc wifi enable" : "svc wifi disable";
            executeShellCommand(command);

            // wait for the Wi-Fi service to be effectively enabled/disabled
            waitUntil(
                    () -> wifiManager.isWifiEnabled() == option,
                    "Timeout waiting for Wi-Fi service to become " + option
            );

            // wait for the Wi-Fi service to be effectively connected/disconnected from a network
            waitUntil(
                    () -> isNetworkReady(option, false),
                    "Timeout waiting for Wi-Fi network connection state: " + option
            );
        }
    }

    /**
     * Auxiliary method to allow changing the airplane mode setting in the tested device
     *
     * @param option true to enable the setting, false to disable it
     */
    public static void setAirplaneMode(boolean option) {
        // airplane mode has no manager class for the sake of developers frustration
        boolean airplaneEnabled = Settings.Global.getInt(InstrumentationRegistry
                        .getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

        // might as well run tests in airplane mode than deal with the non-standardized
        // and frankly overcomplicated way of checking carrier data mode on android
        if (airplaneEnabled != option) {
            String command = option
                    ? "cmd connectivity airplane-mode enable"
                    : "cmd connectivity airplane-mode disable";

            executeShellCommand(command);

            // wait for networking to be effectively available/unavailable
            waitUntil(
                    () -> isNetworkReady(!option, true),
                    "Timeout waiting for airplane mode network state: " + option
            );
        }
    }

    private static boolean isNetworkReady(boolean expectedConnected, boolean requireValidated) {
        ConnectivityManager connectivityManager = (ConnectivityManager) InstrumentationRegistry
                .getInstrumentation().getTargetContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();

        if (!expectedConnected) {
            return activeNetwork == null;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) {
            return false;
        }

        boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (requireValidated) {
            return hasInternet && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        return hasInternet;
    }

    private static void executeShellCommand(String command) {
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        try (ParcelFileDescriptor pfd = automation.executeShellCommand(command)) {
            pfd.checkError();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute shell command: " + command, e);
        }
    }

    private static void waitUntil(BooleanSupplier condition, String timeoutMessage) {
        long deadline = SystemClock.elapsedRealtime() + TIMEOUT_MS;
        while (!condition.getAsBoolean()) {
            if (SystemClock.elapsedRealtime() >= deadline) {
                throw new IllegalStateException(timeoutMessage);
            }
            SystemClock.sleep(POLL_INTERVAL_MS);
        }
    }
}
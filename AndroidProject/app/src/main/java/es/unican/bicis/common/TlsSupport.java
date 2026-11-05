package es.unican.bicis.common;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import es.unican.bicis.R;

/**
 * Adds the root certificates that are missing from the trust store of old Android versions.
 *
 * Android versions before 7.1.1 do not include the Let's Encrypt ISRG Root X1, so HTTPS
 * connections to servers using Let's Encrypt fail with a certificate validation error even
 * when the device has a working internet connection. This class combines the system trust
 * store with the bundled ISRG Root X1, keeping normal certificate and hostname validation
 * enabled.
 */
public final class TlsSupport {

    private static final String TAG = "TlsSupport";

    /** Root certificates that may be missing from old devices' system trust stores. */
    private static final int[] EXTRA_ROOTS = {
            R.raw.isrg_root_x1
    };

    private static SSLSocketFactory sslSocketFactory;
    private static X509TrustManager trustManager;

    private TlsSupport() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Installs a process-wide SSL socket factory that trusts the system CAs plus the
     * bundled roots. Calling this method more than once has no effect.
     *
     * @param context application context used to read the bundled certificates
     */
    public static synchronized void install(Context context) {
        if (sslSocketFactory != null) {
            return;
        }

        try {
            X509TrustManager systemTrustManager = createTrustManager(null);
            X509TrustManager extraTrustManager = createTrustManager(loadExtraRoots(context));
            X509TrustManager combined = new CombinedTrustManager(systemTrustManager, extraTrustManager);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{combined}, new SecureRandom());

            sslSocketFactory = sslContext.getSocketFactory();
            trustManager = combined;

            // osmdroid uses HttpsURLConnection for the map tiles. This also affects other
            // HTTPS connections made by the app that do not explicitly set their own factory.
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            // If anything goes wrong, keep the system defaults instead of breaking HTTPS.
            Log.w(TAG, "Could not add the bundled root certificates; using system defaults", e);
        }
    }

    /**
     * @return the socket factory installed by {@link #install(Context)}, or null if
     *         installation was not possible
     */
    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * @return the trust manager installed by {@link #install(Context)}, or null if
     *         installation was not possible
     */
    public static X509TrustManager getTrustManager() {
        return trustManager;
    }

    private static KeyStore loadExtraRoots(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Resources resources = context.getResources();
        for (int resourceId : EXTRA_ROOTS) {
            try (InputStream input = resources.openRawResource(resourceId)) {
                X509Certificate certificate =
                        (X509Certificate) certificateFactory.generateCertificate(input);
                keyStore.setCertificateEntry(certificate.getSubjectX500Principal().getName(),
                        certificate);
            }
        }
        return keyStore;
    }

    private static X509TrustManager createTrustManager(KeyStore keyStore) throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);

        for (TrustManager manager : factory.getTrustManagers()) {
            if (manager instanceof X509TrustManager) {
                return (X509TrustManager) manager;
            }
        }
        throw new IllegalStateException("No X509TrustManager available");
    }

    /**
     * Trusts a server certificate if it is valid according to either the system trust
     * store or the bundled roots.
     */
    private static final class CombinedTrustManager implements X509TrustManager {

        private final X509TrustManager systemTrustManager;
        private final X509TrustManager extraTrustManager;

        private CombinedTrustManager(X509TrustManager systemTrustManager,
                                     X509TrustManager extraTrustManager) {
            this.systemTrustManager = systemTrustManager;
            this.extraTrustManager = extraTrustManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            systemTrustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            try {
                systemTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                extraTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] systemIssuers = systemTrustManager.getAcceptedIssuers();
            X509Certificate[] extraIssuers = extraTrustManager.getAcceptedIssuers();
            X509Certificate[] allIssuers =
                    new X509Certificate[systemIssuers.length + extraIssuers.length];
            System.arraycopy(systemIssuers, 0, allIssuers, 0, systemIssuers.length);
            System.arraycopy(extraIssuers, 0, allIssuers, systemIssuers.length,
                    extraIssuers.length);
            return allIssuers;
        }
    }
}

package es.unican.bicis.repository;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import es.unican.bicis.common.TlsSupport;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service singletons for the <a href="https://api.citybik.es/v2/">CityBikes API</a>
 */
public class CityBikesService {

    private CityBikesService() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /** CityBikes API base URL */
     static final String BASE_URL = "https://api.citybik.es/";

    /** Retrofit object for the CityBikes API */
    public static final Retrofit retrofit;

    /** Object to access the CityBikes API */
    public static final ICityBikesAPI api;

    static {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // Use the trust manager that includes the roots missing from old Android versions.
        SSLSocketFactory sslSocketFactory = TlsSupport.getSslSocketFactory();
        X509TrustManager trustManager = TlsSupport.getTrustManager();
        if (sslSocketFactory != null && trustManager != null) {
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        }

        OkHttpClient client = clientBuilder
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "Bicis-2026/1.0")
                        .build()))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ICityBikesAPI.class);
    }

}

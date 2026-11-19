package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.injection.RepositoriesModule;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;

@UninstallModules(RepositoriesModule.class)
@HiltAndroidTest
public class FiltrarBicisElectricasErrorUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    private ActivityScenario<StationsView> scenario;

    /** Motivo del fallo que devolverá el repositorio simulado (cada test lo fija). */
    private String mensajeError = "Error en la conexión a internet";

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /** Repositorio simulado que devuelve error al solicitar la red. */
    @BindValue
    INetworksRepository networksRepository = new INetworksRepository() {
        @Override
        public void requestNetworks(NetworksCallback cb) {
            cb.onSuccess(Collections.emptyList());
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            cb.onFailure(new Throwable(mensajeError));
        }
    };

    /** Repositorio simulado de favoritas. */
    @BindValue
    IFavoriteStationsRepository favoriteStationsRepository = new IFavoriteStationsRepository() {
        @Override
        public boolean addFavorite(Station station, String networkId, Location location, String alias) { return true; }
        @Override
        public boolean addFavorite(String stationId, String alias) { return true; }
        @Override
        public boolean isFavorite(String stationId) { return false; }
        @Override
        public Map<String, String> getFavorites() { return Map.of(); }
        @Override
        public List<FavoriteStation> getFavoriteStations() { return Collections.emptyList(); }
    };

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, StationsView.class);
        Network network = new Network();
        network.setId("springbike");
        network.setName("SpringBike");
        intent.putExtra("INTENT_NETWORK", Parcels.wrap(network));
        return intent;
    }

    /**
     * N2-08: Simular fallo de conexión en INetworksRepository al cargar StationsView.
     */
    @Test
    public void testN2_08_errorConexionInternet() {
        mensajeError = "Error en la conexión a internet";
        scenario = ActivityScenario.launch(createIntent());

        onView(withId(R.id.tvResults)).check(matches(withText("Error cargando estaciones")));
        onView(withId(R.id.tvResults)).check(matches(isDisplayed()));
    }

    /**
     * N2-09: Simular fallo de API en INetworksRepository al cargar StationsView.
     */
    @Test
    public void testN2_09_errorServidorAPI() {
        mensajeError = "Error 500: Fallo del servidor";
        scenario = ActivityScenario.launch(createIntent());

        onView(withId(R.id.tvResults)).check(matches(withText("Error cargando estaciones")));
        onView(withId(R.id.tvResults)).check(matches(isDisplayed()));
    }
}

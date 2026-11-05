package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static es.unican.bicis.utils.Matchers.withStationName;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.parceler.Parcels;

import java.util.ArrayList;
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
public class AnadirEstacionFavoritaErrorPersistenciaUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<StationsView> activityRule = new ActivityScenarioRule<>(createIntent());

    // Variable para almacenar el decorView
    private View decorView;

    @Before
    public void setUp() {
        // Obtenemos el decorView
        activityRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

    private static Intent createIntent() {
        // Limpiamos SharedPreferences para asegurar un entorno de prueba limpio
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("favorite_stations_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        Intent intent = new Intent(context, StationsView.class);

        // Creamos la red de prueba para pasarla como parametro en el intent, y que el test se
        // ejecute con la lista de estaciones de springbike ya cargada
        Network network = new Network();
        network.setId("springbike");
        network.setName("SpringBike");

        // Empaquetamos con Parcels
        intent.putExtra("INTENT_NETWORK", Parcels.wrap(network));

        return intent;
    }

    /**
     * Repositorio de prueba de redes simulado
     */
    @BindValue
    INetworksRepository networksRepository = new INetworksRepository() {
        @Override
        public void requestNetworks(NetworksCallback cb) {
            cb.onSuccess(Collections.emptyList());
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            Network network = new Network();
            network.setId("springbike");
            network.setName("SpringBike");

            List<Station> stations = new ArrayList<>();
            Station s1 = new Station();
            s1.setId("sp-001");
            s1.setName("Capitol Complex");
            stations.add(s1);

            network.setStations(stations);
            cb.onSuccess(network);
        }
    };

    /**
     * Repositorio de prueba simulado: devuelve siempre false en addFavorite para
     * simular que falla la persistencia.
     */
    @BindValue
    IFavoriteStationsRepository favoriteStationsRepository = new IFavoriteStationsRepository() {
        @Override
        public boolean addFavorite(Station station, String networkId, Location location, String alias) {
            return false;
        }

        @Override
        public boolean addFavorite(String stationId, String alias) {
            return false;
        }

        @Override
        public boolean isFavorite(String stationId) {
            return false;
        }

        @Override
        public Map<String, String> getFavorites() {
            return Collections.emptyMap();
        }

        @Override
        public List<FavoriteStation> getFavoriteStations() {
            return Collections.emptyList();
        }
    };

    /**
     * UI-03 AnadirEstacionFavoritaErrorPersistenciaUITest: Verifica que cuando un error de persistencia ocurre, la
     * estacion no aparece como favorita.
     */
    @Test
    public void anadirEstacionFavoritaErrorPersistencia () {
        // Comprobamos que se ha cargado la lista de estaciones
        onView(withId(R.id.lvStations)).check(matches(isDisplayed()));

        // Pulsamos la estrella de Capitol Complex
        onData(withStationName("Capitol Complex"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .perform(click());

        // Comprobamos que en el dialogo aparece el texto indicado
        onView(withText("Guardar como favorita")).check(matches(isDisplayed()));
        // Introducimos el alias deseado
        onView(withId(R.id.etAlias)).perform(typeText("Casa"), ViewActions.closeSoftKeyboard());
        // Pulsamos el boton de aceptar
        onView(withId(R.id.btnAccept)).perform(click());

        // Comprobar que aparece el mensaje de error en el toast
        onView(withText(R.string.favorite_error_persistence))
                .inRoot(RootMatchers.withDecorView(not(decorView)))
                .check(matches(isDisplayed()));

        // Comprobamos que la estrella de Capitol Complex aparece sin rellenar
        onData(withStationName("Capitol Complex"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .check(matches(withTagValue(is(android.R.drawable.btn_star_big_off))));

        // Recreamos la actividad y comprobamos que la estrella sigue sin aparecer rellenada
        activityRule.getScenario().recreate();

        onData(withStationName("Capitol Complex"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .check(matches(withTagValue(is(android.R.drawable.btn_star_big_off))));

        // Por ultimo, verificamos que la estacion no se haya guardado como favorita
        assertFalse(favoriteStationsRepository.isFavorite("sp-001"));

    }
}

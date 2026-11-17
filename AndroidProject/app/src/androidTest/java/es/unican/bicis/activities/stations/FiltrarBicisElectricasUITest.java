package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static es.unican.bicis.utils.Matchers.withStationName;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;

import org.junit.After;
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
import es.unican.bicis.model.StationExtra;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;

@UninstallModules(RepositoriesModule.class)
@HiltAndroidTest
public class FiltrarBicisElectricasUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    private ActivityScenario<StationsView> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /** Repositorio simulado de redes para las distintas pruebas de filtrado. */
    @BindValue
    INetworksRepository networksRepository = new INetworksRepository() {
        @Override
        public void requestNetworks(NetworksCallback cb) {
            cb.onSuccess(Collections.emptyList());
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            Network network = new Network();
            network.setId(networkId);

            Location location = new Location();

            if ("springbike".equals(networkId)) {
                network.setName("SpringBike");
                location.setCity("Springfield, Illinois");
                location.setCountry("US");
                network.setLocation(location);

                List<Station> stations = new ArrayList<>();
                stations.add(createStation("sp-001", "Capitol Complex", 12, 4));
                stations.add(createStation("sp-002", "Lincoln Home Visitor Center", 6, 0));
                stations.add(createStation("sp-003", "Union Station", 3, 2));
                stations.add(createStation("sp-004", "Washington Park", 1, 0));
                network.setStations(stations);
                cb.onSuccess(network);
            } else if ("rivercycle".equals(networkId)) {
                network.setName("RiverCycle");
                location.setCity("River City");
                location.setCountry("US");
                network.setLocation(location);

                List<Station> stations = new ArrayList<>();
                stations.add(createStation("rv-001", "River Station 1", 5, 0));
                stations.add(createStation("rv-002", "River Station 2", 2, 0));
                network.setStations(stations);
                cb.onSuccess(network);
            } else if ("northcycleuk".equals(networkId)) {
                network.setName("NorthCycle");
                location.setCity("Northampton");
                location.setCountry("UK");
                network.setLocation(location);

                List<Station> stations = new ArrayList<>();
                Station s1 = new Station();
                s1.setId("nc-001");
                s1.setName("Abington Street");
                s1.setFreeBikes(8);

                Station s2 = new Station();
                s2.setId("nc-002");
                s2.setName("Northampton Railway Station");
                s2.setFreeBikes(2);

                stations.add(s1);
                stations.add(s2);
                network.setStations(stations);
                cb.onSuccess(network);
            } else {
                cb.onFailure(new Throwable("Red no encontrada"));
            }
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

    private static Intent createIntent(String networkId, String networkName) {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("favorite_stations_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        Intent intent = new Intent(context, StationsView.class);
        Network network = new Network();
        network.setId(networkId);
        network.setName(networkName);
        intent.putExtra("INTENT_NETWORK", Parcels.wrap(network));
        return intent;
    }

    private static Station createStation(String id, String name, int freeBikes, int ebikes) {
        Station s = new Station();
        s.setId(id);
        s.setName(name);
        s.setFreeBikes(freeBikes);
        s.setEmptySlots(5);
        s.setTimestamp("2026-10-16T09:15:42.000000+00:00Z");
        StationExtra extra = new StationExtra();
        extra.setEbikes(ebikes);
        s.setExtra(extra);
        return s;
    }

    private void abrirMenuFiltros() {
        onView(withId(R.id.menuItemFilters)).perform(click());
        onView(withText(R.string.filterEbikes)).perform(click());
    }

    /**
     * N2-01: Filtrar estaciones con bicis eléctricas disponibles en SpringBike.
     */
    @Test
    public void testN2_01_filtrarDisponibles() {
        scenario = ActivityScenario.launch(createIntent("springbike", "SpringBike"));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 2")));
        onView(withId(R.id.ivEbike)).check(matches(isDisplayed()));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));

        onData(withStationName("Capitol Complex"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Capitol Complex")));
        onData(withStationName("Union Station"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Union Station")));
    }

    /**
     * N2-02: Filtrar estaciones sin bicis eléctricas disponibles en SpringBike.
     */
    @Test
    public void testN2_02_filtrarNoDisponibles() {
        scenario = ActivityScenario.launch(createIntent("springbike", "SpringBike"));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesNotAvailable)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 2")));
        onView(withId(R.id.ivEbike)).check(matches(isDisplayed()));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(isDisplayed()));

        onData(withStationName("Lincoln Home Visitor Center"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Lincoln Home Visitor Center")));
        onData(withStationName("Washington Park"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Washington Park")));
    }

    /**
     * N2-03: Combinar y desactivar opciones de filtro en SpringBike.
     */
    @Test
    public void testN2_03_combinarYDesactivarOpciones() {
        scenario = ActivityScenario.launch(createIntent("springbike", "SpringBike"));

        // 1. Marcar "Disponible"
        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 2")));
        onView(withId(R.id.ivEbike)).check(matches(isDisplayed()));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));

        // 2. Marcar "No disponible" (ambos marcados)
        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesNotAvailable)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 4")));
        onView(withId(R.id.ivEbike)).check(matches(not(isDisplayed())));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));

        // 3. Desmarcar "Disponible" (solo "No disponible" marcado)
        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 2")));
        onView(withId(R.id.ivEbike)).check(matches(isDisplayed()));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(isDisplayed()));

        // 4. Desmarcar "No disponible" (ambos desmarcados)
        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesNotAvailable)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 4")));
        onView(withId(R.id.ivEbike)).check(matches(not(isDisplayed())));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));
    }

    /**
     * N2-04: Lista vacía sin coincidencias en RiverCycle.
     */
    @Test
    public void testN2_04_listaVaciaSinCoincidencias() {
        scenario = ActivityScenario.launch(createIntent("rivercycle", "RiverCycle"));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 0")));
        onView(withId(R.id.ivEbike)).check(matches(isDisplayed()));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));
    }

    /**
     * N2-05: Cierre del desplegable al pulsar fuera.
     */
    @Test
    public void testN2_05_cierreDesplegableAlPulsarFuera() {
        scenario = ActivityScenario.launch(createIntent("springbike", "SpringBike"));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.tvResults)).check(matches(isDisplayed()));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).check(matches(isChecked()));
        Espresso.pressBack();
    }

    /**
     * N2-06: Reinicio de filtro al recrear la actividad.
     */
    @Test
    public void testN2_06_reinicioFiltroAlReiniciarApp() {
        scenario = ActivityScenario.launch(createIntent("springbike", "SpringBike"));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).perform(click());
        Espresso.pressBack();

        scenario.recreate();

        onView(withId(R.id.ivEbike)).check(matches(not(isDisplayed())));
        onView(withId(R.id.ivEbikeNotAvailable)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 4")));

        abrirMenuFiltros();
        onView(withId(R.id.cbEbikesAvailable)).check(matches(not(isChecked())));
        Espresso.pressBack();
    }

    /**
     * N2-07: Red sin información de bicicletas eléctricas en NorthCycle.
     */
    @Test
    public void testN2_07_redSinInformacionEbikes() {
        scenario = ActivityScenario.launch(createIntent("northcycleuk", "NorthCycle"));

        onView(withId(R.id.menuItemFilters)).perform(click());
        onView(allOf(withClassName(endsWith("ListMenuItemView")), hasDescendant(withText(R.string.filterEbikes))))
                .check(matches(not(isEnabled())));
        Espresso.pressBack();

        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 2")));
    }
}

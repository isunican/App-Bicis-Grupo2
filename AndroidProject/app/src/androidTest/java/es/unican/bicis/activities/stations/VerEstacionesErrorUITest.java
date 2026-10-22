package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.RepositoriesModule;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;

@UninstallModules(RepositoriesModule.class)
@HiltAndroidTest
public class VerEstacionesErrorUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    /** Motivo del fallo que devolverá el repositorio simulado (cada test lo fija). */
    private String mensajeError = "Error en el acceso de datos";

    /**
     * Repositorio de prueba: devuelve la lista de redes correctamente,
     * pero falla al pedir los datos de una red (y por tanto sus estaciones).
     */
    @BindValue
    INetworksRepository repository = new INetworksRepository() {
        @Override
        public void requestNetworks(NetworksCallback cb) {
            Location location = new Location();
            location.setCity("Springfield, Illinois");
            location.setCountry("US");

            Network network = new Network();
            network.setId("springbike");
            network.setName("SpringBike");
            network.setLocation(location);

            List<Network> networks = new ArrayList<>();
            networks.add(network);
            cb.onSuccess(networks);
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            cb.onFailure(new Throwable(mensajeError));
        }
    };

    /**
     * Matcher personalizado para buscar una red por su atributo name
     * dentro del adapter, evitando problemas con toString().
     */
    private static Matcher<Object> withNetworkName(String name) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                if (item instanceof Network) {
                    Network net = (Network) item;
                    return name.equals(net.getName());
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Network with name: " + name);
            }
        };
    }

    /**
     * UI-04: Error en el acceso a los datos -> Se muestra el error en la pantalla de estaciones.
     */
    @Test
    public void testUI04_errorAccesoDatos() {
        onView(withId(R.id.btnBuscar)).perform(click());
        onData(withNetworkName("SpringBike"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvResults)).check(matches(withText("Error cargando estaciones")));
    }

    /**
     * UI-05: Error en la conexión a la red -> Se muestra el error en la pantalla de estaciones.
     */
    @Test
    public void testUI05_errorConexionRed() {
        mensajeError = "Error en la conexión a internet";

        onView(withId(R.id.btnBuscar)).perform(click());
        onData(withNetworkName("SpringBike"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvResults)).check(matches(withText("Error cargando estaciones")));
    }
}
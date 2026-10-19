package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.model.Network;

@HiltAndroidTest
public class VerEstacionesExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

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
     * UI-01: Selección "SpringBike" -> Se muestran todas sus estaciones.
     */
    @Test
    public void testUI01_verEstacionesSpringBike() {
        onData(withNetworkName("SpringBike"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvNetworkLocation)).check(matches(withText("US Springfield, Illinois")));
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 5")));

        onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(0)
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Capitol Complex")));
        onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(0)
                .onChildView(withId(R.id.tvUpdateDate))
                .check(matches(withText("Última fecha de actualización: 16/10/2026 09:15")));
    }

    /**
     * UI-02: Selección "SouthCycle" -> Red sin estaciones.
     */
    @Test
    public void testUI02_verEstacionesSouthCycleSinEstaciones() {
        onData(withNetworkName("SouthCycle"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvNetworkLocation)).check(matches(withText("UK Southampton")));
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 0")));
    }

    /**
     * UI-03: Selección "Bike4fun" -> Una estación sin fecha de actualización.
     */
    @Test
    public void testUI03_verEstacionesBike4funSinFecha() {
        onData(withNetworkName("Bike4fun"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvNetworkLocation)).check(matches(withText("Tripoli")));
        onView(withId(R.id.tvResults)).check(matches(withText("Resultados: 3")));

        onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(2)
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Estación sin datos")));
        onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(2)
                .onChildView(withId(R.id.tvUpdateDate))
                .check(matches(withText("Última fecha de actualización: Sin fecha")));
    }
}

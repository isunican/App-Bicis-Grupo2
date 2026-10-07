package es.unican.bicis.activities.details;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
public class DetailsViewUITest {

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
     * N1-01: Selección "SpringBike" -> Se muestran todos los detalles.
     */
    @Test
    public void testN1_01_verDetallesCompletosSpringBike() {
        onData(withNetworkName("SpringBike"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvName)).check(matches(withText("SpringBike")));
        onView(withId(R.id.tvCity)).check(matches(withText("Springfield, Illinois")));
        onView(withId(R.id.tvCountry)).check(matches(withText("US")));
        onView(withId(R.id.tvCompany)).check(matches(withText("Ballard Technologies")));
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
    }

    /**
     * N1-02: Selección "NorthCycle" -> Muestra detalles disponibles y omite faltantes.
     */
    @Test
    public void testN1_02_verDetallesParcialesNorthCycle() {
        onData(withNetworkName("NorthCycle"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvName)).check(matches(withText("NorthCycle")));
        onView(withId(R.id.tvCity)).check(matches(withText("Northampton")));
        onView(withId(R.id.tvCountry)).check(matches(withText("UK")));
        onView(withId(R.id.tvCompany)).check(matches(withText("Northampton Council Management Co. Ltd")));
    }

    /**
     * N1-03: Selección "SouthCycle" -> Se muestran solo detalles válidos.
     */
    @Test
    public void testN1_03_verDetallesValidosSouthCycle() {
        onData(withNetworkName("SouthCycle"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvName)).check(matches(withText("SouthCycle")));
        onView(withId(R.id.tvCity)).check(matches(withText("Southampton")));
        onView(withId(R.id.tvCountry)).check(matches(withText("UK")));
        onView(withId(R.id.tvCompany)).check(matches(withText("JCDecaux SE, Grundon Waste Management Ltd")));
    }

    /**
     * N1-04: Selección "Bike4fun" -> Detalles no disponibles.
     */
    @Test
    public void testN1_04_detallesNoDisponiblesBike4fun() {
        onData(withNetworkName("Bike4fun"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        onView(withId(R.id.tvName)).check(matches(withText("Bike4fun")));
        onView(withId(R.id.tvCity)).check(matches(withText("Tripoli")));
        onView(withId(R.id.tvCompany)).check(matches(withText("Desconocida")));
    }
}
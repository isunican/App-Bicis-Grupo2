package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasToString;

import static es.unican.bicis.activities.main.CountryMatcher.withCountryName;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;

@HiltAndroidTest
public class SeleccionarSoloPaisUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    @Test
    public void seleccionarSoloPais() {
        // 1. Seleccionar "ES" (España)
        onView(withId(R.id.spCountry)).perform(click());
        onData(withCountryName("España")).perform(click());

        // 2. Pulsar buscar sin seleccionar ciudad
        onView(withId(R.id.btnBuscar)).perform(click());

        // 3. Verificar que se muestran 3 resultados en la etiqueta
        onView(withId(R.id.tvResults)).check(matches(withText(containsString("3"))));

        // 4. Verificar que las redes mostradas son las correctas (Bicing, BiciMAD, TUEBICI)
        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(0)
                .onChildView(withId(R.id.tvName)).check(matches(withText("Bicing")));
        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(1)
                .onChildView(withId(R.id.tvName)).check(matches(withText("BiciMAD")));
        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(2)
                .onChildView(withId(R.id.tvName)).check(matches(withText("TUeBICI")));
    }
}

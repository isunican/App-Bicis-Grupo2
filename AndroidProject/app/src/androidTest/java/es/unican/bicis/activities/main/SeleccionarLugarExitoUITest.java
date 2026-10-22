package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;

import static es.unican.bicis.activities.main.CountryMatcher.withCountryName;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.model.Country;

@HiltAndroidTest
public class SeleccionarLugarExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    /**
     * N1-01 SeleccionarLugarExitoUITest: Verifica las redes en Espanha, Santander
     */
    @Test
    public void seleccionarLugarExito() {
        onView(withId(R.id.spCountry)).perform(click());
        onData(withCountryName("España")).perform(click());

        onView(withId(R.id.spCity)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Santander"))).perform(click());

        onView(withId(R.id.btnBuscar)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(0)
                .onChildView(withId(R.id.tvName))
                .check(matches(withText("TUeBICI")));
    }

}

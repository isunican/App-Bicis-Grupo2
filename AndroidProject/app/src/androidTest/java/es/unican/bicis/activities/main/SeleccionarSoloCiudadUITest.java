package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;

@HiltAndroidTest
public class SeleccionarSoloCiudadUITest {
    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    /**
     * N1-03 SeleccionarSoloCiudadUITest: Verifica las redes en Northampton
     */
    @Test
    public void seleccionarSoloCiudad() {
        onView(withId(R.id.spCity)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Northampton"))).perform(click());

        onView(withId(R.id.btnBuscar)).perform(click());

        onView(withId(R.id.tvResults)).check(matches(withText(containsString("1"))));

        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(0)
                .onChildView(withId(R.id.tvName))
                .check(matches(withText("NorthCycle")));
    }
}

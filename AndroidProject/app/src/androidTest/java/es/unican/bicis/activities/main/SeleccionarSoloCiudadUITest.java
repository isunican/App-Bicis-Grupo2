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

    @Test
    public void seleccionarSoloCiudad() {
        // 1. Seleccionar la ciudad "Northampton" sin tocar el país
        onView(withId(R.id.spCity)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Northampton"))).perform(click());

        // 2. Pulsar buscar
        onView(withId(R.id.btnBuscar)).perform(click());

        // 3. Verificar que se muestra 1 único resultado
        onView(withId(R.id.tvResults)).check(matches(withText(containsString("1"))));

        // 4. Verificar que la red mostrada es North Cycle
        onData(anything()).inAdapterView(withId(R.id.lvNetworks)).atPosition(0)
                .onChildView(withId(R.id.tvName))
                .check(matches(withText("NorthCycle")));
    }
}

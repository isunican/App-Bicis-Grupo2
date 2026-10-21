package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.injection.RepositoriesModule;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;

/**
 * N1-04 ErrorAccesoDatosUITest: Verifica cuando hay error en el acceso de datos
 */
@UninstallModules(RepositoriesModule.class)
@HiltAndroidTest
public class ErrorAccesoDatosUITest {
    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    // Se fuerza manualmente que no pueda acceder a los datos
    @BindValue
    INetworksRepository repository = new INetworksRepository() {
        @Override
        public void requestNetworks(NetworksCallback cb) {
            cb.onFailure(new Throwable("Error en el acceso de datos"));
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            cb.onFailure(new Throwable("Error en el acceso de datos"));
        }
    };

    @Test
    public void errorAccesoDatosMuestraPanel() {

        onView(withId(R.id.btnBuscar)).perform(click());

        onView(withText(containsString("Ha habido un error cargando las redes")))
                .check(matches(isDisplayed()));
    }
}

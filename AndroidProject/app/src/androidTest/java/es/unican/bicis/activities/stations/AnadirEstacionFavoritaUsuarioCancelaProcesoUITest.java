package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static es.unican.bicis.utils.Matchers.withStationName;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.parceler.Parcels;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.FavoriteStationsRepository;

@HiltAndroidTest
public class AnadirEstacionFavoritaUsuarioCancelaProcesoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // Lanzamos StationsView directamente pasándole la red SpringBike en el Intent
    @Rule(order = 1)
    public ActivityScenarioRule<StationsView> activityRule = new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        // Limpiamos SharedPreferences para asegurar un entorno de prueba limpio
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("favorite_stations_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        Intent intent = new Intent(context, StationsView.class);

        // Creamos la red de prueba para pasarla como parametro en el intent, y que el test se
        // ejecuten con la lista de estaciones de springbike ya cargada
        Network network = new Network();
        network.setId("springbike");
        network.setName("SpringBike");

        // Empaquetamos con Parcels
        intent.putExtra("INTENT_NETWORK", Parcels.wrap(network));

        return intent;
    }

    /**
     * UI-03 AnadirEstacionFavoritaUsuarioCancelaProcesoUITest: Verifica que cuando el usuario pulsa la "X", el proceso
     * de guardar una estacion como favorita es cancelado exitosamente.
     */
    @Test
    public void anadirEstacionFavoritaUsuarioCancelaProceso () {
        // Comprobamos que la lista de estaciones esta visible
        onView(withId(R.id.lvStations)).check(matches(isDisplayed()));

        // Pulsamos la estrella de Capitol Complex
        onData(withStationName("Capitol Complex"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .perform(click());

        // Comprobamos que en el dialogo aparece el texto indicado
        onView(withText("Guardar como favorita")).check(matches(isDisplayed()));

        // Pulsamos la "X" para cancelar el proceso
        onView(withId(R.id.ibCloseDialog)).perform(click());
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
        Context context = ApplicationProvider.getApplicationContext();
        FavoriteStationsRepository repo = new FavoriteStationsRepository(context);

        assertFalse(repo.isFavorite("sp-001"));
    }

}

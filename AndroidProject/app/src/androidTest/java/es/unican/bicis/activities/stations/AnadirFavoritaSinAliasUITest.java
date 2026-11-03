package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static es.unican.bicis.utils.Matchers.withStationName;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
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
public class AnadirFavoritaSinAliasUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<StationsView> activityRule = new ActivityScenarioRule<>(createIntent());

    // Variable para almacenar el decorView
    private View decorView;

    @Before
    public void setUp() {
        // Obtenemos el objeto decorView
        activityRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());

        // Limpiamos SharedPreferences para asegurar un entorno de prueba limpio
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("favorite_stations_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, StationsView.class);

        // Creamos la red de prueba para pasarla como parametro en el intent, y que el test se
        // ejecute con la lista de estaciones de springbike ya cargada
        Network network = new Network();
        network.setId("springbike");
        network.setName("SpringBike");

        // Empaquetamos con Parcels
        intent.putExtra("INTENT_NETWORK", Parcels.wrap(network));

        return intent;
    }

    /**
     * UI-02 AnadirFavoritaSinAliasUITest: Verifica que una estacion es guardada como favorita
     * sin un alias exitosamente.
     */
    @Test
    public void anadirFavoritaSinAlias () {
        // Comprobamos que se ha cargado la lista de estaciones
        onView(withId(R.id.lvStations)).check(matches(isDisplayed()));

        // Pulsamos la estrella de Union Station
        onData(withStationName("Union Station"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .perform(click());

        // Comprobamos que en el dialogo aparece el texto indicado
        onView(withText("Guardar como favorita")).check(matches(isDisplayed()));
        // No introducimos ningun alias dentro del campo de texto
        onView(withId(R.id.etAlias)).perform(typeText(""), ViewActions.closeSoftKeyboard());
        // Pulsamos el boton de aceptar
        onView(withId(R.id.btnAccept)).perform(click());

        // Comprobar que aparece el mensaje de exito en el toast
        onView(withText(R.string.favorite_added_success))
                .inRoot(RootMatchers.withDecorView(not(decorView)))
                .check(matches(isDisplayed()));

        // Comprobamos que la estrella de Capitol Complex aparece rellenada
        onData(withStationName("Union Station"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .check(matches(withTagValue(is(android.R.drawable.btn_star_big_on))));

        // Recreamos la actividad y comprobamos que la estrella sigue apareciendo rellenada
        activityRule.getScenario().recreate();

        onData(withStationName("Union Station"))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.ivFavorite))
                .check(matches(withTagValue(is(android.R.drawable.btn_star_big_on))));

        // Por ultimo, verificamos que la estacion se ha guardado realmente como favorita con
        // el nombre de la estacion como alias (comportamiento por defecto cuando no se introduce alias).
        Context context = ApplicationProvider.getApplicationContext();
        FavoriteStationsRepository repo = new FavoriteStationsRepository(context);

        assertTrue(repo.isFavorite("sp-003"));
        assertEquals("Union Station", repo.getFavorites().get("sp-003"));

    }
}

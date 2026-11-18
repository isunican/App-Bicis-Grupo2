package es.unican.bicis.activities.favorites;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import com.google.gson.Gson;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.utils.FeatureManager;

@HiltAndroidTest
public class SuggestedStationsUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // prevent the location permissions dialog from showing up
    // by confirming the permission beforehand
    @Rule(order = 1)
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // manually manage the activity
    private ActivityScenario<MainView> activityScenario;

    @Before
    public void setup() {
        setLocalData();
        FeatureManager.setGPS(true);
        FeatureManager.setNetworking(true);
        activityScenario = ActivityScenario.launch(MainView.class);
    }

    @After
    public void close() {
        if (activityScenario != null) {
            activityScenario.close();
        }
    }

    /**
     * Test UI-01:
     * "Mostrar 5 alternativas con éxito y filtrado de coordenadas (Criterios A1-01 y A1-06)"
     */
    @Test
    public void testUI_01_estacionesAlternativasExito() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Trabajo")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 1")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa A")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa B")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa C")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa D")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa E")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
    }

    /**
     * Test UI-02:
     * "Existen menos de 5 alternativas. (Criterio A1-02)"
     */
    @Test
    public void testUI_02_estacionesAlternativasInsuficientes() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(1)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Gimnasio")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(1)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 2")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(1)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(1)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa 1")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa 2")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsKmDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
    }

    /**
     * Test UI-03:
     * "Lista de alternativas vacía (Criterio A1-03)"
     */
    @Test
    public void testUI_03_estacionesAlternativasVacia() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(0)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Casa")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(0)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 3")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(0)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(0)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        onView(withId(R.id.tvAlternativesEmpty)).check(matches(isDisplayed()));
        onView(withId(R.id.tvAlternativesEmpty)).check(matches(withText(R.string.alternativesEmpty)));
    }

    /**
     * Test UI-04:
     * "GPS del dispositivo desactivado (Criterio A1-04)"
     */
    @Test
    public void testUI_04_estacionesAlternativasSinGPS() {
        FeatureManager.setGPS(false);

        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Trabajo")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 1")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa A")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsString("- km"))));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(0)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa B")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsString("- km"))));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(1)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa C")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsString("- km"))));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(2)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa D")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsString("- km"))));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(3)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeName)).check(matches(withText("Alternativa E")));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsString("- km"))));
        onData(anything()).inAdapterView(withId(R.id.lvAlternatives)).atPosition(4)
                .onChildView(withId(R.id.tvAlternativeDistances)).check(
                        matches(withText(containsMetersDistance())));
    }

    /**
     * Test UI-05:
     * "Estación Favorita sin coordenadas (Criterio A1-05)"
     */
    @Test
    public void testUI_05_estacionesAlternativasFavSinCoordenadas() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(2)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Parque")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(2)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 4")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(2)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(2)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        // message toast is assumed for technical reasons
        onView(withId(R.id.tvAlternativesTitle)).check(doesNotExist());
    }

    /**
     * Test UI-06:
     * "Fallo de conexión de red (Criterio A1-07)"
     */
    @Test
    public void testUI_06_estacionesAlternativasFalloRed() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Trabajo")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 1")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));

        FeatureManager.setNetworking(false);
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.ivSearchAlternatives)).perform(click());

        // message toast is assumed for technical reasons
        onView(withId(R.id.tvAlternativesTitle)).check(doesNotExist());
    }

    /**
     * Auxiliary method to clean the user data cache and fill it with example favorite stations
     * as specified in the test plan for this user story.
     */
    private void setLocalData() {
        String PREF_NAME = "favorite_stations_prefs";

        // clear the user data
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();

        // add favorite stations to the user data
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        FavoriteStation fav1 = new FavoriteStation("Fav_1", "redabundante",
                "Trabajo", "Favorita 1", "ES", "Santander");
        FavoriteStation fav2 = new FavoriteStation("Fav_2", "redescasa",
                "Gimnasio", "Favorita 2", "ES", "Torrelavega");
        FavoriteStation fav3 = new FavoriteStation("Fav_3", "redvacia",
                "Casa", "Favorita 3", "ES", "Laredo");
        FavoriteStation fav4 = new FavoriteStation("Fav_4", "redsincoordenadas",
                "Parque", "Favorita 4", "ES", "Comillas");

        editor.putString(fav1.getStationId(), new Gson().toJson(fav1));
        editor.putString(fav2.getStationId(), new Gson().toJson(fav2));
        editor.putString(fav3.getStationId(), new Gson().toJson(fav3));
        editor.putString(fav4.getStationId(), new Gson().toJson(fav4));
        editor.commit();
    }

    /**
     * Auxiliary method to allow checking if a string contains a valid distance value in kilometers
     * @return the matcher handler
     */
    private Matcher<String> containsKmDistance() {
        return new CustomTypeSafeMatcher<String>("text contains distance in km") {
            @Override
            protected boolean matchesSafely(String text) {
                // regex for dummies: the string contains a number, followed by " km"
                // the dot is a delimiter, not the presence of an actual dot in the string
                return text.matches(".*\\d+ km.*");
            }
        };
    }

    /**
     * Auxiliary method to allow checking if a string contains a valid distance value in meters
     * @return the matcher handler
     */
    private Matcher<String> containsMetersDistance() {
        return new CustomTypeSafeMatcher<String>("text contains distance in m") {
            @Override
            protected boolean matchesSafely(String text) {
                // regex for dummies: the string contains a number, followed by " m"
                // the dot is a delimiter, not the presence of an actual dot in the string
                return text.matches(".*\\d+ m.*");
            }
        };
    }
}

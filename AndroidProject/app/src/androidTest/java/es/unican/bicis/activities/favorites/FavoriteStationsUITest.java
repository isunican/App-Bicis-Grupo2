package es.unican.bicis.activities.favorites;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;

@HiltAndroidTest
public class FavoriteStationsUITest {
    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule = new ActivityScenarioRule<>(MainView.class);

    private static final String PREF_NAME = "favorite_stations_prefs";

    /**
     * Test N1-01:
     * Seleccionar "Ver estaciones favoritas" en el desplegable del icono de la estrella.
     * (el usuario tiene favoritos)
     */
    @Test
    public void testN1_01_usuarioTieneFavoritos() {
        // clear the user data
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();

        // add favorite stations to the user data
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("sp-001", "City Center");
        editor.putString("sp-004", "Park Area");
        editor.putString("nc-001", "Grandma House");
        editor.commit();

        // perform the test
        // note: alphabetically ordered list by default
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(0)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("City Center")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(1)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Grandma House")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(2)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Park Area")));

        onView(withId(R.id.lvFavoriteStations)).check(matches(withListSize(3)));
        onView(withId(R.id.tvResults)).check(matches(withText(containsString("3"))));
    }

    /**
     * Test N1-02:
     * Seleccionar "Ver estaciones favoritas" en el desplegable del icono de la estrella.
     * (el usuario no tiene favoritos)
     */
    @Test
    public void testN1_02_usuarioNoTieneFavoritos() {
        // clear the user data
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();

        // perform the test
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).perform(click());

        onView(withId(R.id.lvFavoriteStations)).check(matches(withListSize(0)));
        onView(withId(R.id.tvResults)).check(matches(withText(containsString("0"))));
    }

    /**
     * Test N1-03:
     * Seleccionar el icono de la estrella y acto seguido, pulsar en cualquier otra parte.
     * (el usuario cancela el proceso)
     */
    @Test
    public void testN1_03_usuarioCancelaProceso() {
        // clear the user data
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();

        // perform the test
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).check(matches(isDisplayed()));
        Espresso.pressBack();
        onView(withText("Ver estaciones favoritas")).check(doesNotExist());

        // check that the other features in this view continue to work
        Espresso.openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText("Info")).check(matches(isDisplayed()));
        Espresso.pressBack();

        // check that the popup list appears again
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).check(matches(isDisplayed()));
    }

    /**
     * Test N1-04:
     * Seleccionar "Ver estaciones favoritas" en el desplegable del icono de la estrella.
     * (sucede un fallo de acceso en la persistencia de los datos)
     */
    @Test
    public void testN1_04_falloPersistenciaDatos() {
        // clear the user data
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();

        // store incorrect data assumed as data corruption
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("sp-001", true);
        editor.commit();

        // perform the test
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).perform(click());
        onView(withText("Error")).inRoot(isDialog()).check(matches(isDisplayed()));

        // confirm the error dialog and go back
        onView(withText(android.R.string.ok)).perform(click());
        Espresso.pressBack();

        // check that the other features in this view continue to work
        Espresso.openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText("Info")).check(matches(isDisplayed()));
        Espresso.pressBack();

        // check that the popup list appears again
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText("Ver estaciones favoritas")).check(matches(isDisplayed()));
    }

    /**
     * Matcher method that allows checking the number of items inside a ListView
     * @param expectedSize the expected number of elements
     * @return Matcher reference object
     */
    private Matcher<View> withListSize(int expectedSize) {
        return new BoundedMatcher<View, ListView>(ListView.class) {
            @Override
            public boolean matchesSafely(ListView listView) {
                return listView.getCount() == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ListView with " + expectedSize + " items");
            }
        };
    }
}

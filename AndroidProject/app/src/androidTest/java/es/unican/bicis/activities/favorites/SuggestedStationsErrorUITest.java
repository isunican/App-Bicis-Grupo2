package es.unican.bicis.activities.favorites;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.RepositoriesModule;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;
import es.unican.bicis.utils.FeatureManager;

@UninstallModules(RepositoriesModule.class)
@HiltAndroidTest
public class SuggestedStationsErrorUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // manually manage the activity
    private ActivityScenario<MainView> activityScenario;

    /**
     * Stub the network repository to simulate an access error
     */
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

    /**
     * Stub the favorite stations local data storage to be initially empty
     */
    @BindValue
    IFavoriteStationsRepository favoriteStationsRepository = new IFavoriteStationsRepository() {
        @Override
        public boolean addFavorite(Station station, String networkId, Location location, String alias) { return true; }
        @Override
        public boolean addFavorite(String stationId, String alias) { return true; }
        @Override
        public boolean isFavorite(String stationId) { return false; }
        @Override
        public Map<String, String> getFavorites() { return Map.of(); }
        @Override
        public List<FavoriteStation> getFavoriteStations() { return Collections.emptyList(); }
    };

    @Before
    public void setup() {
        SuggestedStationsUITest.setLocalData();
        FeatureManager.setAirplaneMode(true);
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
     * Test UI-07:
     * "Error en el acceso a datos (Criterio A1-08)"
     */
    @Test
    public void testUI_07_estacionesAlternativasErrorAccesoDatos() {
        onView(withId(R.id.menuItemFavorites)).perform(click());
        onView(withText(R.string.viewFavoriteStations)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvAlias)).check(matches(withText("Trabajo")));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.tvOriginalName)).check(matches(withText("Favorita 1")));

        // for ease of UI implementation, the magnifier icon is present but hidden
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .check(matches(hasDescendant(withId(R.id.ivSearchAlternatives))));
        onData(anything()).inAdapterView(withId(R.id.lvFavoriteStations)).atPosition(3)
                .onChildView(withId(R.id.ivSearchAlternatives))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}

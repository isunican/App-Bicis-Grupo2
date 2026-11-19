package es.unican.bicis.activities.favorites;

import android.app.AlertDialog;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.R;
import es.unican.bicis.model.AlternativeStation;
import es.unican.bicis.model.Availability;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;

/**
 * View that shows the list of the user's favorite stations.
 */
@AndroidEntryPoint
public class FavoriteStationsView extends AppCompatActivity implements IFavoriteStationsContract.View {

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository networksRepository;

    /** The adapter of the list of favorite stations */
    private FavoriteStationsArrayAdapter adapter;

    /** The presenter of this view */
    private IFavoriteStationsContract.Presenter presenter;

    /** The favorite stations currently shown (used to look up aliases) */
    private List<FavoriteStation> favoriteStations = new ArrayList<>();

    private static final double DEMO_LATITUDE = 43.4716;
    private static final double DEMO_LONGITUDE = -3.8007;

    /**
     * Creates the favorites screen, initializes its toolbar, location support and presenter.
     *
     * @param savedInstanceState previously saved activity state, or {@code null}
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stations);

        // instantiate presenter and launch initial business logic
        presenter = new FavoriteStationsPresenter();
        presenter.init(this);
    }

    /**
     * Initializes the toolbar and screen title.
     *
     * @see IFavoriteStationsContract.View#init()
     */
    @Override
    public void init() {
        // The toolbar is explicitly declared in the layout, so set it as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);  // show back button in action bar
            bar.setTitle(R.string.favoriteStationsTitle);
        }
    }

    /**
     * Creates and returns the repository used to access locally saved favorite stations.
     *
     * @return favorite-stations repository
     * @see IFavoriteStationsContract.View#getFavoriteStationsRepository()
     */
    @Override
    public IFavoriteStationsRepository getFavoriteStationsRepository() {
        return new FavoriteStationsRepository(getApplicationContext());
    }

    /**
     * Displays the saved favorite stations in the list and updates the empty-state display.
     *
     * @param favorites saved favorite stations to display
     * @see IFavoriteStationsContract.View#showFavoriteStations(List)
     */
    @Override
    public void showFavoriteStations(List<FavoriteStation> favorites) {
        this.favoriteStations = favorites;
        ListView list = findViewById(R.id.lvFavoriteStations);
        adapter = new FavoriteStationsArrayAdapter(this, favorites,
                favorite -> presenter.onSearchAlternativesClicked(favorite));
        list.setAdapter(adapter);

        TextView tvResults = findViewById(R.id.tvResults);
        tvResults.setText("Favoritos: " + favorites.size());
    }

    /**
     * Displays a message indicating that favorite stations could not be read or saved.
     *
     * @see IFavoriteStationsContract.View#showPersistenceError()
     */
    @Override
    public void showPersistenceError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.errorTitulo)
                .setMessage(R.string.loadErrorLocal)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Returns the repository used to request bike-network information.
     *
     * @return networks repository
     * @see IFavoriteStationsContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return networksRepository;
    }

    /**
     * Updates the availability indicators for the saved stations.
     *
     * @param availabilities map of station IDs to their availability
     * @see IFavoriteStationsContract.View#showAvailabilities(Map)
     */
    @Override
    public void showAvailabilities(Map<String, Availability> availabilities) {
        if (adapter != null) {
            adapter.updateAvailabilities(availabilities);
        }
    }

    /**
     * Displays a message indicating that the network request failed.
     *
     * @see IFavoriteStationsContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        //Toast.makeText(this, R.string.favorite_error_connection, Toast.LENGTH_SHORT).show();
    }

    /**
     * Finishes this activity when the Up button is pressed.
     *
     * @return {@code true} to indicate that the navigation action was handled
     * @see AppCompatActivity#onSupportNavigateUp()
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Returns the last known device coordinates when location access is available.
     *
     * @return array containing latitude and longitude in decimal degrees, or {@code null}
     *         if location is unavailable
     * @see IFavoriteStationsContract.View#getUserLocation()
     */
    @Override
    public double[] getUserLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!isLocationEnabled(manager)) {
            return new double[0];
        }

        return new double[]{DEMO_LATITUDE, DEMO_LONGITUDE};
    }

    @SuppressWarnings("deprecation")
    private boolean isLocationEnabled(LocationManager manager) {
        if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && manager.isLocationEnabled()) {
            return true;
        }
        int mode = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        return mode != Settings.Secure.LOCATION_MODE_OFF;
    }

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @return {@code true} if connected, {@code false} otherwise
     * @see IFavoriteStationsContract.View#isOnline()
     */
    @Override
    public boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        android.net.Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * Displays the alternative stations found for the selected favorite station.
     *
     * @param favorite favorite station for which alternatives were searched
     * @param alternatives list of alternatives to display
     * @see IFavoriteStationsContract.View#showAlternatives(FavoriteStation, List)
     */
    @Override
    public void showAlternatives(FavoriteStation favorite, List<AlternativeStation> alternatives) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_alternatives, null);
        ListView list = dialogView.findViewById(R.id.lvAlternatives);
        TextView empty = dialogView.findViewById(R.id.tvAlternativesEmpty);

        if (alternatives.isEmpty()) {
            list.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        } else {
            Map<String, String> aliases = new HashMap<>();
            for (FavoriteStation f : favoriteStations) {
                aliases.put(f.getStationId(), f.getAlias());
            }
            list.setAdapter(new AlternativeStationsArrayAdapter(this, alternatives,
                    FavoriteStationsArrayAdapter.flagFromCountryCode(favorite.getCountry()),
                    aliases));
        }

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialog.show();

        // the window takes a good part of the screen
        if (dialog.getWindow() != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            dialog.getWindow().setLayout((int) (metrics.widthPixels * 0.9),
                    (int) (metrics.heightPixels * 0.7));
        }
    }

    /**
     * Displays a message indicating that alternative stations cannot be determined
     * for the selected favorite.
     *
     * @see IFavoriteStationsContract.View#showAlternativesNotPossible()
     */
    @Override
    public void showAlternativesNotPossible() {
        //Toast.makeText(this, R.string.alternativesNotPossible, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a message indicating that alternative stations could not be loaded
     * because of a connection error.
     *
     * @see IFavoriteStationsContract.View#showAlternativesConnectionError()
     */
    @Override
    public void showAlternativesConnectionError() {
        //Toast.makeText(this, R.string.alternativesConnectionError, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a message indicating that the network data required to find alternatives
     * could not be retrieved or processed.
     *
     * @see IFavoriteStationsContract.View#showAlternativesDataError()
     */
    @Override
    public void showAlternativesDataError() {
        //Toast.makeText(this, R.string.alternativesDataError, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a message indicating that favorite-station data could not be accessed
     * from local storage while searching for alternatives.
     *
     * @see IFavoriteStationsContract.View#showAlternativesPersistenceError()
     */
    @Override
    public void showAlternativesPersistenceError() {
        //Toast.makeText(this, R.string.alternativesPersistenceError, Toast.LENGTH_SHORT).show();
    }

}

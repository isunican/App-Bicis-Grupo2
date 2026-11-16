package es.unican.bicis.activities.favorites;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    /** Request code of the location permission */
    private static final int REQUEST_LOCATION = 1;

    /** Last location received from the system */
    private android.location.Location currentLocation;

    /** Favorite whose search is waiting for the answer to the permission dialog */
    private FavoriteStation pendingFavorite;

    /** Listener that keeps currentLocation updated */
    private final android.location.LocationListener locationListener =
            new android.location.LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    currentLocation = location;
                }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }

                @Override
                public void onStatusChanged(String provider, int status, android.os.Bundle extras) { }
            };

    /**
     * Called when the activity is created. Instantiates the presenter and starts the
     * initial business logic.
     * @param savedInstanceState saved state of the activity, or null
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
     * @see IFavoriteStationsContract.View#getFavoriteStationsRepository()
     */
    @Override
    public IFavoriteStationsRepository getFavoriteStationsRepository() {
        return new FavoriteStationsRepository(getApplicationContext());
    }

    /**
     * @see IFavoriteStationsContract.View#showFavoriteStations(List)
     */
    @Override
    public void showFavoriteStations(List<FavoriteStation> favorites) {
        this.favoriteStations = favorites;
        ListView list = findViewById(R.id.lvFavoriteStations);
        adapter = new FavoriteStationsArrayAdapter(this, favorites, favorite -> {
            if (hasLocationPermission()) {
                presenter.onSearchAlternativesClicked(favorite);
            } else {
                // wait for the user's answer before opening the alternatives
                pendingFavorite = favorite;
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            }
        });
        list.setAdapter(adapter);
    }

    /**
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
     * @see IFavoriteStationsContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return networksRepository;
    }

    /**
     * @see IFavoriteStationsContract.View#showAvailabilities(Map)
     */
    @Override
    public void showAvailabilities(Map<String, Availability> availabilities) {
        if (adapter != null) {
            adapter.updateAvailabilities(availabilities);
        }
    }

    /**
     * @see IFavoriteStationsContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        Toast.makeText(this, R.string.favorite_error_connection, Toast.LENGTH_SHORT).show();
    }

    /**
     * Closes this screen when the back button of the action bar is pressed.
     * @return true, since the navigation has been handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * @see IFavoriteStationsContract.View#getUserLocation()
     */
    @Override
    public double[] getUserLocation() {
        if (!hasLocationPermission()) {
            return null;
        }
        if (currentLocation != null) {
            return new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()};
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return null;
        }

        // the most recent location among all the enabled providers
        android.location.Location best = null;
        for (String provider : manager.getProviders(true)) {
            try {
                android.location.Location location = manager.getLastKnownLocation(provider);
                if (location != null && (best == null || location.getTime() > best.getTime())) {
                    best = location;
                }
            } catch (SecurityException e) {
                // the permission was revoked in the meantime: ignore this provider
            }
        }

        if (best == null) {
            return null;
        }
        return new double[]{best.getLatitude(), best.getLongitude()};
    }

    /**
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
     * @see IFavoriteStationsContract.View#showAlternativesNotPossible()
     */
    @Override
    public void showAlternativesNotPossible() {
        Toast.makeText(this, R.string.alternativesNotPossible, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IFavoriteStationsContract.View#showAlternativesConnectionError()
     */
    @Override
    public void showAlternativesConnectionError() {
        Toast.makeText(this, R.string.alternativesConnectionError, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IFavoriteStationsContract.View#showAlternativesDataError()
     */
    @Override
    public void showAlternativesDataError() {
        Toast.makeText(this, R.string.alternativesDataError, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IFavoriteStationsContract.View#showAlternativesPersistenceError()
     */
    @Override
    public void showAlternativesPersistenceError() {
        Toast.makeText(this, R.string.alternativesPersistenceError, Toast.LENGTH_SHORT).show();
    }

    /**
     * @return true if the app has any of the two location permissions
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Starts listening to location updates (if the permission is granted).
     */
    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return;
        }
        try {
            for (String provider : manager.getProviders(true)) {
                manager.requestLocationUpdates(provider, 2000, 0, locationListener);
            }
        } catch (SecurityException e) {
            // permission revoked in the meantime: no location available
        }
    }

    /**
     * Stops listening to location updates.
     */
    private void stopLocationUpdates() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            manager.removeUpdates(locationListener);
        }
    }

    /**
     * Starts listening to the location when the screen is visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    /**
     * Stops listening to the location when the screen is not visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Called when the user answers the location permission dialog. If a search was waiting,
     * the alternatives window is opened (with or without the user's location).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            startLocationUpdates();
            if (pendingFavorite != null) {
                FavoriteStation favorite = pendingFavorite;
                pendingFavorite = null;
                // granted or denied, the alternatives window is opened
                presenter.onSearchAlternativesClicked(favorite);
            }
        }
    }
}
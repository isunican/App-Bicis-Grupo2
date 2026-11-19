package es.unican.bicis.activities.stations;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.R;
import es.unican.bicis.activities.favorites.FavoriteStationsView;
import es.unican.bicis.activities.info.InfoView;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.CountriesList;
import es.unican.bicis.repository.ICountriesList;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

/**
 * View layer for the stations activity. Displays the list of bike stations for a given network.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    private IStationsContract.Presenter presenter;
    private Network network;
    private ListView lvStations;
    private TextView tvResults;
    private TextView tvNetworkLocation;
    private StationsArrayAdapter adapter;
    private PopupWindow filtersPopup;
    private boolean ebikesFilterEnabled = false;


    @Inject
    INetworksRepository repository;

    @Inject
    IFavoriteStationsRepository favoriteStationsRepository;

    /**
     * Creates the stations screen, configures its toolbar and initializes the presenter.
     *
     * @param savedInstanceState previously saved activity state, or {@code null}
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        network = Parcels.unwrap(getIntent().getParcelableExtra("INTENT_NETWORK"));

        // Instantiate presenter and launch initial business logic
        if (favoriteStationsRepository != null) {
            presenter = new StationsPresenter(favoriteStationsRepository);
        } else {
            presenter = new StationsPresenter();
        }
        presenter.init(this);
    }

    /**
     * Inflates the options menu used by the stations screen.
     *
     * @param menu options menu to populate
     * @return {@code true} when the menu has been created
     * @see AppCompatActivity#onCreateOptionsMenu(Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_stations, menu);
        return true;
    }

    /**
     * Updates the menu item states according to the availability of electric-bike data
     * and the current filter configuration.
     *
     * @param menu options menu to update
     * @return {@code true} to display the menu
     * @see AppCompatActivity#onPrepareOptionsMenu(Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menuItemFilter);
        if (item != null) {
            item.setEnabled(ebikesFilterEnabled);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handles toolbar selections, forwarding actions to the presenter or opening the filter UI.
     *
     * @param item selected menu item
     * @return {@code true} if the action was handled, otherwise delegates to the superclass
     * @see AppCompatActivity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItemInfo) {
            presenter.onMenuInfoClicked();
            return true;
        }
        if (itemId == R.id.menuItemFavoriteStations) {
            presenter.onMenuFavoriteStationsClicked();
            return true;
        }
        if (itemId == R.id.menuItemFilter) {
            presenter.onMenuFiltersClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the toolbar, title, station list and associated listeners.
     *
     * @see IStationsContract.View#init()
     */
    @Override
    public void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            if (network != null) {
                bar.setTitle(network.getName());
            }
        }

        tvNetworkLocation = findViewById(R.id.tvNetworkLocation);
        lvStations = findViewById(R.id.lvStations);
        tvResults = findViewById(R.id.tvResults);

    }

    /**
     * Returns the repository used to retrieve network details.
     *
     * @return networks repository
     * @see IStationsContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() { return repository;}

    /**
     * Returns the identifier of the network selected for this activity.
     *
     * @return selected network ID
     * @see IStationsContract.View#getSelectedNetworkId()
     */
    @Override
    public String getSelectedNetworkId() {
        if (network != null) {
            return network.getId();
        }
        return null;
    }

    /**
     * Displays the supplied stations in the list using the station adapter.
     *
     * @param stations stations to display
     * @see IStationsContract.View#showStations(List)
     */
    @Override
    public void showStations(List<Station> stations) {
        this.adapter = new StationsArrayAdapter(this, stations, station -> presenter.onFavoriteIconClicked(station));
        lvStations.setAdapter(this.adapter);
    }

    /**
     * Displays the selected network's city and country.
     *
     * @param networkLocation location of the selected network
     * @see IStationsContract.View#showNetworkLocation(Location)
     */
    @Override
    public void showNetworkLocation(Location networkLocation) {
        String city = networkLocation.getCity();

        if (city != null && !city.isEmpty()) {
            tvNetworkLocation.setText(city);
        }

    }

    /**
     * Displays an error explaining why the station list could not be loaded.
     *
     * @param reason error description, if available
     * @see IStationsContract.View#showLoadError(String)
     */
    @Override
    public void showLoadError(String reason) {
        tvResults.setText("Error cargando estaciones");
        String message = (reason != null && !reason.isEmpty()) ? reason : "Error de conexión";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the number of stations successfully loaded.
     *
     * @param stations number of loaded stations
     * @see IStationsContract.View#showLoadCorrect(int)
     */
    @Override
    public void showLoadCorrect(int stations) {
        tvResults.setText(String.format("Resultados: %d", stations));
    }

    /**
     * Starts the application's information activity.
     *
     * @see IStationsContract.View#showInfoActivity()
     */
    @Override
    public void showInfoActivity() {
        Intent intent = new Intent(this, InfoView.class);
        startActivity(intent);
    }

    /**
     * Displays the electric-bike filter dialog with the current option states.
     *
     * @param availableChecked whether the availability option is selected
     * @param notAvailableChecked whether the non-availability option is selected
     * @see IStationsContract.View#showFilterMenu(boolean, boolean)
     */
    @Override
    public void showFilterMenu(boolean availableChecked, boolean notAvailableChecked) {
        View menu = getLayoutInflater().inflate(R.layout.popup_filters, null);
        CheckBox cbAvailable = menu.findViewById(R.id.cbEbikesAvailable);
        CheckBox cbNotAvailable = menu.findViewById(R.id.cbEbikesNotAvailable);

        // Establecemos el estado de las casillas
        cbAvailable.setChecked(availableChecked);
        cbNotAvailable.setChecked(notAvailableChecked);

        // Tras seleccionar el filtro, se muestra los checkboxes
        cbAvailable.setOnCheckedChangeListener((b, checked) ->
                presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, checked));
        cbNotAvailable.setOnCheckedChangeListener((b, checked) ->
                presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, checked));

        if (filtersPopup != null && filtersPopup.isShowing()) {
            filtersPopup.dismiss();
        }
        filtersPopup = new PopupWindow(menu,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // con focusable a true se cierra al pulsar fuera
        filtersPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filtersPopup.setOutsideTouchable(true);
        filtersPopup.setElevation(8f);

        View anchor = findViewById(R.id.menuItemFilters);
        if (anchor == null) {
            anchor = findViewById(R.id.toolbar);
        }
        filtersPopup.showAsDropDown(anchor);

    }

    /**
     * Enables or disables the electric-bike filter menu option.
     *
     * @param enabled whether electric-bike filtering is available for this network
     * @see IStationsContract.View#enableEbikesFilter(boolean)
     */
    @Override
    public void enableEbikesFilter(boolean enabled) {
        ebikesFilterEnabled = enabled;
        invalidateOptionsMenu();   // fuerza a recalcular el menú
    }

    /**
     * Updates the visual indicator showing which electric-bike filter options are active.
     *
     * @param availableActive whether the available option is active
     * @param notAvailableActive whether the unavailable option is active
     * @see IStationsContract.View#showFilterIndicator(boolean, boolean)
     */
    @Override
    public void showFilterIndicator(boolean availableActive, boolean notAvailableActive) {
        ImageView ivEbike = findViewById(R.id.ivEbike);
        ImageView ivEbikeNotAvailable = findViewById(R.id.ivEbikeNotAvailable);

        if (availableActive) {
            if (ivEbike != null) {
                ivEbike.setVisibility(View.VISIBLE);
            }
            if (ivEbikeNotAvailable != null) {
                ivEbikeNotAvailable.setVisibility(View.GONE);
            }
        } else if (notAvailableActive) {
            if (ivEbike != null) {
                ivEbike.setVisibility(View.VISIBLE);
            }
            if (ivEbikeNotAvailable != null) {
                ivEbikeNotAvailable.setVisibility(View.VISIBLE);
            }
        } else {
            if (ivEbike != null) {
                ivEbike.setVisibility(View.GONE);
            }
            if (ivEbikeNotAvailable != null) {
                ivEbikeNotAvailable.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Displays the flag emoji associated with the selected network's country.
     *
     * @param flag country flag emoji
     * @see IStationsContract.View#showCountryFlag(String)
     */
    @Override
    public void showCountryFlag(String flag) {
        TextView text = findViewById(R.id.tvFlag);
        text.setText(flag);
    }

    /**
     * Returns the country-list provider used to resolve country information.
     *
     * @return country-list provider
     * @see IStationsContract.View#getCountriesList()
     */
    @Override
    public ICountriesList getCountriesList() {
        return new CountriesList(this);
    }

    /**
     * Starts the favorite stations activity.
     *
     * @see IStationsContract.View#showFavoriteStationsActivity()
     */
    @Override
    public void showFavoriteStationsActivity() {
        Intent intent = new Intent(this, FavoriteStationsView.class);
        startActivity(intent);
    }

    /**
     * Displays a dialog allowing the user to enter an alias for the selected station.
     *
     * @param station station to add to favorites
     * @see IStationsContract.View#showAddFavoriteDialog(Station)
     */
    @Override
    public void showAddFavoriteDialog(Station station) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_favorite, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText input = dialogView.findViewById(R.id.etAlias);
        ImageButton ibClose = dialogView.findViewById(R.id.ibCloseDialog);
        Button btnAccept = dialogView.findViewById(R.id.btnAccept);

        if (ibClose != null) {
            ibClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                String alias = input != null ? input.getText().toString() : "";
                presenter.onConfirmAddFavorite(station, alias);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    /**
     * Displays a confirmation message after a station has been saved as a favorite.
     *
     * @see IStationsContract.View#showFavoriteAddedSuccess()
     */
    @Override
    public void showFavoriteAddedSuccess() {
        Toast.makeText(this, R.string.favorite_added_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays an alert indicating that the operation requires an internet connection.
     *
     * @see IStationsContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        Toast.makeText(this, R.string.favorite_error_connection, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays an alert indicating that local persistence failed.
     *
     * @see IStationsContract.View#showPersistenceError()
     */
    @Override
    public void showPersistenceError() {
        Toast.makeText(this, R.string.favorite_error_persistence, Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates the favorite icon for the given station to reflect its saved state.
     *
     * @param stationId station identifier
     * @param isFavorite whether the station is currently saved as a favorite
     * @see IStationsContract.View#updateFavoriteIcon(String, boolean)
     */
    @Override
    public void updateFavoriteIcon(String stationId, boolean isFavorite) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
     * Releases activity resources when the activity is destroyed.
     *
     * @see AppCompatActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        if (filtersPopup != null && filtersPopup.isShowing()) {
            filtersPopup.dismiss();
        }
        super.onDestroy();
    }

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @return {@code true} if connected, {@code false} otherwise
     * @see IStationsContract.View#isOnline()
     */
    @Override
    public boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        android.net.Network activeNetwork = manager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * Displays a connection error related to loading favorite stations.
     *
     * @see IStationsContract.View#showFavoritesConnectionError()
     */
    @Override
    public void showFavoritesConnectionError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.errorTitulo)
                .setMessage(R.string.loadErrorLocal)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

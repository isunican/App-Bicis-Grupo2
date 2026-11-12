package es.unican.bicis.activities.stations;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.content.Intent;

import org.parceler.Parcels;

import java.util.ArrayList;
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
import es.unican.bicis.activities.favorites.FavoriteStationsView;
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

    @Inject
    INetworksRepository repository;

    @Inject
    IFavoriteStationsRepository favoriteStationsRepository;

    /**
     * Initializes the activity, unpacks the selected network from intent extras, and initializes the presenter.
     *
     * @param savedInstanceState saved instance state bundle
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
     * Inflates the toolbar menu.
     *
     * @param menu the menu to inflate into
     * @return true if menu creation succeeded
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_stations, menu);
        return true;
    }

    /**
     * Handles selection events on action bar menu items.
     *
     * @param item the selected menu item
     * @return true if item selection was handled
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
     * @see IStationsContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() { return repository;}

    /**
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
     * @see IStationsContract.View#showStations(List)
     */
    @Override
    public void showStations(List<Station> stations) {
        this.adapter = new StationsArrayAdapter(this, stations, station -> presenter.onFavoriteIconClicked(station));
        lvStations.setAdapter(this.adapter);
    }

    /**
     * @see IStationsContract.View#showNetworkLocation(Location)
     */
    @Override
    public void showNetworkLocation(Location networkLocation) {
        List<String> location = new ArrayList<>();

        String country = networkLocation.getCountry();
        String city = networkLocation.getCity();
        if (country != null && !country.isEmpty()) {
            location.add(country);
        }
        if (city != null && !city.isEmpty()) {
            location.add(city);
        }

        tvNetworkLocation.setText(TextUtils.join(" ", location));
    }

    /**
     * @see IStationsContract.View#showLoadError(String)
     * @param reason Brief explanation on why the error occurred (it could be null).
     */
    @Override
    public void showLoadError(String reason) {
        tvResults.setText("Error cargando estaciones");
        String message = (reason != null && !reason.isEmpty()) ? reason : "Error de conexión";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IStationsContract.View#showLoadCorrect(int)
     */
    @Override
    public void showLoadCorrect(int stations) {
        tvResults.setText(String.format("Resultados: %d", stations));
    }

    /**
     * @see IStationsContract.View#showInfoActivity()
     */
    @Override
    public void showInfoActivity() {
        Intent intent = new Intent(this, InfoView.class);
        startActivity(intent);
    }

    /**
     * @see IStationsContract.View#showFilterMenu(boolean, boolean, boolean)
     * @param ebikesAvailable  false if the network provides no e-bike data (option disabled)
     * @param availableChecked state of the "Disponible" checkbox
     * @param notAvailableChecked state of the "No disponible" checkbox
     */
    @Override
    public void showFilterMenu(boolean ebikesAvailable, boolean availableChecked, boolean notAvailableChecked) {
        View menu = getLayoutInflater().inflate(R.layout.popup_filters, null);
        TextView tvFilter = menu.findViewById(R.id.tvFilterEbikes);
        View submenu = menu.findViewById(R.id.llFilterEbikesSubmenu);
        CheckBox cbAvailable = menu.findViewById(R.id.cbEbikesAvailable);
        CheckBox cbNotAvailable = menu.findViewById(R.id.cbEbikesNotAvailable);

        // Establecemos el estado de las casillas
        cbAvailable.setChecked(availableChecked);
        cbNotAvailable.setChecked(notAvailableChecked);

        // Primero se muestran los filtros disponiles (actualmente tan solo el de bicis electricas)
        if (ebikesAvailable) {
            tvFilter.setOnClickListener(v -> {
                if (submenu.getVisibility() == View.VISIBLE) {
                    submenu.setVisibility(View.GONE);
                } else {
                    submenu.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // La red no tiene datos de bicis eléctricas (se deshabilita la opción)
            tvFilter.setEnabled(false);
            tvFilter.setAlpha(0.4f);
        }

        // Tras seleccionar el filtro, se muestra el submenu con los checkboxes
        cbAvailable.setOnCheckedChangeListener((b, checked) ->
                presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, checked));
        cbNotAvailable.setOnCheckedChangeListener((b, checked) ->
                presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, checked));

        if (filtersPopup != null && filtersPopup.isShowing()) {
            filtersPopup.dismiss();
        }
        filtersPopup = new PopupWindow(menu,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // con focusable a true se cierra al pulsar fuera
        filtersPopup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        filtersPopup.setOutsideTouchable(true);
        filtersPopup.setElevation(8f);

        View anchor = findViewById(R.id.menuItemFilters);
        if (anchor == null) {
            anchor = findViewById(R.id.toolbar);
        }
        filtersPopup.showAsDropDown(anchor);

    }

    /**
     * @see IStationsContract.View#showFilterIndicator(boolean)
     * @param active true if any filter is currently applied.
     */
    @Override
    public void showFilterIndicator(boolean active) {
        ImageView ivEbike = findViewById(R.id.ivEbike);

        if (active) {
            ivEbike.setVisibility(View.VISIBLE);
        } else {
            ivEbike.setVisibility(View.GONE);
        }
        
    }

    @Override
    public void showCountryFlag(String flag) {
        TextView text = findViewById(R.id.tvFlag);
        text.setText(flag);
    }

    @Override
    public ICountriesList getCountriesList() {
        return new CountriesList(this);
    }

    /**
     * @see IStationsContract.View#showFavoriteStationsActivity()
     */
    @Override
    public void showFavoriteStationsActivity() {
        Intent intent = new Intent(this, FavoriteStationsView.class);
        startActivity(intent);
    }

    /**
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
     * @see IStationsContract.View#showFavoriteAddedSuccess()
     */
    @Override
    public void showFavoriteAddedSuccess() {
        Toast.makeText(this, R.string.favorite_added_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IStationsContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        Toast.makeText(this, R.string.favorite_error_connection, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IStationsContract.View#showPersistenceError()
     */
    @Override
    public void showPersistenceError() {
        Toast.makeText(this, R.string.favorite_error_persistence, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IStationsContract.View#updateFavoriteIcon(String, boolean)
     */
    @Override
    public void updateFavoriteIcon(String stationId, boolean isFavorite) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Navigates back when clicking the UP button on the action bar.
     *
     * @return true when navigation completes
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * @see IStationsContract.View#isOnline()
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

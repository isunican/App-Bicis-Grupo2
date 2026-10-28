package es.unican.bicis.activities.stations;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.R;
import es.unican.bicis.activities.info.InfoView;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.activities.favorites.FavoriteStationsView;

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

    @Inject
    INetworksRepository repository;

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
        presenter = new StationsPresenter();
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
        menuInflater.inflate(R.menu.menu, menu);
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
}

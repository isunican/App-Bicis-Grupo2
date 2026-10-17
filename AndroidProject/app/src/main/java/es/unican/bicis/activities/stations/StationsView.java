package es.unican.bicis.activities.stations;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The stations view of the application.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    private IStationsContract.Presenter presenter;
    private Network network;
    private ListView lvStations;
    private TextView tvResults;
    private TextView tvNetworkLocation;
    @Inject
    INetworksRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        network = Parcels.unwrap(getIntent().getParcelableExtra("INTENT_NETWORK"));

        // instantiate presenter and launch initial business logic
        presenter = new StationsPresenter();
        presenter.init(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * This is called when an item in the action bar menu is selected.
     * @param item The menu item that was selected.
     *
     * @return true if we have handled the selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItemInfo) {
            presenter.onMenuInfoClicked();
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
        StationsArrayAdapter adapter = new StationsArrayAdapter(this, stations);
        lvStations.setAdapter(adapter);
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

}

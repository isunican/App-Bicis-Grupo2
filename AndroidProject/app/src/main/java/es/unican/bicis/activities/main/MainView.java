package es.unican.bicis.activities.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import es.unican.bicis.R;
import es.unican.bicis.activities.info.InfoView;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.CountriesList;
import es.unican.bicis.repository.ICountriesList;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The main view of the application. It shows a list of bike sharing networks.
 */
@AndroidEntryPoint
public class MainView extends AppCompatActivity implements IMainContract.View {

    /** The presenter of this view */
    private IMainContract.Presenter presenter;

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The default theme does not include a toolbar.
        // In this app the toolbar is explicitly declared in the layout
        // Set this toolbar as the activity ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // instantiate presenter and launch initial business logic
        presenter = new MainPresenter();
        presenter.init(this);
    }

    /**
     * This creates the menu that is shown in the action bar (the upper toolbar)
     * @param menu The options menu in which you place your items.
     *
     * @return true because we are defining a new menu
     */
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
     * @see IMainContract.View#init()
     */
    @Override
    public void init() {
        // initialize on click listeners (when clicking on a network in the list)
        ListView list = findViewById(R.id.lvNetworks);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Network network = (Network) parent.getItemAtPosition(position);
            presenter.onNetworkClicked(network);
        });

        // search button action upon press
        Spinner countrySpinner = findViewById(R.id.spCountry);
        Spinner citySpinner = findViewById(R.id.spCity);
        Button searchButton = findViewById(R.id.btnBuscar);
        searchButton.setOnClickListener(v -> {
            // send the selected spinner items in the view to the presenter
            Country country = (Country) countrySpinner.getSelectedItem();
            String city = (String) citySpinner.getSelectedItem();
            presenter.onSearchClicked(country, city);
        });

        // country spinner listener
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country country = (Country) parent.getItemAtPosition(position);

                // tell the presenter to do the respective action when a country gets picked
                presenter.onCountrySelected(country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do if no country gets selected
                // but we're forced to override this method
            }
        });
    }

    /**
     * @see IMainContract.View#getNetworksRepository()
     * @return the repository to access the data
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return repository;
    }

    @Override
    public ICountriesList getCountriesList() {
        // i go back to the view because i need the application context for the json parser,
        // which i understand can only be obtained from classes that extend Activity
        return new CountriesList(getApplicationContext());
    }

    /**
     * @see IMainContract.View#showNetworks(List)
     * @param networks the list of networks
     */
    @Override
    public void showNetworks(List<Network> networks) {
        ListView list = findViewById(R.id.lvNetworks);
        NetworksArrayAdapter adapter = new NetworksArrayAdapter(this, networks);
        list.setAdapter(adapter);
    }

    @Override
    public void showCountries(List<Country> countries) {
        Spinner spinner = findViewById(R.id.spCountry);
        HintArrayAdapter<Country> adapter = new HintArrayAdapter<>(this, countries, getString(R.string.countryHint), Country::getName);
        spinner.setAdapter(adapter);
    }

    @Override
    public void showCountryError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.errorTitulo)
                .setMessage(R.string.errorPaisNoSeleccionado)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * @see IMainContract.View#showLoadCorrect(int)
     * @param networks
     */
    @Override
    public void showLoadCorrect(int networks) {
        String showText = getString(R.string.labelResults) + ": " + networks;
        TextView text = findViewById(R.id.tvResults);
        text.setText(showText);
    }

    /**
     * @see IMainContract.View#showLoadError()
     */
    @Override
    public void showLoadError() {
        Toast.makeText(this, R.string.loadError, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IMainContract.View#showNetworkDetails(Network)
     * @param network the network
     */
    @Override
    public void showNetworkDetails(Network network) {
        Intent intent = new Intent(this, DetailsView.class);
        intent.putExtra(DetailsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }

    /**
     * @see IMainContract.View#showInfoActivity()
     */
    @Override
    public void showInfoActivity() {
        Intent intent = new Intent(this, InfoView.class);
        startActivity(intent);
    }

    @Override
    public void showCities(List<String> cities) {
        Spinner spinner = findViewById(R.id.spCity);
        HintArrayAdapter<String> adapter = new HintArrayAdapter<>(this, cities, getString(R.string.cityHint), city -> city);
        spinner.setAdapter(adapter);
    }
}
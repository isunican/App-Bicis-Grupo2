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
import es.unican.bicis.activities.stations.StationsView;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.activities.favorites.FavoriteStationsView;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.repository.IFavoriteStationsRepository;

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

    /**
     * Creates the main screen, configures its toolbar and initializes the presenter.
     *
     * @param savedInstanceState previously saved activity state, or {@code null}
     * @see AppCompatActivity#onCreate(Bundle)
     */
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
        presenter.init(this, getApplicationContext());
    }

    /**
     * Inflates the options menu displayed in the main toolbar.
     *
     * @param menu options menu to populate
     * @return {@code true} when the menu has been created
     * @see AppCompatActivity#onCreateOptionsMenu(Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Handles toolbar menu selections by forwarding the information or favorites action
     * to the presenter.
     *
     * @param item selected menu item
     * @return {@code true} if the item was handled, otherwise delegates to the superclass
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the listeners for network selection, country selection and search actions.
     *
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

            /**
             * Notifies the presenter when a country is selected in the country spinner.
             *
             * @param parent spinner containing the selected item
             * @param view selected row view
             * @param position selected item position
             * @param id selected item identifier
             * @see AdapterView.OnItemSelectedListener#onItemSelected(AdapterView, View, int, long)
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country country = (Country) parent.getItemAtPosition(position);

                // tell the presenter to do the respective action when a country gets picked
                presenter.onCountrySelected(country);
            }

            /**
             * Does nothing when the country spinner has no selected item.
             *
             * @param parent spinner whose selection was cleared
             * @see AdapterView.OnItemSelectedListener#onNothingSelected(AdapterView)
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do if no country gets selected
                // but we're forced to override this method
            }
        });
    }

    /**
     * Returns the repository injected into this activity for retrieving bike networks.
     *
     * @return repository used to access network data
     * @see IMainContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return repository;
    }

    /**
     * Displays the supplied networks in the list using the networks adapter.
     *
     * @param networks networks to display
     * @see IMainContract.View#showNetworks(List)
     */
    @Override
    public void showNetworks(List<Network> networks) {
        ListView list = findViewById(R.id.lvNetworks);
        NetworksArrayAdapter adapter = new NetworksArrayAdapter(this, networks, network -> presenter.onNetworkInfoClicked(network));
        list.setAdapter(adapter);
    }

    /**
     * Populates the country spinner with the supplied countries and a hint row.
     *
     * @param countries countries available for selection
     * @see IMainContract.View#showCountries(List)
     */
    @Override
    public void showCountries(List<Country> countries) {
        Spinner spinner = findViewById(R.id.spCountry);
        HintArrayAdapter<Country> adapter = new HintArrayAdapter<>(this, countries, getString(R.string.countryHint), Country::getName);
        spinner.setAdapter(adapter);
    }

    /**
     * Displays an alert informing the user that an internet connection is required.
     *
     * @see IMainContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.errorTitulo)
                .setMessage(R.string.errorNoInternetConnection)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Updates the result counter with the number of networks displayed.
     *
     * @param networks number of networks found
     * @see IMainContract.View#showLoadCorrect(int)
     */
    @Override
    public void showLoadCorrect(int networks) {
        String showText = getString(R.string.labelResults) + ": " + networks;
        TextView text = findViewById(R.id.tvResults);
        text.setText(showText);
    }

    /**
     * Displays the flag emoji associated with the selected country.
     *
     * @param flag country flag emoji
     * @see IMainContract.View#showCountryFlag(String)
     */
    @Override
    public void showCountryFlag(String flag) {
        TextView text = findViewById(R.id.tvFlag);
        text.setText(flag);
    }

    /**
     * Displays an alert indicating that the network request failed.
     *
     * @see IMainContract.View#showLoadError()
     */
    @Override
    public void showLoadError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.errorTitulo)
                .setMessage(R.string.loadError)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Starts the stations activity for the selected network.
     *
     * @param network network whose stations will be displayed
     * @see IMainContract.View#showStations(Network)
     */
    @Override
    public void showStations(Network network) {
        Intent intent = new Intent(this, StationsView.class);
        intent.putExtra(DetailsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }

    /**
     * Starts the application's information activity.
     *
     * @see IMainContract.View#showInfoActivity()
     */
    @Override
    public void showInfoActivity() {
        Intent intent = new Intent(this, InfoView.class);
        startActivity(intent);
    }

    /**
     * Starts the activity displaying the user's favorite stations.
     *
     * @see IMainContract.View#showFavoriteStationsActivity()
     */
    @Override
    public void showFavoriteStationsActivity() {
        Intent intent = new Intent(this, FavoriteStationsView.class);
        startActivity(intent);
    }

    /**
     * Starts the details activity for the selected network.
     *
     * @param network network whose details will be displayed
     * @see IMainContract.View#showNetworkDetails(Network)
     */
    @Override
    public void showNetworkDetails(Network network) {
        Intent intent = new Intent(this, DetailsView.class);
        intent.putExtra(DetailsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }

    /**
     * Populates the city spinner with the supplied city names and a hint row.
     *
     * @param cities city names available for selection
     * @see IMainContract.View#showCities(List)
     */
    @Override
    public void showCities(List<String> cities) {
        Spinner spinner = findViewById(R.id.spCity);
        HintArrayAdapter<String> adapter = new HintArrayAdapter<>(this, cities, getString(R.string.cityHint), city -> city);
        spinner.setAdapter(adapter);
    }

    /**
     * Creates and returns the repository used to access locally stored favorite stations.
     *
     * @return repository for favorite-station persistence
     * @see IMainContract.View#getFavoriteStationsRepository()
     */
    @Override
    public IFavoriteStationsRepository getFavoriteStationsRepository() {
        return new FavoriteStationsRepository(getApplicationContext());
    }

    /**
     * Displays an alert indicating that locally stored favorite-station data could not be accessed.
     *
     * @see IMainContract.View#showPersistenceError()
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
}

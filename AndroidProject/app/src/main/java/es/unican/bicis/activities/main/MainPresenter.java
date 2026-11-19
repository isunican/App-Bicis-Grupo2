package es.unican.bicis.activities.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.unican.bicis.common.Utils;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.CountriesList;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;
import es.unican.bicis.common.DataAccessException;

/**
 * The presenter of the main activity of the application. It controls {@link MainView}
 */
public class MainPresenter implements IMainContract.Presenter {

    /** The view that is controlled by this presenter */
    private IMainContract.View view;

    /** The context to be used for resource access */
    private Context context;

    /** The indicator for yet unpopulated lists over no internet situations */
    private boolean delayLoad;

    /**
     * Attaches the presenter to the view and initializes the initial country and city data.
     * If there is no connection, initializes the selectors with empty lists and displays
     * the connection error.
     *
     * @param view view controlled by this presenter
     * @param context context used for application resources and services
     * @see IMainContract.Presenter#init(IMainContract.View, Context)
     */
    @Override
    public void init(IMainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        this.view.init();

        if (isOnline()) {
            delayLoad = false;
            loadCountries();
            loadCities(null);
        } else {
            delayLoad = true;
            view.showCountries(new ArrayList<>());
            view.showCities(new ArrayList<>());
            view.showConnectionError();
        }
    }

    /**
     * Requests networks matching the selected country and city. If initial data loading
     * was delayed due to a connection error, retries loading countries and cities first.
     * Displays a connection error when the device is offline.
     *
     * @param country selected country, or {@code null} if no country is selected
     * @param city selected city, or {@code null} if no city is selected
     * @see IMainContract.Presenter#onSearchClicked(Country, String)
     */
    @Override
    public void onSearchClicked(Country country, String city) {
        if (isOnline()) {
            if (delayLoad) {
                delayLoad = false;
                loadCountries();
                loadCities(null);
            }

            if (country == null) {
                loadNetworks(null, city);
            } else {
                loadNetworks(country.getIdAPI(), city);
            }
        } else {
            view.showConnectionError();
        }
    }

    /**
     * Opens the stations screen for the selected network.
     *
     * @param network selected network
     * @see IMainContract.Presenter#onNetworkClicked(Network)
     */
    @Override
    public void onNetworkClicked(Network network) {
        view.showStations(network);
    }

    /**
     * Requests the view to open the application's information screen.
     *
     * @see IMainContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * Checks connectivity and verifies that favorite stations can be read from local storage
     * before requesting the view to open the favorites screen. Displays the corresponding
     * error if connectivity or persistence is unavailable.
     *
     * @see IMainContract.Presenter#onMenuFavoriteStationsClicked()
     */
    @Override
    public void onMenuFavoriteStationsClicked() {
        if (!isOnline()) {
            view.showConnectionError();
            return;
        }
        try {
            view.getFavoriteStationsRepository().getFavoriteStations();
        } catch (RuntimeException | DataAccessException e) {
            view.showPersistenceError();
            return;
        }
        view.showFavoriteStationsActivity();
    }

    /**
     * Opens the details screen for the selected network.
     *
     * @param network network whose details were requested
     * @see IMainContract.Presenter#onNetworkInfoClicked(Network)
     */
    @Override
    public void onNetworkInfoClicked(Network network) {
        view.showNetworkDetails(network);
    }

    /**
     * Updates the city selector for the selected country and displays its flag.
     * If no country is selected, requests cities from all countries and displays the globe.
     *
     * @param country selected country, or {@code null} for all countries
     * @see IMainContract.Presenter#onCountrySelected(Country)
     */
    @Override
    public void onCountrySelected(Country country) {
        // country spinner might be selected before it was asynchronously initialized
        if (country != null) {
            loadCities(country.getIdAPI());
            view.showCountryFlag(country.getFlag());
        } else {
            loadCities(null); // Fixed city bug
            view.showCountryFlag(Country.GLOBE);
        }
    }

    /**
     * Loads the list of networks located on a given country and/or city, providing
     * all networks present at the platform if neither of those were specified.
     * @param countryIdAPI the chosen country (its ID), or null if none was specified
     * @param city the chosen city, or null if none was specified
     */
    private void loadNetworks(String countryIdAPI, String city) {
        INetworksRepository repository = view.getNetworksRepository();

        NetworksCallback callBack = new NetworksCallback() {

            /**
             * Handles the successful network-list response by sorting the networks, applying
             * the selected country and city filters, and updating the list and result count.
             *
             * @param networks networks received from the repository
             * @see NetworksCallback#onSuccess(List)
             */
            @Override
            public void onSuccess(List<Network> networks) {
                Collections.sort(networks, (n1, n2) -> Utils.compareNames(n1.getName(), n2.getName()));

                if (countryIdAPI == null) {
                    if (city == null) {
                        view.showNetworks(networks);
                        view.showLoadCorrect(networks.size());
                    } else {
                        List<Network> filtered = filterByCity(networks, city);
                        view.showNetworks(filtered);
                        view.showLoadCorrect(filtered.size());
                    }
                } else {
                    List<Network> filtered = filterByCountry(networks, countryIdAPI);
                    if (city != null) {
                        filtered = filterByCity(filtered, city);
                    }
                    view.showNetworks(filtered);
                    view.showLoadCorrect(filtered.size());
                }
            }

            /**
             * Notifies the view that the network-list request failed.
             *
             * @param e cause of the request failure
             * @see NetworksCallback#onFailure(Throwable)
             */
            @Override
            public void onFailure(Throwable e) {
                view.showLoadError();
            }
        };

        repository.requestNetworks(callBack);
    }

    /**
     * Loads the list of cities located inside a country, providing all the
     * cities present at the platform is none was specified.
     * @param countryIdAPI the chosen country (its ID), or null if none was specified
     */
    private void loadCities(String countryIdAPI) {
        INetworksRepository repository = view.getNetworksRepository();

        NetworksCallback callBack = new NetworksCallback() {

            /**
             * Extracts the distinct city names from the returned networks, optionally restricting
             * them to the selected country, and updates the city selector.
             *
             * @param networks networks received from the repository
             * @see NetworksCallback#onSuccess(List)
             */
            @Override
            public void onSuccess(List<Network> networks) {
                if (countryIdAPI == null) {
                    view.showCities(extractCities(networks));
                } else {
                    view.showCities(extractCities(filterByCountry(networks, countryIdAPI)));
                }
            }

            /**
             * Ignores a failed city-list request because no error message is required for this operation.
             *
             * @param e cause of the request failure
             * @see NetworksCallback#onFailure(Throwable)
             */
            @Override
            public void onFailure(Throwable e) {
                // No se requiere ninguna acción
            }
        };

        repository.requestNetworks(callBack);
    }

    /**
     * Loads the list of cities present at the platform.
     */
    private void loadCountries() {
        INetworksRepository repository = view.getNetworksRepository();

        NetworksCallback callBack = new NetworksCallback() {

            /**
             * Extracts the distinct countries represented by the networks and updates the country selector.
             *
             * @param networks networks received from the repository
             * @see NetworksCallback#onSuccess(List)
             */
            @Override
            public void onSuccess(List<Network> networks) {
                view.showCountries(extractCountries(networks));
            }

            /**
             * Ignores a failed country-list request because no error message is required for this operation.
             *
             * @param e cause of the request failure
             * @see NetworksCallback#onFailure(Throwable)
             */
            @Override
            public void onFailure(Throwable e) {
                // No se requiere ninguna acción
            }
        };

        repository.requestNetworks(callBack);
    }

    /**
     * Filters the list of bike networks over a given country, so that the resulting
     * list only includes networks that are present in that specific country.
     * @param networks the list of bike networks
     * @param countryIdAPI the chosen country (its ID as per the API)
     * @return the filtered list of bike networks
     */
    private List<Network> filterByCountry(List<Network> networks, String countryIdAPI) {
        List<Network> filtered = new ArrayList<>();
        for (Network network : networks) {
            Location location = network.getLocation();

            // only grab the ones that match our filter
            if (location != null && countryIdAPI.equals(location.getCountry())) {
                filtered.add(network);
            }
        }
        return filtered;
    }

    /**
     * Filters the list of bike networks over a given city, so that the resulting
     * list only includes networks that are present in that specific city.
     * @param networks the list of bike networks
     * @param city the chosen city
     * @return the filtered list of bike networks
     */
    private List<Network> filterByCity(List<Network> networks, String city) {
        List<Network> filtered = new ArrayList<>();
        for (Network network : networks) {
            Location location = network.getLocation();
            if (location != null && city.equals(location.getCity())) {
                filtered.add(network);
            }
        }
        return filtered;
    }

    /**
     * Obtains the list of cities that appear on a set of networks
     * @param networks the list of bike networks
     * @return the list of cities
     */
    private List<String> extractCities(List<Network> networks) {
        // handled with this datatype to avoid storing repeated cities
        Set<String> cities = new LinkedHashSet<>();
        for (Network network : networks) {
            Location location = network.getLocation();
            if (location != null && location.getCity() != null) {
                cities.add(location.getCity());
            }
        }
        return new ArrayList<>(cities);
    }

    /**
     * Obtains the list of countries that appear on a set of networks
     * @param networks the list of bike networks
     * @return the list of countries
     */
    private List<Country> extractCountries(List<Network> networks) {
        // handled with this datatype to avoid storing repeated countries
        Set<String> countriesStrings = new LinkedHashSet<>();
        for (Network network : networks) {
            Location location = network.getLocation();
            if (location != null && location.getCountry() != null) {
                countriesStrings.add(location.getCountry());
            }
        }

        // associate each found country with their respective attributes
        Map<String, Country> allCountries = new CountriesList(context).getMap();
        List<Country> countries = new ArrayList<>();
        for (String countryIdAPI : countriesStrings) {
            countries.add(Country.find(allCountries, countryIdAPI));
        }
        return countries;
    }

    /**
     * Checks that the application has networking and internet connectivity
     * @return true if there is an active internet connection, false if otherwise
     */
    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        android.net.Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}

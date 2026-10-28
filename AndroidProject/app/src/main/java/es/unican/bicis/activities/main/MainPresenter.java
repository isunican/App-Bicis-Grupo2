package es.unican.bicis.activities.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.CountriesList;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

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
     * @see IMainContract.Presenter#init(IMainContract.View, Context)
     * @param view the associated view class object
     * @param context a context used to access resources (e.g. the Application)
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
     * @see IMainContract.Presenter#onSearchClicked(Country, String) 
     * @param country the selected country, or null if none was selected
     * @param city the selected city, or null if none was selected
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
     * @see IMainContract.Presenter#onNetworkClicked(Network)
     * @param network the network that has been clicked
     */
    @Override
    public void onNetworkClicked(Network network) {
        view.showStations(network);
    }

    /**
     * @see IMainContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * @see IMainContract.Presenter#onMenuFavoriteStationsClicked()
     */
    @Override
    public void onMenuFavoriteStationsClicked() {
        view.showFavoriteStationsActivity();
    }

    /**
     * @see IMainContract.Presenter#onCountrySelected(Country) 
     * @param country the selected country, or null if none was selected
     */
    @Override
    public void onNetworkInfoClicked(Network network) {
        view.showNetworkDetails(network);
    }

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

            @Override
            public void onSuccess(List<Network> networks) {
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

            @Override
            public void onSuccess(List<Network> networks) {
                if (countryIdAPI == null) {
                    view.showCities(extractCities(networks));
                } else {
                    view.showCities(extractCities(filterByCountry(networks, countryIdAPI)));
                }
            }

            @Override
            public void onFailure(Throwable e) {
                //view.showLoadError();
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

            @Override
            public void onSuccess(List<Network> networks) {
                view.showCountries(extractCountries(networks));
            }

            @Override
            public void onFailure(Throwable e) {
                //view.showLoadError();
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
            Country country = allCountries.get(countryIdAPI);
            if (country != null) {
                countries.add(country);
            } else {
                Country unknownCountry = new Country();
                unknownCountry.setName(countryIdAPI);
                unknownCountry.setIdISO(countryIdAPI);
                unknownCountry.setIdAPI(countryIdAPI);
                unknownCountry.setFlag(Country.WHITE_FLAG);
                countries.add(unknownCountry);
            }
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

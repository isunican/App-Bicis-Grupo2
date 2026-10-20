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
    private Context context;
    private boolean delayLoad;

    /**
     * initialize the spinners to populate their content
     * @see IMainContract.Presenter#init(IMainContract.View)
     * @param view the view to control
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
            view.showCountryFlag(Country.GLOBE);
        }
    }

    /**
     * Loads the list of networks located on a given country and, optionally, city
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
     * load the list of cities located inside a country
     * @param countryIdAPI
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
                view.showLoadError();
            }
        };

        repository.requestNetworks(callBack);
    }

    private void loadCountries() {
        INetworksRepository repository = view.getNetworksRepository();

        NetworksCallback callBack = new NetworksCallback() {

            @Override
            public void onSuccess(List<Network> networks) {
                view.showCountries(extractCountries(networks));
            }

            @Override
            public void onFailure(Throwable e) {
                view.showLoadError();
            }
        };

        repository.requestNetworks(callBack);
    }

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

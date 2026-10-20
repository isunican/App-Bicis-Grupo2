package es.unican.bicis.activities.main;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.ICountriesList;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

/**
 * The presenter of the main activity of the application. It controls {@link MainView}
 */
public class MainPresenter implements IMainContract.Presenter {

    /** The view that is controlled by this presenter */
    private IMainContract.View view;

    /**
     * @see IMainContract.Presenter#init(IMainContract.View)
     * @param view the view to control
     */
    @Override
    public void init(IMainContract.View view) {
        this.view = view;
        this.view.init();

        // initialize the spinners to populate their content
        ICountriesList countriesList = view.getCountriesList();
        List<Country> countries = countriesList.getList();
        this.view.showCountries(countries);
        loadCities(null);
    }

    @Override
    public void onSearchClicked(Country country, String city) {
        if (country == null) {
            loadNetworks(null, city);
        } else {
            loadNetworks(country.getIdAPI(), city);
        }
    }

    /**
     * @see IMainContract.Presenter#onNetworkClicked(Network)
     * @param network the network that has been clicked
     */
    @Override
    public void onNetworkClicked(Network network) {
        view.showNetworkDetails(network);
    }

    /**
     * @see IMainContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    @Override
    public void onCountrySelected(Country country) {
        loadCities(country.getIdAPI());
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
        // stored with this datatype to avoid storing repeated cities
        Set<String> cities = new LinkedHashSet<>();
        for (Network network : networks) {
            Location location = network.getLocation();
            if (location != null && location.getCity() != null) {
                cities.add(location.getCity());
            }
        }
        return new ArrayList<>(cities);
    }
}

package es.unican.bicis.activities.stations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.ICountriesList;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.model.Station;

/**
 * The presenter of the stations activity from the application. It controls {@link StationsView}
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** The view that is controlled by this presenter.*/
    private IStationsContract.View view;

    /**
     * Links the presenter to the view and triggers station data fetching.
     *
     * @param view the view instance to attach
     */
    @Override
    public void init(IStationsContract.View view) {
        this.view = view;
        this.view.init();
        load();
    }

    /**
     * Handles clicks on the info menu option by navigating to the info screen.
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * Fetches station details for the selected network from the repository and passes them to the view.
     */
    private void load() {
        String networkId = view.getSelectedNetworkId();

        if (networkId == null || networkId.trim().isEmpty()) {
            view.showLoadError(null);
            return;
        }

        INetworksRepository repository = view.getNetworksRepository();
        NetworkDetailsCallback callBack = new NetworkDetailsCallback() {

            @Override
            public void onSuccess(Network network) {
                if (network == null) {
                    view.showLoadError(null);
                    return;
                }

                List<Station> stations = network.getStations();
                if (stations == null) {
                    stations = new ArrayList<>();
                }

                Location networkLocation = network.getLocation();
                if (networkLocation == null) {
                    networkLocation = new Location();
                }

                view.showNetworkLocation(networkLocation);
                view.showStations(stations);
                view.showCountryFlag(resolveFlag(networkLocation.getCountry()));
                view.showLoadCorrect(stations.size());
            }

            @Override
            public void onFailure(Throwable e) {
                if (e != null) {
                    view.showLoadError(e.getMessage());
                } else {
                    view.showLoadError(null);
                }
            }


        };



        repository.requestNetwork(callBack, networkId);
    }

    private String resolveFlag(String countryIdAPI) {
        if (countryIdAPI == null || countryIdAPI.isEmpty()) {
            return Country.GLOBE;
        }

        Map<String, Country> countries = null;
        ICountriesList countriesList = view.getCountriesList();
        if (countriesList != null) {
            countries = countriesList.getMap();
        }

        String flag = Country.find(countries, countryIdAPI).getFlag();
        return Objects.requireNonNullElse(flag, Country.WHITE_FLAG);

    }
}

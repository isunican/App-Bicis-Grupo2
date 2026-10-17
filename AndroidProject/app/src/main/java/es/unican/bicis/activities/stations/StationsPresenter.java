package es.unican.bicis.activities.stations;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.model.Station;

/**
 * The presenter of the stations activity from the application. It controls {@link StationsView}
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** The view that is controlled by this presenter.*/
    private IStationsContract.View view;

    @Override
    public void init(IStationsContract.View view) {
        this.view = view;
        this.view.init();
        load();
    }

    /**
     * @see IMainContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

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
}

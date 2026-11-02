package es.unican.bicis.activities.stations;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.activities.favorites.FavoriteStationsView;


/**
 * Presenter implementation for the stations activity.
 */
public class StationsPresenter implements IStationsContract.Presenter {

    private IStationsContract.View view;
    private IFavoriteStationsRepository favoriteStationsRepository;
    private Location currentNetworkLocation;

    /**
     * Constructs presenter with custom favorite repository (useful for testing/injection).
     *
     * @param favoriteStationsRepository repository instance
     */
    public StationsPresenter(IFavoriteStationsRepository favoriteStationsRepository) {
        this.favoriteStationsRepository = favoriteStationsRepository;
    }

    /**
     * Default constructor.
     */
    public StationsPresenter() {
        // Default constructor
    }

    @Override
    public void init(IStationsContract.View view) {
        this.view = view;
        this.view.init();
        if (favoriteStationsRepository == null && view instanceof Context) {
            favoriteStationsRepository = new FavoriteStationsRepository((Context) view);
        }
        load();
    }

    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * Handles clicks on the favorite stations menu option by navigating to that screen.
     */
    @Override
    public void onMenuFavoriteStationsClicked() {
        view.showFavoriteStationsActivity();
    }

    @Override
    public void onFavoriteIconClicked(Station station) {
        if (station == null) {
            return;
        }
        if (favoriteStationsRepository != null && favoriteStationsRepository.isFavorite(station.getId())) {
            return;
        }
        view.showAddFavoriteDialog(station);
    }

    @Override
    public void onConfirmAddFavorite(Station station, String alias) {
        if (station == null) {
            return;
        }

        if (favoriteStationsRepository == null) {
            view.showPersistenceError();
            return;
        }

        if (favoriteStationsRepository.isFavorite(station.getId())) {
            return;
        }

        String finalAlias = alias;
        if (finalAlias == null || finalAlias.trim().isEmpty()) {
            finalAlias = station.getName() != null ? station.getName() : "";
        }

        String networkId = view != null ? view.getSelectedNetworkId() : "";

        boolean success = favoriteStationsRepository.addFavorite(station, networkId, currentNetworkLocation, finalAlias);
        if (success) {
            view.updateFavoriteIcon(station.getId(), true);
            view.showFavoriteAddedSuccess();
        } else {
            view.showPersistenceError();
        }
    }

    private void load() {
        String networkId = view.getSelectedNetworkId();

        if (networkId == null || networkId.trim().isEmpty()) {
            view.showLoadError(null);
            return;
        }

        INetworksRepository repository = view.getNetworksRepository();
        repository.requestNetwork(new NetworkDetailsCallback() {
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

                currentNetworkLocation = networkLocation;

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
        }, networkId);
    }
}

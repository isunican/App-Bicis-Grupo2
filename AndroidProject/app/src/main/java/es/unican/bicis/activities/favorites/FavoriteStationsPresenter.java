package es.unican.bicis.activities.favorites;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.unican.bicis.model.Availability;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.NetworkDetailsCallback;
import java.util.ArrayList;
import java.util.Collections;

import es.unican.bicis.common.Utils;

/**
 * The presenter of the favorite stations activity. It controls {@link FavoriteStationsView}
 */
public class FavoriteStationsPresenter implements IFavoriteStationsContract.Presenter {

    /** The view that is controlled by this presenter */
    private IFavoriteStationsContract.View view;

    /** Avoids showing the connection error more than once */
    private boolean connectionErrorShown;

    /**
     * @see IFavoriteStationsContract.Presenter#init(IFavoriteStationsContract.View)
     */
    @Override
    public void init(IFavoriteStationsContract.View view) {
        this.view = view;
        this.view.init();
        load();
    }

    /**
     * Reads the favorite stations from the repository and passes them to the view.
     * If they cannot be read, the view is told to show a persistence error.
     * Then the current availability of the stations is requested.
     */
    private void load() {
        List<FavoriteStation> favorites;
        try {
            favorites = view.getFavoriteStationsRepository().getFavoriteStations();
        } catch (RuntimeException e) {
            view.showPersistenceError();
            return;
        }
        // sort alphabetically by alias, numbers first (same criterion as the stations list)
        favorites = new ArrayList<>(favorites);
        Collections.sort(favorites, (f1, f2) -> Utils.compareNames(f1.getAlias(), f2.getAlias()));

        view.showFavoriteStations(favorites);
        loadAvailabilities(favorites);
    }

    /**
     * Requests each distinct network of the favorite stations (one request per network)
     * and passes the availability of the favorite stations to the view.
     * @param favorites the favorite stations
     */
    private void loadAvailabilities(List<FavoriteStation> favorites) {
        Set<String> networkIds = new HashSet<>();
        for (FavoriteStation favorite : favorites) {
            String networkId = favorite.getNetworkId();
            if (networkId != null && !networkId.isEmpty()) {
                networkIds.add(networkId);
            }
        }

        for (String networkId : networkIds) {
            view.getNetworksRepository().requestNetwork(new NetworkDetailsCallback() {
                @Override
                public void onSuccess(Network network) {
                    Map<String, Availability> availabilities = new HashMap<>();
                    if (network.getStations() != null) {
                        for (Station station : network.getStations()) {
                            availabilities.put(station.getId(),
                                    Availability.bikesAvailability(station.getFreeBikes()));
                        }
                    }
                    view.showAvailabilities(availabilities);
                }

                @Override
                public void onFailure(Throwable e) {
                    if (!connectionErrorShown) {
                        connectionErrorShown = true;
                        view.showConnectionError();
                    }
                }
            }, networkId);
        }
    }
}
package es.unican.bicis.activities.favorites;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.unican.bicis.common.DataAccessException;
import es.unican.bicis.model.Availability;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.NetworkDetailsCallback;
import java.util.ArrayList;
import java.util.Collections;
import es.unican.bicis.model.AlternativeStation;

import es.unican.bicis.common.Utils;

/**
 * The presenter of the favorite stations activity. It controls {@link FavoriteStationsView}
 */
public class FavoriteStationsPresenter implements IFavoriteStationsContract.Presenter {

    /** The view that is controlled by this presenter */
    private IFavoriteStationsContract.View view;

    /** Avoids showing the connection error more than once */
    private boolean connectionErrorShown;

    /** Maximum number of alternative stations that are suggested */
    private static final int MAX_ALTERNATIVES = 5;

    /** Networks loaded from the API, by network id (used to look for alternatives) */
    private final Map<String, Network> networks = new HashMap<>();

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
        } catch (RuntimeException | DataAccessException e) {
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
                    networks.put(networkId, network);
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

    /**
     * @see IFavoriteStationsContract.Presenter#buscarEstacionesAlternativas(FavoriteStation)
     */
    @Override
    public List<AlternativeStation> buscarEstacionesAlternativas(FavoriteStation favorite) {
        Network network = networks.get(favorite.getNetworkId());
        if (network == null || network.getStations() == null) {
            return null;
        }

        // look for the favorite station among the stations of its network
        Station origin = null;
        for (Station station : network.getStations()) {
            if (station.getId() != null && station.getId().equals(favorite.getStationId())) {
                origin = station;
            }
        }
        if (origin == null || origin.getLatitude() == null || origin.getLongitude() == null) {
            return null;
        }

        double[] user = view.getUserLocation();

        List<AlternativeStation> alternatives = new ArrayList<>();
        for (Station station : network.getStations()) {
            if (station == origin) {
                continue;
            }
            Availability availability = Availability.bikesAvailability(station.getFreeBikes());
            boolean hasBikes = availability == Availability.HIGH || availability == Availability.LOW;
            if (!hasBikes || station.getLatitude() == null || station.getLongitude() == null) {
                continue;
            }

            double toFavorite = Utils.distanceKm(origin.getLatitude(), origin.getLongitude(),
                    station.getLatitude(), station.getLongitude());
            Double toUser = null;
            if (user != null) {
                toUser = Utils.distanceKm(user[0], user[1],
                        station.getLatitude(), station.getLongitude());
            }
            alternatives.add(new AlternativeStation(station, toFavorite, toUser));
        }

        // closest first, and keep only the first MAX_ALTERNATIVES
        Collections.sort(alternatives,
                (a, b) -> Double.compare(a.getDistanceToFavoriteKm(), b.getDistanceToFavoriteKm()));
        return new ArrayList<>(alternatives.subList(0, Math.min(MAX_ALTERNATIVES, alternatives.size())));
    }
    /**
     * @see IFavoriteStationsContract.Presenter#onSearchAlternativesClicked(FavoriteStation)
     */
    @Override
    public void onSearchAlternativesClicked(FavoriteStation favorite) {
        if (!view.isOnline()) {
            view.showAlternativesConnectionError();
            return;
        }

        try {
            view.getFavoriteStationsRepository().getFavoriteStations();
        } catch (RuntimeException | DataAccessException e) {
            view.showAlternativesPersistenceError();
            return;
        }

        String networkId = favorite.getNetworkId();
        if (networkId == null || networkId.isEmpty()) {
            view.showAlternativesNotPossible();
            return;
        }

        view.getNetworksRepository().requestNetwork(new NetworkDetailsCallback() {
            @Override
            public void onSuccess(Network network) {
                networks.put(networkId, network);
                List<AlternativeStation> alternatives = buscarEstacionesAlternativas(favorite);
                if (alternatives == null) {
                    view.showAlternativesNotPossible();
                } else {
                    view.showAlternatives(favorite, alternatives);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                view.showAlternativesDataError();
            }
        }, networkId);
    }
}
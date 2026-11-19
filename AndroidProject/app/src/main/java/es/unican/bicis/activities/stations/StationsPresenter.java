package es.unican.bicis.activities.stations;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.unican.bicis.common.Utils;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.StationEbikesFilter;
import es.unican.bicis.repository.ICountriesList;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.common.DataAccessException;

/**
 * Presenter implementation for the stations activity.
 */
public class StationsPresenter implements IStationsContract.Presenter {

    private IStationsContract.View view;
    private IFavoriteStationsRepository favoriteStationsRepository;
    private Location currentNetworkLocation;

    /**
     * List of all the stations in the current network.
     */
    private final List<Station> allStations = new ArrayList<>();

    private final StationEbikesFilter ebikesFilter = new StationEbikesFilter();
    private boolean ebikesAvailable = false;

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

    /**
     * Attaches the presenter to the stations view and loads the selected network's stations.
     *
     * @param view view controlled by this presenter
     * @see IStationsContract.Presenter#init(IStationsContract.View)
     */
    @Override
    public void init(IStationsContract.View view) {
        this.view = view;
        this.view.init();
        if (favoriteStationsRepository == null && view instanceof Context) {
            favoriteStationsRepository = new FavoriteStationsRepository((Context) view);
        }
        load();
    }

    /**
     * Requests the view to open the information screen.
     *
     * @see IStationsContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * Handles clicks on the favorite stations menu option. Checks the connection and the
     * persistence system first; the screen is only opened if both are available,
     * otherwise the error is shown and the current screen is kept.
     *
     * @see IStationsContract.Presenter#onMenuFavoriteStationsClicked()
     */
    @Override
    public void onMenuFavoriteStationsClicked() {
        if (!view.isOnline()) {
            view.showFavoritesConnectionError();
            return;
        }
        try {
            if (favoriteStationsRepository == null) {
                view.showPersistenceError();
                return;
            }
            favoriteStationsRepository.getFavoriteStations();
        } catch (RuntimeException | DataAccessException e) {
            view.showPersistenceError();
            return;
        }
        view.showFavoriteStationsActivity();
    }

    /**
     * Requests the view to display the electric-bike filter options.
     *
     * @see IStationsContract.Presenter#onMenuFiltersClicked()
     */
    @Override
    public void onMenuFiltersClicked() {
        view.showFilterMenu(ebikesFilter.isAvailable(),
                ebikesFilter.isNotAvailable());
    }

    /**
     * Updates the selected electric-bike availability filter and refreshes the displayed stations.
     *
     * @param option filter option being changed
     * @param checked whether the option is selected
     * @see IStationsContract.Presenter#onEbikesFilterOptionToggled(IStationsContract.FilterOption, boolean)
     */
    @Override
    public void onEbikesFilterOptionToggled(IStationsContract.FilterOption option, boolean checked) {

        if (!ebikesAvailable) {
            return;
        }

        if (option == IStationsContract.FilterOption.AVAILABLE) {
            ebikesFilter.setAvailable(checked);
        } else {
            ebikesFilter.setNotAvailable(checked);
        }

        List<Station> filtered = ebikesFilter.applyFilter(allStations);
        view.showStations(filtered);
        view.showLoadCorrect(filtered.size());
        boolean onlyAvailable = ebikesFilter.isAvailable() && !ebikesFilter.isNotAvailable();
        boolean onlyNotAvailable = !ebikesFilter.isAvailable() && ebikesFilter.isNotAvailable();
        view.showFilterIndicator(onlyAvailable, onlyNotAvailable);
    }

    /**
     * Handles a favorite-star click. If the station is not already saved, asks the view
     * to display the alias-entry dialog.
     *
     * @param station station associated with the clicked favorite icon
     * @see IStationsContract.Presenter#onFavoriteIconClicked(Station)
     */
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

    /**
     * Saves the station as a favorite using the entered alias, updates the favorite icon,
     * and informs the view about the result of the persistence operation.
     *
     * @param station station to save
     * @param alias alias entered by the user
     * @see IStationsContract.Presenter#onConfirmAddFavorite(Station, String)
     */
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

    /**
     * Requests the selected network's details, sorts its stations, stores them for filtering,
     * determines whether electric-bike information is available, and updates the view with
     * the stations, network location, country flag and load result.
     * Reports an error if no network is selected or the request fails.
     */
    private void load() {
        String networkId = view.getSelectedNetworkId();

        if (networkId == null || networkId.trim().isEmpty()) {
            view.showLoadError(null);
            return;
        }

        INetworksRepository repository = view.getNetworksRepository();
        repository.requestNetwork(new NetworkDetailsCallback() {

            /**
             * Processes the successfully retrieved network by storing its stations, determining
             * electric-bike information availability, and updating the view with station and
             * network-location data.
             *
             * @param network retrieved network
             * @see NetworkDetailsCallback#onSuccess(Network)
             */
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

                Collections.sort(stations, (s1, s2) -> Utils.compareNames(s1.getName(), s2.getName()));
                allStations.addAll(stations);
                ebikesAvailable = network.providesEbikesInfo();
                view.enableEbikesFilter(ebikesAvailable);

                Location networkLocation = network.getLocation();
                if (networkLocation == null) {
                    networkLocation = new Location();
                }

                currentNetworkLocation = networkLocation;

                Map<String, Country> countries = null;
                ICountriesList countriesList = view.getCountriesList();
                if (countriesList != null) {
                    countries = countriesList.getMap();
                }

                view.showNetworkLocation(networkLocation);
                view.showStations(stations);
                view.showCountryFlag(Country.getCountryFlag(countries, networkLocation.getCountry()));
                view.showLoadCorrect(stations.size());
            }

            /**
             * Reports the network-details request failure to the view.
             *
             * @param e request failure
             * @see NetworkDetailsCallback#onFailure(Throwable)
             */
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

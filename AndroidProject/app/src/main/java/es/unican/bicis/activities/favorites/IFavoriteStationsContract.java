package es.unican.bicis.activities.favorites;

import java.util.List;
import java.util.Map;

import es.unican.bicis.model.AlternativeStation;
import es.unican.bicis.model.Availability;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the favorite stations activity.
 * This activity shows the list of stations that the user has marked as favorite.
 */
public interface IFavoriteStationsContract {

    /**
     * Methods that must be implemented in the Favorite Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view and loads the favorite stations.
         * Only the View should call this method
         * @param view the associated view class object
         */
        public void init(View view);

        /**
         * Looks for the stations closest to the given favorite station that have bikes available.
         * At most 5 are returned, ordered from the closest to the farthest. Stations without
         * bikes or without coordinates are ignored.
         * @param favorite the favorite station without availability
         * @return the alternative stations, or null if the coordinates of the favorite station
         *         are unknown
         */
        public List<AlternativeStation> buscarEstacionesAlternativas(FavoriteStation favorite);

        /**
         * The presenter is informed that the magnifier icon of a favorite station has been
         * clicked. It checks the connection and the data access, requests the network of the
         * station and shows the alternative stations or the corresponding error.
         * Only the View should call this method
         * @param favorite the favorite station without availability
         */
        public void onSearchAlternativesClicked(FavoriteStation favorite);
    }

    /**
     * Methods that must be implemented in the Favorite Stations View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * Initialize the view (toolbar and title).
         * Only the Presenter should call this method
         */
        public void init();

        /**
         * Returns the repository where the favorite stations are stored.
         * It is created in the view because it needs Android resources.
         * Only the Presenter should call this method
         * @return the favorite stations repository
         */
        public IFavoriteStationsRepository getFavoriteStationsRepository();

        /**
         * The view is requested to display the given favorite stations.
         * Only the Presenter should call this method
         * @param favorites the list of favorite stations (it may be empty)
         */
        public void showFavoriteStations(List<FavoriteStation> favorites);

        /**
         * The view is requested to display a message saying that the favorite
         * stations could not be read from the persistence system.
         * Only the Presenter should call this method
         */
        public void showPersistenceError();

        /**
         * Returns a repository that can be called by the Presenter to retrieve bike networks.
         * Only the Presenter should call this method
         * @return the networks repository handler
         */
        public INetworksRepository getNetworksRepository();

        /**
         * The view is requested to display the current availability of bikes of some stations.
         * Only the Presenter should call this method
         * @param availabilities the availability of each station, indexed by station id
         */
        public void showAvailabilities(Map<String, Availability> availabilities);

        /**
         * The view is requested to display a message saying that there is no connection
         * or that the data could not be requested.
         * Only the Presenter should call this method
         */
        public void showConnectionError();

        /**
         * Returns the current location of the user's device.
         * Only the Presenter should call this method
         * @return an array with the latitude and the longitude in degrees, or null if the
         *         location is unknown (no permission or GPS off)
         */
        public double[] getUserLocation();

        /**
         * Checks whether the device has an active internet connection.
         * Only the Presenter should call this method
         * @return true if there is an active internet connection, false otherwise
         */
        public boolean isOnline();

        /**
         * The view is requested to show the alternative stations of a favorite station.
         * Only the Presenter should call this method
         * @param favorite the favorite station without availability
         * @param alternatives the alternative stations (it may be empty)
         */
        public void showAlternatives(FavoriteStation favorite, List<AlternativeStation> alternatives);

        /**
         * The view is requested to show a message saying that the alternative stations
         * cannot be obtained for the favorite station.
         * Only the Presenter should call this method
         */
        public void showAlternativesNotPossible();

        /**
         * The view is requested to show a message saying that there is no internet connection.
         * Only the Presenter should call this method
         */
        public void showAlternativesConnectionError();

        /**
         * The view is requested to show a message saying that the data of the network could
         * not be accessed.
         * Only the Presenter should call this method
         */
        public void showAlternativesDataError();

        /**
         * The view is requested to show a message saying that the local storage could not be
         * accessed.
         * Only the Presenter should call this method
         */
        public void showAlternativesPersistenceError();
    }
}
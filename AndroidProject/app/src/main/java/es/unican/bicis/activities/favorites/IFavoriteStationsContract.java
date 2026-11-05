package es.unican.bicis.activities.favorites;

import java.util.List;

import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import java.util.Map;

import es.unican.bicis.model.Availability;
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
    }
}
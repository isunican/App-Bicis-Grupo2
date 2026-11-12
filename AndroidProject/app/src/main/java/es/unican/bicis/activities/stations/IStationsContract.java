package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.ICountriesList;


/**
 * Presenter-View contract for the bike stations activity.
 * The stations activity displays the list of bike stations for a selected network.
 */
public interface IStationsContract {

    /**
     * Options of the electric bikes filter.
     */
    enum FilterOption { AVAILABLE, NOT_AVAILABLE }

    /**
     * Methods that must be implemented by the Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Binds the presenter to its view and initiates data loading.
         * @param view the view controlled by this presenter
         */
        public void init(View view);

        /**
         * Notifies the presenter that the info menu option was clicked.
         */
        public void onMenuInfoClicked();

        /**
         * The presenter is informed that the favorite stations menu item has been clicked.
         */
        public void onMenuFavoriteStationsClicked();

        /**
         * Notifies the presenter that the filter menu option was clicked.
         */
        public void onMenuFiltersClicked();

        /**
         * Notifies the presenter that the user checked/unchecked an electric bikes filter option.
         *
         * @param option  the option modified
         * @param checked new state of the checkbox
         */
        public void onEbikesFilterOptionToggled(FilterOption option, boolean checked);

        /**
         * Notifies presenter that a station's star icon was clicked.
         *
         * @param station selected station
         */
        void onFavoriteIconClicked(Station station);

        /**
         * Confirms adding a station to favorites with an optional alias.
         *
         * @param station station to be added
         * @param alias custom alias entered by user
         */
        void onConfirmAddFavorite(Station station, String alias);
    }

    /**
     * Methods that must be implemented by the Stations View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * Initializes the view elements and layout components.
         */
        public void init();

        /**
         * Gets the repository used to access network and station data.
         *
         * @return the repository instance
         */
        public INetworksRepository getNetworksRepository();

        /**
         * Gets the ID of the network selected by the user.
         *
         * @return network ID, or null if no network was selected
         */
        public String getSelectedNetworkId();

        /**
         * Displays the list of stations in the user interface.
         *
         * @param stations list of stations to display
         */
        public void showStations(List<Station> stations);

        /**
         * Displays the network location details (country and city) in the UI.
         *
         * @param networkLocation location object containing country and city info
         */
        public void showNetworkLocation(Location networkLocation);


        /**
         * Displays an error message when station loading fails.
         *
         * @param reason brief error description, or null if unspecified
         */
        public void showLoadError(String reason);

        /**
         * Displays the total number of successfully loaded stations.
         *
         * @param stations count of loaded stations
         */
        public void showLoadCorrect(int stations);

        /**
         * Opens the main information activity.
         */
        public void showInfoActivity();


        /**
         * The view is requested to open the favorite stations activity.
         */
        public void showFavoriteStationsActivity();

        /**
         * Displays popup dialog allowing user to enter an alias for the favorite station.
         *
         * @param station selected station
         */
        void showAddFavoriteDialog(Station station);

        /**
         * Displays message notifying user that the favorite station was saved successfully.
         */
        void showFavoriteAddedSuccess();

        /**
         * Displays popup message indicating connection error during operation.
         */
        void showConnectionError();

        /**
         * Displays popup message indicating a data persistence error.
         */
        void showPersistenceError();

        /**
         * Updates the visual representation of the favorite star icon.
         *
         * @param stationId station ID
         * @param isFavorite true if marked as favorite
         */
        void updateFavoriteIcon(String stationId, boolean isFavorite);

        /**
         * Displays the flag of the network's country.
         *
         * @param flag flag emoji to display
         */
        public void showCountryFlag(String flag);


        ICountriesList getCountriesList();

        /** Checks whether the device has an active internet connection. */
        boolean isOnline();

        /** Shows an error message indicating that the favorite stations cannot be opened without connection. */
        void showFavoritesConnectionError();

        /**
         * Shows the electric bikes filter menu.
         *
         * @param ebikesAvailable  false if the network provides no ebike data
         * @param availableChecked state of the "Disponible" checkbox
         * @param notAvailableChecked state of the "No disponible" checkbox
         */
        public void showFilterMenu(boolean ebikesAvailable, boolean availableChecked, boolean notAvailableChecked);

        /**
         * Shows an indicator of the applied filters currently in the bottom bar.
         * @param active true if any filter is currently applied.
         */
        public void showFilterIndicator(boolean active);

    }
}

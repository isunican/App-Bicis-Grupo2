package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;


/**
 * Presenter-View contract for the bike stations activity.
 * The stations activity displays the list of bike stations for a selected network.
 */
public interface IStationsContract {

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
    }
}

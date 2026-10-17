package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;


public interface IStationsContract {

    /**
     * Methods that must be implemented in the Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view.
         * Only the View should call this method
         * @param view
         */
        public void init(View view);


        public void onMenuInfoClicked();

    }


    public interface View {

        /**
         * Initialize the view. Typically this should initialize all the listeners in the view.
         * Only the Presenter should call this method
         */
        public void init();

        public INetworksRepository getNetworksRepository();

        /**
         * Returns the id of the network that was selected previously by the user.
         * It will be necessary to get the networks' stations list.
         * Only the Presenter should call this method.
         * @return networkId or null if no network was selected.
         */
        public String getSelectedNetworkId();

        /**
         * The view is requested to display the given list of stations.
         * Only the Presenter should call this method.
         * @param stations
         */
        public void showStations(List<Station> stations);

        public void showNetworkLocation(Location networkLocation);


        /**
         * The view is requested to display a notification indicating that the networks
         * were not loaded correctly.
         * Only the Presenter should call this method.
         * @param reason Brief explanation on why the error occurred (it could be null).
         */
        public void showLoadError(String reason);

        public void showLoadCorrect(int stations);

        /**
         * The view is requested to open the info activity.
         * Only the Presenter should call this method
         */
        public void showInfoActivity();

    }
}

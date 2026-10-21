package es.unican.bicis.activities.main;

import android.content.Context;

import java.util.List;

import es.unican.bicis.model.Country;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Main activity.
 * The Main activity shows a list of bike sharing networks.
 */
public interface IMainContract {

    /**
     * Methods that must be implemented in the Main Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view.
         * Only the View should call this method
         * @param view the associated view class object
         * @param applicationContext a context used to access resources (e.g. the Application)
         */
        public void init(View view, Context applicationContext);

        /**
         * The presenter is informed that the search button, provided for
         * querying a list of bike networks, has been clicked.
         * Only the View should call this method
         * @param country the selected country, or null if none was selected
         * @param city the selected city, or null if none was selected
         */
        public void onSearchClicked(Country country, String city);

        /**
         * The presenter is informed that a network has been clicked
         * Only the View should call this method
         * @param network the network that has been clicked
         */
        public void onNetworkClicked(Network network);

        /**
         * The presenter is informed that the Info item in the menu has been clicked
         * Only the View should call this method
         */
        public void onMenuInfoClicked();

        /**
         * The presenter is informed that a country item has just been selected at the
         * country selector UI element (usually a spinner).
         * Only the View should call this method
         * @param country the selected country, or null if none was selected
         */
        public void onCountrySelected(Country country);
    }

    /**
     * Methods that must be implemented in the Main View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * Initialize the view. Typically this should initialize all the listeners in the view.
         * Only the Presenter should call this method
         */
        public void init();

        /**
         * Returns a repository that can be called by the Presenter to retrieve bike networks.
         * This method must be located in the view because Android resources must be accessed
         * in order to instantiate a repository (for example Internet Access). This requires
         * dependencies to Android. We want to keep the Presenter free of Android dependencies,
         * therefore the Presenter should be unable to instantiate repositories and must rely on
         * the view to create the repository.
         * Only the Presenter should call this method
         * @return the networks repository handler
         */
        public INetworksRepository getNetworksRepository();

        /**
         * The view is requested to display the given list of bike networks.
         * Only the Presenter should call this method
         * @param networks the list of networks
         */
        public void showNetworks(List<Network> networks);

        /**
         * The view is requested to populate the list of countries shown to the user.
         * Only the Presenter should call this method
         * @param countries the list of countries
         */
        public void showCountries(List<Country> countries);

        /**
         * The view is requested to display a notification indicating that no internet
         * connection is available to the application whilst being required.
         * Only the Presenter should call this method
         */
        void showConnectionError();

        /**
         * The view is requested to display a notification indicating that the networks
         * were loaded correctly.
         * Only the Presenter should call this method
         * @param networks the number of networks that were loaded
         */
        public void showLoadCorrect(int networks);

        /**
         * The view is requested to display the emoji flag of the currently selected country.
         * Only the Presenter should call this method
         * @param flag the emoji representing the country flag in the UNICODE character set
         */
        public void showCountryFlag(String flag);

        /**
         * The view is requested to display a notification indicating that the networks
         * were not loaded correctly.
         * Only the Presenter should call this method
         */
        public void showLoadError();

        /**
         * The view is requested to display the detailed view of the given network.
         * Only the Presenter should call this method
         * @param network the network
         */
        public void showNetworkDetails(Network network);

        /**
         * The view is requested to open the info activity.
         * Only the Presenter should call this method
         */
        public void showInfoActivity();

        /**
         * The view is requested to populate the list of cities shown to the user.
         * Only the Presenter should call this method
         * @param cities the list of cities
         */
        public void showCities(List<String> cities);
    }
}

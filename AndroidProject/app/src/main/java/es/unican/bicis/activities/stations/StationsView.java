package es.unican.bicis.activities.stations;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The stations view of the application.
 */
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    @Override
    public void init() {

    }

    @Override
    public INetworksRepository getNetworksRepository() {
        return null;
    }

    @Override
    public String getSelectedNetworkId() {
        return "";
    }

    @Override
    public void showStations(List<Station> stations) {

    }

    @Override
    public void showNoNetworkSelected() {

    }

    @Override
    public void showLoadError(String reason) {

    }

    @Override
    public void showLoadCorrect(int stations) {

    }

}

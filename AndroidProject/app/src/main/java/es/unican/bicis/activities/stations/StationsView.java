package es.unican.bicis.activities.stations;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The stations view of the application.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    private IStationsContract.Presenter presenter;

    @Inject
    INetworksRepository repository;

    @Override
    public void init() {

    }

    @Override
    public INetworksRepository getNetworksRepository() {
        return repository;
    }

    @Override
    public String getSelectedNetworkId() {
        return "";
    }

    @Override
    public void showStations(List<Station> stations) {

    }

    @Override
    public void showLoadError(String reason) {

    }

    @Override
    public void showLoadCorrect(int stations) {

    }

}

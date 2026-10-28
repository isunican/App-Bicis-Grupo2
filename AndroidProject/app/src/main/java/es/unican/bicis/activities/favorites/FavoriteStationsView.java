package es.unican.bicis.activities.favorites;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import es.unican.bicis.R;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.repository.IFavoriteStationsRepository;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.model.Availability;
import es.unican.bicis.repository.INetworksRepository;

/**
 * View that shows the list of the user's favorite stations.
 */
@AndroidEntryPoint
public class FavoriteStationsView extends AppCompatActivity implements IFavoriteStationsContract.View {

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository networksRepository;

    /** The adapter of the list of favorite stations */
    private FavoriteStationsArrayAdapter adapter;

    /** The presenter of this view */
    private IFavoriteStationsContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stations);

        // instantiate presenter and launch initial business logic
        presenter = new FavoriteStationsPresenter();
        presenter.init(this);
    }

    /**
     * @see IFavoriteStationsContract.View#init()
     */
    @Override
    public void init() {
        // The toolbar is explicitly declared in the layout, so set it as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);  // show back button in action bar
            bar.setTitle(R.string.favoriteStationsTitle);
        }
    }

    /**
     * @see IFavoriteStationsContract.View#getFavoriteStationsRepository()
     */
    @Override
    public IFavoriteStationsRepository getFavoriteStationsRepository() {
        return new FavoriteStationsRepository(getApplicationContext());
    }

    /**
     * @see IFavoriteStationsContract.View#showFavoriteStations(List)
     */
    @Override
    public void showFavoriteStations(List<FavoriteStation> favorites) {
        ListView list = findViewById(R.id.lvFavoriteStations);
        adapter = new FavoriteStationsArrayAdapter(this, favorites);
        list.setAdapter(adapter);

        TextView tvResults = findViewById(R.id.tvResults);
        tvResults.setText(getString(R.string.favoritesCount, favorites.size()));
    }

    /**
     * @see IFavoriteStationsContract.View#showPersistenceError()
     */
    @Override
    public void showPersistenceError() {
        Toast.makeText(this, R.string.favorite_error_persistence, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IFavoriteStationsContract.View#getNetworksRepository()
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return networksRepository;
    }

    /**
     * @see IFavoriteStationsContract.View#showAvailabilities(Map)
     */
    @Override
    public void showAvailabilities(Map<String, Availability> availabilities) {
        if (adapter != null) {
            adapter.updateAvailabilities(availabilities);
        }
    }

    /**
     * @see IFavoriteStationsContract.View#showConnectionError()
     */
    @Override
    public void showConnectionError() {
        Toast.makeText(this, R.string.favorite_error_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
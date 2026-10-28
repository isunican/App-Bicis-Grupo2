package es.unican.bicis.activities.favorites;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.R;
import es.unican.bicis.repository.FavoriteStationsRepository;
import es.unican.bicis.repository.IFavoriteStationsRepository;

/**
 * View that shows the list of the user's favorite stations.
 */
public class FavoriteStationsView extends AppCompatActivity {

    /** The repository where the favorite stations are stored */
    private IFavoriteStationsRepository favoriteStationsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stations);

        // The toolbar is explicitly declared in the layout, so set it as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);  // show back button in action bar
            bar.setTitle(R.string.favoriteStationsTitle);
        }

        favoriteStationsRepository = new FavoriteStationsRepository(getApplicationContext());
        showFavoriteStations();
    }

    /**
     * Shows the stored favorite stations (their aliases) and how many there are.
     * If there are none, the list is shown empty.
     */
    private void showFavoriteStations() {
        List<String> aliases = new ArrayList<>(favoriteStationsRepository.getFavorites().values());

        ListView list = findViewById(R.id.lvFavoriteStations);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, aliases));

        TextView tvResults = findViewById(R.id.tvResults);
        tvResults.setText(getString(R.string.favoritesCount, aliases.size()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
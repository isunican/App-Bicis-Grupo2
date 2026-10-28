package es.unican.bicis.repository;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * SharedPreferences-based implementation for permanent persistence of favorite stations.
 */
public class FavoriteStationsRepository implements IFavoriteStationsRepository {

    private static final String PREF_NAME = "favorite_stations_prefs";
    private final SharedPreferences prefs;

    /**
     * Constructs a repository instance linked to local SharedPreferences.
     *
     * @param context application context
     */
    public FavoriteStationsRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean addFavorite(String stationId, String alias) {
        if (stationId == null || stationId.trim().isEmpty()) {
            return false;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(stationId, alias != null ? alias.trim() : "");
        return editor.commit(); // Returns false if disk write fails
    }

    @Override
    public boolean isFavorite(String stationId) {
        if (stationId == null) {
            return false;
        }
        return prefs.contains(stationId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getFavorites() {
        return (Map<String, String>) prefs.getAll();
    }
}
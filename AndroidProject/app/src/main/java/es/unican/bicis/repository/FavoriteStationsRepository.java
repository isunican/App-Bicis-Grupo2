package es.unican.bicis.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Station;

/**
 * SharedPreferences-based implementation for permanent persistence of favorite stations.
 */
public class FavoriteStationsRepository implements IFavoriteStationsRepository {

    private static final String PREF_NAME = "favorite_stations_prefs";
    private final SharedPreferences prefs;
    private final Gson gson;

    /**
     * Constructs a repository instance linked to local SharedPreferences.
     *
     * @param context application context
     */
    public FavoriteStationsRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    @Override
    public boolean addFavorite(Station station, String networkId, Location location, String alias) {
        if (station == null || station.getId() == null || station.getId().trim().isEmpty()) {
            return false;
        }

        String country = location != null ? location.getCountry() : "";
        String city = location != null ? location.getCity() : "";

        FavoriteStation favStation = new FavoriteStation(
                station.getId(),
                networkId != null ? networkId : "",
                alias != null ? alias.trim() : "",
                station.getName() != null ? station.getName() : "",
                country != null ? country : "",
                city != null ? city : ""
        );

        String json = gson.toJson(favStation);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(station.getId(), json);
        return editor.commit();
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
    public Map<String, String> getFavorites() {
        Map<String, ?> allEntries = prefs.getAll();
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                String strVal = (String) val;
                try {
                    FavoriteStation fav = gson.fromJson(strVal, FavoriteStation.class);
                    if (fav != null && fav.getAlias() != null && !fav.getAlias().isEmpty()) {
                        result.put(entry.getKey(), fav.getAlias());
                    } else if (fav != null && fav.getStationName() != null) {
                        result.put(entry.getKey(), fav.getStationName());
                    } else {
                        result.put(entry.getKey(), strVal);
                    }
                } catch (JsonSyntaxException e) {
                    result.put(entry.getKey(), strVal);
                }
            }
        }
        return result;
    }

    @Override
    public List<FavoriteStation> getFavoriteStations() {
        Map<String, ?> allEntries = prefs.getAll();
        List<FavoriteStation> list = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                String strVal = (String) val;
                try {
                    FavoriteStation fav = gson.fromJson(strVal, FavoriteStation.class);
                    if (fav != null && fav.getStationId() != null) {
                        list.add(fav);
                    } else {
                        list.add(new FavoriteStation(entry.getKey(), "", strVal, strVal, "", ""));
                    }
                } catch (JsonSyntaxException e) {
                    list.add(new FavoriteStation(entry.getKey(), "", strVal, strVal, "", ""));
                }
            }
        }
        return list;
    }
}

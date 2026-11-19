package es.unican.bicis.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.unican.bicis.common.DataAccessException;
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

    /**
     * Saves a favorite station together with its network, location and display metadata.
     * The station is serialized as JSON and stored in SharedPreferences under its station ID.
     *
     * @param station station to save
     * @param networkId ID of the network containing the station
     * @param location network location used to store city and country
     * @param alias custom alias for the station; {@code null} is stored as the repository's null marker
     * @return {@code true} if the preferences commit succeeds; {@code false} otherwise
     * @see IFavoriteStationsRepository#addFavorite(Station, String, Location, String)
     */
    @Override
    public boolean addFavorite(Station station, String networkId, Location location, String alias) {
        if (alias == null) {
            alias = "NULL";
        }

        if (station == null || station.getId() == null || station.getId().trim().isEmpty()) {
            return false;
        }

        String country = location != null ? location.getCountry() : "";
        String city = location != null ? location.getCity() : "";

        FavoriteStation favStation = new FavoriteStation(
                station.getId(),
                networkId != null ? networkId : "",
                alias.trim(),
                station.getName() != null ? station.getName() : "",
                country != null ? country : "",
                city != null ? city : ""
        );

        String json = gson.toJson(favStation);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(station.getId(), json);
        return editor.commit();
    }

    /**
     * Saves a station ID and alias in SharedPreferences. This overload stores the alias
     * directly and is retained for entries without full station metadata.
     *
     * @param stationId station ID to save
     * @param alias alias to associate with the station; {@code null} is stored as the null marker
     * @return {@code true} if the preferences commit succeeds; {@code false} otherwise
     * @see IFavoriteStationsRepository#addFavorite(String, String)
     */
    @Override
    public boolean addFavorite(String stationId, String alias) {
        if (alias == null) {
            alias = "NULL";
        }

        if (stationId == null || stationId.trim().isEmpty()) {
            return false;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(stationId, alias.trim());
        return editor.commit(); // Returns false if disk write fails
    }

    /**
     * Checks whether SharedPreferences contains an entry for the given station ID.
     *
     * @param stationId station ID to check
     * @return {@code true} if the station is stored as a favorite; {@code false} if the ID is null
     *         or no entry exists
     * @see IFavoriteStationsRepository#isFavorite(String)
     */
    @Override
    public boolean isFavorite(String stationId) {
        if (stationId == null) {
            return false;
        }
        return prefs.contains(stationId);
    }

    /**
     * Reads favorite entries and returns a map of station IDs to display aliases.
     * JSON entries are parsed to retrieve the alias or station name; legacy plain-string
     * entries are preserved as aliases.
     *
     * @return map of favorite station IDs to their display names
     * @throws DataAccessException if a stored preference has an unsupported value type
     * @see IFavoriteStationsRepository#getFavorites()
     */
    @Override
    public Map<String, String> getFavorites() throws DataAccessException {
        Map<String, ?> allEntries = prefs.getAll();
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                // value may be null if the favorite has no set alias
                String strVal = null;

                if (!val.equals("NULL")) {
                    strVal = (String) val;
                }

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
            } else {
                // value is of an unexpected type due to mishandling or corruption
                throw new DataAccessException();
            }
        }
        return result;
    }

    /**
     * Reads all favorite entries and returns them as FavoriteStation objects. JSON records
     * are parsed; legacy entries are converted into records with incomplete metadata.
     *
     * @return list of saved favorite station records
     * @throws DataAccessException if a stored preference has an unsupported value type
     * @see IFavoriteStationsRepository#getFavoriteStations()
     */
    @Override
    public List<FavoriteStation> getFavoriteStations() throws DataAccessException {
        Map<String, ?> allEntries = prefs.getAll();
        List<FavoriteStation> list = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                // value may be null if the favorite has no set alias
                String strVal = null;

                if (!val.equals("NULL")) {
                    strVal = (String) val;
                }

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
            } else {
                // value is of an unexpected type due to mishandling or corruption
                throw new DataAccessException();
            }
        }
        return list;
    }
}

package es.unican.bicis.repository;

import java.util.Map;

/**
 * Repository interface for managing permanent storage of favorite stations and their custom aliases.
 */
public interface IFavoriteStationsRepository {

    /**
     * Stores a station as a favorite with an optional custom alias.
     *
     * @param stationId ID of the station to save
     * @param alias custom alias given by the user (can be null or empty)
     * @return true if saved successfully, false if a persistence error occurred
     */
    boolean addFavorite(String stationId, String alias);

    /**
     * Checks whether a station is currently marked as favorite.
     *
     * @param stationId ID of the station
     * @return true if marked as favorite, false otherwise
     */
    boolean isFavorite(String stationId);

    /**
     * Retrieves all stored favorite stations alongside their assigned aliases.
     *
     * @return map containing station IDs as keys and aliases as values
     */
    Map<String, String> getFavorites();
}
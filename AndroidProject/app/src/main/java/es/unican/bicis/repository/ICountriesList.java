package es.unican.bicis.repository;

import java.util.Map;
import es.unican.bicis.model.Country;

/**
 * A handler class to facilitate the retrieval of country names
 */
public interface ICountriesList {

    /**
     * Obtains a map of all country names around the world
     * @return a map of country names, keyed by country ID as referenced in the API
     */
    public Map<String, Country> getMap();
}

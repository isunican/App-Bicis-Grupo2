package es.unican.bicis.repository;

import java.util.Map;
import es.unican.bicis.model.Country;

public interface ICountriesList {
    public Map<String, Country> getMap();
}

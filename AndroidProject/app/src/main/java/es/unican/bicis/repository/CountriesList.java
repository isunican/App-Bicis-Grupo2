package es.unican.bicis.repository;

import android.content.Context;
import java.util.Map;
import es.unican.bicis.R;
import es.unican.bicis.common.Utils;
import es.unican.bicis.model.Country;

/**
 * Implementation of {@link ICountriesList} that provides static data loaded from a
 * local json resource instead of accessing a remote resource on the net. Given that the
 * list of countries around the world changes so infrequently, this solution sufficiently
 * serves the needs of this type of application.
 */
public class CountriesList implements ICountriesList {

    /** The context used to access the raw resources */
    private final Context context;

    /**
     * Creates a handler class that reads the countries from the local json resource
     * @param context a context used to access the resources (e.g. the Application)
     */
    public CountriesList(Context context) {
        this.context = context;
    }

    /**
     * Returns the countries stored in the local json resource
     * @see ICountriesList#getMap()
     * @return a map of country names, keyed by country ID as referenced in the API
     */
    @Override
    public Map<String, Country> getMap() {
        return Utils.parseCountries(context, R.raw.countries_es);
    }
}

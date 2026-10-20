package es.unican.bicis.repository;

import android.content.Context;
import java.util.Map;
import es.unican.bicis.R;
import es.unican.bicis.common.Utils;
import es.unican.bicis.model.Country;

public class CountriesList implements ICountriesList {

    private final Context context;

    public CountriesList(Context context) {
        this.context = context;
    }

    @Override
    public Map<String, Country> getMap() {
        return Utils.parseCountries(context, R.raw.countries_es);
    }
}

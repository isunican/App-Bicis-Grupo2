package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;
import org.parceler.Parcel;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a country as defined by the ISO 3166 standard, featuring
 * their accepted international name for the application target language, as well as
 * their flag as an emoji in the UNICODE character set.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Country {

    /** Emoji characters to be used in special situations as placeholders */
    public static final String WHITE_FLAG = "🏳️";
    public static final String GLOBE = "🌐";

    @SerializedName("name")      protected String name;
    @SerializedName("idISO")     protected String idISO;
    @SerializedName("idAPI")     protected String idAPI;
    @SerializedName("flag")      protected String flag;

    /**
     * Creates a placeholder for a country that is not in the countries list.
     *
     * @param idAPI country id as returned by the CityBikes API
     * @return a country with the id as name and a white flag
     */
    public static Country unknown(String idAPI) {
        Country country = new Country();
        country.setName(idAPI);
        country.setIdISO(idAPI);
        country.setIdAPI(idAPI);
        country.setFlag(WHITE_FLAG);
        return country;
    }

    /**
     * Finds a country by its API id, or creates an unknown one if it is not found.
     *
     * @param all   map of known countries (idAPI -> Country), may be null
     * @param idAPI country id as returned by the CityBikes API
     * @return the matching country, never null
     */
    public static Country find(Map<String, Country> all, String idAPI) {
        if (all == null) {
            return unknown(idAPI);
        }

        Country country = all.get(idAPI);
        if (country == null) {
            return unknown(idAPI);
        }

        return country;
    }


}

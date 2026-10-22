package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Model that represents the response for retrieval of country names
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Getter
@Setter
public class CountriesResponse {
    @SerializedName("countries")    private List<Country> countries;
}

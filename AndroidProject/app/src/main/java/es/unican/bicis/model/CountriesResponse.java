package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountriesResponse {
    @SerializedName("countries")    private List<Country> countries;
}

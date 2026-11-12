package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A bike sharing network (a city-level bike rental system), as defined in the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Network {

    @SerializedName("id")                   protected String id;
    @SerializedName("name")                 protected String name;
    @SerializedName("location")             protected Location location;
    @SerializedName("href")                 protected String href;
    @SerializedName("company")              protected String[] company;
    @SerializedName("gbfs_href")            protected String gbfsHref;
    @SerializedName("stations")             protected List<Station> stations;

    /** @return true if any station has information about the availability of electric bikes. */
    public boolean providesEbikesInfo() {
        if (stations == null) {
            return false;
        }
        for (Station s : stations) {
            if (s.getExtra() != null && s.getExtra().getEbikes() != null) {
                return true;
            }
        }
        return false;
    }

}
package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bike-sharing station and its bike availability information.
 */
@Parcel
@Getter
@Setter
public class Station {

    @SerializedName("id")           protected String id;
    @SerializedName("name")         protected String name;
    @SerializedName("latitude")     protected Double latitude;
    @SerializedName("longitude")    protected Double longitude;
    @SerializedName("timestamp")    protected String timestamp;
    @SerializedName("free_bikes")    protected Integer freeBikes;
    @SerializedName("empty_slots")   protected Integer emptySlots;
    @SerializedName("extra")    protected StationExtra extra;

    /**
     * Checks whether at least one electric bike is currently available at this station.
     *
     * @return {@code true} if the extra data reports more than zero electric bikes;
     *         {@code false} if the information is missing or no electric bikes are available
     */
    public boolean hasElectricBikes() {
        if (extra == null || extra.getEbikes() == null) {
            return false;
        }
        return extra.getEbikes() > 0;
    }

    /**
     * Returns the number of electric bikes currently available at this station.
     *
     * @return the reported number of electric bikes, or {@code 0} if the information is missing
     */
    public int ebikes() {
        if (extra == null || extra.getEbikes() == null) {
            return 0;
        }
        return extra.getEbikes();
    }

}

package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import lombok.Getter;
import lombok.Setter;

@Parcel
@Getter
@Setter
public class Station {

    @SerializedName("id")           protected String id;
    @SerializedName("name")         protected String name;
    @SerializedName("latitude")     protected double latitude;
    @SerializedName("longitude")    protected double longitude;
    @SerializedName("timestamp")    protected String timestamp;
    @SerializedName("free_bikes")    protected Integer freeBikes;
    @SerializedName("empty_slots")   protected Integer emptySlots;
    @SerializedName("free_ebikes")  protected Integer freeEbikes;

    /** @return true if the station has any electric bikes available. */
    public boolean hasElectricBikes() {
        return freeBikes > 0 && freeEbikes > 0;
    }

}

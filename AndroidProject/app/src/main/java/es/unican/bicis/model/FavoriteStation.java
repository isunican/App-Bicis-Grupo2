package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Data model representing a favorite bike station, storing network ID, custom alias, and location metadata.
 */
@Parcel
@Getter
@Setter
public class FavoriteStation {

    @SerializedName("station_id")   protected String stationId;
    @SerializedName("network_id")   protected String networkId;
    @SerializedName("alias")        protected String alias;
    @SerializedName("station_name") protected String stationName;
    @SerializedName("country")      protected String country;
    @SerializedName("city")         protected String city;

    /**
     * Default constructor required by Parceler and Gson.
     */
    public FavoriteStation() {
        // Default constructor
    }

    /**
     * Constructs a favorite station with network ID, custom alias, and location metadata.
     *
     * @param stationId ID of station
     * @param networkId ID of network containing this station
     * @param alias custom alias given by user
     * @param stationName original station name
     * @param country country of network
     * @param city city of network
     */
    public FavoriteStation(String stationId, String networkId, String alias, String stationName, String country, String city) {
        this.stationId = stationId;
        this.networkId = networkId;
        this.alias = alias;
        this.stationName = stationName;
        this.country = country;
        this.city = city;
    }
}

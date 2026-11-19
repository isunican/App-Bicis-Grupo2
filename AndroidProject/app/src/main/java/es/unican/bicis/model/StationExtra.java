package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Additional information reported for a bike-sharing station, including electric-bike
 * availability and whether the station provides electric bikes.
 */
@Parcel
@Getter
@Setter
public class StationExtra {
    @SerializedName("ebikes")      protected Integer ebikes;
    @SerializedName("has_ebikes")  protected Boolean hasEbikes;
}

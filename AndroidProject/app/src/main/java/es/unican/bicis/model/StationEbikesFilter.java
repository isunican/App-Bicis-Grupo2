package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Filter configuration for stations according to whether electric bikes are available.
 * If both options are selected or neither is selected, the filter does not restrict
 * the station list.
 */
@Parcel
@Getter
@Setter
public class StationEbikesFilter {
    @SerializedName("available")    protected boolean available;  // casilla «Disponible»
    @SerializedName("notAvailable") protected boolean notAvailable;   // casilla «No disponible»

    /**
     * Applies the selected availability options to the supplied stations.
     * Stations are included according to whether electric bikes are available; if the filter
     * is inactive, all stations are returned in a new list.
     *
     * @param stations stations to filter
     * @return a list containing the stations that match the current filter
     */
    public List<Station> applyFilter(List<Station> stations) {
        if ((available && notAvailable) || (!available && !notAvailable)) {
            return new ArrayList<>(stations);
        }
        List<Station> result = new ArrayList<>();
        for (Station s : stations) {
            if ((available && s.ebikes() > 0) || (!available && notAvailable && s.ebikes() <= 0)) {
                result.add(s);
            }
        }
        return result;
    }
}

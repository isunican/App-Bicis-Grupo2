package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Parcel
@Getter
@Setter
public class StationEbikesFilter {
    @SerializedName("available")    protected boolean available;  // casilla «Disponible»
    @SerializedName("notAvailable") protected boolean notAvailable;   // casilla «No disponible»

    public List<Station> applyFilter(List<Station> stations) {
        if ((available && notAvailable) || (!available && !notAvailable)) {
            return new ArrayList<>(stations);
        }
        List<Station> result = new ArrayList<>();
        for (Station s : stations) {
            if (available) {
                if (s.ebikes() > 0) {
                    result.add(s);
                }
            } else if (notAvailable) {
                if (s.ebikes() <= 0) {
                    result.add(s);
                }
            }
        }
        return result;
    }
}

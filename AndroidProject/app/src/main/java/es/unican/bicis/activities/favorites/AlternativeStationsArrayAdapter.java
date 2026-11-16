package es.unican.bicis.activities.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;

import es.unican.bicis.R;
import es.unican.bicis.model.AlternativeStation;
import es.unican.bicis.model.Availability;
import es.unican.bicis.model.Station;

/**
 * Adapter that renders each alternative station as a row of a ListView.
 */
public class AlternativeStationsArrayAdapter extends BaseAdapter {

    /** Application context */
    private final Context context;

    /** The alternative stations to be displayed */
    private final List<AlternativeStation> alternatives;

    /** Flag emoji of the country of the stations */
    private final String flag;

    /** Aliases of the stations that are also favorites, indexed by station id */
    private final Map<String, String> aliases;

    /**
     * Constructs a new adapter instance.
     * @param context application context
     * @param alternatives the alternative stations to display
     * @param flag the flag emoji of the country of the stations
     * @param aliases the aliases of the stations that are also favorites, by station id
     */
    public AlternativeStationsArrayAdapter(@NonNull Context context,
                                           @NonNull List<AlternativeStation> alternatives,
                                           String flag, @NonNull Map<String, String> aliases) {
        this.context = context;
        this.alternatives = alternatives;
        this.flag = flag;
        this.aliases = aliases;
    }

    /** @return the number of alternative stations in the list */
    @Override
    public int getCount() {
        return alternatives.size();
    }

    /**
     * @param position position of the item in the list
     * @return the alternative station at the given position
     */
    @Override
    public Object getItem(int position) {
        return alternatives.get(position);
    }

    /**
     * @param position position of the item in the list
     * @return the position itself, used as item identifier
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Builds the row of an alternative station: flag, name or alias, availability color,
     * original name and distances to the user and to the favorite station.
     * @param position position of the item in the list
     * @param convertView row view to reuse, or null
     * @param parent parent view the row will be attached to
     * @return the view of the row
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlternativeStation alternative = alternatives.get(position);
        Station station = alternative.getStation();

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.alternative_station_list_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.tvAlternativeFlag)).setText(flag);

        // alias if the station is also a favorite, otherwise its own name
        String alias = aliases.get(station.getId());
        String name = (alias != null && !alias.isEmpty()) ? alias : station.getName();
        ((TextView) convertView.findViewById(R.id.tvAlternativeName)).setText(name);

        ((TextView) convertView.findViewById(R.id.tvAlternativeOriginalName))
                .setText(station.getName());

        // green when there are many bikes, orange when there are few
        Availability availability = Availability.bikesAvailability(station.getFreeBikes());
        int color = availability == Availability.HIGH
                ? R.color.availabilityHigh : R.color.availabilityLow;
        ImageView iv = convertView.findViewById(R.id.ivAlternativeAvailability);
        iv.setColorFilter(ContextCompat.getColor(context, color));

        // distances: "- km" if the location of the user is unknown
        String toUser = alternative.getDistanceToUserKm() == null
                ? context.getString(R.string.distanceUnknown)
                : context.getString(R.string.distanceKm, alternative.getDistanceToUserKm());
        String toFavorite = formatMeters(alternative.getDistanceToFavoriteKm());
        ((TextView) convertView.findViewById(R.id.tvAlternativeDistances))
                .setText(context.getString(R.string.alternativesDistances, toUser, toFavorite));

        return convertView;
    }
    /**
     * Formats a distance given in kilometers as meters, for example "350 m".
     *
     * @param km distance in kilometers
     * @return the distance in meters as text
     */
    private String formatMeters(double km) {
        return Math.round(km * 1000) + " m";
    }
}
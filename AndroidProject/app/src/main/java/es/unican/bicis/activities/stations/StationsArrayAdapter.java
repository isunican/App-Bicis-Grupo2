package es.unican.bicis.activities.stations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.unican.bicis.R;
import es.unican.bicis.model.Availability;
import es.unican.bicis.model.Station;

/**
 * Array adapter that formats and renders individual station rows inside a ListView.
 */
public class StationsArrayAdapter extends BaseAdapter {

    /** List of stations to be displayed */
    private final List<Station> stations;

    /** Application context */
    private final Context context;

    /**
     * Constructs a new adapter instance.
     *
     * @param context application context
     * @param objects list of stations to display
     */
    public StationsArrayAdapter(@NonNull Context context, @NonNull List<Station> objects) {
        // we know the parameters are not null because of the @NonNull annotation
        this.stations = objects;
        this.context = context;
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public Object getItem(int position) {
        return stations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Station station = (Station) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.activity_stations_list_item, parent, false);
        }

        // name
        {
            TextView tv = convertView.findViewById(R.id.tvStationName);
            tv.setText(station.getName());
        }

        // last update
        {
            TextView tv = convertView.findViewById(R.id.tvUpdateDate);
            tv.setText("Última fecha de actualización: " + formatDate(station.getTimestamp()));

        }

        // availability
        {
            ImageView iv = convertView.findViewById(R.id.ivAvailability);
            Availability availability = Availability.bikesAvailability(station.getFreeBikes());
            iv.setColorFilter(ContextCompat.getColor(context, getAvailabilityColor(availability)));

        }

        return convertView;
    }

    /**
     * Converts an ISO-8601 date string into a user-friendly format (dd/MM/yyyy HH:mm).
     *
     * @param rawDate raw ISO-8601 timestamp string from the model
     * @return formatted date string, or a fallback string if null or invalid
     */
    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return context.getString(R.string.noDate);
        }
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date date = inputFormat.parse(rawDate);
            return date != null ? outputFormat.format(date) : rawDate;
        } catch (ParseException e) {
            return rawDate;
        }
    }

    /**
     * Gets the corresponding color resource ID for a given availability status.
     *
     * @param av availability status enum value
     * @return color resource ID
     */
    private int getAvailabilityColor(Availability av) {
        int color;

        switch (av) {
            case HIGH:
                color = R.color.availabilityHigh;
                break;
            case LOW:
                color = R.color.availabilityLow;
                break;
            case NONE:
                color = R.color.availabilityNone;
                break;
            default:
                return R.color.availabilityUnknown;
        }

        return color;
    }
}

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
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;

public class StationsArrayAdapter extends BaseAdapter {

    private final List<Station> stations;

    private final Context context;

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
            Availability availability = Availability.freeBikes(station.getFreeBikes());
            iv.setColorFilter(ContextCompat.getColor(context, getAvailabilityColor(availability)));

        }

        return convertView;
    }

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

    private int getAvailabilityColor(Availability av) {
        int color = -1;

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
            case UNKNOWN:
                color = R.color.availabilityUnknown;
                break;
        }

        return color;
    }
}

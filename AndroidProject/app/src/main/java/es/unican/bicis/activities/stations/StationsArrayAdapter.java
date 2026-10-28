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
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.FavoriteStationsRepository;

/**
 * Array adapter that formats and renders individual station rows with favorite action.
 */
public class StationsArrayAdapter extends BaseAdapter {

    /**
     * Listener interface for star icon click events.
     */
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Station station);
    }
    /** List of stations to be displayed */
    private final List<Station> stations;

    /** Application context */
    private final Context context;

    private final OnFavoriteClickListener favoriteClickListener;
    private final IFavoriteStationsRepository favoriteStationsRepository;

    /**
     * Constructs a new adapter instance.
     *
     * @param context application context
     * @param objects list of stations to display
     * @param listener listener for favorite action
     */
    public StationsArrayAdapter(@NonNull Context context, @NonNull List<Station> objects, OnFavoriteClickListener listener) {
        this.stations = objects;
        this.context = context;
        this.favoriteClickListener = listener;
        this.favoriteStationsRepository = new FavoriteStationsRepository(context);
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

        TextView tvName = convertView.findViewById(R.id.tvStationName);
        tvName.setText(station.getName());

        TextView tvUpdate = convertView.findViewById(R.id.tvUpdateDate);
        tvUpdate.setText(context.getString(R.string.last_update_date, formatDate(station.getTimestamp())));

        ImageView ivAv = convertView.findViewById(R.id.ivAvailability);
        Availability availability = Availability.bikesAvailability(station.getFreeBikes());
        ivAv.setColorFilter(ContextCompat.getColor(context, getAvailabilityColor(availability)));

        ImageView ivStar = convertView.findViewById(R.id.ivFavorite);
        if (ivStar != null) {
            boolean isFav = favoriteStationsRepository.isFavorite(station.getId());
            if (isFav) {
                ivStar.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                ivStar.setImageResource(android.R.drawable.btn_star_big_off);
            }

            ivStar.setOnClickListener(v -> {
                if (!isFav && favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(station);
                }
            });
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

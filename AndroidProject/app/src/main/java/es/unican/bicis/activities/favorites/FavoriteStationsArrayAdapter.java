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
import java.util.Locale;

import es.unican.bicis.R;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.FavoriteStation;

import java.util.HashMap;
import java.util.Map;

import es.unican.bicis.model.Availability;

/**
 * Adapter that renders each favorite station as a row of a ListView.
 */
public class FavoriteStationsArrayAdapter extends BaseAdapter {

    /**
     * Listener interface for the click events of the magnifier icon.
     */
    public interface OnSearchAlternativesClickListener {

        /**
         * Called when the magnifier icon of a favorite station is clicked.
         * @param favorite the favorite station
         */
        void onSearchAlternativesClick(FavoriteStation favorite);
    }

    /** Listener of the clicks on the magnifier icon */
    private final OnSearchAlternativesClickListener searchListener;

    /** List of favorite stations to be displayed */
    private final List<FavoriteStation> favorites;

    /** Application context */
    private final Context context;


    /** Current availability of the stations, indexed by station id */
    private final Map<String, Availability> availabilities = new HashMap<>();

    /**
     * Constructs a new adapter instance.
     * @param context application context
     * @param favorites list of favorite stations to display
     * @param listener listener of the clicks on the magnifier icon
     */
    public FavoriteStationsArrayAdapter(@NonNull Context context, @NonNull List<FavoriteStation> favorites,
                                        OnSearchAlternativesClickListener listener) {
        this.context = context;
        this.favorites = favorites;
        this.searchListener = listener;
    }

    /** @return the number of favorite stations in the list */
    @Override
    public int getCount() {
        return favorites.size();
    }

    /**
     * @param position position of the item in the list
     * @return the favorite station at the given position
     */
    @Override
    public Object getItem(int position) {
        return favorites.get(position);
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
     * Builds the row of a favorite station: flag, alias, availability color,
     * city, original station name and delete icon.
     * @param position position of the item in the list
     * @param convertView row view to reuse, or null
     * @param parent parent view the row will be attached to
     * @return the view of the row
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavoriteStation favorite = (FavoriteStation) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.activity_favorite_stations_list_item, parent, false);
        }

        // country flag
        {
            TextView tv = convertView.findViewById(R.id.tvFlag);
            tv.setText(flagFromCountryCode(favorite.getCountry()));
        }

        // alias (in bold in the layout)
        {
            TextView tv = convertView.findViewById(R.id.tvAlias);
            tv.setText(favorite.getAlias());
        }

        // city
        {
            TextView tv = convertView.findViewById(R.id.tvCity);
            tv.setText(favorite.getCity());
        }

        // original name of the station
        {
            TextView tv = convertView.findViewById(R.id.tvOriginalName);
            tv.setText(favorite.getStationName());
        }

        // availability: unknown until the real availability has been received
        {
            ImageView iv = convertView.findViewById(R.id.ivAvailability);
            Availability availability = availabilities.get(favorite.getStationId());
            if (availability == null) {
                availability = Availability.UNKNOWN;
            }
            iv.setColorFilter(ContextCompat.getColor(context, getAvailabilityColor(availability)));
        }
        // magnifier: only shown over the red indicator (station without bikes)
        {
            ImageView ivSearch = convertView.findViewById(R.id.ivSearchAlternatives);
            Availability availability = availabilities.get(favorite.getStationId());
            if (availability == Availability.NONE) {
                ivSearch.setVisibility(View.VISIBLE);
                ivSearch.setOnClickListener(v -> {
                    if (searchListener != null) {
                        searchListener.onSearchAlternativesClick(favorite);
                    }
                });
            } else {
                ivSearch.setVisibility(View.GONE);
                ivSearch.setOnClickListener(null);
            }
        }

        return convertView;
    }
    /**
     * Updates the availability of the stations and refreshes the list.
     * @param newAvailabilities the availability of each station, indexed by station id
     */
    public void updateAvailabilities(Map<String, Availability> newAvailabilities) {
        availabilities.putAll(newAvailabilities);
        notifyDataSetChanged();
    }

    /**
     * Gets the color resource ID for a given availability status.
     * @param av availability status
     * @return color resource ID
     */
    private int getAvailabilityColor(Availability av) {
        switch (av) {
            case HIGH:
                return R.color.availabilityHigh;
            case LOW:
                return R.color.availabilityLow;
            case NONE:
                return R.color.availabilityNone;
            default:
                return R.color.availabilityUnknown;
        }
    }

    /**
     * Converts a two-letter country code (for example "ES") into its flag emoji.
     * @param countryCode the ISO code of the country
     * @return the flag emoji, or a white flag if the code is not valid
     */
    static String flagFromCountryCode(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            return Country.WHITE_FLAG;
        }
        String code = countryCode.toUpperCase(Locale.ROOT);
        if (code.equals("UK")) {
            // the API uses UK for the United Kingdom, but the ISO code (used by flag emojis) is GB
            code = "GB";
        }
        int first = Character.codePointAt(code, 0) - 'A' + 0x1F1E6;
        int second = Character.codePointAt(code, 1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }
}